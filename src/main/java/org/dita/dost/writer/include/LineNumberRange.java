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

public class LineNumberRange extends AllRange implements Range {

    private final int start;
    private final int end;

    public LineNumberRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void copyLines(final BufferedReader codeReader) throws IOException, SAXException {
        boolean first = true;
        String line = codeReader.readLine();
        for (int i = 0; line != null; i++) {
            if (i >= start && i <= end) {
                if (first) {
                    first = false;
                } else {
                    handler.characters(CoderefResolver.XML_NEWLINE, 0, CoderefResolver.XML_NEWLINE.length);
                }
                final char[] ch = line.toCharArray();
                handler.characters(ch, 0, ch.length);
            }
            line = codeReader.readLine();
        }
    }
}
