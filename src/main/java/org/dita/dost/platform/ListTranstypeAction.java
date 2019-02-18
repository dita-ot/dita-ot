/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * List transtypes integration action.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
final class ListTranstypeAction extends ImportAction {

    /**
     * Get result.
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String separator = paramTable.getOrDefault("separator", "|");
        final List<String> v = valueSet.stream()
                .map(fileValue -> fileValue.value)
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(v);
        final StringBuilder retBuf = new StringBuilder();
        for (final Iterator<String> i = v.iterator(); i.hasNext();) {
            retBuf.append(i.next());
            if (i.hasNext()) {
                retBuf.append(separator);
            }
        }
        final char[] ret = retBuf.toString().toCharArray();
        buf.characters(ret, 0, ret.length);
    }

    @Override
    public String getResult() {
        throw new UnsupportedOperationException();
    }

}
