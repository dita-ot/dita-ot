/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.module.Content;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * @author Zhang, Yuan Peng
 */
public class DitaWriter extends AbstractWriter implements ContentHandler,LexicalHandler{

    private XMLReader reader;
    private OutputStreamWriter output;
    private HashMap counterMap;
    private String filename;
    private String absolutePath;
    private String tempDir;
    private boolean exclude; // when exclude is true the tag will be excluded.
    private boolean needResolveEntity; //check whether the entity need resolve.
    private Set filterSet;
    private HashMap filterMap;
    private int level;// level is used to count the element level in the
    // filtering
    private int columnNumber; // columnNumber is used to adjust column name

    /**
     * 
     */
    public DitaWriter() {
        super();
        exclude = false;
        filterMap = new HashMap();
        columnNumber = 0;
        try {
            //SAXParserFactory spFactory = SAXParserFactory.newInstance();
            //spFactory.setValidating(true);
            //spFactory.setFeature("http://xml.org/sax/features/validation", true);
            //SAXParser parser = spFactory.newSAXParser();
            //reader = parser.getXMLReader();
            if (System.getProperty("org.xml.sax.driver") == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * check whether we will filter this tag return true means we will exclude
     * this tag return false means we will include this tag
     */
    private boolean check(String attName, Attributes atts) {
        
        String value = atts.getValue(attName);
        //String temp = value.trim();
        int index;
        boolean ret=false;
        String action;
        
        if (value == null) {
            return false;
        }
        
        index = value.indexOf(' ');
        while(index!=-1){
            action = (String)filterMap.get(attName + "=" + value.substring(0,index));
            if(action != null && action.equals("exclude")){
                ret=true;
            }else if(action != null){
                return false;
            }
            value = value.substring(index+1);
            index = value.indexOf(' ');
        }
        action = (String)filterMap.get(attName + "=" + value);
        if(action != null && action.equals("exclude")){
            ret=true;
        }else if(action != null){
            return false;
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dita.dost.module.AbstractWriter#setContent(org.dita.dost.module.Content)
     */
    public void setContent(Content content) {
        filterSet = (Set) content.getCollection();
        if(filterSet != null){
            Iterator i = filterSet.iterator();
            Map.Entry entry;
            
            while (i.hasNext())
            {
                entry = (Map.Entry)i.next();
                filterMap.put(entry.getKey(), entry.getValue());
            }
        }
        tempDir = (String) content.getObject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dita.dost.module.AbstractWriter#write(java.lang.String)
     */
    public void write(String filename) {
        exclude = false;
        needResolveEntity = true;
        int index;
        File outputFile;
        System.out.println(filename);
        index = filename.indexOf('|');
        try {
            if(index!=-1){
                this.filename = filename.replace('|',File.separatorChar)
                .replace('/',File.separatorChar).replace('\\',File.separatorChar);
                outputFile = new File(tempDir 
                        + File.separatorChar + filename.substring(index+1));
            }else{
                this.filename = filename;
                outputFile = new File(tempDir + File.separatorChar + filename);
            }
            counterMap = new HashMap();
            File dirFile = outputFile.getParentFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            absolutePath = dirFile.getCanonicalPath();
            FileOutputStream fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, "UTF-8");

            
            // start to parse the file and direct to output in the temp
            // directory
            if(index!=-1){
                reader.parse(this.filename);
            }else{
                reader.parse(filename);
            }
            output.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!exclude && needResolveEntity) { 
        	// exclude shows whether it's excluded by filtering
        	// isEntity shows whether it's an entity.
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write("</" + qName + ">");
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            if (level > 0) {
                // If it is the end of a child of an excluded tag, level
                // decrease
                level--;
            } else {
                exclude = false;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        String pi;
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                if (data != null) {
                    pi = target + " " + data;
                } else {
                    pi = target;
                }
                output.write("<?" + pi + "?>");
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(name);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            output.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            output.write(System.getProperty("line.separator"));
            if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
            {
                output.write("<?workdir " + absolutePath + "?>");
            }else{
                output.write("<?workdir " + '/' + absolutePath + "?>");
            }
            output.write(System.getProperty("line.separator"));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        String value;
        String nextValue;
        
        if (counterMap.containsKey(qName)) {
            value = (String) counterMap.get(qName);
            nextValue = Integer.toString(Integer.parseInt(value) + 1);
            counterMap.put(qName, nextValue);
        } else {
            counterMap.put(qName, "1");
            nextValue = "1";
        }

        if (!exclude) { // exclude shows whether it's excluded by filtering
            if (!check("audience", atts) && !check("platform", atts)
                    && !check("product", atts) && !check("otherprops", atts)) {
                try {
                    if (qName.equals("tgroup") || qName.equals("row")) {
                        columnNumber = 0; // initialize the column number
                    }
                    // copy the element name and all of its attribute
                    output.write("<" + qName);
                    for (int i = 0; i < atts.getLength(); i++) {
                        String attQName = atts.getQName(i);
                        String attValue;
                        if (attQName.equals("colname")) {
                            columnNumber++;
                            attValue = "col" + Integer.toString(columnNumber);
                        } else {
                            attValue = atts.getValue(i);
                        }
                        output.write(" " + attQName + "=\"" + attValue + "\"");
                    }
                    // write the xtrf and xtrc attributes which contain debug
                    // information
                    output.write(" xtrf=\"" + filename + "\"");
                    output.write(" xtrc=\"" + qName + ":" + nextValue + "\"");
                    output.write(">");
                    
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }// try
            } else {
                exclude = true;
                level = 0;
            }
        } else {// if(!exclude)
            // If it is the start of a child of an excluded tag, level increase
            level++;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {

    }

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	public void comment(char[] ch, int start, int length) throws SAXException {

	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	public void endCDATA() throws SAXException {
	    try{
	        output.write("]]>");
	    }catch(Exception e){
	        e.printStackTrace(System.out);
	    }
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	public void endDTD() throws SAXException {
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	public void startCDATA() throws SAXException {
	    try{
	        output.write("<![CDATA[");
	    }catch(Exception e){
	        e.printStackTrace(System.out);
	    }
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String name) throws SAXException {
		if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
            	needResolveEntity = StringUtils.checkEntity(name);
            	if(!needResolveEntity){
            		output.write(StringUtils.getEntity(name));
            	}
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

	}
}
