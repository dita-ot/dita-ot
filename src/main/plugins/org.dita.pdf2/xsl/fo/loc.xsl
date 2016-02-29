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

  <!-- The variables changeset contains a list of all change-item elements. -->
  <!--<xsl:variable name="changeset">
    <xsl:for-each select="//*[contains (@class, ' relmgmt-d/change-item ')]">
      <xsl:value-of select="."/>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:if test="not(@id)">
          <xsl:attribute name="id">
            <xsl:call-template name="get-id"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:copy>
    </xsl:for-each>
  </xsl:variable>-->
  <xsl:variable name="changeset" select="//*[contains (@class, ' relmgmt-d/change-item ')]"/>

  <!--   LOC   -->

  <xsl:template match="ot-placeholder:changelist" name="createChangeList">
    <xsl:if test="//*[contains(@class, ' relmgmt-d/change-historylist ')]">
      <!-- TODO: This is the standard processing, without the <critdates> element. -->
      <!--exists tables with titles-->
      <fo:page-sequence master-reference="toc-sequence" xsl:use-attribute-sets="page-sequence.loc">
        <xsl:call-template name="insertTocStaticContents"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block start-indent="0in">

            <xsl:call-template name="createLOCHeader"/>


            <xsl:text>topic/title of concepts that have a change-item:</xsl:text>
            <!-- Select topic/title of all topic that have a change-item -->
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')][.//*[contains (@class, ' relmgmt-d/change-item ')]]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>topic/title of all change-items:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>...:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' relmgmt-d/change-item ')][child::*[contains(@class, ' relmgmt-d/change-completed ' )]]" mode="list.of.changes"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>

      <!-- TODO: This uses the <critdates> element. -->
      <fo:page-sequence master-reference="toc-sequence" xsl:use-attribute-sets="page-sequence.loc">
        <xsl:call-template name="insertTocStaticContents"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block start-indent="0in">

            <xsl:call-template name="createLOCHeader"/>

            <xsl:text>critdates</xsl:text>
            <xsl:apply-templates select="//*[contains(@class, ' topic/critdates ')]"/>

            <xsl:text>topic/title of concepts that have a change-item:</xsl:text>
            <!-- Select topic/title of all topic that have a change-item -->
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')][.//*[contains (@class, ' relmgmt-d/change-item ')]]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>topic/title of all change-items:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>...:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' relmgmt-d/change-item ')][child::*[contains(@class, ' relmgmt-d/change-completed ' )]]" mode="list.of.changes"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </xsl:if>

  </xsl:template>

  <xsl:template name="createLOCHeader">
    <fo:block>
      <xsl:text>HELLO WORLD X</xsl:text>
    </fo:block>
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
  
  <xsl:template match="//*[contains(@class, ' topic/critdates ')]">
    <xsl:text>createBookRelease xx: </xsl:text>

    <!-- Create a list of all book releases -->
    <xsl:variable name="bookReleaseDates" select="*[contains (@class, ' topic/created ')]/@date,
                                                  *[contains (@class, ' topic/revised ')]/@modified"/>


    <xsl:for-each select="1 to (count($bookReleaseDates) - 1)">
      <xsl:variable name="i" select="position()"/>
      <xsl:variable name="timeRangeStart" select="$bookReleaseDates[$i]"/>
      <xsl:variable name="timeRangeEnd" select="$bookReleaseDates[$i + 1]"/>

      <fo:block><xsl:text>Book Release: </xsl:text><xsl:value-of select="$i"/></fo:block>
      <!--<fo:block>
        <xsl:text>Release Range Start: </xsl:text><xsl:value-of select="$timeRangeStart"/>
        <xsl:text>, Release Range End: </xsl:text><xsl:value-of select="$timeRangeEnd"/>
      </fo:block>-->

<!--      <xsl:for-each select="//*[contains (@class, ' relmgmt-d/change-item ')]">
        <xsl:call-template name="getChangeListEntry">
          <xsl:with-param name="timeRangeStart" select="$timeRangeStart"/>
          <xsl:with-param name="timeRangeEnd" select="$timeRangeEnd"/>
        </xsl:call-template>
      </xsl:for-each>-->


      <fo:block>
        <xsl:for-each select="1 to (count($changeset))">
          <xsl:variable name="j" select="position()"/>
          <xsl:variable name="currentChangeItem" select="$changeset[$j]"/>
          <xsl:variable name="currentChangeItemChangeCompleted" select="$changeset[$j]/*[contains (@class, ' relmgmt-d/change-completed ')]"/>

