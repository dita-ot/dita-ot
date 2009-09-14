<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>



<!-- Match a tutorial-style topicref. Create all of the hierarchy links associated with the topicref. -->
<xsl:template match="*[@href][not(@linking='none' or @linking='targetonly' or @scope='external' or @scope='peer' or @type='external')][not(@format) or @format='dita' or @format='DITA'][not(@href='')]
                      [@type='tutorial' or @type='tutorialIntro' or @type='tutorialModule' or @type='tutorialLesson' or @type='tutorialSummary']">
  <!-- If using a maplist, and the map is in a different directory, the extra path must be added -->
  <xsl:param name="pathFromMaplist"/>
  <!-- Href that points from this map to the topic this href references. -->
  <xsl:variable name="use-href">
    <xsl:choose>
      <xsl:when test="@copy-to and contains(@copy-to,'.dita')">
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="originalLink"><xsl:value-of select="@copy-to"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="originalLink"><xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <!-- Path from the original maplist (or from this map) to the HREF target. -->
  <!-- P018249: updated to simplify the href, to make generated links simpler -->
  <xsl:variable name="simpleHrefFromOriginalMapOrMaplist">
    <xsl:call-template name="simplifyLink">
      <xsl:with-param name="originalLink">
        <xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="$use-href"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!-- Path from the topic back to the map's directory (with map): for ref/abc.dita, will be "../" -->
  <!-- P018249: pass pathFromMaplist in case the directory has changed -->
  <xsl:variable name="pathBackToMapDirectory">
    <xsl:call-template name="pathBackToMapDirectory">
      <xsl:with-param name="path"><xsl:value-of select="$use-href"/></xsl:with-param>
      <xsl:with-param name="pathFromMaplist"><xsl:value-of select="$pathFromMaplist"/></xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <!--<xsl:message>******************************************</xsl:message>
  <xsl:message> In <xsl:value-of select="@href"/>
  <xsl:message>pathFromMaplist is <xsl:value-of select="$pathFromMaplist"/></xsl:message>
  <xsl:message>use-href is <xsl:value-of select="$use-href"/></xsl:message>
  <xsl:message>simpleHrefFromOriginalMapOrMaplist is <xsl:value-of select="$simpleHrefFromOriginalMapOrMaplist"/></xsl:message>
  <xsl:message>pathBackToMapDirectory is <xsl:value-of select="$pathBackToMapDirectory"/></xsl:message>-->

  <!-- If going to print, and @print=no, do not create links for this topicref -->
  <xsl:if test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no')">
    <maplinks href="{$simpleHrefFromOriginalMapOrMaplist}">
      <linkpool class="- topic/linkpool ">
        <xsl:if test="@xtrf">
          <xsl:attribute name="xtrf"><xsl:value-of select="@xtrf"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@xtrc">
          <xsl:attribute name="xtrc"><xsl:value-of select="@xtrc"/></xsl:attribute>
        </xsl:if>

        <xsl:if test="/*[@id]">
          <xsl:attribute name="mapkeyref"><xsl:value-of select="/*/@id"/></xsl:attribute>
        </xsl:if>

        <!--parent-->
        <xsl:apply-templates mode="link" select="ancestor::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][1]">
          <xsl:with-param name="role">parent</xsl:with-param>
          <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
          <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>


        <!--prereqs - preceding with importance=required and in a sequence, but leaving the immediately previous one alone to avoid duplication with prev/next generation-->
        <xsl:if test="parent::*[@collection-type='sequence']">
          <xsl:apply-templates mode="link" select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][position()>1][@importance='required']">
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </xsl:if>

        <!--family-->
        <xsl:if test="parent::*[@collection-type='family']">
          <xsl:apply-templates mode="link" select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">sibling</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
          <xsl:apply-templates mode="link" select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">sibling</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </xsl:if>

        <!--next/prev-->
        <xsl:if test="parent::*[@collection-type='sequence']">
          <xsl:apply-templates mode="link" select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][1]">
            <xsl:with-param name="role">previous</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
          <xsl:apply-templates mode="link" select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][1]">
            <xsl:with-param name="role">next</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </xsl:if>

        <!--children-->
