/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import static org.dita.dost.module.GenMapAndTopicListModule.ELEMENT_STUB;
import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.split;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

/**
 * Split topic into multiple files for {@code by-topic} chunking.
 * Not reusable and not thread-safe.
 */
public final class SeparateChunkTopicParser extends AbstractChunkTopicParser {

    // stub is used as the anchor to mark where to insert generated child
    // topicref inside current topicref
    private Element stub;
    // siblingStub is similar to stub. The only different is it is used to
    // insert generated topicref sibling to current topicref
    private Element siblingStub;
    private final Deque<URI> outputFileNameStack = new ArrayDeque<>();
    private Element topicDoc = null;
    final Deque<Writer> outputStack = new ArrayDeque<>();
    final Deque<Element> stubStack = new ArrayDeque<>();
    final Deque<String> lang = new LinkedList<>();

    @Override
    public void write(final URI currentFile) {
        this.currentFile = currentFile;
        final URI hrefValue = toURI(getValue(rootTopicref, ATTRIBUTE_NAME_HREF));
        final URI copytoValue = toURI(getValue(rootTopicref, ATTRIBUTE_NAME_COPY_TO));
        final URI resolveBase;
        final String scopeValue = getCascadeValue(rootTopicref, ATTRIBUTE_NAME_SCOPE);
        // Chimera path, has fragment
        URI parseFilePath;
        final Collection<String> chunkValue = split(getValue(rootTopicref, ATTRIBUTE_NAME_CHUNK));
        final String processRoleValue = getCascadeValue(rootTopicref, ATTRIBUTE_NAME_PROCESSING_ROLE);
        boolean dotchunk = false;

        if (copytoValue != null) {
            if (hrefValue != null && hrefValue.getFragment() != null) {
                parseFilePath = setFragment(copytoValue, hrefValue.getFragment());
            } else {
                parseFilePath = copytoValue;
            }
        } else {
            parseFilePath = hrefValue;
        }

        try {
            // if the path to target file make sense
            currentParsingFile = currentFile.resolve(parseFilePath);
            URI outputFileName;

            resolveBase = copytoValue == null ? currentParsingFile : currentFile;

            /*
             * FIXME: we have code flaws here, references in ditamap need to
             * be updated to new created file.
             */
            String id = null;
            String firstTopicID = null;
            if (parseFilePath.getFragment() != null) {
                id = parseFilePath.getFragment();
                if (chunkValue.contains(CHUNK_SELECT_BRANCH)) {
                    outputFileName = resolve(currentFile, id + FILE_EXTENSION_DITA);
                    targetTopicId = id;
                    startFromFirstTopic = false;
                    selectMethod = CHUNK_SELECT_BRANCH;
                } else if (chunkValue.contains(CHUNK_SELECT_DOCUMENT)) {
                    firstTopicID = getFirstTopicId(new File(stripFragment(currentFile.resolve(parseFilePath))));

                    topicDoc = getTopicDoc(currentFile.resolve(parseFilePath));

                    if (firstTopicID != null) {
                        outputFileName = resolve(currentFile, firstTopicID + FILE_EXTENSION_DITA);
                        targetTopicId = firstTopicID;
                    } else {
                        outputFileName = resolve(currentParsingFile, null);
                        dotchunk = true;
                        targetTopicId = null;
                    }
                    selectMethod = CHUNK_SELECT_DOCUMENT;
                } else {
                    outputFileName = resolve(currentFile, id + FILE_EXTENSION_DITA);
                    targetTopicId = id;
                    startFromFirstTopic = false;
                    selectMethod = CHUNK_SELECT_TOPIC;
                }
            } else {
                firstTopicID = getFirstTopicId(new File(stripFragment(currentFile.resolve(parseFilePath))));

                topicDoc = getTopicDoc(currentFile.resolve(parseFilePath));

                if (firstTopicID != null) {
                    outputFileName = resolve(resolveBase, firstTopicID + FILE_EXTENSION_DITA);
                    targetTopicId = firstTopicID;
                } else {
                    outputFileName = resolve(resolveBase, null);
                    dotchunk = true;
                    targetTopicId = null;
                }
                selectMethod = CHUNK_SELECT_DOCUMENT;
            }
            if (copytoValue != null) {
                // use @copy-to value as the new file name
                outputFileName = resolve(resolveBase, copytoValue.toString());
            }

            if (job.getStore().exists(outputFileName)) {
                final URI t = outputFileName;
                outputFileName = resolve(resolveBase, generateFilename());
                conflictTable.put(outputFileName, t);
                dotchunk = false;
            }

            final OutputStream out = job.getStore().getOutputStream(outputFileName);
            output = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            outputFile = outputFileName;

            if (!dotchunk) {
                final FileInfo fi = generateFileInfo(outputFile);
                job.add(fi);

                changeTable.put(currentFile.resolve(parseFilePath),
                        setFragment(outputFileName, id));
                // new generated file
                changeTable.put(outputFileName, outputFileName);
            }

            // change the href value
            final URI newHref = setFragment(getRelativePath(currentFile.resolve(FILE_NAME_STUB_DITAMAP), outputFileName),
                                            firstTopicID != null ? firstTopicID : id);
            rootTopicref.setAttribute(ATTRIBUTE_NAME_HREF, newHref.toString());

            include = false;

            addStubElements();

            // Place siblingStub
            if (rootTopicref.getNextSibling() != null) {
                rootTopicref.getParentNode().insertBefore(siblingStub, rootTopicref.getNextSibling());
            } else {
                rootTopicref.getParentNode().appendChild(siblingStub);
            }

            logger.info("Processing " + currentParsingFile);
            job.getStore().transform(currentParsingFile, this);
            output.flush();

            removeStubElements();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                    output = null;
                    if (dotchunk) {
                        if (job.getStore().exists(currentParsingFile)) {
                            logger.debug("Delete " + currentParsingFile);
                            job.getStore().delete(currentParsingFile);
                        }
                        logger.debug("Move " + outputFile + " to " + currentParsingFile);
                        job.getStore().move(outputFile, currentParsingFile);
                        final FileInfo fi = job.getFileInfo(outputFile);
                        if (fi != null) {
                            job.remove(fi);
                        }
                    }
                }
            } catch (final Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private void addStubElements() {
        stub = rootTopicref.getOwnerDocument().createElement(ELEMENT_STUB);
        siblingStub = rootTopicref.getOwnerDocument().createElement(ELEMENT_STUB);
        if (rootTopicref.hasChildNodes()) {
            final NodeList list = rootTopicref.getElementsByTagName(MAP_TOPICMETA.localName);
            if (list.getLength() > 0) {
                final Node node = list.item(0);
                final Node nextSibling = node.getNextSibling();
                // no sibling so node is the last child
                if (nextSibling == null) {
                    node.getParentNode().appendChild(stub);
                } else {
                    // has sibling node
                    node.getParentNode().insertBefore(stub, nextSibling);
                }
            } else {
                // no topicmeta tag.
                rootTopicref.insertBefore(stub, rootTopicref.getFirstChild());
            }
        } else {
            rootTopicref.appendChild(stub);
        }
    }

    private void removeStubElements() {
        stub.getParentNode().removeChild(stub);
        siblingStub.getParentNode().removeChild(siblingStub);
    }

    /**
     * get the document node of a topic file.
     *
     * @param absolutePathToFile topic file
     * @return element.
     */
    private Element getTopicDoc(final URI absolutePathToFile) {
        try {
            final Document doc = job.getStore().getDocument(absolutePathToFile);
            return doc.getDocumentElement();
        } catch (final IOException e) {
            logger.error("Failed to parse " + absolutePathToFile + ": " + e.getMessage(), e);
        }
        return null;
    }

    private URI resolve(final URI base, final String file) {
        assert base.isAbsolute();
        assert base.toString().startsWith(job.tempDirURI.toString());

        final FileInfo srcFi = job.getFileInfo(base);
        final URI dst;
        if (file != null) {
            dst = srcFi.result.resolve(file);
        } else {
            dst = setPath(srcFi.result, srcFi.result.getPath() + FILE_EXTENSION_CHUNK);
        }
        final URI tmp = tempFileNameScheme.generateTempFileName(dst);

        if (job.getFileInfo(tmp) == null) {
            job.add(new FileInfo.Builder(srcFi)
                    .result(dst)
                    .uri(tmp)
                    .build());
        }


        return job.tempDirURI.resolve(tmp);
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String id = atts.getValue(ATTRIBUTE_NAME_ID);
        final AttributesImpl attsMod = new AttributesImpl(atts);
        final String xmlLang = atts.getValue(ATTRIBUTE_NAME_XML_LANG);
        final String currentLang;


        if (skip && skipLevel > 0) {
            skipLevel++;
        }

        if (xmlLang != null) {
            currentLang = xmlLang;
        } else if (!lang.isEmpty()) {
            currentLang = lang.peek();
        } else {
            currentLang = null;
        }
        lang.push(currentLang);

        try {
            if (TOPIC_TOPIC.matches(cls)) {
                topicSpecSet.add(qName);

                if (include && !CHUNK_SELECT_TOPIC.equals(selectMethod)) {
                    // chunk="by-topic" and next topic element found
                    outputStack.push(output);
                    outputFileNameStack.push(outputFile);

                    outputFile = generateOutputFilename(id);
                    final OutputStream out = job.getStore().getOutputStream(outputFile);
                    output = new OutputStreamWriter(out, StandardCharsets.UTF_8);

                    if (atts.getIndex(ATTRIBUTE_NAME_XML_LANG) < 0 && currentLang != null) {
                        attsMod.addAttribute("", ATTRIBUTE_NAME_LANG, ATTRIBUTE_NAME_XML_LANG, "CDATA", currentLang );
                    }
//                    final FileInfo fi = generateFileInfo(outputFile);
//                    job.add(fi);

                    changeTable.put(outputFile, outputFile);
                    if (id != null) {
                        changeTable.put(setFragment(currentParsingFile, id), setFragment(outputFile, id));
                    } else {
                        changeTable.put(currentParsingFile, outputFile);
                    }

                    // write xml header and workdir PI to the new generated file
                    writeStartDocument(output);
                    if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                        writeProcessingInstruction(output, PI_WORKDIR_TARGET, new File(currentFile).getParentFile().getAbsolutePath());
                    } else {
                        writeProcessingInstruction(output, PI_WORKDIR_TARGET, UNIX_SEPARATOR + currentFile.resolve("."));
                    }
                    writeProcessingInstruction(output, PI_WORKDIR_TARGET_URI, currentFile.resolve(".").toString());

                    // create a new child element in separate case topicref is equals to parameter
                    // element in separateChunk(Element element)
                    final Element newTopicref = rootTopicref.getOwnerDocument().createElement(MAP_TOPICREF.localName);
                    newTopicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
                    newTopicref.setAttribute(ATTRIBUTE_NAME_XTRF, ATTR_XTRF_VALUE_GENERATED);
                    newTopicref.setAttribute(ATTRIBUTE_NAME_HREF, getRelativePath(currentFile.resolve(FILE_NAME_STUB_DITAMAP), outputFile).toString());

                    final Element topic = searchForNode(topicDoc, id, ATTRIBUTE_NAME_ID, TOPIC_TOPIC);
                    final Element topicmeta = createTopicMeta(topic);
                    newTopicref.appendChild(topicmeta);

                    if (stub != null) {
                        if (includelevel == 0 && siblingStub != null) {
                            // if it is the following sibling topic to the first topic in ditabase
                            // The first topic will not enter the logic at here because when meeting
                            // with first topic in ditabase, the include value is false
                            siblingStub.getParentNode().insertBefore(newTopicref, siblingStub);
                        } else {
                            stub.getParentNode().insertBefore(newTopicref, stub);
                        }
                        stubStack.push(stub);
                        stub = (Element) stub.cloneNode(false);
                        newTopicref.appendChild(stub);
                    }
                }

                processSelect(id);
            }

            if (include) {
                includelevel++;
                final Attributes resAtts = processAttributes(attsMod);
                writeStartElement(output, qName, resAtts);
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (skip && skipLevel > 0) {
            skipLevel--;
        } else if (skip) {
            include = true;
            skip = false;
            skipLevel = 0;
        }

        if (include) {
            includelevel--;
            // prevent adding </dita> into output
            if (includelevel >= 0) {
                writeEndElement(output, qName);
            }
            if (includelevel == 0 && !CHUNK_SELECT_DOCUMENT.equals(selectMethod)) {
                include = false;
            }
            if (topicSpecSet.contains(qName) && !outputStack.isEmpty()) {
                // if it is end of topic and separate is true
                try {
                    output.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                }
                output = outputStack.pop();
                outputFile = outputFileNameStack.pop();
                stub.getParentNode().removeChild(stub);
                stub = stubStack.pop();
            }
        }

        lang.pop();
    }

}
