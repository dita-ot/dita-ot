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
 * This class contains all the constants used in DITA-OT.
 * 
 * @version 1.0 2005-06-22
 * 
 * @author Wu, Zhi Qiang
 */
public abstract class Constants {
    /**INT 0.*/
    public static final int INT_0 = 0;
    /**INT 1.*/
    public static final int INT_1 = 1;
    /**INT 2.*/
    public static final int INT_2 = 2;
    /**INT 4.*/
    public static final int INT_4 = 4;
    /**INT 8.*/
    public static final int INT_8 = 8;
    /**INT 16.*/
    public static final int INT_16 = 16;
    /**INT 32.*/
    public static final int INT_32 = 32;
    /**INT 64.*/
    public static final int INT_64 = 64;
    /**INT 128.*/
    public static final int INT_128 = 128;
    /**INT 256.*/
    public static final int INT_256 = 256;
    /**INT 512.*/
    public static final int INT_512 = 512;
    /**INT 1024.*/
    public static final int INT_1024 = 1024;
    /**INT 2048.*/
	public static final int INT_2048 = 2048;
	/**INT 4096.*/
	public static final int INT_4096 = 4096;
	/**INT 17.*/
    public static final int INT_17 = 17;
    /**INT 37.*/
    public static final int INT_37 = 37;    
    
    /**.gif extension.*/
    public static final String FILE_EXTENSION_GIF = ".gif";
    /**.dita extension.*/
    public static final String FILE_EXTENSION_DITA = ".dita";
    /**.xml extension.*/
    public static final String FILE_EXTENSION_XML = ".xml";
    /**.html extension.*/
    public static final String FILE_EXTENSION_HTML = ".html";
    /**.htm extension.*/
    public static final String FILE_EXTENSION_HTM = ".htm";
    /**.hhp extension.*/
    public static final String FILE_EXTENSION_HHP = ".hhp";
    /**.hhc extension.*/
    public static final String FILE_EXTENSION_HHC = ".hhc";
    /**.hhk extension.*/
    public static final String FILE_EXTENSION_HHK = ".hhk";
    /**.jpg extension.*/
    public static final String FILE_EXTENSION_JPG = ".jpg";
    //Added by William on 2009-10-10 for resources bug:2873560 start
    /**.swf extension.*/
    public static final String FILE_EXTENSION_SWF = ".swf";
    //Added by William on 2009-10-10 for resources bug:2873560 end
    /**.eps extension.*/
    public static final String FILE_EXTENSION_EPS = ".eps";
    /**.ditamap extension.*/
    public static final String FILE_EXTENSION_DITAMAP = ".ditamap";
    /**.temp extension.*/
    public static final String FILE_EXTENSION_TEMP = ".temp";    
    /**.jpeg extension.*/
    public static final String FILE_EXTENSION_JPEG = ".jpeg";
    /**.png extension.*/
    public static final String FILE_EXTENSION_PNG = ".png";
    /**.svg extension.*/
    public static final String FILE_EXTENSION_SVG = ".svg";
    /**.tiff extension.*/
    public static final String FILE_EXTENSION_TIFF = ".tiff";
    /**.tif extension.*/
    public static final String FILE_EXTENSION_TIF = ".tif";
    /**.pdf extension.*/
    public static final String FILE_EXTENSION_PDF = ".pdf";
    
