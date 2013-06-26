/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Image metadata filter.
 */
public final class ImageMetadataFilter extends AbstractXMLFilter {

    private static final String ATTR_VERTICAL_DPI = "vertical-dpi";
    private static final String ATTR_HORIZONTAL_DPI = "horizontal-dpi";
    private static final String ATTR_IMAGE_HEIGHT = "image-height";
    private static final String ATTR_IMAGE_WIDTH = "image-width";
    private static final float MM_TO_INCH = 25.4f;
    public static final String DITA_OT_PREFIX = "dita-ot";
    public static final String DITA_OT_NS = "http://dita-ot.sourceforge.net/ns/201007/dita-ot";
    
    // Variables ---------------------------------------------------------------

    private final File outputDir;
    private final File tempDir;
    private final String uplevels;
    private File currentFile = null;
    private int depth = 0;
    private final Map<File, Attributes> cache = new HashMap<File, Attributes>();

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     */
    public ImageMetadataFilter(final File outputDir, final File tempDir, final String uplevels) {
        this.outputDir = outputDir;
        this.tempDir = tempDir;
        this.uplevels = uplevels;
    }

    // AbstractWriter methods --------------------------------------------------

    @Override
    public void setContent(final Content content) {
        // NOOP
    }

    @Override
    public void write(final String filename) throws DITAOTException {
        // ignore in-exists file
        if (filename == null || !new File(filename).exists()) {
            return;
        }
        currentFile = new File(filename);
        logger.logInfo("Processing " + filename);
        super.write(filename);
    } 
    
    // XMLFilter methods -------------------------------------------------------

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (TOPIC_IMAGE.matches(atts)) {
            final XMLUtils.AttributesBuilder a = new XMLUtils.AttributesBuilder(atts);
            final File imgInput = getImageFile(atts);
            if (imgInput.exists()) {
                Attributes m = cache.get(imgInput);
                if (m == null) {
                    m = readMetadata(imgInput);
                    cache.put(imgInput, m);
                }
                a.addAll(m);
            }
            depth = 1;
            super.startPrefixMapping(DITA_OT_PREFIX , DITA_OT_NS);
            super.startElement(uri, localName, name, a.build());
        } else {
            if (depth > 0) {
                depth++;
            }
            super.startElement(uri, localName, name, atts);
        }
    }
    
    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        super.endElement(uri, localName, name);
        if (depth > 0) {
            if (depth == 1) {
                super.endPrefixMapping(DITA_OT_PREFIX );
            }
            depth--;
        }
    }

    // Private methods ---------------------------------------------------------
    
    private Attributes readMetadata(final File imgInput) {
        logger.logInfo("Reading " + imgInput);
        final XMLUtils.AttributesBuilder a = new XMLUtils.AttributesBuilder();
        try {
            final ImageInputStream iis = ImageIO.createImageInputStream(imgInput);
            final Iterator<ImageReader> i = ImageIO.getImageReaders(iis);
            if (!i.hasNext()) {
                logger.logInfo("Image " + imgInput + " format not supported");
            } else {
                final ImageReader r = i.next();
                r.setInput(iis);
                final BufferedImage img = r.read(0);
                a.add(DITA_OT_NS, ATTR_IMAGE_WIDTH, DITA_OT_PREFIX + ":" + ATTR_IMAGE_WIDTH, "CDATA", Integer.toString(img.getWidth()));
                a.add(DITA_OT_NS, ATTR_IMAGE_HEIGHT, DITA_OT_PREFIX + ":" + ATTR_IMAGE_HEIGHT, "CDATA", Integer.toString(img.getHeight()));
                final Element node = (Element) r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
                final NodeList hs = node.getElementsByTagName("HorizontalPixelSize");
                if(hs != null && hs.getLength() == 1) {
                    final float v =  Float.parseFloat(((Element) hs.item(0)).getAttribute("value"));
                    final int dpi = Math.round(MM_TO_INCH / v);
                    a.add(DITA_OT_NS, ATTR_HORIZONTAL_DPI, DITA_OT_PREFIX + ":" + ATTR_HORIZONTAL_DPI, "CDATA", Integer.toString(dpi));
                }
                final NodeList vs = node.getElementsByTagName("VerticalPixelSize");
                if(vs != null && vs.getLength() == 1) {
                    final float v =  Float.parseFloat(((Element) vs.item(0)).getAttribute("value"));
                    final int dpi = Math.round(MM_TO_INCH / v);
                    a.add(DITA_OT_NS, ATTR_VERTICAL_DPI, DITA_OT_PREFIX + ":" + ATTR_VERTICAL_DPI, "CDATA", Integer.toString(dpi));
                }
            }
        } catch (final Exception e) {
            logger.logError("Failed to read image " + imgInput + " metadata: " + e.getMessage(), e);
        }
        return a.build();
    }

    private File getImageFile(final Attributes atts) {
        final String fileDir = tempDir.toURI().relativize(currentFile.getParentFile().toURI()).toASCIIString();
        final StringBuilder fileName = new StringBuilder(fileDir).append("./");
        if (OutputUtils.getGeneratecopyouter() != OutputUtils.Generate.OLDSOLUTION) {
            fileName.append(uplevels);
        }
        fileName.append(atts.getValue(ATTRIBUTE_NAME_HREF));
        final URI imgInputUri = outputDir.toURI().resolve(fileName.toString());
        final File imgInput = new File(imgInputUri);
        return imgInput;
    }
    
}
