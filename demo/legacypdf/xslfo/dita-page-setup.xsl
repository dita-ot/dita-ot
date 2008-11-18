<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
  <!ENTITY nbsp          "&#160;">
]>

<!-- This file contains information about page sizes and root template
     for the initial page setup. You may re-use this file in your own
     stylesheets as-is without modificaitons.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.1'>

 <xsl:param name="page.height">
   <xsl:choose>
     <xsl:when test="$page.orientation = 'portrait'">
       <xsl:value-of select="$page.height.portrait"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$page.width.portrait"/>
     </xsl:otherwise>
   </xsl:choose>
 </xsl:param>

 <xsl:param name="page.height.portrait">
   <xsl:choose>
     <xsl:when test="$paper.type = 'A4landscape'">210mm</xsl:when>
     <xsl:when test="$paper.type = 'USletter'">11in</xsl:when>
     <xsl:when test="$paper.type = 'USlandscape'">8.5in</xsl:when>
     <xsl:when test="$paper.type = '4A0'">2378mm</xsl:when>
     <xsl:when test="$paper.type = '2A0'">1682mm</xsl:when>
     <xsl:when test="$paper.type = 'A0'">1189mm</xsl:when>
     <xsl:when test="$paper.type = 'A1'">841mm</xsl:when>
     <xsl:when test="$paper.type = 'A2'">594mm</xsl:when>
     <xsl:when test="$paper.type = 'A3'">420mm</xsl:when>
     <xsl:when test="$paper.type = 'A4'">297mm</xsl:when>
     <xsl:when test="$paper.type = 'A5'">210mm</xsl:when>
     <xsl:when test="$paper.type = 'A6'">148mm</xsl:when>
     <xsl:when test="$paper.type = 'A7'">105mm</xsl:when>
     <xsl:when test="$paper.type = 'A8'">74mm</xsl:when>
     <xsl:when test="$paper.type = 'A9'">52mm</xsl:when>
     <xsl:when test="$paper.type = 'A10'">37mm</xsl:when>
     <xsl:when test="$paper.type = 'B0'">1414mm</xsl:when>
     <xsl:when test="$paper.type = 'B1'">1000mm</xsl:when>
     <xsl:when test="$paper.type = 'B2'">707mm</xsl:when>
     <xsl:when test="$paper.type = 'B3'">500mm</xsl:when>
     <xsl:when test="$paper.type = 'B4'">353mm</xsl:when>
     <xsl:when test="$paper.type = 'B5'">250mm</xsl:when>
     <xsl:when test="$paper.type = 'B6'">176mm</xsl:when>
     <xsl:when test="$paper.type = 'B7'">125mm</xsl:when>
     <xsl:when test="$paper.type = 'B8'">88mm</xsl:when>
     <xsl:when test="$paper.type = 'B9'">62mm</xsl:when>
     <xsl:when test="$paper.type = 'B10'">44mm</xsl:when>
     <xsl:when test="$paper.type = 'C0'">1297mm</xsl:when>
     <xsl:when test="$paper.type = 'C1'">917mm</xsl:when>
     <xsl:when test="$paper.type = 'C2'">648mm</xsl:when>
     <xsl:when test="$paper.type = 'C3'">458mm</xsl:when>
     <xsl:when test="$paper.type = 'C4'">324mm</xsl:when>
     <xsl:when test="$paper.type = 'C5'">229mm</xsl:when>
     <xsl:when test="$paper.type = 'C6'">162mm</xsl:when>
     <xsl:when test="$paper.type = 'C7'">114mm</xsl:when>
     <xsl:when test="$paper.type = 'C8'">81mm</xsl:when>
     <xsl:when test="$paper.type = 'C9'">57mm</xsl:when>
     <xsl:when test="$paper.type = 'C10'">40mm</xsl:when>
     <xsl:otherwise>11in</xsl:otherwise>
   </xsl:choose>
 </xsl:param>

 <xsl:param name="page.width">
   <xsl:choose>
     <xsl:when test="$page.orientation = 'portrait'">
       <xsl:value-of select="$page.width.portrait"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$page.height.portrait"/>
     </xsl:otherwise>
   </xsl:choose>
 </xsl:param>
 <xsl:param name="page.width.portrait">
   <xsl:choose>
     <xsl:when test="$paper.type = 'USletter'">8.5in</xsl:when>
     <xsl:when test="$paper.type = '4A0'">1682mm</xsl:when>
     <xsl:when test="$paper.type = '2A0'">1189mm</xsl:when>
     <xsl:when test="$paper.type = 'A0'">841mm</xsl:when>
     <xsl:when test="$paper.type = 'A1'">594mm</xsl:when>
     <xsl:when test="$paper.type = 'A2'">420mm</xsl:when>
     <xsl:when test="$paper.type = 'A3'">297mm</xsl:when>
     <xsl:when test="$paper.type = 'A4'">210mm</xsl:when>
     <xsl:when test="$paper.type = 'A5'">148mm</xsl:when>
     <xsl:when test="$paper.type = 'A6'">105mm</xsl:when>
     <xsl:when test="$paper.type = 'A7'">74mm</xsl:when>
     <xsl:when test="$paper.type = 'A8'">52mm</xsl:when>
     <xsl:when test="$paper.type = 'A9'">37mm</xsl:when>
     <xsl:when test="$paper.type = 'A10'">26mm</xsl:when>
     <xsl:when test="$paper.type = 'B0'">1000mm</xsl:when>
     <xsl:when test="$paper.type = 'B1'">707mm</xsl:when>
     <xsl:when test="$paper.type = 'B2'">500mm</xsl:when>
     <xsl:when test="$paper.type = 'B3'">353mm</xsl:when>
     <xsl:when test="$paper.type = 'B4'">250mm</xsl:when>
     <xsl:when test="$paper.type = 'B5'">176mm</xsl:when>
     <xsl:when test="$paper.type = 'B6'">125mm</xsl:when>
     <xsl:when test="$paper.type = 'B7'">88mm</xsl:when>
     <xsl:when test="$paper.type = 'B8'">62mm</xsl:when>
     <xsl:when test="$paper.type = 'B9'">44mm</xsl:when>
     <xsl:when test="$paper.type = 'B10'">31mm</xsl:when>
     <xsl:when test="$paper.type = 'C0'">917mm</xsl:when>
     <xsl:when test="$paper.type = 'C1'">648mm</xsl:when>
     <xsl:when test="$paper.type = 'C2'">458mm</xsl:when>
     <xsl:when test="$paper.type = 'C3'">324mm</xsl:when>
     <xsl:when test="$paper.type = 'C4'">229mm</xsl:when>
     <xsl:when test="$paper.type = 'C5'">162mm</xsl:when>
     <xsl:when test="$paper.type = 'C6'">114mm</xsl:when>
     <xsl:when test="$paper.type = 'C7'">81mm</xsl:when>
     <xsl:when test="$paper.type = 'C8'">57mm</xsl:when>
     <xsl:when test="$paper.type = 'C9'">40mm</xsl:when>
     <xsl:when test="$paper.type = 'C10'">28mm</xsl:when>
     <xsl:otherwise>8.5in</xsl:otherwise>
   </xsl:choose>
 </xsl:param>




  <!-- Root template and page setup -->
  <xsl:template name="setup-root"> <!--match="/" priority="3"-->
    <fo:root
          font-family="{$body.font.family}"
          font-size="{$body.font.size}">
      <fo:layout-master-set>
        <fo:simple-page-master
          master-name="body"
          page-width="{$page.width}"
          page-height="{$page.height}"
          margin-top="{$page.margin.top}"
          margin-bottom="{$page.margin.bottom}"
          margin-left="{$page.margin.inner}"
          margin-right="{$page.margin.outer}">
          
          <fo:region-body margin-bottom="{$body.margin.bottom}"
            margin-top="{$body.margin.top}"
            region-name="xsl-region-body">
          </fo:region-body>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="body">
        <fo:flow flow-name="xsl-region-body">
            <xsl:apply-templates/>
            <!--xsl:call-template name="conref-setup"/--> <!-- was start-for-conref -->
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

<!-- for the fallthru above, use apply-templates mode="toplevel" for normal, un-conrefed view; -->
<!-- call "start-for-conref" to invoke full processing on the still-flakey conrefed view -->

<xsl:template name="start-for-conref">
<!-- this should fall through to whatever is the document element of the instance -->
  <xsl:choose>
    <xsl:when test="//@conref">
      <xsl:variable name="copy-tree">
        <xsl:apply-templates mode="resolve-conref"/>
      </xsl:variable>
      <xsl:apply-templates select="$copy-tree/*" mode="toplevel"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates mode="toplevel"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
</xsl:stylesheet>
