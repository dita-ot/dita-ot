<?xml version="1.0" encoding="UTF-8"?>

<!-- 20170503 SCH: Add support for troubleshooting elements. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:opentopic="http://www.idiominc.com/opentopic"
  xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
  xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
  xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
  xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
  exclude-result-prefixes="ot-placeholder opentopic opentopic-index opentopic-func dita2xslfo xs"
  version="2.0">

  <!-- hazardstatement =================================================== -->

  <xsl:template match="*[contains(@class,' hazard-d/hazardstatement ')]">
    <fo:table xsl:use-attribute-sets="hazardstatement">
      <xsl:call-template name="commonattributes"/>

      <fo:table-body>
        <fo:table-row>
          <fo:table-cell width="100px">
            <xsl:apply-templates select="./*[contains(@class,' hazard-d/hazardsymbol ')]"/>
          </fo:table-cell>
          <fo:table-cell text-align="left">
            <xsl:call-template name="hazardStatementHead" />
            <xsl:apply-templates select="*[contains(@class,' hazard-d/messagepanel ')]" />
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template name="hazardStatementHead">
    <xsl:choose>
      <xsl:when test="@type eq 'note'">
        <fo:block xsl:use-attribute-sets="hazardstatement-note-head" >
          <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'tip'">
        <fo:block xsl:use-attribute-sets="hazardstatement-tip-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'fastpath'">
        <fo:block xsl:use-attribute-sets="hazardstatement-fastpath-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'restriction'">
        <fo:block xsl:use-attribute-sets="hazardstatement-restriction-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'important'">
        <fo:block xsl:use-attribute-sets="hazardstatement-important-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'remember'">
        <fo:block xsl:use-attribute-sets="hazardstatement-remember-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'attention'">
        <fo:block xsl:use-attribute-sets="hazardstatement-attention-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'caution'">
        <fo:block xsl:use-attribute-sets="hazardstatement-caution-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'notice'">
        <fo:block xsl:use-attribute-sets="hazardstatement-notice-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'danger'">
        <fo:block xsl:use-attribute-sets="hazardstatement-danger-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:when test="@type eq 'warning'">
        <fo:block xsl:use-attribute-sets="hazardstatement-warning-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:when>

      <xsl:otherwise>
        <fo:block xsl:use-attribute-sets="hazardstatement-other-head">
            <xsl:call-template name="hazardStatementHeadContent" />
        </fo:block>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' hazard-d/hazardstatement ')]" mode="setIconImagePath">
    <xsl:variable name="noteType" as="xs:string">
      <xsl:choose>
        <xsl:when test="@type">
          <xsl:value-of select="@type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'note'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="getVariable">
      <xsl:with-param name="id" select="concat($noteType, ' Note Image Path')"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="hazardStatementHeadContent">
      <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="exists(@type)">
          <xsl:value-of select="@type"/>
        </xsl:when>
        <xsl:otherwise>other</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="iconImagePath">
      <xsl:apply-templates select="." mode="setIconImagePath"/>
    </xsl:variable>
    <fo:inline xsl:use-attribute-sets="hazardstatement-head">
    <xsl:if test="not($iconImagePath = '')">
      <fo:external-graphic src="url('{concat($artworkPrefix, $iconImagePath)}')" xsl:use-attribute-sets="hazardstatement-head-icon"/>
    </xsl:if>
    <xsl:choose>
        <xsl:when test="@type='note' or not(@type)">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Note'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='notice'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Notice'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='tip'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Tip'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='fastpath'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Fastpath'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='restriction'">
         <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Restriction'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='important'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Important'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='remember'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Remember'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='attention'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Attention'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='caution'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Caution'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='danger'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Danger'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='warning'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Warning'"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="@type='trouble'">
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Trouble'"/>
            </xsl:call-template>
        </xsl:when>                  
        <xsl:when test="@type='other'">
         <xsl:choose>
              <xsl:when test="@othertype">
                <xsl:value-of select="@othertype"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>[</xsl:text>
                <xsl:value-of select="@type"/>
                <xsl:text>]</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
        </xsl:when>
      </xsl:choose>
     <!-- <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'#note-separator'"/>
      </xsl:call-template>
    <xsl:text>  </xsl:text>-->
  </fo:inline>
  </xsl:template>

  <!-- hazardsymbol ====================================================== -->

  <xsl:template match="*[contains(@class,' hazard-d/hazardsymbol ')]">
    <fo:block text-align="center" display-align="center">
      <xsl:variable name="href">
        <xsl:choose>
          <xsl:when test="(@scope = 'external' or opentopic-func:isAbsolute(@href))">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($input.dir.url, @href)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <fo:external-graphic src="url('{$href}')" vertical-align="middle" display-align="center" text-align="left">
        <xsl:attribute name="content-height">scale-to-fit</xsl:attribute>
        <xsl:attribute name="height">75px</xsl:attribute>
        <xsl:attribute name="width">75px</xsl:attribute>
        <xsl:attribute name="content-width">scale-to-fit</xsl:attribute>
        <xsl:attribute name="content-width">scale-to-fit</xsl:attribute>
      </fo:external-graphic>
    </fo:block>
  </xsl:template>

  <!-- messagepanel ====================================================== -->

  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]">
    <fo:block xsl:use-attribute-sets="messagepanel">
          <xsl:call-template name="commonattributes"/>
          <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <!-- typeofhazard ====================================================== -->

  <xsl:template match="*[contains(@class,' hazard-d/typeofhazard ')]">
    <fo:block xsl:use-attribute-sets="typeofhazard">
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <!-- consequence ======================================================= -->

  <xsl:template match="*[contains(@class,' hazard-d/consequence ')]">
    <fo:block xsl:use-attribute-sets="consequence">
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <!-- howtoavoid ======================================================== -->

  <xsl:template match="*[contains(@class,' hazard-d/howtoavoid ')]">
    <fo:block xsl:use-attribute-sets="howtoavoid">
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>