    /**map element.*/
    public static final String ELEMENT_NAME_MAP = "map";
    /**indexterm element.*/
    public static final String ELEMENT_NAME_INDEXTERM = "indexterm";
    /**index-see element.*/
    public static final String ELEMENT_NAME_INDEXSEE = "index-see";
    /**index-see-also element.*/
    public static final String ELEMENT_NAME_INDEXSEEALSO = "index-see-also";
    /**index-sort-as element.*/
    public static final String ELEMENT_NAME_INDEXSORTAS = "index-sort-as";
    /**topicref element.*/
    public static final String ELEMENT_NAME_TOPICREF = "topicref";
    /**topicmeta element.*/
    public static final String ELEMENT_NAME_TOPICMETA = "topicmeta";
    /**linktext element.*/
    public static final String ELEMENT_NAME_LINKTEXT = "linktext";
    /**navtitle element.*/
    public static final String ELEMENT_NAME_NAVTITLE = "navtitle";
    /**shortdesc element.*/
    public static final String ELEMENT_NAME_SHORTDESC = "shortdesc";
    /**keywords element.*/
    public static final String ELEMENT_NAME_KEYWORDS = "keywords";
    /**maplinks element.*/
    public static final String ELEMENT_NAME_MAPLINKS = "maplinks";
    /**linkpool element.*/
    public static final String ELEMENT_NAME_LINKPOOL = "linkpool";
    /**linklist element.*/
    public static final String ELEMENT_NAME_LINKLIST = "linklist";
    /**prop element.*/
    public static final String ELEMENT_NAME_PROP = "prop";
    /**prolog element.*/
    public static final String ELEMENT_NAME_PROLOG = "prolog";
    /**map element.*/
    public static final String ELEMENT_NAME_ACTION = "action";
    /**action element.*/
    public static final String ELEMENT_NAME_DITA = "dita";
    /**resourceid element.*/
    public static final String ELEMENT_NAME_RESOURCEID = "resourceid";
    /**audience element.*/
    public static final String ELEMENT_NAME_AUDIENCE = "audience";
    /**platform element.*/
    public static final String ELEMENT_NAME_PLATFORM = "platform";
    /**product element.*/
    public static final String ELEMENT_NAME_PRODUCT = "product";
    /**otherprops element.*/
    public static final String ELEMENT_NAME_OTHERPROPS = "otherprops";
    //Added by William on 2010-07-16 for bug:3030317 start	
    /**props element.*/
    public static final String ELEMENT_NAME_PROPS = "props";
    /**rev element.*/
    public static final String ELEMENT_NAME_REV = "rev";
    //Added by William on 2010-07-16 for bug:3030317 start	
    /**tgroup element.*/
    public static final String ELEMENT_NAME_TGROUP = "tgroup";
    /**row element.*/
    public static final String ELEMENT_NAME_ROW = "row";
    /**title element.*/
    public static final String ELEMENT_NAME_TITLE = "title";
    /**entry element.*/
    public static final String ELEMENT_NAME_ENTRY = "entry";
    /**colspec element.*/
    public static final String ELEMENT_NAME_COLSPEC = "colspec";
    /**topic element.*/
    public static final String ELEMENT_NAME_TOPIC = "topic";
    /**glossentry element.*/
    public static final String ELEMENT_NAME_GLOSSENTRY = "glossentry";
    /**glossterm element.*/
    public static final String ELEMENT_NAME_GLOSSTERM = "glossterm";
    /**glossSurfaceForm element.*/
    public static final String ELEMENT_NAME_GLOSSSURFACEFORM = "glossSurfaceForm";
    /**glossAcronym element.*/
    public static final String ELEMENT_NAME_GLOSSACRONYM = "glossAcronym";
    /**glossStatus element.*/
    public static final String ELEMENT_NAME_GLOSSSTATUS = "glossStatus";
    /**glossSynonym element.*/
    public static final String ELEMENT_NAME_GLOSSSYNONYM = "glossSynonym";
    /**glossAbbreviation element.*/
    public static final String ELEMENT_NAME_GLOSSABBREVIATION = "glossAbbreviation";
    /**glossShortForm element.*/
    public static final String ELEMENT_NAME_GLOSSSHORTFORM = "glossShortForm";
    /**subjectScheme element.*/
    public static final String ELEMENT_NAME_SUBJECT_SCHEME = "subjectScheme";
    /**subjectdef element.*/
    public static final String ELEMENT_NAME_SUBJECT_DEF = "subjectdef";
    /**attributedef element.*/
    public static final String ELEMENT_NAME_ATTRIBUTE_DEF = "attributedef";
    /**elementdef element.*/
    public static final String ELEMENT_NAME_ELEMENT_DEF = "elementdef";
    /**defaultSubject element.*/
    public static final String ELEMENT_NAME_DEFAULT_SUBJECT = "defaultSubject";
    
