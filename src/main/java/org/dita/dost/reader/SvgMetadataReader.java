/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.writer.ImageMetadataFilter;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reader for SVG dimension metadata.
 */
public class SvgMetadataReader extends AbstractXMLReader {

    private static final String WIDTH_ATTR = "width";
    private static final String HEIGHT_ATTR = "height";
    private static final String SVG_ELEM = "svg";
    private static final String SVG_NS = "http://www.w3.org/2000/svg";

    private ImageMetadataFilter.Dimensions dimensions;

    @Override
    public void startDocument() throws SAXException {
        dimensions = new ImageMetadataFilter.Dimensions();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        if (SVG_NS.equals(uri) &&
                (SVG_ELEM.equals(localName) || SVG_ELEM.equals(qName) ||
                        (qName != null && qName.startsWith(SVG_ELEM + ":")))) {
            dimensions.width = atts.getValue(WIDTH_ATTR);
            dimensions.height = atts.getValue(HEIGHT_ATTR);
        }
    }

    public ImageMetadataFilter.Dimensions getDimensions() {
        return dimensions;
    }

    public static class EmptyEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            return new InputSource(new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            });
        }
    }
}
