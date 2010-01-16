package com.moldflow.batik.transcoder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerConfigurationException;

/* SVGScriptExecutor
 * Transcodes an SVG document into an SVG document, processing
 * any onload scripts. 
 * 
 * Author: Deborah Pickett, Moldflow Corporation, deborah_pickett@moldflow.com
 * Revision: 20080618
 * 
 * Use like this:
 *   SVGScriptExecutor t = new SVGScriptExecutor();
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
public class SVGScriptExecutor extends SVGAbstractTranscoder {

    private Transformer transformer;

    public SVGScriptExecutor() throws TranscoderException {
        super();
    	// Allow onload so that elements can resize themselves dynamically.
    	hints.put(KEY_EXECUTE_ONLOAD, Boolean.TRUE);
        // Identity transformer.
        try {
          transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
          throw new TranscoderException(e);
        }
    }

    public SVGScriptExecutor(Transformer t) {
        super();
    	// Allow onload so that elements can resize themselves dynamically.
    	hints.put(KEY_EXECUTE_ONLOAD, Boolean.TRUE);
        transformer = t;
    }

    
    public void transcode(Document document,
    		String uri,
    		TranscoderOutput output)
    throws TranscoderException
    {
    	// Read SVG input, and let it run its script.
    	super.transcode(document, uri, output);

	// Delete the onLoad attribute, now that it is no longer needed.
        document.getDocumentElement().removeAttribute("onload");

    	// Output the resulting document to the output transcoder.
    	try {
    		OutputStream os = output.getOutputStream();
    		if (os != null) {
                        transformer.transform(new DOMSource(document),
                          new StreamResult(os));
    			return;
    		}

    		// Writer
    		Writer wr = output.getWriter();
    		if (wr != null) {
                        transformer.transform(new DOMSource(document),
                          new StreamResult(wr));
    			return;
    		}

    		// URI
    		String outputuri = output.getURI();
    		if ( outputuri != null ){
                        transformer.transform(new DOMSource(document),
                          new StreamResult(outputuri));
    			return;
    		}
    	} catch (Exception ex) {
    		throw new TranscoderException(ex);
    	} 
    }
}
