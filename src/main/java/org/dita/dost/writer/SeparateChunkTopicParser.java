/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collection;

import static org.dita.dost.module.GenMapAndTopicListModule.ELEMENT_STUB;
import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.XMLUtils.getDocumentBuilder;
import static org.dita.dost.util.XMLUtils.getXMLReader;

/**
 * ChunkTopicParser class, writing chunking content into relative topic files
 * and then update list. Not reusable and not thread-safe.
 * 
 * <p>
 * TODO: Refactor to be a SAX filter.
 * </p>
 */
public final class SeparateChunkTopicParser extends AbstractChunkTopicParser {

    private final XMLReader reader;

    /**
     * Constructor.
     */
    public SeparateChunkTopicParser() {
        super(true);
        try {
            reader = getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    @Override
    public void write(final File fileDir) throws DITAOTException {
        // pass map's directory path
        filePath = fileDir.toURI();
        final URI hrefValue = toURI(getValue(rootTopicref, ATTRIBUTE_NAME_HREF));
        final URI copytoValue = toURI(getValue(rootTopicref, ATTRIBUTE_NAME_COPY_TO));
        final String scopeValue = getCascadeValue(rootTopicref, ATTRIBUTE_NAME_SCOPE);
        // Chimera path, has fragment
        URI parseFilePath;
        final Collection<String> chunkValue = split(getValue(rootTopicref,ATTRIBUTE_NAME_CHUNK));
        final String processRoleValue = getCascadeValue(rootTopicref, ATTRIBUTE_NAME_PROCESSING_ROLE);
        boolean dotchunk = false;

        if (copytoValue != null && !chunkValue.contains(CHUNK_TO_CONTENT)) {
            if (hrefValue != null && hrefValue.getFragment() != null) {
                parseFilePath = setFragment(copytoValue, hrefValue.getFragment());
            } else {
                parseFilePath = copytoValue;
            }
        } else {
            parseFilePath = hrefValue;
        }

        // if @copy-to is processed in chunk module, the list file needs to be
        // updated.
        // Because @copy-to should be included in fulltopiclist, and the source
        // of coyy-to should be excluded in fulltopiclist.
        if (copytoValue != null && chunkValue.contains(CHUNK_TO_CONTENT)) {
            copyto.add(copytoValue);
            if (hrefValue != null && hrefValue.getFragment() != null) {
                copytoSource.add(stripFragment(hrefValue));
                copytotarget2source.put(copytoValue, stripFragment(hrefValue));
            } else {
                copytoSource.add(hrefValue);
                copytotarget2source.put(copytoValue, hrefValue);
            }
        }
        try {
            if (parseFilePath != null && !ATTR_SCOPE_VALUE_EXTERNAL.equals(scopeValue)
                    && !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleValue)) {
                // if the path to target file make sense
                currentParsingFile = filePath.resolve(parseFilePath);
                URI outputFileName;
                /*
                 * FIXME: we have code flaws here, references in ditamap need to
                 * be updated to new created file.
                 */
                String id = null;
                String firstTopicID = null;
                if (parseFilePath.getFragment() != null) {
                    id = parseFilePath.getFragment();
                    if (chunkValue.contains(CHUNK_SELECT_BRANCH)) {
                        outputFileName = filePath.resolve(id + FILE_EXTENSION_DITA);
                        targetTopicId = id;
                        startFromFirstTopic = false;
                        selectMethod = CHUNK_SELECT_BRANCH;
                    } else if (chunkValue.contains(CHUNK_SELECT_DOCUMENT)) {
                        firstTopicID = getFirstTopicId(filePath.resolve(parseFilePath).getPath());

                        topicDoc = getTopicDoc(filePath.resolve(parseFilePath));

                        if (firstTopicID != null) {
                            outputFileName = filePath.resolve(firstTopicID + FILE_EXTENSION_DITA);
                            targetTopicId = firstTopicID;
                        } else {
                            outputFileName = setPath(currentParsingFile, currentParsingFile.getPath() + FILE_EXTENSION_CHUNK);
                            dotchunk = true;
                            targetTopicId = null;
                        }
                        selectMethod = CHUNK_SELECT_DOCUMENT;
                    } else {
                        outputFileName = filePath.resolve(id + FILE_EXTENSION_DITA);
                        targetTopicId = id;
                        startFromFirstTopic = false;
                        selectMethod = CHUNK_SELECT_TOPIC;
                    }
                } else {
                    firstTopicID = getFirstTopicId(filePath.resolve(parseFilePath).getPath());

                    topicDoc = getTopicDoc(filePath.resolve(parseFilePath));

                    if (firstTopicID != null) {
                        outputFileName = filePath.resolve(firstTopicID + FILE_EXTENSION_DITA);
                        targetTopicId = firstTopicID;
                    } else {
                        outputFileName = setPath(currentParsingFile, currentParsingFile.getPath() + FILE_EXTENSION_CHUNK);
                        dotchunk = true;
                        targetTopicId = null;
                    }
                    selectMethod = CHUNK_SELECT_DOCUMENT;
                }
                if (copytoValue != null) {
                    // use @copy-to value as the new file name
                    outputFileName = filePath.resolve(copytoValue);
                }

                if (new File(outputFileName).exists()) {
                    final URI t = outputFileName;
                    outputFileName = filePath.resolve(generateFilename());
                    conflictTable.put(outputFileName, t);
                    dotchunk = false;
                }
                output = new OutputStreamWriter(new FileOutputStream(new File(outputFileName)), UTF8);
                outputFile = outputFileName;
                if (!dotchunk) {
                    changeTable.put(filePath.resolve(parseFilePath),
                            setFragment(outputFileName, id));
                    // new generated file
                    changeTable.put(outputFileName, outputFileName);
                }
                // change the href value
                if (firstTopicID == null) {
                    rootTopicref.setAttribute(ATTRIBUTE_NAME_HREF,
                            setFragment(getRelativePath(filePath.resolve(FILE_NAME_STUB_DITAMAP), outputFileName), id).toString());
                } else {
                    rootTopicref.setAttribute(ATTRIBUTE_NAME_HREF,
                            setFragment(getRelativePath(filePath.resolve(FILE_NAME_STUB_DITAMAP), outputFileName), firstTopicID).toString());
                }
                include = false;
                // just a mark?
                stub = rootTopicref.getOwnerDocument().createElement(ELEMENT_STUB);
                siblingStub = rootTopicref.getOwnerDocument().createElement(ELEMENT_STUB);
                // <element>
                // <stub/>
                // ...
                // </element>
                // <siblingstub/>
                // ...
                // Place stub
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

                    // element.insertBefore(stub,element.getFirstChild());
                } else {
                    rootTopicref.appendChild(stub);
                }

                // Place siblingStub
                if (rootTopicref.getNextSibling() != null) {
                    rootTopicref.getParentNode().insertBefore(siblingStub, rootTopicref.getNextSibling());
                } else {
                    rootTopicref.getParentNode().appendChild(siblingStub);
                }

                reader.setErrorHandler(new DITAOTXMLErrorHandler(currentParsingFile.getPath(), logger));
                logger.info("Processing " + currentParsingFile);
                reader.parse(currentParsingFile.toString());
                output.flush();

                // remove stub and siblingStub
                stub.getParentNode().removeChild(stub);
                siblingStub.getParentNode().removeChild(siblingStub);
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                    output = null;
                    if (dotchunk && !new File(currentParsingFile).delete()) {
                        logger.error(MessageUtils.getInstance()
                                .getMessage("DOTJ009E", currentParsingFile.getPath(), outputFile.getPath()).toString());
                    }
                    if (dotchunk && !new File(outputFile).renameTo(new File(currentParsingFile))) {
                        logger.error(MessageUtils.getInstance()
                                .getMessage("DOTJ009E", currentParsingFile.getPath(), outputFile.getPath()).toString());
                    }
                }
            } catch (final Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * get the document node of a topic file.
     * @param absolutePathToFile topic file
     * @return element.
     */
    private Element getTopicDoc(final URI absolutePathToFile){
        final DocumentBuilder builder = getDocumentBuilder();
        try {
            final Document doc = builder.parse(absolutePathToFile.toString());
            return doc.getDocumentElement();
        } catch (final SAXException | IOException e) {
            logger.error("Failed to parse " + absolutePathToFile + ": " + e.getMessage(), e);
        }
        return null;
    }

}
