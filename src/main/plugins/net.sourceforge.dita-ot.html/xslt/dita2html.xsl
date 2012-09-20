<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                xmlns:saxon="http://icl.com/saxon"
                xmlns:java="org.dita.dost.util.StringUtils"
                version="1.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="java dita-ot ditamsg">
  
  <xsl:import href="plugin:org.dita.xhtml:xsl/dita2xhtml.xsl"/>
  <xsl:import href="plugin:org.dita.xhtml:xsl/map2htmtoc/map2htmlImpl.xsl"/>

  <xsl:param name="input.map.url"/>
  <xsl:param name="FILEREF" select="'file:'"/>

  <xsl:variable name="input.map" select="document($input.map.url)"/>

  <xsl:template match="*" mode="chapterBody">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <body>
      <xsl:call-template name="gen-style">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <xsl:if test="@outputclass">
        <xsl:attribute name="class">
          <xsl:value-of select="@outputclass"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="self::dita">
        <xsl:if test="*[contains(@class,' topic/topic ')][1]/@outputclass">
          <xsl:attribute name="class">
            <xsl:value-of select="*[contains(@class,' topic/topic ')][1]/@outputclass"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:apply-templates select="." mode="addAttributesToBody"/>
      <xsl:call-template name="setidaname"/>
      <div id="body">
        <xsl:call-template name="start-flagit">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="start-revflag">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="generateBreadcrumbs"/>
        <xsl:call-template name="gen-user-header"/>
        <xsl:call-template name="processHDR"/>
        <xsl:if test="$INDEXSHOW = 'yes'">
          <xsl:apply-templates select="/*/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/*[contains(@class,' topic/indexterm ')] |
                                       /dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/*[contains(@class,' topic/indexterm ')]"/>
        </xsl:if>
        <xsl:call-template name="gen-user-sidetoc"/>
        <div id="main">
          <xsl:apply-templates/>
          <xsl:call-template name="gen-endnotes"/>
        </div>
        <xsl:call-template name="gen-user-footer"/>
        <xsl:call-template name="processFTR"/>
        <xsl:call-template name="end-revflag">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="end-flagit">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <a href="https://github.com/dita-ot/dita-ot"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>
      </div>
    </body>
    
  </xsl:template>

  <xsl:template match="*" mode="gen-user-sidetoc">
    <div id="nav">
      <xsl:apply-templates select="$input.map/*[contains(@class, ' map/map ')]" mode="toc"/>
    </div>
  </xsl:template>

  <xsl:template match="*" mode="gen-user-header">
    <div id="header">
      <h1>
        <a href="/">
          <xsl:for-each select="$input.map/*[contains(@class, ' map/map ')]">
            <xsl:choose>
              <xsl:when test="*[contains(@class, ' topic/title ')]">
                <xsl:apply-templates select="*[contains(@class, ' topic/title ')]/node()"/>
              </xsl:when>
              <xsl:when test="@title">
                <xsl:value-of select="@title"/>
              </xsl:when>
              <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
                <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]/node()"/>
              </xsl:when>
              <xsl:otherwise>DITA Open Toolkit</xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </a>
      </h1>
      <hr/>
    </div>
  </xsl:template>  

  <xsl:template match="*" mode="gen-user-footer">
    <div id="footer">
      <hr/>
    </div>
  </xsl:template>
  
  <xsl:template name="setanametag"/>

  <xsl:template match="*" mode="add-link-target-attribute"/>
  
  <!-- Navigation -->
    
  <xsl:template match="*[contains(@class, ' map/map ')]">
    <xsl:param name="pathFromMaplist" select="$PATH2PROJ"/>
    <ul>
      <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
        <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
      </xsl:apply-templates>
    </ul>
  </xsl:template>
  
</xsl:stylesheet>