    /**conref attribute.*/
    public static final String ATTRIBUTE_NAME_CONREF = "conref";
    /**href attribute.*/
    public static final String ATTRIBUTE_NAME_HREF = "href";    
    /**navtitle attribute.*/
    public static final String ATTRIBUTE_NAME_NAVTITLE = "navtitle";
    /**format attribute.*/
    public static final String ATTRIBUTE_NAME_FORMAT = "format";
    /**charset attribute.*/
    public static final String ATTRIBUTE_NAME_CHARSET = "charset";
    /**charset attribute.*/
    public static final String ATTRIBUTE_NAME_LANG = "lang";
    /**att attribute.*/
    public static final String ATTRIBUTE_NAME_ATT = "att";
    /**val attribute.*/
    public static final String ATTRIBUTE_NAME_VAL = "val";
    /**id attribute.*/
    public static final String ATTRIBUTE_NAME_ID = "id";
    /**class attribute.*/
    public static final String ATTRIBUTE_NAME_CLASS = "class";
    /**colname attribute.*/
    public static final String ATTRIBUTE_NAME_COLNAME = "colname";
    //Added by William on 2009-06-30 for colname bug:2811358 start
    /**morerows attribute.*/
    public static final String ATTRIBUTE_NAME_MOREROWS = "morerows";
    //Added by William on 2009-06-30 for colname bug:2811358 start
    /**namest attribute.*/
    public static final String ATTRIBUTE_NAME_NAMEST = "namest";
    /**nameend attribute.*/
    public static final String ATTRIBUTE_NAME_NAMEEND = "nameend";
    /**xml:lang attribute.*/
    public static final String ATTRIBUTE_NAME_XML_LANG = "xml:lang";    
    /**domains attribute.*/
    public static final String ATTRIBUTE_NAME_DOMAINS = "domains";
    /**props attribute.*/
    public static final String ATTRIBUTE_NAME_PROPS = "props";
    /**scope attribute.*/
    public static final String ATTRIBUTE_NAME_SCOPE = "scope";
    /**type attribute.*/
    public static final String ATTRIBUTE_NAME_TYPE = "type";
    /**img attribute.*/
    public static final String ATTRIBUTE_NAME_IMG = "img";
    /**copy-to attribute.*/
    public static final String ATTRIBUTE_NAME_COPY_TO = "copy-to";
    /**data attribute.*/
    public static final String ATTRIBUTE_NAME_DATA = "data";
    /**codebase attribute.*/
    public static final String ATTRIBUTE_NAME_CODEBASE = "codebase";
    /**imageref attribute.*/
    public static final String ATTRIBUTE_NAME_IMAGEREF = "imageref";
    /**start attribute.*/
    public static final String ATTRIBUTE_NAME_START="start";
    /**conref attribute.*/
    public static final String ATTRIBUTE_NAME_END="end";
    /**conaction attribute.*/
    public static final String ATTRIBUTE_NAME_CONACTION="conaction";
    /**keyref attribute.*/
    public static final String ATTRIBUTE_NAME_KEYREF = "keyref";
    /**conkeyref attribute.*/
    public static final String ATTRIBUTE_NAME_CONKEYREF	="conkeyref";
    /**keys attribute.*/
    public static final String ATTRIBUTE_NAME_KEYS = "keys";
    /**xtrf attribute.*/
    public static final String ATTRIBUTE_NAME_XTRF = "xtrf";
    /**processing-role attribute.*/
    public static final String ATTRIBUTE_NAME_PROCESSING_ROLE = "processing-role";
    /**toc attribute.*/
    public static final String ATTRIBUTE_NAME_TOC = "toc";
    /**print attribute.*/
    public static final String ATTRIBUTE_NAME_PRINT = "print";
        
    /**
     * Constant for value of attribute format in dita files.
     */
    /** Constants for format attribute value dita*/
    public static final String ATTRIBUTE_FORMAT_VALUE_DITA = "dita";
    /** Constants for format attribute value html*/
    public static final String ATTRIBUTE_FORMAT_VALUE_HTML = "html";
    /** Constants for format attribute value windows*/
    public static final String ATTRIBUTE_FORMAT_VALUE_WINDOWS = "windows";
    
    /** Constants for index type(javahelp).*/
    public static final String INDEX_TYPE_JAVAHELP = "javahelp";
    /** Constants for index type(htmlhelp).*/
    public static final String INDEX_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for index type(eclipsehelp).*/
    public static final String INDEX_TYPE_ECLIPSEHELP = "eclipsehelp";
    
    /** Constants for transform type(xhtml).*/
    public static final String TRANS_TYPE_XHTML = "xhtml";
    /** Constants for transform type(eclipsehelp).*/
    public static final String TRANS_TYPE_ECLIPSEHELP = "eclipsehelp";
    /** Constants for transform type(javahelp).*/
    public static final String TRANS_TYPE_JAVAHELP = "javahelp";
    /** Constants for transform type(htmlhelp).*/
    public static final String TRANS_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for transform type(eclipsecontent).*/
    public static final String TRANS_TYPE_ECLIPSECONTENT = "eclipsecontent";
    
    /** Constant for generated property file name(dita.list).*/
    public static final String FILE_NAME_DITA_LIST = "dita.list";
    /** Constant for generated property file name(dita.xml.properties).*/
    public static final String FILE_NAME_DITA_LIST_XML="dita.xml.properties";
    /** Constant for generated property file name(catalog-dita.xml).*/
    public static final String FILE_NAME_CATALOG = "catalog-dita.xml";
    //store the scheme files refered by a scheme file in the form of Map<String Set<String>>
    /** Constant for generated property file name(subrelation.xml).*/
    public static final String FILE_NAME_SUBJECT_RELATION = "subrelation.xml";
    
