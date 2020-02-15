/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.writer.CoderefResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;

public class AllRange implements Range {

    ContentHandler handler;

    @Override
    public Range handler(final ContentHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void copyLines(BufferedReader codeReader) throws IOException, SAXException {
        boolean first = true;
        String line;
        while ((line = codeReader.readLine()) != null) {
            if (first) {
                first = false;
            } else {
                handler.characters(CoderefResolver.XML_NEWLINE, 0, CoderefResolver.XML_NEWLINE.length);
            }
            final char[] ch = line.toCharArray();
            handler.characters(ch, 0, ch.length);
        }
    }
}
