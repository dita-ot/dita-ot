<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
  xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
  xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
  xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
  xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
  xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
  xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
  xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
  xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools" version="1.0">
  <xsl:import href="../common/output-message.xsl"/>
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="dita2odt-utilities.xsl"/>

  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="DRAFT" select="'no'"/>
  <xsl:param name="OUTPUTDIR" select="''"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
<!-- Define a newline character -->
<xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>
  
  <xsl:attribute-set name="root">
    <xsl:attribute name="office:version">1.1</xsl:attribute>
  </xsl:attribute-set>

  <xsl:template match="/">
    <office:document-styles xsl:use-attribute-sets="root">
      <xsl:call-template name="root"/>
    </office:document-styles>
  </xsl:template>

  <xsl:template name="root">
    <!--xsl:call-template name="gen-list-table"/-->
    <office:font-face-decls>
      <style:font-face style:name="Courier New" svg:font-family="&apos;Courier New&apos;"
        style:font-family-generic="modern" style:font-pitch="fixed"/>
      <style:font-face style:name="Tahoma1" svg:font-family="Tahoma"/>
      <style:font-face style:name="Arial2" svg:font-family="Arial" style:font-pitch="variable"/>
      <style:font-face style:name="SimSun" svg:font-family="SimSun" style:font-pitch="variable"/>
      <style:font-face style:name="Tahoma" svg:font-family="Tahoma" style:font-pitch="variable"/>
      <style:font-face style:name="Arial" svg:font-family="Arial" style:font-family-generic="roman"
        style:font-pitch="variable"/>
      <style:font-face style:name="Arial1" svg:font-family="Arial" style:font-family-generic="swiss"
        style:font-pitch="variable"/>
    </office:font-face-decls>
    <office:styles>
      <style:default-style style:family="graphic">
        <style:graphic-properties draw:start-line-spacing-horizontal="0.1114in"
          draw:start-line-spacing-vertical="0.1114in" draw:end-line-spacing-horizontal="0.1114in"
          draw:end-line-spacing-vertical="0.1114in"/>
        <style:paragraph-properties style:text-autospace="ideograph-alpha" style:line-break="strict"
          style:writing-mode="lr-tb">
          <style:tab-stops/>
        </style:paragraph-properties>
        <style:text-properties style:use-window-font-color="true" style:font-name="Arial"
          fo:font-size="12pt" fo:language="en" fo:country="US" style:font-name-asian="Arial2"
          style:font-size-asian="12pt" style:language-asian="none" style:country-asian="none"
          style:font-name-complex="Tahoma" style:font-size-complex="12pt"
          style:language-complex="none" style:country-complex="none"/>
      </style:default-style>
      <style:default-style style:family="paragraph">
        <style:paragraph-properties fo:hyphenation-remain-char-count="2"
          fo:hyphenation-push-char-count="2" fo:hyphenation-ladder-count="no-limit"
          style:text-autospace="ideograph-alpha" style:punctuation-wrap="hanging"
          style:line-break="strict" style:tab-stop-distance="0.5in" style:writing-mode="page"/>
        <style:text-properties style:use-window-font-color="true" style:font-name="Arial"
          fo:font-size="12pt" fo:language="en" fo:country="US" style:font-name-asian="Arial2"
          style:font-size-asian="12pt" style:language-asian="none" style:country-asian="none"
          style:font-name-complex="Tahoma" style:font-size-complex="12pt"
          style:language-complex="none" style:country-complex="none" fo:hyphenate="false"/>
      </style:default-style>
      
      <style:style style:name="indent_paragraph_style" style:display-name="indent_paragraph_style"
        style:family="paragraph" style:class="text" style:parent-style-name="Default_20_Text">
        <style:paragraph-properties fo:margin-left="0.25in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false"/>
      </style:style>
      
      <style:style style:name="Default_20_Text" style:family="paragraph">
        <style:text-properties fo:font-size="10.5pt" style:font-size-asian="10.5pt"
          style:font-size-complex="10.5pt"/>
      </style:style>
      
      <style:style style:name="default_text_style" style:family="text">
        <style:text-properties fo:font-size="10.5pt" style:font-size-asian="10.5pt"
          style:font-size-complex="10.5pt"/>
      </style:style>
      
      <!-- code and screen style -->
      <style:style style:name="Code_Paragraph" style:family="paragraph" style:parent-style-name="indent_paragraph_style">
        <style:paragraph-properties fo:margin-left="0cm" fo:margin-right="0cm" fo:margin-top="0cm" fo:margin-bottom="0cm" fo:keep-together="always" fo:text-indent="0cm" style:auto-text-indent="false" fo:background-color="#d9d9d9" fo:padding-left="0cm" fo:padding-right="0cm" fo:padding-top="0.106cm" fo:padding-bottom="0.106cm" fo:border-left="none" fo:border-right="none" fo:border-top="0.002cm solid #000000" fo:border-bottom="0.002cm solid #000000">
          <style:tab-stops>
            <style:tab-stop style:position="0.75cm"/>
            <style:tab-stop style:position="1.499cm"/>
            <style:tab-stop style:position="2.251cm"/>
            <style:tab-stop style:position="3cm"/>
            <style:tab-stop style:position="3.75cm"/>
            <style:tab-stop style:position="4.5cm"/>
            <style:tab-stop style:position="5.249cm"/>
            <style:tab-stop style:position="6.001cm"/>
            <style:tab-stop style:position="6.75cm"/>
            <style:tab-stop style:position="7.5cm"/>
            <style:tab-stop style:position="8.25cm"/>
            <style:tab-stop style:position="8.999cm"/>
          </style:tab-stops>
          <style:background-image/>
        </style:paragraph-properties>
      </style:style>
      
      <style:style style:name="Code_Style_Paragraph" style:family="paragraph" style:parent-style-name="Code_Paragraph">
        <style:paragraph-properties text:number-lines="true" text:line-number="0"/>
      </style:style>
      
      <style:style style:name="Common_Heading_Style" style:display-name="Text Body Single"
        style:family="paragraph" style:parent-style-name="Default_20_Text">
        <style:paragraph-properties fo:margin-top="0in" fo:margin-bottom="0.0835in"/>
      </style:style>
      
      <style:style style:name="Heading" style:family="paragraph"
        style:class="text">
        <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in"
          fo:keep-with-next="always"/>
        <style:text-properties style:font-name="Arial1" fo:font-size="14pt"
          style:font-name-asian="SimSun" style:font-size-asian="14pt"
          style:font-name-complex="Tahoma" style:font-size-complex="14pt"/>
      </style:style>
      
      <!-- Font style -->
      <!-- 
      <style:text-properties
        style:text-position="super 58%" fo:font-style="italic"
        style:text-underline-style="solid"
        fo:font-weight="bold"/>
      -->
      <xsl:comment>Font style</xsl:comment>
      <xsl:value-of select="$newline"/>
      <style:style style:name="bold" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties fo:font-weight="bold" style:font-weight-asian="bold"
          style:font-weight-complex="bold"/>
      </style:style>
      <style:style style:name="sub" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="sub 58%"/>
      </style:style>
      <style:style style:name="sup" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="super 58%"/>
      </style:style>
      <style:style style:name="underline" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-underline-style="solid"
          style:text-underline-type="single" style:text-underline-width="auto"
          style:text-underline-color="font-color"/>
      </style:style>
      <style:style style:name="italic" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties fo:font-style="italic" style:font-style-asian="italic"
          style:font-style-complex="italic"/>
      </style:style>
      <!-- call style templates -->
      <xsl:call-template name="create-aligment-style"/>
      <xsl:call-template name="create-font"/>
      <xsl:call-template name="create-boolean-style"/>
      <xsl:call-template name="create-state-style"/>
      <xsl:call-template name="create-footnote-style"/>
      <xsl:call-template name="create_ul_style"/>
      <xsl:call-template name="create_ol_style"/>
      <xsl:call-template name="create_sl_style"/>
      <xsl:apply-templates/>
      <text:outline-style>
        <text:outline-level-style text:level="1" style:num-format=""/>
        <text:outline-level-style text:level="2" style:num-format=""/>
        <text:outline-level-style text:level="3" style:num-format=""/>
        <text:outline-level-style text:level="4" style:num-format=""/>
        <text:outline-level-style text:level="5" style:num-format=""/>
        <text:outline-level-style text:level="6" style:num-format=""/>
        <text:outline-level-style text:level="7" style:num-format=""/>
        <text:outline-level-style text:level="8" style:num-format=""/>
        <text:outline-level-style text:level="9" style:num-format=""/>
        <text:outline-level-style text:level="10" style:num-format=""/>
      </text:outline-style>
      <text:notes-configuration text:note-class="footnote"
        text:citation-style-name="footnote_symbol"
        text:citation-body-style-name="footnote_anchor" style:num-format="1"
        text:start-value="0" text:footnotes-position="page" text:start-numbering-at="document"/>
      <text:notes-configuration text:note-class="endnote" style:num-format="i" text:start-value="0"/>
      <text:linenumbering-configuration text:number-lines="false" text:offset="0.1965in"
        style:num-format="1" text:number-position="left" text:increment="5"/>
    </office:styles>

    <!-- page number -->
    <office:master-styles>
      <style:master-page style:name="Standard" style:page-layout-name="pm1">
        <style:footer>
          <text:p text:style-name="Footer"><text:tab/><text:tab/>Page <text:page-number text:select-page="current">196</text:page-number> of <text:page-count style:num-format="1">738</text:page-count></text:p>
        </style:footer>
      </style:master-page>
    </office:master-styles>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/topic ')]">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/body ')]">
    <xsl:apply-templates/>  
  </xsl:template>
  
  <!-- create header style -->
  <xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
    <!--xsl:call-template name="gen-id"/-->
    <xsl:variable name="depth">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])"/>
    </xsl:variable>
    <xsl:call-template name="create-header-styles">
      <xsl:with-param name="depth" select="$depth"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- create header style for section -->
  <xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/title ')]">
    <xsl:variable name="depth">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')]) + 1"/>
    </xsl:variable>
    <xsl:call-template name="create-header-styles">
      <xsl:with-param name="depth" select="$depth"/>
    </xsl:call-template>
  </xsl:template>

  <!-- 
  <xsl:template match="synsect"/>


  <xsl:template match="*[contains(@class,' topic/section ')]"/>

  <xsl:template match="*[contains(@class,' topic/example ')]"/>

  <xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]"/>
  
  -->
  
  <!-- header styles -->
  <xsl:template name="create-header-styles">
    <xsl:param name="depth"/>
    <xsl:value-of select="$newline"/>
    <xsl:element name="style:style">
      <xsl:attribute name="style:family">
        <xsl:value-of select="'paragraph'"/>
      </xsl:attribute>
      <xsl:attribute name="style:parent-style-name">
        <xsl:value-of select="'Heading'"/>
      </xsl:attribute>
      <xsl:attribute name="style:next-style-name">
        <xsl:value-of select="'Common_Heading_Style'"/>
      </xsl:attribute>
      <!-- create book mark -->
      <xsl:choose>
        <xsl:when test="$depth='1'">
          <xsl:attribute name="style:display-name">Heading 1</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_1</xsl:attribute>
          <style:text-properties fo:font-size="115%" fo:font-weight="bold"
            style:font-size-asian="115%" style:font-weight-asian="bold"
            style:font-size-complex="115%" style:font-weight-complex="bold"
            style:text-underline-style="solid"
            style:text-underline-type="single" style:text-underline-width="auto"
            style:text-underline-color="font-color"/>
        </xsl:when>
        <xsl:when test="$depth='2'">
          <xsl:attribute name="style:display-name">Heading 2</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_2</xsl:attribute>
          <style:text-properties fo:font-size="100%" fo:font-weight="bold"
            style:font-size-asian="100%" style:font-weight-asian="bold"
            style:font-size-complex="100%" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='3'">
          <xsl:attribute name="style:display-name">Heading 3</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_3</xsl:attribute>
          <style:text-properties fo:font-size="85%" fo:font-weight="bold"
            style:font-size-asian="85%" style:font-weight-asian="bold"
            style:font-size-complex="85%" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='4'">
          <xsl:attribute name="style:display-name">Heading 4</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_4</xsl:attribute>
          <style:text-properties fo:font-size="70%" fo:font-weight="bold"
            style:font-size-asian="70%" style:font-weight-asian="bold"
            style:font-size-complex="115%" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='5'">
          <xsl:attribute name="style:display-name">Heading 5</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_5</xsl:attribute>
          <style:text-properties fo:font-size="55%" fo:font-weight="bold"
            style:font-size-asian="55%" style:font-weight-asian="bold"
            style:font-size-complex="55%" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="style:display-name">Heading 6</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_6</xsl:attribute>
          <style:text-properties fo:font-size="40%" fo:font-weight="bold"
            style:font-size-asian="40%" style:font-weight-asian="bold"
            style:font-size-complex="40%" style:font-weight-complex="bold"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
  
  <!--Create styles for list-->
  <xsl:template name="create_ul_style">
        <xsl:comment>Create styles for list</xsl:comment>
        <xsl:value-of select="$newline"/>
        <text:list-style style:name="list_style" style:display-name="list_style">
          <text:list-level-style-bullet text:level="1" text:style-name="list_style_1"
            style:num-suffix="." text:bullet-char="●">
            <style:list-level-properties text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="2" text:style-name="list_style_2"
            style:num-suffix="." text:bullet-char="•">
            <style:list-level-properties text:space-before="0.2917in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="3" text:style-name="list_style_3"
            style:num-suffix="." text:bullet-char="✔">
            <style:list-level-properties text:space-before="0.5835in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="4" text:style-name="list_style_4"
            style:num-suffix="." text:bullet-char="✗">
            <style:list-level-properties text:space-before="0.8748in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="5" text:style-name="list_style_5"
            style:num-suffix="." text:bullet-char="➔">
            <style:list-level-properties text:space-before="1.1665in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="6" text:style-name="list_style_6"
            style:num-suffix="." text:bullet-char="➢">
            <style:list-level-properties text:space-before="1.4583in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="7" text:style-name="list_style_7"
            style:num-suffix="." text:bullet-char="●">
            <style:list-level-properties text:space-before="1.75in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="8" text:style-name="list_style_8"
            style:num-suffix="." text:bullet-char="•">
            <style:list-level-properties text:space-before="2.0417in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="9" text:style-name="list_style_9"
            style:num-suffix="." text:bullet-char="✔">
            <style:list-level-properties text:space-before="2.3335in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-number text:level="10" style:num-suffix="." style:num-format="1">
            <style:list-level-properties text:space-before="1.7724in"
              text:min-label-width="0.1965in"/>
          </text:list-level-style-number>
        </text:list-style>
    
        <text:list-style style:name="list_style_without_bullet">
          <text:list-level-style-bullet text:level="1" text:style-name="list_style_1"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="2" text:style-name="list_style_2"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="0.2917in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="3" text:style-name="list_style_3"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="0.5835in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="4" text:style-name="list_style_4"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="0.8748in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="5" text:style-name="list_style_5"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="1.1665in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="6" text:style-name="list_style_6"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="1.4583in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="7" text:style-name="list_style_7"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="1.75in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="8" text:style-name="list_style_8"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="2.0417in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-bullet text:level="9" text:style-name="list_style_9"
            style:num-suffix="." text:bullet-char=" ">
            <style:list-level-properties text:space-before="2.3335in"
              text:min-label-width="0.2917in"/>
            <style:text-properties style:font-name="Wingdings"/>
          </text:list-level-style-bullet>
          <text:list-level-style-number text:level="10" style:num-suffix="." style:num-format="1">
            <style:list-level-properties text:space-before="1.7724in"
              text:min-label-width="0.1965in"/>
          </text:list-level-style-number>
        </text:list-style>
  </xsl:template>
  <!-- Simple list style -->
  <xsl:template name="create_sl_style">
    <xsl:comment>Simple list style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <text:list-style style:name="simple_list_style">
      <text:list-level-style-number text:level="1" text:style-name="Numbering_20_Symbols"
        style:num-format="">
        <style:list-level-properties
          text:list-level-position-and-space-mode="label-alignment">
          <style:list-level-label-alignment text:label-followed-by="listtab"
            text:list-tab-stop-position="0.5in" fo:text-indent="-0.25in"
            fo:margin-left="0.5in"/>
        </style:list-level-properties>
      </text:list-level-style-number>
    </text:list-style> 
  </xsl:template>
  
  <!-- Ordered list style. -->
  <xsl:template name="create_ol_style">
    <xsl:comment>Ordered list style.</xsl:comment>
    <xsl:value-of select="$newline"/>
    <text:list-style style:name="ordered_list_style">
      <text:list-level-style-number text:level="1"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="2"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="0.1972in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="3"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="0.3937in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="4"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="0.5909in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="5"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="0.7874in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="6"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="0.9846in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="7"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="1.1815in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="8"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="1.3787in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="9"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="1.5752in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
      <text:list-level-style-number text:level="10"
        style:num-suffix="." style:num-format="1">
        <style:list-level-properties text:space-before="1.7724in"
          text:min-label-width="0.1965in"/>
      </text:list-level-style-number>
    </text:list-style>
  </xsl:template>
  
  <!-- fig style -->
  <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
    <xsl:comment>fig style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <style:style style:name="center" style:family="paragraph">
      <style:paragraph-properties fo:text-align="center" style:justify-single-word="false"/>
    </style:style>
  </xsl:template>
  
  <!-- Alignment style -->
  <xsl:template name="create-aligment-style">
    <xsl:comment>Alignment style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <style:style style:name="left" style:family="paragraph">
      <style:paragraph-properties fo:text-align="left" style:justify-single-word="false"/>
    </style:style>
    
    <style:style style:name="right" style:family="paragraph">
      <style:paragraph-properties fo:text-align="right" style:justify-single-word="false"/>
    </style:style>
    
    <style:style style:name="center" style:family="paragraph">
      <style:paragraph-properties fo:text-align="center" style:justify-single-word="false"/>
    </style:style>
    
    <style:style style:name="justify" style:family="paragraph">
      <style:paragraph-properties fo:text-align="justify" style:justify-single-word="false"/>
    </style:style>
    
  </xsl:template>
  
  <!-- boolean style -->
  <xsl:template name="create-boolean-style">
    <xsl:comment>boolean style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <style:style style:name="boolean_style" style:family="text">
      <style:text-properties fo:color="#00ff00" style:font-name="Arial1"
        style:font-name-complex="Arial1"/>
    </style:style>
  </xsl:template>
  
  <!-- Add for "New <data> element (#9)" in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/data ')]"/>

  <!-- Add for "Support foreign content vocabularies such as 
    MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]"/>
  <!-- font style -->
  <xsl:template name="create-font">
    <xsl:comment>font style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <style:style style:name="Courier_New" style:family="text">
      <style:text-properties style:font-name="Courier New"/>
    </style:style>
  </xsl:template>
  
  <xsl:template name="create-state-style">
    <style:style style:name="state_style" style:family="text">
      <style:text-properties fo:color="#ff0000" style:font-name="Arial1"
        style:font-name-complex="Arial1"/>
    </style:style>
  </xsl:template>
  <!-- footnote style -->
  <xsl:template name="create-footnote-style">
    <xsl:comment>footnote style</xsl:comment>
    <xsl:value-of select="$newline"/>
    <style:style style:name="footnote" style:family="paragraph"
      style:parent-style-name="Default_20_Text" style:class="extra">
      <style:paragraph-properties fo:margin-left="0.1965in" fo:margin-right="0in"
        fo:text-indent="-0.1965in" style:auto-text-indent="false" text:number-lines="false"
        text:line-number="0"/>
      <style:text-properties fo:font-size="10pt" style:font-size-asian="10pt"
        style:font-size-complex="10pt"/>
    </style:style>
    
    <style:style style:name="footnote_symbol" style:display-name="Footnote Symbol"
      style:family="text"/>
    <style:style style:name="footnote_anchor" style:display-name="Footnote anchor"
      style:family="text">
      <style:text-properties style:text-position="super 58%"/>
    </style:style>
  </xsl:template>
  
  
  
  <xsl:template match="text()|@*"/>
  
  
    
</xsl:stylesheet>
