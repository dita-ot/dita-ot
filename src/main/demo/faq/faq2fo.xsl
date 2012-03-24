<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  faq2fo.xsl
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

<xsl:variable name="FAQStringFile" select="document('faq_strings.xml')"/>

<!-- Most of the faq demo elements rely on default stylesheet support
     for the base class of the derived elements (ie, faqgroup is processed
     by its base section support) -->

<!-- Some faq elements require specific new behavior rather than the default, 
     base class support of the derived elements.  These new behaviors are
     given below.  Overrides to the overall behavior of the output (tweaks, or
     things that are independent of the topic content, typically) can be 
     added to the shell stylesheet that imports both the base class and the
     specialization-specific behaviors. -->

<!-- The faqlist is modelled on simpletable, which has a tabular output by default.
     This implementation overrides the default by producing a sequentially-presented
     list instead.   -->

<xsl:template match="*[contains(@class,' faq/faqlist ')]">
  <fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' faq/faqitem ')]">
  <fo:block>
    <!-- suppress faqprop by selecting all child elements BUT faqprop -->
    <xsl:apply-templates select="*[not( contains(@class,' faq/faqprop ') )]"/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' faq/faqquest ')]">
  <!-- a real version would style the element via a class defined 
       in an external CSS file -->
  <fo:block>
    <fo:inline font-style="italic" font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Question'"/>
        <xsl:with-param name="stringFile" select="$FAQStringFile"/>
      </xsl:call-template>
      <xsl:text>:</xsl:text>
    </fo:inline>
    <xsl:text> </xsl:text>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' faq/faqans ')]">
  <fo:block margin-top="0.5em" margin-left="1em">
    <fo:inline font-style="italic" font-weight="bold">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Answer'"/>
        <xsl:with-param name="stringFile" select="$FAQStringFile"/>
      </xsl:call-template>
      <xsl:text>:</xsl:text>
    </fo:inline>
    <xsl:text> </xsl:text>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- A specialized rule can customize the processing of a base element
     within its content.  This rule provides special behavior for keyword
     elements in the context of a faqlist. -->

<xsl:template match="*[contains(@class,' faq/faqlist ')] // 
    *[contains(@class,' topic/keyword ')]">
  <fo:inline font-style="italic">
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>

</xsl:stylesheet>