<!--          <fo:block>
            <xsl:text>timeRangeStart: </xsl:text><xsl:value-of select="$timeRangeStart"/>
          </fo:block>
          <fo:block>
            <xsl:text>currentChangeItemChangeCompleted: </xsl:text><xsl:value-of select="$currentChangeItemChangeCompleted"/>
          </fo:block>
          <fo:block>
            <xsl:text>timeRangeEnd: </xsl:text><xsl:value-of select="$timeRangeEnd"/>
          </fo:block>-->

          <xsl:choose>
            <!-- Check if all date strings are not null empty -->
            <xsl:when test="$timeRangeStart != ''
                            and $currentChangeItemChangeCompleted != ''
                            and $timeRangeEnd">
              <!-- Convert strings to dates -->
              <fo:block><xsl:text>----- </xsl:text></fo:block>
              <xsl:variable name="timeRangeStartAsDate" select="xs:date($timeRangeStart)"/>
              <xsl:variable name="currentChangeItemChangeCompletedAsDate" select="xs:date($currentChangeItemChangeCompleted)"/>
              <xsl:variable name="timeRangeEndAsDate" select="xs:date($timeRangeEnd)"/>
              <fo:block><xsl:text>timeRangeStartAsDate: </xsl:text><xsl:value-of select="$timeRangeStartAsDate"/></fo:block>
              <fo:block><xsl:text>currentChangeItemChangeCompletedAsDate: </xsl:text><xsl:value-of select="$currentChangeItemChangeCompletedAsDate"/></fo:block>
              <fo:block><xsl:text>timeRangeEndAsDate: </xsl:text><xsl:value-of select="$timeRangeEndAsDate"/></fo:block>
              <fo:block><xsl:text>----- </xsl:text></fo:block>

              <xsl:choose>
                <xsl:when test="($timeRangeStartAsDate &lt; $currentChangeItemChangeCompletedAsDate)
                                and ($currentChangeItemChangeCompletedAsDate &lt; $timeRangeEndAsDate)">
                  <fo:block>
                    <xsl:text>Processed item:</xsl:text>
                  </fo:block>
                  <fo:block>
                    <xsl:value-of select="$currentChangeItem"/>
                    <xsl:value-of select="$changeset[$j]"/>
                    <xsl:value-of select="."/>
                  </fo:block>
                </xsl:when>
              </xsl:choose>
            </xsl:when>
          </xsl:choose>
          <!--
            <fo:block>
              <xsl:text>change-completed1: </xsl:text><xsl:value-of />
            </fo:block>

            <xsl:text>$currentChangeItem: </xsl:text><xsl:value-of select="$currentChangeItem"/>
            <xsl:text>change organization: </xsl:text><xsl:value-of select="$currentChangeItem/*[contains (@class, ' relmgmt-d/change-organization ')]"/>
          -->
        </xsl:for-each>
      </fo:block>
      <!--
        <change-person>Stefan</change-person>
        <change-organization>Doctales</change-organization>
        <change-revisionid>001</change-revisionid>
        <change-request-reference>
          <change-request-system>Jira</change-request-system>
          <change-request-id>0001</change-request-id>
        </change-request-reference>
        <change-started>2016-01-01</change-started>
        <change-completed>2016-01-02</change-completed>
        <change-summary>Something has changed</change-summary>
      -->

    </xsl:for-each>
    <fo:block>END</fo:block>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' bookmap/changelist ')]" mode="list.of.changes">
    <fo:block><xsl:text>HELLO WORLD 1</xsl:text></fo:block>
  </xsl:template>


  <xsl:template name="getChangeListEntry">
    <xsl:param name="timeRangeStart"/>
    <xsl:param name="timeRangeEnd"/>
    <fo:block>
      <xsl:text>timeRangeStart is: </xsl:text><xsl:value-of select="$timeRangeStart"/>
      <xsl:text>timeRangeEnd is: </xsl:text><xsl:value-of select="$timeRangeEnd"/>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
