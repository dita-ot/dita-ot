<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corporation 2011 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
  
  <xsl:import href="topic2fo.xsl"/>

  <xsl:import href="../../cfg/fo/attrs/commons-attr_xep.xsl"/>
  <xsl:import href="index_xep.xsl"/>
  
  <dita:extension id="dita.xsl.xslfo" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>
  
  <xsl:import href="cfg:fo/attrs/custom.xsl"/>
  <xsl:import href="cfg:fo/xsl/custom.xsl"/>
  
</xsl:stylesheet>
