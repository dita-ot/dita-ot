//-----------------------------------------------------------------------------------------------------------
// ParseBySAXParser.java
//
// This file sets up the XML parser using JAXP 1.2 to validate XML documents.
// It checks to make sure that the user has passed enough information
// for the parser to validate xml documents based internal DTD or XML Schema
// definition or external XML Schema.
//
//-----------------------------------------------------------------------------------------------------------
// Written May 17 2003 by Eric Sirois.
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

    import javax.xml.parsers.SAXParserFactory;
    import javax.xml.parsers.SAXParser;
    import org.xml.sax.XMLReader;

    import org.xml.sax.InputSource;
    import org.xml.sax.helpers.DefaultHandler;
    import org.xml.sax.SAXException;
    import org.xml.sax.SAXParseException;
    import javax.xml.parsers.ParserConfigurationException;
    import java.io.File;
    import java.io.IOException;

    public class ParseBySAXParser {

        private static boolean anError = false;
        private static ParseBySAXParser parserInstance;

        private static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        private static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";
        private static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

        public ParseBySAXParser (String xmlDoc, boolean schemaValidation, String xmlSchema){
          String _xmlDoc = xmlDoc;
          String _xmlSchema = xmlSchema;
		   boolean _schemaValidation = schemaValidation;
          parserInstance = this;
          validate(_xmlDoc, _schemaValidation, _xmlSchema);
        }
        public ParseBySAXParser (String xmlDoc){
          String _xmlDoc = xmlDoc;
          String _xmlSchema = null;
		   boolean _schemaValidation = false;
          parserInstance = this;
          validate(_xmlDoc, _schemaValidation, _xmlSchema);
        }

        private void validate(String xmlDoc, boolean schemaValidation, String xmlSchema){
          SAXParser parser = null;
          XMLReader reader = null;
          SAXParserFactory spf = null;

          try {
              //Certain features and properties need to be turned on/off in a specific sequence to be
              //able to validate the instance document in differrent use cases.
              if(schemaValidation == true) {
                   //Using JAXP 1.2 to validate an XML document using an external schema.
	               if (xmlSchema != null) {
	                 spf = SAXParserFactory.newInstance();
	                 parser= spf.newSAXParser();
                    reader = parser.getXMLReader();

                    parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
	                 parser.setProperty(JAXP_SCHEMA_SOURCE, xmlSchema);

	                 spf.setNamespaceAware(true);
                    spf.setValidating(true);
                   }
                   //Using JAXP 1.2 to validate an XML document using the xsi:noNamespaceSchemaLocation value.
                   else {
                        spf = SAXParserFactory.newInstance();

                        spf.setNamespaceAware(true);
                        spf.setValidating(true);

                        parser= spf.newSAXParser();
                        reader = parser.getXMLReader();

                        parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                    }

	          }
	          //Validating an XML document using the DOCTYPE value.
 	          else{
                    spf = SAXParserFactory.newInstance();
                    parser= spf.newSAXParser();
                    reader = parser.getXMLReader();
                    //Validate document using DTD.
                   reader.setFeature("http://xml.org/sax/features/validation", true);

	           }
	          Handler dh = new Handler();

              File _anXMLFile = new File(xmlDoc);
              if (_anXMLFile.exists())  parser.parse(_anXMLFile, dh);
            }
            catch (IOException ioe)  {
                ioe.printStackTrace();
				  setError(true);
            }
            catch(SAXException se) {
	            se.printStackTrace();
				  setError(true);
            }
            catch (ParserConfigurationException pce) {
                pce.printStackTrace();
				  setError(true);
            }
        }


        public void setError(boolean error){
          anError = error;
        }
        public boolean hasErrors(){
          return anError;
        }

       public class Handler extends DefaultHandler {
          public void warning(SAXParseException ex){
            System.err.print("Warning!! -");
            System.err.println(ex);
            //setError(true);
          }
          public void error(SAXParseException ex){
              System.err.print("Error!! -");
              System.err.println(ex);
              //setError(true);
          }
          public void fatalError(SAXParseException ex) throws SAXException{
              System.err.print("Fatal Error!! -");
              System.err.println(ex);
              //setError(true);
          }
          public void endDocument(){
            System.out.println("---------------------------Done-----------------------");
          }
        }
    }