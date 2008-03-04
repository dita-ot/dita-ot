/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;

/**
 * This class contain all the constants used in this toolkit.
 * 
 * @version 1.0 2005-06-22
 * 
 * @author Wu, Zhi Qiang
 */
public abstract class Constants {
    /**
     * Useful int constant.  
     */
    public static final int INT_0 = 0;    
    public static final int INT_1 = 1;        
    public static final int INT_2 = 2;
    public static final int INT_4 = 4;
    public static final int INT_8 = 8;
    public static final int INT_16 = 16;
    public static final int INT_32 = 32;
    public static final int INT_64 = 64;
    public static final int INT_128 = 128;
    public static final int INT_256 = 256;
    public static final int INT_512 = 512;
    public static final int INT_1024 = 1024;
	public static final int INT_2048 = 2048;
	public static final int INT_4096 = 4096;
    public static final int INT_17 = 17;
    public static final int INT_37 = 37;    
    
    /**
     * Constants for file extensions used in this toolkit.
     */
    public static final String FILE_EXTENSION_GIF = ".gif";
    public static final String FILE_EXTENSION_DITA = ".dita";
    public static final String FILE_EXTENSION_XML = ".xml";
    public static final String FILE_EXTENSION_HTML = ".html";
    public static final String FILE_EXTENSION_HTM = ".htm";
    public static final String FILE_EXTENSION_JPG = ".jpg";
    public static final String FILE_EXTENSION_EPS = ".eps";
    public static final String FILE_EXTENSION_DITAMAP = ".ditamap";
    public static final String FILE_EXTENSION_TEMP = ".temp";    
    public static final String FILE_EXTENSION_JPEG = ".jpeg";
    public static final String FILE_EXTENSION_PNG = ".png";
    public static final String FILE_EXTENSION_SVG = ".svg";
    public static final String FILE_EXTENSION_TIFF = ".tiff";
	public static final String FILE_EXTENSION_TIF = ".tif";
    
    /**
     * Constants for element names in dita files.
     */
    public static final String ELEMENT_NAME_MAP = "map";
    public static final String ELEMENT_NAME_INDEXTERM = "indexterm";
    public static final String ELEMENT_NAME_INDEXSEE = "index-see";
    public static final String ELEMENT_NAME_INDEXSEEALSO = "index-see-also";
    public static final String ELEMENT_NAME_INDEXSORTAS = "index-sort-as";
    public static final String ELEMENT_NAME_TOPICREF = "topicref";
    public static final String ELEMENT_NAME_TOPICMETA = "topicmeta";
    public static final String ELEMENT_NAME_KEYWORDS = "keywords";
    public static final String ELEMENT_NAME_MAPLINKS = "maplinks";
    public static final String ELEMENT_NAME_LINKPOOL = "linkpool";
    public static final String ELEMENT_NAME_PROP = "prop";
    public static final String ELEMENT_NAME_PROLOG = "prolog";
    public static final String ELEMENT_NAME_ACTION = "action";
    public static final String ELEMENT_NAME_DITA = "dita";
    public static final String ELEMENT_NAME_RESOURCEID = "resourceid";
    public static final String ELEMENT_NAME_AUDIENCE = "audience";
    public static final String ELEMENT_NAME_PLATFORM = "platform";
    public static final String ELEMENT_NAME_PRODUCT = "product";
    public static final String ELEMENT_NAME_OTHERPROPS = "otherprops";
    public static final String ELEMENT_NAME_TGROUP = "tgroup";
    public static final String ELEMENT_NAME_ROW = "row";
    public static final String ELEMENT_NAME_TITLE = "title";
    public static final String ELEMENT_NAME_ENTRY = "entry";
    public static final String ELEMENT_NAME_COLSPEC = "colspec";
    public static final String ELEMENT_NAME_TOPIC = "topic";
    
