//-----------------------------------------------------------------------------------------------------------
// TransformUsingXMLSchema.java
//
// This file is a wrapper class for ParseBySaxParser class.
// It checks to make sure that the user has passed enough information
// for the parser to validate xml documents based internal DTD or XML Schema
// definition or external XML Schema.
//
//-----------------------------------------------------------------------------------------------------------
// Written June 17 2003 by Eric Sirois.
//-----------------------------------------------------------------------------------------------------------
//
// (C) Copyright IBM Corp. 2003.  All rights reserved.
//
// US Government Users Restricted Rights Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
//
// The program is provided "as is" without any warranty express or
// implied, including the warranty of non-infringement and the implied
// warranties of merchantibility and fitness for a particular purpose.
// IBM will not be liable for any damages suffered by you as a result
// of using the Program. In no event will IBM be liable for any
// special, indirect or consequential damages or lost profits even if
// IBM has been advised of the possibility of their occurrence. IBM
// will not be liable for any third party claims against you.
//
//-----------------------------------------------------------------------------------------------------------
//Usage: java TransformUsingXMLSchema xmlDoc xsltDoc htmlDoc
//-----------------------------------------------------------------------------------------------------------
//xmlDoc:       The external URI location of an XML document to transform
//xmlSchema: The external URI location of an XSL stylesheet
//htmlDoc:      The external URI location to write the resultant HTML document
//-----------------------------------------------------------------------------------------------------------

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validate the XML input by using SAX to turn on namespace awareness,
 * schema awareness and validation, and report problems to an error
 * handler.
 *
 */
public class TransformUsingXMLSchema extends DefaultHandler {

  private static String _xmlDoc = null;
  private static String _xsltDoc = null;
  private static String _htmlDoc = null;

  private static final String JAXP_SCHEMA_LANGUAGE =
  "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  private static final String W3C_XML_SCHEMA =
  "http://www.w3.org/2001/XMLSchema";

  public static void main(String[] args)  throws Exception  {
    TransformUsingXMLSchema tuxl = new TransformUsingXMLSchema();
    _xmlDoc = args[0];
    _xsltDoc = args[1];
    _htmlDoc = args[2];
    tuxl.transform(  _xmlDoc, _xsltDoc, _htmlDoc);
  }

  void transform ( String xmlDoc, String xsltDoc, String htmlDoc )  throws Exception
   {
     // Since we're going to use a SAX feature, the transformer must support
    // input in the form of a SAXSource.
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if(tfactory.getFeature(SAXSource.FEATURE))
    {
      // Standard way of creating an XMLReader in JAXP 1.1.
      SAXParserFactory pfactory= SAXParserFactory.newInstance();
      pfactory.setNamespaceAware(true); // Very important!
      // Turn on validation.
      pfactory.setValidating(true);
      // Get an XMLReader.
      SAXParser parser = pfactory.newSAXParser();
      XMLReader reader = parser.getXMLReader();

      parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

      // Instantiate an error handler (see the Handler inner class below) that will report any
      // errors or warnings that occur as the XMLReader is parsing the XML input.

      reader.setErrorHandler(this);
      SAXSource source = null;
      Transformer t = null;
      // Standard way of creating a transformer from a URL.
      if (xsltDoc != null){t = tfactory.newTransformer(new StreamSource(xsltDoc));}
      else printUsage();

      // Specify a SAXSource that takes both an XMLReader and a URL.
      if (xmlDoc != null){source = new SAXSource(reader, new InputSource(xmlDoc));}
      else printUsage();
      // Transform to a file.
      try {
        if (htmlDoc != null){t.transform(source, new StreamResult(htmlDoc));}
        else printUsage();
      }
      catch (TransformerException te) {
        // The TransformerException wraps someting other than a SAXParseException
        // warning or error, either of which should be "caught" by the Handler.
        System.out.println("Not a SAXParseException warning or error: " + te.getMessage());
      }

      System.out.println("=====Done=====");
    } else
      System.out.println("tfactory does not support SAX features!");
  }
  private void printUsage (){
    System.err.println();
    System.err.println("Usage: java TransformUsingXMLSchema xmlDoc xsltDoc htmlDoc");
    System.err.println();
    System.err.println("xmlDoc:       The external URI location of an XML document to transform");
    System.err.println("xmlSchema: The external URI location of an XSL stylesheet");
    System.err.println("htmlDoc:      The external URI location to write the resultant HTML document");
    System.err.println("====================================================");
    System.exit(1);
  }
  public void warning (SAXParseException spe) throws SAXException  {
    System.out.println("SAXParseException warning: " + spe.getMessage());
  }

  public void error (SAXParseException spe) 	throws SAXException  {
    System.out.println("SAXParseException error: " + spe.getMessage());
  }

}
