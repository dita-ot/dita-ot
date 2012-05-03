<?xml version="1.0" encoding="UTF-8"?>
<!--
  This file is part of the DITA Open Toolkit project.
  See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                version="2.0"
                exclude-result-prefixes="opentopic-index">

  <!-- Disable index creation -->
  <xsl:template name="createIndex">
    <!--
      FOP 1.0 does not support fo:index-page-citation-list and it does not have
      an extension for similar functionality. As a result, index generation is
      not supported.
    -->
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">010</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Disable index entry processing in keywords -->
  <xsl:template match="opentopic-index:index.entry"/>

</xsl:stylesheet>