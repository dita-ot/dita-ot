<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg" version="1.0"
                exclude-result-prefixes="dita-ot ditamsg">

  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-textonly.xsl"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:param name="OUTEXT" select="'.html'"/>
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
  </xsl:param>
  <xsl:param name="PATH2PROJ">
    <xsl:apply-templates select="/processing-instruction('path2project-uri')[1]" mode="get-path2project"/>
  </xsl:param>
  
  <!-- Deprecated -->
  <xsl:template match="processing-instruction('workdir')" mode="get-work-dir">
    <xsl:value-of select="concat(., '/')"/>
  </xsl:template>  

  <xsl:template match="*[contains(@class, ' map/map ')]" mode="toc">
    <xsl:param name="pathFromMaplist" select="$PATH2PROJ"/>
    <xsl:if test="descendant::*[contains(@class, ' map/topicref ')][not(@toc = 'no')][not(@processing-role = 'resource-only')]">
      <ul>
        <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]" mode="toc">
          <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
        </xsl:apply-templates>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicref ')]
                        [not(@toc = 'no')]
                        [not(@processing-role = 'resource-only')]"
                mode="toc">
    <xsl:param name="pathFromMaplist"/>
    <xsl:variable name="title">
      <xsl:apply-templates select="." mode="get-navtitle"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="normalize-space($title)">
        <li>
          <xsl:choose>
            <!-- If there is a reference to a DITA or HTML file, and it is not external: -->
            <xsl:when test="normalize-space(@href)">
              <xsl:element name="a">
                <xsl:attribute name="href">
                  <xsl:choose>
                    <xsl:when test="@copy-to and not(contains(@chunk, 'to-content')) and 
                                    (not(@format) or @format = 'dita' or @format = 'ditamap') ">
                      <xsl:if test="not(@scope = 'external')">
                        <xsl:value-of select="$pathFromMaplist"/>
                      </xsl:if>
                      <xsl:call-template name="replace-extension">
                        <xsl:with-param name="filename" select="@copy-to"/>
                        <xsl:with-param name="extension" select="$OUTEXT"/>
                      </xsl:call-template>
                      <xsl:if test="not(contains(@copy-to, '#')) and contains(@href, '#')">
                        <xsl:value-of select="concat('#', substring-after(@href, '#'))"/>
                      </xsl:if>
                    </xsl:when>
                    <xsl:when test="not(@scope = 'external') and (not(@format) or @format = 'dita' or @format = 'ditamap')">
                      <xsl:value-of select="$pathFromMaplist"/>
                      <xsl:call-template name="replace-extension">
                        <xsl:with-param name="filename" select="@href"/>
                        <xsl:with-param name="extension" select="$OUTEXT"/>
                      </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise><!-- If non-DITA, keep the href as-is -->
                      <xsl:if test="not(@scope = 'external')">
                        <xsl:value-of select="$pathFromMaplist"/>
                      </xsl:if>
                      <xsl:value-of select="@href"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
                <xsl:if test="@scope = 'external' or not(not(@format) or @format = 'dita' or @format = 'ditamap')">
                  <xsl:attribute name="target">_blank</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="$title"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$title"/>
            </xsl:otherwise>
          </xsl:choose>
          <!-- If there are any children that should be in the TOC, process them -->
          <xsl:if test="descendant::*[contains(@class, ' map/topicref ')]
                                     [not(@toc = 'no')]
                                     [not(@processing-role = 'resource-only')]">
            <ul>
              <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]" mode="toc">
                <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
              </xsl:apply-templates>
            </ul>
          </xsl:if>
        </li>
      </xsl:when>
      <xsl:otherwise><!-- if it is an empty topicref -->
        <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]" mode="toc">
          <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- If toc=no, but a child has toc=yes, that child should bubble up to the top -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]
                        [@toc = 'no']
                        [not(@processing-role = 'resource-only')]"
                mode="toc">
    <xsl:param name="pathFromMaplist"/>
    <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]" mode="toc">
      <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!--
  <xsl:template match="*[contains(@class, ' map/topicref ')][@toc = 'no' or @processing-role = 'resource-only']"
                mode="toc" priority="1000"/>
  -->
  
  <xsl:template match="*" mode="toc" priority="-1"/>

  <xsl:template match="*" mode="get-navtitle">
    <xsl:choose>
      <!-- If navtitle is specified -->
      <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]">
        <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:when test="@navtitle">
        <xsl:value-of select="@navtitle"/>
      </xsl:when>
      <!-- If this references a DITA file, try to open the file and get the title -->
      <!--
      <xsl:when test="normalize-space(@href) and 
                      not(ancestor-or-self::*[@scope][1][@scope = 'external' or @scope = 'peer'])">
        <xsl:apply-templates select="." mode="getNavtitleFromTopic">
          <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
        </xsl:apply-templates>
      </xsl:when>
      -->
      <!-- If there is no title and none can be retrieved, check for <linktext> -->
      <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
        <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <!-- No local title, and not targeting a DITA file. Could be just a container setting
           metadata, or a file reference with no title. Issue message for the second case. -->
      <xsl:otherwise>
        <xsl:if test="normalize-space(@href)">
          <xsl:apply-templates select="." mode="ditamsg:could-not-retrieve-navtitle-using-fallback">
            <xsl:with-param name="target" select="@href"/>
            <xsl:with-param name="fallback" select="@href"/>
          </xsl:apply-templates>
          <xsl:value-of select="@href"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  <xsl:template match="*" mode="getNavtitleFromTopic">
    <xsl:param name="WORKDIR"/>
    <!- - Need to worry about targeting a nested topic? Not for now. - ->
    <xsl:variable name="FileWithPath">
      <xsl:choose>
        <xsl:when test="@copy-to and not(contains(@chunk, 'to-content'))">
          <xsl:value-of select="$WORKDIR"/>
          <xsl:value-of select="@copy-to"/>
          <xsl:if test="not(contains(@copy-to, '#')) and contains(@href, '#')">
            <xsl:value-of select="concat('#', substring-after(@href, '#'))"/>
          </xsl:if>
        </xsl:when>
        <xsl:when test="contains(@href,'#')">
          <xsl:value-of select="$WORKDIR"/>
          <xsl:value-of select="substring-before(@href,'#')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$WORKDIR"/>
          <xsl:value-of select="@href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="TargetFile" select="document($FileWithPath,/)"/>
    <xsl:choose>
      <xsl:when test="not($TargetFile)">
        <!- - DITA file does not exist - ->
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
            <!- - attempt to recover by using linktext - ->
            <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
                                 mode="dita-ot:text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="ditamsg:missing-target-file-no-navtitle"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!- - First choice for navtitle: topic/titlealts/navtitle - ->
      <xsl:when test="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]">
        <xsl:apply-templates select="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <!- - Second choice for navtitle: topic/title - ->
      <xsl:when test="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
        <xsl:apply-templates select="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <!- - This might be a combo article; modify the same queries: dita/topic/titlealts/navtitle - ->
      <xsl:when test="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]">
        <xsl:apply-templates select="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <!- - Second choice: dita/topic/title - ->
      <xsl:when test="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
        <xsl:apply-templates select="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <!- - Last choice: use the linktext specified within the topicref - ->
      <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
        <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
                             mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:could-not-retrieve-navtitle-using-fallback">
          <xsl:with-param name="target" select="$FileWithPath"/>
          <xsl:with-param name="fallback" select="'***'"/>
        </xsl:apply-templates>
        <xsl:text>***</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  -->

  <xsl:template match="*" mode="ditamsg:missing-target-file-no-navtitle">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">008</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" mode="ditamsg:could-not-retrieve-navtitle-using-fallback">
    <xsl:param name="target"/>
    <xsl:param name="fallback"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">009</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$target"/>;%2=<xsl:value-of select="$fallback"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
