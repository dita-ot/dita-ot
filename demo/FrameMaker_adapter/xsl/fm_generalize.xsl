<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  generalize.xsl 
 | Convert specialied DITA topics into revertable, "generalized" form
 *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <xsl:template match="@*">
      <xsl:copy/>
    </xsl:template>

    <xsl:template match="@class|@refclass" mode="simplify-class">
                <xsl:variable name="clsString" select="normalize-space(substring-after(.,' '))"></xsl:variable>
                <xsl:choose>
                    <xsl:when test="substring-after($clsString,' ')=''">
                        <xsl:value-of select="translate($clsString,'/','.')"/>
                    </xsl:when>
                    <xsl:otherwise>
                           <xsl:value-of select="translate(substring-after($clsString,' '),'/','.')"/>
                    </xsl:otherwise>
                </xsl:choose>
    </xsl:template>

    <xsl:template match="@class">
      <xsl:attribute name="class">
        <xsl:apply-templates select="." mode="simplify-class"/>
      </xsl:attribute>
    </xsl:template>
                          
<!-- Removed by Paul Prescod -->
<!-- Added back debbiep-sf: required for bookmap processing. -->
   <xsl:template match="@refclass">
      <xsl:attribute name="refclass">
        <xsl:apply-templates select="." mode="simplify-class"/>
      </xsl:attribute>
    </xsl:template>

    <xsl:template match="@id">

	<xsl:copy-of select="."/>

	<xsl:processing-instruction name="Fm">MARKER [Hypertext] newlink <xsl:value-of select="."/></xsl:processing-instruction>
    </xsl:template>

  <xsl:template name="wrap-fm-pi-begin">
    <xsl:if test="@audience">
      <xsl:processing-instruction name="Fm">condstart <xsl:value-of select="@audience"
      /></xsl:processing-instruction>
    </xsl:if>
    <xsl:if test="@platform">
      <xsl:processing-instruction name="Fm">condstart <xsl:value-of select="@platform"
      /></xsl:processing-instruction>
    </xsl:if>
    <xsl:if test="@product">
      <xsl:processing-instruction name="Fm">condstart <xsl:value-of select="@product"
      /></xsl:processing-instruction>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="wrap-fm-pi-end">
    <xsl:if test="@product">
      <xsl:processing-instruction name="Fm">condend <xsl:value-of select="@product"
      /></xsl:processing-instruction>
    </xsl:if>
    <xsl:if test="@platform">
      <xsl:processing-instruction name="Fm">condend <xsl:value-of select="@platform"
      /></xsl:processing-instruction>
    </xsl:if>
    <xsl:if test="@audience">
      <xsl:processing-instruction name="Fm">condend <xsl:value-of select="@audience"
      /></xsl:processing-instruction>
    </xsl:if>
  </xsl:template>
  
    <xsl:template match="*[@class]">
          <xsl:apply-templates select="." mode="replace-tag"/>
    </xsl:template>

  <xsl:template match="*[@class]" mode="replace-tag">
          <xsl:call-template name="wrap-fm-pi-begin"/>
              <xsl:variable name="generalize" select="substring-before(substring-after(@class,'/'),' ')"/>
              <xsl:element name="{$generalize}">
                  <xsl:apply-templates select="@*[name() != 'id']"/>
                  <xsl:apply-templates select="@id"/>
                  <xsl:apply-templates select="." mode="replace-content"/>
              </xsl:element>
        <xsl:call-template name="wrap-fm-pi-end"/>
  </xsl:template>

   <xsl:template match="*" mode="replace-content">
     <xsl:apply-templates select="." mode="process-children"/>
   </xsl:template>

   <xsl:template match="*" mode="process-children">
     <xsl:apply-templates/>
   </xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2006. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->
