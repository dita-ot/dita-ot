<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    exclude-result-prefixes="xsl opentopic-func"
    xmlns:suitesol="http://suite-sol.com/namespaces/mapcounts"
    version="2.0">

   <xsl:import href="flag-rules.xsl"/>
   <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
   <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
   <xsl:import href="flagging-preprocessImpl.xsl"/>
  
   <dita:extension id="dita.xsl.xslfo.flagging-preprocess"
     behavior="org.dita.dost.platform.ImportXSLAction"
     xmlns:dita="http://dita-ot.sourceforge.net"
   />

   <xsl:param name="filterFile" select="''"/>


</xsl:stylesheet>