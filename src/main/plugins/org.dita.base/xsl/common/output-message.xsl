<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->

<!--
  Standard error message template for DITA processing in XSL. 
  Call output-message with an ID that corresponds to a declared
  message in an installed plugin. Additional parameters to
  the message are optional.
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                exclude-result-prefixes="xs dita-ot">
  
  <xsl:template name="output-message">
    <xsl:param name="ctx" select="." tunnel="yes"/>
    <xsl:param name="id" as="xs:string"/>
    <xsl:param name="msg" select="'***'"/>
    <xsl:param name="msgparams" select="''"/>    
    
    <xsl:variable name="msgdoc" select="document('platform:config/messages.xml')" as="document-node()?"/>
    <xsl:variable name="msgcontent" as="xs:string*">
      <xsl:choose>
        <xsl:when test="$msg != '***'">
          <xsl:value-of select="$msg"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$msgdoc/messages/message[@id = $id]" mode="get-message-content">    
            <xsl:with-param name="params" select="$msgparams"/>    
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="msgseverity" as="xs:string*">
      <xsl:value-of select="$msgdoc/messages/message[@id = $id]/@type"/>    
    </xsl:variable>
    <xsl:variable name="localclass" select="$ctx/@class" as="attribute(class)?"/>
    <xsl:variable name="xtrf" select="$ctx/@xtrf" as="attribute(xtrf)?"/>
    <xsl:variable name="xtrc" select="$ctx/@xtrc" as="attribute(xtrc)?"/>
    <xsl:variable name="debugloc">
      <xsl:if test="$xtrf | $xtrc">
        <xsl:if test="$xtrf">
          <xsl:value-of select="$xtrf"/>
        </xsl:if>
        <xsl:if test="$xtrf and $xtrc">
          <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:if test="$xtrc">
          <xsl:value-of select="if (contains($xtrc, ';')) then substring-after($xtrc, ';') else $xtrc"/>
        </xsl:if>
        <xsl:text>: </xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="m" as="xs:string*">
      <xsl:if test="normalize-space($debugloc)">
        <xsl:value-of select="$debugloc"/>
      </xsl:if>
      <xsl:sequence select="$msgcontent"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$msgseverity='FATAL'">
        <xsl:message terminate="yes">
          <xsl:processing-instruction name="error-code" select="$id"/>
          <xsl:processing-instruction name="level" select="$msgseverity"/>
          <xsl:value-of select="$m" separator=""/>
        </xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:processing-instruction name="error-code" select="$id"/>
          <xsl:processing-instruction name="level" select="$msgseverity"/>
          <xsl:value-of select="$m" separator=""/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="message" mode="get-message-content">
    <xsl:param name="params"/>
    <xsl:variable name="reason" select="reason/text()"/>
    <xsl:variable name="response" select="response/text()"/>    
    <xsl:variable name="messageType" select="@type"/>
    <xsl:text>[</xsl:text><xsl:value-of select="@id"/><xsl:text>]</xsl:text>
    <xsl:text>[</xsl:text><xsl:value-of select="@type"/><xsl:text>]</xsl:text>
    <xsl:text>: </xsl:text>
    <xsl:call-template name="replaceParams">
      <xsl:with-param name="string" select="$reason"/>
      <xsl:with-param name="params" select="$params"/>    
    </xsl:call-template>
    <xsl:text> </xsl:text>
    <xsl:call-template name="replaceParams">
      <xsl:with-param name="string" select="$response"/>
      <xsl:with-param name="params" select="$params"/>    
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="replaceParams">
    <xsl:param name="string"/>
    <xsl:param name="params"/>
    <xsl:choose>
      <xsl:when test="contains($params, ';')">
        <xsl:variable name="param" select="substring-before($params, ';')"/>
        <xsl:variable name="newString" as="xs:string">
          <xsl:value-of>
            <xsl:call-template name="replace">
              <xsl:with-param name="text" select="$string"/>
              <xsl:with-param name="from" select="substring-before($param, '=')"/>
              <xsl:with-param name="to" select="substring-after($param, '=')"/>            
            </xsl:call-template>
          </xsl:value-of>
        </xsl:variable>
        <xsl:call-template name="replaceParams">
          <xsl:with-param name="string" select="$newString"/>
          <xsl:with-param name="params" select="substring-after($params, ';')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($params, '=')">
        <xsl:call-template name="replace">
          <xsl:with-param name="text" select="$string"/>
          <xsl:with-param name="from" select="substring-before($params, '=')"/>
          <xsl:with-param name="to" select="substring-after($params, '=')"/>            
        </xsl:call-template>   
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
      
</xsl:stylesheet>

