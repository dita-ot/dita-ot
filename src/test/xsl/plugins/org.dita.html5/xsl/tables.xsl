<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
  xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable" version="2.0"
  exclude-result-prefixes="xs dita-ot table simpletable">

  <xsl:include href="../../../../../main/plugins/org.dita.html5/xsl/tables.xsl"/>
  <xsl:include href="../../../../../main/plugins/org.dita.html5/xsl/css-class.xsl"/>
  <xsl:include href="../../../../../main/plugins/org.dita.html5/xsl/functions.xsl"/>

  <!-- Mocks -->

  <xsl:function name="dita-ot:get-current-language" as="xs:string">
    <xsl:param name="ctx" as="node()"/>
    <xsl:text>en-US</xsl:text>
  </xsl:function>

  <xsl:template name="commonattributes">
    <xsl:param name="default-output-class"/>
    <xsl:if test="$default-output-class">
      <xsl:attribute name="class" select="$default-output-class"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="setid"/>

  <xsl:template name="style">
    <xsl:param name="contents"/>
    <xsl:if test="normalize-space($contents)">
      <xsl:attribute name="style" select="$contents"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getLowerCaseLang"/>

  <xsl:template name="getVariable">
    <xsl:param name="id"/>
    <xsl:sequence select="$id"/>
  </xsl:template>

  <xsl:key name="enumerableByClass" match="
      *[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/simpletable ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/fn ') and empty(@callout)]" use="tokenize(@class, '\s+')"/>

  <xsl:function name="dita-ot:get-variable" as="node()*">
    <xsl:param name="ctx" as="node()"/>
    <xsl:param name="id" as="xs:string"/>
    <xsl:value-of select="$id"/>
  </xsl:function>

  <xsl:template match="*" mode="set-output-class">
    <xsl:param name="default"/>
    <xsl:variable name="output-class">
      <xsl:apply-templates select="." mode="get-output-class"/>
    </xsl:variable>
    <xsl:variable name="draft-revs" as="xs:string*">
      <!-- If draft is on, add revisions to default class. Simplifies processing in DITA-OT 1.6 and earlier
           that created an extra div or span around revised content, just to hold @class with revs. -->
      <!--
      <xsl:if test="$DRAFT = 'yes'">
        <xsl:sequence select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]/revprop/@val"/>
      </xsl:if>
      -->
    </xsl:variable>
    <xsl:variable name="flag-outputclass" as="xs:string*"
      select="tokenize(normalize-space(*[contains(@class, ' ditaot-d/ditaval-startprop ')]/@outputclass), '\s+')"/>
    <xsl:variable name="using-output-class" as="xs:string*">
      <xsl:choose>
        <xsl:when test="string-length(normalize-space($output-class)) > 0">
          <xsl:value-of select="tokenize(normalize-space($output-class), '\s+')"/>
        </xsl:when>
        <xsl:when test="string-length(normalize-space($default)) > 0">
          <xsl:value-of select="tokenize(normalize-space($default), '\s+')"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="ancestry" as="xs:string?"/>
    <xsl:variable name="outputclass-attribute" as="xs:string">
      <xsl:value-of>
        <xsl:apply-templates select="@outputclass" mode="get-value-for-class"/>
      </xsl:value-of>
    </xsl:variable>
    <!-- Revised design with DITA-OT 1.5: include class ancestry if requested; 
         combine user output class with element default, giving priority to the user value. -->
    <xsl:variable name="classes" as="xs:string*" select="
        tokenize($ancestry, '\s+'),
        $using-output-class,
        $draft-revs,
        tokenize($outputclass-attribute, '\s+'),
        $flag-outputclass"/>
    <xsl:if test="exists($classes)">
      <xsl:attribute name="class" select="distinct-values($classes)" separator=" "/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@outputclass" mode="get-value-for-class">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="node() | @*" mode="get-output-class"/>

</xsl:stylesheet>
