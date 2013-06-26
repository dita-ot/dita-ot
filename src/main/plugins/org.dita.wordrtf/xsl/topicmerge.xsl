<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- book.xsl 
 | Merge DITA topics with "validation" of topic property
 *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<!-- Include error message template -->
<xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>

<!-- Set the prefix for error message numbers -->
<xsl:variable name="msgprefix">DOTX</xsl:variable>

<xsl:variable name="xml-path"></xsl:variable>

<xsl:output method="xml" encoding="utf-8" />

<xsl:template match="/*">
   <xsl:element name="{name()}">
     <xsl:apply-templates select="@*" mode="copy-element"/>
     <xsl:apply-templates select="*"/>
   </xsl:element>
</xsl:template>

<xsl:template match="/*/*[contains(@class,' map/topicmeta ')]" priority="1">
  <xsl:apply-templates select="." mode="copy-element"/>
</xsl:template>

<xsl:template match="/*[contains(@class,' map/map ')]/*[contains(@class,' topic/title ')]">
  <xsl:apply-templates select="." mode="copy-element"/>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicmeta ')]"/>
<xsl:template match="*[contains(@class,' map/navref ')]"/>
<xsl:template match="*[contains(@class,' map/reltable ')]"/>
<xsl:template match="*[contains(@class,' map/anchor ')]"/>

<xsl:template match="*[contains(@class,' map/topicref ')][@href][not(@href='')][not(@print='no')]">
  <xsl:variable name="topicrefClass"><xsl:value-of select="@class"/></xsl:variable>
  <xsl:comment>Start of imbed for <xsl:value-of select="@href"/></xsl:comment>
  <xsl:choose>
    <xsl:when test="@format and not(@format='dita')">
       <!-- Topicref to non-dita files will be ingored in PDF transformation -->
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">049</xsl:with-param>
        <xsl:with-param name="msgsev">I</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="contains(@href,'#')">
      <xsl:variable name="sourcefile"><xsl:value-of select="substring-before(@href,'#')"/></xsl:variable>
      <xsl:variable name="sourcetopic"><xsl:value-of select="substring-after(@href,'#')"/></xsl:variable>
      <xsl:variable name="targetName"><xsl:value-of select="name(document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1])"/></xsl:variable>
      <xsl:if test="$targetName and not($targetName='')">
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <xsl:apply-templates select="document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="$sourcefile"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
      </xsl:if>
    </xsl:when>
    <!-- If the target is a topic, as opposed to a ditabase mixed file -->
    <xsl:when test="document(@href,/)/*[contains(@class,' topic/topic ')]">
      <xsl:variable name="targetName"><xsl:value-of select="name(document(@href,/)/*)"/></xsl:variable>
      <xsl:if test="$targetName and not($targetName='')">
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document(@href,/)/*/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <!-- If the root element of the topic does not contain an id attribute, then generate one.
             Later, we will use these id attributes as anchors for PDF bookmarks. -->
        <xsl:if test="not(document(@href,/)/*/@id)">
          <xsl:attribute name="id"><xsl:value-of select="generate-id()"/></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="document(@href,/)/*/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="@href"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
      </xsl:if>
    </xsl:when>
    <!-- Otherwise: pointing to ditabase container; output each topic in the ditabase file.
         The refclass value is copied to each of the main topics.
         If this topicref has children, they will be treated as children of the <dita> wrapper.
         This is the same as saving them as peers of the topics in the ditabase file. -->
    <xsl:otherwise>
      <xsl:for-each select="document(@href,/)/*/*">
        <xsl:element name="{name()}">
          <xsl:apply-templates select="@*" mode="copy-element"/>
          <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
          <xsl:apply-templates select="*" mode="copy-element"/>
        </xsl:element>
      </xsl:for-each>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')][not(@href)]">
  <xsl:element name="{name()}">
    <xsl:apply-templates select="@*" mode="copy-element"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
<xsl:param name="src-file"></xsl:param>
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
      <xsl:with-param name="src-file"><xsl:value-of select="$src-file"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

  <xsl:template match="@id" mode="copy-element">
    <xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
  </xsl:template>