<!--???to do: shd be linking to appropriate descendants, not just children - ie grandchildren of eg topicgroup (non-href/non-title topicrefs) children-->
        <xsl:if test="child::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
          <linkpool class="- topic/linkpool ">
            <xsl:if test="@xtrf">
              <xsl:attribute name="xtrf"><xsl:value-of select="@xtrf"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@xtrc">
              <xsl:attribute name="xtrc"><xsl:value-of select="@xtrc"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="@collection-type">
              <xsl:attribute name="collection-type"><xsl:value-of select="@collection-type"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="link" select="*[@href][not(@linking='none')][not(@linking='sourceonly')]">
              <xsl:with-param name="role">child</xsl:with-param>
              <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
              <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
          </linkpool>
        </xsl:if>

        <!--friends-->
        <xsl:if test="ancestor::*[contains(@class, ' map/relcell ')]">
          <xsl:apply-templates mode="link" select="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">friend</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
          <xsl:apply-templates mode="link" select="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">friend</xsl:with-param>
            <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </xsl:if>

<!-- =============================================================== -->
<!-- TUTORIAL SPEC :: Begin change ================================ -->

        <xsl:if test="@type='tutorial' or @type='tutorialIntro' or @type='tutorialModule' or @type='tutorialLesson' or @type='tutorialSummary' or @type='authorInfo'">
          <xsl:if test="not(ancestor::*[contains(@class, ' map/reltable ')])">
            <!-- Uncertain why [last()] in the following test does not seem to work with all XSLT processors:
                 select="preceding::*|ancestor::*)[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][last()]"
                 Workaround is the for-each, with a test for last -->
            <xsl:for-each select="(preceding::*|ancestor::*)[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
              <xsl:if test="position()=last()">
                <xsl:apply-templates mode="link" select=".">
                  <xsl:with-param name="role">previous</xsl:with-param>
                  <xsl:with-param name="overrideTemplate">yes</xsl:with-param>
                  <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
                </xsl:apply-templates>
              </xsl:if>
            </xsl:for-each>
            <xsl:apply-templates mode="link" select="(following::*|descendant::*)[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][1]">
              <xsl:with-param name="role">next</xsl:with-param>
              <xsl:with-param name="overrideTemplate">yes</xsl:with-param>
              <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:if>

        <xsl:if test="@type='tutorial' or @type='tutorialIntro' or   @type='tutorialModule' or @type='tutorialLesson' or @type='tutorialSummary' or @type='authorInfo'">
          <xsl:apply-templates mode="link" select="ancestor::*[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">tutbreadcrumb</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </xsl:if>

        <xsl:if test="@type='tutorialModule'">
          <xsl:apply-templates mode="link" select="*[@href][not(@linking='none')][not(@linking='sourceonly')]">
            <xsl:with-param name="role">tutmodulelesson</xsl:with-param>
              <!--<xsl:with-param name="pathBackToMapDirectory" select="concat($pathBackToMapDirectory,$pathFromMaplist)"/>-->
              <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
        </xsl:if>

        <xsl:if test="@type='tutorialIntro'">
          <xsl:choose>
            <xsl:when test="(following::*|descendant::*)[contains(@class, ' map/topicref ')][@type='tutorialModule']">
              <xsl:apply-templates mode="link" select="(following::*|descendant::*)[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][@type='tutorialModule']">
                <xsl:with-param name="role">tutmodulelesson</xsl:with-param>
                <xsl:with-param name="overrideTemplate">yes</xsl:with-param>
                <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="(following::*|descendant::*)[contains(@class, ' map/topicref ')][@type='tutorialLesson']">
              <xsl:apply-templates mode="link" select="(following::*|descendant::*)[contains(@class, ' map/topicref ')][@href][not(@linking='none')][not(@linking='sourceonly')][@type='tutorialLesson']">
                <xsl:with-param name="role">tutmodulelesson</xsl:with-param>
                <xsl:with-param name="overrideTemplate">yes</xsl:with-param>
                <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>

            </xsl:otherwise>
          </xsl:choose>

        </xsl:if>

