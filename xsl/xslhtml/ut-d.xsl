<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- XHTML output with XML syntax -->
<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>


<!-- == User Technologies UNIQUE SUBSTRUCTURES == -->

<!-- imagemap -->
<xsl:template match="*[contains(@class,' ut-d/imagemap ')]" name="topic.ut-d.imagemap">
<div><xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>

  <!-- the image -->
  <xsl:element name="img">
    <xsl:attribute name="usemap">#<xsl:value-of select="generate-id(.)"/></xsl:attribute>
    <xsl:attribute name="border">0</xsl:attribute>
    <!-- Process the 'normal' image attributes, using this special mode -->
    <xsl:apply-templates select="*[contains(@class,' topic/image ')]" mode="imagemap-image"/>
  </xsl:element><xsl:value-of select="$newline"/>

<map name="{generate-id(.)}" id="{generate-id(.)}">

<xsl:for-each select="*[contains(@class,' ut-d/area ')]">
  <xsl:value-of select="$newline"/><xsl:element name="area">

   <!-- if no xref/@href - error -->
  <xsl:choose>
   <xsl:when test="*[contains(@class,' topic/xref ')]/@href">
    <!-- special call to have the XREF/@HREF processor do the work -->
    <xsl:apply-templates select="*[contains(@class, ' topic/xref ')]" mode="imagemap-xref"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="output-message">
     <xsl:with-param name="msgnum">044</xsl:with-param>
     <xsl:with-param name="msgsev">E</xsl:with-param>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>

   <!-- create ALT text from XREF content-->
   <!-- if no XREF content, use @HREF, & put out a warning -->
  <xsl:choose>
    <xsl:when test="*[contains(@class, ' topic/xref ')]">
     <xsl:variable name="alttext"><xsl:apply-templates select="*[contains(@class, ' topic/xref ')]" mode="text-only"/></xsl:variable>
     <xsl:attribute name="alt"><xsl:value-of select="normalize-space($alttext)"/></xsl:attribute>
     <xsl:attribute name="title"><xsl:value-of select="normalize-space($alttext)"/></xsl:attribute>
    </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="output-message">     
     <xsl:with-param name="msgnum">045</xsl:with-param>
     <xsl:with-param name="msgsev">W</xsl:with-param>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>

  <!-- if not valid shape (blank, rect, circle, poly); Warning, pass thru the value -->
  <xsl:variable name="shapeval"><xsl:value-of select="*[contains(@class,' ut-d/shape ')]"/></xsl:variable>
  <xsl:attribute name="shape">
   <xsl:value-of select="$shapeval"/>
  </xsl:attribute>
  <xsl:variable name="shapetest"><xsl:value-of select="concat('-',$shapeval,'-')"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="contains('--rect-circle-poly-default-',$shapetest)"/>
   <xsl:otherwise>
    <xsl:call-template name="output-message">
     <xsl:with-param name="msgnum">046</xsl:with-param>
     <xsl:with-param name="msgsev">W</xsl:with-param>
     <xsl:with-param name="msgparams">%1=<xsl:value-of select="$shapeval"/></xsl:with-param>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>

  <!-- if no coords & shape<>'default'; Warning, pass thru the value -->
  <xsl:variable name="coordval"><xsl:value-of select="*[contains(@class,' ut-d/coords ')]"/></xsl:variable>
  <xsl:choose>
   <xsl:when test="string-length($coordval)>0 and not($shapeval='default')">
    <xsl:attribute name="coords">
     <xsl:value-of select="$coordval"/>
    </xsl:attribute>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="output-message">
     <xsl:with-param name="msgnum">047</xsl:with-param>
     <xsl:with-param name="msgsev">W</xsl:with-param>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>

  </xsl:element>
</xsl:for-each>

<xsl:value-of select="$newline"/></map>
<xsl:call-template name="end-revflag"/>
</div>
</xsl:template>

<!-- In the context of IMAGE - call these attribute processors -->
<xsl:template match="*[contains(@class, ' topic/image ')]" mode="imagemap-image">
 <xsl:apply-templates select="@href|@height|@width|@longdescref"/>
</xsl:template>

<!-- In the context of XREF - call it's HREF processor -->
<xsl:template match="*[contains(@class, ' topic/xref ')]" mode="imagemap-xref">
 <xsl:attribute name="href"><xsl:call-template name="href"/></xsl:attribute>
 <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
  <xsl:attribute name="target">_blank</xsl:attribute>
 </xsl:if>
</xsl:template>

</xsl:stylesheet>
