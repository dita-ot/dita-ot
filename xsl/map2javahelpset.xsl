<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
 
  <xsl:param name="javahelpmap"/>
  <xsl:param name="javahelptoc"/>
  
  
  <xsl:output
    method="xml"
    omit-xml-declaration="no"
    encoding="UTF-8"
    doctype-public="-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
    doctype-system="http://java.sun.com/products/javahelp/helpset_1_0.dtd"
    indent="yes"/>

  <xsl:template match="*[contains(@class, ' map/map ')]">

    <helpset version="1.0">
      <title>
        <xsl:choose>
          <xsl:when test="@title">
            <xsl:value-of select="@title"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Sample Title</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </title>
      <maps>
        <homeID>home</homeID>        
        <mapref>
          <xsl:attribute name="location">
            <xsl:value-of select="$javahelpmap"/><xsl:text>.jhm</xsl:text>
          </xsl:attribute>
        </mapref>
      </maps>
      <view>
        <name>TOC</name>
        <label>TOC</label>
        <type>javax.help.TOCView</type>
        <data>
          <xsl:value-of select="$javahelptoc"/><xsl:text>.xml</xsl:text>
        </data>
      </view>
      <view mergetype="javax.help.SortMerge">
        <name>index</name>
        <label>Index</label>
        <type>javax.help.IndexView</type>
        <data><xsl:value-of select="$javahelptoc"/><xsl:text>_index.xml</xsl:text></data>
      </view>
      <view>
        <name>Search</name>
        <label>Search</label>
        <type>javax.help.SearchView</type>
        <data engine="com.sun.java.help.search.DefaultSearchEngine"> 
          JavaHelpSearch </data>
      </view>
    </helpset>
    
  </xsl:template>
  
</xsl:stylesheet>
