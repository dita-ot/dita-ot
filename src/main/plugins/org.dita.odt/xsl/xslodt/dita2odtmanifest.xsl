<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
  version="1.0">

  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  
  <xsl:strip-space elements="*"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
  <xsl:template match="/">
    <manifest:manifest>
      <xsl:call-template name="root"/>
    </manifest:manifest>
  </xsl:template>

  <xsl:template name="root">
    <!--xsl:call-template name="gen-list-table"/-->
    <manifest:file-entry manifest:media-type="application/vnd.oasis.opendocument.text" manifest:full-path="/"/>
    <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="content.xml"/>
    <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="styles.xml"/>
  </xsl:template>

</xsl:stylesheet>
