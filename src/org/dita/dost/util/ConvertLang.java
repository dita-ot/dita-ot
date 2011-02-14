/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;
/**
 * This class is for converting charset and escaping 
 * entities in html help component files.
 * 
 * @version 1.0 2010-09-30
 * 
 * @author Zhang Di Hua
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConvertLang extends Task {
	// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 start
	private static final String tag1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String tag2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>[OPTIONS]";
	private static final String tag3 = "&lt;?xml version=\"1.0\" encoding=\"utf-8\"?&gt;";
	// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 end

    private String basedir;
    
    private String outputdir;
    
    private String message;
    
    //code page content
    private String codepg = 
    "<codepages>" +
    "<language lang=\"en-us\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"ar-eg\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"864\"/>" +
    "    <cp format=\"html\" encoding=\"1256\" charset=\"windows-1256\"/>" +
    "    <cp format=\"windows\" encoding=\"1256\" charset=\"windows-1256\"/>" +
    "</language>" +
    "<language lang=\"be-by\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"1251\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"bg-bg\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"915\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"ca-es\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"cs-cz\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"da-dk\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"de-ch\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"de-de\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"el-gr\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"813\"/>" +
    "    <cp format=\"html\" encoding=\"813\" charset=\"iso-8859-7\"/>" +
    "    <cp format=\"windows\" encoding=\"1253\" charset=\"windows-1253\"/>" +
    "</language>" +
    "<language lang=\"en-ca\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"en-gb\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"es-es\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"et-ee\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"922\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1257\" charset=\"windows-1257\"/>" +
    "</language>" +
    "<language lang=\"fi-fi\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"fr-be\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"fr-ca\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"fr-ch\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"fr-fr\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"he-il\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"1255\"/>" +
    "    <cp format=\"html\" encoding=\"1255\" charset=\"windows-1255\"/>" +
    "    <cp format=\"windows\" encoding=\"1255\" charset=\"windows-1255\"/>" +
    "</language>" +
    "<language lang=\"hi-in\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"utf-8\"/>" +
    "    <cp format=\"html\" encoding=\"utf-8\" charset=\"utf-8\"/>" +
    "    <cp format=\"windows\" encoding=\"utf-8\" charset=\"utf-8\"/>" +
    "</language>" +
    "<language lang=\"hr-hr\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"hu-hu\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"is-is\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"it-ch\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"it-it\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"ja-jp\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"943\"/>" +
    "    <cp format=\"html\" encoding=\"943\" charset=\"Shift_JIS\"/>" +
    "    <cp format=\"windows\" encoding=\"943\" charset=\"Shift_JIS\"/>" +
    "</language>" +
    "<language lang=\"kk-kz\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"utf-8\"/>" +
    "    <cp format=\"html\" encoding=\"utf-8\" charset=\"utf-8\"/>" +
    "    <cp format=\"windows\" encoding=\"utf-8\" charset=\"utf-8\"/>" +
    "</language>" +
    "<language lang=\"ko-kr\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"1363\"/>" +
    "    <cp format=\"html\" encoding=\"1363\" charset=\"euc-kr\"/>" +
    "    <cp format=\"windows\" encoding=\"1363\" charset=\"euc-kr\"/>" +
    "</language>" +
    "<language lang=\"lt-lt\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"921\"/>" +
    "    <cp format=\"html\" encoding=\"1257\" charset=\"windows-1257\"/>" +
    "    <cp format=\"windows\" encoding=\"1257\" charset=\"windows-1257\"/>" +
    "</language>" +
    "<language lang=\"lv-lv\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"921\"/>" +
    "    <cp format=\"html\" encoding=\"1257\" charset=\"windows-1257\"/>" +
    "    <cp format=\"windows\" encoding=\"1257\" charset=\"windows-1257\"/>" +
    "</language>" +
    "<language lang=\"mk-mk\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"855\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"nl-be\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"nl-nl\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"no-no\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"pl-pl\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"pt-br\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"pt-pt\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"ro-ro\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"ru-ru\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"866\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"sk-sk\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"sl-si\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"sr-sp\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"855\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"sr-rs\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"855\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"sr-latn-rs\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"852\"/>" +
    "    <cp format=\"html\" encoding=\"912\" charset=\"iso-8859-2\"/>" +
    "    <cp format=\"windows\" encoding=\"1250\" charset=\"windows-1250\"/>" +
    "</language>" +
    "<language lang=\"sr-cs\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"855\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"sv-se\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"850\"/>" +
    "    <cp format=\"html\" encoding=\"819\" charset=\"iso-8859-1\"/>" +
    "    <cp format=\"windows\" encoding=\"1252\" charset=\"windows-1252\"/>" +
    "</language>" +
    "<language lang=\"th-th\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"874\"/>" +
    "    <cp format=\"html\" encoding=\"874\" charset=\"tis-620\"/>" +
    "    <cp format=\"windows\" encoding=\"874\" charset=\"tis-620\"/>" +
    "</language>" +
    "<language lang=\"tr-tr\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"857\"/>" +
    "    <cp format=\"html\" encoding=\"920\" charset=\"iso-8859-9\"/>" +
    "    <cp format=\"windows\" encoding=\"1254\" charset=\"windows-1254\"/>" +
    "</language>" +
    "<language lang=\"uk-ua\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"1251\"/>" +
    "    <cp format=\"html\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "    <cp format=\"windows\" encoding=\"1251\" charset=\"windows-1251\"/>" +
    "</language>" +
    "<language lang=\"ur-pk\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"utf-8\"/>" +
    "    <cp format=\"html\" encoding=\"utf-8\" charset=\"utf-8\"/>" +
    "    <cp format=\"windows\" encoding=\"1256\" charset=\"windows-1256\"/>" +
    "</language>" +
    "<language lang=\"zh-cn\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"1386\"/>" +
    "    <cp format=\"html\" encoding=\"1386\" charset=\"gb2312\"/>" +
    "    <cp format=\"windows\" encoding=\"1386\" charset=\"gb2312\"/>" +
    "</language>" +
    "<language lang=\"zh-tw\">" +
    "    <cp format=\"ibmiddoc\" encoding=\"950\"/>" +
    "    <cp format=\"html\" encoding=\"950\" charset=\"big5\"/>" +
    "    <cp format=\"windows\" encoding=\"950\" charset=\"big5\"/>" +
    "</language>" +
    "</codepages>";
    
    private String langcode;
    //charset map(e.g html = iso-8859-1)
    private Map<String, String>charsetMap = new HashMap<String, String>();
    //lang map(e.g ar- = 0x0c01 Arabic (EGYPT))
    private Map<String, String>langMap = new HashMap<String, String>();
    //entity map(e.g 38 = &amp;)
    private Map<String, String>entityMap = new HashMap<String, String>();
    
    
    private DITAOTJavaLogger logger = new DITAOTJavaLogger();

	/**
     * Executes the Ant task.
     */
    public void execute(){
    	
    	logger.logInfo(message);
    	    	
    	//ensure outdir is absolute
		if (!new File(outputdir).isAbsolute()) {
			outputdir = new File(basedir, outputdir).getAbsolutePath();
		}
		
		//initialize language map
		createLangMap();
		//initialize entity map
		createEntityMap();
		//initialize charset map
        createCharsetMap();
        //change charset of html files
        convertHtmlCharset();
        //update entity and lang code
        updateAllEntitiesAndLangs();
    }

	private void createLangMap() {
		
		langMap.put("ar-", "0x0c01 Arabic (EGYPT)");
		langMap.put("be-", "0x0423 Byelorussian");
		langMap.put("bg-", "0x0402 Bulgarian");
		langMap.put("ca-", "0x0403 Catalan");
		langMap.put("cs-", "0x0405 Czech");
		langMap.put("da-", "0x0406 Danish");
		langMap.put("de-ch-", "0x0807 German (SWITZERLAND)");
		langMap.put("de-", "0x0407 German (GERMANY)");
		langMap.put("el-", "0x0408 Greek");
		langMap.put("en-gb-", "0x0809 English (UNITED KINGDOM)");
		langMap.put("en-uk-", "0x0809 English (UNITED KINGDOM)");
		langMap.put("en-us-", "0x0409 English (United States)");
		langMap.put("en-", "0x0409 English (United States)");
		langMap.put("en", "0x0409 English (United States)");
		langMap.put("es-", "0x040a Spanish (Spain)");
		langMap.put("et-", "0x0425 Estonian");
		langMap.put("fi-", "0x040b Finnish");
		langMap.put("fr-be-", "0x080c French (BELGIUM)");
		langMap.put("fr-ca-", "0x0c0c French (CANADA)");
		langMap.put("fr-ch-", "0x100c French (SWITZERLAND)");
		langMap.put("fr-", "0x040c French (FRANCE)");
		langMap.put("he-", "0x040d Hebrew");
		langMap.put("hr-", "0x041a Croatian");
		langMap.put("hu-", "0x040e Hungarian");
		langMap.put("is-", "0x040f Icelandic");
		langMap.put("it-ch-", "0x0810 Italian (SWITZERLAND)");
		langMap.put("it-", "0x0410 Italian (ITALY)");
		langMap.put("ja-", "0x0411 Japanese");
		langMap.put("ko-", "0x0412 Korean");
		langMap.put("lt-", "0x0427 Lithuanian");
		langMap.put("lv-", "0x0426 Latvian (Lettish)");
		langMap.put("mk-", "0x042f Macedonian");
		langMap.put("nl-be-", "0x0813 Dutch (Belgium)");
		langMap.put("nl-", "0x0413 Dutch (Netherlands)");
		langMap.put("no-", "0x0414 Norwegian (Bokmal)");
		langMap.put("pl-", "0x0415 Polish");
		langMap.put("pt-br-", "0x0416 Portuguese (BRAZIL)");
		langMap.put("pt-pt-", "0x0816 Portuguese (PORTUGAL)");
		langMap.put("pt-", "0x0416 Portuguese (BRAZIL)");
		langMap.put("ro-", "0x0418 Romanian");
		langMap.put("ru-", "0x0419 Russian");
		langMap.put("sk-", "0x041b Slovak");
		langMap.put("sl-", "0x0424 Slovenian");
		langMap.put("sr-cyrl-", "0x0c1a Serbian (Cyrillic)");
		langMap.put("sr-latn-", "0x081a Serbian (Latin)");
		langMap.put("sr-", "0x0c1a Serbian (Cyrillic)");
		langMap.put("sv-", "0x041d Swedish");
		langMap.put("th-", "0x041e Thai");
		langMap.put("tr-", "0x041f Turkish");
		langMap.put("uk-", "0x0422 Ukrainian");
		langMap.put("zh-cn-", "0x0804 Chinese (CHINA)");
		langMap.put("zh-hans-", "0x0804 Chinese (CHINA)");
		langMap.put("zh-tw-", "0x0404 Chinese (TAIWAN, PROVINCE OF CHINA)");
		langMap.put("zh-hant-", "0x0404 Chinese (TAIWAN, PROVINCE OF CHINA)");
		langMap.put("zh-", "0x0804 Chinese (CHINA)");
		
	}
	
	private void createEntityMap(){
		
		entityMap.put("180" ,"&acute;"  );
		entityMap.put("184" ,"&cedil;"  );
		entityMap.put("710" ,"&circ;"   );
		entityMap.put("175" ,"&macr;"   );
		entityMap.put("183" ,"&middot;" );
		entityMap.put("732" ,"&tilde;"  );
		entityMap.put("168" ,"&uml;"    );
		entityMap.put("193" ,"&Aacute;" );
		entityMap.put("225" ,"&aacute;" );
		entityMap.put("194" ,"&Acirc;"  );
		entityMap.put("226" ,"&acirc;"  );
		entityMap.put("198" ,"&AElig;"  );
		entityMap.put("230" ,"&aelig;"  );
		entityMap.put("192" ,"&Agrave;" );
		entityMap.put("224" ,"&agrave;" );
		entityMap.put("197" ,"&Aring;"  );
		entityMap.put("229" ,"&aring;"  );
		entityMap.put("195" ,"&Atilde;" );
		entityMap.put("227" ,"&atilde;" );
		entityMap.put("196" ,"&Auml;"   );
		entityMap.put("228" ,"&auml;"   );
		entityMap.put("199" ,"&Ccedil;" );
		entityMap.put("231" ,"&ccedil;" );
		entityMap.put("201" ,"&Eacute;" );
		entityMap.put("233" ,"&eacute;" );
		entityMap.put("202" ,"&Ecirc;"  );
		entityMap.put("234" ,"&ecirc;"  );
		entityMap.put("200" ,"&Egrave;" );
		entityMap.put("232" ,"&egrave;" );
		entityMap.put("208" ,"&ETH;"    );
		entityMap.put("240" ,"&eth;"    );
		entityMap.put("203" ,"&Euml;"   );
		entityMap.put("235" ,"&euml;"   );
		entityMap.put("205" ,"&Iacute;" );
		entityMap.put("237" ,"&iacute;" );
		entityMap.put("206" ,"&Icirc;"  );
		entityMap.put("238" ,"&icirc;"  );
		entityMap.put("204" ,"&Igrave;" );
		entityMap.put("236" ,"&igrave;" );
		entityMap.put("207" ,"&Iuml;"   );
		entityMap.put("239" ,"&iuml;"   );
		entityMap.put("209" ,"&Ntilde;" );
		entityMap.put("241" ,"&ntilde;" );
		entityMap.put("211" ,"&Oacute;" );
		entityMap.put("243" ,"&oacute;" );
		entityMap.put("212" ,"&Ocirc;"  );
		entityMap.put("244" ,"&ocirc;"  );
		entityMap.put("338" ,"&OElig;"  );
		entityMap.put("339" ,"&oelig;"  );
		entityMap.put("210" ,"&Ograve;" );
		entityMap.put("242" ,"&ograve;" );
		entityMap.put("216" ,"&Oslash;" );
		entityMap.put("248" ,"&oslash;" );
		entityMap.put("213" ,"&Otilde;" );
		entityMap.put("245" ,"&otilde;" );
		entityMap.put("214" ,"&Ouml;"   );
		entityMap.put("246" ,"&ouml;"   );
		entityMap.put("352" ,"&Scaron;" );
		entityMap.put("353" ,"&scaron;" );
		entityMap.put("223" ,"&szlig;"  );
		entityMap.put("222" ,"&THORN;"  );
		entityMap.put("254" ,"&thorn;"  );
		entityMap.put("218" ,"&Uacute;" );
		entityMap.put("250" ,"&uacute;" );
		entityMap.put("219" ,"&Ucirc;"  );
		entityMap.put("251" ,"&ucirc;"  );
		entityMap.put("217" ,"&Ugrave;" );
		entityMap.put("249" ,"&ugrave;" );
		entityMap.put("220" ,"&Uuml;"   );
		entityMap.put("252" ,"&uuml;"   );
		entityMap.put("221" ,"&Yacute;" );
		entityMap.put("253" ,"&yacute;" );
		entityMap.put("255" ,"&yuml;"   );
		entityMap.put("376" ,"&Yuml;"   );
		entityMap.put("162" ,"&cent;"   );
		entityMap.put("164" ,"&curren;" );
		entityMap.put("8364", "&euro;"  );
		entityMap.put("163" ,"&pound;"  );
		entityMap.put("165" ,"&yen;"    );
		entityMap.put("166" ,"&brvbar;" );
		entityMap.put("8226", "&bull;"  );
		entityMap.put("169" ,"&copy;"   );
		entityMap.put("8224", "&dagger;");
		entityMap.put("8225", "&Dagger;");
		entityMap.put("8260", "&frasl;" );
		entityMap.put("8230", "&hellip;");
		entityMap.put("161" ,"&iexcl;"  );
		entityMap.put("8465", "&image;" );
		entityMap.put("191" ,"&iquest;" );
		entityMap.put("8206", "&lrm;"   );
		entityMap.put("8212", "&mdash;" );
		entityMap.put("8211", "&ndash;" );
		entityMap.put("172" ,"&not;"    );
		entityMap.put("8254", "&oline;" );
		entityMap.put("170" ,"&ordf;"   );
		entityMap.put("186" ,"&ordm;"   );
		entityMap.put("182" ,"&para;"   );
		entityMap.put("8240", "&permil;");
		entityMap.put("8242", "&prime;" );
		entityMap.put("8243", "&Prime;" );
		entityMap.put("8476", "&real;"  );
		entityMap.put("174" ,"&reg;"    );
		entityMap.put("8207", "&rlm;"   );
		entityMap.put("167" ,"&sect;"   );
		entityMap.put("173" ,"&shy;"    );
		entityMap.put("185" ,"&sup1;"   );
		entityMap.put("8482", "&trade;" );
		entityMap.put("8472", "&weierp;");
		entityMap.put("8222", "&bdquo;" );
		entityMap.put("171" ,"&laquo;"  );
		entityMap.put("8220", "&ldquo;" );
		entityMap.put("8249", "&lsaquo;");
		entityMap.put("8216", "&lsquo;" );
		entityMap.put("187" ,"&raquo;"  );
		entityMap.put("8221", "&rdquo;" );
		entityMap.put("8250", "&rsaquo;");
		entityMap.put("8217", "&rsquo;" );
		entityMap.put("8218", "&sbquo;" );
		entityMap.put("8195", "&emsp;"  );
		entityMap.put("8194", "&ensp;"  );
		entityMap.put("160" ,"&nbsp;"   );
		entityMap.put("8201", "&thinsp;");
		entityMap.put("8205", "&zwj;"   );
		entityMap.put("8204", "&zwnj;"  );

	}
	
	private void createCharsetMap() {
		try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(codepg);
            InputSource source = new InputSource(reader);
            Document doc = builder.parse(source);
			Element root = doc.getDocumentElement();
			NodeList childNodes = root.getChildNodes();
			//search the node with langcode
			for(int i = 0; i< childNodes.getLength(); i++){
				Node node = childNodes.item(i);
				//only for element node
				if(node.getNodeType() == Node.ELEMENT_NODE){
					Element e = (Element)node;
					String lang = e.getAttribute(Constants.ATTRIBUTE_NAME_LANG);
					//node found
					if(langcode.equalsIgnoreCase(lang)||
					   lang.startsWith(langcode)){
						//store the value into a map
						//charsetMap = new HashMap<String, String>();
						//iterate child nodes skip the 1st one
						NodeList subChild = e.getChildNodes();
						for(int j = 0; j< subChild.getLength(); j++){
							Node subNode = subChild.item(j);
							if(subNode.getNodeType() == Node.ELEMENT_NODE){
								Element elem = (Element)subNode;
								String format = elem.getAttribute(Constants.ATTRIBUTE_NAME_FORMAT);
								String charset = elem.getAttribute(Constants.ATTRIBUTE_NAME_CHARSET);
								//store charset into map
								charsetMap.put(format, charset);
							}
							
						}
						break;
					}
				}
			}
			//no matched charset is found set default value en-us
			if(charsetMap.size() == 0){
				charsetMap.put(Constants.ATTRIBUTE_FORMAT_VALUE_HTML, "iso-8859-1");
				charsetMap.put(Constants.ATTRIBUTE_FORMAT_VALUE_WINDOWS, "windows-1252");
			}
        } catch (Exception e) {
            /* Since an exception is used to stop parsing when the search
             * is successful, catch the exception.
             */
            logger.logException(e);
        }
	}
	
	// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 start
	private String replaceXmlTag(String source,String tag){
		int startPos = source.indexOf(tag);
		int endPos = startPos + tag.length();
		StringBuilder sb = new StringBuilder();
		sb.append(source.substring(0,startPos)).append(source.substring(endPos));
		return sb.toString();
	}
	// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 end

	private void convertHtmlCharset() {
		File outputDir = new File(outputdir);
		File[] files = outputDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				//Recursive method
				convertCharset(files[i]);
		}
		
	}
	//Recursive method
	private void convertCharset(File inputFile){
		if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				convertCharset(files[i]);
			}
		}else if(FileUtils.isHTMLFile(inputFile.getName())||
				FileUtils.isHHCFile(inputFile.getName())||
				FileUtils.isHHKFile(inputFile.getName())){
			
			String fileName = inputFile.getAbsolutePath();
			BufferedReader reader = null;
			Writer writer = null;
			try {
				//prepare for the input and output
				FileInputStream inputStream = new FileInputStream(inputFile);
				InputStreamReader streamReader = new InputStreamReader(inputStream, Constants.UTF8);
				reader = new BufferedReader(streamReader);
									
				File outputFile = new File(fileName + Constants.FILE_EXTENSION_TEMP);
				FileOutputStream outputStream = new FileOutputStream(outputFile);
				OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, Constants.UTF8);
				writer = new BufferedWriter(streamWriter);
				
				String value = reader.readLine();
				while(value != null){
					//meta tag contains charset found
					if(value.contains("<meta http-equiv") && value.contains("charset")){
						int insertPoint = value.indexOf("charset=") + "charset=".length();
						String subString = value.substring(0, insertPoint);
						int remainIndex = value.indexOf(Constants.UTF8) + Constants.UTF8.length();
						String remainString = value.substring(remainIndex);
						//change the charset
						String newValue = subString + charsetMap.get(Constants.ATTRIBUTE_FORMAT_VALUE_HTML) + remainString;
						//write into the output file
						writer.write(newValue);
						//add line break
						writer.write(Constants.LINE_SEPARATOR);
					}else{
						// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 start
						if(value.contains(tag1)){
							value = replaceXmlTag(value,tag1);
						}else if(value.contains(tag2)){
							value = replaceXmlTag(value,tag2);
						}else if(value.contains(tag3)){
							value = replaceXmlTag(value,tag3);
						}
						// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 end
						
						//other values
						writer.write(value);
						writer.write(Constants.LINE_SEPARATOR);
					}
					value = reader.readLine();
				} 
				
				writer.close();
				reader.close();
				
				//delete old file
				if (!inputFile.delete()) {
					Properties prop = new Properties();
					prop.put("%1", inputFile.getPath());
					prop.put("%2", outputFile.getPath());
					logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
							.toString());
				}
				//rename newly created file to the old file
				if (!outputFile.renameTo(inputFile)) {
					Properties prop = new Properties();
					prop.put("%1", inputFile.getPath());
					prop.put("%2", outputFile.getPath());
					logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
							.toString());
				}
				
				
			} catch (FileNotFoundException e) {
				logger.logException(e);
			} catch (UnsupportedEncodingException e) {
				logger.logException(e);
			} catch (IOException e) {
				logger.logException(e);
			} 
		}
	}
	
	private void updateAllEntitiesAndLangs() {
		File outputDir = new File(outputdir);
		File[] files = outputDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			//Recursive method
			updateEntityAndLang(files[i]);
		}
		
	}
	//Recursive method
    private void updateEntityAndLang(File inputFile) {
		//directory case
    	if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				updateEntityAndLang(files[i]);
			}
		}
		//html/hhc/hhk file case
		else if(FileUtils.isHTMLFile(inputFile.getName())||
				FileUtils.isHHCFile(inputFile.getName())||
				FileUtils.isHHKFile(inputFile.getName())){
			//do converting work
			convertEntityAndCharset(inputFile, Constants.ATTRIBUTE_FORMAT_VALUE_HTML);
		
		}
		//hhp file case
		else if(FileUtils.isHHPFile(inputFile.getName())){
			//do converting work
			convertEntityAndCharset(inputFile, Constants.ATTRIBUTE_FORMAT_VALUE_WINDOWS);
			//update language setting of hhp file
			String fileName = inputFile.getAbsolutePath();
			//get new charset
			String charset = charsetMap.get(Constants.ATTRIBUTE_FORMAT_VALUE_WINDOWS);
			BufferedReader reader = null;
			BufferedWriter writer = null;
			try {
				//prepare for the input and output
				FileInputStream inputStream = new FileInputStream(inputFile);
				InputStreamReader streamReader = new InputStreamReader(inputStream, charset);
				//wrapped into reader
				reader = new BufferedReader(streamReader);
				
				File outputFile = new File(fileName + Constants.FILE_EXTENSION_TEMP);
				FileOutputStream outputStream = new FileOutputStream(outputFile);
				
				//convert charset
				OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charset);
				//wrapped into writer
				writer = new BufferedWriter(streamWriter);
				
				String value = reader.readLine();
				while(value != null){
					// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 start
					if(value.contains(tag1)){
						value = replaceXmlTag(value,tag1);
					}else if(value.contains(tag2)){
						value = replaceXmlTag(value,tag2);
					}else if(value.contains(tag3)){
						value = replaceXmlTag(value,tag3);
					}
					// Added on 2010-11-05 for bug Unnecessary XML declaration in HHP and HHC - ID: 3101964 end
					
					//meta tag contains charset found
					if(value.contains("Language=")){
						int insertPoint = value.indexOf("Language=") + "Language=".length();
						String subString = value.substring(0, insertPoint);
						//get new lang
						Set<Entry<String, String>> entrySet = langMap.entrySet();
						for(Entry<String, String> entry : entrySet){
							if(langcode.startsWith(entry.getKey())){
								String lang = entry.getValue();
								//change the language setting
								String newValue = subString + lang;
								//write into the output file
								writer.write(newValue);
								//add line break
								writer.write(Constants.LINE_SEPARATOR);
								break;
							}
						}
						
					}else{
						//other values
						writer.write(value);
						writer.write(Constants.LINE_SEPARATOR);
					}
					value = reader.readLine();
				}
				
				writer.close();
				reader.close();
				//delete old file
				if (!inputFile.delete()) {
					Properties prop = new Properties();
					prop.put("%1", inputFile.getPath());
					prop.put("%2", outputFile.getPath());
					logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
							.toString());
				}
				//rename newly created file to the old file
				if (!outputFile.renameTo(inputFile)) {
					Properties prop = new Properties();
					prop.put("%1", inputFile.getPath());
					prop.put("%2", outputFile.getPath());
					logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
							.toString());
				}
				
				
			} catch (FileNotFoundException e) {
				logger.logException(e);
			} catch (UnsupportedEncodingException e) {
				logger.logException(e);
			} catch (IOException e) {
				logger.logException(e);
			} 
		}
		
	}

	private void convertEntityAndCharset(File inputFile, String format) {
		String fileName = inputFile.getAbsolutePath();
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			//prepare for the input and output
			FileInputStream inputStream = new FileInputStream(inputFile);
			InputStreamReader streamReader = new InputStreamReader(inputStream, Constants.UTF8);
			//wrapped into reader
			reader = new BufferedReader(streamReader);
			
			File outputFile = new File(fileName + Constants.FILE_EXTENSION_TEMP);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			//get new charset
			String charset = charsetMap.get(format);
			//convert charset
			OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charset);
			//wrapped into writer
			writer = new BufferedWriter(streamWriter);
			
			//read a character
			int charCode = reader.read();
			while(charCode != -1){
				String key = String.valueOf(charCode);
				//Is an entity char
				if(entityMap.containsKey(key)){
					//get related entity
					String value = entityMap.get(key);
					//write entity into output file
					writer.write(value);
				}else{
					//normal process
					writer.write(charCode);
				}
				charCode = reader.read();
			}
			writer.close();
			reader.close();
			//delete old file
			if (!inputFile.delete()) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
			//rename newly created file to the old file
			if (!outputFile.renameTo(inputFile)) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
			
			
		} catch (FileNotFoundException e) {
			logger.logException(e);
		} catch (UnsupportedEncodingException e) {
			logger.logException(e);
		} catch (IOException e) {
			logger.logException(e);
		} 
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public void setLangcode(String langcode) {
		this.langcode = langcode;
	}
	
	/*public static void main(String[] args) {
		//prepare for the input and output
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream("C:/20101102/build3/DITA-OT1.5.2/odt_LineBreak/chmOut/DITA-articles.hhp");
			InputStreamReader streamReader = new InputStreamReader(inputStream, Constants.UTF8);
			BufferedReader reader = new BufferedReader(streamReader);
			
			String value = reader.readLine();
			while(value != null){
				System.out.println(value);
				value = reader.readLine();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ConvertLang conv = new ConvertLang();
		conv.setOutputdir("C:/20101102/build3/DITA-OT1.5.2/odt_LineBreak/chmOut/chmOutTest");
		conv.setMessage("test");
		conv.setLangcode("fr-fr");
		conv.execute();
		
		
	}*/

	public void setMessage(String message) {
		this.message = message;
	}

	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

}
