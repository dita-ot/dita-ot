/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.writer.CoderefResolver;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;

public class AnchorRange extends AllRange implements Range {

    private final String start;
    private final String end;
    private int include;

    public AnchorRange(final String start, final String end) {
        this.start = start;
        this.end = end;
        include = start != null ? -1 : 1;
    }

    @Override
    public void copyLines(final BufferedReader codeReader) throws IOException, SAXException {
        boolean first = true;
        String line;
        while ((line = codeReader.readLine()) != null) {
            if (include == -1 && start != null) {
                include = line.contains(start) ? 0 : -1;
            } else if (include > -1 && end != null) {
                include = line.contains(end) ? -1 : include;
            }
            if (include > 0) {
                if (first) {
                    first = false;
                } else {
                    handler.characters(CoderefResolver.XML_NEWLINE, 0, CoderefResolver.XML_NEWLINE.length);
                }
                final char[] ch = line.toCharArray();
                handler.characters(ch, 0, ch.length);
            }
            if (include >= 0) {
                include++;
            }
        }
    }
}