    /**Constants for the properties's name(hreftargetslist).*/
    public static final String HREF_TARGET_LIST = "hreftargetslist";
    /**Constants for the properties's name(canditopicslist).*/
    public static final String HREF_TOPIC_LIST = "canditopicslist";
    /**Constants for the properties's name(skipchunklist).*/
    public static final String CHUNK_TOPIC_LIST = "skipchunklist";
    /**Constants for the properties's name(htmllist).*/
    public static final String HTML_LIST = "htmllist";
    /**Constants for the properties's name(imagelist).*/
    public static final String IMAGE_LIST = "imagelist";
    /**Constants for the properties's name(flagimagelist).*/
    public static final String FLAG_IMAGE_LIST = "flagimagelist";
    /**Constants for the properties's name(conreflist).*/
    public static final String CONREF_LIST = "conreflist";
    /**Constants for the properties's name(hrefditatopiclist).*/
    public static final String HREF_DITA_TOPIC_LIST = "hrefditatopiclist";
    /**Constants for the properties's name(fullditatopiclist).*/
    public static final String FULL_DITA_TOPIC_LIST = "fullditatopiclist";
    /**Constants for the properties's name(fullditamaplist).*/
    public static final String FULL_DITAMAP_LIST = "fullditamaplist";
    /**Constants for the properties's name(user.input.file).*/
    public static final String INPUT_DITAMAP = "user.input.file";
    /**Constants for the properties's name(fullditamapandtopiclist).*/
    public static final String FULL_DITAMAP_TOPIC_LIST = "fullditamapandtopiclist";
    /**Constants for the properties's name(conreftargetslist).*/
    public static final String CONREF_TARGET_LIST = "conreftargetslist";
    /**Constants for the properties's name(copytosourcelist).*/
    public static final String COPYTO_SOURCE_LIST = "copytosourcelist";
    /**Constants for the properties's name(copytotarget2sourcemaplist).*/
    public static final String COPYTO_TARGET_TO_SOURCE_MAP_LIST = "copytotarget2sourcemaplist";
    /**Constants for the properties's name(subtargetslist).*/
    public static final String SUBSIDIARY_TARGET_LIST = "subtargetslist";
    /**Constants for the properties's name(chunkedtopiclist).*/
    public static final String CHUNKED_TOPIC_LIST="chunkedtopiclist";
    /**Constants for the properties's name(chunkedditamaplist).*/
    public static final String CHUNKED_DITAMAP_LIST="chunkedditamaplist";
    /**Constants for the properties's name(outditafileslist).*/
    public static final String OUT_DITA_FILES_LIST="outditafileslist";
    /**Constants for the properties's name(relflagimagelist).*/
    public static final String REL_FLAGIMAGE_LIST="relflagimagelist";
    /**Constants for the properties's name(conrefpushlist).*/
    public static final String CONREF_PUSH_LIST = "conrefpushlist";
    /**Constants for the properties's name(keylist).*/
    public static final String KEY_LIST = "keylist";
    /**Constants for the properties's name(keyreflist).*/
    public static final String KEYREF_LIST = "keyreflist";
    /**Constants for the properties's name(codereflist).*/
    public static final String CODEREF_LIST = "codereflist";
    /**Constants for the properties's name(resourceonlylist).*/
    public static final String RESOURCE_ONLY_LIST = "resourceonlylist";
    //list all of the scheme files
    /**Constants for the properties's name(subjectschemelist).*/
    public static final String SUBJEC_SCHEME_LIST = "subjectschemelist";

    /**Constants for common params used in ant invoker(tempDir).*/
    public static final String ANT_INVOKER_PARAM_TEMPDIR = "tempDir";
    /**Constants for common params used in ant invoker(ditaext).*/
    public static final String ANT_INVOKER_PARAM_DITAEXT = "ditaext";
    /**Constants for common params used in ant invoker(basedir).*/
    public static final String ANT_INVOKER_PARAM_BASEDIR = "basedir";
    /**Constants for common params used in ant invoker(inputmap).*/
    public static final String ANT_INVOKER_PARAM_INPUTMAP = "inputmap";
    /**Constants for common params used in ant invoker(ditaval).*/
    public static final String ANT_INVOKER_PARAM_DITAVAL = "ditaval";
    /**Constants for common params used in ant invoker(maplinks).*/
    public static final String ANT_INVOKER_PARAM_MAPLINKS = "maplinks";

    /**Constants for extensive params used in ant invoker(targetext).*/
    public static final String ANT_INVOKER_EXT_PARAM_TARGETEXT = "targetext";
    /**Constants for extensive params used in ant invoker(indextype).*/
    public static final String ANT_INVOKER_EXT_PARAM_INDEXTYPE = "indextype";
    /**Constants for extensive params used in ant invoker(indexclass).*/
    public static final String ANT_INVOKER_EXT_PARAM_INDEXCLASS = "indexclass";
    /**Constants for extensive params used in ant invoker(encoding).*/
    public static final String ANT_INVOKER_EXT_PARAM_ENCODING = "encoding";
    /**Constants for extensive params used in ant invoker(output).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUT = "output";
    /**Constants for extensive params used in ant invoker(input).*/
    public static final String ANT_INVOKER_EXT_PARAM_INPUT = "input";
    /**Constants for extensive params used in ant invoker(ditadir).*/
    public static final String ANT_INVOKER_EXT_PARAM_DITADIR = "ditadir";
    /**Constants for extensive params used in ant invoker(inputdir).*/
    public static final String ANT_INVOKER_EXT_PARAM_INPUTDIR = "inputdir";
    /**Constants for extensive params used in ant invoker(style).*/
    public static final String ANT_INVOKER_EXT_PARAM_STYLE = "style";
    /**Constants for extensive params used in ant invoker(transtype).*/
    public static final String ANT_INVOKER_EXT_PARAM_TRANSTYPE = "transtype";
    /**Constants for extensive params used in ant invoker(outercontrol).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTTERCONTROL="outercontrol";
    /**Constants for extensive params used in ant invoker(generatecopyouter).*/
    public static final String ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER="generatecopyouter";
    /**Constants for extensive params used in ant invoker(onlytopicinmap).*/
    public static final String ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP="onlytopicinmap";
    /**Constants for extensive params used in ant invoker(validate).*/
    public static final String ANT_INVOKER_EXT_PARAM_VALIDATE="validate";
    /**Constants for extensive params used in ant invoker(outputdir).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUTDIR="outputdir";
    /**Constants for extensive params used in ant invoker(gramcache).*/
    public static final String ANT_INVOKER_EXT_PARAM_GRAMCACHE="gramcache";
    /**Constants for file separator.*/
    public static final String FILE_SEPARATOR = File.separator;
    /**Constants for line separator.*/
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**OS relevant constants(OS NAME).*/
    public static final String OS_NAME = System.getProperty("os.name");
    /**OS relevant constants(windows).*/
    public static final String OS_NAME_WINDOWS = "windows";
    
