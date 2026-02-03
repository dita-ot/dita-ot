/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import static java.util.Collections.*;
import static net.sf.saxon.s9api.streams.Steps.attribute;
import static net.sf.saxon.s9api.streams.Steps.descendant;
import static org.dita.dost.chunk.ChunkOperation.Operation.COMBINE;
import static org.dita.dost.chunk.ChunkOperation.Operation.SPLIT;
import static org.dita.dost.chunk.ChunkOperation.Select.TOPIC;
import static org.dita.dost.chunk.ChunkUtils.WHITESPACE;
import static org.dita.dost.chunk.ChunkUtils.isCompatible;
import static org.dita.dost.module.ChunkModule.ROOT_CHUNK_OVERRIDE;
import static org.dita.dost.reader.ChunkMapReader.CHUNK_BY_TOPIC;
import static org.dita.dost.reader.ChunkMapReader.CHUNK_TO_CONTENT;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.DitaUtils.*;
import static org.dita.dost.util.DitaUtils.isDitaFormat;
import static org.dita.dost.util.FileUtils.getName;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Step;
import org.dita.dost.chunk.ChunkOperation.ChunkBuilder;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.*;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.w3c.dom.*;

/**
 * DITA 2.x chunk processing.
 *
 * @since 3.7
 */
public class ChunkModule extends AbstractPipelineModuleImpl {

  static final String GEN_CHUNK_PREFIX = "Chunk";
  static final String GEN_UNIQUE_PREFIX = "unique_";
  static final String SPLIT_CHUNK_DUPLICATE_SUFFIX = "1";
  private TempFileNameScheme tempFileNameScheme;
  private String rootChunkOverride;
  private boolean compatibilityMode;

