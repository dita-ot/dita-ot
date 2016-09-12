/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ImageMetadataFilter;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SvgMetadataReaderTest {

    private SvgMetadataReader svgMetadataReader;
    private XMLReader reader;

    @Before
    public void setUp() {
        svgMetadataReader = new SvgMetadataReader();
        try {
            reader = XMLUtils.getXMLReader();
        } catch (final SAXException e) {
            throw new RuntimeException(e);
        }
        reader.setContentHandler(svgMetadataReader);
        reader.setEntityResolver(new SvgMetadataReader.EmptyEntityResolver());
    }

    @Test
    public void testImageWithDoctype() throws IOException, SAXException {
        try (final InputStream in = this.getClass().getResourceAsStream("/SvgMetadataReaderTest/SVG_example_markup_grid.svg")) {
            reader.parse(new InputSource(in));
            final ImageMetadataFilter.Dimensions dimensions = svgMetadataReader.getDimensions();
            assertEquals("391", dimensions.width);
            assertEquals("392", dimensions.height);
            assertNull(dimensions.verticalDpi);
            assertNull(dimensions.horizontalDpi);
        }
    }

    @Test
    public void testImageWithoutDoctype() throws IOException, SAXException {
        try (final InputStream in = this.getClass().getResourceAsStream("/SvgMetadataReaderTest/without_doctype.svg")) {
            reader.parse(new InputSource(in));
            final ImageMetadataFilter.Dimensions dimensions = svgMetadataReader.getDimensions();
            assertEquals("391", dimensions.width);
            assertEquals("392", dimensions.height);
            assertNull(dimensions.verticalDpi);
            assertNull(dimensions.horizontalDpi);
        }
    }

}
