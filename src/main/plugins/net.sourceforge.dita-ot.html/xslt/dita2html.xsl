<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                xmlns:saxon="http://icl.com/saxon"
                xmlns:java="org.dita.dost.util.StringUtils"
                version="1.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="java dita-ot ditamsg">
  
  <xsl:import href="plugin:org.dita.xhtml:xsl/dita2xhtml.xsl"/>
  <!--xsl:import href="../../../xsl/map2htmtoc/map2htmtocImpl.xsl"/-->

  <xsl:param name="input.map.url"/>
  <xsl:param name="FILEREF" select="'file:'"/>

  <xsl:variable name="input.map" select="document($input.map.url)"/>

  <xsl:template match="*" mode="chapterBody">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <body>
      <xsl:call-template name="gen-style">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <xsl:if test="@outputclass">
        <xsl:attribute name="class">
          <xsl:value-of select="@outputclass"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="self::dita">
        <xsl:if test="*[contains(@class,' topic/topic ')][1]/@outputclass">
          <xsl:attribute name="class">
            <xsl:value-of select="*[contains(@class,' topic/topic ')][1]/@outputclass"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:apply-templates select="." mode="addAttributesToBody"/>
      <xsl:call-template name="setidaname"/>
      <div id="body">
        <xsl:call-template name="start-flagit">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="start-revflag">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="generateBreadcrumbs"/>
        <xsl:call-template name="gen-user-header"/>
        <xsl:call-template name="processHDR"/>
        <xsl:if test="$INDEXSHOW = 'yes'">
          <xsl:apply-templates select="/*/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/*[contains(@class,' topic/indexterm ')] |
                                       /dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/*[contains(@class,' topic/indexterm ')]"/>
        </xsl:if>
        <xsl:call-template name="gen-user-sidetoc"/>
        <div id="main">
          <xsl:apply-templates/>
          <xsl:call-template name="gen-endnotes"/>
        </div>
        <xsl:call-template name="gen-user-footer"/>
        <xsl:call-template name="processFTR"/>
        <xsl:call-template name="end-revflag">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <xsl:call-template name="end-flagit">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
        <a href="https://github.com/dita-ot/dita-ot"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>
      </div>
    </body>
    
  </xsl:template>

  <xsl:template match="*" mode="gen-user-sidetoc">
    <div id="nav">
      <xsl:apply-templates select="$input.map/*[contains(@class, ' map/map ')]"/>
    </div>
  </xsl:template>

  <xsl:template match="*" mode="gen-user-header">
    <div id="header">
      <h1>
        <a href="/">
          <xsl:for-each select="$input.map/*[contains(@class, ' map/map ')]">
            <xsl:choose>
              <xsl:when test="*[contains(@class, ' topic/title ')]">
                <xsl:apply-templates select="*[contains(@class, ' topic/title ')]/node()"/>
              </xsl:when>
              <xsl:when test="@title">
                <xsl:value-of select="@title"/>
              </xsl:when>
              <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
                <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]/node()"/>
              </xsl:when>
              <xsl:otherwise>DITA Open Toolkit</xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </a>
      </h1>
      <hr/>
    </div>
  </xsl:template>  

  <xsl:template match="*" mode="gen-user-footer">
    <div id="footer">
      <hr/>
    </div>
  </xsl:template>
  
  <xsl:template name="setanametag"/>

  <xsl:template match="*" mode="add-link-target-attribute"/>
  
  <!-- Navigation -->
  
  <xsl:template match="*[contains(@class, ' map/topicref ')][@toc='no' or @processing-role='resource-only']"
                priority="1000"/>
  
  <xsl:template match="*[contains(@class, ' map/map ')]">
    <xsl:param name="pathFromMaplist" select="$PATH2PROJ"/>
    <ul>
      <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
        <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
      </xsl:apply-templates>
    </ul>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' map/topicref ')]">
    <xsl:param name="pathFromMaplist"/>
    <xsl:variable name="title">
      <xsl:apply-templates select="." mode="get-navtitle"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$title and $title!=''">
        <li>
          <xsl:choose>
            <xsl:when test="@href and not(@href='')">
              <a>
                <xsl:attribute name="href">
                  <xsl:choose>
                    <xsl:when test="@copy-to and not(contains(@chunk, 'to-content')) and (not(@format) or @format = 'dita' or @format='ditamap' ) ">
                      <xsl:if test="not(@scope='external')">
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
                    <xsl:when test="not(@scope = 'external') and (not(@format) or @format = 'dita' or @format='ditamap')">
                      <xsl:if test="not(@scope='external')">
                        <xsl:value-of select="$pathFromMaplist"/>
                      </xsl:if>
                      <xsl:call-template name="replace-extension">
                        <xsl:with-param name="filename" select="@href"/>
                        <xsl:with-param name="extension" select="$OUTEXT"/>
                      </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:if test="not(@scope='external')">
                        <xsl:value-of select="$pathFromMaplist"/>
                      </xsl:if>
                      <xsl:value-of select="@href"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
                <!--
                <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
                  <xsl:attribute name="target">_blank</xsl:attribute>
                </xsl:if>
                -->
                <xsl:value-of select="$title"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <span>
                <xsl:value-of select="$title"/>
              </span>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="descendant::*[contains(@class, ' map/topicref ')][not(contains(@toc,'no'))][not(@processing-role='resource-only')]">
            <ul>
              <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
                <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
              </xsl:apply-templates>
            </ul>
          </xsl:if>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
          <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>
    
  <!--xsl:template match="*[contains(@class, ' map/topicref ')][@toc='no']">
    <xsl:param name="pathFromMaplist"/>
    <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
      <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
    </xsl:apply-templates>
  </xsl:template-->
  
  <xsl:template match="processing-instruction('workdir')" mode="get-work-dir">
    <xsl:value-of select="concat(., '/')"/>
  </xsl:template>  
  
  <xsl:template name="navtitle">
    <xsl:apply-templates select="." mode="get-navtitle"/>
  </xsl:template>
  
  <xsl:template match="*" mode="get-navtitle">
    <xsl:variable name="WORKDIR">
      <xsl:value-of select="$FILEREF"/>
      <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="*[contains(@class,'- map/topicmeta ')]/*[contains(@class, '- topic/navtitle ')]">
        <xsl:apply-templates 
          select="*[contains(@class,'- map/topicmeta ')]/*[contains(@class, '- topic/navtitle ')]" 
          mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:when test="not(*[contains(@class,'- map/topicmeta ')]/*[contains(@class, '- topic/navtitle ')]) and @navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
      <xsl:when test="@href and not(@href='') and 
                      not ((ancestor-or-self::*/@scope)[last()]='external') and
                      not ((ancestor-or-self::*/@scope)[last()]='peer') and
                      not ((ancestor-or-self::*/@type)[last()]='external') and
                      not ((ancestor-or-self::*/@type)[last()]='local')">
        <xsl:apply-templates select="." mode="getNavtitleFromTopic">
          <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
        <xsl:apply-templates 
          select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
          mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="@href and not(@href='')">
            <xsl:apply-templates select="." mode="ditamsg:could-not-retrieve-navtitle-using-fallback">
              <xsl:with-param name="target" select="@href"/>
              <xsl:with-param name="fallback" select="@href"/>
            </xsl:apply-templates>
            <xsl:value-of select="@href"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*" mode="getNavtitleFromTopic">
    <xsl:param name="WORKDIR"/>
    <xsl:variable name="FileWithPath">
      <xsl:choose>
        <xsl:when test="@copy-to and not(contains(@chunk, 'to-content'))">
          <xsl:value-of select="$WORKDIR"/><xsl:value-of select="@copy-to"/>
          <xsl:if test="not(contains(@copy-to, '#')) and contains(@href, '#')">
            <xsl:value-of select="concat('#', substring-after(@href, '#'))"/>
          </xsl:if>
        </xsl:when>
        <xsl:when test="contains(@href,'#')">
          <xsl:value-of select="$WORKDIR"/><xsl:value-of select="substring-before(@href,'#')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$WORKDIR"/><xsl:value-of select="@href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="TargetFile" select="document($FileWithPath,/)"/>
    <xsl:choose>
      <xsl:when test="not($TargetFile)">
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">  <!-- attempt to recover by using linktext -->
            <xsl:apply-templates
               select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
               mode="dita-ot:text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="ditamsg:missing-target-file-no-navtitle"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]">
        <xsl:apply-templates 
          select="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]"
          mode="dita-ot:text-only"/>
      </xsl:when>
      <!-- Second choice for navtitle: topic/title -->
      <xsl:when test="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
        <xsl:apply-templates 
          select="$TargetFile/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"
          mode="dita-ot:text-only"/>
      </xsl:when>
      <!-- This might be a combo article; modify the same queries: dita/topic/titlealts/navtitle -->
      <xsl:when test="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]">
        <xsl:apply-templates 
          select="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/navtitle ')]"
          mode="dita-ot:text-only"/>
      </xsl:when>
      <!-- Second choice: dita/topic/title -->
      <xsl:when test="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
        <xsl:apply-templates 
          select="$TargetFile/dita/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"
          mode="dita-ot:text-only"/>
      </xsl:when>
      <!-- Last choice: use the linktext specified within the topicref -->
      <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
        <xsl:apply-templates 
          select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"
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
    
  <xsl:template match="*[contains(@class, ' map/navref ')]"/>
  <xsl:template match="*[contains(@class, ' map/anchor ')]"/>
  <xsl:template match="*[contains(@class, ' map/reltable ')]"/>
  <xsl:template match="*[contains(@class, ' map/topicmeta ')]"/>
  <xsl:template match="*[contains(@class, ' mapgroup-d/keydef ')]"/>

</xsl:stylesheet>
