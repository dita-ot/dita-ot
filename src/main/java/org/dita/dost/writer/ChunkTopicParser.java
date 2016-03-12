/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import java.io.*;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

/**
 * ChunkTopicParser class, writing chunking content into relative topic files
 * and then update list. Not reusable and not thread-safe.
 * 
 * <p>
 * TODO: Refactor to be a SAX filter.
 * </p>
 */
public final class ChunkTopicParser extends AbstractChunkTopicParser {

    private final XMLReader reader;

    /**
     * Constructor.
     */
    public ChunkTopicParser() {
        super(false);
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
        filePath = fileDir.toURI();
        try {
            output = new StringWriter();
            processChunk(rootTopicref, null);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void processChunk(final Element topicref, final URI outputFile) {
        final URI hrefValue = toURI(getValue(topicref, ATTRIBUTE_NAME_HREF));
        final Collection<String> chunkValue = split(getValue(topicref, ATTRIBUTE_NAME_CHUNK));
        final URI copytoValue = toURI(getValue(topicref, ATTRIBUTE_NAME_COPY_TO));
        final String scopeValue = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
        final String classValue = getValue(topicref, ATTRIBUTE_NAME_CLASS);
        final String processRoleValue = getCascadeValue(topicref, ATTRIBUTE_NAME_PROCESSING_ROLE);

        URI outputFileName = outputFile;
        Writer tempWriter = null;
        Set<String> tempTopicID = null;

        targetTopicId = null;
        selectMethod = CHUNK_SELECT_DOCUMENT;
        include = false;

        boolean needWriteDitaTag = true;

        try {
            URI parseFilePath;
            if (copytoValue != null && !chunkValue.contains(CHUNK_TO_CONTENT)) {
                if (hrefValue.getFragment() != null) {
                    parseFilePath = setFragment(copytoValue, hrefValue.getFragment());
                } else {
                    parseFilePath = copytoValue;
                }
            } else {
                parseFilePath = hrefValue;
            }

            // if @copy-to is processed in chunk module, the list file needs to
            // be updated.
            // Because @copy-to should be included in fulltopiclist, and the
            // source of coyy-to should be excluded in fulltopiclist.
            if (copytoValue != null && chunkValue.contains(CHUNK_TO_CONTENT)
                    && hrefValue != null) {
                copyto.add(copytoValue);
                if (hrefValue.getFragment() != null) {
                    copytoSource.add(stripFragment(hrefValue));
                    copytotarget2source.put(copytoValue, stripFragment(hrefValue));
                } else {
                    copytoSource.add(hrefValue);
                    copytotarget2source.put(copytoValue, hrefValue);
                }
            }

            if (parseFilePath != null && !ATTR_SCOPE_VALUE_EXTERNAL.equals(scopeValue)) {
                // now the path to target file make sense
                if (chunkValue.contains(CHUNK_TO_CONTENT)) {
                    // if current element contains "to-content" in chunk
                    // attribute
                    // we need to create new buffer and flush the buffer to
                    // file
                    // after processing is finished
                    tempWriter = output;
                    tempTopicID = topicID;
                    output = new StringWriter();
                    topicID = new HashSet<>();
                    if (MAP_MAP.matches(classValue)) {
                        // Very special case, we have a map element with
                        // href value.
                        // This is a map that needs to be chunked to
                        // content.
                        // No need to parse any file, just generate a stub
                        // output.
                        outputFileName = filePath.resolve(parseFilePath);
                        needWriteDitaTag = false;
                    } else if (copytoValue != null) {
                        // use @copy-to value as the new file name
                        outputFileName = filePath.resolve(copytoValue);
                    } else if (hrefValue != null) {
                        // try to use href value as the new file name
                        if (chunkValue.contains(CHUNK_SELECT_TOPIC)
                                || chunkValue.contains(CHUNK_SELECT_BRANCH)) {
                            if (hrefValue.getFragment() != null) {
                                // if we have an ID here, use it.
                                outputFileName = filePath.resolve(hrefValue.getFragment() + FILE_EXTENSION_DITA);
                            } else {
                                // Find the first topic id in target file if
                                // any.
                                final String firstTopic = getFirstTopicId(filePath.resolve(hrefValue).getPath());
                                if (firstTopic != null) {
                                    outputFileName = filePath.resolve(firstTopic + FILE_EXTENSION_DITA);
                                } else {
                                    outputFileName = filePath.resolve(hrefValue);
                                }
                            }
                        } else {
                            // otherwise, use the href value instead
                            outputFileName = filePath.resolve(hrefValue);
                        }
                    } else {
                        // use randomly generated file name
                        outputFileName = filePath.resolve(generateFilename());
                    }

                    // Check if there is any conflict
                    if (new File(outputFileName).exists() && !MAP_MAP.matches(classValue)) {
                        final URI t = outputFileName;
                        outputFileName = filePath.resolve(generateFilename());
                        conflictTable.put(outputFileName, t);
                    }
                    // add newly generated file to changTable
                    // the new entry in changeTable has same key and value
                    // in order to indicate it is a newly generated file
                    changeTable.put(outputFileName, outputFileName);
                }
                // "by-topic" couldn't reach here
                this.outputFile = outputFileName;

                final URI path = filePath.resolve(parseFilePath);
                URI newpath;
                if (path.getFragment() != null) {
                    newpath = setFragment(outputFileName, path.getFragment());
                } else {
                    final String firstTopicID = getFirstTopicId(new File(path).getAbsolutePath());
                    if (firstTopicID != null) {
                        newpath = setFragment(outputFileName, firstTopicID);
                    } else {
                        newpath = outputFileName;
                    }
                }
                // add file name changes to changeTable, this will be
                // used in
                // TopicRefWriter's updateHref method, very important!!!
                changeTable.put(path, newpath);
                // update current element's @href value
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, getRelativePath(filePath.resolve(FILE_NAME_STUB_DITAMAP), newpath).toString());

                if (parseFilePath.getFragment() != null) {
                    targetTopicId = parseFilePath.getFragment();
                }

                final String s = getChunkByToken(chunkValue, "select-", null);
                if (s != null) {
                    selectMethod = s;
                    // if the current topic href referred to a entire
                    // topic file,it will be handled in "document" level.
                    if (targetTopicId == null) {
                        selectMethod = CHUNK_SELECT_DOCUMENT;
                    }
                }
                final URI tempPath = currentParsingFile;
                currentParsingFile = filePath.resolve(parseFilePath);

                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleValue)) {
                    currentParsingFileTopicIDChangeTable = new HashMap<>();
                    // TODO recursive point
                    logger.info("Processing " + currentParsingFile);
                    reader.parse(currentParsingFile.toString());
                    if (currentParsingFileTopicIDChangeTable.size() > 0) {
                        final URI href = toURI(topicref.getAttribute(ATTRIBUTE_NAME_HREF));
                        final String pathtoElem = href.getFragment() != null
                                ? href.getFragment()
                                : "";
                        final String old_elementid = pathtoElem.contains(SLASH)
                                ? pathtoElem.substring(0, pathtoElem.indexOf(SLASH))
                                : pathtoElem;
                        if (!old_elementid.isEmpty()) {
                            final String new_elementid = currentParsingFileTopicIDChangeTable.get(old_elementid);
                            if (new_elementid != null && !new_elementid.isEmpty()) {
                                topicref.setAttribute(ATTRIBUTE_NAME_HREF, setFragment(href, new_elementid).toString());
                            }
                        }
                    }
                    currentParsingFileTopicIDChangeTable = null;
                }
                // restore the currentParsingFile
                currentParsingFile = tempPath;
            }

            if (topicref.hasChildNodes()) {
                // if current element has child nodes and chunk results for
                // this element has value
                // which means current element makes sense for chunk action.
                final StringWriter tempOutput = (StringWriter) output;
                output = new StringWriter();
                final NodeList children = topicref.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    final Node current = children.item(i);
                    if (MAP_TOPICREF.matches(current)) {
                        processChunk((Element) current, outputFileName);
                    }
                }

                // merge results
                final StringBuffer parentResult = tempOutput.getBuffer();
                final CharSequence tmpContent = ((StringWriter) output).getBuffer();
                // Skip empty parents and @processing-role='resource-only' entries.
                // append into root topic
                if (parentResult.length() > 0
                        && parseFilePath != null
                        && !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleValue)) {
                    insertAfter(hrefValue, parentResult, tmpContent);
                // replace contents
                } else {
                    parentResult.append(tmpContent);
                }
                // restore back to parent's output this is a different temp
                output = tempOutput;
            }

            if (chunkValue.contains(CHUNK_TO_CONTENT)) {
                final String tmpContent = output.toString();
                writeToContentChunk(tmpContent, outputFileName, needWriteDitaTag);
                // restore back original output
                output = tempWriter;
                topicID = tempTopicID;
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Append XML content into root element
     *
     * @param hrefValue href of the topicref
     * @param parentResult XML content to insert into
     * @param tmpContent XML content to insert
     */
    private void insertAfter(final URI hrefValue, final StringBuffer parentResult, final CharSequence tmpContent) {
        int insertpoint = parentResult.lastIndexOf("</");
        final int end = parentResult.indexOf(">", insertpoint);

        if (insertpoint == -1 || end == -1) {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ033E", hrefValue.toString()).toString());
        } else {
            if (ELEMENT_NAME_DITA.equals(parentResult.substring(insertpoint, end).trim())) {
                insertpoint = parentResult.lastIndexOf("</", insertpoint - 1);
            }
            parentResult.insert(insertpoint, tmpContent);
        }
    }

    // flush the buffer to file after processing is finished
    private void writeToContentChunk(final String tmpContent, final URI outputFileName, final boolean needWriteDitaTag) throws IOException {
        assert outputFileName.isAbsolute();
        logger.info("Writing " + outputFileName);
        OutputStreamWriter ditaFileOutput = null;
        try {
            ditaFileOutput = new OutputStreamWriter(new FileOutputStream(new File(outputFileName)), UTF8);
            if (outputFileName.equals(changeTable.get(outputFileName))) {
                // if the output file is newly generated file
                // write the xml header and workdir PI into new file
                writeStartDocument(ditaFileOutput);
                final URI workDir = outputFileName.resolve(".");
                if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                    writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET, new File(workDir).getAbsolutePath());
                } else {
                    writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET, UNIX_SEPARATOR + new File(workDir).getAbsolutePath());
                }
                writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET_URI, workDir.toString());

                if (conflictTable.get(outputFileName) != null) {
                    final String relativePath = getRelativeUnixPath(new File(filePath) + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP,
                            new File(conflictTable.get(outputFileName)).getAbsolutePath());
                    String path2project = getRelativeUnixPath(relativePath);
                    if (null == path2project) {
                        path2project = "";
                    }
                    writeProcessingInstruction(ditaFileOutput, PI_PATH2PROJ_TARGET, path2project);
                    writeProcessingInstruction(ditaFileOutput, PI_PATH2PROJ_TARGET_URI, path2project.isEmpty() ? "./" : toURI(path2project).toString());
                }
            }
            if (needWriteDitaTag) {
                final AttributesImpl atts = new AttributesImpl();
                addOrSetAttribute(atts, ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
                addOrSetAttribute(atts, ATTRIBUTE_PREFIX_DITAARCHVERSION + COLON + ATTRIBUTE_NAME_DITAARCHVERSION, "1.2");
                writeStartElement(ditaFileOutput, ELEMENT_NAME_DITA, atts);
            }
            // write the final result to the output file
            ditaFileOutput.write(tmpContent);
            if (needWriteDitaTag) {
                writeEndElement(ditaFileOutput, ELEMENT_NAME_DITA);
            }
            ditaFileOutput.flush();
        } finally {
            if (ditaFileOutput != null) {
                ditaFileOutput.close();
            }
        }
    }

}
