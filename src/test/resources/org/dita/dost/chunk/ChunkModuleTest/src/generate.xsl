<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xmlns:x="x" exclude-result-prefixes="xs ditaarch x"
  version="3.0">

  <xsl:template match="/">
    <xsl:apply-templates select="tests/test"/>
  </xsl:template>

  <xsl:template match="test">
    <xsl:variable name="extends" select="
        if (exists(@extends)) then
          ../test[@name = current()/@extends]
        else
          ()"/>
    <xsl:apply-templates select="(*, $extends/*) except resource">
      <xsl:with-param name="dir" select="@name"/>
    </xsl:apply-templates>
    <xsl:result-document href="{@name}/.job.xml" indent="yes" omit-xml-declaration="yes">
      <job>
        <property name="user.input.dir.uri">
          <string>file:/</string>
        </property>
        <files>
          <xsl:for-each select="*, $extends/*">
            <file src="file:/{@href}" uri="{@href}" path="{@href}">
              <xsl:choose>
                <xsl:when test="self::map">
                  <xsl:attribute name="format">ditamap</xsl:attribute>
                  <xsl:attribute name="input">true</xsl:attribute>
                </xsl:when>
                <xsl:when test="self::resource">
                  <xsl:copy-of select="@format"/>
                  <xsl:attribute name="target">true</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="format">dita</xsl:attribute>
                  <xsl:attribute name="target">true</xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
            </file>
          </xsl:for-each>
        </files>
      </job>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="test[@abstact = 'true']" priority="10"/>

  <xsl:template match="topic | dita">
    <xsl:param name="dir" as="xs:string"/>
    <xsl:result-document href="{$dir}/{@href}" indent="yes" omit-xml-declaration="yes">
      <xsl:apply-templates select="." mode="generate"/>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="map">
    <xsl:param name="dir" as="xs:string"/>
    <xsl:result-document href="{$dir}/{@href}" indent="yes" omit-xml-declaration="yes">
      <xsl:copy>
        <xsl:attribute name="class" select="'- map/map '"/>
        <xsl:attribute name="ditaarch:DITAArchVersion">2.0</xsl:attribute>
        <xsl:apply-templates select="@* except @href | node()" mode="generate"/>
      </xsl:copy>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="@* | node()" mode="generate" priority="-10">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="navtitle" mode="generate">
    <xsl:copy>
      <xsl:attribute name="class" select="concat('- topic/', local-name(), ' ')"/>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="topicref | topicmeta" mode="generate">
    <xsl:copy>
      <xsl:attribute name="class" select="concat('- map/', local-name(), ' ')"/>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="topicgroup | topichead" mode="generate">
    <xsl:copy>
      <xsl:attribute name="class" select="concat('+ map/topicref mapgroup-d/', local-name(), ' ')"/>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="topic" mode="generate">
    <xsl:variable name="test" select="ancestor::test" as="element()"/>
    <topic class="- topic/topic " id="{@id}" ditaarch:DITAArchVersion="2.0">
      <title class="- topic/title ">
        <xsl:value-of select="@title"/>
      </title>
      <xsl:if test="exists(link | p | image) or $test/@generate-links = 'true'">
        <body class="- topic/body ">
          <xsl:for-each select="image">
            <image class="- topic/image ">
              <xsl:copy-of select="@*"/>
            </image>
          </xsl:for-each>
          <p class="- topic/p ">
            <xsl:choose>
              <xsl:when test="p">
                <xsl:value-of select="p"/>
              </xsl:when>
              <xsl:when test="link">
                <xsl:for-each select="link">
                  <xref class="- topic/xref ">
                    <xsl:copy-of select="@*"/>
                  </xref>
                </xsl:for-each>
              </xsl:when>
            </xsl:choose>
            <xsl:if test="$test/@generate-links = 'true'">
              <xsl:call-template name="generate-links">
                <xsl:with-param name="test" select="$test"/>
                <xsl:with-param name="element" as="element()">
                  <xref class="- topic/xref "/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:if>
          </p>
        </body>
      </xsl:if>
      <xsl:if test="link or $test/@generate-links = 'true'">
        <related-links class="- topic/related-links ">
          <xsl:for-each select="link">
            <link class="- topic/link ">
              <xsl:copy-of select="@*"/>
            </link>
          </xsl:for-each>
          <xsl:if test="$test/@generate-links = 'true'">
            <xsl:call-template name="generate-links">
              <xsl:with-param name="test" select="$test"/>
              <xsl:with-param name="element" as="element()">
                <link class="- topic/link "/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
        </related-links>
      </xsl:if>
      <xsl:apply-templates select="topic" mode="#current"/>
    </topic>
  </xsl:template>

  <xsl:template name="generate-links">
    <xsl:param name="test" as="element()"/>
    <xsl:param name="element" as="element()"/>
    <xsl:variable name="base-uri" select="ancestor-or-self::topic[@href][1]/@href"/>
    <xsl:variable name="current-topic" select="."/>
    <xsl:for-each select="$test//topic[not(. is $current-topic)]">
      <xsl:variable name="current-root-topic" select="$current-topic/ancestor-or-self::topic[last()]"/>
      <xsl:variable name="target-root-topic" select="ancestor-or-self::topic[last()]"/>
      <xsl:variable name="href" select="$target-root-topic/@href"/>
      <xsl:element name="{name($element)}">
        <xsl:copy-of select="$element/@*"/>
        <xsl:attribute name="href">
          <xsl:choose>
            <xsl:when test="$current-root-topic is $target-root-topic">
              <xsl:text>#</xsl:text>
              <xsl:value-of select="@id"/>
            </xsl:when>
            <xsl:when test="$current-topic is $target-root-topic">
              <xsl:text>1: </xsl:text>
              <xsl:value-of select="x:relativize($href, $base-uri)"/>
              <xsl:if test="exists(parent::topic)">
                <xsl:text>#</xsl:text>
                <xsl:value-of select="@id"/>
              </xsl:if>
            </xsl:when>
            <xsl:when test="$current-topic is $target-root-topic">
              <xsl:text>2: </xsl:text>
              <xsl:value-of select="x:relativize($href, $base-uri)"/>
              <xsl:if test="exists(parent::topic)">
                <xsl:text>#</xsl:text>
                <xsl:value-of select="@id"/>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="x:relativize($href, $base-uri)"/>
              <xsl:if test="exists(parent::topic)">
                <xsl:text>#</xsl:text>
                <xsl:value-of select="@id"/>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>

  <xsl:function name="x:relativize" as="xs:string">
    <xsl:param name="this" as="xs:string"/>
    <xsl:param name="that" as="xs:string"/>
    <xsl:variable name="this-tokens" as="xs:string+" select="tokenize($this, '/')"/>
    <xsl:variable name="that-tokens" as="xs:string+" select="tokenize($that, '/')"/>
    <xsl:value-of select="x:relativize.strip-and-prefix($that-tokens, $this-tokens)"/>
  </xsl:function>

  <xsl:function name="x:relativize.strip-and-prefix" as="xs:string">
    <xsl:param name="a" as="xs:string+"/>
    <xsl:param name="b" as="xs:string+"/>
    <xsl:choose>
      <xsl:when test="head($a) = head($b) and count($a) ne 1 and count($b) ne 1">
        <xsl:sequence select="x:relativize.strip-and-prefix(tail($a), tail($b))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of>
          <xsl:for-each select="tail($a)">../</xsl:for-each>
          <xsl:value-of select="$b" separator="/"/>
        </xsl:value-of>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="dita" mode="generate">
    <dita ditaarch:DITAArchVersion="2.0">
      <xsl:apply-templates select="topic" mode="#current"/>
    </dita>
  </xsl:template>

</xsl:stylesheet>
