<?xml version="1.0" encoding="UTF-8"?>
<!--
    ============================================================
    Copyright (c) 2007 Antenna House, Inc. All rights reserved.
    Antenna House is a trademark of Antenna House, Inc.
    URL    : http://www.antennahouse.com/
    E-mail : info@antennahouse.com
    ============================================================
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
  
  <xsl:import href="topic2fo.xsl"/>
  
  <xsl:import href="../../cfg/fo/attrs/tables-attr_axf.xsl"/>
  <xsl:import href="../../cfg/fo/attrs/toc-attr_axf.xsl"/>
  <xsl:import href="../../cfg/fo/attrs/index-attr_axf.xsl" />
  <xsl:import href="index_axf.xsl"/>
  
  <dita:extension id="dita.xsl.xslfo" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>
  
  <xsl:import href="cfg:fo/attrs/custom.xsl"/>
  <xsl:import href="cfg:fo/xsl/custom.xsl"/>
  
</xsl:stylesheet>
