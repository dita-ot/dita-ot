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

  <!-- The variable changeset contains a list of all change-item elements. -->
  <xsl:variable name="changeset" select="//*[contains (@class, ' relmgmt-d/change-item ')]"/>

  <!-- The variable topicsWithChangeItem contains a list of all topics containing change-item elements. -->
  <xsl:variable name="topicsWithChangeItem" select="//*[contains (@class, ' topic/topic ')][.//*[contains (@class, ' relmgmt-d/change-item ')]]"/>

  <!-- Keep date format in a variable -->
  <xsl:variable name="dateFormat">[MNn] [D], [Y]</xsl:variable>

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
  
  <xsl:template match="//*[contains(@class, ' bookmap/bookmeta ')]/*[contains(@class, ' topic/critdates ')]">

    <!-- Create a list of all book releases -->
    <xsl:variable name="bookReleaseDates" select="*[contains (@class, ' topic/created ')]/@date |
                                                  *[contains (@class, ' topic/revised ')]/@modified"/>
    <xsl:variable name="bookReleaseString">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'Book Release'"/>
      </xsl:call-template>
    </xsl:variable>
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

    <!--<xsl:for-each select="1 to (count($bookReleaseDates) - 1)">-->
    <xsl:for-each select="$bookReleaseDates ">
      <xsl:variable name="i" select="position() - 1"/>

      <!-- timeRangeStart and timeRangeEnd set the date range between two book releases -->
      <xsl:variable name="timeRangeStart" select="$bookReleaseDates[$i]"/>
      <xsl:variable name="timeRangeEnd" select="$bookReleaseDates[$i + 1]"/>
      <xsl:variable name="timeRangeStartAsDate" select="xs:date($timeRangeStart)"/>
      <xsl:variable name="timeRangeEndAsDate" select="xs:date($timeRangeEnd)"/>
      <fo:block>
        <fo:table xsl:use-attribute-sets="releaseManagementTable">
          <xsl:call-template name="selectAtts"/>
          <xsl:call-template name="globalAtts"/>
          <fo:table-body>
            <fo:table-row xsl:use-attribute-sets="releaseManagementTable.bookRelease.row">
              <fo:table-cell xsl:use-attribute-sets="releaseManagementTable.bookRelease.cell">
                <fo:block xsl:use-attribute-sets="releaseManagementTable.bookRelease.content">
                  <fo:block>
                    <xsl:value-of select="$bookReleaseString"/>
                    <xsl:value-of select="$colonSymbol"/>
                    <xsl:value-of select="$i + 1"/>
                  </fo:block>
                  <fo:block>
                    <xsl:value-of select="$bookReleaseDateString"/>
                    <xsl:value-of select="$colonSymbol"/>
                    <xsl:value-of select="$separator"/>
                    <xsl:value-of select="format-date($timeRangeEndAsDate, $dateFormat)"/>
                  </fo:block>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <!-- Iterate over all change-items -->
            <xsl:for-each select="$changeset">
              <xsl:variable name="j" select="position()"/>
              <xsl:variable name="currentChangeItem" select="$changeset[$j]"/>
              <xsl:variable name="currentChangeItemChangeCompleted" select="$changeset[$j]/*[contains (@class, ' relmgmt-d/change-completed ')]"/>
              <xsl:if test="$timeRangeStart != ''
                            and $currentChangeItemChangeCompleted != ''
                            and $timeRangeEnd">
                <xsl:variable name="currentChangeItemChangeCompletedAsDate" select="xs:date($currentChangeItemChangeCompleted)"/>
                <xsl:if test="($timeRangeStartAsDate &lt;= $currentChangeItemChangeCompletedAsDate) and ($currentChangeItemChangeCompletedAsDate &lt;= $timeRangeEndAsDate)">
                  <fo:table-row xsl:use-attribute-sets="releaseManagementTable.changeItem.row">
                    <fo:table-cell xsl:use-attribute-sets="releaseManagementTable.changeItem.cell">
                      <fo:block xsl:use-attribute-sets="releaseManagementTable.changeItem.content">
                        <xsl:apply-templates mode="relmgmt-table" select="$currentChangeItem">
                          <xsl:with-param name="change-item" select="$currentChangeItem"/>
                        </xsl:apply-templates>
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                </xsl:if>
              </xsl:if>
            </xsl:for-each>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' relmgmt-d/change-item ')]" mode="relmgmt-table">
    <xsl:param name="change-item"/>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-organization ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-organization"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-organization ')][1]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-person ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-person"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-person ')][1]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-revisionid ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-revisionid"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-revisionid ')]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-request-reference ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-request-reference"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-request-reference ')][1]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-started ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-started"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-started')][1]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-completed ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-completed"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-completed')][1]"/>
    </xsl:for-each>
    <xsl:for-each select="$change-item/*[contains (@class, ' relmgmt-d/change-summary ')][1]">
      <xsl:apply-templates mode="relmgmt-table-change-summary"
                           select="$change-item/*[contains (@class, ' relmgmt-d/change-summary')][1]"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-organization ')]" mode="relmgmt-table-change-organization">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Organization'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-person ')]" mode="relmgmt-table-change-person">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Person'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-revisionid ')]" mode="relmgmt-table-change-revisionid">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Revision ID'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-request-reference ')]" mode="relmgmt-table-change-request-reference">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Request'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="*[contains (@class, ' relmgmt-d/change-request-system ')]"/><xsl:text>-</xsl:text><xsl:value-of select="*[contains (@class, ' relmgmt-d/change-request-id')]"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-started ')]" mode="relmgmt-table-change-started">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Started'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="format-date(., $dateFormat)"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-completed ')]" mode="relmgmt-table-change-completed">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Completed'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="format-date(., $dateFormat)"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' relmgmt-d/change-summary ')]" mode="relmgmt-table-change-summary">
    <fo:block>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Change Summary'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'ColonSymbol'"/></xsl:call-template>
      <xsl:call-template name="getVariable"><xsl:with-param name="id" select="'figure-number-separator'"/></xsl:call-template>
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
