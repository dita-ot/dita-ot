/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.io.BufferedReader;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

final class IncludeText {

    private final Job job;
    private final URI currentFile;
    private final ContentHandler contentHandler;
    private final DITAOTLogger logger;

    IncludeText(Job job, URI currentFile, ContentHandler contentHandler, DITAOTLogger logger) {
        this.job = job;
        this.currentFile = currentFile;
        this.contentHandler = contentHandler;
        this.logger = logger;
    }

    boolean include(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Charset charset = getCharset(atts.getValue(ATTRIBUTE_NAME_FORMAT), atts.getValue(ATTRIBUTE_NAME_ENCODING));
        final Range range = getRange(hrefValue);
        final File codeFile = getFile(hrefValue);
        if (codeFile != null) {
            try (BufferedReader codeReader = Files.newBufferedReader(codeFile.toPath(), charset)) {
                range.copyLines(codeReader);
            } catch (final Exception e) {
                logger.error("Failed to process include {0}", codeFile, e);
                return false;
            }
        }
        return true;
    }

    private File getFile(URI hrefValue) {
        final File tempFile = toFile(stripFragment(currentFile.resolve(hrefValue))).getAbsoluteFile();
        final URI rel = job.tempDirURI.relativize(tempFile.toURI());
        final Job.FileInfo fi = job.getFileInfo(rel);

//        if (tempFile.exists() && fi != null && PR_D_CODEREF.localName.equals(fi.format)) {
//            return tempFile;
//        }
        if (fi != null && "file".equals(fi.src.getScheme())) {
            return new File(fi.src);
        }
        return null;
    }

    /**
     * Factory method for Range implementation
     */
    private Range getRange(final URI uri) {
        int start = 0;
        int end = Integer.MAX_VALUE;
        String startId = null;
        String endId = null;

        final String fragment = uri.getFragment();
        if (fragment != null) {
            // RFC 5147
            final Matcher m = Pattern.compile("^line=(?:(\\d+)|(\\d+)?,(\\d+)?)$").matcher(fragment);
            if (m.matches()) {
                if (m.group(1) != null) {
                    start = Integer.parseInt(m.group(1));
                    end = start;
                } else {
                    if (m.group(2) != null) {
                        start = Integer.parseInt(m.group(2));
                    }
                    if (m.group(3) != null) {
                        end = Integer.parseInt(m.group(3)) - 1;
                    }
                }
                return new LineNumberRange(start, end).handler(contentHandler);
            } else {
                final Matcher mc = Pattern.compile("^line-range\\((\\d+)(?:,\\s*(\\d+))?\\)$").matcher(fragment);
                if (mc.matches()) {
                    start = Integer.parseInt(mc.group(1)) - 1;
                    if (mc.group(2) != null) {
                        end = Integer.parseInt(mc.group(2)) - 1;
                    }
                    return new LineNumberRange(start, end).handler(contentHandler);
                } else {
                    final Matcher mi = Pattern.compile("^token=([^,\\s)]*)(?:,\\s*([^,\\s)]+))?$").matcher(fragment);
                    if (mi.matches()) {
                        if (mi.group(1) != null && mi.group(1).length() != 0) {
                            startId = mi.group(1);
                        }
                        if (mi.group(2) != null) {
                            endId = mi.group(2);
                        }
                        return new AnchorRange(startId, endId).handler(contentHandler);
                    }
                }
            }
        }

        return new AllRange().handler(contentHandler);
    }

    /**
     * Get code file charset.
     *
     * @param format   format attribute value, may be {@code null}
     * @param encoding encoding attribute balue, may be {@code null}
     * @return charset if set, otherwise default charset
     */
    private Charset getCharset(final String format, final String encoding) {
        Charset c = null;
        try {
            if (encoding != null) {
                c = Charset.forName(encoding);
            } else if (format != null) {
                final String[] tokens = format.trim().split("[;=]");
                if (tokens.length >= 3 && tokens[1].trim().equals(ATTRIBUTE_NAME_CHARSET)) {
                    c = Charset.forName(tokens[2].trim());
                }
            }
        } catch (final RuntimeException e) {
            logger.error(MessageUtils.getMessage("DOTJ052E", encoding).toString());
        }
        if (c == null) {
            final String defaultCharset = Configuration.configuration.get("default.coderef-charset");
            if (defaultCharset != null) {
                c = Charset.forName(defaultCharset);
            } else {
                c = Charset.defaultCharset();
            }
        }
        return c;
    }
}