    /**
     * Misc string constants used in this toolkit.
     */
    /**STRING_EMPTY.*/
    public static final String STRING_EMPTY = "";
    /**LEFT_BRACKET.*/
    public static final String LEFT_BRACKET = "(";
    /**RIGHT_BRACKET.*/
    public static final String RIGHT_BRACKET = ")";
    /**SLASH.*/
    public static final String SLASH = "/";    
    /**BACK_SLASH.*/
    public static final String BACK_SLASH = "\\";
    /**SHARP.*/
    public static final String SHARP = "#";    
    /**STICK.*/
    public static final String STICK = "|";    
    /**EQUAL.*/
    public static final String EQUAL = "=";    
    /**COMMA.*/
    public static final String COMMA = ",";    
    /**LESS_THAN.*/
    public static final String LESS_THAN = "<";    
    /**GREATER_THAN.*/
    public static final String GREATER_THAN = ">";    
    /**QUESTION.*/
    public static final String QUESTION = "?";    
    /**QUOTATION.*/
    public static final String QUOTATION = "\"";    
    /**COLON.*/
    public static final String COLON = ":";
    /**DOT.*/
    public static final String DOT= ".";
    /**DOUBLE_BACK_SLASH.*/
    public static final String DOUBLE_BACK_SLASH = "\\\\";    
    /**COLON_DOUBLE_SLASH.*/
    public static final String COLON_DOUBLE_SLASH = "://";    
    /**CDATA_HEAD.*/
    public static final String CDATA_HEAD = "<![CDATA[";    
    /**CDATA_END.*/
    public static final String CDATA_END = "]]>";
    /**DOCTYPE_HEAD.*/
    public static final String DOCTYPE_HEAD = "<!DOCTYPE ";
    /**META_HEAD.*/
    public static final String META_HEAD = "<metadata class=\"- topic/metadata \">";    
    /**META_END.*/
    public static final String META_END = "</metadata>";    
    /**PROLOG_HEAD.*/
    public static final String PROLOG_HEAD = "<prolog class=\"- topic/prolog \">";
    /**PROLOG_END.*/
    public static final String PROLOG_END = "</prolog>";    
    /**RELATED_LINKS_HEAD.*/
    public static final String RELATED_LINKS_HEAD = "<related-links class=\"- topic/related-links \">";    
    /**RELATED_LINKS_END.*/
    public static final String RELATED_LINKS_END = "</related-links>";    
    /**XML_HEAD.*/
    public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    /**STRING_BLANK.*/
    public static final String STRING_BLANK = " ";
    /**COUNTRY_US.*/
    public static final String COUNTRY_US = "us";
    /**LANGUAGE_EN.*/
    public static final String LANGUAGE_EN = "en";    
    /**UTF8.*/
    public static final String UTF8 = "UTF-8";
    /**SAX_DRIVER_PROPERTY.*/
    public static final String SAX_DRIVER_PROPERTY = "org.xml.sax.driver";    
    /**SAX_DRIVER_DEFAULT_CLASS.*/
    public static final String SAX_DRIVER_DEFAULT_CLASS = "org.apache.xerces.parsers.SAXParser"; 
    /**SAX_DRIVER_SUN_HACK_CLASS.*/
    public static final String SAX_DRIVER_SUN_HACK_CLASS = "com.sun.org.apache.xerces.internal.parsers.SAXParser";
    /**SAX_DRIVER_CRIMSON_CLASS.*/
    public static final String SAX_DRIVER_CRIMSON_CLASS = "org.apache.crimson.parser.XMLReaderImpl";
    /**RESOLVER_CLASS.*/
    public static final String RESOLVER_CLASS = "org.apache.xml.resolver.tools.CatalogResolver";
    /**LEXICAL_HANDLER_PROPERTY.*/
    public static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";    
    /**FEATURE_NAMESPACE_PREFIX.*/
    public static final String FEATURE_NAMESPACE_PREFIX = "http://xml.org/sax/features/namespace-prefixes";
    /**FEATURE_NAMESPACE.*/
    public static final String FEATURE_NAMESPACE = "http://xml.org/sax/features/namespaces";
    /**FEATURE_VALIDATION.*/
    public static final String FEATURE_VALIDATION = "http://xml.org/sax/features/validation";
    /**FEATURE_VALIDATION_SCHEMA.*/
    public static final String FEATURE_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";    
    /**TEMP_DIR_DEFAULT.*/
    public static final String TEMP_DIR_DEFAULT = "temp";
    /**FILTER_ACTION_EXCLUDE.*/
    public static final String FILTER_ACTION_EXCLUDE = "exclude";
    /**ATTR_CLASS_VALUE_TITLE.*/
	public static final String ATTR_CLASS_VALUE_TITLE = " topic/title ";
	/**ATTR_CLASS_VALUE_MAP.*/
	public static final String ATTR_CLASS_VALUE_MAP = " map/map ";
	/**ATTR_CLASS_VALUE_INDEXTERM.*/
	public static final String ATTR_CLASS_VALUE_INDEXTERM = " topic/indexterm ";
	/**ATTR_CLASS_VALUE_INDEXSEE.*/
	public static final String ATTR_CLASS_VALUE_INDEXSEE = " indexing-d/index-see ";
	/**ATTR_CLASS_VALUE_INDEXSEEALSO.*/
	public static final String ATTR_CLASS_VALUE_INDEXSEEALSO = " indexing-d/index-see-also ";
	/**ATTR_CLASS_VALUE_INDEXSORTAS.*/
	public static final String ATTR_CLASS_VALUE_INDEXSORTAS = " indexing-d/index-sort-as ";
	/**ATTR_CLASS_VALUE_TOPIC.*/
	public static final String ATTR_CLASS_VALUE_TOPIC = " topic/topic ";
	/**ATTR_CLASS_VALUE_XREF.*/
	public static final String ATTR_CLASS_VALUE_XREF = " topic/xref ";
	/**ATTR_CLASS_VALUE_LINK.*/
	public static final String ATTR_CLASS_VALUE_LINK = " topic/link ";
	/**ATTR_CLASS_VALUE_TOPICREF.*/
	public static final String ATTR_CLASS_VALUE_TOPICREF = " map/topicref ";
	/**ATTR_SCOPE_VALUE_LOCAL.*/
	public static final String ATTR_SCOPE_VALUE_LOCAL = "local";
	/**ATTR_SCOPE_VALUE_EXTERNAL.*/
	public static final String ATTR_SCOPE_VALUE_EXTERNAL = "external";
	/**ATTR_SCOPE_VALUE_PEER.*/
	public static final String ATTR_SCOPE_VALUE_PEER = "peer";
	/**ATTR_FORMAT_VALUE_DITA.*/
	public static final String ATTR_FORMAT_VALUE_DITA = "dita";
	//added by william on 2009-08-06 for bug:2832696 start
	/**ATTR_FORMAT_VALUE_DITAMAP.*/
	public static final String ATTR_FORMAT_VALUE_DITAMAP = "ditamap";
	//added by william on 2009-08-06 for bug:2832696 end
	/**ATTRIBUTE_NAME_DITAARCHVERSION.*/
	public static final String ATTRIBUTE_NAME_DITAARCHVERSION = "DITAArchVersion";
	/**ATTRIBUTE_PREFIX_DITAARCHVERSION.*/
	public static final String ATTRIBUTE_PREFIX_DITAARCHVERSION = "ditaarch";
	/**ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION.*/
	public static final String ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION = "xmlns:ditaarch";
	/**ATTR_CLASS_VALUE_OBJECT.*/
	public static final String ATTR_CLASS_VALUE_OBJECT = " topic/object ";
	/**ATTR_CLASS_VALUE_TOPICMETA.*/
	public static final String ATTR_CLASS_VALUE_TOPICMETA = " map/topicmeta ";
	//Added by William on 2009-06-24 for req #12014 start
	/**ATTR_CLASS_VALUE_EXPORTANCHORS.*/
	public static final String ATTR_CLASS_VALUE_EXPORTANCHORS = " delay-d/exportanchors ";
	/**ATTR_CLASS_VALUE_ANCHORKEY.*/
	public static final String ATTR_CLASS_VALUE_ANCHORKEY = " delay-d/anchorkey ";
	/**ATTR_CLASS_VALUE_ANCHORID.*/
	public static final String ATTR_CLASS_VALUE_ANCHORID = " delay-d/anchorid ";
	//Added by William on 2009-06-24 for req #12014 end
	
