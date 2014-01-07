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
      <style:font-face style:name="Courier" svg:font-family="Courier"
        style:font-family-generic="modern"/>
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
      <!-- page break style -->
      <style:style style:name="PB" style:family="paragraph">
        <style:paragraph-properties fo:break-after="page"/>
      </style:style>
      
      <style:style style:name="indent_paragraph_style" style:display-name="indent_paragraph_style"
        style:family="paragraph" style:class="text" style:parent-style-name="Default_20_Text">
        <style:paragraph-properties fo:margin-left="0.25in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false"/>
      </style:style>
      
      <style:style style:name="indent_text_style" style:display-name="indent_text_style"
        style:family="text" style:class="text" style:parent-style-name="default_text_style">
        <style:text-properties fo:margin-left="0.25in" fo:margin-right="0in"
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
        <style:paragraph-properties 
          fo:margin-left="0cm" 
          fo:margin-right="0cm" 
          fo:margin-top="0cm" 
          fo:margin-bottom="0cm" 
          fo:keep-together="always" 
          fo:text-indent="0cm" 
          style:auto-text-indent="false" 
          fo:background-color="#d9d9d9" 
          fo:padding-left="0cm" 
          fo:padding-right="0cm" 
          fo:padding-top="0.106cm" 
          fo:padding-bottom="0.106cm" 
          fo:border-left="none" 
          fo:border-right="none" 
          fo:border-top="0.002cm solid #000000" 
          fo:border-bottom="0.002cm solid #000000">
          <style:background-image/>
        </style:paragraph-properties>
      </style:style>
      <!-- code and screen style -->
      <style:style style:name="Code_Text" style:family="text" style:parent-style-name="indent_text_style">
        <style:text-properties fo:keep-together="always" style:auto-text-indent="false" fo:background-color="#d9d9d9" fo:padding-top="0.106cm" fo:padding-bottom="0.106cm">
          <style:background-image/>
        </style:text-properties>
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
      
      <!-- Table Style -->
      <!-- 
      <style:style style:name="table_style" style:family="table">
        <style:table-properties fo:keep-with-next="true"/>
      </style:style>
      -->
      
      <!-- required-cleanup style -->
      <style:style style:name="required_cleanup_style" style:family="text" style:parent-style-name="indent_paragraph_style">
        <style:text-properties fo:color="#cc3333"/>
      </style:style>
      
      <style:style style:name="bold_paragraph" style:family="paragraph" style:parent-style-name="indent_paragraph_style">
        <style:text-properties fo:font-weight="bold" style:font-weight-asian="bold"
          style:font-weight-complex="bold"/>
      </style:style>
      
      <style:style style:name="bold" style:family="text">
        <style:text-properties fo:font-weight="bold" style:font-weight-asian="bold"
          style:font-weight-complex="bold"/>
      </style:style>
      <style:style style:name="sub1" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-10% 58%"/>
      </style:style>
      <style:style style:name="sub2" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-20% 58%"/>
      </style:style>
      <style:style style:name="sub3" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-30% 58%"/>
      </style:style>
      <style:style style:name="sub4" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-40% 58%"/>
      </style:style>
      <style:style style:name="sub5" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-50% 58%"/>
      </style:style>
      <style:style style:name="sub6" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="-60% 58%"/>
      </style:style>
      <style:style style:name="sup1" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="40% 58%"/>
      </style:style>
      <style:style style:name="sup2" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="50% 58%"/>
      </style:style>
      <style:style style:name="sup3" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="60% 58%"/>
      </style:style>
      <style:style style:name="sup4" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="70% 58%"/>
      </style:style>
      <style:style style:name="sup5" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="80% 58%"/>
      </style:style>
      <style:style style:name="sup6" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-position="90% 58%"/>
      </style:style>
      <style:style style:name="underline" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-underline-style="solid"
          style:text-underline-type="single" style:text-underline-width="auto"
          style:text-underline-color="font-color"/>
      </style:style>
      <style:style style:name="double-underline" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-underline-style="solid"
          style:text-underline-type="double" style:text-underline-width="auto"
          style:text-underline-color="font-color"/>
      </style:style>
      <style:style style:name="overline" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-overline-style="solid"
          style:text-overline-width="auto" style:text-overline-color="font-color"/>
      </style:style>
      <style:style style:name="italic" style:family="text">
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
      
      <!-- standard style -->
      <style:style style:name="Standard" style:family="paragraph" style:class="text">
        <style:paragraph-properties 
        fo:margin-top="0.141cm" fo:margin-bottom="0.141cm" fo:orphans="2" 
        fo:widows="2" text:number-lines="false" text:line-number="0"/>
        <style:text-properties 
        style:use-window-font-color="true" style:font-name="Arial" fo:font-size="10pt" fo:language="en" fo:country="US" 
        style:font-name-asian="Times New Roman" style:font-size-asian="10pt" style:font-name-complex="Times New Roman" 
        style:font-size-complex="12pt" style:language-complex="ar" style:country-complex="SA"/>
      </style:style>
      <!-- book title style -->
      <style:style style:name="Title" style:family="paragraph" style:parent-style-name="Standard" style:class="chapter">
        <style:paragraph-properties 
          fo:margin-top="7cm" fo:margin-bottom="0.423cm" fo:padding-left="0cm" fo:padding-right="0cm" 
          fo:padding-top="0.035cm" fo:padding-bottom="0cm" fo:border-left="none" fo:border-right="none" 
          fo:border-top="none" fo:border-bottom="none"/>
        <style:text-properties 
          fo:color="#333399" fo:font-size="20pt" fo:font-weight="bold" style:letter-kerning="true" 
          style:font-size-asian="24pt" style:font-weight-asian="bold" style:font-name-complex="Arial" 
          style:font-size-complex="16pt" style:font-weight-complex="bold"/>
      </style:style>
      <!-- borderde paragraph -->
      <style:style style:name="border_paragraph" style:family="paragraph" style:parent-style-name="Standard"
        style:class="chapter">
        <style:paragraph-properties fo:border-left="0.0007in solid #000000"
          fo:border-right="0.0007in solid #000000"
          fo:border-top="0.0007in solid #000000"
          fo:border-bottom="0.0007in solid #000000"/>
      </style:style>
      <!-- draft-comment paragraph -->
      <style:style style:name="draftcomment_paragraph" style:family="paragraph" style:parent-style-name="border_paragraph"
        style:class="chapter">
        <style:paragraph-properties fo:background-color="#99ff99"/>
      </style:style>
      <!-- syntaxdiagram style -->
      <style:style style:name="syntaxdiagram_paragraph" style:family="paragraph" style:parent-style-name="Standard"
        style:class="chapter">
        <style:paragraph-properties fo:color="#800000"/>
        <style:text-properties fo:color="#800000"/>
      </style:style>
      <style:style style:name="syntaxdiagram_text" style:family="text" style:parent-style-name="default_text_style"
        style:class="chapter">
        <style:text-properties fo:color="#800000"/>
      </style:style>
      
      <!-- Styles used in toc  start-->
      <style:style style:name="Index" style:family="paragraph"
        style:parent-style-name="Default_20_Text" style:class="index">
        <style:paragraph-properties text:number-lines="false" text:line-number="0"/>
        <style:text-properties fo:color="#0000ff"/>
      </style:style>
      
      <style:style style:name="underline_none" style:family="text" style:parent-style-name="default_text_style">
        <style:text-properties style:text-underline-style="none"
          style:text-underline-type="none" style:text-underline-width="none"
          style:text-underline-color="none"/>
      </style:style>
      
      <style:style style:name="Contents_20_Heading" style:display-name="Contents Heading"
        style:family="paragraph" style:parent-style-name="Heading" style:class="index">
        <style:paragraph-properties fo:margin-left="0in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false" text:number-lines="false"
          text:line-number="0"/>
        <style:text-properties fo:font-size="16pt" fo:font-weight="bold"
          style:font-size-asian="16pt" style:font-weight-asian="bold"
          style:font-size-complex="16pt" style:font-weight-complex="bold"/>
      </style:style>
      
      <style:style style:name="Contents_20_Heading_TOC" style:display-name="Contents Heading"
        style:family="paragraph" style:parent-style-name="Heading" style:class="index">
        <style:paragraph-properties fo:margin-left="0in" fo:margin-right="0in" fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" fo:text-indent="0in"
          style:auto-text-indent="false"
          text:number-lines="false"
          text:line-number="0"/>
        <style:text-properties fo:color="#333399" fo:font-size="18pt" fo:font-weight="bold"
          style:letter-kerning="true" style:font-size-asian="18pt"
          style:font-weight-asian="bold" style:font-name-complex="Arial1"
          style:font-size-complex="16pt" style:font-weight-complex="bold"/>
      </style:style>

      <style:style style:name="Contents_20_1" style:display-name="Contents 1"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="0in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="6.7in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      
      <style:style style:name="Contents_20_2" style:display-name="Contents 2"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="0.2in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="6.5in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_3" style:display-name="Contents 3"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="0.4in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="6.3in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_4" style:display-name="Contents 4"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="0.6in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="6.1n" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_5" style:display-name="Contents 5"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="0.8in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="5.9in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_6" style:display-name="Contents 6"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="1.0in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="5.7in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_7" style:display-name="Contents 7"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="1.2in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="5.5in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_8" style:display-name="Contents 8"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="1.4in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="5.3in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_9" style:display-name="Contents 9"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="1.6in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="5.1in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Contents_20_10" style:display-name="Contents 10"
        style:family="paragraph" style:parent-style-name="Index" style:class="index">
        <style:paragraph-properties fo:margin-left="1.8in" fo:margin-right="0in"
          fo:text-indent="0in" style:auto-text-indent="false">
          <style:tab-stops>
            <style:tab-stop style:position="4.9in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      
      <style:style style:name="P1" style:family="paragraph" style:parent-style-name="Contents_20_1">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="6.7in" style:type="right"
              style:leader-style="dotted" style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      
      <style:style style:name="P2" style:family="paragraph" style:parent-style-name="Contents_20_2">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="6.5in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P3" style:family="paragraph" style:parent-style-name="Contents_20_3">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="6.3in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P4" style:family="paragraph" style:parent-style-name="Contents_20_4">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="6.1in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P5" style:family="paragraph" style:parent-style-name="Contents_20_5">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="5.9in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P6" style:family="paragraph" style:parent-style-name="Contents_20_6">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="5.7in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P7" style:family="paragraph" style:parent-style-name="Contents_20_7">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="5.5in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P8" style:family="paragraph" style:parent-style-name="Contents_20_8">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="5.3in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P9" style:family="paragraph" style:parent-style-name="Contents_20_9">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="5.1in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="P10" style:family="paragraph" style:parent-style-name="Contents_20_10">
        <style:paragraph-properties>
          <style:tab-stops>
            <style:tab-stop style:position="4.9in" style:type="right" style:leader-style="dotted"
              style:leader-text="."/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      <style:style style:name="Sect1" style:family="section">
        <style:section-properties style:editable="false">
          <style:columns fo:column-count="1" fo:column-gap="0in"/>
        </style:section-properties>
      </style:style>
      <!-- Styles used in toc  end-->
      
      <!-- Footer style -->
      <style:style style:name="Footer" style:family="paragraph"
        style:parent-style-name="Default_20_Text" style:class="extra">
        <style:paragraph-properties text:number-lines="false" text:line-number="0">
          <style:tab-stops>
            <style:tab-stop style:position="3in" style:type="center"/>
            <style:tab-stop style:position="6in" style:type="right"/>
          </style:tab-stops>
        </style:paragraph-properties>
      </style:style>
      
    </office:styles>
    
    <office:automatic-styles>
      <style:page-layout style:name="pm1">
        <style:page-layout-properties fo:page-width="8.5in" fo:page-height="11in"
          style:num-format="1" style:print-orientation="portrait" fo:margin-top="1in"
          fo:margin-bottom="1in" fo:margin-left="1.25in" fo:margin-right="1.25in"
          style:shadow="none" style:writing-mode="lr-tb" style:layout-grid-color="#c0c0c0"
          style:layout-grid-lines="20" style:layout-grid-base-height="0.278in"
          style:layout-grid-ruby-height="0.139in" style:layout-grid-mode="none"
          style:layout-grid-ruby-below="false" style:layout-grid-print="true"
          style:layout-grid-display="true" style:footnote-max-height="0in">
          <style:footnote-sep style:width="0.0071in" style:distance-before-sep="0.0402in"
            style:distance-after-sep="0.0402in" style:adjustment="left"
            style:rel-width="25%" style:color="#000000"/>
        </style:page-layout-properties>
        <style:header-style/>
        <style:footer-style>
          <style:header-footer-properties fo:min-height="0.2402in" fo:margin-left="0in"
            fo:margin-right="0in" fo:margin-top="0.2in" style:dynamic-spacing="false"/>
        </style:footer-style>
      </style:page-layout>
    </office:automatic-styles>

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
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="16pt" fo:font-weight="bold"
            style:font-size-asian="16pt" style:font-weight-asian="bold"
            style:font-size-complex="16pt" style:font-weight-complex="bold"
            style:text-underline-style="solid"
            style:text-underline-type="single" style:text-underline-width="auto"
            style:text-underline-color="font-color"/>
        </xsl:when>
        <xsl:when test="$depth='2'">
          <xsl:attribute name="style:display-name">Heading 2</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_2</xsl:attribute>
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="15pt" fo:font-weight="bold"
            style:font-size-asian="15pt" style:font-weight-asian="bold"
            style:font-size-complex="15pt" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='3'">
          <xsl:attribute name="style:display-name">Heading 3</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_3</xsl:attribute>
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="14pt" fo:font-weight="bold"
            style:font-size-asian="14pt" style:font-weight-asian="bold"
            style:font-size-complex="14pt" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='4'">
          <xsl:attribute name="style:display-name">Heading 4</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_4</xsl:attribute>
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="13pt" fo:font-weight="bold"
            style:font-size-asian="13pt" style:font-weight-asian="bold"
            style:font-size-complex="13pt" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:when test="$depth='5'">
          <xsl:attribute name="style:display-name">Heading 5</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_5</xsl:attribute>
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="12pt" fo:font-weight="bold"
            style:font-size-asian="12pt" style:font-weight-asian="bold"
            style:font-size-complex="12pt" style:font-weight-complex="bold"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="style:display-name">Heading 6</xsl:attribute>
          <xsl:attribute name="style:name">Heading_20_6</xsl:attribute>
          <style:paragraph-properties fo:margin-top="0.1665in" fo:margin-bottom="0.0835in" />
          <style:text-properties fo:font-size="11pt" fo:font-weight="bold"
            style:font-size-asian="11pt" style:font-weight-asian="bold"
            style:font-size-complex="11pt" style:font-weight-complex="bold"/>
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
    <style:style style:name="boolean_style" style:family="text" style:parent-style-name="default_text_style">
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
    
    <style:style style:name="Courier" style:family="text">
      <style:text-properties style:font-name="Courier" />
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
        style:font-size-complex="10pt" style:font-name="Courier"/>
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
