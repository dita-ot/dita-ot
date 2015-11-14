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
        // pass map's directory path
        filePath = fileDir;
        try {
            output = new StringWriter();
            processChunk(rootTopicref, null);
            if (!copyto.isEmpty()) {
                updateList();
            }
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

    private void processChunk(final Element topicref, final File outputFile) {
        final String hrefValue = getValue(topicref, ATTRIBUTE_NAME_HREF);
        final Collection<String> chunkValue = split(getValue(topicref, ATTRIBUTE_NAME_CHUNK));
        final String copytoValue = getValue(topicref, ATTRIBUTE_NAME_COPY_TO);
        final String scopeValue = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
        final String classValue = getValue(topicref, ATTRIBUTE_NAME_CLASS);
        final String processRoleValue = getCascadeValue(topicref, ATTRIBUTE_NAME_PROCESSING_ROLE);

        File outputFileName = outputFile;
        Writer tempWriter = null;
        Set<String> tempTopicID = null;

        targetTopicId = null;
        selectMethod = CHUNK_SELECT_DOCUMENT;
        include = false;

        boolean needWriteDitaTag = true;

        try {
            String parseFilePath;
            if (copytoValue != null && !chunkValue.contains(CHUNK_TO_CONTENT)) {
                if (getFragment(hrefValue) != null) {
                    parseFilePath = setFragment(copytoValue, getFragment(hrefValue));
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
                if (getFragment(hrefValue) != null) {
                    copytoSource.add(stripFragment(hrefValue));
                    copytotarget2source.put(toURI(copytoValue), toURI(stripFragment(hrefValue)));
                } else {
                    copytoSource.add(hrefValue);
                    copytotarget2source.put(toURI(copytoValue), toURI(hrefValue));
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
                        outputFileName = resolve(filePath, parseFilePath);
                        needWriteDitaTag = false;
                    } else if (copytoValue != null) {
                        // use @copy-to value as the new file name
                        outputFileName = resolve(filePath, copytoValue);
                    } else if (hrefValue != null) {
                        // try to use href value as the new file name
                        if (chunkValue.contains(CHUNK_SELECT_TOPIC)
                                || chunkValue.contains(CHUNK_SELECT_BRANCH)) {
                            if (getFragment(hrefValue) != null) {
                                // if we have an ID here, use it.
                                outputFileName = resolve(filePath, getFragment(hrefValue) + FILE_EXTENSION_DITA);
                            } else {
                                // Find the first topic id in target file if
                                // any.
                                final String firstTopic = getFirstTopicId(resolve(filePath, hrefValue).getPath());
                                if (firstTopic != null) {
                                    outputFileName = resolve(filePath, firstTopic + FILE_EXTENSION_DITA);
                                } else {
                                    outputFileName = resolve(filePath, hrefValue);
                                }
                            }
                        } else {
                            // otherwise, use the href value instead
                            outputFileName = resolve(filePath, hrefValue);
                        }
                    } else {
                        // use randomly generated file name
                        outputFileName = resolve(filePath, generateFilename());
                    }

                    // Check if there is any conflict
                    if (outputFileName.exists() && !MAP_MAP.matches(classValue)) {
                        final File t = outputFileName;
                        outputFileName = resolve(filePath, generateFilename());
                        conflictTable.put(outputFileName.getPath(), t.getPath());
                    }
                    // add newly generated file to changTable
                    // the new entry in changeTable has same key and value
                    // in order to indicate it is a newly generated file
                    changeTable.put(outputFileName.getPath(), outputFileName.getPath());
                }
                // "by-topic" couldn't reach here
                this.outputFile = outputFileName;

                final String path = resolveTopic(filePath, parseFilePath);
                // FIXME: Should be URI
                String newpath;
                if (getFragment(path) != null) {
                    newpath = setFragment(outputFileName.getPath(), getFragment(path));
                } else {
                    final String firstTopicID = getFirstTopicId(path);
                    if (firstTopicID != null) {
                        newpath = setFragment(outputFileName.getPath(), firstTopicID);
                    } else {
                        newpath = outputFileName.getPath();
                    }
                }
                // add file name changes to changeTable, this will be
                // used in
                // TopicRefWriter's updateHref method, very important!!!
                changeTable.put(path, newpath);
                // update current element's @href value
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, getRelativeUnixPath(filePath + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP, newpath));

                if (getFragment(parseFilePath) != null) {
                    targetTopicId = getFragment(parseFilePath);
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
                final File tempPath = currentParsingFile;
                currentParsingFile = resolve(filePath, parseFilePath);

                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleValue)) {
                    currentParsingFileTopicIDChangeTable = new HashMap<>();
                    // TODO recursive point
                    logger.info("Processing " + currentParsingFile.toURI());
                    reader.parse(currentParsingFile.toURI().toString());
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
    private void insertAfter(String hrefValue, StringBuffer parentResult, CharSequence tmpContent) {
        int insertpoint = parentResult.lastIndexOf("</");
        final int end = parentResult.indexOf(">", insertpoint);

        if (insertpoint == -1 || end == -1) {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ033E", hrefValue).toString());
        } else {
            if (ELEMENT_NAME_DITA.equals(parentResult.substring(insertpoint, end).trim())) {
                insertpoint = parentResult.lastIndexOf("</", insertpoint - 1);
            }
            parentResult.insert(insertpoint, tmpContent);
        }
    }

    // flush the buffer to file after processing is finished
    private void writeToContentChunk(final String tmpContent, final File outputFileName, final boolean needWriteDitaTag) throws IOException {
        logger.info("Writing " + outputFileName);
        OutputStreamWriter ditaFileOutput = null;
        try {
            ditaFileOutput = new OutputStreamWriter(new FileOutputStream(outputFileName), UTF8);
            if (outputFileName.getPath().equals(changeTable.get(outputFileName.getPath()))) {
                // if the output file is newly generated file
                // write the xml header and workdir PI into new file
                writeStartDocument(ditaFileOutput);
                final File workDir = outputFileName.getParentFile().getAbsoluteFile();
                if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                    writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET, workDir.getAbsolutePath());
                } else {
                    writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET, UNIX_SEPARATOR + workDir.getAbsolutePath());
                }
                writeProcessingInstruction(ditaFileOutput, PI_WORKDIR_TARGET_URI, workDir.toURI().toString());

                if ((conflictTable.get(outputFileName.getPath()) != null)) {
                    final String relativePath = getRelativeUnixPath(filePath + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP, conflictTable.get(outputFileName.getPath()));
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