    /**
     * Constants for attribute names in dita files.
     */
    public static final String ATTRIBUTE_NAME_CONREF = "conref";
    public static final String ATTRIBUTE_NAME_HREF = "href";    
    public static final String ATTRIBUTE_NAME_NAVTITLE = "navtitle";
    public static final String ATTRIBUTE_NAME_FORMAT = "format";
    public static final String ATTRIBUTE_NAME_ATT = "att";
    public static final String ATTRIBUTE_NAME_VAL = "val";
    public static final String ATTRIBUTE_NAME_ID = "id";
    public static final String ATTRIBUTE_NAME_CLASS = "class";
    public static final String ATTRIBUTE_NAME_COLNAME = "colname";
    public static final String ATTRIBUTE_NAME_NAMEST = "namest";
    public static final String ATTRIBUTE_NAME_NAMEEND = "nameend";
    public static final String ATTRIBUTE_NAME_XML_LANG = "xml:lang";    
    public static final String ATTRIBUTE_NAME_DOMAINS = "domains";
    public static final String ATTRIBUTE_NAME_PROPS = "props";
    public static final String ATTRIBUTE_NAME_SCOPE = "scope";
	public static final String ATTRIBUTE_NAME_TYPE = "type";
    public static final String ATTRIBUTE_NAME_IMG = "img";
    public static final String ATTRIBUTE_NAME_COPY_TO = "copy-to";
	public static final String ATTRIBUTE_NAME_DATA = "data";
	public static final String ATTRIBUTE_NAME_IMAGEREF = "imageref";
    
    /**
     * Constant for value of attribute format in dita files.
     */
    public static final String ATTRIBUTE_FORMAT_VALUE_DITA = "dita";    
    
    /**
     * Constants for index type. 
     */
    public static final String INDEX_TYPE_JAVAHELP = "javahelp";
    public static final String INDEX_TYPE_HTMLHELP = "htmlhelp"; 
    public static final String INDEX_TYPE_ECLIPSEHELP = "eclipsehelp";

    /** 
     * Constant for generated property file name. 
     */
    public static final String FILE_NAME_DITA_LIST = "dita.list";
    public static final String FILE_NAME_DITA_LIST_XML="dita.xml.properties";
    public static final String FILE_NAME_CATALOG = "catalog-dita.xml";
    
    /**
     * Constants for all the properties's name.
     */
    public static final String HREF_TARGET_LIST = "hreftargetslist";
    public static final String HTML_LIST = "htmllist";
    public static final String IMAGE_LIST = "imagelist";
    public static final String FLAG_IMAGE_LIST = "flagimagelist";
    public static final String CONREF_LIST = "conreflist";
    public static final String HREF_DITA_TOPIC_LIST = "hrefditatopiclist";
    public static final String FULL_DITA_TOPIC_LIST = "fullditatopiclist";
    public static final String FULL_DITAMAP_LIST = "fullditamaplist";    
    public static final String FULL_DITAMAP_TOPIC_LIST = "fullditamapandtopiclist";
    public static final String CONREF_TARGET_LIST = "conreftargetslist";
    public static final String COPYTO_SOURCE_LIST = "copytosourcelist";
    public static final String COPYTO_TARGET_TO_SOURCE_MAP_LIST = "copytotarget2sourcemaplist";
    public static final String SUBSIDIARY_TARGET_LIST = "subtargetslist";
    public static final String CHUNKED_TOPIC_LIST="chunkedtopiclist";
    public static final String CHUNKED_DITAMAP_LIST="chunkedditamaplist";
    public static final String OUT_DITA_FILES_LIST="outditafileslist";
    public static final String REL_FLAGIMAGE_LIST="relflagimagelist";

    /**
     * Constants for common params used in ant invoker.
     */
    public static final String ANT_INVOKER_PARAM_TEMPDIR = "tempDir";
    public static final String ANT_INVOKER_PARAM_DITAEXT = "ditaext";
    public static final String ANT_INVOKER_PARAM_BASEDIR = "basedir";
    public static final String ANT_INVOKER_PARAM_INPUTMAP = "inputmap";    
    public static final String ANT_INVOKER_PARAM_DITAVAL = "ditaval";    
    public static final String ANT_INVOKER_PARAM_MAPLINKS = "maplinks";

