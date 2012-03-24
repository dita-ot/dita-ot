/*
 * (c) Copyright IBM Corp. 2003, 2005 All Rights Reserved.
 */

//-----------------------------------------------------------------------------------------------------------
// ValidateXMLDoc.java
//
// This file is a wrapper class for ParseBySaxParser class.
// It checks to make sure that the user has passed enough information
// for the parser to validate xml documents based internal DTD or XML Schema
// definition or external XML Schema.
//
//-----------------------------------------------------------------------------------------------------------
// Written May 17 2003 by Eric Sirois.
//-----------------------------------------------------------------------------------------------------------
//
//-----------------------------------------------------------------------------------------------------------
//Usage: java ValidateXMLDoc xmlDoc [options]
//-----------------------------------------------------------------------------------------------------------
//The parser will use the instance document DOCTYPE value by default
//options:
//-s                   Validate the instance document using the defined noNamespaceSchemaLocation value.
//[xmlschema]   Validate instance document using an external XML Schema
//
//xmlSchema: The external URI location of a no namespace XML Schema
//       This will override the DTD/XML Schema that is
//       defined in the XML document"
//-----------------------------------------------------------------------------------------------------------

public class ValidateXMLDoc {
    private static String _xmlDoc = null;
    private static String _xmlSchema = null;
    private static String _option = null;

    public static void main(String args[]){

         if (args.length == 0 ){
             printUsage();
             System.exit(1);

          }
					else if (args.length == 1){
						 _xmlDoc = args[0];
						 ParseBySAXParser test = new ParseBySAXParser(_xmlDoc);
					}
          else if (args.length >= 2 ){
             _xmlDoc = args[0];
             _option = args[1];
             if (args.length == 3)_xmlSchema = args[2];
             if (_option.equals("-s")){
                ParseBySAXParser test = new ParseBySAXParser(_xmlDoc, true,_xmlSchema);
			  }else{
                  printUsage();
                  System.exit(1);
            }
          }else{
                  printUsage();
                  System.exit(1);
          }

      }
      /** Prints the usage. */
      private static void printUsage() {

         System.err.println();
         System.err.println("Usage: java ValidateXMLDoc xmlDoc  [options]");
         System.err.println();
		  System.err.println("====================================================");
		  System.err.println("The parser will use the instance document DOCTYPE value by default");
		  System.err.println("options:");
		  System.err.println("-s           Validate the instance document using the defined noNamespaceSchemaLocation value..");
		  System.err.println("[xmlSchema]   Validate instance document using extenal XML Schema.");
		  System.err.println();
		  System.err.println("xmlSchema: The external URI location of a no namespace XML Schema");
		  System.err.println("     This will override the DTD/XML Schema that is \n" +
		   		                          "     defined in the XML document");
		  System.err.println("====================================================");
      }
}