	//Added by William on 2009-12-21 for bug:2916469 start
	/**ATTR_CLASS_VALUE_NAVTITLE.*/
	public static final String ATTR_CLASS_VALUE_NAVTITLE = " topic/navtitle ";
	//Added by William on 2009-12-21 for bug:2916469 end
	/**ATTR_CLASS_VALUE_AUTHOR.*/
	public static final String ATTR_CLASS_VALUE_AUTHOR = " topic/author ";
	/**ATTR_CLASS_VALUE_SOURCE.*/
	public static final String ATTR_CLASS_VALUE_SOURCE = " topic/source ";
	/**ATTR_CLASS_VALUE_PUBLISHER.*/
	public static final String ATTR_CLASS_VALUE_PUBLISHER = " topic/publisher ";
	/**ATTR_CLASS_VALUE_COPYRIGHT.*/
	public static final String ATTR_CLASS_VALUE_COPYRIGHT = " topic/copyright ";
	/**ATTR_CLASS_VALUE_CRITDATES.*/
	public static final String ATTR_CLASS_VALUE_CRITDATES = " topic/critdates ";
	/**ATTR_CLASS_VALUE_PERMISSIONS.*/
	public static final String ATTR_CLASS_VALUE_PERMISSIONS = " topic/permissions ";
	/**ATTR_CLASS_VALUE_CATEGORY.*/
	public static final String ATTR_CLASS_VALUE_CATEGORY = " topic/category ";
	/**ATTR_CLASS_VALUE_AUDIENCE.*/
	public static final String ATTR_CLASS_VALUE_AUDIENCE = " topic/audience ";
	/**ATTR_CLASS_VALUE_KEYWORDS.*/
	public static final String ATTR_CLASS_VALUE_KEYWORDS = " topic/keywords ";
	/**ATTR_CLASS_VALUE_PRODINFO.*/
	public static final String ATTR_CLASS_VALUE_PRODINFO = " topic/prodinfo ";
	/**ATTR_CLASS_VALUE_OTHERMETA.*/
	public static final String ATTR_CLASS_VALUE_OTHERMETA = " topic/othermeta ";
	/**ATTR_CLASS_VALUE_RESOURCEID.*/
	public static final String ATTR_CLASS_VALUE_RESOURCEID = " topic/resourceid ";
	/**ATTR_CLASS_VALUE_DATA.*/
	public static final String ATTR_CLASS_VALUE_DATA = " topic/data ";
	/**ATTR_CLASS_VALUE_DATAABOUT.*/
	public static final String ATTR_CLASS_VALUE_DATAABOUT = " topic/data-about ";
	/**ATTR_CLASS_VALUE_DRAFTCOMMENT.*/
	public static final String ATTR_CLASS_VALUE_DRAFTCOMMENT = " topic/draft-comment ";
	/**ATTR_CLASS_VALUE_REQUIREDCLEANUP.*/
	public static final String ATTR_CLASS_VALUE_REQUIREDCLEANUP = " topic/required-cleanup ";
	/**ATTR_CLASS_VALUE_FOREIGN.*/
	public static final String ATTR_CLASS_VALUE_FOREIGN = " topic/foreign ";
	/**ATTR_CLASS_VALUE_UNKNOWN.*/
	public static final String ATTR_CLASS_VALUE_UNKNOWN = " topic/unknown ";
	/**ATTR_CLASS_VALUE_MAP_SEARCHTITLE.*/
	public static final String ATTR_CLASS_VALUE_MAP_SEARCHTITLE = " map/searchtitle ";
	//Added by William on 2009-07-25 for bug:2826143 start
	/**ATTR_CLASS_VALUE_MAP_LINKTEXT.*/
	public static final String ATTR_CLASS_VALUE_MAP_LINKTEXT = " map/linktext ";
	/**ATTR_CLASS_VALUE_MAP_SHORTDESC.*/
	public static final String ATTR_CLASS_VALUE_MAP_SHORTDESC = " map/shortdesc ";
	//Added by William on 2009-07-25 for bug:2826143 start
	/**ATTR_CLASS_VALUE_TOPIC_SEARCHTITLE.*/
	public static final String ATTR_CLASS_VALUE_TOPIC_SEARCHTITLE = " topic/searchtitle ";
	/**ATTR_CLASS_VALUE_PROLOG.*/
	public static final String ATTR_CLASS_VALUE_PROLOG = " topic/prolog ";
	/**ATTR_CLASS_VALUE_ABSTRACT.*/
	public static final String ATTR_CLASS_VALUE_ABSTRACT = " topic/abstract ";
	/**ATTR_CLASS_VALUE_SHORTDESC.*/
	public static final String ATTR_CLASS_VALUE_SHORTDESC = " topic/shortdesc ";
	/**ATTR_CLASS_VALUE_TITLEALTS.*/
	public static final String ATTR_CLASS_VALUE_TITLEALTS = " topic/titlealts ";
	/**ATTR_CLASS_VALUE_RELATED_LINKS.*/
	public static final String ATTR_CLASS_VALUE_RELATED_LINKS = " topic/related-links ";
	/**ATTR_CLASS_VALUE_BODY.*/
	public static final String ATTR_CLASS_VALUE_BODY = " topic/body ";	
	/**ATTR_CLASS_VALUE_RELTABLE.*/
	public static final String ATTR_CLASS_VALUE_RELTABLE = " map/reltable ";
	/**ATTR_CLASS_VALUE_METADATA.*/
	public static final String ATTR_CLASS_VALUE_METADATA = " topic/metadata ";
	/**ATTR_CLASS_VALUE_TOPICHEAD.*/
	public static final String ATTR_CLASS_VALUE_TOPICHEAD = " mapgroup-d/topichead ";
	/**ATTR_CLASS_VALUE_CODEREF.*/
	public static final String ATTR_CLASS_VALUE_CODEREF = " pr-d/coderef ";
	/**ATTR_CLASS_VALUE_TOPIC_GROUP.*/
	public static final String ATTR_CLASS_VALUE_TOPIC_GROUP = " mapgroup-d/topicgroup ";
	/**ATTR_CLASS_VALUE_TOPIC_HEAD.*/
	public static final String ATTR_CLASS_VALUE_TOPIC_HEAD = " mapgroup-d/topichead ";
	/**ATTR_CLASS_VALUE_SUBJECT_SCHEME.*/
	public static final String ATTR_CLASS_VALUE_SUBJECT_SCHEME = " subjectScheme/subjectScheme ";
	/**ATTR_CLASS_VALUE_SUBJECT_DEF.*/
	public static final String ATTR_CLASS_VALUE_SUBJECT_DEF = " subjectScheme/subjectdef ";
	/**ATTR_CLASS_VALUE_SCHEME_REF.*/
	public static final String ATTR_CLASS_VALUE_SCHEME_REF = " subjectScheme/schemeref ";
	/**ATTR_CLASS_VALUE_ENUMERATION_DEF.*/
	public static final String ATTR_CLASS_VALUE_ENUMERATION_DEF = " subjectScheme/enumerationdef ";
	/**ATTR_CLASS_VALUE_ATTRIBUTE_DEF.*/
	public static final String ATTR_CLASS_VALUE_ATTRIBUTE_DEF = " subjectScheme/attributedef ";
	/**ATTR_CLASS_VALUE_ELEMENT_DEF.*/
	public static final String ATTR_CLASS_VALUE_ELEMENT_DEF = " subjectScheme/elementdef ";
	/**ATTR_CLASS_VALUE_DEFAULT_SUBJECT.*/
	public static final String ATTR_CLASS_VALUE_DEFAULT_SUBJECT = " subjectScheme/defaultSubject ";
	/**ATTR_CLASS_VALUE_SUBJECT_SCHEME_BASE.*/
	public static final String ATTR_CLASS_VALUE_SUBJECT_SCHEME_BASE = " subjectScheme/";
	/**ATTR_PROCESSING_ROLE_VALUE_NORMAL.*/
	public static final String ATTR_PROCESSING_ROLE_VALUE_NORMAL = "normal";
	/**ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.*/
	public static final String ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY = "resource-only";
	
