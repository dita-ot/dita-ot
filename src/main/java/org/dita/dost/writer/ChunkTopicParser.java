/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getRelativeUnixPath;
import static org.dita.dost.util.StringUtils.split;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

/**
 * Combine topic into a single file for {@code to-content} chunking.
 * Not reusable and not thread-safe.
 */
public final class ChunkTopicParser extends AbstractChunkTopicParser {

    /**
     * Constructor.
     */
    public ChunkTopicParser() {
        super();
//        try {
//            reader = getXMLReader();
//            reader.setContentHandler(this);
//            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
//        } catch (final Exception e) {
//            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
//        }
    }

    @Override
    public void write(final URI currentFile) {
        this.currentFile = currentFile;
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
        final String formatValue = getValue(topicref, ATTRIBUTE_NAME_FORMAT);

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

            if (parseFilePath != null && !ATTR_SCOPE_VALUE_EXTERNAL.equals(scopeValue) && isFormatDita(formatValue)) {
                // now the path to target file make sense
                if (chunkValue.contains(CHUNK_TO_CONTENT)) {
                    // if current element contains "to-content" in chunk attribute
                    // we need to create new buffer and flush the buffer to file
                    // after processing is finished
                    tempWriter = output;
                    tempTopicID = topicID;
                    output = new StringWriter();
                    topicID = new HashSet<>();
                    if (MAP_MAP.matches(classValue)) {
                        // Very special case, we have a map element with href value.
                        // This is a map that needs to be chunked to content.
                        // No need to parse any file, just generate a stub output.
                        outputFileName = currentFile.resolve(parseFilePath);
                        needWriteDitaTag = false;
                    } else if (copytoValue != null) {
                        // use @copy-to value as the new file name
                        outputFileName = currentFile.resolve(copytoValue);
                    } else if (hrefValue != null) {
                        // try to use href value as the new file name
                        if (chunkValue.contains(CHUNK_SELECT_TOPIC)
                                || chunkValue.contains(CHUNK_SELECT_BRANCH)) {
                            if (hrefValue.getFragment() != null) {
                                // if we have an ID here, use it.
                                outputFileName = currentFile.resolve(hrefValue.getFragment() + FILE_EXTENSION_DITA);
                            } else {
                                // Find the first topic id in target file if any.
                                final String firstTopic = getFirstTopicId(new File(stripFragment(currentFile.resolve(hrefValue))));
                                if (firstTopic != null) {
                                    outputFileName = currentFile.resolve(firstTopic + FILE_EXTENSION_DITA);
                                } else {
                                    outputFileName = currentFile.resolve(hrefValue);
                                }
                            }
                        } else {
                            // otherwise, use the href value instead
                            outputFileName = currentFile.resolve(hrefValue);
                        }
                    } else {
                        // use randomly generated file name
                        outputFileName = generateOutputFile(currentFile);
                    }

                    // Check if there is any conflict
                    if (job.getStore().exists(outputFileName) && !MAP_MAP.matches(classValue)) {
                        final URI t = outputFileName;
                        outputFileName = generateOutputFile(currentFile);
                        conflictTable.put(outputFileName, t);
                    }
                    // add newly generated file to changTable
                    // the new entry in changeTable has same key and value
                    // in order to indicate it is a newly generated file
                    changeTable.put(outputFileName, outputFileName);

                    final FileInfo fi = generateFileInfo(outputFileName);
                    job.add(fi);
                }
                // "by-topic" couldn't reach here
                this.outputFile = outputFileName;

                final URI path = currentFile.resolve(parseFilePath);
                URI newpath;
                if (path.getFragment() != null) {
                    newpath = setFragment(outputFileName, path.getFragment());
                } else {
                    final String firstTopicID = getFirstTopicId(new File(path));
                    if (firstTopicID != null) {
                        newpath = setFragment(outputFileName, firstTopicID);
                    } else {
                        newpath = outputFileName;
                    }
                }
                // add file name changes to changeTable, this will be used in
                // TopicRefWriter's updateHref method, very important!!!
                changeTable.put(path, newpath);
                // update current element's @href value
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, getRelativePath(currentFile.resolve(FILE_NAME_STUB_DITAMAP), newpath).toString());

                if (parseFilePath.getFragment() != null) {
                    targetTopicId = parseFilePath.getFragment();
                }

                final String s = getChunkByToken(chunkValue, "select-", null);
                if (s != null) {
                    selectMethod = s;
                    // if the current topic href referred to a entire
                    // topic file, it will be handled in "document" level.
                    if (targetTopicId == null) {
                        selectMethod = CHUNK_SELECT_DOCUMENT;
                    }
                }
                final URI tempPath = currentParsingFile;
                currentParsingFile = currentFile.resolve(parseFilePath);

                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleValue)) {
                    currentParsingFileTopicIDChangeTable = new HashMap<>();
                    // TODO recursive point
                    logger.info("Processing " + currentParsingFile);
                    job.getStore().transform(currentParsingFile, this);
//                    reader.parse(currentParsingFile.toString());
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
                // if current element has child nodes and chunk results for this element has value
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
     * @param hrefValue    href of the topicref
     * @param parentResult XML content to insert into
     * @param tmpContent   XML content to insert
     */
    private void insertAfter(final URI hrefValue, final StringBuffer parentResult, final CharSequence tmpContent) {
        int insertpoint = parentResult.lastIndexOf("</");
        final int end = parentResult.indexOf(">", insertpoint);

        if (insertpoint == -1 || end == -1) {
            logger.error(MessageUtils.getMessage("DOTJ033E", hrefValue.toString()).toString());
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
        try (Writer ditaFileOutput = new OutputStreamWriter(job.getStore().getOutputStream(outputFileName), StandardCharsets.UTF_8)) {
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

                final File path2rootmap = toFile(getRelativePath(outputFileName, job.getInputMap())).getParentFile();
                writeProcessingInstruction(ditaFileOutput, PI_PATH2ROOTMAP_TARGET_URI, path2rootmap == null ? "./" : toURI(path2rootmap).toString());

                if (conflictTable.get(outputFileName) != null) {
                    final String relativePath = getRelativeUnixPath(new File(currentFile.resolve(".")) + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP,
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
                addOrSetAttribute(atts, ATTRIBUTE_PREFIX_DITAARCHVERSION + COLON + ATTRIBUTE_NAME_DITAARCHVERSION, "1.3");
                writeStartElement(ditaFileOutput, ELEMENT_NAME_DITA, atts);
            }
            // write the final result to the output file
            ditaFileOutput.write(tmpContent);
            if (needWriteDitaTag) {
                writeEndElement(ditaFileOutput, ELEMENT_NAME_DITA);
            }
            ditaFileOutput.flush();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String id = atts.getValue(ATTRIBUTE_NAME_ID);

        if (skip && skipLevel > 0) {
            skipLevel++;
        }

        if (TOPIC_TOPIC.matches(cls)) {
            topicSpecSet.add(qName);

            processSelect(id);
        }

        if (include) {
            includelevel++;

            AttributesImpl resAtts = new AttributesImpl(checkForNSDeclaration(atts,uri));
            writeStartElement(output, qName, resAtts);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {

        //pop the namespace level
        if (uri != "") {
            if (namespaceMap.containsKey(uri)){
                decreaseNamespaceLevel(uri);
            }
        }

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
        }
    }


    private void increaseNamespaceLevel (String uri){
        namespaceMap.put(uri, namespaceMap.get(uri) + 1);

    }

    private void decreaseNamespaceLevel (String uri){
        namespaceMap.put(uri, namespaceMap.get(uri) - 1);
    }

    /**
     * Check if we need to add 'xmlns' attribute with prefix
     *
     * @param atts
     * @param uri
     *
     * @return updated attributes
     */
    private Attributes checkForNSDeclaration (Attributes atts, String uri){
        AttributesImpl resAtts = null;

        //This part is to handle namespace declaration in the content.
        if (uri != "") {
            if (namespaceMap.containsKey(uri)){
                increaseNamespaceLevel(uri);
            }else {
                namespaceMap.put(uri, 1);
            }
            resAtts =  new AttributesImpl(processAttributesNS(atts,uri));
        }else {
            resAtts = new AttributesImpl(processAttributes(atts));        }

        return resAtts;
    }

}
