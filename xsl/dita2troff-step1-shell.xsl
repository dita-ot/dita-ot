<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:saxon="http://icl.com/saxon" extension-element-prefixes="saxon" >

  <!-- Import the main conversion routine -->
  <xsl:import href="troff/step1.xsl"/>

  <!-- Import overrides for highlight domain -->
  <xsl:import href="troff/step1-hi-d.xsl"/>

  <!-- DITAEXT file extension name of dita topic file -->
  <xsl:param name="DITAEXT" select="'.dita'"/>

  <xsl:output method="xml" encoding="UTF-8" indent="no" />

</xsl:stylesheet>