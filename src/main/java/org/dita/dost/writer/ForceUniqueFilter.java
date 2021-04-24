/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.chunk.ChunkModule.isLocalScope;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getExtension;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.URLUtils.*;

/**
 * Create copy-to attributes for duplicate topicrefs.
 */
public final class ForceUniqueFilter extends AbstractXMLFilter {

    private final Map<URI, Integer> topicrefCount = new HashMap<>();
    private final Deque<Boolean> ignoreStack = new ArrayDeque<>();
    /**
     * Generated copy-to mappings, key is target topic and value is source topic.
     */
    public final Map<FileInfo, FileInfo> copyToMap = new HashMap<>();
    private TempFileNameScheme tempFileNameScheme;

    // ContentHandler methods

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        ignoreStack.push(MAP_RELTABLE.matches(atts) ? false : ignoreStack.isEmpty() || ignoreStack.peek());

        Attributes res = atts;
        if (ignoreStack.peek() && MAP_TOPICREF.matches(res)) {
            final URI href = toURI(res.getValue(ATTRIBUTE_NAME_HREF));
            final URI copyTo = toURI(res.getValue(ATTRIBUTE_NAME_COPY_TO));
            final URI source = copyTo != null ? copyTo : href;
            final String scope = res.getValue(ATTRIBUTE_NAME_SCOPE);
            final String format = res.getValue(ATTRIBUTE_NAME_FORMAT);
            // FIXME: handle cascading
            final String processingRole = res.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
            if (source != null &&
                    isLocalScope(scope) &&
                    (format == null || format.equals(ATTR_FORMAT_VALUE_DITA)) &&
                    (processingRole == null || processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_NORMAL))) {
                final URI file = stripFragment(source);
                Integer count = topicrefCount.getOrDefault(file, 0);
                count++;
                topicrefCount.put(file, count);
                if (count > 1) { // not only reference to this topic
                    final FileInfo srcFi = job.getFileInfo(currentFile.resolve(stripFragment(source)));
                    if (srcFi != null) {
                        final FileInfo dstFi = generateCopyToTarget(srcFi, count);
                        copyToMap.put(dstFi, srcFi);

                        final URI dstTempAbs = job.tempDirURI.resolve(dstFi.uri);
                        final URI targetRel = getRelativePath(currentFile, dstTempAbs);
                        final URI target = setFragment(targetRel, href.getFragment());

                        final AttributesImpl buf = new AttributesImpl(atts);
                        XMLUtils.addOrSetAttribute(buf, ATTRIBUTE_NAME_COPY_TO, target.toString());
                        res = buf;
                    }
                }
            }
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);

        ignoreStack.pop();
    }

    private FileInfo generateCopyToTarget(final FileInfo srcFi, final int count) {
        for (int i = 0; ; i++) {
            final StringBuilder ext = new StringBuilder();
            ext.append('_');
            ext.append(Integer.toString(count));
            if (i > 0) {
                ext.append('_');
                ext.append(Integer.toString(i));
            }
            ext.append('.');
            ext.append(getExtension(srcFi.result.toString()));

            final URI dstResult = toURI(replaceExtension(srcFi.result.toString(), ext.toString()));
            final URI dstTemp = tempFileNameScheme.generateTempFileName(dstResult);
            if (job.getFileInfo(dstTemp) == null) {
                return new FileInfo.Builder(srcFi)
                        .uri(dstTemp)
                        .result(dstResult)
                        .build();
            }
        }
    }

    public void setTempFileNameScheme(TempFileNameScheme tempFileNameScheme) {
        this.tempFileNameScheme = tempFileNameScheme;
    }
}
