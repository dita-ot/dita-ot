<!-- 
  This file is part of the DITA Open Toolkit project hosted on
  Sourceforge.net. See the accompanying license.txt file for
  applicable licenses.
  
  (C) Copyright Shawn McKenzie, 2007. All Rights Reserved.
  *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="topicref.xsl"/>
  <!--<xsl:import href="topichead.xsl"/>-->
  <xsl:import href="jstext.xsl"/>
  <xsl:import href="gethref.xsl"/>
  
  <xsl:param name="ditaext"/>
  <xsl:param name="htmlext"/>
  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:template match="/">

    <!-- need to output an html file that includes refs to necessary js and that builds
        a script element with js entries for the toc -->
    <xsl:text>
     var tree;
          
     function treeInit() {
     tree = new YAHOO.widget.TreeView("treeDiv1");
     var root = tree.getRoot();
    </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>
     tree.draw(); 
     } 
    
     YAHOO.util.Event.addListener(window, "load", treeInit); 
     </xsl:text>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/map ')]">
    <xsl:variable name="parent" select="'root'"/>
    <xsl:apply-templates>
      <xsl:with-param name="parent" select="$parent"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicmeta ')]">
    <!-- do nothing for now -->
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/navref ')]">
    <xsl:message> WARNING! navref not supported. </xsl:message>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/anchor ')]">
    <xsl:message> WARNING! anchor not supported. </xsl:message>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicgroup ')]">
    <!-- do nothing for now -->
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/reltable ')]">
    <!-- do nothing now -->
  </xsl:template>

</xsl:stylesheet>
