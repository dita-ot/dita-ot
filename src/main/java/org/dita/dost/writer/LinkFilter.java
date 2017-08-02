/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import com.google.common.annotations.VisibleForTesting;
import org.dita.dost.util.Job;
import org.dita.dost.util.URLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.setFragment;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.addOrSetAttribute;

public class LinkFilter extends AbstractXMLFilter {

    private URI destFile;
    private URI base;

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        Attributes res = atts;

        if (hasLocalDitaLink(atts)) {
            final AttributesImpl resAtts = new AttributesImpl(atts);
            int i = atts.getIndex(ATTRIBUTE_NAME_IMAGEREF);
            if (i == -1) {
                i = atts.getIndex(ATTRIBUTE_NAME_HREF);
            }
            if (i == -1) {
                i = atts.getIndex(ATTRIBUTE_NAME_DATA);
            }
            if (i != -1) {
                final URI resHref = getHref(toURI(atts.getValue(i)));
                addOrSetAttribute(resAtts, atts.getQName(i), resHref.toString());
                res = resAtts;
            }
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    private boolean hasLocalDitaLink(final Attributes atts) {
        final boolean hasHref = atts.getIndex(ATTRIBUTE_NAME_HREF) != -1;
        final boolean notExternal = !ATTR_SCOPE_VALUE_EXTERNAL.equals(atts.getValue(ATTRIBUTE_NAME_SCOPE));
        if (hasHref && notExternal) {
            return true;
        }
        final URI data = toURI(atts.getValue(ATTRIBUTE_NAME_DATA));
        if (data != null && !data.isAbsolute()) {
            return true;
        }
        final URI imageref = toURI(atts.getValue(ATTRIBUTE_NAME_IMAGEREF));
        if (imageref != null && !imageref.isAbsolute()) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    URI getHref(final URI href) {
        if (href.getFragment() != null && (href.getPath() == null || href.getPath().equals(""))) {
            return href;
        }
        final URI targetAbs = stripFragment(currentFile.resolve(href));
        final Job.FileInfo targetFileInfo = job.getFileInfo(targetAbs);
        if (targetFileInfo != null) {
            final URI rel = URLUtils.getRelativePath(base, targetFileInfo.result);
            final URI targetDestFile = job.tempDirURI.resolve(rel);
            final URI relTarget = URLUtils.getRelativePath(destFile, targetDestFile);
            return setFragment(relTarget, href.getFragment());
        } else {
            return href;
        }
    }

    public void setDestFile(final URI destFile) {
        this.destFile = destFile;
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        base = job.getInputDir();
    }
}