<xsl:template match="@href" mode="copy-element" priority="1">
  <xsl:param name="src-file"></xsl:param>

  <xsl:variable name="file-path">  
    <xsl:call-template name="get-file-path">
      <xsl:with-param name="src-file">
        <xsl:value-of select="$src-file"/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:value-of select="."/>
  </xsl:variable>

  <xsl:variable name="file-path-new">
    <xsl:call-template name="normalize-path">
      <xsl:with-param name="file-path">
        <xsl:value-of select="translate($file-path,'\','/')"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>  

  <xsl:choose>
    <xsl:when test="contains(.,'://') or ../@scope='external' or ../@scope='peer'">
      <xsl:copy/>
    </xsl:when>
    <xsl:when test="(parent::*[contains(@class,' topic/xref ')] or parent::*[contains(@class,' topic/link ')]) and (not(../@format) or ../@format='dita' or ../@format='DITA')">
      <xsl:choose>
        <xsl:when test="starts-with(.,'#')">
          <xsl:variable name="refer-path" select="substring-after(.,'#')"/>
          <xsl:choose>
            <xsl:when test="contains($refer-path,'/')">
              <xsl:variable name="topic-id" select="substring-before($refer-path,'/')"/>
              <xsl:variable name="target-id" select="substring-after($refer-path,'/')"/>
              <xsl:variable name="href-value">
                <xsl:value-of select="generate-id(//*[contains(@class,' topic/topic ')][@id=$topic-id]//*[@id=$target-id]/@id)"/>
              </xsl:variable>
              <xsl:if test="not($href-value='')">
                <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="$href-value"/></xsl:attribute>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="href-value">
                <xsl:value-of select="generate-id(//*[contains(@class,' topic/topic ')][@id=$refer-path]/@id)"/>
              </xsl:variable>
              <xsl:if test="not($href-value='')">
                <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="$href-value"/></xsl:attribute>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="contains(.,'#')">
          <xsl:variable name="file-name" select="substring-before(.,'#')"/>
          <xsl:variable name="refer-path" select="substring-after(.,'#')"/>
          <xsl:variable name="file-name-doc" select="document($file-name,/)"/>
          <xsl:if test="$file-name-doc and not($file-name-doc='')">
          <xsl:choose>
            <xsl:when test="contains($refer-path,'/')">
              <xsl:variable name="topic-id" select="substring-before($refer-path,'/')"/>
              <xsl:variable name="target-id" select="substring-after($refer-path,'/')"/>
              <xsl:variable name="href-value">
                <xsl:value-of select="generate-id($file-name-doc//*[contains(@class,' topic/topic ')][@id=$topic-id]//*[@id=$target-id]/@id)"/>
              </xsl:variable>
              <xsl:if test="not($href-value='')">
                <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="$href-value"/></xsl:attribute>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="href-value">
                <xsl:value-of select="generate-id($file-name-doc//*[contains(@class,' topic/topic ')][@id=$refer-path]/@id)"/>
              </xsl:variable>
              <xsl:if test="not($href-value='')">
                <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="$href-value"/></xsl:attribute>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="current-doc" select="document(.,/)"/>
          <xsl:if test="$current-doc and not($current-doc='')">
          <xsl:choose>
            <xsl:when test="$current-doc//*[contains(@class,' topic/topic ')]/@id">
              <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="generate-id($current-doc//*[contains(@class,' topic/topic ')][1]/@id)"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise><xsl:text>#</xsl:text><xsl:value-of select="generate-id($current-doc//*[contains(@class,' topic/topic ')][1])"/></xsl:otherwise>
          </xsl:choose>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:when>
    <xsl:otherwise>
      <xsl:attribute name="href">
        <xsl:value-of select="$file-path-new"/>
      </xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>  
</xsl:template>

<xsl:template name="get-file-path">
  <xsl:param name="src-file"/>  
  <xsl:if test="contains($src-file,'/')">
    <xsl:value-of select="substring-before($src-file,'/')"/>
    <xsl:text>/</xsl:text>
    <xsl:call-template name="get-file-path">
      <xsl:with-param name="src-file">
        <xsl:value-of select="substring-after($src-file,'/')"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:if>  
</xsl:template>

<xsl:template name="normalize-path">
        <xsl:param name="file-path" />
        <xsl:choose>       
            <xsl:when test="contains($file-path,'..')">
                <xsl:variable name="firstdir" select="substring-before($file-path, '/')" />
                <xsl:variable name="newpath" select="substring-after($file-path,'/')" />
                <xsl:choose>
                    <xsl:when test="$firstdir='..'">
                        <xsl:text>../</xsl:text>
                        <xsl:call-template name="normalize-path">
                            <xsl:with-param name="file-path">
                                <xsl:value-of select="$newpath"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="beforedotdot" select="substring-before($file-path,'/..')"></xsl:variable>
                        <xsl:variable name="beforedotdotparent" >
                            <xsl:call-template name="parent-path">
                                <xsl:with-param name="pathname" select="$beforedotdot" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="afterdotdot" select="substring-after($file-path,'../')"></xsl:variable>
                        <xsl:call-template name="normalize-path">
                            <xsl:with-param name="file-path">
                                <xsl:value-of select="concat($beforedotdotparent,$afterdotdot)"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                 </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$file-path"></xsl:value-of>
            </xsl:otherwise>    
        </xsl:choose>
 </xsl:template>
	
	<xsl:template name="parent-path">
        <xsl:param name="pathname" />
        <xsl:choose>
            <xsl:when test="contains($pathname, '/')">
                <xsl:value-of select="substring-before($pathname, '/')"/>
                <xsl:text>/</xsl:text>
                <xsl:call-template name="parent-path">
                    <xsl:with-param name="pathname" select="substring-after($pathname,'/')"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

  <xsl:template match="processing-instruction()">
    <xsl:copy></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
