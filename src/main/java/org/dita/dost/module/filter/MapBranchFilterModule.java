/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.filter;

import static java.util.Collections.singletonList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.getExtProps;
import static org.dita.dost.util.StringUtils.getExtPropsFromSpecializations;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.getChildElements;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.BranchFilterModule.Branch;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Flag;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.*;

/**
 * Branch filter module for map processing.
 *
 * <p>Branch filtering is done with the following steps:</p>
 * <ol>
 *   <li>Split braches so that each branch will only contain a single ditavalref</li>
 *   <li>Generate copy-to attribute for each brach generated topicref</li>
 *   <li>Filter map based on branch filters</li>
 *   <li>Rewrite duplicate generated copy-to targets</li>
 * </ol>
 *
 * @since 2.5
 */
public class MapBranchFilterModule extends AbstractBranchFilterModule {

  public static final String BRANCH_COPY_TO = "filter-copy-to";

  /** Current map being processed, relative to temporary directory */
  private URI map;
  /** Absolute path for filter file. */
  private URI ditavalFile;

  @Override
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    final FileInfo fi = job.getFileInfo(f -> f.isInput).iterator().next();
    if (!ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
      return null;
    }
    processMap(fi);

    try {
      job.write();
    } catch (final IOException e) {
      throw new DITAOTException("Failed to serialize job configuration: " + e.getMessage(), e);
    }

