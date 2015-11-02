/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import org.dita.dost.util.Job;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getExtension;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.URLUtils.*;

/**
 * Create copy-to attributes for duplicate topicrefs.
 */
public final class ForceUniqueFilter extends AbstractXMLFilter {

    /** Absolute path to current source file. */
    private URI currentFile;
    private final Map<URI, Integer> topicrefCount = new HashMap<>();
    private final Deque<Boolean> ignoreStack = new ArrayDeque<>();
    /** Generated copy-to mappings, key is target topic and value is source topic. */
    public final Map<URI, URI> copyToMap = new HashMap<>();

    public void setCurrentFile(final URI currentFile) {
        this.currentFile = currentFile;
    }

    // ContentHandler methods

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        ignoreStack.push(MAP_RELTABLE.matches(atts) ? false : ignoreStack.isEmpty() || ignoreStack.peek());

        Attributes res = atts;
        if (ignoreStack.peek() && MAP_TOPICREF.matches(res)) {
            final URI href = toURI(res.getValue(ATTRIBUTE_NAME_HREF));
            final String scope = res.getValue(ATTRIBUTE_NAME_SCOPE);
            final String format = res.getValue(ATTRIBUTE_NAME_FORMAT);
            // FIXME: handle cascading
            final String processingRole = res.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
            if (href != null &&
                    (scope == null || scope.equals(ATTR_SCOPE_VALUE_LOCAL)) &&
                    (format == null || format.equals(ATTR_FORMAT_VALUE_DITA)) &&
                    (processingRole == null || processingRole.equals(ATTR_PROCESSING_ROLE_VALUE_NORMAL))) {
                final URI file = stripFragment(href);
                Integer count = topicrefCount.containsKey(file) ? topicrefCount.get(file) : 0;
                count++;
                topicrefCount.put(file, count);
                if (count > 1) { // not only reference to this topic
                    final String copyTo = res.getValue(ATTRIBUTE_NAME_COPY_TO);
                    if (copyTo == null) { // skip we there is already a copy-to
                        final URI generatedCopyTo = generateCopyToTarget(href, count);

                        final URI source = currentFile.resolve(file);
                        final URI target = currentFile.resolve(stripFragment(generatedCopyTo));
                        final URI relSource = getRelativePath(job.getInputDir(), source);
                        final URI relTarget = getRelativePath(job.getInputDir(), target);
                        copyToMap.put(relTarget, relSource);

                        final AttributesImpl buf = new AttributesImpl(atts);
                        XMLUtils.addOrSetAttribute(buf, ATTRIBUTE_NAME_COPY_TO, generatedCopyTo.toString());
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

    private URI generateCopyToTarget(final URI src, final int count) {
        final URI path = stripFragment(src);
        final String fragment = src.getFragment();
        for (int i = 0; ; i++) {
            final StringBuilder ext = new StringBuilder();
            ext.append('_');
            ext.append(Integer.toString(count));
            if (i > 0) {
                ext.append('_');
                ext.append(Integer.toString(i));
            }
            ext.append('.');
            ext.append(getExtension(path.toString()));
            final URI dst = toURI(replaceExtension(path.toString(), ext.toString()));
            final URI target = URLUtils.getRelativePath(job.getInputDir().resolve("dummy"), currentFile.resolve(dst));
            if (job.getFileInfo(target) == null) {
                return setFragment(dst, fragment);
            }
        }
    }

}