	/**ATTR_PRINT_VALUE_YES.*/
	public static final String ATTR_PRINT_VALUE_YES = "yes";
	/**ATTR_PRINT_VALUE_NO.*/
	public static final String ATTR_PRINT_VALUE_NO = "no";
	/**ATTR_PRINT_VALUE_PRINT_ONLY.*/
	public static final String ATTR_PRINT_VALUE_PRINT_ONLY = "printonly";

	
    /**
     * constants for filtering or flagging.
     */	
	public static final String DEFAULT_ACTION = "default";
	/**chunk attribute.*/
	public static final String ATTRIBUTE_NAME_CHUNK = "chunk";
		
	/**constants for indexterm prefix(See).*/
	public static final String IndexTerm_Prefix_See = "See";
	/**constants for indexterm prefix(See also).*/
	public static final String IndexTerm_Prefix_See_Also = "See also";
	/**name attribute.*/
	public static final String ATTRIBUTE_NAME_NAME = "name";
	/**type attribute value subjectScheme.*/
	public static final String ATTR_TYPE_VALUE_SUBJECT_SCHEME = "subjectScheme";
	/**store how many scheme files a ditamap file used in form of Map<String, Set<String>>.*/
	public static final String FILE_NAME_SUBJECT_DICTIONARY = "subject_scheme.dictionary";
	//Added by William on 2009-06-24 for req #12014 start
	/**export.xml to store exported elements.*/
	public static final String FILE_NAME_EXPORT_XML = "export.xml";
	/**pluginId.xml to store the plugin id.*/
	public static final String FILE_NAME_PLUGIN_XML = "pluginId.xml";
	//Added by William on 2009-06-24 for req #12014 start
	
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start
	/** Configuration filename. */
	public static final String CONF_PROPERTIES = "configuration.properties";
	/** Property name for supported image extensions. */
	public static final String CONF_SUPPORTED_IMAGE_EXTENSIONS = "supported_image_extensions";
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- end
	
    /**
     * Instances should NOT be constructed in standard programming.
     */
    private Constants() {
    }
}