    return null;
  }

  /**
   * Process map for branch replication.
   */
  protected void processMap(final FileInfo fi) {
    this.map = fi.uri;
    currentFile = job.tempDirURI.resolve(map);
    ditavalFile =
      Optional.of(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL)).filter(File::exists).map(File::toURI).orElse(null);

    logger.info("Processing " + currentFile);
    final Document doc;
    final SubjectScheme subjectSchemeMap;
    try {
      logger.debug("Reading " + currentFile);
      final XdmNode node = job.getStore().getImmutableNode(currentFile);
      subjectSchemeMap = getSubjectScheme(node.getOutermostElement());
      doc = xmlUtils.cloneDocument(node);
    } catch (final IOException e) {
      logger.error("Failed to parse " + currentFile, e);
      return;
    }

    logger.debug("Split branches and generate copy-to");
    splitBranches(doc.getDocumentElement(), Branch.EMPTY);
    logger.debug("Filter map");
    filterBranches(doc.getDocumentElement(), subjectSchemeMap);
    logger.debug("Rewrite duplicate topic references");
    rewriteDuplicates(doc.getDocumentElement());

    logger.debug("Writing " + currentFile);
    try {
      job.getStore().writeDocument(doc, currentFile);
    } catch (final IOException e) {
      logger.error("Failed to serialize " + map.toString() + ": " + e.getMessage(), e);
    }

    removeFilteredTopics(doc);
  }

  /** Remove orphan topic fileinfos where all topicrefs have been filtered out. */
  private void removeFilteredTopics(Document doc) {
    logger.debug("Prune orphan fileinfo topics");
    Set<URI> allReferences = collectReferences(doc);
    for (FileInfo fileInfo : job.getFileInfo()) {
      if (fileInfo.format == null || fileInfo.format.equals(ATTR_FORMAT_VALUE_DITA)) {
        var abs = job.tempDirURI.resolve(fileInfo.uri);
        if (!allReferences.contains(abs)) {
          logger.debug("Remove orphan fileinfo for {0}", abs);
          job.remove(fileInfo);
        }
      }
    }
  }

  private Set<URI> collectReferences(Document doc) {
    var res = new HashSet<URI>();
    var children = doc.getElementsByTagName("*");
    for (int i = 0; i < children.getLength(); i++) {
      final Element child = (Element) children.item(i);
      if (
        MAP_TOPICREF.matches(child) &&
        isDitaFormat(child.getAttributeNode(ATTRIBUTE_NAME_FORMAT)) &&
        !child.getAttribute(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_EXTERNAL)
      ) {
        for (var name : new String[] { ATTRIBUTE_NAME_HREF, ATTRIBUTE_NAME_COPY_TO, BRANCH_COPY_TO }) {
          var value = child.getAttribute(name);
          if (!value.isEmpty()) {
            res.add(stripFragment(currentFile.resolve(value)));
          }
        }
      }
    }
    return res;
  }

  /** Rewrite href or copy-to if duplicates exist. */
  private void rewriteDuplicates(final Element root) {
    // collect href and copy-to
    final Map<URI, Map<Set<URI>, List<Attr>>> refs = new HashMap<>();
    for (final Element e : getTopicrefs(root)) {
      Attr attr = e.getAttributeNode(BRANCH_COPY_TO);
      if (attr == null) {
        attr = e.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
        if (attr == null) {
          attr = e.getAttributeNode(ATTRIBUTE_NAME_HREF);
        }
      }
      if (attr != null) {
        final URI h = stripFragment(map.resolve(attr.getValue()));
        Map<Set<URI>, List<Attr>> attrsMap = refs.computeIfAbsent(h, k -> new HashMap<>());
        final Set<URI> currentFilter = getBranchFilters(e);
        List<Attr> attrs = attrsMap.computeIfAbsent(currentFilter, k -> new ArrayList<>());
        attrs.add(attr);
      }
    }
    // check and rewrite
    for (final Map.Entry<URI, Map<Set<URI>, List<Attr>>> ref : refs.entrySet()) {
      final Map<Set<URI>, List<Attr>> attrsMaps = ref.getValue();
      if (attrsMaps.size() > 1) {
        if (attrsMaps.containsKey(Collections.EMPTY_LIST)) {
          attrsMaps.remove(Collections.EMPTY_LIST);
        } else {
          Set<URI> first = attrsMaps.keySet().iterator().next();
          attrsMaps.remove(first);
        }
        int i = 1;
        for (final Map.Entry<Set<URI>, List<Attr>> attrsMap : attrsMaps.entrySet()) {
          final String suffix = "-" + i;
          final List<Attr> attrs = attrsMap.getValue();
          for (final Attr attr : attrs) {
            final String gen = addSuffix(attr.getValue(), suffix);
            logger.info(
              MessageUtils.getMessage("DOTJ065I", attr.getValue(), gen).setLocation(attr.getOwnerElement()).toString()
            );
            if (attr.getName().equals(BRANCH_COPY_TO)) {
              attr.setValue(gen);
            } else {
              attr.getOwnerElement().setAttribute(BRANCH_COPY_TO, gen);
            }

            final URI dstUri = stripFragment(map.resolve(gen));
            if (dstUri != null) {
              final URI absTarget = stripFragment(currentFile.resolve(attr.getValue()));
              final FileInfo hrefFileInfo = job.getFileInfo(absTarget);
              if (hrefFileInfo != null) {
                final URI newResult = addSuffix(hrefFileInfo.result, suffix);
                final FileInfo.Builder dstBuilder = new FileInfo.Builder(hrefFileInfo).uri(dstUri).result(newResult);
                if (hrefFileInfo.format == null) {
                  dstBuilder.format(ATTR_FORMAT_VALUE_DITA);
                }
                final FileInfo dstFileInfo = dstBuilder.build();
                job.add(dstFileInfo);
              }
            }
          }
          i++;
        }
      }
    }
  }

  private Set<URI> getBranchFilters(final Element e) {
    final Set<URI> res = new HashSet<>();
    Element current = e;
    while (current != null) {
      final List<Element> ditavalref = getChildElements(current, DITAVAREF_D_DITAVALREF);
      if (!ditavalref.isEmpty()) {
        res.add(toURI(ditavalref.get(0).getAttribute(ATTRIBUTE_NAME_HREF)));
      }
      final Node parent = current.getParentNode();
      if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
        current = (Element) parent;
      } else {
        break;
      }
    }
    return res;
  }

  /** Add suffix to file name */
  private static String addSuffix(final String href, final String suffix) {
    final int idx = href.lastIndexOf(".");
    return idx != -1 ? (href.substring(0, idx) + suffix + href.substring(idx)) : (href + suffix);
  }

  /** Add suffix to file name */
  private static URI addSuffix(final URI href, final String suffix) {
    return URI.create(addSuffix(href.toString(), suffix));
  }

  /** Get all topicrefs */
  private List<Element> getTopicrefs(final Element root) {
    final List<Element> res = new ArrayList<>();
    final NodeList all = root.getElementsByTagName("*");
    for (int i = 0; i < all.getLength(); i++) {
      final Element elem = (Element) all.item(i);
      if (
        MAP_TOPICREF.matches(elem) &&
        isDitaFormat(elem.getAttributeNode(ATTRIBUTE_NAME_FORMAT)) &&
        !elem.getAttribute(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_EXTERNAL)
      ) {
        res.add(elem);
      }
    }
    return res;
  }

  private boolean isDitaFormat(final Attr formatAttr) {
    return (
      formatAttr == null ||
      ATTR_FORMAT_VALUE_DITA.equals(formatAttr.getNodeValue()) ||
      ATTR_FORMAT_VALUE_DITAMAP.equals(formatAttr.getNodeValue())
    );
  }

  /** Filter map and remove excluded content. */
  private void filterBranches(final Element root, final SubjectScheme subjectSchemeMap) {
    final String domains = root.getAttribute(ATTRIBUTE_NAME_DOMAINS);
    final String specializations = root.getAttribute(ATTRIBUTE_NAME_SPECIALIZATIONS);
    final QName[][] props = !domains.isEmpty() ? getExtProps(domains) : getExtPropsFromSpecializations(specializations);
    final List<FilterUtils> baseFilter = getBaseFilter(subjectSchemeMap);
    filterBranches(root, baseFilter, props, subjectSchemeMap);
  }

  private List<FilterUtils> getBaseFilter(final SubjectScheme subjectSchemeMap) {
    if (ditavalFile != null && !subjectSchemeMap.subjectSchemeMap().isEmpty()) {
      final FilterUtils f = getFilterUtils(ditavalFile).refine(subjectSchemeMap);
      return singletonList(f);
    }
    return Collections.emptyList();
  }

  private void filterBranches(
    final Element elem,
    final List<FilterUtils> filters,
    final QName[][] props,
    final SubjectScheme subjectSchemeMap
  ) {
    final List<FilterUtils> fs = combineFilterUtils(elem, filters, subjectSchemeMap);

    boolean exclude = false;
    for (final FilterUtils f : fs) {
      exclude = f.needExclude(elem, props);
      if (exclude) {
        break;
      }
    }

    if (exclude) {
      elem.getParentNode().removeChild(elem);
    } else {
      final List<Element> childElements = getChildElements(elem);
      final Set<Flag> flags = fs
        .stream()
        .flatMap(f -> f.getFlags(elem, props).stream())
        .map(f -> f.adjustPath(currentFile, job))
        .collect(Collectors.toSet());
      for (Flag flag : flags) {
        final Element startElement = (Element) elem.getOwnerDocument().importNode(flag.getStartFlag(), true);
        final Node firstChild = elem.getFirstChild();
        if (firstChild != null) {
          elem.insertBefore(startElement, firstChild);
        } else {
          elem.appendChild(startElement);
        }
        final Element endElement = (Element) elem.getOwnerDocument().importNode(flag.getEndFlag(), true);
        elem.appendChild(endElement);
      }
      for (final Element child : childElements) {
        filterBranches(child, fs, props, subjectSchemeMap);
      }
    }
  }

  /**
   * Duplicate branches so that each {@code ditavalref} will in a separate branch.
   */
  void splitBranches(final Element elem, final Branch filter) {
    final List<Element> ditavalRefs = getChildElements(elem, DITAVAREF_D_DITAVALREF);
    if (ditavalRefs.size() > 0) {
      // remove ditavalrefs
      for (final Element branch : ditavalRefs) {
        elem.removeChild(branch);
      }
      // create additional branches after current element
      final List<Element> branches = new ArrayList<>(ditavalRefs.size());
      branches.add(elem);
      final Node next = elem.getNextSibling();
      for (int i = 1; i < ditavalRefs.size(); i++) {
        final Element clone = (Element) elem.cloneNode(true);
        if (next != null) {
          elem.getParentNode().insertBefore(clone, next);
        } else {
          elem.getParentNode().appendChild(clone);
        }
        branches.add(clone);
      }
      // insert ditavalrefs
      for (int i = 0; i < branches.size(); i++) {
        final Element branch = branches.get(i);
        final Element ditavalref = ditavalRefs.get(i);
        branch.insertBefore(ditavalref, branch.getFirstChild());
        final Branch currentFilter = filter.merge(ditavalref);
        processAttributes(branch, currentFilter);
        final Branch childFilter = new Branch(
          currentFilter.resourcePrefix,
          currentFilter.resourceSuffix,
          Optional.empty(),
          Optional.empty()
        );
        // process children of all branches
        for (final Element child : getChildElements(branch, MAP_TOPICREF)) {
          if (DITAVAREF_D_DITAVALREF.matches(child)) {
            continue;
          }
          splitBranches(child, childFilter);
        }
      }
    } else {
      processAttributes(elem, filter);
      for (final Element child : getChildElements(elem, MAP_TOPICREF)) {
        splitBranches(child, filter);
      }
    }
  }

  private void processAttributes(final Element elem, final Branch filter) {
    if (filter.resourcePrefix.isPresent() || filter.resourceSuffix.isPresent()) {
      final String href = elem.getAttribute(ATTRIBUTE_NAME_HREF);
      final String copyTo = elem.getAttribute(ATTRIBUTE_NAME_COPY_TO);
      final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
      if ((!href.isEmpty() || !copyTo.isEmpty()) && !scope.equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
        final FileInfo hrefFileInfo = job.getFileInfo(currentFile.resolve(href));

        final FileInfo copyToFileInfo = !copyTo.isEmpty() ? job.getFileInfo(currentFile.resolve(copyTo)) : null;

        final URI dstSource;
        dstSource = generateCopyTo((copyToFileInfo != null ? copyToFileInfo : hrefFileInfo).result, filter);
        final URI dstTemp = tempFileNameScheme.generateTempFileName(dstSource);
        final FileInfo.Builder dstBuilder = new FileInfo.Builder(hrefFileInfo).result(dstSource).uri(dstTemp);
        if (dstBuilder.build().format == null) {
          dstBuilder.format(ATTR_FORMAT_VALUE_DITA);
        }
        if (hrefFileInfo.src == null && href != null) {
          if (copyToFileInfo != null) {
            dstBuilder.src(copyToFileInfo.src);
          }
        }
        final FileInfo dstFileInfo = dstBuilder.build();

        elem.setAttribute(BRANCH_COPY_TO, dstTemp.toString());
        if (!copyTo.isEmpty()) {
          elem.removeAttribute(ATTRIBUTE_NAME_COPY_TO);
        }

        job.add(dstFileInfo);
      }
    }

    if (filter.keyscopePrefix.isPresent() || filter.keyscopeSuffix.isPresent()) {
      final StringBuilder buf = new StringBuilder();
      final String keyscope = elem.getAttribute(ATTRIBUTE_NAME_KEYSCOPE);
      if (!keyscope.isEmpty()) {
        for (final String key : keyscope.trim().split("\\s+")) {
          filter.keyscopePrefix.ifPresent(buf::append);
          buf.append(key);
          filter.keyscopeSuffix.ifPresent(buf::append);
          buf.append(' ');
        }
      } else {
        filter.keyscopePrefix.ifPresent(buf::append);
        filter.keyscopeSuffix.ifPresent(buf::append);
      }
      elem.setAttribute(ATTRIBUTE_NAME_KEYSCOPE, buf.toString().trim());
    }
  }

  static URI generateCopyTo(final URI href, final Branch filter) {
    final StringBuilder buf = new StringBuilder(href.toString());
    final Optional<String> suffix = filter.resourceSuffix;
    suffix.ifPresent(s -> {
      final int sep = buf.lastIndexOf(URI_SEPARATOR);
      final int i = buf.lastIndexOf(".");
      if (i != -1 && (sep == -1 || i > sep)) {
        buf.insert(i, s);
      } else {
        buf.append(s);
      }
    });
    final Optional<String> prefix = filter.resourcePrefix;
    prefix.ifPresent(s -> {
      final int i = buf.lastIndexOf(URI_SEPARATOR);
      if (i != -1) {
        buf.insert(i + 1, s);
      } else {
        buf.insert(0, s);
      }
    });
    return toURI(buf.toString());
  }
}