    /**
     * Constants for extensive params used in ant invoker.
     */
    public static final String ANT_INVOKER_EXT_PARAM_TARGETEXT = "targetext";
    public static final String ANT_INVOKER_EXT_PARAM_INDEXTYPE = "indextype";
    public static final String ANT_INVOKER_EXT_PARAM_ENCODING = "encoding";
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUT = "output";
    public static final String ANT_INVOKER_EXT_PARAM_INPUT = "input";
    public static final String ANT_INVOKER_EXT_PARAM_DITADIR = "ditadir";
    public static final String ANT_INVOKER_EXT_PARAM_INPUTDIR = "inputdir";
    public static final String ANT_INVOKER_EXT_PARAM_STYLE = "style";
    public static final String ANT_INVOKER_EXT_PARAM_TRANSTYPE = "transtype";
    public static final String ANT_INVOKER_EXT_PARAM_OUTTERCONTROL="outercontrol";
    public static final String ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER="generatecopyouter";
    public static final String ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP="onlytopicinmap";
    public static final String ANT_INVOKER_EXT_PARAM_VALIDATE="validate";
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUTDIR="outputdir";
    /**
     * Constants for file and line separator.
     */
    public static final String FILE_SEPARATOR = File.separator;    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**
     * OS relevant constants
     */
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_NAME_WINDOWS = "windows";
    