<!-- TUTORIAL SPEC :: End change ================================== -->


      </linkpool>
    </maplinks>
  </xsl:if>
  <xsl:apply-templates>
    <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
  </xsl:apply-templates>
</xsl:template>



<!-- RDA: added @type checks into the template match rule, so that this will only run on tutorial style links.
          Within the template, check other rules; if it should be processed as usual, call apply-imports. -->
<xsl:template mode="link" match="*[@href][not(@linking='none')][not(@linking='sourceonly')]
                                  [@type='tutorial' or @type='tutorialIntro' or @type='tutorialModule' or @type='tutorialLesson' or @type='tutorialSummary']">
  <xsl:param name="role">#none#</xsl:param>
  <xsl:param name="overrideTemplate">#no#</xsl:param>
  <xsl:param name="pathBackToMapDirectory"/>
  <xsl:param name="insideTutorialSequence">
    <!-- Set to "yes" if inside a tutorial sequence; set to "no" if not. -->
    <xsl:choose>
      <xsl:when test="ancestor::*[@type='tutorial' or @type='tutorialIntro' or @type='tutorialModule' or @type='tutorialLesson' or @type='tutorialSummary']">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <!-- If going to print, and @print=no, do not create links for this topicref -->
  <xsl:choose>
    <xsl:when test="($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no'"/>
    <!--RDA: Remove some default links in tutorials -->
    <!-- RDA: Removed check for @type from next test; must be a tutorial type to be in this template -->
    <xsl:when test="($role='parent' or $role='child' or $role='next' or $role='previous') and
                    $overrideTemplate='#no#' and
                    $insideTutorialSequence='yes'">
    </xsl:when>
    <xsl:otherwise>
      <!--<xsl:message>    Creating link to [<xsl:value-of select="$pathBackToMapDirectory"/>]<xsl:value-of select="@href"/></xsl:message>-->
      <link class="- topic/link ">
        <!-- Save the original topicref info, so later processing knows what this link came from -->
        <xsl:if test="@class">
          <xsl:attribute name="mapclass"><xsl:value-of select="@class"/></xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="@type|@scope|@importance|@format|@platform|@product|@audience|@otherprops|@rev|@xtrf|@xtrc"/>

        <xsl:attribute name="href">
          <xsl:choose>
            <!--absolute path - use as-is-->
            <xsl:when test="starts-with(@href,'http://') or starts-with(@href,'/') or
                            starts-with(@href,'https://') or starts-with(@href,'ftp:/')">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <!-- If the target has a copy-to value, link to that -->
            <xsl:when test="@copy-to">
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink"><xsl:value-of select="$pathBackToMapDirectory"/><xsl:value-of select="@copy-to"/></xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!--ref between two local paths - adjust normally-->
            <xsl:otherwise>
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink"><xsl:value-of select="$pathBackToMapDirectory"/><xsl:value-of select="@href"/></xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="not($role='#none#')">
          <xsl:attribute name="role"><xsl:value-of select="$role"/></xsl:attribute>
        </xsl:if>

        <!--figure out the metadata-->
        <!--xsl:call-template name="translatemetadata"/-->
        <!--figure out the other attributes- to do post-beta 2-->

        <!--figure out the linktext and desc-->
        <xsl:if test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">

          <!-- - Do not output linktext when The final output type is PDF or IDD
                 The target of the HREF is a local DITA file
                 The user has not specified locktitle to override the title -->
          <xsl:if test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and (not(@scope) or @scope='local') and (not(@format) or @format='dita' or @format='DITA') and (not(@locktitle) or @locktitle='no'))">
            <linktext class="- topic/linktext "><xsl:value-of select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')])"/></linktext>
          </xsl:if>
        </xsl:if>
        <xsl:if test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]">
          <!--<desc class="- topic/desc "><xsl:value-of select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')])"/></desc>-->
          <desc class="- topic/desc "><xsl:copy-of select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]/*|*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]/text()"/></desc>
        </xsl:if>
      </link>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
