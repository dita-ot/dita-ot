<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="step1.xsl"/>
  <xsl:import href="step1-task.xsl"/>
  <xsl:import href="step1-hi-d.xsl"/>
  <xsl:import href="step1-pr-d.xsl"/>
  <xsl:import href="step1-sw-d.xsl"/>
  <xsl:import href="step1-ui-d.xsl"/>
  <xsl:import href="step1-ut-d.xsl"/>
  <xsl:import href="step1-markup-d.xsl"/>
  <xsl:import href="step1-xml-d.xsl"/>

  <dita:extension id="dita.xsl.troff-ast" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

  <xsl:param name="DEFAULTLANG" select="'en-us'"/>
    
</xsl:stylesheet>
