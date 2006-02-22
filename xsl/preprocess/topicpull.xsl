<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan" xmlns:exsl="http://exslt.org/common">
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="../common/output-message.xsl"/>
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:param name="FILEREF">file://</xsl:param>
  <!-- The directory where the topic resides, starting with root -->
  <xsl:param name="WORKDIR" select="'./'"/>
  <xsl:param name="DITAEXT" select="'.xml'"/>  
  <xsl:param name="DBG" select="'no'"/>
  
  <xsl:template match="processing-instruction('workdir')" mode="get-work-dir">
    <xsl:value-of select="."/>
    <xsl:text>/</xsl:text>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/link ')]">    
    <xsl:if test="@href=''">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">017</xsl:with-param>
        <xsl:with-param name="msgsev">E</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    <xsl:copy>
      <!--copy existing explicit attributes-->
      <xsl:apply-templates select="@*"/>
      <!--copy inheritable attributes that aren't already explicitly defined-->
      <!--@type|@format|@scope|@importance|@role-->
      <!--need to create type variable regardless of whether it exists, for passing as a parameter to getstuff template-->
      <xsl:variable name="type">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">type</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="not(@type)">
        <xsl:choose>
          <xsl:when test="$type='#none#'">
            <!--do nothing - will attempt to grab type from target in get-stuff template-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="type">
              <xsl:value-of select="$type"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!--need to create format variable regardless of whether it exists, for passing as a parameter to getstuff template-->
      <xsl:variable name="format">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">format</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="not(@format)">
        <xsl:choose>
          <xsl:when test="$format='#none#'">
            <!--do nothing - no attribute-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="format">
              <xsl:value-of select="$format"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!--need to create scope variable regardless of whether it exists, for passing as a parameter to getstuff template-->
      <xsl:variable name="scope">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">scope</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="not(@scope)">
        <xsl:choose>
          <xsl:when test="$scope='#none#'">
            <!--do nothing - no attribute-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="scope">
              <xsl:value-of select="$scope"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!--only create importance variable if @importance not explicitly defined and already copied-->
      <xsl:if test="not(@importance)">
        <xsl:variable name="importance">
          <xsl:call-template name="inherit">
            <xsl:with-param name="attrib">importance</xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$importance='#none#'">
            <!--do nothing - no attribute-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="importance">
              <xsl:value-of select="$importance"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <!--only create role variable if @role not explicitly defined and already copied-->
      <xsl:if test="not(@role)">
        <xsl:variable name="role">
          <xsl:call-template name="inherit">
            <xsl:with-param name="attrib">role</xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$role='#none#'">
            <!--do nothing - no attribute-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="role">
              <xsl:value-of select="$role"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      
      <xsl:choose>
        <xsl:when test="@type and *[contains(@class, ' topic/linktext ')] and *[contains(@class, ' topic/desc ')]">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <!--grab type, text and metadata, as long there's an href to grab from, otherwise error-->
          <xsl:choose>
            <xsl:when test="@href=''"/>
            <xsl:when test="@href">
              <xsl:call-template name="get-stuff">
                <xsl:with-param name="localtype">
                  <xsl:value-of select="$type"/>
                </xsl:with-param>
                <xsl:with-param name="scope">
                  <xsl:value-of select="$scope"/>
                </xsl:with-param>
                <xsl:with-param name="format">
                  <xsl:value-of select="$format"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="output-message">
                <xsl:with-param name="msgnum">028</xsl:with-param>
                <xsl:with-param name="msgsev">E</xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>
  <xsl:template name="inherit">
    <xsl:param name="attrib"/>
    <xsl:choose>
      <xsl:when test="ancestor-or-self::*/@*[local-name()=$attrib]">
        <xsl:value-of select="(ancestor-or-self::*/@*[local-name()=$attrib])[last()]"/>
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class, ' topic/xref ')]">
    <xsl:if test="@href=''">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">017</xsl:with-param>
        <xsl:with-param name="msgsev">E</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    <xsl:choose>
      <!-- replace "*|text()" with "normalize-space()" to handle xref without 
        valid link content, in this situation, the xref linktext should be 
        grabbed from href target. -->
      <xsl:when test="normalize-space()">
        <xsl:copy>
          <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="@href and not(@href='')">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <!--create variables for attributes that will be passed by parameter to the getstuff template (which is shared with link, which needs the attributes in variables to save doing inheritance checks for each one)-->
          <xsl:variable name="type">
            <xsl:choose>
              <xsl:when test="@type">
                <xsl:value-of select="@type"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="format">
            <xsl:choose>
              <xsl:when test="@format">
                <xsl:value-of select="@format"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="scope">
            <xsl:choose>
              <xsl:when test="@scope">
                <xsl:value-of select="@scope"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <!--grab type, text and metadata, as long there's an href to grab from, otherwise error-->
          <xsl:call-template name="get-stuff">
            <xsl:with-param name="localtype"><xsl:value-of select="$type"/></xsl:with-param>
            <xsl:with-param name="scope"><xsl:value-of select="$scope"/></xsl:with-param>
            <xsl:with-param name="format"><xsl:value-of select="$format"/></xsl:with-param>
          </xsl:call-template>
        </xsl:copy>
      </xsl:when>
      <!-- Ignore <xref></xref>, <xref href=""></xref> -->
      <xsl:otherwise>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">028</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="verify-type-attribute">
    <xsl:param name="type"/>
    <!-- Type value specified on the link -->
    <xsl:param name="actual-class"/>
    <!-- Class value of the target element -->
    <xsl:param name="actual-name"/>
    <!-- Name of the target element -->
    <xsl:param name="targetting"/>
    <!-- Targetting a "topic" or "element" -->
    <xsl:choose>
      <!-- The type is correct; concept typed as concept, newtype defined as newtype -->
      <xsl:when test="$type=$actual-name"/>
      <!-- If the actual class contains the specified type; reference can be called topic,
         specializedReference can be called reference -->
      <xsl:when test="($targetting='topic' and contains($actual-class,concat(' ',$type,'/',$type,' '))) or                     ($targetting='element' and contains($actual-class,concat('/',$type,' ')))">
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">029</xsl:with-param>
          <xsl:with-param name="msgsev">I</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="name()"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!-- Otherwise: incorrect type is specified -->
      <xsl:otherwise>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">030</xsl:with-param>
          <xsl:with-param name="msgsev">W</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="name()"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="get-stuff">
    <xsl:param name="localtype">#none#</xsl:param>
    <xsl:param name="scope">#none#</xsl:param>
    <xsl:param name="format">#none#</xsl:param>
    <xsl:param name="WORKDIR">
      <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
    </xsl:param>
    <!--the file name of the target, if any-->
    <xsl:variable name="file">
      <xsl:choose>
        <xsl:when test="contains(@href,'://') and contains(@href,'#')">
          <xsl:value-of select="substring-before(@href,'#')"/>
        </xsl:when>
        <xsl:when test="contains(@href,'://')">
          <xsl:value-of select="@href"/>
        </xsl:when>
        <xsl:when test="contains(@href,'#')">
          <xsl:value-of select="$FILEREF"/>
          <xsl:value-of select="$WORKDIR"/>
          <xsl:value-of select="substring-before(@href,'#')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$FILEREF"/>
          <xsl:value-of select="$WORKDIR"/>
          <xsl:value-of select="@href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!--the position of the target topic relative to the current one: in the same file, referenced by id in another file, or referenced as the first topic in another file-->
    <xsl:variable name="topicpos">
      <xsl:choose>
        <xsl:when test="starts-with(@href,'#')">samefile</xsl:when>
        <xsl:when test="contains(@href,'#')">otherfile</xsl:when>
        <xsl:otherwise>firstinfile</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$topicpos!='samefile' and                 ($scope!='external' and $scope!='peer') and                 ($format='dita' or $format='DITA' or $format='#none#')">
      <xsl:if test="not(document($file,/)) or not(document($file,/)/*/*)">
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">031</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="$file"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
    <!--the id of the target topic-->
    <xsl:variable name="topicid">
      <xsl:choose>
        <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
          <xsl:value-of select="substring-before(substring-after(@href,'#'),'/')"/>
        </xsl:when>
        <xsl:when test="contains(@href,'#')">
          <xsl:value-of select="substring-after(@href,'#')"/>
        </xsl:when>
        <xsl:otherwise>#none#</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!--the id of the target element, if any-->
    <xsl:variable name="elemid">
      <xsl:choose>
        <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
          <xsl:value-of select="substring-after(substring-after(@href,'#'),'/')"/>
        </xsl:when>
        <xsl:otherwise>#none#</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!--type - grab type from target, if not defined locally -->
    <xsl:variable name="type">
      <xsl:choose>
        <!--just use localtype if it's not "none"-->
        <xsl:when test="not($localtype='#none#')">
          <xsl:value-of select="$localtype"/>
        </xsl:when>
        <!--check whether it's worth trying to retrieve-->
        <xsl:when
          test="$scope='external' or $scope='peer' or not($format='#none#' or $format='dita' or $format='DITA')">#none#
          <!--type is unavailable-->
        </xsl:when>
        <!-- If this is an empty href, ignore it; we already put out a message -->
        <xsl:when test="@href=''"/>
        <!--check whether file extension is correct, for targets in other files-->
        <xsl:when
            test="not($topicpos='samefile') and not(contains($file,$DITAEXT))">#none#<xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">006</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <!--grab from target topic-->
        <xsl:when test="$elemid='#none#'">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:choose>
                <xsl:when test="//*[contains(@class, ' topic/topic ')][@id=$topicid]">
                  <xsl:value-of select="local-name(//*[contains(@class, ' topic/topic ')][@id=$topicid])"/>
                </xsl:when>
                <xsl:otherwise>#none#
                  <!--type could not be retrieved-->
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:when test="$topicpos='otherfile'">
              <xsl:choose>
                <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])"/>
                </xsl:when>
                <xsl:otherwise>#none#
                  <!--type could not be retrieved-->
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:when test="$topicpos='firstinfile'">
              <xsl:choose>
                <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][1])"/>
                </xsl:when>
                <xsl:otherwise>#none#
                  <!--type could not be retrieved-->
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <!--never happens - all three values for topicpos are tested-->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <!--grab type from target element-->
        <xsl:when test="$localtype='#none#'">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:choose>
                <xsl:when test="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid]">
                  <xsl:value-of select="local-name(//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid])"/>
                </xsl:when>
                <xsl:otherwise>#none#
                  <!--type could not be retrieved-->
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:when test="$topicpos='otherfile'">
              <xsl:choose>
                <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid]">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid])"/>
                </xsl:when>
                <xsl:otherwise>#none#
                  <!--type could not be retrieved-->
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <!--never happens - must be either same file or other file, firstinfile not possible if there's an element id present-->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!--tested both conditions for localtype (exists or not), so no otherwise-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!--now, create the type attribute, if the type attribute didn't exist locally but was retrieved successfully-->
    <xsl:if test="$localtype='#none#' and not($type='#none#')">
      <xsl:attribute name="type">
        <xsl:value-of select="$type"/>
      </xsl:attribute>
    </xsl:if>

    <xsl:if test="$localtype!='#none#' and (contains(@href,$DITAEXT) or starts-with(@href,'#')) and not(@scope='external' or @scope='peer') and (not(@format) or @format='dita' or @format='DITA')">
      <xsl:choose>
        <!-- If this is an xref, there can't be any elements or text inside -->
        <xsl:when test="contains(@class,' topic/xref ') and not(*|text())">
          <xsl:choose>
            <!-- targetting an element in the same file (not a topic) -->
            <xsl:when test="$topicpos='samefile' and $elemid!='#none#' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid]">
              <xsl:call-template name="verify-type-attribute">
                <xsl:with-param name="type">
                  <xsl:value-of select="$localtype"/>
                </xsl:with-param>
                <xsl:with-param name="actual-class">
                  <xsl:value-of select="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid][1]/@class"/>
                </xsl:with-param>
                <xsl:with-param name="actual-name">
                  <xsl:value-of select="local-name(//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid][1])"/>
                </xsl:with-param>
                <xsl:with-param name="targetting">element</xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!-- targetting a topic in the same file -->
            <xsl:when test="$topicpos='samefile' and $elemid='#none#' and //*[contains(@class, ' topic/topic ')][@id=$topicid]">
              <xsl:call-template name="verify-type-attribute">
                <xsl:with-param name="type">
                  <xsl:value-of select="$localtype"/>
                </xsl:with-param>
                <xsl:with-param name="actual-class">
                  <xsl:value-of select="//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                </xsl:with-param>
                <xsl:with-param name="actual-name">
                  <xsl:value-of select="local-name(//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                </xsl:with-param>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!-- targetting an element in another  file (not a topic) -->
            <xsl:when test="$topicpos='otherfile' and $elemid!='#none#' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid]">
              <xsl:call-template name="verify-type-attribute">
                <xsl:with-param name="type">
                  <xsl:value-of select="$localtype"/>
                </xsl:with-param>
                <xsl:with-param name="actual-class">
                  <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid][1]/@class"/>
                </xsl:with-param>
                <xsl:with-param name="actual-name">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[@id=$elemid][1])"/>
                </xsl:with-param>
                <xsl:with-param name="targetting">element</xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!-- targetting a topic in another file -->
            <xsl:when test="$topicpos='otherfile' and $elemid='#none#' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]">
              <xsl:call-template name="verify-type-attribute">
                <xsl:with-param name="type">
                  <xsl:value-of select="$localtype"/>
                </xsl:with-param>
                <xsl:with-param name="actual-class">
                  <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                </xsl:with-param>
                <xsl:with-param name="actual-name">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                </xsl:with-param>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!-- targetting a topic in another file -->
            <xsl:when test="$topicpos='firstinfile' and document($file,/)//*[contains(@class, ' topic/topic ')]">
              <xsl:call-template name="verify-type-attribute">
                <xsl:with-param name="type">
                  <xsl:value-of select="$localtype"/>
                </xsl:with-param>
                <xsl:with-param name="actual-class">
                  <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][1]/@class"/>
                </xsl:with-param>
                <xsl:with-param name="actual-name">
                  <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][1])"/>
                </xsl:with-param>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:call-template>
            </xsl:when>
          </xsl:choose>
        </xsl:when>
        <!-- If this is a link, linktext, linkdesc, or @type must be missing.
           There should not be any links with element IDs, but put in the check just to be sure. -->
        <!-- If linktext, desc, and @type are all specified, we won't be here, so assume something is not specified. -->
        <xsl:when test="contains(@class,' topic/link ')">
          <xsl:choose>
            <!-- If there is a link to an element (error condition, so skip) -->
            <xsl:when test="$elemid!='#none#'"/>
            <!-- If we know this link came from a map, it has already been checked -->
            <xsl:when test="contains(@xtrf,'.ditamap')"/>
            <xsl:when test="ancestor::*[contains(@class, ' topic/linkpool ')]/@mapkeyref"/>
            <xsl:otherwise>
              <xsl:choose>
                <!-- targetting a topic in this file -->
                <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]">
                  <xsl:call-template name="verify-type-attribute">
                    <xsl:with-param name="type">
                      <xsl:value-of select="$localtype"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-class">
                      <xsl:value-of select="//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-name">
                      <xsl:value-of select="local-name(//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                    </xsl:with-param>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:call-template>
                </xsl:when>
                <!-- targetting a topic in another file -->
                <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]">
                  <xsl:call-template name="verify-type-attribute">
                    <xsl:with-param name="type">
                      <xsl:value-of select="$localtype"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-class">
                      <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-name">
                      <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                    </xsl:with-param>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:call-template>
                </xsl:when>
                <!-- targetting the first topic in another file -->
                <xsl:when test="$topicpos='firstinfile' and document($file,/)//*[contains(@class, ' topic/topic ')]">
                  <xsl:call-template name="verify-type-attribute">
                    <xsl:with-param name="type">
                      <xsl:value-of select="$localtype"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-class">
                      <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][1]/@class"/>
                    </xsl:with-param>
                    <xsl:with-param name="actual-name">
                      <xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][1])"/>
                    </xsl:with-param>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:call-template>
                </xsl:when>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
    <!--create class value string implied by the link's type, used for comparison with class strings in the target topic for validation-->
    <xsl:variable name="classval">
      <xsl:call-template name="classval">
        <xsl:with-param name="type">
          <xsl:value-of select="$type"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!--linktext-->
    <xsl:variable name="linktext">
      <xsl:choose>
        <!--when type is external, or format is defaulted to not-DITA 
            (because scope is external), or format is explicitly something 
            non-DITA, use the href value with no error message-->
        <xsl:when test="$type='external' or ($scope='external' and $format='#none#') or not($format='#none#' or $format='dita' or $format='DITA')">
          <xsl:value-of select="@href"/>
        </xsl:when>
        <!--when scope is external or peer and format is DITA, don't use
          the href - defer to the final output process - and leave it 
          to the final output process to emit an error msg-->
        <xsl:when test="$scope='peer' or $scope='external'">#none#</xsl:when>
        <xsl:when test="@href=''">#none#</xsl:when>

        <!--when format is DITA, it's a different file, and file extension 
          is wrong, use the href and generate an error -->
        <xsl:when test="not($topicpos='samefile') and not(contains($file,$DITAEXT))">
          <xsl:value-of select="@href"/>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">006</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <!-- otherwise pull text from the target -->
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="getlinktext">
            <xsl:with-param name="file">
              <xsl:value-of select="$file"/>
            </xsl:with-param>
            <xsl:with-param name="topicpos">
              <xsl:value-of select="$topicpos"/>
            </xsl:with-param>
            <xsl:with-param name="classval">
              <xsl:value-of select="$classval"/>
            </xsl:with-param>
            <xsl:with-param name="topicid">
              <xsl:value-of select="$topicid"/>
            </xsl:with-param>
            <xsl:with-param name="elemid">
              <xsl:value-of select="$elemid"/>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <!-- first check if the link content is valid, if it is invalid, need to 
           pull text from the target. -->
      <xsl:when test="normalize-space()=''">
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/xref ')">
          <!-- need to avoid flattening complex markup here-->
          <xsl:value-of select="$linktext"/>
        </xsl:if>
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/link ')">
          <linktext class=" topic/linktext ">
            <xsl:value-of select="$linktext"/>
          </linktext>
        </xsl:if>
      </xsl:when>
      <!--if there's link content other than a shortdesc, no need to pull linktext from target-->
      <xsl:when test="text()|*[not(contains(@class, ' topic/desc '))]">
        <xsl:apply-templates select="text()|*[not(contains(@class, ' topic/desc '))]"/>
      </xsl:when>
      <!--pull text from the target-->
      <xsl:otherwise>        
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/xref ')">
          <!-- need to avoid flattening complex markup here-->
          <xsl:value-of select="$linktext"/>
        </xsl:if>
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/link ')">
          <linktext class=" topic/linktext ">
            <xsl:value-of select="$linktext"/>
          </linktext>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <!--if it's an xref, shortdesc not allowed-->
      <xsl:when test="contains(@class, ' topic/xref ')"/>
      <!--if there's already a desc, copy it-->
      <xsl:when test="*[contains(@class, ' topic/desc ')]">
        <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
      </xsl:when>
      <!--if the target is inaccessible, don't do anything - shortdesc is optional -->
      <xsl:when test="$scope='external' or $scope='peer' or $type='external' or not($format='#none#' or $format='dita' or $format='DITA')"/>
      <!--otherwise try pulling shortdesc from target-->
      <xsl:otherwise>
        <xsl:variable name="shortdesc">
          <xsl:apply-templates select="." mode="getshortdesc">
            <xsl:with-param name="file">
              <xsl:value-of select="$file"/>
            </xsl:with-param>
            <xsl:with-param name="topicpos">
              <xsl:value-of select="$topicpos"/>
            </xsl:with-param>
            <xsl:with-param name="classval">
              <xsl:value-of select="$classval"/>
            </xsl:with-param>
            <xsl:with-param name="topicid">
              <xsl:value-of select="$topicid"/>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="not($shortdesc='#none#')">
          <desc class=" topic/desc ">
            <xsl:apply-templates select="exsl:node-set($shortdesc)"/>
          </desc>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--template used to construct the class value used to validate link targets against source types. -->
  <xsl:template name="classval">
    <xsl:param name="type">#none#</xsl:param>
    <xsl:choose>
      <!--if type doesn't exist, assume target is a topic of some kind-->
      <xsl:when test="$type='#none#'">
        <xsl:text/>topic/topic<xsl:text/>
      </xsl:when>
      <!--if there is an element id, construct a partial classvalue and just use that-->
      <xsl:when
          test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">/<xsl:value-of select="$type"/>
        <xsl:text/>
      </xsl:when>
      <!-- otherwise there's a type but no element id, so construct a root element classvalue, eg task/task or concept/concept-->
      <xsl:otherwise>
        <xsl:text/>
        <xsl:value-of select="$type"/>/<xsl:value-of select="$type"/>
        <xsl:text/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--basic getlinktext template provides generic support, for linking to topics 
    only; more specific fig types are handled with higher priority settings; 
    template should only be called AFTER conditions have been checked such as
    scope and format that would otherwise prevent text pulling -->
  <xsl:template mode="getlinktext" match="*[contains(@class,' topic/link ')] | *[contains(@class,' topic/xref ')]">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!--targetting a topic in the same file-->
      <xsl:when test="$topicpos='samefile'">
        <xsl:choose>
          <xsl:when test="//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/title ')]">
            
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="(//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          
          <xsl:when test="//*[contains(@class, ' topic/topic ')][@id=$topicid]">
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="(//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <!--if can't retrieve, output href as linktext, emit message. since href doesn't include file name, no issues with file extension-->
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">032</xsl:with-param>
              <xsl:with-param name="msgsev">E</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!--targetting the first topic in a target file-->
      <xsl:when test="$topicpos='firstinfile'">
        <xsl:choose>
          <xsl:when test="document($file,/)//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/title ')]">
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="(document($file,/)//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/title ')]">
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <!-- if can't retrieve, don't create the linktext - defer to the final output process, which will massage the file name-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!--targetting a particular topic in another file-->
      <xsl:when test="$topicpos='otherfile'">
        <xsl:choose>
          <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/title ')]">
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/title ')]">
            <xsl:variable name="target-text">
              <xsl:apply-templates
                select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:variable>
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <!-- if can't retrieve, don't create the linktext - defer to the final output process, which will massage the file name-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!--tested all three values for topicpos, for both topics and elements - no otherwise-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--get linktext for xref to any body elements that did not have a local type,
    but whose type was determined by retrieval from the target; overridden by
    higher-priority templates for anything that has a locally defined type -->
  <xsl:template mode="getlinktext" priority="1" match="*[contains(@class,' topic/xref ')][contains(@href,'#') and contains(substring-after(@href,'#'),'/')]">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="useclassval">
      <xsl:choose>
        <!--if it's a known type we can handle, use type as-is-->
        <xsl:when test="      $classval='/li '    or $classval='/fn '    or $classval='/dlentry '    or $classval='/section '   or $classval='/example '   or $classval='/fig '   or $classval='/figgroup '">
          <!--can be handled as-is-->
          <xsl:value-of select="$classval"/>
        </xsl:when>
        <!--otherwise figure out what it's topic-level equivalent is by looking it up in the target element's class value-->
        <xsl:otherwise>
          <!-- taking classval as a parameter, and returning useclassval-->
          <xsl:call-template name="firstclass">
            <xsl:with-param name="file">
              <xsl:value-of select="$file"/>
            </xsl:with-param>
            <xsl:with-param name="topicpos">
              <xsl:value-of select="$topicpos"/>
            </xsl:with-param>
            <xsl:with-param name="classval">
              <xsl:value-of select="$classval"/>
            </xsl:with-param>
            <xsl:with-param name="topicid">
              <xsl:value-of select="$topicid"/>
            </xsl:with-param>
            <xsl:with-param name="elemid">
              <xsl:value-of select="$elemid"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <!--processing as a list item - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="$useclassval='/li '">
        <xsl:call-template name="litext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!--processing as a footnote - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="$useclassval='/fn '">
        <xsl:call-template name="fntext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!--processing as a dlentry - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="$useclassval='/dlentry '">
        <xsl:call-template name="dlentrytext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!--processing as a table - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="$useclassval='/table '">
        <xsl:call-template name="tabletext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!--processing as a figure - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="$useclassval='/fig '">
        <xsl:call-template name="figtext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <!--if it's none of the above types, then apply generic processing - for table, fig, etc. - looking for a child title element-->
      <xsl:otherwise>
        <xsl:call-template name="blocktext">
          <xsl:with-param name="file">
            <xsl:value-of select="$file"/>
          </xsl:with-param>
          <xsl:with-param name="topicpos">
            <xsl:value-of select="$topicpos"/>
          </xsl:with-param>
          <xsl:with-param name="classval">
            <xsl:value-of select="$useclassval"/>
          </xsl:with-param>
          <xsl:with-param name="topicid">
            <xsl:value-of select="$topicid"/>
          </xsl:with-param>
          <xsl:with-param name="elemid">
            <xsl:value-of select="$elemid"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--used to retrieve a topic-level type from the target element - for example, 
    if user specifies "step" as the type, this will find the target step, 
    then look up its topic-level equivalent - /li - and use that -->
  <xsl:template name="firstclass">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!--look for the target in the same file, and create the topic-level classval if accessible-->
      <xsl:when
          test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">/<xsl:value-of select="substring-before(substring-after(//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/@class,' topic/'),' ')"/>
        <xsl:text/>
      </xsl:when>
      <!--look for the target in another file, and create the topic-level classval if accessible-->
      <xsl:when
          test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">/<xsl:value-of select="substring-before(substring-after(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/@class,' topic/'),' ')"/>
        <xsl:text/>
      </xsl:when>
      <xsl:otherwise>
        <!--don't generate error msg, since will also be attempting retrieval of linktext, and don't want to double-up on error msgs-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="blocktext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='figgroup' or @type='section' or @type='example']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!--look for the target in another file, and create the linktext if accessible-->
      <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!--otherwise use the href, unless it contains .dita, in which case defer to the final output pass to decide what to do with the file extension-->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">032</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="figtext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='fig']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <!-- and look for the target in another file, and create the linktext if accessible-->
      <xsl:when test="($topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1])                        or ($topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1])">
        <xsl:variable name="ancestorlangUpper">
          <!-- the current xml:lang value (en-us if none found) -->
          <xsl:choose>
            <xsl:when test="ancestor-or-self::*/@xml:lang">
              <xsl:value-of select="ancestor-or-self::*/@xml:lang"/>
            </xsl:when>
            <xsl:otherwise>en-us</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="ancestorlang">
          <!-- convert the value to lower case -->
          <xsl:value-of select="translate($ancestorlangUpper,                                       '-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',                                       '-abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz')"/>
        </xsl:variable>
        <xsl:variable name="fig-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates
                select="(//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="fignumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates
                select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="fignumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <!-- Hungarian: "1. Figure " -->
          <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )">
            <xsl:value-of select="$fig-count-actual"/>
            <xsl:text>. </xsl:text>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Figure'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Figure'"/>
            </xsl:call-template>
            <xsl:text/>
            <xsl:value-of select="$fig-count-actual"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">032</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Determine the number of the figure being linked to -->
  <xsl:template
    match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]"
    mode="fignumber" priority="2">
    <xsl:number count="*/fig/title" level="any"/>
  </xsl:template>
  <!-- Table links -->
  <xsl:template name="tabletext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='table']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <!-- and look for the target in another file, and create the linktext if accessible-->
      <xsl:when test="($topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1])                        
        or ($topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1])">
        <xsl:variable name="ancestorlangUpper">
          <!-- the current xml:lang value (en-us if none found) -->
          <xsl:choose>
            <xsl:when test="ancestor-or-self::*/@xml:lang">
              <xsl:value-of select="ancestor-or-self::*/@xml:lang"/>
            </xsl:when>
            <xsl:otherwise>en-us</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="ancestorlang">
          <!-- convert the value to lower case -->
          <xsl:value-of select="translate($ancestorlangUpper,                                       
            '-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',                                       
            '-abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz')"/>
        </xsl:variable>
        <xsl:variable name="tbl-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates
                select="(//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="tblnumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates
                select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/title ')][1]" mode="tblnumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <!-- Hungarian: "1. Table " -->
          <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )">
            <xsl:value-of select="$tbl-count-actual"/>
            <xsl:text>. </xsl:text>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Table'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Table'"/>
            </xsl:call-template>
            <xsl:text/>
            <xsl:value-of select="$tbl-count-actual"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!--otherwise use the href, unless it contains .dita, in which case defer to the final output pass to decide what to do with the file extension-->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">032</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Determine the number of the table being linked to -->
  <xsl:template
    match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]"
    mode="tblnumber" priority="2">
    <xsl:number count="*/table/title" level="any"/>
  </xsl:template>
  <xsl:template name="litext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='li']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <!-- If the list item exists, and is in an OL, process it -->
      <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class,' topic/ol ')]/*[contains(@class, $classval)][@id=$elemid]">
        <xsl:apply-templates mode="xref" select="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class,' topic/ol ')]/*[contains(@class, $classval)][@id=$elemid]"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class,' topic/ol ')]/*[contains(@class, $classval)][@id=$elemid]">
        <xsl:apply-templates mode="xref" select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class,' topic/ol ')]/*[contains(@class, $classval)][@id=$elemid]"/>
      </xsl:when>
      <!-- If the list item exists, but is in some other kind of list, issue a message -->
      <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">
        <xsl:call-template name="invalid-list-item"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">
        <xsl:call-template name="invalid-list-item"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">033</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template
    match="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]"
    mode="xref" priority="2">
    <xsl:number level="multiple"
      count="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]" format="1.a.i.1.a.i.1.a.i"/>
  </xsl:template>
  <!-- Instead of matching an unordered list item, we will call this template; that way
     the error points to the XREF, not to the list item. -->
  <xsl:template name="invalid-list-item">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">034</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="fntext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='fn']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">
        <xsl:apply-templates mode="xref" select="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]">
        <xsl:apply-templates mode="xref" select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">035</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/fn ')]" mode="xref">
    <xsl:variable name="fnid">
      <xsl:number from="/" level="any"/>
    </xsl:variable>
    <xsl:variable name="callout">
      <xsl:value-of select="@callout"/>
    </xsl:variable>
    <xsl:variable name="convergedcallout">
      <xsl:choose>
        <xsl:when test="string-length($callout)&gt;'0'">
          <xsl:value-of select="$callout"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$fnid"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <a name="fnsrc_{$fnid}" href="#fntarg_{$fnid}">
      <sup>
        <xsl:value-of select="$convergedcallout"/>
      </sup>
    </a>
  </xsl:template>
  <!--getting text from a dlentry target-->
  <xsl:template name="dlentrytext" mode="getlinktext" priority="2" match="*[contains(@class,' topic/xref ')][@type='dlentry']">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile' and //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/dlterm ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/dlterm ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/dlterm ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ')]//*[contains(@class, $classval)][@id=$elemid]/*[contains(@class,' topic/dlterm ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="contains(@href,$DITAEXT)">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">036</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--getting the shortdesc for a link; called from main mode template for link/xref, 
    only after conditions such as scope and format have been tested and a text pull
    has been determined to be appropriate-->
  <xsl:template mode="getshortdesc" match="*[contains(@class,' topic/link ')]">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile'">
        <xsl:choose>
          <xsl:when test="//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]/* | //*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:when test="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]/* | //*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:when test="//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/shortdesc ')]/* | //*[contains(@class, $classval)][1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:when test="//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')]/* | //*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:otherwise>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$topicpos='firstinfile'">
        <xsl:choose>
          <xsl:when test="document($file,/)//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="(document($file,/)//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/shortdesc ')]/* | (document($file,/)//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="(document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/shortdesc ')]/* | (document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:otherwise>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile'">
        <xsl:choose>
          <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/shortdesc ')]/* | (document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]">
            <xsl:copy-of select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class, ' topic/shortdesc ')]/* | (document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class, ' topic/shortdesc ')]/text()"/>
          </xsl:when>
          <xsl:otherwise>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!--tested all three values for topicpos - no otherwise-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/image ')]" mode="text-only">
    <xsl:choose>
      <xsl:when test="*[contains(@class,' topic/alt ')]">
        <xsl:apply-templates mode="text-only"/>
      </xsl:when>
      <xsl:when test="@alt">
        <xsl:value-of select="@alt"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/boolean ')]" mode="text-only">
    <xsl:value-of select="name()"/>
    <xsl:text>: </xsl:text>
    <xsl:value-of select="@state"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/state ')]" mode="text-only">
    <xsl:value-of select="name()"/>
    <xsl:text>: </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="@value"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="text-only"/>
  <xsl:template match="*" mode="text-only">
    <xsl:apply-templates select="text()|*" mode="text-only"/>
  </xsl:template>
  <xsl:template match="*|@*|comment()|processing-instruction()|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