    /**
     * Misc string constants used in this toolkit.
     */
    public static final String STRING_EMPTY = "";
    public static final String SLASH = "/";    
    public static final String BACK_SLASH = "\\";
    public static final String SHARP = "#";    
    public static final String STICK = "|";    
    public static final String EQUAL = "=";    
    public static final String COMMA = ",";    
    public static final String LESS_THAN = "<";    
    public static final String GREATER_THAN = ">";    
    public static final String QUESTION = "?";    
    public static final String QUOTATION = "\"";    
    public static final String COLON = ":";
    public static final String DOT= ".";
    public static final String DOUBLE_BACK_SLASH = "\\\\";    
    public static final String COLON_DOUBLE_SLASH = "://";    
    public static final String CDATA_HEAD = "<![CDATA[";    
    public static final String CDATA_END = "]]>";    
    public static final String META_HEAD = "<metadata class=\"- topic/metadata \">";    
    public static final String META_END = "</metadata>";    
    public static final String PROLOG_HEAD = "<prolog class=\"- topic/prolog \">";
    public static final String PROLOG_END = "</prolog>";    
    public static final String RELATED_LINKS_HEAD = "<related-links class=\"- topic/related-links \">";    
    public static final String RELATED_LINKS_END = "</related-links>";    
    public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    public static final String STRING_BLANK = " ";
    public static final String COUNTRY_US = "us";
    public static final String LANGUAGE_EN = "en";    
    public static final String UTF8 = "UTF-8";
    public static final String SAX_DRIVER_PROPERTY = "org.xml.sax.driver";    
    public static final String SAX_DRIVER_DEFAULT_CLASS = "org.apache.xerces.parsers.SAXParser"; 
    public static final String SAX_DRIVER_SUN_HACK_CLASS = "com.sun.org.apache.xerces.internal.parsers.SAXParser";
    public static final String SAX_DRIVER_CRIMSON_CLASS = "org.apache.crimson.parser.XMLReaderImpl";
    public static final String RESOLVER_CLASS = "org.apache.xml.resolver.tools.CatalogResolver";
    public static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";    
    public static final String FEATURE_NAMESPACE_PREFIX = "http://xml.org/sax/features/namespace-prefixes";
    public static final String FEATURE_NAMESPACE = "http://xml.org/sax/features/namespaces";
    public static final String FEATURE_VALIDATION = "http://xml.org/sax/features/validation";
    public static final String FEATURE_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";    
    public static final String TEMP_DIR_DEFAULT = "temp";
	public static final String FILTER_ACTION_EXCLUDE = "exclude";
	public static final String ATTR_CLASS_VALUE_TITLE = " topic/title ";
	public static final String ATTR_CLASS_VALUE_MAP = " map/map ";
	public static final String ATTR_CLASS_VALUE_INDEXTERM = " topic/indexterm ";
	public static final String ATTR_CLASS_VALUE_TOPIC = " topic/topic ";
	public static final String ATTR_CLASS_VALUE_XREF = " topic/xref ";
	public static final String ATTR_CLASS_VALUE_LINK = " topic/link ";
	public static final String ATTR_CLASS_VALUE_TOPICREF = " map/topicref ";
	public static final String ATTR_SCOPE_VALUE_LOCAL = "local";
	public static final String ATTR_SCOPE_VALUE_EXTERNAL = "external";
	public static final String ATTR_FORMAT_VALUE_DITA = "dita";
	public static final String ATTRIBUTE_NAME_DITAARCHVERSION = "DITAArchVersion";
	public static final String ATTRIBUTE_PREFIX_DITAARCHVERSION = "ditaarch";
	public static final String ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION = "xmlns:ditaarch";
	public static final String ATTR_CLASS_VALUE_OBJECT = " topic/object ";
	public static final String ATTR_CLASS_VALUE_TOPICMETA = " map/topicmeta ";
	public static final String ATTR_CLASS_VALUE_AUTHOR = " topic/author ";
	public static final String ATTR_CLASS_VALUE_SOURCE = " topic/source ";
	public static final String ATTR_CLASS_VALUE_PUBLISHER = " topic/publisher ";
	public static final String ATTR_CLASS_VALUE_COPYRIGHT = " topic/copyright ";
	public static final String ATTR_CLASS_VALUE_CRITDATES = " topic/critdates ";
	public static final String ATTR_CLASS_VALUE_PERMISSIONS = " topic/permissions ";
	public static final String ATTR_CLASS_VALUE_CATEGORY = " topic/category ";
	public static final String ATTR_CLASS_VALUE_AUDIENCE = " topic/audience ";
	public static final String ATTR_CLASS_VALUE_KEYWORDS = " topic/keywords ";
	public static final String ATTR_CLASS_VALUE_PRODINFO = " topic/prodinfo ";
	public static final String ATTR_CLASS_VALUE_OTHERMETA = " topic/othermeta ";
	public static final String ATTR_CLASS_VALUE_RESOURCEID = " topic/resourceid ";
	public static final String ATTR_CLASS_VALUE_DATA = " topic/data ";
	public static final String ATTR_CLASS_VALUE_DATAABOUT = " topic/data-about ";
	public static final String ATTR_CLASS_VALUE_FOREIGN = " topic/foreign ";
	public static final String ATTR_CLASS_VALUE_UNKNOWN = " topic/unknown ";
	public static final String ATTR_CLASS_VALUE_MAP_SEARCHTITLE = " map/searchtitle ";
	public static final String ATTR_CLASS_VALUE_TOPIC_SEARCHTITLE = " topic/searchtitle ";
	public static final String ATTR_CLASS_VALUE_PROLOG = " topic/prolog ";
	public static final String ATTR_CLASS_VALUE_ABSTRACT = " topic/abstract ";
	public static final String ATTR_CLASS_VALUE_SHORTDESC = " topic/shortdesc ";
	public static final String ATTR_CLASS_VALUE_TITLEALTS = " topic/titlealts ";
	public static final String ATTR_CLASS_VALUE_RELATED_LINKS = " topic/related-links ";
	public static final String ATTR_CLASS_VALUE_BODY = " topic/body ";	
	public static final String ATTR_CLASS_VALUE_RELTABLE = " map/reltable ";
	
    /**
     * constants for filtering or flagging.
     */	
	public static final String DEFAULT_ACTION="default";
	public static final String ATTRIBUTE_NAME_CHUNK = "chunk";
		
	
    /**
     * Private constructor used to forbid instantiation.
     */
    private Constants() {
    }
}
