<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  
 | DITA domains support for the demo set; extend as needed
 |
 *-->


<xsl:stylesheet version="1.0" 
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:output
    method="xml"
    encoding="utf-8"
    indent="no"
/>

<xsl:variable name="ERStringFile" select="document('elementref_strings.xml')"/>


<!-- this element is based on "term" which has implicit linking, so we provide
  processing to ensure that it does not default -->
<xsl:template match="*[contains(@class,' elementref/element ')]">
  <xsl:apply-templates/>
</xsl:template>

<!-- new behavior for longname (apply parentheses) -->
<xsl:template match="*[contains(@class,' elementref/longname ')]"><xsl:text> (</xsl:text><xsl:apply-templates/><xsl:text>)</xsl:text>
</xsl:template>


<!-- title subsections: -->
<xsl:template match="*[contains(@class,' elementref/elementname ')]" priority="2">
<xsl:if test="$trace='yes'"><fo:block color="magenta">{arrived at h1 heading context FOR STANDALONE TOPICS!}</fo:block></xsl:if>
  <xsl:if test="@id"><fo:inline id="{@id}"></fo:inline></xsl:if>
  <fo:block xsl:use-attribute-sets="topictitle1" id="{generate-id()}" line-height="100%">
    <fo:block><fo:leader color="black" leader-pattern="rule" rule-thickness="3pt" leader-length="100%"/></fo:block>
<xsl:variable name="title-txt">
        <xsl:apply-templates select="element"/>
</xsl:variable>
    <fo:block><!-- prefix text? --><xsl:value-of select="$title-txt"/>
      <!--fo:marker marker-class-name="chaptitle">
        <xsl:value-of select="$title-txt"/>
      </fo:marker-->
    </fo:block>
  </fo:block>
  <!--xsl:call-template name="gen-body-qatoc"/--> <!-- only one of these will usually "take" -->
  <!--xsl:call-template name="gen-sect-qatoc"/-->
  <!--xsl:call-template name="gen-ptoc"/-->
</xsl:template>

<!-- new behaviors for booleans in attrequired context -->
<xsl:template match="attrequired/boolean[@state='no']">No</xsl:template>
<xsl:template match="attrequired/boolean[@state='yes']"><fo:inline font-weight="bold">Yes</fo:inline></xsl:template>




<!-- Section level headings: purpose, containedby, contains, attributes, examples-->

<xsl:template match="*[contains(@class,' elementref/purpose ')]">
  <fo:block margin-top="15pt" line-height="12pt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="gen-toc-id"/>
    <fo:block font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Purpose'"/>
        <xsl:with-param name="stringFile" select="$ERStringFile"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' elementref/containedby ')]">
  <fo:block margin-top="15pt" line-height="12pt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="gen-toc-id"/>
    <fo:block font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Contained by'"/>
        <xsl:with-param name="stringFile" select="$ERStringFile"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' elementref/contains ')]">
  <fo:block margin-top="15pt" line-height="12pt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="gen-toc-id"/>
    <fo:block font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Contains'"/>
        <xsl:with-param name="stringFile" select="$ERStringFile"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' elementref/examples ')]">
  <fo:block margin-top="15pt" line-height="12pt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="gen-toc-id"/>
    <fo:block font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Examples'"/>
        <xsl:with-param name="stringFile" select="$ERStringFile"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' elementref/attributes ')]">
  <fo:block margin-top="15pt" line-height="12pt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="gen-toc-id"/>
    <fo:block font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Attributes'"/>
        <xsl:with-param name="stringFile" select="$ERStringFile"/>
      </xsl:call-template>
    </fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' elementref/attlist ')]">
  <fo:table xsl:use-attribute-sets="table.data frameall">
    <xsl:call-template name="semtbl-colwidth"/>
    <fo:table-body>
      <!--xsl:call-template name="gen-dflt-data-hdr"/-->
    <fo:table-row>
      <fo:table-cell start-indent="2pt" background-color="silver" padding="2pt" text-align=
        "center" font-weight="bold" xsl:use-attribute-sets="frameall">
        <xsl:attribute name="column-number">1</xsl:attribute>
        <fo:block>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Name'"/>
          <xsl:with-param name="stringFile" select="$ERStringFile"/>
        </xsl:call-template>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell start-indent="2pt" background-color="silver" padding="2pt" text-align=
        "center" font-weight="bold" xsl:use-attribute-sets="frameall">
        <xsl:attribute name="column-number">2</xsl:attribute>
        <fo:block>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Description'"/>
          <xsl:with-param name="stringFile" select="$ERStringFile"/>
        </xsl:call-template>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell start-indent="2pt" background-color="silver" padding="2pt" text-align=
        "center" font-weight="bold" xsl:use-attribute-sets="frameall">
        <xsl:attribute name="column-number">3</xsl:attribute>
        <fo:block>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Required?'"/>
          <xsl:with-param name="stringFile" select="$ERStringFile"/>
        </xsl:call-template>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
      <xsl:apply-templates/>
    </fo:table-body>
  </fo:table>

</xsl:template>

<!-- we let this default to pick up conref processing for its base class -->
<xsl:template match="xattribute">
</xsl:template>

<!-- support only attname and attdesc and attrequired -->
<xsl:template match="*[contains(@class,' elementref/attname ')]">
  <fo:table-cell column-number="1" start-indent="2pt" background-color="white" padding="2pt" xsl:use-attribute-sets="frameall">
    <fo:block><xsl:apply-templates/></fo:block>
  </fo:table-cell>
</xsl:template>

<xsl:template match="*[contains(@class,' elementref/attdesc ')]">
  <fo:table-cell column-number="2" start-indent="2pt" background-color="white" padding="2pt" xsl:use-attribute-sets="frameall">
    <fo:block><xsl:apply-templates/></fo:block>
  </fo:table-cell>
</xsl:template>

<xsl:template match="*[contains(@class,' elementref/attrequired ')]">
  <fo:table-cell column-number="3" start-indent="2pt" background-color="white" padding="2pt" xsl:use-attribute-sets="frameall">
    <fo:block><xsl:apply-templates/></fo:block>
  </fo:table-cell>
</xsl:template>

<!-- which means we need to consume these events to prevent fallthrough -->
<xsl:template match="*[contains(@class,' elementref/atttype ')]|*[contains(@class,' elementref/attdefvalue ')]">
  <!--td valign="top"><xsl:apply-templates/></td-->
</xsl:template>


</xsl:stylesheet>
