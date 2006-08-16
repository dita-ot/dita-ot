/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.DebugAndFilterModule;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * DitaWriter reads dita topic file and insert debug information and filter out the
 * content that is not necessary in the output.
 * 
 * @author Zhang, Yuan Peng
 */
public class DitaWriter extends AbstractXMLWriter {

    private static final String ATTRIBUTE_END = "\"";
    private static final String ATTRIBUTE_XTRC_START = " xtrc=\"";
    private static final String ATTRIBUTE_XTRF_START = " xtrf=\"";
    private static final String COLUMN_NAME_COL = "col";
    private static final String OS_NAME_WINDOWS = "windows";
    private static final String PI_END = "?>";
    private static final String PI_PATH2PROJ_HEAD = "<?path2project ";
    private static final String PI_WORKDIR_HEAD = "<?workdir ";

    
    private static boolean checkDITAHREF(Attributes atts){
    	// TO DO add implementation
    	String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
    	String scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
    	String formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
    	
    	
    	
    	if (classValue == null
    			|| (classValue.indexOf(Constants.ATTR_CLASS_VALUE_XREF) == -1
    			&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_LINK) == -1
    			&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF) == -1))
    	{
    		return false;
    	} 
    	
    	if (scopeValue == null){
    		scopeValue = Constants.ATTR_SCOPE_VALUE_LOCAL;
    	}
    	if (formatValue == null){
    		formatValue = Constants.ATTR_FORMAT_VALUE_DITA;
    	}
    	
    	if (scopeValue.equalsIgnoreCase(Constants.ATTR_SCOPE_VALUE_LOCAL)
    			&& formatValue.equalsIgnoreCase(Constants.ATTR_FORMAT_VALUE_DITA)){
    		return true;
    	}
    	
    	return false;
    }
    
    private static String replaceCONREF (Attributes atts){
    	
    	/*
         * replace all the backslash with slash in 
         * all href and conref attribute
         */
        String attValue = atts.getValue(Constants.ATTRIBUTE_NAME_CONREF);
        if (attValue != null){
        	attValue = attValue.replaceAll(Constants.DOUBLE_BACK_SLASH, Constants.SLASH);
        }
        
        if(attValue.indexOf(Constants.FILE_EXTENSION_DITAMAP) == -1){
        	return FileUtils.replaceExtName(attValue);
        }

    	return attValue;
    }

	private static String replaceHREF (String attName, Attributes atts){
    	
    	String attValue = null;
    	
    	if (attName == null){
    		return null;
    	}
    	
    	attValue = atts.getValue(attName);
        
    	
    	if (attValue != null){
    		/*
             * replace all the backslash with slash in 
             * all href and conref attribute
             */     
    		attValue = attValue.replaceAll(Constants.DOUBLE_BACK_SLASH, Constants.SLASH);
    	} else {
    		return null;
    	}
    	
    	if(checkDITAHREF(atts)){
    		return FileUtils.replaceExtName(attValue);
        }
    	
    	return attValue;
    }
    private String absolutePath;
    private HashMap catalogMap; //map that contains the information from XML Catalog
    private List colSpec;
    private int columnNumber; // columnNumber is used to adjust column name
    private int columnNumberEnd; //columnNumberEnd is the end value for current entry
    private HashMap counterMap;
    private boolean exclude; // when exclude is true the tag will be excluded.
    private int level;// level is used to count the element level in the filtering
    private DITAOTJavaLogger logger;
    private boolean needResolveEntity; //check whether the entity need resolve.
    private OutputStreamWriter output;
    private String path2Project;
    private String props; // contains the attribution specialization from props
    
    private XMLReader reader;
    private String tempDir;
    private String traceFilename;

    /**
     * Default constructor of DitaWriter class.
     */
    public DitaWriter() {
        super();
        exclude = false;
        columnNumber = 1;
        columnNumberEnd = 0;
        catalogMap = CatalogUtils.getCatalog(null);
        absolutePath = null;
        path2Project = null;
        counterMap = null;
        traceFilename = null;
        level = 0;
        needResolveEntity = false;
        output = null;
        tempDir = null;
        colSpec = null;
        props = null;
        logger = new DITAOTJavaLogger();
                
        try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY, Constants.SAX_DRIVER_DEFAULT_CLASS);
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature(Constants.FEATURE_VALIDATION, true); 
            reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);

            
            reader.setEntityResolver(new CatalogResolver());
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    
    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     * 
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!exclude && needResolveEntity) { 
        	// exclude shows whether it's excluded by filtering
        	// isEntity shows whether it's an entity.
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }
    
    /**
	 * @param atts
	 * @throws IOException
	 */
	private void copyElementAttribute(Attributes atts) throws IOException {
		// copy the element's attributes    
		int attsLen = atts.getLength();
		for (int i = 0; i < attsLen; i++) {
		    String attQName = atts.getQName(i);
		    String attValue;
		    
		    if(Constants.ATTRIBUTE_NAME_HREF.equals(attQName)
		    		|| Constants.ATTRIBUTE_NAME_COPY_TO.equals(attQName)){
		        
		        attValue = replaceHREF(attQName, atts);
		        
		    } else if (Constants.ATTRIBUTE_NAME_CONREF.equals(attQName)){
		                                    
		        attValue = replaceCONREF(atts);
		    } else {
		        attValue = atts.getValue(i);
		    }

		    // replace '&' with '&amp;'
			//if (attValue.indexOf('&') > 0) {
				//attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
			//}
		    attValue = StringUtils.escapeXML(attValue);
			
		    //output all attributes except colname
		    if (!Constants.ATTRIBUTE_NAME_COLNAME.equals(attQName)){
		    	output.write(new StringBuffer().append(Constants.STRING_BLANK)
		    			.append(attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
		    			.append(attValue).append(Constants.QUOTATION).toString());
		    }
		}
	}

	/**
	 * @param qName
	 * @param atts
	 * @throws IOException
	 */
	private void copyElementName(String qName, Attributes atts) throws IOException {
		//copy the element name
		output.write(Constants.LESS_THAN + qName);
		if (Constants.ELEMENT_NAME_TGROUP.equals(qName)){
			columnNumber = 1; // initialize the column number
		    columnNumberEnd = 0;
		    colSpec = new ArrayList(Constants.INT_16);
		}else if(Constants.ELEMENT_NAME_ROW.equals(qName)) {
		    columnNumber = 1; // initialize the column number
		    columnNumberEnd = 0;
		}else if(Constants.ELEMENT_NAME_COLSPEC.equals(qName)){
			columnNumber = columnNumberEnd +1;
			if(atts.getValue(Constants.ATTRIBUTE_NAME_COLNAME) != null){
				colSpec.add(atts.getValue(Constants.ATTRIBUTE_NAME_COLNAME));
			}else{
				colSpec.add(COLUMN_NAME_COL+columnNumber);
			}
			columnNumberEnd = columnNumber;
			output.write(new StringBuffer().append(Constants.STRING_BLANK)
		    		.append(Constants.ATTRIBUTE_NAME_COLNAME).append(Constants.EQUAL).append(Constants.QUOTATION)
		    		.append(COLUMN_NAME_COL+columnNumber).append(Constants.QUOTATION).toString());
		}else if(Constants.ELEMENT_NAME_ENTRY.equals(qName)){
			//TO DO
			columnNumber = getStartNumber(atts, columnNumberEnd);
			if(columnNumber > columnNumberEnd){
				output.write(new StringBuffer().append(Constants.STRING_BLANK)
		        		.append(Constants.ATTRIBUTE_NAME_COLNAME).append(Constants.EQUAL).append(Constants.QUOTATION)
		        		.append(COLUMN_NAME_COL+columnNumber).append(Constants.QUOTATION).toString());
			}
			columnNumberEnd = getEndNumber(atts, columnNumber);
		}
	}

	/**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     * 
     */
    public void endCDATA() throws SAXException {
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    
    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     * 
     */
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    
    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     * 
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (exclude) {
            if (level > 0) {
                // If it is the end of a child of an excluded tag, level
                // decrease
                level--;
            } else {
                exclude = false;
            }
        } else { // exclude shows whether it's excluded by filtering
            try {
                output.write(Constants.LESS_THAN + Constants.SLASH 
                        + qName + Constants.GREATER_THAN);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     * 
     */
    public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}

	private int getEndNumber(Attributes atts, int columnStart) {
		int ret;
		if (atts.getValue("nameend") == null){
			return columnStart;
		}else{
			ret = colSpec.indexOf(atts.getValue("nameend")) + 1;
			if(ret == 0){
				return columnStart;
			}
			return ret;
		}
	}

	private int getStartNumber(Attributes atts, int previousEnd) {		
		int ret;
		if (atts.getValue("colnum") != null){
			return new Integer(atts.getValue("colnum")).intValue();
		}else if(atts.getValue("namest") != null){
			ret = colSpec.indexOf(atts.getValue("namest")) + 1;
			if(ret == 0){
				return previousEnd + 1;
			}
			return ret;
		}else if(atts.getValue("colname") != null){
			ret = colSpec.indexOf(atts.getValue("colname")) + 1;
			if(ret == 0){
				return previousEnd + 1;
			}
			return ret;
		}else{
			return previousEnd + 1;
		}
	}

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     * 
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }
		
    
    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     * 
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (catalogMap.get(publicId)!=null){
            File dtdFile = new File((String)catalogMap.get(publicId));
            return new InputSource(dtdFile.getAbsolutePath());
       }
        return null;
    }
    
    /**
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     * 
     */
    public void setContent(Content content) {        
        tempDir = (String) content.getValue();
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     * 
     */
    public void skippedEntity(String name) throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(name);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }
	
	/**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     * 
     */
    public void startCDATA() throws SAXException {
	    try{
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    
    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     * 
     */
    public void startDocument() throws SAXException {
        try {
            output.write(Constants.XML_HEAD);
            output.write(Constants.LINE_SEPARATOR);
            if(Constants.OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS)==-1)
            {
                output.write(PI_WORKDIR_HEAD + absolutePath + PI_END);
            }else{
                output.write(PI_WORKDIR_HEAD + Constants.SLASH + absolutePath + PI_END);
            }
            output.write(Constants.LINE_SEPARATOR);
            if(path2Project != null){
            	output.write(PI_PATH2PROJ_HEAD + path2Project + PI_END);
            }else{
            	output.write(PI_PATH2PROJ_HEAD + PI_END);
            }
            output.write(Constants.LINE_SEPARATOR);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    
    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        Integer value;
        Integer nextValue;
        String domains = null;
        String attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
        
        if (attrValue != null && attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC) != -1){
        	domains = atts.getValue(Constants.ATTRIBUTE_NAME_DOMAINS);
        	props = StringUtils.getExtProps(domains);
        }
        
        if (counterMap.containsKey(qName)) {
            value = (Integer) counterMap.get(qName);
            nextValue = new Integer(value.intValue()+1);
            counterMap.put(qName, nextValue);
        } else {
            nextValue = new Integer(Constants.INT_1);
            counterMap.put(qName, nextValue);
        }

        if (exclude) {
            // If it is the start of a child of an excluded tag, level increase
            level++;
        } else { // exclude shows whether it's excluded by filtering
            if (FilterUtils.needExclude(atts, props)){
                exclude = true;
                level = 0;
            }else{
                try {
                	copyElementName(qName, atts);
                    
                    copyElementAttribute(atts);
                    // write the xtrf and xtrc attributes which contain debug
                    // information
                    output.write(ATTRIBUTE_XTRF_START + traceFilename + ATTRIBUTE_END);
                    output.write(ATTRIBUTE_XTRC_START + qName + Constants.COLON + nextValue.toString() + ATTRIBUTE_END);
                    output.write(Constants.GREATER_THAN);
                    
                } catch (Exception e) {
                	logger.logException(e);
                }// try
            } 
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     * 
     */
    public void startEntity(String name) throws SAXException {
		if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
            	needResolveEntity = StringUtils.checkEntity(name);
            	if(!needResolveEntity){
            		output.write(StringUtils.getEntity(name));
            	}
            } catch (Exception e) {
            	logger.logException(e);
            }
        }

	}

   
    /**
     * @see org.dita.dost.writer.AbstractWriter#write(java.lang.String)
     * 
     */
    public void write(String filename) {
		int index;
		int fileExtIndex;
		File outputFile;
		File dirFile;
		FileOutputStream fileOutput = null;
        exclude = false;
        needResolveEntity = true;
        index = filename.indexOf(Constants.STICK);
        fileExtIndex = filename.endsWith(Constants.FILE_EXTENSION_DITAMAP)
        			 ? -1
        			 : filename.lastIndexOf(Constants.DOT);
        
        try {
        	StringBuffer outputFilename = new StringBuffer(tempDir + File.separator);
            if(index!=-1){
                traceFilename = filename.replace('|',File.separatorChar)
                .replace('/',File.separatorChar).replace('\\',File.separatorChar);
                outputFilename.append((fileExtIndex == -1 || fileExtIndex <= index)
                						?filename.substring(index+1)
                						:filename.substring(index+1, fileExtIndex)+DebugAndFilterModule.extName);
                
                path2Project = FileUtils.getPathtoProject(filename.substring(index+1));
            }else{
                traceFilename = filename;
                outputFilename.append((fileExtIndex == -1)
                					   ? filename
                					   : filename.substring(0, fileExtIndex)+DebugAndFilterModule.extName);
                
                path2Project = FileUtils.getPathtoProject(filename);
            }
            outputFile = new File(outputFilename.toString());
            counterMap = new HashMap();
            dirFile = outputFile.getParentFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            absolutePath = dirFile.getCanonicalPath();
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, Constants.UTF8);

            
            // start to parse the file and direct to output in the temp
            // directory
            reader.parse(traceFilename);
            
            output.close();
        } catch (Exception e) {
        	logger.logException(e);
        }finally {
            try {
                fileOutput.close();
            }catch (Exception e) {
            	logger.logException(e);
            }
        }
    }
}
