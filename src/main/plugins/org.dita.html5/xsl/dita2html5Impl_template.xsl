<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2016 Jarno Elovirta

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/related-links.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-textonly.xsl"/>
  
  <xsl:import href="topic.xsl"/>
  <xsl:import href="concept.xsl"/>
  <xsl:import href="task.xsl"/>
  <xsl:import href="reference.xsl"/>  
  <xsl:import href="ut-d.xsl"/>
  <xsl:import href="sw-d.xsl"/>
  <xsl:import href="pr-d.xsl"/>
  <xsl:import href="ui-d.xsl"/>
  <xsl:import href="hi-d.xsl"/>
  <xsl:import href="abbrev-d.xsl"/>
  <xsl:import href="markup-d.xsl"/>
  <xsl:import href="xml-d.xsl"/>
  
  <xsl:import href="nav.xsl"/>
  
  <xsl:import href="htmlflag.xsl"/>
    
  <dita:extension id="dita.xsl.html5" 
      behavior="org.dita.dost.platform.ImportXSLAction" 
      xmlns:dita="http://dita-ot.sourceforge.net"/>

  <!-- root rule -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
</xsl:stylesheet>
