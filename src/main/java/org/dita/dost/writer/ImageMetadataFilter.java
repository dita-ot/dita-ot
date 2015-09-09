/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.binary.Base64;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Job;
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
    private final Map<URI, Attributes> cache = new HashMap<>();
    private final Job job;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     */
    public ImageMetadataFilter(final File outputDir, final Job job) {
        this.outputDir = outputDir;
        this.job = job;
        this.tempDir = job.tempDir;
        this.uplevels = job.getProperty("uplevels");
    }

    // AbstractWriter methods --------------------------------------------------

    @Override
    public void write(final File filename) throws DITAOTException {
        // ignore in-exists file
        if (filename == null || !filename.exists()) {
            return;
        }
        currentFile = filename;
        logger.info("Processing " + filename.getAbsolutePath());
        super.write(filename);
    } 
    
    // XMLFilter methods -------------------------------------------------------

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (TOPIC_IMAGE.matches(atts)) {
            final XMLUtils.AttributesBuilder a = new XMLUtils.AttributesBuilder(atts);
            if (atts.getValue(ATTRIBUTE_NAME_HREF) != null) {
                final URI imgInput = getImageFile(toURI(atts.getValue(ATTRIBUTE_NAME_HREF)));
                if (exists(imgInput)) {
                    Attributes m = cache.get(imgInput);
                    if (m == null) {
                        m = readMetadata(imgInput);
                        cache.put(imgInput, m);
                    }
                    a.addAll(m);
                } else {
                    logger.error("Image file " + imgInput + " not found");
                }
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
    
    private Attributes readMetadata(final URI imgInput) {
        logger.info("Reading " + imgInput);
        final XMLUtils.AttributesBuilder a = new XMLUtils.AttributesBuilder();
        try {
            InputStream in = null;
            ImageReader r = null;
            ImageInputStream iis = null;
            try {
                in = getInputStream(imgInput);
                iis = ImageIO.createImageInputStream(in);
                final Iterator<ImageReader> i = ImageIO.getImageReaders(iis);
                if (!i.hasNext()) {
                    logger.info("Image " + imgInput + " format not supported");
                } else {
                    r = i.next();
                    r.setInput(iis);
                    final int imageIndex = r.getMinIndex();
                    a.add(DITA_OT_NS, ATTR_IMAGE_WIDTH, DITA_OT_PREFIX + ":" + ATTR_IMAGE_WIDTH, "CDATA", Integer.toString(r.getWidth(imageIndex)));
                    a.add(DITA_OT_NS, ATTR_IMAGE_HEIGHT, DITA_OT_PREFIX + ":" + ATTR_IMAGE_HEIGHT, "CDATA", Integer.toString(r.getHeight(imageIndex)));
                    final Element node = (Element) r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
                    final NodeList hs = node.getElementsByTagName("HorizontalPixelSize");
                    if (hs != null && hs.getLength() == 1) {
                        final float v = Float.parseFloat(((Element) hs.item(0)).getAttribute("value"));
                        final int dpi = Math.round(MM_TO_INCH / v);
                        a.add(DITA_OT_NS, ATTR_HORIZONTAL_DPI, DITA_OT_PREFIX + ":" + ATTR_HORIZONTAL_DPI, "CDATA", Integer.toString(dpi));
                    }
                    final NodeList vs = node.getElementsByTagName("VerticalPixelSize");
                    if (vs != null && vs.getLength() == 1) {
                        final float v = Float.parseFloat(((Element) vs.item(0)).getAttribute("value"));
                        final int dpi = Math.round(MM_TO_INCH / v);
                        a.add(DITA_OT_NS, ATTR_VERTICAL_DPI, DITA_OT_PREFIX + ":" + ATTR_VERTICAL_DPI, "CDATA", Integer.toString(dpi));
                    }
                }
            } finally {
                if (r != null) {
                    r.dispose();
                }
                if (iis != null) {
                    iis.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        } catch (final Exception e) {
            logger.error("Failed to read image " + imgInput + " metadata: " + e.getMessage(), e);
        }
        return a.build();
    }

    private InputStream getInputStream(final URI imgInput) throws IOException {
        if (imgInput.getScheme().equals("data")) {
            final String data = imgInput.getSchemeSpecificPart();
            final int separator = data.indexOf(',');
            final String metadata = data.substring(0, separator);
            if (metadata.endsWith(";base64")) {
                logger.info("Base-64 encoded data URI");
                return new ByteArrayInputStream(Base64.decodeBase64(data.substring(separator + 1)));
            } else {
                logger.info("ASCII encoded data URI");
                return new ByteArrayInputStream(data.substring(separator).getBytes());
            }
        } else {
            return imgInput.toURL().openConnection().getInputStream();
        }
    }

    private URI getImageFile(final URI href) {
        URI fileDir = tempDir.toURI().relativize(currentFile.getParentFile().toURI());
        if (job.getGeneratecopyouter() != Job.Generate.OLDSOLUTION) {
            fileDir = fileDir.resolve(uplevels.replace(File.separator, URI_SEPARATOR));
        }
        final URI fileName = fileDir.resolve(href);
        final URI imgInputUri = outputDir.toURI().resolve(fileName);
        return imgInputUri;
    }
    
}
