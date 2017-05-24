<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
                xmlns:opentopic="http://www.idiominc.com/opentopic"
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
                exclude-result-prefixes="opentopic opentopic-index dita2xslfo ot-placeholder xs"
                version="2.0">

  <!-- The variable change-items contains a list of all change-item elements that have a change-completed date. -->
  <xsl:variable name="change-items" select="//*[contains (@class, ' relmgmt-d/change-item ')][*[contains (@class, ' relmgmt-d/change-completed ')] != '']"/>

  <!-- Keep date format in a variable -->
  <xsl:variable name="dateFormat"><xsl:call-template name="getVariable"><xsl:with-param name="id" select="'#date-format'"/></xsl:call-template></xsl:variable>

  <!--   LOC   -->
  <xsl:template match="ot-placeholder:changelist" name="createChangeList">
    <xsl:if test="//*[contains(@class, ' relmgmt-d/change-historylist ')]">
      <fo:page-sequence master-reference="toc-sequence" xsl:use-attribute-sets="page-sequence.loc">
        <xsl:call-template name="insertTocStaticContents"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block start-indent="0in">
            <xsl:call-template name="createLOCHeader"/>
            <xsl:apply-templates select="//*[contains(@class, ' topic/critdates ')][1]"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </xsl:if>
  </xsl:template>

  <xsl:template name="createLOCHeader">
    <fo:block xsl:use-attribute-sets="__lotf__heading" id="{$id.lot}">
      <fo:marker marker-class-name="current-header">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'List of Changes'"/>
        </xsl:call-template>
      </fo:marker>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'List of Changes'"/>
      </xsl:call-template>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' bookmap/bookmeta ')]/*[contains(@class, ' topic/critdates ')]">

    <!-- Create a list of all book releases -->
    <xsl:variable name="bookReleaseDates" select="*[contains (@class, ' topic/created ')]/@date |
                                                  *[contains (@class, ' topic/revised ')]/@modified"/>
    <xsl:variable name="bookReleaseDateString">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'Book Release Date'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="colonSymbol">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'ColonSymbol'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="separator">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'figure-number-separator'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:for-each select="$bookReleaseDates">
      <xsl:variable name="previousDateIndex" select="position() - 1"/>
      <xsl:variable name="previousDate" select="xs:date($bookReleaseDates[$previousDateIndex])"/>
      <xsl:variable name="date" select="xs:date(.)"/>
      <fo:block>
        <fo:table xsl:use-attribute-sets="releaseManagementTable">
          <xsl:call-template name="selectAtts"/>
          <xsl:call-template name="globalAtts"/>
          <fo:table-body>
            <fo:table-row xsl:use-attribute-sets="tbody.row">
              <fo:table-cell xsl:use-attribute-sets="tbody.row.entry">
                <fo:block xsl:use-attribute-sets="tbody.row.entry__content">
                  <xsl:value-of select="$bookReleaseDateString"/>
                  <xsl:value-of select="$colonSymbol"/>
                  <xsl:value-of select="$separator"/>
                  <xsl:value-of select="format-date(., $dateFormat)"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <xsl:for-each select="$change-items">
              <xsl:variable name="change-completed" select="xs:date(*[contains (@class, ' relmgmt-d/change-completed ')])"/>
              <xsl:if test="($previousDate &lt;= $change-completed)
                          and ($change-completed &lt;= $date)">
                <fo:table-row xsl:use-attribute-sets="tbody.row">
                  <fo:table-cell xsl:use-attribute-sets="tbody.row.entry">
                    <fo:block xsl:use-attribute-sets="tbody.row.entry__content">
                      <xsl:apply-templates mode="relmgmt-table"/>
                    </fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </xsl:if>
            </xsl:for-each>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' relmgmt-d/change-item ')]" mode="relmgmt-table">
    <xsl:apply-templates mode="relmgmt-table"/>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-organization ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Organization'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-person ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Person'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-revisionid ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Revision ID'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-request-reference ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Request'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="*[contains (@class, ' relmgmt-d/change-request-system ')]"/><xsl:text>-</xsl:text><xsl:value-of select="*[contains (@class, ' relmgmt-d/change-request-id')]"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-started ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Started'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="format-date(., $dateFormat)"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-completed ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Completed'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="format-date(., $dateFormat)"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-summary ')]" mode="relmgmt-table">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Summary'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