  @Override
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    init(input);
    try {
      final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
      final URI mapFile = job.tempDirURI.resolve(in.uri);
      final Document mapDoc = getInputMap(mapFile);
      final Float ditaVersion = getDitaVersion(mapDoc.getDocumentElement());
      if (ditaVersion == null || ditaVersion < 2.0f) {
        if (
          compatibilityMode && isCompatible(mapDoc, rootChunkOverride != null ? Set.of(rootChunkOverride) : emptySet())
        ) {
          rewriteToCompatibilityMode(mapDoc);
          logger.debug("Process DITA 1.x chunks in compatibility mode");
        } else {
          return null;
        }
      }
      logger.info("Processing {0}", mapFile);
      final List<ChunkOperation> chunks = collectChunkOperations(mapFile, mapDoc);
      if (chunks.isEmpty()) {
        return null;
      }
      final Map<URI, URI> combineRewriteMap = processCombine(mapFile, mapDoc, chunks);
      final Map<URI, URI> splitRewriteMap = processSplit(mapFile, mapDoc, chunks);
      rewriteLinks(combineRewriteMap, splitRewriteMap);
      job.write();
    } catch (IOException e) {
      throw new DITAOTException(e);
    }
    return null;
  }

  private void rewriteToCompatibilityMode(Document mapDoc) {
    List<Element> elements = toList(mapDoc.getElementsByTagName("*"));
    for (Element elem : elements) {
      var chunkAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CHUNK);
      if (chunkAttr != null) {
        var rewrittenChunk = Stream
          .of(WHITESPACE.split(chunkAttr.getValue()))
          .filter(token -> !token.isBlank())
          .map(token ->
            switch (token) {
              case CHUNK_TO_CONTENT -> COMBINE.name;
              case CHUNK_BY_TOPIC -> SPLIT.name;
              default -> token;
            }
          )
          .collect(Collectors.joining(" "));
        chunkAttr.setValue(rewrittenChunk);
      }
    }
  }

  private void removeChunkAttributes(final Element map, final ChunkOperation.Operation operation) {
    if (getChunkValues(map).contains(operation.name)) {
      map.removeAttribute(ATTRIBUTE_NAME_CHUNK);
    }
    for (Element topicref : getChildElements(map, MAP_TOPICREF, true)) {
      if (getChunkValues(topicref).contains(operation.name)) {
        topicref.removeAttribute(ATTRIBUTE_NAME_CHUNK);
      }
    }
  }

  /**
   * Rewrite links in topics
   */
  private void rewriteLinks(final Map<URI, URI> combineRewriteMap, final Map<URI, URI> splitRewriteMap) {
    if (!combineRewriteMap.isEmpty() && !splitRewriteMap.isEmpty()) {
      final Map<URI, URI> rewriteMap = new HashMap<>(combineRewriteMap);
      rewriteMap.putAll(splitRewriteMap);
      final Map<URI, URI> rewriteMapAll = unmodifiableMap(rewriteMap);

      final Collection<FileInfo> topics = job.getFileInfo(DitaUtils::isDitaFormat);
      (parallel ? topics.parallelStream() : topics.stream()).forEach(fi -> {
          try {
            final URI uri = job.tempDirURI.resolve(fi.uri);
            final Document doc = job.getStore().getDocument(uri);
            rewriteTopicLinks(doc, uri, rewriteMapAll);
            job.getStore().writeDocument(doc, uri);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    }
  }

  /**
   * Rewrite link in a topic.
   */
  private void rewriteTopicLinks(final Document doc, final URI src, final Map<URI, URI> rewriteMap) {
    final List<Element> elements = toList(doc.getDocumentElement().getElementsByTagName("*"));
    for (Element link : elements) {
      if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
        final URI href = toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI abs = src.resolve(href);
        final URI rewrite = rewriteMap.get(abs);
        if (rewrite != null) {
          final URI rel = getRelativePath(src.resolve("."), rewrite);
          link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
        } else {
          final URI rel = getRelativePath(src.resolve("."), abs);
          link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
        }
      }
    }
  }

  /**
   * Process all combine chunks in input map.
   */
  private Map<URI, URI> processCombine(final URI mapFile, final Document mapDoc, final List<ChunkOperation> chunks)
    throws IOException {
    boolean hasCombine = chunks.stream().anyMatch(c -> c.operation().equals(COMBINE));
    if (hasCombine) {
      final Map<URI, URI> rewriteMap = new HashMap<>();
      final Set<URI> normalTopicRefs = getNormalTopicRefs(mapFile, mapDoc);
      final List<ChunkOperation> rewrittenChunks = rewriteCombineChunks(
        mapFile,
        mapDoc,
        normalTopicRefs,
        rewriteMap,
        chunks
      );
      rewriteTopicrefs(mapFile, rewrittenChunks);
      removeChunkAttributes(mapDoc.getDocumentElement(), COMBINE);
      logger.info("Writing {0}", mapFile);
      job.getStore().writeDocument(mapDoc, mapFile);
      // for each chunk
      generateChunks(rewrittenChunks, unmodifiableMap(rewriteMap));
      removeChunkSources(normalTopicRefs, rewrittenChunks);
      return unmodifiableMap(rewriteMap);
    }
    return emptyMap();
  }

  /**
   * Process all split chunks in input map.
   */
  private Map<URI, URI> processSplit(final URI mapFile, final Document mapDoc, final List<ChunkOperation> chunks)
    throws IOException {
    if (chunks.stream().anyMatch(c -> c.operation().equals(SPLIT))) {
      final List<ChunkOperation> rewrittenChunks = rewriteSplitChunks(mapFile, mapDoc, chunks);
      final Map<URI, URI> rewriteMap = new HashMap<>();
      mapDoc.getDocumentElement().removeAttribute(ATTRIBUTE_NAME_CHUNK);
      for (ChunkOperation chunk : rewrittenChunks) {
        if (chunk.operation() == SPLIT) {
          logger.info("Split {0}", chunk.src());
          final FileInfo fileInfo = job.getFileInfo(chunk.src());
          final Document doc = job.getStore().getDocument(chunk.src());
          var topicrefs = splitNestedTopic(fileInfo, doc.getDocumentElement(), chunk.topicref(), rewriteMap);
          if (doc.getDocumentElement().getTagName().equals(ELEMENT_NAME_DITA)) {
            processSplitDitabase(chunk, topicrefs);
          } else {
            processSplitTopic(chunk, doc, topicrefs, fileInfo);
          }
        }
      }
      removeChunkAttributes(mapDoc.getDocumentElement(), SPLIT);
      logger.info("Writing {0}", mapFile);
      job.getStore().writeDocument(mapDoc, mapFile);
      return rewriteMap;
    }
    return emptyMap();
  }

  private List<ChunkOperation> rewriteSplitChunks(
    final URI mapFile,
    final Document mapDoc,
    List<ChunkOperation> chunks
  ) {
    final Set<URI> normalTopicRefs = getNormalTopicRefs(mapFile, mapDoc);
    final List<ChunkOperation> res = new ArrayList<>(chunks.size());
    for (ChunkOperation chunk : chunks) {
      final ChunkBuilder builder = ChunkOperation.builder(chunk);
      if (normalTopicRefs.contains(chunk.src())) {
        builder.dst(addSuffixToPath(chunk.src(), "1"));
      } else {
        builder.dst(chunk.src());
      }
      res.add(builder.build());
    }
    return res;
  }

  private List<String> getChunkValues(Element elem) {
    var value = elem.getAttribute(ATTRIBUTE_NAME_CHUNK);
    if (value.isEmpty()) {
      return emptyList();
    }
    return List.of(WHITESPACE.split(value));
  }

  private Set<URI> getNormalTopicRefs(final URI mapFile, final Document mapDoc) {
    final Element root = mapDoc.getDocumentElement();
    if (getChunkValues(root).contains(SPLIT.name)) {
      return emptySet();
    }
    final Set<URI> res = new HashSet<>();
    res.add(mapFile);
    getNormalTopicRefsWalker(mapFile, root, res);
    return res;
  }

  private void getNormalTopicRefsWalker(final URI mapFile, final Element root, final Set<URI> res) {
    if (MAP_TOPICREF.matches(root) || MAP_MAP.matches(root)) {
      final List<String> chunk = getChunkValues(root);
      final String href = root.getAttribute(ATTRIBUTE_NAME_HREF);
      if (chunk.isEmpty() && !href.isEmpty() && isNormalProcessRole(root) && isDitaFormat(root)) {
        res.add(removeFragment(mapFile.resolve(href)));
      }
      if (!chunk.contains(COMBINE.name)) {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          final Node child = children.item(i);
          if (child.getNodeType() == Node.ELEMENT_NODE) {
            getNormalTopicRefsWalker(mapFile, (Element) child, res);
          }
        }
      }
    }
  }

  private void processSplitTopic(ChunkOperation chunk, Document doc, List<Element> topicrefs, FileInfo fileInfo)
    throws IOException {
    logger.info("Write {0}", chunk.dst());
    if (chunk.dst() != null && !Objects.equals(chunk.src(), chunk.dst())) {
      final URI src = job.tempDirURI.resolve(fileInfo.uri);
      final URI dst = chunk.dst();
      final URI tmp = job.tempDirURI.relativize(dst);
      final URI result = addSuffixToPath(fileInfo.result, SPLIT_CHUNK_DUPLICATE_SUFFIX);
      final FileInfo adoptedFileInfo = new Builder(fileInfo).uri(tmp).result(result).build();
      job.add(adoptedFileInfo);
    }
    for (Element topicref : topicrefs) {
      chunk.topicref().appendChild(topicref);
    }
    job.getStore().writeDocument(doc, chunk.dst());
  }

  private void processSplitDitabase(ChunkOperation chunk, List<Element> topicrefs) throws IOException {
    final Element parentNode = (Element) chunk.topicref().getParentNode();
    if (topicrefs.isEmpty()) {
      // move nested topicrefs of split topicref next to split topicref
      final List<Element> nestedTopicrefs = getChildElements(chunk.topicref(), MAP_TOPICREF);
      for (Element nestedTopicref : nestedTopicrefs) {
        parentNode.insertBefore(chunk.topicref().removeChild(nestedTopicref), chunk.topicref());
      }
    } else {
      // move nested topicrefs of split topicref into last generated topicref
      final List<Element> nestedTopicrefs = getChildElements(chunk.topicref(), MAP_TOPICREF);
      final Element lastTopicref = topicrefs.get(topicrefs.size() - 1);
      for (Element nestedTopicref : nestedTopicrefs) {
        lastTopicref.appendChild(chunk.topicref().removeChild(nestedTopicref));
      }
      // insert generated topicrefs next to split topicref
      for (Element topicref : topicrefs) {
        parentNode.insertBefore(topicref, chunk.topicref());
      }
    }
    // remove split topicref
    parentNode.removeChild(chunk.topicref());
    // remove ditabase
    job.remove(job.getFileInfo(chunk.src()));
    logger.info("Delete {0}", chunk.src());
    job.getStore().delete(chunk.src());
  }

  private List<Element> splitNestedTopic(
    final FileInfo fileInfo,
    final Element topic,
    final Element topicref,
    final Map<URI, URI> rewriteMap
  ) {
    topicref.removeAttribute(ATTRIBUTE_NAME_CHUNK);
    return getChildElements(topic, TOPIC_TOPIC)
      .stream()
      .map(nestedTopic -> {
        final Element nestedTopicref = topicref.getOwnerDocument().createElement(MAP_TOPICREF.localName);
        nestedTopicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());

        final List<Element> childTopicrefs = splitNestedTopic(fileInfo, nestedTopic, nestedTopicref, rewriteMap);
        for (Element childTopicref : childTopicrefs) {
          nestedTopicref.appendChild(childTopicref);
        }

        final String id = nestedTopic.getAttribute(ATTRIBUTE_NAME_ID);
        final Element removedNestedTopic = (Element) topic.removeChild(nestedTopic);
        final Document doc = xmlUtils.newDocument();
        final Element adoptedNestedTopic = (Element) doc.adoptNode(removedNestedTopic);
        doc.appendChild(adoptedNestedTopic);
        cascadeNamespaces(adoptedNestedTopic, topic);
        final URI src = job.tempDirURI.resolve(fileInfo.uri);
        final URI dst = addSuffixToPath(src, id);
        final URI tmp = job.tempDirURI.relativize(dst);
        final URI result = addSuffixToPath(fileInfo.result, id);
        final FileInfo adoptedFileInfo = new Builder(fileInfo).uri(tmp).result(result).build();
        job.add(adoptedFileInfo);
        try {
          logger.info("Write {0}", dst);
          job.getStore().writeDocument(doc, dst);
        } catch (IOException e) {
          logger.error("Failed to write {0}", dst, e);
        }

        nestedTopicref.setAttribute(ATTRIBUTE_NAME_HREF, tmp.resolve(".").relativize(tmp).toString());

        rewriteMap.put(setFragment(src, id), dst);

        return nestedTopicref;
      })
      .collect(Collectors.toList());
  }

  private String generateSuffix(String id) {
    return "_" + id;
  }

  private URI addSuffixToPath(URI src, String id) {
    return URLUtils.addSuffixToPath(src, generateSuffix(id));
  }

  private void cascadeNamespaces(Element dst, Node src) {
    if (src.getParentNode() != null) {
      cascadeNamespaces(dst, src.getParentNode());
    }
    final NamedNodeMap attributes = src.getAttributes();
    if (attributes != null) {
      for (int i = 0; i < attributes.getLength(); i++) {
        final Attr attribute = (Attr) attributes.item(i);
        if (Objects.equals(attribute.getPrefix(), XMLConstants.XMLNS_ATTRIBUTE)) {
          dst.setAttributeNode((Attr) dst.getOwnerDocument().importNode(attribute, false));
        }
      }
    }
  }

  private void init(AbstractPipelineInput input) {
    try {
      tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    tempFileNameScheme.setBaseDir(job.getInputDir());

    if (input.getAttribute(ROOT_CHUNK_OVERRIDE) != null) {
      rootChunkOverride = input.getAttribute(ROOT_CHUNK_OVERRIDE);
    }
    compatibilityMode = Boolean.parseBoolean(Configuration.configuration.get("compatibility.chunk.v2-for-v1"));
  }

  private Document getInputMap(URI mapFile) throws IOException {
    final Document doc = job.getStore().getDocument(mapFile);
    if (rootChunkOverride != null) {
      logger.debug("Use override root chunk {0}", rootChunkOverride);
      doc.getDocumentElement().setAttribute(ATTRIBUTE_NAME_CHUNK, rootChunkOverride);
    }
    return doc;
  }

  private void removeChunkSources(final Set<URI> normalTopicRefs, final List<ChunkOperation> chunks) {
    final Set<URI> sources = collectResources(chunks, ChunkOperation::src);
    final Set<URI> destinations = collectResources(chunks, ChunkOperation::dst);
    final Set<URI> removed = sources
      .stream()
      .filter(dst -> !destinations.contains(dst) && !normalTopicRefs.contains(dst))
      .collect(Collectors.toSet());
    removed.forEach(tmp -> {
      logger.info("Remove {0}", tmp);
      try {
        job.getStore().delete(tmp);
      } catch (IOException e) {
        logger.error("Failed to delete " + tmp, e);
      }
      var tmpFileInfo = job.getFileInfo(tmp);
      if (tmpFileInfo != null) {
        job.remove(tmpFileInfo);
      }
    });
    final Set<URI> added = destinations.stream().filter(dst -> !sources.contains(dst)).collect(Collectors.toSet());
    added.forEach(tmp -> {
      if (job.getFileInfo(tmp) == null) {
        final FileInfo src = chunks
          .stream()
          .filter(chunk -> chunk.src() != null && (chunk.dst() != null && removeFragment(chunk.dst()).equals(tmp)))
          .findAny()
          .flatMap(chunk -> Optional.ofNullable(job.getFileInfo(removeFragment(chunk.src()))))
          .orElse(null);
        final Builder builder = src != null ? FileInfo.builder(src) : FileInfo.builder();
        final URI dstRel = job.tempDirURI.relativize(tmp);
        if (src != null && src.result != null && src.isInput && Objects.equals(src.format, ATTR_FORMAT_VALUE_DITAMAP)) {
          var result = URI.create(replaceExtension(src.result.toString(), FILE_EXTENSION_DITA));
          builder.result(result).isInput(false);
        }
        //                final URI result = src != null && src.result != null ? src.result.resolve(tmp) : toDirURI(job.getOutputDir()).resolve(tmp);
        final FileInfo dstFi = builder
          .uri(dstRel)
          .format(ATTR_FORMAT_VALUE_DITA)
          //                        .result(result)
          .build();
        job.add(dstFi);
      }
    });
  }

  private Set<URI> collectResources(List<ChunkOperation> chunks, final Function<ChunkOperation, URI> pick) {
    final Set<URI> sources = new HashSet<>();
    collectResources(chunks, pick, sources);
    return unmodifiableSet(sources);
  }

  private void collectResources(List<ChunkOperation> chunks, final Function<ChunkOperation, URI> pick, Set<URI> res) {
    for (ChunkOperation chunk : chunks) {
      final URI uri = pick.apply(chunk);
      if (uri != null) {
        res.add(removeFragment(uri));
      }
      collectResources(chunk.children(), pick, res);
    }
  }

  /**
   * Generate combine chunks by merging topics and rewriting links.
   */
  private void generateChunks(List<ChunkOperation> chunks, Map<URI, URI> rewriteMap) {
    //            if (job.getFileInfo(dst) == null) {
    //                final FileInfo src = chunk.src != null ? job.getFileInfo(removeFragment(chunk.src, null)) : null;
    //                final FileInfo.Builder builder = src != null ? FileInfo.builder(src) : FileInfo.builder();
    //                final URI dstRel = job.tempDirURI.relativize(dst);
    //                final FileInfo dstFi = builder
    //                        .uri(dstRel)
    //                        // FIXME add result
    //                        .build();
    //                job.add(dstFi);
    //            }
    (parallel ? chunks.stream().parallel() : chunks.stream()).map(chunk -> {
        var dst = removeFragment(chunk.dst());
        var tmp = addSuffixToPath(dst, "tmp");
        logger.info("Generate chunk {0} to {1}", dst, tmp);
        try {
          //   recursively merge chunk topics
          final Document chunkDoc = merge(chunk);
          rewriteLinks(chunkDoc, chunk.src(), rewriteMap);
          chunkDoc.normalizeDocument();
          logger.info("Writing {0}", tmp);
          job.getStore().writeDocument(chunkDoc, tmp);
          return Map.entry(tmp, dst);
        } catch (IOException e) {
          logger.error("Failed to generate chunk {0}", dst, e);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .toList()
      .forEach(tmpFile -> {
        try {
          logger.info("Moving {0} to {1}", tmpFile.getKey(), tmpFile.getValue());
          job.getStore().move(tmpFile.getKey(), tmpFile.getValue());
        } catch (IOException e) {
          logger.error("Failed to move chunk {0} to {1}", tmpFile.getKey(), tmpFile.getValue(), e);
        }
      });
  }

  /**
   * Rewrite topicrefs.
   */
  private void rewriteTopicrefs(final URI mapFile, final List<ChunkOperation> chunks) {
    for (ChunkOperation chunk : chunks) {
      final URI dst = getRelativePath(mapFile.resolve("."), chunk.dst());
      if (!MAP_MAP.matches(chunk.topicref())) {
        chunk.topicref().setAttribute(ATTRIBUTE_NAME_HREF, dst.toString());
      }
      if (MAPGROUP_D_TOPICGROUP.matches(chunk.topicref())) {
        chunk.topicref().setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
      }
      rewriteTopicrefs(mapFile, chunk.children());
    }
  }

  /**
   * Based on topic moves in chunk, rewrite link in a topic.
   */
  private void rewriteLinks(final Document doc, final URI src, final Map<URI, URI> rewriteMap) {
    //    Objects.requireNonNull(src);
    final List<Element> elements = toList(doc.getDocumentElement().getElementsByTagName("*"));
    for (Element link : elements) {
      if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
        final URI href = toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI abs = src.resolve(href);
        final URI rewrite = rewriteMap.get(abs);
        if (rewrite != null) {
          final URI rel = getRelativePath(src.resolve("."), rewrite);
          link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
        } else {
          final URI rel = getRelativePath(src.resolve("."), abs);
          link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
        }
      }
    }
  }

  /**
   * Rewrite chunks and collect topic moves.
   */
  private List<ChunkOperation> rewriteCombineChunks(
    final URI mapFile,
    final Document mapDoc,
    final Set<URI> normalTopicRefs,
    final Map<URI, URI> rewriteMap,
    final List<ChunkOperation> chunks
  ) {
    return chunks
      .stream()
      .filter(chunk -> chunk.operation() == COMBINE)
      .map(chunk -> rewriteCombineChunk(mapFile, mapDoc, normalTopicRefs, rewriteMap, chunk).build())
      .toList();
  }

  private ChunkBuilder rewriteCombineChunk(
    final URI mapFile,
    final Document mapDoc,
    final Set<URI> normalTopicRefs,
    final Map<URI, URI> rewriteMap,
    final ChunkOperation rootChunk
  ) {
    String id = null;
    URI dst;
    if (MAP_MAP.matches(rootChunk.topicref())) {
      id = rootChunk.topicref().getAttribute(ATTRIBUTE_NAME_ID);
      if (id.isEmpty()) {
        id = replaceExtension(getName(mapFile.getPath()), "");
      }
      dst = URI.create(replaceExtension(mapFile.toString(), FILE_EXTENSION_DITA));
      final Collection<URI> values = rewriteMap.values();
      //            for (int i = 1; values.contains(dst); i++) {
      //                // FIXME
      //                id = generateChunkPrefix(i);
      //                dst = setFragment(rootChunk.src != null
      //                        ? setFragment(rootChunk.src, id)
      //                        : mapFile.resolve(id + FILE_EXTENSION_DITA), id);
      //            }
    } else {
      // init id
      if (rootChunk.src() != null && rootChunk.src().getFragment() != null) {
        id = rootChunk.src().getFragment();
      } else if (rootChunk.src() != null) {
        id = getRootTopicId(rootChunk.src());
      }
      if (id == null) {
        id = GEN_CHUNK_PREFIX + 1;
      }
      // init dst
      URI dstBase;
      if (rootChunk.src() == null) {
        for (int i = 1;; i++) {
          final URI res = mapFile.resolve(GEN_CHUNK_PREFIX + i + FILE_EXTENSION_DITA);
          if (rewriteMap.values().stream().anyMatch(d -> removeFragment(d).equals(res))) {
            continue;
          } else {
            dstBase = res;
            dst = dstBase;
            break;
          }
        }
      } else {
        dstBase = rootChunk.src();
        dst = dstBase;
      }
      dst = setFragment(dst, id);

      // guarantee unique dst
      final Collection<URI> values = rewriteMap.values();
      for (int i = 1; values.contains(dst) || normalTopicRefs.contains(removeFragment(dst)); i++) {
        //                if (rootChunk.src == null) {
        ////                    id = GEN_CHUNK_PREFIX + i;
        ////                } else {
        ////                    assert false;
        ////                    dstBase = rootChunk.src;
        //                }
        dst = addSuffix(dstBase, Integer.toString(i));
        dst = setFragment(dst, id);
      }
    }

    rewriteMap.put(rootChunk.src(), dst);
    if (rootChunk.src() != null) {
      rewriteMap.put(setFragment(rootChunk.src(), id), dst);
    }

    final ChunkBuilder builder = ChunkOperation
      .builder(rootChunk.operation())
      .select(rootChunk.select())
      .topicref(rootChunk.topicref())
      .src(rootChunk.src())
      .dst(dst)
      .id(id);
    for (ChunkOperation child : rootChunk.children()) {
      final ChunkBuilder childBuilder = rewriteChunkChild(rewriteMap, Objects.requireNonNull(dst), child);
      builder.addChild(childBuilder);
    }
    return builder;
  }

  private String generateChunkPrefix(int index) {
    return GEN_CHUNK_PREFIX + index;
  }

  private ChunkBuilder rewriteChunkChild(
    final Map<URI, URI> rewriteMap,
    final URI rootChunkDst,
    final ChunkOperation chunk
  ) {
    String id;
    if (chunk.src() != null && chunk.src().getFragment() != null) {
      id = chunk.src().getFragment();
    } else if (chunk.src() != null) {
      id = getRootTopicId(chunk.src());
    } else {
      id = null;
    }
    URI dst = setFragment(rootChunkDst, id);
    final Collection<URI> values = rewriteMap.values();
    for (int i = 1; id == null || values.contains(dst); i++) {
      id = generateUniquePrefix(i);
      dst = setFragment(rootChunkDst, id);
    }

    rewriteMap.put(chunk.src(), dst);
    if (chunk.src() != null) {
      rewriteMap.put(setFragment(chunk.src(), id), dst);
    }

    final ChunkBuilder builder = ChunkOperation
      .builder(chunk.operation())
      .select(chunk.select())
      .topicref(chunk.topicref())
      .src(chunk.src())
      .dst(dst)
      .id(id);
    for (ChunkOperation child : chunk.children()) {
      builder.addChild(rewriteChunkChild(rewriteMap, rootChunkDst, child));
    }
    return builder;
  }

  private String generateUniquePrefix(int index) {
    return GEN_UNIQUE_PREFIX + index;
  }

  /**
   * Get root topic ID.
   */
  private String getRootTopicId(final URI src) {
    logger.debug("Get root ID from {0}", src);
    try {
      final XdmNode node = job.getStore().getImmutableNode(src);
      final Step<XdmNode> firstTopicId = descendant(TOPIC_TOPIC.matcher()).first().then(attribute(ATTRIBUTE_NAME_ID));
      return node.select(firstTopicId).findFirst().map(XdmNode::getStringValue).orElse(null);
    } catch (IOException e) {
      logger.error("Failed to read root ID from {0}", src, e);
      return null;
    }
  }

  /**
   * Merge chunk tree into a single topic.
   */
  private Document merge(final ChunkOperation rootChunk) throws IOException {
    Document doc;
    if (rootChunk.src() != null) {
      Element dstTopic = getElement(rootChunk.src());
      if (rootChunk.select() == TOPIC) {
        dstTopic = (Element) dstTopic.cloneNode(true);
        for (Element topicChild : getChildElements(dstTopic, TOPIC_TOPIC)) {
          dstTopic.removeChild(topicChild);
        }
      }
      var dstRoot =
        switch (rootChunk.select()) {
          case TOPIC, BRANCH -> dstTopic;
          case DOCUMENT -> dstTopic.getOwnerDocument().getDocumentElement();
        };

      doc = dstTopic.getOwnerDocument();
      if (dstRoot.getNodeName().equals(ELEMENT_NAME_DITA)) {
        final Element lastChildTopic = getLastChildTopic(dstTopic);
        if (lastChildTopic != null) {
          dstTopic = lastChildTopic;
        }
        mergeTopic(rootChunk, rootChunk, dstTopic);
      } else if (MAP_MAP.matches(dstTopic)) {
        // XXX: When is this possible? At least in `map` test
        final Element navtitle = getNavtitle(rootChunk.topicref());
        doc = xmlUtils.newDocument();
        if (navtitle != null) {
          final Element ditaWrapper = createDita(doc, dstTopic);
          doc.appendChild(ditaWrapper);
          final Element topic = createTopic(doc, rootChunk.id());
          topic.appendChild(createTitle(doc, navtitle));
          ditaWrapper.appendChild(topic);
          mergeTopic(rootChunk, rootChunk, topic);
        } else {
          final Element ditaWrapper = createDita(doc, dstTopic);
          doc.appendChild(ditaWrapper);
          mergeTopic(rootChunk, rootChunk, ditaWrapper);
        }
      } else {
        final Element ditaWrapper = createDita(doc, dstTopic);
        doc.replaceChild(ditaWrapper, doc.getDocumentElement());
        if (dstRoot.getParentNode() != null) {
          // XXX: Should this clone the element
          dstRoot = (Element) dstRoot.getParentNode().removeChild(dstRoot);
        }
        ditaWrapper.appendChild(dstRoot);
        mergeTopic(rootChunk, rootChunk, dstTopic);
      }
    } else {
      final Element navtitle = getNavtitle(rootChunk.topicref());
      if (navtitle != null) {
        doc = xmlUtils.newDocument();
        final Element ditaWrapper = createDita(doc, rootChunk.topicref());
        doc.appendChild(ditaWrapper);
        final Element topic = createTopic(doc, rootChunk.id());
        topic.appendChild(createTitle(doc, navtitle));
        ditaWrapper.appendChild(topic);
        mergeTopic(rootChunk, rootChunk, topic);
      } else {
        doc = xmlUtils.newDocument();
        final Element ditaWrapper = createDita(doc, rootChunk.topicref());
        doc.appendChild(ditaWrapper);
        mergeTopic(rootChunk, rootChunk, ditaWrapper);
      }
    }
    return doc;
  }

  private Element createTitle(final Document doc, final Element src) {
    final Element title = doc.createElement(TOPIC_TITLE.localName);
    title.setAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TITLE.toString());
    final List<Node> children = toList(src.getChildNodes());
    for (Node child : children) {
      title.appendChild(doc.importNode(child, true));
    }
    return title;
  }

  private Element createDita(final Document doc, final Element src) {
    String version = src
      .getOwnerDocument()
      .getDocumentElement()
      .getAttributeNS(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION);
    return createDita(doc, version);
  }

  private Element createDita(final Document doc, final String version) {
    final Element ditaWrapper = doc.createElement(ELEMENT_NAME_DITA);
    ditaWrapper.setAttributeNS(
      DITA_NAMESPACE,
      ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION,
      Objects.requireNonNullElse(version, "2.0")
    );
    return ditaWrapper;
  }

  /**
   * Merge chunk tree fragment into a single topic.
   */
  private void mergeTopic(final ChunkOperation rootChunk, final ChunkOperation chunk, final Element dstTopic)
    throws IOException {
    for (ChunkOperation child : chunk.children()) {
      Element added = null;
      if (child.src() != null) {
        final Element root = getElement(child.src());
        if (root.getNodeName().equals(ELEMENT_NAME_DITA)) {
          final List<Element> importedTopics =
            switch (child.select()) {
              case DOCUMENT, BRANCH -> getChildElements(root, TOPIC_TOPIC)
                .stream()
                .map(topic -> (Element) dstTopic.getOwnerDocument().importNode(topic, true))
                .toList();
              case TOPIC -> getChildElements(root, TOPIC_TOPIC)
                .stream()
                .map(topic -> {
                  var imported = (Element) dstTopic.getOwnerDocument().importNode(topic, true);
                  for (Element childTopic : XMLUtils.getChildElements(imported, Constants.TOPIC_TOPIC)) {
                    imported.removeChild(childTopic);
                  }
                  return imported;
                })
                .toList();
            };
          for (final Element imported : importedTopics) {
            rewriteTopicId(imported, child);
            relativizeLinks(imported, child.src(), rootChunk.dst());
            added = (Element) dstTopic.appendChild(imported);
          }
          mergeTopic(rootChunk, child, added);
        } else {
          final Map.Entry<Element, List<Element>> imported = importSelectedTopic(root, dstTopic.getOwnerDocument(), child.select());
          rewriteTopicId(imported.getKey(), child);
          for (var i : imported.getValue()) {
            relativizeLinks(i, child.src(), rootChunk.dst());
            dstTopic.appendChild(i);
          }
//          Element selected =
//            switch (rootChunk.select()) {
//              case DOCUMENT -> {
//                if (child.src().getFragment() == null) {
//                  yield imported;
//                }
//                if (imported.getAttribute(ATTRIBUTE_NAME_ID).equals(root.getAttribute(ATTRIBUTE_NAME_ID))) {
//                  yield imported;
//                }
//                for (Element importedTopic : getChildElements(imported, TOPIC_TOPIC, true)) {
//                  if (importedTopic.getAttribute(ATTRIBUTE_NAME_ID).equals(root.getAttribute(ATTRIBUTE_NAME_ID))) {
//                    yield importedTopic;
//                  }
//                }
//                throw new RuntimeException("Unable to find matching ID B");
//              }
//              case BRANCH, TOPIC -> imported;
//            };

          mergeTopic(rootChunk, child, imported.getKey());
        }
      } else {
        final Element imported = createTopic(dstTopic.getOwnerDocument(), child.id());
        final Element navtitle = getNavtitle(child.topicref());
        if (navtitle != null) {
          imported.appendChild(createTitle(dstTopic.getOwnerDocument(), navtitle));
        }
        added = (Element) dstTopic.appendChild(imported);
        mergeTopic(rootChunk, child, added);
      }
    }
  }

  /**
   * Import selected topic and topics that are pulled along.
   *
   * @return key is selected element; value is a list of topic level topics, including selected element
   */
  private Map.Entry<Element, List<Element>> importSelectedTopic(
    Element src,
    Document dst,
    ChunkOperation.Select select
  ) {
    return switch (select) {
      case DOCUMENT -> {
        var root = src.getOwnerDocument().getDocumentElement();
        List<Element> roots = root.getNodeName().equals(ELEMENT_NAME_DITA)
          ? getChildElements(root, TOPIC_TOPIC)
          : List.of(root);
        var imported = roots.stream().map(r -> (Element) dst.importNode(r, true)).toList();
        Element selected = imported
          .stream()
          .flatMap(topic -> Stream.concat(Stream.of(topic), getChildElements(topic, TOPIC_TOPIC, true).stream()))
          .filter(topic -> topic.getAttribute(ATTRIBUTE_NAME_ID).equals(src.getAttribute(ATTRIBUTE_NAME_ID)))
          .findFirst()
          .orElse(imported.get(0));
        yield Map.entry(selected, imported);
      }
      case BRANCH -> {
        var imported = (Element) dst.importNode(src, true);
        yield Map.entry(imported, List.of(imported));
      }
      case TOPIC -> {
        var imported = (Element) dst.importNode(src, true);
        for (Element childTopic : getChildElements(imported, TOPIC_TOPIC)) {
          imported.removeChild(childTopic);
        }
        yield Map.entry(imported, List.of(imported));
      }
    };
  }

  private Element getElement(URI src) throws IOException {
    logger.info("Reading {0}", src);
    final Document chunkDoc = job.getStore().getDocument(src);
    if (src.getFragment() != null) {
      final NodeList children = chunkDoc.getElementsByTagName("*");
      for (int i = 0; i < children.getLength(); i++) {
        final Node child = children.item(i);
        if (TOPIC_TOPIC.matches(child) && ((Element) child).getAttribute(ATTRIBUTE_NAME_ID).equals(src.getFragment())) {
          return (Element) child;
        }
      }
      return null;
    }
    return chunkDoc.getDocumentElement();
  }

  private Element getLastChildTopic(final Element dita) {
    final List<Element> childElements = getChildElements(dita, TOPIC_TOPIC);
    if (childElements.isEmpty()) {
      return null;
    }
    return childElements.get(childElements.size() - 1);
  }

  private Element createTopic(final Document doc, final String id) {
    final Element imported = doc.createElement(TOPIC_TOPIC.localName);
    imported.setAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString());
    imported.setAttribute(ATTRIBUTE_NAME_ID, id);
    return imported;
  }

  private void rewriteTopicId(final Element topic, final ChunkOperation chunk) {
    //    if (Objects.equals(chunk.src().getFragment(), topic.getAttribute(ATTRIBUTE_NAME_ID))) {
    topic.setAttribute(ATTRIBUTE_NAME_ID, chunk.id());
    //    }
  }

  private void relativizeLinks(final Element topic, final URI src, final URI dst) {
    final List<Element> elements = toList(topic.getElementsByTagName("*"));
    for (Element link : elements) {
      if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
        final URI href = toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI abs = src.resolve(href);
        final URI rel = getRelativePath(dst.resolve("."), abs);
        link.setAttribute(ATTRIBUTE_NAME_HREF, rel.toString());
      }
    }
  }

  /**
   * Walk map and collect chunks.
   */
  private List<ChunkOperation> collectChunkOperations(final URI mapFile, final Document mapDoc) {
    logger.debug("Collect chunk operations");
    final List<ChunkOperation> chunks = new ArrayList<>();
    collectChunkOperations(mapFile, mapDoc.getDocumentElement(), chunks, null);
    return unmodifiableList(chunks);
  }

  private ChunkOperation.Select getChunkSelect(List<String> values) {
    for (String value : values) {
      var select = ChunkOperation.Select.of(value);
      if (select != null) {
        return select;
      }
    }
    return ChunkOperation.Select.DOCUMENT;
  }

  private void collectChunkOperations(
    final URI mapFile,
    final Element elem,
    final List<ChunkOperation> chunks,
    final ChunkOperation.Operation defaultOperation
  ) {
    List<String> chunk = getChunkValues(elem);
    if (chunk.isEmpty() && defaultOperation != null) {
      chunk = List.of(defaultOperation.name().toLowerCase());
    }
    if (chunk.contains(COMBINE.name)) {
      if (MAP_MAP.matches(elem)) {
        final URI href = URI.create(replaceExtension(mapFile.toString(), FILE_EXTENSION_DITA));
        final ChunkBuilder builder = ChunkOperation
          .builder(COMBINE)
          .select(getChunkSelect(chunk))
          .src(mapFile)
          .dst(href)
          .topicref(elem);
        //     remove contents
        //                elem.removeChild(child);
        getChildElements(elem, MAP_TOPICREF)
          .stream()
          .flatMap(child -> collectCombineChunks(mapFile, child).stream())
          .forEachOrdered(builder::addChild);
        chunks.add(builder.build());
      } else {
        final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        final URI href = hrefNode != null ? mapFile.resolve(hrefNode.getValue()) : null;
        final ChunkBuilder builder = ChunkOperation
          .builder(COMBINE)
          .select(getChunkSelect(chunk))
          .src(href)
          //                    .dst(href)
          .topicref(elem);
        //     remove contents
        //                elem.removeChild(child);
        getChildElements(elem, MAP_TOPICREF)
          .stream()
          .flatMap(child -> collectCombineChunks(mapFile, child).stream())
          .forEachOrdered(builder::addChild);
        chunks.add(builder.build());
        for (Element child : getChildElements(elem, MAP_TOPICREF)) {
          collectChunkOperations(mapFile, child, chunks, null);
        }
      }
    } else if (chunk.contains(SPLIT.name)) {
      if (MAP_MAP.matches(elem)) {
        for (Element child : getChildElements(elem, MAP_TOPICREF)) {
          collectChunkOperations(mapFile, child, chunks, SPLIT);
        }
      } else {
        final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        if (hrefNode != null) {
          final URI href = setFragment(mapFile.resolve(hrefNode.getValue()), null);
          final ChunkBuilder builder = ChunkOperation
            .builder(SPLIT)
            .select(getChunkSelect(chunk))
            .src(href)
            //                    .dst(href)
            .topicref(elem);
          //     remove contents
          //                elem.removeChild(child);
          chunks.add(builder.build());
        } else {
          logger.warn(MessageUtils.getMessage("DOTJ086W", elem.getTagName()).setLocation(elem).toString());
        }
        for (Element child : getChildElements(elem, MAP_TOPICREF)) {
          collectChunkOperations(mapFile, child, chunks, defaultOperation);
        }
      }
    } else {
      for (Element child : getChildElements(elem, MAP_TOPICREF)) {
        collectChunkOperations(mapFile, child, chunks, defaultOperation);
      }
    }
  }

  /**
   * Collect combine chunk contents.
   */
  private List<ChunkBuilder> collectCombineChunks(final URI mapFile, final Element elem) {
    final List<String> chunkAttr = getChunkValues(elem);
    if (!chunkAttr.isEmpty()) {
      if (chunkAttr.contains(COMBINE.name)) {
        return emptyList();
      } else if (chunkAttr.contains(SPLIT.name)) {
        logger.warn(MessageUtils.getMessage("DOTJ087W", String.join(" ", chunkAttr)).setLocation(elem).toString());
        elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
      } else {
        elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
      }
    }
    final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
    final Element navtitle = getNavtitle(elem);
    if (hrefNode != null && isDitaFormat(elem) && isLocalScope(elem) && isNormalProcessRole(elem)) {
      final URI href = mapFile.resolve(hrefNode.getValue());
      final ChunkBuilder builder = ChunkOperation
        .builder(COMBINE)
        .select(getChunkSelect(chunkAttr))
        .src(href)
        .topicref(elem);
      for (Element child : getChildElements(elem, MAP_TOPICREF)) {
        for (ChunkBuilder chunkBuilder : collectCombineChunks(mapFile, child)) {
          builder.addChild(chunkBuilder);
        }
      }
      return singletonList(builder);
    } else if (navtitle != null) {
      final ChunkBuilder builder = ChunkOperation.builder(COMBINE).select(getChunkSelect(chunkAttr)).topicref(elem);
      for (Element child : getChildElements(elem, MAP_TOPICREF)) {
        for (ChunkBuilder chunkBuilder : collectCombineChunks(mapFile, child)) {
          builder.addChild(chunkBuilder);
        }
      }
      return singletonList(builder);
    } else {
      return getChildElements(elem, MAP_TOPICREF)
        .stream()
        .flatMap(child -> collectCombineChunks(mapFile, child).stream())
        .collect(Collectors.toList());
    }
  }

  private Element getNavtitle(final Element topicref) {
    if (topicref != null) {
      return getChildElement(topicref, MAP_TOPICMETA)
        .flatMap(topicmeta -> getChildElement(topicmeta, TOPIC_NAVTITLE))
        .orElse(null);
    }
    return null;
  }

  private Element getRootTopic(final Element topic) {
    Element root = topic.getOwnerDocument().getDocumentElement();
    if (true || !root.getNodeName().equals(ELEMENT_NAME_DITA)) {
      return root;
    }
    Element current = topic;
    while (true) {
      Node parent = current.getParentNode();
      if (parent == null || parent instanceof Document) {
        return current;
      } else if (parent instanceof Element parentElement && parentElement.getNodeName().equals(ELEMENT_NAME_DITA)) {
        return current;
      } else {
        current = (Element) parent;
      }
    }
  }
}
