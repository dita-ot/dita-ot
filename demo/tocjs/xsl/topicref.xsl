<!-- 
  This file is part of the DITA Open Toolkit project hosted on
  Sourceforge.net. See the accompanying license.txt file for
  applicable licenses.
  
  (C) Copyright Shawn McKenzie, 2007. All Rights Reserved.
  *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="*[contains(@class, ' map/topicref ')]">
    <xsl:param name="parent"/>
    <xsl:param name="contentwin"/>

<!--    <xsl:message>
####### In map/topicref for <xsl:value-of select="@href"/>, $contentwin is set to: <xsl:value-of select="$contentwin"/>     
    </xsl:message>-->
    
    <xsl:variable name="apos">'</xsl:variable>
    <xsl:variable name="jsapos">\'</xsl:variable>
    <xsl:variable name="comma">,</xsl:variable>
    <xsl:variable name="empty_string" select="''"/>
    <xsl:variable name="quote">"</xsl:variable>
    <xsl:variable name="quotestring">\"</xsl:variable>
    <xsl:variable name="self" select="generate-id()"/>

    <xsl:if test="not(@toc='no')">
      <xsl:if test="@href">

        <xsl:text>var </xsl:text>
        <xsl:value-of select="concat('obj', $self)"/>
        <xsl:text> = { label: "</xsl:text>

        
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]">
            <!--<xsl:message> - testing navtitle - <xsl:value-of select="topicmeta/navtitle"/></xsl:message>-->
            <xsl:call-template name="replace-string">
              <xsl:with-param name="text">
                <xsl:value-of select="topicmeta/navtitle"/>
              </xsl:with-param>
              <xsl:with-param name="from" select="$quote"/>
              <xsl:with-param name="to" select="$quotestring"/>
            </xsl:call-template>
          </xsl:when>
          
          <xsl:when test="@navtitle">
            <!--<xsl:message> - testing2 navtitle - <xsl:value-of select="@navtitle"/></xsl:message>-->
            <xsl:call-template name="replace-string">
              <xsl:with-param name="text">
                <xsl:value-of select="@navtitle"/>
              </xsl:with-param>
              <xsl:with-param name="from" select="$quote"/>
              <xsl:with-param name="to" select="$quotestring"/>
            </xsl:call-template>
          </xsl:when>
          
          <xsl:otherwise>
            <!--<xsl:message> - testing3 navtitle - <xsl:value-of select="@navtitle"/></xsl:message>-->
            <xsl:call-template name="replace-string">
              <xsl:with-param name="text">
                <xsl:value-of select="@navtitle"/>
              </xsl:with-param>
              <xsl:with-param name="from" select="$quote"/>
              <xsl:with-param name="to" select="$quotestring"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        
        
<!--        <xsl:if test="@navtitle">
          
          <xsl:call-template name="replace-string">
            <xsl:with-param name="text">
              <xsl:value-of select="@navtitle"/>
            </xsl:with-param>
            <xsl:with-param name="from" select="$quote"/>
            <xsl:with-param name="to" select="$quotestring"/>
          </xsl:call-template>
        </xsl:if>

        <xsl:if test="topicmeta/navtitle">
          
          <xsl:call-template name="replace-string">
            <xsl:with-param name="text">
              <xsl:value-of select="topicmeta/navtitle"/>
            </xsl:with-param>
            <xsl:with-param name="from" select="$quote"/>
            <xsl:with-param name="to" select="$quotestring"/>
          </xsl:call-template>
        </xsl:if>-->


        <xsl:text>", href:"</xsl:text>
        <xsl:call-template name="gethref">
          <xsl:with-param name="ditahref" select="@href"/>
        </xsl:call-template>
        <xsl:text>", target:"</xsl:text><xsl:value-of select="$contentwin"/><xsl:text>" };
    </xsl:text>

        <xsl:text>var </xsl:text>
        <xsl:value-of select="$self"/>
        <xsl:text> = new YAHOO.widget.TextNode(</xsl:text>
        <xsl:value-of select="concat('obj', $self)"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$parent"/>
        <xsl:text>, false);</xsl:text>

        <xsl:apply-templates>
          <xsl:with-param name="parent" select="$self"/>
          <xsl:with-param name="contentwin" select="$contentwin"/>
        </xsl:apply-templates>
      </xsl:if>

      
      <xsl:if test="not(@href)">
        
        <!-- I think this next if never gets called 
        <xsl:if test="not(@navtitle) and not(topicmeta/navtitle)">
          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$parent"/>
            <xsl:with-param name="contentwin" select="$contentwin"/>
          </xsl:apply-templates>
        </xsl:if>
        -->
        
        <xsl:if test="@navtitle">
          <xsl:text>var </xsl:text>
          <xsl:value-of select="$self"/>
          <xsl:text> = new YAHOO.widget.TextNode("</xsl:text>
          <xsl:value-of select="@navtitle"/>
          <xsl:text>", </xsl:text>
          <xsl:value-of select="$parent"/>
          <xsl:text>, false);</xsl:text>

          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$self"/>
            <xsl:with-param name="contentwin" select="$contentwin"/>
          </xsl:apply-templates>
        </xsl:if>

        <!-- for 1.5 toolkit and later -->
        <xsl:if test="topicmeta/navtitle">
          <xsl:text>var </xsl:text>
          <xsl:value-of select="$self"/>
          <xsl:text> = new YAHOO.widget.TextNode("</xsl:text>
          <xsl:value-of select="topicmeta/navtitle"/>
          <xsl:text>", </xsl:text>
          <xsl:value-of select="$parent"/>
          <xsl:text>, false);</xsl:text>
          
          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$self"/>
            <xsl:with-param name="contentwin" select="$contentwin"/>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:if>

      <!-- Need to deal with topicgroup elements that are used for navigation purposes -->
      <xsl:if test="topicgroup">
        <xsl:for-each select="*[contains(@class,' map/topicref ')]">
          <xsl:apply-templates>
            <xsl:with-param name="parent" select="$self"/>
            <xsl:with-param name="contentwin" select="$contentwin"/>
          </xsl:apply-templates>
        </xsl:for-each>
      </xsl:if>
      
    </xsl:if>
  </xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="from"/>
    <xsl:param name="to"/>

    <xsl:choose>
      <xsl:when test="contains($text, $from)">

        <xsl:variable name="before" select="substring-before($text, $from)"/>
        <xsl:variable name="after" select="substring-after($text, $from)"/>
        <xsl:variable name="prefix" select="concat($before, $to)"/>

        <xsl:value-of select="$before"/>
        <xsl:value-of select="$to"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="$after"/>
          <xsl:with-param name="from" select="$from"/>
          <xsl:with-param name="to" select="$to"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
