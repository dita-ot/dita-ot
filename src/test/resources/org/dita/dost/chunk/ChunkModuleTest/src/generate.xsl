<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" exclude-result-prefixes="xs" version="2.0">

  <xsl:template match="/">
    <xsl:variable name="tests" as="element()*">
      <test name="combine">
        <map href="combine.ditamap">
          <topicref href="a.dita" chunk="combine">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
        </map>
        <topic id="topic_a" href="a.dita" title="a">
          <p>a</p>
        </topic>
        <topic id="topic_b" href="b.dita" title="b">
          <p>b</p>
        </topic>
        <topic id="topic_c" href="c.dita" title="c">
          <p>c</p>
        </topic>
      </test>
      <test name="map">
        <map href="map.ditamap" chunk="combine">
          <topicref href="a.dita">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
        </map>
        <topic id="topic_r4k_cyw_g4b" href="a.dita" title="a">
          <p>a</p>
        </topic>
        <topic id="topic_ayt_dyw_g4b" href="b.dita" title="b">
          <p>b</p>
        </topic>
        <topic id="topic_ayt_dyw_g4b" href="c.dita" title="c">
          <p>c</p>
        </topic>
      </test>
      <test name="duplicate">
        <map href="combine.ditamap">
          <topicref href="a.dita" chunk="combine">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
        </map>
        <topic id="topic" href="a.dita" title="a">
          <p>a</p>
        </topic>
        <topic id="topic" href="b.dita" title="b">
          <p>b</p>
        </topic>
        <topic id="topic" href="c.dita" title="c">
          <p>c</p>
        </topic>
      </test>
      <test name="multiple">
        <map href="combine.ditamap">
          <topicref href="a.dita" chunk="combine">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
          <topicref href="a.dita">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
        </map>
        <topic id="topic" href="a.dita" title="a">
          <p>a</p>
        </topic>
        <topic id="topic" href="b.dita" title="b">
          <p>b</p>
        </topic>
        <topic id="topic" href="c.dita" title="c">
          <p>c</p>
        </topic>
      </test>
      <test name="link">
        <map href="link.ditamap">
          <topicref href="a.dita" chunk="combine">
            <topicref href="b.dita">
              <topicref href="c.dita"/>
            </topicref>
          </topicref>
        </map>
        <topic id="topic_a" href="a.dita" title="a">
          <link href="b.dita"/>
          <link href="c.dita"/>
        </topic>
        <topic id="topic_b" href="b.dita" title="b">
          <link href="a.dita"/>
          <link href="c.dita"/>
        </topic>
        <topic id="topic_c" href="c.dita" title="c">
          <link href="a.dita"/>
          <link href="b.dita"/>
        </topic>
      </test>
      <test name="nested">
        <map href="nested.ditamap">
          <topicref href="a.dita#topic_a2" chunk="combine">
            <topicref href="b.dita#topic_b2"/>
          </topicref>
        </map>
        <topic id="topic_a1" href="a.dita" title="a1">
          <topic id="topic_a2" title="a2"/>
        </topic>
        <topic id="topic_b1" href="b.dita" title="b1">
          <topic id="topic_b2" title="b2"/>
        </topic>
      </test>
    </xsl:variable>
    <xsl:for-each select="$tests">
      <xsl:apply-templates select="*">
        <xsl:with-param name="dir" select="@name"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="topic">
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

  <xsl:template match="topicref" mode="generate">
    <xsl:copy>
      <xsl:attribute name="class" select="'- map/topicref '"/>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="topic" mode="generate">
    <topic class="- topic/topic " id="{@id}" ditaarch:DITAArchVersion="2.0">
      <title class="- topic/title ">
        <xsl:value-of select="@title"/>
      </title>
      <xsl:if test="link | p">
        <body class="- topic/body ">
          <p class="- topic/p ">
            <xsl:choose>
              <xsl:when test="link">
                <xsl:for-each select="link">
                  <xref class="- topic/xref " href="{@href}"/>
                </xsl:for-each>
              </xsl:when>
              <xsl:when test="p">
                <xsl:value-of select="p"/>
              </xsl:when>
            </xsl:choose>
          </p>
        </body>
      </xsl:if>
      <xsl:apply-templates select="topic" mode="#current"/>
      <xsl:if test="link">
        <related-links class="- topic/related-links ">
          <xsl:for-each select="link">
            <link class="- topic/link " href="{@href}"/>
          </xsl:for-each>
        </related-links>
      </xsl:if>
    </topic>
  </xsl:template>

</xsl:stylesheet>
