<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- book.xsl 
 | Merge DITA topics with "validation" of topic property
 *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="root-path"></xsl:param>
<xsl:variable name="xml-path"></xsl:variable>
<xsl:output method="xml"
            encoding="utf-8"
            indent="yes"
/>

<xsl:template match="/*">
   <xsl:element name="{name()}">
     <xsl:apply-templates select="@*" mode="copy-element"/>
     <xsl:apply-templates select="*"/>
   </xsl:element>
</xsl:template>

<xsl:template match="/*/*[contains(@class,' map/topicmeta ')]" priority="1">
  <xsl:apply-templates select="." mode="copy-element"/>
</xsl:template>
<xsl:template match="*[contains(@class,' map/topicmeta ')]"/>
<xsl:template match="*[contains(@class,' map/navref ')]"/>
<xsl:template match="*[contains(@class,' map/reltable ')]"/>
<xsl:template match="*[contains(@class,' map/anchor ')]"/>

<xsl:template match="*[contains(@class,' map/topicref ')][@href][not(@print='no')]">
  <xsl:variable name="topicrefClass"><xsl:value-of select="@class"/></xsl:variable>
  <xsl:comment>Start of imbed for <xsl:value-of select="@href"/></xsl:comment>
  <xsl:choose>
    <xsl:when test="contains(@href,'#')">
      <xsl:variable name="sourcefile"><xsl:value-of select="substring-before(@href,'#')"/></xsl:variable>
      <xsl:variable name="sourcetopic"><xsl:value-of select="substring-after(@href,'#')"/></xsl:variable>
      <xsl:variable name="targetName"><xsl:value-of select="name(document($sourcefile)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1])"/></xsl:variable>
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document($sourcefile)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <xsl:apply-templates select="document($sourcefile)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="$sourcefile"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    <!-- If the target is a topic, as opposed to a ditabase mixed file -->
    <xsl:when test="document(@href)/*[contains(@class,' topic/topic ')]">
      <xsl:variable name="targetName"><xsl:value-of select="name(document(@href)/*)"/></xsl:variable>
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document(@href)/*/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <!-- If the root element of the topic does not contain an id attribute, then generate one.
             Later, we will use these id attributes as anchors for PDF bookmarks. -->
        <xsl:if test="not(document(@href)/*/@id)">
          <xsl:attribute name="id"><xsl:value-of select="generate-id()"/></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="document(@href)/*/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="@href"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    <!-- Otherwise: pointing to ditabase container; output each topic in the ditabase file.
         The refclass value is copied to each of the main topics.
         If this topicref has children, they will be treated as children of the <dita> wrapper.
         This is the same as saving them as peers of the topics in the ditabase file. -->
    <xsl:otherwise>
      <xsl:for-each select="document(@href)/*/*">
        <xsl:element name="{name()}">
          <xsl:apply-templates select="@*" mode="copy-element"/>
          <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
          <xsl:apply-templates select="*" mode="copy-element"/>
        </xsl:element>
      </xsl:for-each>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')][not(@href)]">
  <xsl:element name="{name()}">
    <xsl:apply-templates select="@*" mode="copy-element"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
<xsl:param name="src-file"></xsl:param>
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
      <xsl:with-param name="src-file"><xsl:value-of select="$src-file"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/image ')]/@href" mode="copy-element" priority="1">
<xsl:param name="src-file"></xsl:param>
  <xsl:choose>
    <xsl:when test="contains(.,'://')">
      <xsl:copy/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="extract-path">
        <xsl:with-param name="path"><xsl:value-of select="$src-file"/></xsl:with-param>
        <xsl:with-param name="xml-path"><xsl:value-of select="$root-path"/></xsl:with-param>
        <xsl:with-param name="href" select="."/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template name="extract-path">    
<xsl:param name="path"></xsl:param>  <!-- file name with file path -->
<xsl:param name="xml-path"></xsl:param> <!-- used to extract the file path -->
<xsl:param name="href"></xsl:param>

  <xsl:choose>
  <xsl:when test="contains($path,'/')">
    <xsl:call-template name="extract-path">
    <xsl:with-param name="path"><xsl:value-of select="substring-after($path,'/')"/></xsl:with-param>
    <xsl:with-param name="xml-path"><xsl:value-of select="concat($xml-path,substring-before($path,'/'),'/')"/></xsl:with-param>
    <xsl:with-param name="href"><xsl:value-of select="$href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:choose>
      <xsl:when test="starts-with($xml-path,'/')">
        <xsl:attribute name="href"><xsl:text>file://</xsl:text><xsl:value-of select="concat($xml-path,$href)"/></xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="href"><xsl:text>file:///</xsl:text><xsl:value-of select="concat($xml-path,$href)"/></xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:otherwise>
  </xsl:choose>

</xsl:template>

</xsl:stylesheet>
