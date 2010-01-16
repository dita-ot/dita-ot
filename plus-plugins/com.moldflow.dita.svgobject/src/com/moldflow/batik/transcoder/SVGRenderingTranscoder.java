package com.moldflow.batik.transcoder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.awt.Dimension;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGeneratorContext;

/* SVGRenderingTranscoder
 * Transcodes an SVG document into an SVG document, stroking
 * any text as paths.
 * 
 * Author: Deborah Pickett, Moldflow Corporation, deborah_pickett@moldflow.com
 * Revision: 20080618
 * 
 * Use like this:
 *   SVGRenderingTranscoder t = new SVGRenderingTranscoder();
 *
 *   String inURI = new File(args[0]).toURL().toString();
 *   TranscoderInput input = new TranscoderInput(inURI);
 *   OutputStream ostream = new FileOutputStream("out.svg");
 *   Writer writer = new OutputStreamWriter(ostream, "UTF-8");
 *   TranscoderOutput output = new TranscoderOutput(writer);
 *
 *   t.transcode(input, output);
 *
 *   ostream.flush();
 *   ostream.close();
 *   System.exit(0);
 */
public class SVGRenderingTranscoder extends SVGAbstractTranscoder {

	public SVGRenderingTranscoder() {
		hints.put(KEY_TEXT_AS_SHAPES, Boolean.FALSE);
	}

    public static final TranscodingHints.Key KEY_TEXT_AS_SHAPES
    = new BooleanKey();
    
    public void transcode(Document document,
			String uri,
			TranscoderOutput output)
	throws TranscoderException
	{
		// Read SVG input and create the GVT tree.
		super.transcode(document, uri, output);

		// Create result document to render into.
		Document doc;
		if (output.getDocument() == null) {
			DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
			doc = domImpl.createDocument(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_SVG_TAG, null);
		} else doc = output.getDocument();

		// Render GVT tree into document.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(
				SVGGeneratorContext.createDefault(doc), 
				((Boolean) hints.get(KEY_TEXT_AS_SHAPES)).booleanValue());
		// Get size of canvas to render into.
		Dimension d = new Dimension();
		d.setSize(width, height);
		svgGenerator.setSVGCanvasSize(d);

		// Paint the GVT tree into the SVG generator.
		this.root.paint(svgGenerator);

		// Output the resulting document to the output transcoder.
		try {
			OutputStream os = output.getOutputStream();
			if (os != null) {
				svgGenerator.stream(svgGenerator.getRoot(), new OutputStreamWriter(os), false, false);
				return;
			}

			// Writer
			Writer wr = output.getWriter();
			if (wr != null) {
				svgGenerator.stream(svgGenerator.getRoot(), wr, false, false);
				return;
			}

			// URI
			String outputuri = output.getURI();
			if ( outputuri != null ){
				try{
					URL url = new URL(outputuri);
					URLConnection urlCnx = url.openConnection();
					os = urlCnx.getOutputStream();
					svgGenerator.stream(svgGenerator.getRoot(), new OutputStreamWriter(os), false, false);
					return;
				} catch (MalformedURLException e){
					handler.fatalError(new TranscoderException(e));
				} catch (IOException e){
					handler.fatalError(new TranscoderException(e));
				}
			}
		} catch (Exception ex) {
			throw new TranscoderException(ex);
		} 
	}
}
