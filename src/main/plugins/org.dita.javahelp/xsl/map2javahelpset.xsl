<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
  version="1.0">
 
  <xsl:param name="javahelpmap"/>
  <xsl:param name="javahelptoc"/>
  <xsl:param name="basedir"/>
  <xsl:param name="outputdir"/>
  
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
          <xsl:when test="*[contains(@class,' topic/title ')]">
            <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
          </xsl:when>
          <xsl:when test="@title">
            <xsl:value-of select="@title"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Sample Title</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </title>
      <maps>
        <homeID>
			<xsl:variable name="filePath">
				<xsl:choose>
					<xsl:when
						test="contains($outputdir, ':\') or contains($outputdir, ':/')">
						<xsl:value-of select="concat('file:/', $outputdir)" />
					</xsl:when>
					<xsl:when test="starts-with($outputdir, '/')">
						<xsl:value-of select="concat('file://', $outputdir)" />
					</xsl:when>
					<xsl:when test="starts-with($basedir,'/')">
						<xsl:value-of select="concat('file://', $basedir, '/', $outputdir)" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat('file:/', $basedir, '/', $outputdir)" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
            <!--xsl:variable name="homeId" select="document(concat($filePath ,'/', $javahelpmap, '.jhm'), /)"-->
            <xsl:variable name="homeId">
              <xsl:value-of select="document(concat($filePath ,'/', $javahelpmap, '.jhm'))/map/mapID[1]/@target" />
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="$homeId = ''">
                <!--xsl:when test="not(string($homeId))"-->
                <xsl:value-of select="'home'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$homeId"/>
              </xsl:otherwise>
            </xsl:choose>
		</homeID>
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
      <view mergetype="javax.help.AppendMerge">
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
