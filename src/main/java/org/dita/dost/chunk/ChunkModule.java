/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Step;
import org.dita.dost.chunk.ChunkOperation.ChunkBuilder;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.URLUtils;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static net.sf.saxon.s9api.streams.Steps.attribute;
import static net.sf.saxon.s9api.streams.Steps.descendant;
import static org.dita.dost.chunk.ChunkOperation.Operation.COMBINE;
import static org.dita.dost.chunk.ChunkOperation.Operation.SPLIT;
import static org.dita.dost.module.ChunkModule.ROOT_CHUNK_OVERRIDE;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getName;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

public class ChunkModule extends AbstractPipelineModuleImpl {

    static final String GEN_CHUNK_PREFIX = "Chunk";
    static final String GEN_UNIQUE_PREFIX = "unique_";
    static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.\\w+?$");
    static final String SPLIT_CHUNK_DUPLICATE_SUFFIX = "1";
    private TempFileNameScheme tempFileNameScheme;
    private String rootChunkOverride;

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        init(input);
        try {
            // read modifiable map
            final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            final URI mapFile = job.tempDirURI.resolve(in.uri);
            logger.info("Processing {0}", mapFile);
            final Document mapDoc = getInputMap(mapFile);
            final Float ditaVersion = getDitaVersion(mapDoc.getDocumentElement());
            if (ditaVersion == null || ditaVersion < 2.0f) {
                return null;
            }
            final List<ChunkOperation> chunks = collectChunkOperations(mapFile, mapDoc);
            final Map<URI, URI> combineRewriteMap = processCombine(mapFile, mapDoc, chunks);
            final Map<URI, URI> splitRewriteMap = processSplit(mapFile, mapDoc, chunks);
            rewriteLinks(combineRewriteMap, splitRewriteMap);
            job.write();
        } catch (IOException e) {
            throw new DITAOTException(e);
        }
        return null;
    }

    /**
     * Rewrite links in topics
     */
    private void rewriteLinks(Map<URI, URI> combineRewriteMap, Map<URI, URI> splitRewriteMap) {
        if (!combineRewriteMap.isEmpty() && !splitRewriteMap.isEmpty()) {
            final Map<URI, URI> rewriteMap = new HashMap<>(combineRewriteMap);
            rewriteMap.putAll(splitRewriteMap);
            job.getFileInfo(fi -> fi.format.equals(ATTR_FORMAT_VALUE_DITA)).forEach(fi -> {
                try {
                    final URI uri = job.tempDirURI.resolve(fi.uri);
                    final Document doc = job.getStore().getDocument(uri);
                    rewriteTopicLinks(
                            doc,
                            uri,
                            rewriteMap);
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
    private void rewriteTopicLinks(final Document doc,
                                   final URI src,
                                   final Map<URI, URI> rewriteMap) {
        final List<Element> elements = toList(doc.getDocumentElement().getElementsByTagName("*"));
        for (Element link : elements) {
            if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
                final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
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
    private Map<URI, URI> processCombine(final URI mapFile, final Document mapDoc, final List<ChunkOperation> chunks) throws IOException {
        // walk topicref | map
        if (chunks.stream().anyMatch(c -> c.operation.equals(COMBINE))) {
            final Map<URI, URI> rewriteMap = new HashMap<>();
            final List<ChunkOperation> rewrittenChunks = rewriteCombineChunks(mapFile, mapDoc, rewriteMap, chunks);
            rewriteTopicrefs(mapFile, rewrittenChunks);
            logger.info("Writing {0}", mapFile);
            job.getStore().writeDocument(mapDoc, mapFile);
            // for each chunk
            generateChunks(rewrittenChunks, unmodifiableMap(rewriteMap));
            removeChunkSources(rewrittenChunks);
            return unmodifiableMap(rewriteMap);
        }
        return emptyMap();
    }

    /**
     * Process all split chunks in input map.
     */
    private Map<URI, URI> processSplit(final URI mapFile, final Document mapDoc, final List<ChunkOperation> chunks) throws IOException {
        if (chunks.stream().anyMatch(c -> c.operation.equals(SPLIT))) {
            final List<ChunkOperation> rewrittenChunks = rewriteSplitChunks(mapFile, mapDoc, chunks);
            final Map<URI, URI> rewriteMap = new HashMap<>();
            mapDoc.getDocumentElement().removeAttribute(ATTRIBUTE_NAME_CHUNK);
            for (ChunkOperation chunk : rewrittenChunks) {
                if (chunk.operation == SPLIT) {
                    logger.info("Split {0}", chunk.src);
                    final FileInfo fileInfo = job.getFileInfo(chunk.src);
                    final Document doc = job.getStore().getDocument(chunk.src);
                    var topicrefs = splitNestedTopic(fileInfo, doc.getDocumentElement(), chunk.topicref, rewriteMap);
                    if (doc.getDocumentElement().getTagName().equals(ELEMENT_NAME_DITA)) {
                        processSplitDitabase(chunk, topicrefs);
                    } else {
                        processSplitTopic(chunk, doc, topicrefs, fileInfo);
                    }
                }
            }
            job.getStore().writeDocument(mapDoc, mapFile);
            return rewriteMap;
        }
        return emptyMap();
    }

    private List<ChunkOperation> rewriteSplitChunks(final URI mapFile, final Document mapDoc, List<ChunkOperation> chunks) {
        final Set<URI> normalTopicRefs = getNormalTopicRefs(mapFile, mapDoc);
        final List<ChunkOperation> res = new ArrayList<>(chunks.size());
        for (ChunkOperation chunk : chunks) {
            final ChunkBuilder builder = new ChunkBuilder(chunk);
            if (normalTopicRefs.contains(chunk.src)) {
                builder.dst(addSuffixToPath(chunk.src, "1"));
            } else {
                builder.dst(chunk.src);
            }
            res.add(builder.build());
        }
        return res;
    }

    private Set<URI> getNormalTopicRefs(final URI mapFile, final Document mapDoc) {
        if (mapDoc.getDocumentElement().getAttribute(ATTRIBUTE_NAME_CHUNK).equals(SPLIT.name)) {
            return emptySet();
        }
        return getChildElements(mapDoc.getDocumentElement(), MAP_TOPICREF, true).stream()
                .filter(topicref -> topicref.getAttribute(ATTRIBUTE_NAME_CHUNK).isEmpty() &&
                        !topicref.getAttribute(ATTRIBUTE_NAME_HREF).isEmpty() &&
                        isNormalProcessRole(topicref))
                .map(topicref -> setFragment(mapFile.resolve(topicref.getAttribute(ATTRIBUTE_NAME_HREF)), null))
                .collect(Collectors.toSet());
    }


    private void processSplitTopic(ChunkOperation chunk, Document doc, List<Element> topicrefs, FileInfo fileInfo) throws IOException {
        logger.info("Write {0}", chunk.dst);
        if (chunk.dst != null && !Objects.equals(chunk.src, chunk.dst)) {
            final URI src = job.tempDirURI.resolve(fileInfo.uri);
            final URI dst = chunk.dst;
            final URI tmp = job.tempDirURI.relativize(dst);
            //FIXME
            final URI result = addSuffixToPath(fileInfo.result, SPLIT_CHUNK_DUPLICATE_SUFFIX);
            final FileInfo adoptedFileInfo = new Builder(fileInfo)
                    .uri(tmp)
                    .result(result)
                    .build();
            job.add(adoptedFileInfo);
        }
        for (Element topicref : topicrefs) {
            chunk.topicref.appendChild(topicref);
        }
        job.getStore().writeDocument(doc, chunk.dst);
    }

    private void processSplitDitabase(ChunkOperation chunk, List<Element> topicrefs) throws IOException {
        final Element parentNode = (Element) chunk.topicref.getParentNode();
        if (topicrefs.isEmpty()) {
            // move nested topicrefs of split topicref next to split topicref
            final List<Element> nestedTopicrefs = getChildElements(chunk.topicref, MAP_TOPICREF);
            for (Element nestedTopicref : nestedTopicrefs) {
                parentNode.insertBefore(chunk.topicref.removeChild(nestedTopicref), chunk.topicref);
            }
        } else {
            // move nested topicrefs of split topicref into last generated topicref
            final List<Element> nestedTopicrefs = getChildElements(chunk.topicref, MAP_TOPICREF);
            final Element lastTopicref = topicrefs.get(topicrefs.size() - 1);
            for (Element nestedTopicref : nestedTopicrefs) {
                lastTopicref.appendChild(chunk.topicref.removeChild(nestedTopicref));
            }
            // insert generated topicrefs next to split topicref
            for (Element topicref : topicrefs) {
                parentNode.insertBefore(topicref, chunk.topicref);
            }
        }
        // remove split topicref
        parentNode.removeChild(chunk.topicref);
        // remove ditabase
        job.remove(job.getFileInfo(chunk.src));
        logger.info("Delete {0}", chunk.src);
        job.getStore().delete(chunk.src);
    }

    private List<Element> splitNestedTopic(final FileInfo fileInfo,
                                           final Element topic,
                                           final Element topicref,
                                           final Map<URI, URI> rewriteMap) {
        topicref.removeAttribute(ATTRIBUTE_NAME_CHUNK);
        return getChildElements(topic, TOPIC_TOPIC).stream()
                .map(nestedTopic -> {
                    final Element nestedTopicref = topicref.getOwnerDocument().createElement(MAP_TOPICREF.localName);
                    nestedTopicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());

                    final List<Element> childTopicrefs = splitNestedTopic(fileInfo, nestedTopic, nestedTopicref, rewriteMap);
                    for (Element childTopicref : childTopicrefs) {
                        nestedTopicref.appendChild(childTopicref);
                    }

                    final String id = nestedTopic.getAttribute(ATTRIBUTE_NAME_ID);
                    final Element removedNestedTopic = (Element) topic.removeChild(nestedTopic);
                    final Document doc = xmlUtils.getDocumentBuilder().newDocument();
                    final Element adoptedNestedTopic = (Element) doc.adoptNode(removedNestedTopic);
                    doc.appendChild(adoptedNestedTopic);
                    cascadeNamespaces(adoptedNestedTopic, topic);
                    final URI src = job.tempDirURI.resolve(fileInfo.uri);
                    final URI dst = addSuffixToPath(src, id);
                    final URI tmp = job.tempDirURI.relativize(dst);
                    final URI result = addSuffixToPath(fileInfo.result, id);
                    final FileInfo adoptedFileInfo = new Builder(fileInfo)
                            .uri(tmp)
                            .result(result)
                            .build();
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
        final String suffix = generateSuffix(id);
        final Matcher matcher = EXTENSION_PATTERN.matcher(src.getPath());
        if (matcher.find()) {
            return setPath(src, matcher.replaceAll(suffix + "$0"));
        } else {
            return setPath(src, src.getPath() + suffix);
        }
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
    }

    private Document getInputMap(URI mapFile) throws IOException {
        final Document doc = job.getStore().getDocument(mapFile);
        if (rootChunkOverride != null) {
            logger.debug("Use override root chunk {0}", rootChunkOverride);
            doc.getDocumentElement().setAttribute(ATTRIBUTE_NAME_CHUNK, rootChunkOverride);
        }
        return doc;
    }

    private void removeChunkSources(List<ChunkOperation> chunks) {
        final Set<URI> sources = collectResources(chunks, chunk -> chunk.src);
        final Set<URI> destinations = collectResources(chunks, chunk -> chunk.dst);
        final Set<URI> removed = sources.stream()
                .filter(dst -> !destinations.contains(dst))
                .collect(Collectors.toSet());
        removed.forEach(tmp -> {
            logger.info("Remove {0}", tmp);
            try {
                job.getStore().delete(tmp);
            } catch (IOException e) {
                logger.error("Failed to delete " + tmp, e);
            }
            job.remove(job.getFileInfo(tmp));
        });
        final Set<URI> added = destinations.stream()
                .filter(dst -> !sources.contains(dst))
                .collect(Collectors.toSet());
        added.forEach(tmp -> {
            if (job.getFileInfo(tmp) == null) {
                final FileInfo src = chunks.stream()
                        .filter(chunk ->
                                chunk.src != null &&
                                        (chunk.dst != null && removeFragment(chunk.dst).equals(tmp))
                        )
                        .findAny()
                        .flatMap(chunk -> Optional.ofNullable(job.getFileInfo(removeFragment(chunk.src))))
                        .orElse(null);
                final Builder builder = src != null ? FileInfo.builder(src) : FileInfo.builder();
                final URI dstRel = job.tempDirURI.relativize(tmp);
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
            collectResources(chunk.children, pick, res);
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
        (parallel ? chunks.stream().parallel() : chunks.stream())
                .forEach(chunk -> {
                    logger.info("Generate chunk {0}", removeFragment(chunk.dst));
                    try {
                        //   recursively merge chunk topics
                        final Document chunkDoc = merge(chunk);
                        rewriteLinks(chunkDoc, chunk.src, rewriteMap);
                        chunkDoc.normalizeDocument();
                        final URI dst = removeFragment(chunk.dst);
                        logger.info("Writing {0}", dst);
                        job.getStore().writeDocument(chunkDoc, dst);
                    } catch (IOException e) {
                        logger.error("Failed to generate chunk {0}", removeFragment(chunk.dst), e);
                    }
                });
    }

    /**
     * Rewrite topicrefs.
     */
    private void rewriteTopicrefs(final URI mapFile, final List<ChunkOperation> chunks) {
        for (ChunkOperation chunk : chunks) {
            final URI dst = getRelativePath(mapFile.resolve("."), chunk.dst);
            if (!MAP_MAP.matches(chunk.topicref)) {
                chunk.topicref.setAttribute(ATTRIBUTE_NAME_HREF, dst.toString());
            }
            if (MAPGROUP_D_TOPICGROUP.matches(chunk.topicref)) {
                chunk.topicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
            }
            rewriteTopicrefs(mapFile, chunk.children);
        }
    }

    /**
     * Based on topic moves in chunk, rewrite link in a topic.
     */
    private void rewriteLinks(final Document doc,
                              final URI src,
                              final Map<URI, URI> rewriteMap) {
        final List<Element> elements = toList(doc.getDocumentElement().getElementsByTagName("*"));
        for (Element link : elements) {
            if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
                final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
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
    private List<ChunkOperation> rewriteCombineChunks(final URI mapFile,
                                                      final Document mapDoc,
                                                      final Map<URI, URI> rewriteMap,
                                                      final List<ChunkOperation> chunks) {
        return chunks.stream()
                .filter(chunk -> chunk.operation == COMBINE)
                .map(chunk -> rewriteCombineChunk(mapFile, mapDoc, rewriteMap, chunk).build())
                .collect(Collectors.toUnmodifiableList());
    }

    private ChunkBuilder rewriteCombineChunk(final URI mapFile,
                                             final Document mapDoc,
                                             final Map<URI, URI> rewriteMap,
                                             final ChunkOperation rootChunk) {
        final Set<URI> normalTopicRefs = getNormalTopicRefs(mapFile, mapDoc);
        String id = null;
        URI dst;
        if (MAP_MAP.matches(rootChunk.topicref)) {
            id = rootChunk.topicref.getAttribute(ATTRIBUTE_NAME_ID);
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
            if (rootChunk.src != null && rootChunk.src.getFragment() != null) {
                id = rootChunk.src.getFragment();
            } else if (rootChunk.src != null) {
                id = getRootTopicId(rootChunk.src);
            }
            if (id == null) {
                id = GEN_CHUNK_PREFIX + 1;
            }
            // init dst
            if (rootChunk.src == null) {
                dst = mapFile.resolve(id + FILE_EXTENSION_DITA);
            } else {
                dst = rootChunk.src;
            }
            dst = setFragment(dst, id);
            final Collection<URI> values = rewriteMap.values();
            for (int i = 1; values.contains(dst); i++) {
                id = GEN_CHUNK_PREFIX + i;
                if (rootChunk.src == null) {
                    dst = mapFile.resolve(id + FILE_EXTENSION_DITA);
                } else {
                    dst = rootChunk.src;
                }
                dst = setFragment(dst, id);
            }
        }

        rewriteMap.put(rootChunk.src, dst);
        if (rootChunk.src != null) {
            rewriteMap.put(setFragment(rootChunk.src, id), dst);
        }

        final ChunkBuilder builder = new ChunkBuilder(rootChunk.operation)
                .topicref(rootChunk.topicref)
                .src(rootChunk.src)
                .dst(dst)
                .id(id);
        for (ChunkOperation child : rootChunk.children) {
            final ChunkBuilder childBuilder = rewriteChunkChild(
                    rewriteMap,
                    rootChunk.src != null ? rootChunk.src : dst,
                    child);
            builder.addChild(childBuilder);
        }
        return builder;
    }

    private String generateChunkPrefix(int index) {
        return GEN_CHUNK_PREFIX + index;
    }

    private ChunkBuilder rewriteChunkChild(final Map<URI, URI> rewriteMap,
                                           final URI rootChunkSrc,
                                           final ChunkOperation chunk) {
        String id;
        if (chunk.src != null && chunk.src.getFragment() != null) {
            id = chunk.src.getFragment();
        } else if (chunk.src != null) {
            id = getRootTopicId(chunk.src);
        } else {
            id = null;
        }
        URI dst = setFragment(rootChunkSrc, id);
        final Collection<URI> values = rewriteMap.values();
        for (int i = 1; id == null || values.contains(dst); i++) {
            id = generateUniquePrefix(i);
            dst = setFragment(rootChunkSrc, id);
        }

        rewriteMap.put(chunk.src, dst);
        if (chunk.src != null) {
            rewriteMap.put(setFragment(chunk.src, id), dst);
        }

        final ChunkBuilder builder = new ChunkBuilder(chunk.operation)
                .topicref(chunk.topicref)
                .src(chunk.src)
                .dst(dst)
                .id(id);
        for (ChunkOperation child : chunk.children) {
            builder.addChild(rewriteChunkChild(rewriteMap, rootChunkSrc, child));
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
            final Step<XdmNode> firstTopicId = descendant(TOPIC_TOPIC.matcher()).first()
                    .then(attribute(ATTRIBUTE_NAME_ID));
            return node.select(firstTopicId)
                    .findFirst()
                    .map(XdmNode::getStringValue)
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Failed to read root ID from {0}", src, e);
            return null;
        }
    }

    /**
     * Merge chunk tree into a single topic.
     */
    private Document merge(final ChunkOperation rootChunk) throws IOException {
        final Document doc;
        if (rootChunk.src != null) {
            Element dstTopic = getElement(rootChunk.src);
            doc = dstTopic.getOwnerDocument();
            if (dstTopic.getNodeName().equals(ELEMENT_NAME_DITA)) {
                final Element lastChildTopic = getLastChildTopic(dstTopic);
                if (lastChildTopic != null) {
                    dstTopic = lastChildTopic;
                }
            } else {
                final Element ditaWrapper = createDita(doc);
                doc.replaceChild(ditaWrapper, doc.getDocumentElement());
                if (dstTopic.getParentNode() != null) {
                    dstTopic = (Element) dstTopic.getParentNode().removeChild(dstTopic);
                }
                ditaWrapper.appendChild(dstTopic);
            }
            mergeTopic(rootChunk, rootChunk, dstTopic);
        } else {
            final Element navtitle = getNavtitle(rootChunk.topicref);
            if (navtitle != null) {
                doc = getDocumentBuilder().newDocument();
                final Element ditaWrapper = createDita(doc);
                doc.appendChild(ditaWrapper);
                final Element topic = createTopic(doc, rootChunk.id);
                topic.appendChild(createTitle(doc, navtitle));
                ditaWrapper.appendChild(topic);
                mergeTopic(rootChunk, rootChunk, topic);
            } else {
                doc = getDocumentBuilder().newDocument();
                final Element ditaWrapper = createDita(doc);
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

    private Element createDita(final Document doc) {
        final Element ditaWrapper = doc.createElement(ELEMENT_NAME_DITA);
        ditaWrapper.setAttributeNS(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "2.0");
        return ditaWrapper;
    }

    /**
     * Merge chunk tree fragment into a single topic.
     */
    private void mergeTopic(final ChunkOperation rootChunk,
                            final ChunkOperation chunk,
                            final Element dstTopic) throws IOException {
        for (ChunkOperation child : chunk.children) {
            Element added;
            if (child.src != null) {
                final Element root = getElement(child.src);
                if (root.getNodeName().equals(ELEMENT_NAME_DITA)) {
                    final List<Element> rootTopics = getChildElements(root, TOPIC_TOPIC);
                    int i = 1;
                    for (final Element topic : rootTopics) {
                        final Element imported;
                        imported = (Element) dstTopic.getOwnerDocument().importNode(topic, true);
                        rewriteTopicId(imported, child.id);
                        relativizeLinks(imported, child.src, rootChunk.dst);
                        added = (Element) dstTopic.appendChild(imported);
//                        if (i++ == rootTopics.size()) {
//                        }
                    }
                    mergeTopic(rootChunk, child, dstTopic);
                } else {
                    final Element imported = (Element) dstTopic.getOwnerDocument().importNode(root, true);
                    rewriteTopicId(imported, child.id);
                    relativizeLinks(imported, child.src, rootChunk.dst);
                    added = (Element) dstTopic.appendChild(imported);
                    mergeTopic(rootChunk, child, added);
                }
            } else {
                final Element imported = createTopic(dstTopic.getOwnerDocument(), child.id);
                final Element navtitle = getNavtitle(child.topicref);
                if (navtitle != null) {
                    imported.appendChild(createTitle(dstTopic.getOwnerDocument(), navtitle));
                }
                added = (Element) dstTopic.appendChild(imported);
                mergeTopic(rootChunk, child, added);
            }
        }
    }

    private Element getElement(URI src) throws IOException {
        logger.info("Reading {0}", src);
        final Document chunkDoc = job.getStore().getDocument(src);
        if (src.getFragment() != null) {
            final NodeList children = chunkDoc.getElementsByTagName("*");
            for (int i = 0; i < children.getLength(); i++) {
                final Node child = children.item(i);
                if (TOPIC_TOPIC.matches(child)
                        && ((Element) child).getAttribute(ATTRIBUTE_NAME_ID).equals(src.getFragment())) {
                    return (Element) child;
                }
            }
            return null;
        } else {
            return chunkDoc.getDocumentElement();
        }
    }

//    private List<Element> getRootTopics(final Document chunkDoc) {
//        final Element root = chunkDoc.getDocumentElement();
//        if (root.getNodeName().equals(ELEMENT_NAME_DITA)) {
//            return getChildElements(root, TOPIC_TOPIC);
//        } else {
//            return Collections.singletonList(root);
//        }
//    }

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

    private void rewriteTopicId(final Element topic, final String id) {
        topic.setAttribute(ATTRIBUTE_NAME_ID, id);
    }

    private void relativizeLinks(final Element topic, final URI src, final URI dst) {
        final List<Element> elements = toList(topic.getElementsByTagName("*"));
        for (Element link : elements) {
            if (TOPIC_LINK.matches(link) || TOPIC_XREF.matches(link)) {
                final URI href = URLUtils.toURI(link.getAttribute(ATTRIBUTE_NAME_HREF));
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

    private void collectChunkOperations(final URI mapFile,
                                        final Element elem,
                                        final List<ChunkOperation> chunks,
                                        final ChunkOperation.Operation defaultOperation) {
        String chunk = elem.getAttribute(ATTRIBUTE_NAME_CHUNK);
        if (chunk.isEmpty() && defaultOperation != null) {
            chunk = defaultOperation.name().toLowerCase();
        }
        if (chunk.equals(COMBINE.name)) {
            if (MAP_MAP.matches(elem)) {
                //     create chunk
                final URI href = URI.create(replaceExtension(mapFile.toString(), FILE_EXTENSION_DITA));
                final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                        //.src(href)
                        .dst(href)
                        .topicref(elem);
                //     remove contents
                //                elem.removeChild(child);
                getChildElements(elem, MAP_TOPICREF).stream()
                        .flatMap(child -> collectCombineChunks(mapFile, child).stream())
                        .forEachOrdered(builder::addChild);
                // remove @chunk
                elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
                chunks.add(builder.build());
            } else {
                //     create chunk
                final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
                final URI href = hrefNode != null ? mapFile.resolve(hrefNode.getValue()) : null;
                final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                        .src(href)
//                    .dst(href)
                        .topicref(elem);
                //     remove contents
                //                elem.removeChild(child);
                getChildElements(elem, MAP_TOPICREF).stream()
                        .flatMap(child -> collectCombineChunks(mapFile, child).stream())
                        .forEachOrdered(builder::addChild);
                // remove @chunk
                elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
                chunks.add(builder.build());
            }
        } else if (chunk.equals(SPLIT.name)) {
            // split every topicref
            if (MAP_MAP.matches(elem)) {
                // split default for topicrefs
                for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                    collectChunkOperations(mapFile, child, chunks, SPLIT);
                }
            } else {
                //     create chunk
                final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
                if (hrefNode != null) {
                    final URI href = setFragment(mapFile.resolve(hrefNode.getValue()), null);
                    final ChunkBuilder builder = new ChunkBuilder(SPLIT)
                            .src(href)
//                    .dst(href)
                            .topicref(elem);
                    //     remove contents
                    //                elem.removeChild(child);
//                getChildElements(elem, MAP_TOPICREF).stream()
//                        .flatMap(child -> collect(mapFile, child).stream())
//                        .forEachOrdered(builder::addChild);
                    // remove @chunk
//                    elem.removeAttribute(ATTRIBUTE_NAME_CHUNK);
                    chunks.add(builder.build());
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
        final Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        final Element navtitle = getNavtitle(elem);
        if (hrefNode != null && isDitaFormat(elem) && isLocalScope(elem)) {
            final URI href = mapFile.resolve(hrefNode.getValue());
            final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                    .src(href)
                    .topicref(elem);
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                for (ChunkBuilder chunkBuilder : collectCombineChunks(mapFile, child)) {
                    builder.addChild(chunkBuilder);
                }
            }
            return Collections.singletonList(builder);
        } else if (navtitle != null) {
            final ChunkBuilder builder = new ChunkBuilder(COMBINE)
                    .topicref(elem);
            for (Element child : getChildElements(elem, MAP_TOPICREF)) {
                for (ChunkBuilder chunkBuilder : collectCombineChunks(mapFile, child)) {
                    builder.addChild(chunkBuilder);
                }
            }
            return Collections.singletonList(builder);
        } else {
            return getChildElements(elem, MAP_TOPICREF).stream()
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

    public boolean isDitaFormat(final Element elem) {
        final String format = elem.getAttribute(ATTRIBUTE_NAME_FORMAT);
        return isDitaFormat(format);
    }

    public boolean isDitaFormat(final String format) {
        return format == null || format.isEmpty() || format.equals(ATTR_FORMAT_VALUE_DITA);
    }

    public static boolean isLocalScope(final Element elem) {
        final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
        return isLocalScope(scope);
    }

    public static boolean isLocalScope(final String scope) {
        return scope == null || scope.isEmpty() || scope.equals(ATTR_SCOPE_VALUE_LOCAL);
    }

    public static boolean isNormalProcessRole(final Element elem) {
        final String processingRole = elem.getAttribute(ATTRIBUTE_NAME_PROCESSING_ROLE);
        return !processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
    }

    public static Float getDitaVersion(Element topic) {
        final String ditaVersion = topic.getAttributeNS(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION);
        if (!ditaVersion.isEmpty()) {
            try {
                return Float.valueOf(ditaVersion);
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        return null;
    }
}
