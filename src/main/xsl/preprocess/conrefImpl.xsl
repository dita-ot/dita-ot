<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:conref="http://dita-ot.sourceforge.net/ns/200704/conref"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  exclude-result-prefixes="ditamsg conref xs dita-ot">

  <xsl:import href="../common/output-message.xsl"/>
  <xsl:import href="../common/dita-utilities.xsl"/>

  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix" select="'DOTX'"/>

  <xsl:param name="EXPORTFILE"/>
  <xsl:param name="TRANSTYPE"/>
  <xsl:param name="DBG" select="no"/>

  <xsl:param name="file-being-processed"/>

  <xsl:variable name="ORIGINAL-DOMAINS" select="(/*/@domains | /dita/*[@domains][1]/@domains)[1]" as="xs:string"/>

  <xsl:key name="id" match="*[@id]" use="@id"/>

  <xsl:template match="/">
    <xsl:apply-templates>
      <xsl:with-param name="conref-ids" select="()"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- If the target element does not exist, this template will be called to issue an error -->
  <xsl:template name="missing-target-error">
    <xsl:apply-templates select="." mode="ditamsg:missing-conref-target-error"/>
  </xsl:template>

  <!-- If an ID is duplicated, and there are 2 possible targets, issue a warning -->
  <xsl:template name="duplicateConrefTarget">
    <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
  </xsl:template>

  <!-- Determine the relative path to a conref'ed file. Start with the path and
     filename. Output each single directory, and chop it off. Keep going until
     only the filename is left. -->
  <xsl:template name="find-relative-path" as="xs:string">
    <xsl:param name="remainingpath" as="xs:string">
      <xsl:choose>
        <xsl:when test="contains(@conref, '#')">
          <xsl:value-of select="substring-before(@conref, '#')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@conref"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:value-of>
      <xsl:if test="contains($remainingpath, '/')">
        <xsl:value-of select="substring-before($remainingpath, '/')"/>
        <xsl:text>/</xsl:text>
        <xsl:call-template name="find-relative-path">
          <xsl:with-param name="remainingpath" select="substring-after($remainingpath, '/')"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:value-of>
  </xsl:template>

  <xsl:template name="get-source-attribute" as="xs:string*">
    <xsl:param name="current-node" select="."/>
    <xsl:apply-templates select="$current-node/@*" mode="get-source-attribute"/>
  </xsl:template>

  <xsl:template match="@*" mode="get-source-attribute" as="xs:string?">
    <xsl:if test="not(. = '')">
      <xsl:value-of select="name()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@xtrc | @xtrf" mode="get-source-attribute" priority="10"/>
  <xsl:template match="@conref" mode="get-source-attribute" priority="10"/>
  <!-- DITA 1.1 added the key -dita-use-conref-target, which can be used on required attributes
     to be sure they do not override the same attribute on a target element. -->
  <xsl:template match="@*[. = '-dita-use-conref-target']" mode="get-source-attribute" priority="11"/>
  <!-- The value -dita-ues-conref-target replaces the need for the following templates, which
     ensured that known required attributes did not override the conref target. They are left
     here for completeness. -->
  <xsl:template match="*[contains(@class, ' topic/image ')]/@href" mode="get-source-attribute" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/tgroup ')]/@cols" mode="get-source-attribute" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/boolean ')]/@state" mode="get-source-attribute" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/state ')]/@name" mode="get-source-attribute" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/state ')]/@value" mode="get-source-attribute" priority="10"/>
  <xsl:template match="*[contains(@class, ' mapgroup-d/topichead ')]/@navtitle" mode="get-source-attribute" priority="10"/>

  <xsl:template match="@*" mode="conaction-target">
    <xsl:choose>
      <xsl:when test="name() = 'conaction' or name() = 'conref'"/>
      <xsl:otherwise>
        <xsl:copy/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- When content is pushed from one topic to another, it is still rendered in the original context.
 Processors may delete empty the element that with the conaction="mark" attribute. -->
  <xsl:template match="*[@conaction]" priority="10">
    <xsl:choose>
      <xsl:when test="@conaction != 'mark'">
        <xsl:copy>
          <xsl:apply-templates select="@*" mode="conaction-target"/>
          <xsl:apply-templates select="* | comment() | processing-instruction() | text()"/>
        </xsl:copy>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!--if something has a conref attribute, jump to the target if valid and continue applying templates-->

  <xsl:template match="*[@conref][@conref != ''][not(@conaction)]" priority="10">
    <!-- If we have already followed a relative path, pick it up -->
    <xsl:param name="current-relative-path" as="xs:string" select="''"/>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:param name="source-attributes" as="xs:string*"/>
    <xsl:param name="conref-ids" as="xs:string*"/>
    <xsl:param name="WORKDIR" as="xs:string">
      <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
    </xsl:param>
    <xsl:param name="original-element" as="xs:string">
      <xsl:call-template name="get-original-element"/>
    </xsl:param>


    <xsl:param name="original-attributes" select="@*" as="attribute()*"/>

    <xsl:variable name="conrefend" as="xs:string?">
      <xsl:choose>
        <xsl:when test="dita-ot:has-element-id(@conrefend)">
          <xsl:value-of select="dita-ot:get-element-id(@conrefend)"/>
        </xsl:when>
        <xsl:when test="contains(@conrefend, '#')">
          <xsl:value-of select="substring-after(@conrefend, '#')"/>
        </xsl:when>
        <xsl:when test="contains(@conrefend, '/')">
          <xsl:value-of select="substring-after(@conrefend, '/')"/>
        </xsl:when>
        <xsl:when test="not(@conrefend = '')">
          <xsl:value-of select="@conrefend"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>


    <xsl:variable name="add-relative-path" as="xs:string">
      <xsl:call-template name="find-relative-path"/>
    </xsl:variable>
    <!-- Add this to the list of followed conref IDs -->
    <xsl:variable name="updated-conref-ids" select="($conref-ids, generate-id(.))"/>

    <!-- Keep the source node in a variable, to pass to the target. It can be used to save 
       attributes that were specified locally. If for some reason somebody passes from
       conref straight to conref, then just save the first one (in source-attributes) -->

    <!--get element local name, parent topic's domains, and then file name, topic id, element id from conref value-->
    <xsl:variable name="element" select="local-name(.)"/>
    <!--xsl:variable name="domains"><xsl:value-of select="ancestor-or-self::*[@domains][1]/@domains"/></xsl:variable-->

    <xsl:variable name="file-prefix" select="concat($WORKDIR, $current-relative-path)" as="xs:string"/>

    <xsl:variable name="file-origin" as="xs:string">
      <xsl:call-template name="get-file-uri">
        <xsl:with-param name="href" select="@conref"/>
        <xsl:with-param name="file-prefix" select="$file-prefix"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="file" as="xs:string">
      <xsl:call-template name="replace-blank">
        <xsl:with-param name="file-origin">
          <xsl:value-of select="$file-origin"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- get domains attribute in the target file -->
    <xsl:variable name="domains" select="(document($file, /)/*/@domains | document($file, /)/dita/*[@domains][1]/@domains)[1]" as="xs:string"/>
    <!--the file name is useful to href when resolveing conref -->
    <xsl:variable name="conref-filename" as="xs:string">
      <xsl:call-template name="replace-blank">
        <xsl:with-param name="file-origin" select="substring-after(substring-after($file-origin, $file-prefix), $add-relative-path)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="conref-source-topic" as="xs:string">
      <xsl:choose>
        <xsl:when test="exists($conref-source-topicid) and not($conref-source-topicid = '')">
          <xsl:value-of select="$conref-source-topicid"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- conref file name with relative path -->
    <xsl:variable name="filename" select="substring-after($file-origin, $file-prefix)"/>

    <!-- replace the extension name -->
    <xsl:variable name="FILENAME" select="concat(substring-before($filename, '.'), '.dita')"/>

    <xsl:variable name="topicid" select="dita-ot:get-topic-id(@conref)" as="xs:string?"/>
    <xsl:variable name="elemid" select="dita-ot:get-element-id(@conref)" as="xs:string?"/>

    <xsl:choose>
      <!-- exportanchors defined in topicmeta-->
      <xsl:when test="($TRANSTYPE = 'eclipsehelp')
                  and (document($EXPORTFILE, /)//file[@name = $FILENAME]/id[@name = $elemid])
                  and (document($EXPORTFILE, /)//file[@name = $FILENAME]/topicid[@name = $topicid]) ">
        <!-- just copy -->
        <xsl:copy>
          <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
            <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
            <xsl:with-param name="conref-filename" select="$conref-filename"/>
            <xsl:with-param name="topicid" select="$topicid"/>
            <xsl:with-param name="elemid" select="$elemid"/>
            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
            <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
            <xsl:with-param name="conref-ids" select="$conref-ids"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:when>
      <!-- exportanchors defined in prolog-->
      <xsl:when test="($TRANSTYPE = 'eclipsehelp') 
                  and document($EXPORTFILE, /)//file[@name = $FILENAME]/topicid[@name = $topicid]/id[@name = $elemid]">
        <!-- just copy -->
        <xsl:copy>
          <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
            <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
            <xsl:with-param name="conref-filename" select="$conref-filename"/>
            <xsl:with-param name="topicid" select="$topicid"/>
            <xsl:with-param name="elemid" select="$elemid"/>
            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
            <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
            <xsl:with-param name="conref-ids" select="$conref-ids"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:when>
      <!-- just has topic id -->
      <xsl:when test="empty($elemid) and ($TRANSTYPE = 'eclipsehelp') 
            and (document($EXPORTFILE, /)//file[@name = $FILENAME]/topicid[@name = $topicid]
             or document($EXPORTFILE, /)//file[@name = $FILENAME]/topicid[@name = $topicid]/id[@name = $elemid])">
        <!-- just copy -->
        <xsl:copy>
          <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
            <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
            <xsl:with-param name="conref-filename" select="$conref-filename"/>
            <xsl:with-param name="topicid" select="$topicid"/>
            <xsl:with-param name="elemid" select="$elemid"/>
            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
            <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
            <xsl:with-param name="conref-ids" select="$conref-ids"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="current-element" select="."/>
        <!-- do as usual -->
        <xsl:variable name="topicpos" as="xs:string">
          <xsl:choose>
            <xsl:when test="starts-with(@conref, '#')">samefile</xsl:when>
            <xsl:when test="contains(@conref, '#')">otherfile</xsl:when>
            <xsl:otherwise>firstinfile</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:choose>
          <!-- If this conref has already been followed, stop to prevent an infinite loop -->
          <xsl:when test="$conref-ids = generate-id(.)">
            <xsl:apply-templates select="." mode="ditamsg:conrefLoop"/>
          </xsl:when>
          <!--targetting an element inside a topic-->
          <xsl:when test="dita-ot:has-element-id(@conref)">
            <xsl:choose>
              <xsl:when test="$topicpos = 'samefile'">
                <xsl:variable name="target" select="key('id', $elemid)[local-name() = $element][ancestor::*[contains(@class, ' topic/topic ')][1][@id = $topicid]]"/>
                <xsl:choose>
                  <xsl:when test="$target">
                    <xsl:apply-templates select="$target[1]" mode="conref-target">
                      <xsl:with-param name="source-attributes" as="xs:string*">
                        <xsl:choose>
                          <xsl:when test="exists($source-attributes)">
                            <xsl:sequence select="$source-attributes"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="get-source-attribute"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:with-param>
                      <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                      <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                      <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                      <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                      <xsl:with-param name="conrefend" select="$conrefend"/>
                      <xsl:with-param name="original-element" select="$original-element"/>
                      <xsl:with-param name="original-attributes" select="$original-attributes"/>
                    </xsl:apply-templates>
                    <xsl:if test="$target[2]">
                      <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:missing-conref-target-error"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$topicpos = 'otherfile'">
                <xsl:choose>
                  <xsl:when test="conref:isValid($domains)">
                    <xsl:for-each select="document($file, /)">
                      <xsl:variable name="target" select="key('id', $elemid)[local-name() = $element][ancestor::*[contains(@class, ' topic/topic ')][1][@id = $topicid]]"/>
                      <xsl:choose>
                        <xsl:when test="$target">
                          <xsl:apply-templates select="$target[1]" mode="conref-target">
                            <xsl:with-param name="source-attributes" as="xs:string*">
                              <xsl:choose>
                                <xsl:when test="exists($source-attributes)">
                                  <xsl:sequence select="$source-attributes"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="get-source-attribute">
                                    <xsl:with-param name="current-node" select="$current-element"/>
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                            <xsl:with-param name="conref-filename" select="$conref-filename"/>
                            <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                            <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                            <xsl:with-param name="conrefend" select="$conrefend"/>
                            <xsl:with-param name="original-element" select="$original-element"/>
                            <xsl:with-param name="original-attributes" select="$original-attributes"/>
                          </xsl:apply-templates>
                          <xsl:if test="$target[2]">
                            <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                          </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise/>
              <!--never happens - only other value is firstinfile, but we know there's a # in the conref so it's either samefile or otherfile-->
            </xsl:choose>
          </xsl:when>

          <!--targetting a topic-->
          <xsl:when test="contains(@class, ' topic/topic ')">
            <xsl:choose>
              <xsl:when test="$topicpos = 'samefile'">
                <xsl:variable name="target" select="key('id', $topicid)[contains(@class, ' topic/topic ')][local-name() = $element]"/>
                <xsl:choose>
                  <xsl:when test="$target">
                    <xsl:apply-templates select="$target[1]" mode="conref-target">
                      <xsl:with-param name="source-attributes" as="xs:string*">
                        <xsl:choose>
                          <xsl:when test="exists($source-attributes)">
                            <xsl:sequence select="$source-attributes"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="get-source-attribute"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:with-param>
                      <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                      <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                      <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                      <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                      <xsl:with-param name="conrefend" select="$conrefend"/>
                      <xsl:with-param name="original-element" select="$original-element"/>
                      <xsl:with-param name="original-attributes" select="$original-attributes"/>
                    </xsl:apply-templates>
                    <xsl:if test="$target[2]">
                      <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:missing-conref-target-error"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$topicpos = 'otherfile'">
                <xsl:choose>
                  <xsl:when test="conref:isValid($domains)">
                    <xsl:for-each select="document($file, /)">
                      <xsl:variable name="target" select="key('id', $topicid)[contains(@class, ' topic/topic ')][local-name() = $element]"/>
                      <xsl:choose>
                        <xsl:when test="$target">
                          <xsl:apply-templates select="$target[1]" mode="conref-target">
                            <xsl:with-param name="source-attributes" as="xs:string*">
                              <xsl:choose>
                                <xsl:when test="exists($source-attributes)">
                                  <xsl:sequence select="$source-attributes"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="get-source-attribute">
                                    <xsl:with-param name="current-node" select="$current-element"/>
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                            <xsl:with-param name="conref-filename" select="$conref-filename"/>
                            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                            <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                            <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                            <xsl:with-param name="conrefend" select="$conrefend"/>
                            <xsl:with-param name="original-element" select="$original-element"/>
                            <xsl:with-param name="original-attributes" select="$original-attributes"/>
                          </xsl:apply-templates>
                          <xsl:if test="$target[2]">
                            <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                          </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$topicpos = 'firstinfile'">
                <xsl:choose>
                  <xsl:when test="conref:isValid($domains)">
                    <xsl:for-each select="document($file, /)">
                      <xsl:variable name="target" select="//*[contains(@class, ' topic/topic ')][1][local-name() = $element]"/>
                      <xsl:choose>
                        <xsl:when test="$target">
                          <xsl:variable name="firstTopicId" select="$target/@id"/>
                          <xsl:choose>
                            <!-- if the first topic id is exported and transtype is eclipsehelp-->
                            <xsl:when test="$TRANSTYPE = 'eclipsehelp' and document($EXPORTFILE, $current-element)//file[@name = $FILENAME]/topicid[@name = $firstTopicId]">
                              <!-- just copy -->
                              <xsl:copy>
                                <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
                                  <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                                  <xsl:with-param name="conref-filename" select="$conref-filename"/>
                                  <xsl:with-param name="topicid" select="$topicid"/>
                                  <xsl:with-param name="elemid" select="$elemid"/>
                                  <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                                  <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                                  <xsl:with-param name="conref-ids" select="$conref-ids"/>
                                </xsl:apply-templates>
                              </xsl:copy>
                            </xsl:when>
                            <xsl:otherwise>
                              <!-- do the normal process -->
                              <xsl:apply-templates select="$target[1]" mode="conref-target">
                                <xsl:with-param name="source-attributes" as="xs:string*">
                                  <xsl:choose>
                                    <xsl:when test="exists($source-attributes)">
                                      <xsl:sequence select="$source-attributes"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                      <xsl:call-template name="get-source-attribute">
                                        <xsl:with-param name="current-node" select="$current-element"/>
                                      </xsl:call-template>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </xsl:with-param>
                                <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                                <xsl:with-param name="conref-filename" select="$conref-filename"/>
                                <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                                <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                                <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                                <xsl:with-param name="conrefend" select="$conrefend"/>
                                <xsl:with-param name="original-element" select="$original-element"/>
                                <xsl:with-param name="original-attributes" select="$original-attributes"/>
                              </xsl:apply-templates>
                              <xsl:if test="$target[2]">
                                <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                              </xsl:if>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise/>
              <!--never happens - only three possible values for topicpos, all are tested-->
            </xsl:choose>
          </xsl:when>

          <!--targetting a topicref from within a map-->
          <xsl:when test="contains(@class, ' map/topicref ')">
            <xsl:choose>
              <xsl:when test="$topicpos = 'samefile'">
                <xsl:variable name="target" select="key('id', $topicid)[contains(@class, ' map/topicref ')][local-name() = $element]"/>
                <xsl:choose>
                  <xsl:when test="$target">
                    <xsl:apply-templates select="$target[1]" mode="conref-target">
                      <xsl:with-param name="source-attributes" as="xs:string*">
                        <xsl:choose>
                          <xsl:when test="exists($source-attributes)">
                            <xsl:sequence select="$source-attributes"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="get-source-attribute"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:with-param>
                      <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                      <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                      <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                      <xsl:with-param name="conrefend" select="$conrefend"/>
                      <xsl:with-param name="original-element" select="$original-element"/>
                      <xsl:with-param name="original-attributes" select="$original-attributes"/>
                    </xsl:apply-templates>
                    <xsl:if test="$target[2]">
                      <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:missing-conref-target-error"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$topicpos = 'otherfile'">
                <xsl:choose>
                  <xsl:when test="conref:isValid($domains)">
                    <xsl:for-each select="document($file,  /)">
                      <xsl:variable name="target" select="key('id', $topicid)[contains(@class, ' map/topicref ')][local-name() = $element]"/>
                      <xsl:choose>
                        <xsl:when test="$target">
                          <xsl:apply-templates select="$target[1]" mode="conref-target">
                            <xsl:with-param name="source-attributes" as="xs:string*">
                              <xsl:choose>
                                <xsl:when test="exists($source-attributes)">
                                  <xsl:sequence select="$source-attributes"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="get-source-attribute">
                                    <xsl:with-param name="current-node" select="$current-element"/>
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                            <xsl:with-param name="conref-filename" select="$conref-filename"/>
                            <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                            <xsl:with-param name="conrefend" select="$conrefend"/>
                            <xsl:with-param name="original-element" select="$original-element"/>
                            <xsl:with-param name="original-attributes" select="$original-attributes"/>
                          </xsl:apply-templates>
                          <xsl:if test="$target[2]">
                            <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                          </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="ditamsg:malformedConrefInMap"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>

          <!--targetting anything else within a map (such as reltable)-->
          <xsl:when test="contains(/*/@class, ' map/map ')">
            <xsl:choose>
              <xsl:when test="$topicpos = 'samefile'">
                <xsl:variable name="target" select="key('id', $topicid)[local-name() = $element]"/>
                <xsl:choose>
                  <xsl:when test="$target">
                    <xsl:apply-templates select="($target)[1]" mode="conref-target">
                      <xsl:with-param name="source-attributes" as="xs:string*">
                        <xsl:choose>
                          <xsl:when test="exists($source-attributes)">
                            <xsl:sequence select="$source-attributes"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:call-template name="get-source-attribute"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:with-param>
                      <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                      <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                      <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                      <xsl:with-param name="conrefend" select="$conrefend"/>
                      <xsl:with-param name="original-element" select="$original-element"/>
                      <xsl:with-param name="original-attributes" select="$original-attributes"/>
                    </xsl:apply-templates>
                    <xsl:if test="($target)[2]">
                      <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:missing-conref-target-error"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$topicpos = 'otherfile'">
                <xsl:choose>
                  <xsl:when test="conref:isValid($domains)">
                    <xsl:for-each select="document($file, /)">
                      <xsl:variable name="target" select="key('id', $topicid)[local-name() = $element]"/>
                      <xsl:choose>
                        <xsl:when test="$target">
                          <xsl:apply-templates select="$target[1]" mode="conref-target">
                            <xsl:with-param name="source-attributes" as="xs:string*">
                              <xsl:choose>
                                <xsl:when test="exists($source-attributes)">
                                  <xsl:sequence select="$source-attributes"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:call-template name="get-source-attribute">
                                    <xsl:with-param name="current-node" select="$current-element"/>
                                  </xsl:call-template>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                            <xsl:with-param name="conref-filename" select="$conref-filename"/>
                            <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                            <xsl:with-param name="conrefend" select="$conrefend"/>
                            <xsl:with-param name="original-element" select="$original-element"/>
                            <xsl:with-param name="original-attributes" select="$original-attributes"/>
                          </xsl:apply-templates>
                          <xsl:if test="$target[2]">
                            <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                          </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="ditamsg:malformedConrefInMap"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>

          <!--targetting an element in a map, and the source is a element from a topic, add by wxzhang 20070605-->
          <xsl:when test="substring-after(@conref, '#') != ''">
            <xsl:choose>
              <xsl:when test="conref:isValid($domains)">
                <xsl:for-each select="document($file, /)">
                  <xsl:variable name="target" select="key('id', $topicid)[local-name() = $element]"/>
                  <xsl:choose>
                    <!-- to resolve the problem of conref from map to topic -->
                    <xsl:when test="$target">
                      <xsl:apply-templates select="$target[1]" mode="conref-target">
                        <xsl:with-param name="source-attributes" as="xs:string*">
                          <xsl:choose>
                            <xsl:when test="exists($source-attributes)">
                              <xsl:sequence select="$source-attributes"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:call-template name="get-source-attribute">
                                <xsl:with-param name="current-node" select="$current-element"/>
                              </xsl:call-template>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:with-param>
                        <xsl:with-param name="current-relative-path" select="concat($current-relative-path, $add-relative-path)"/>
                        <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                        <xsl:with-param name="conref-filename" select="$conref-filename"/>
                        <xsl:with-param name="conref-source-topicid" select="$conref-source-topic"/>
                        <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                        <xsl:with-param name="conrefend" select="$conrefend"/>
                        <xsl:with-param name="original-element" select="$original-element"/>
                        <xsl:with-param name="original-attributes" select="$original-attributes"/>
                      </xsl:apply-templates>
                      <xsl:if test="$target[2]">
                        <xsl:apply-templates select="." mode="ditamsg:duplicateConrefTarget"/>
                      </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:apply-templates select="$current-element" mode="ditamsg:missing-conref-target-error"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="ditamsg:domainMismatch"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="ditamsg:malformedConref"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- When an element is the target of a conref, treat everything the same as any other element EXCEPT the attributes.
     Create the current element, and add all attributes except @id. Then go back to the original element
     (passed in source-attributes). Process all attributes on that element, except @conref. They will
     replace values in the source. -->
  <xsl:template match="*" mode="conref-target">
    <xsl:param name="WORKDIR" as="xs:string"/>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:param name="conref-ids" as="xs:string*"/>
    <xsl:param name="source-attributes" as="xs:string*"/>
    <xsl:param name="current-relative-path" as="xs:string"/>
    <!-- File system path from original file to here -->
    <xsl:param name="conref-filename" as="xs:string?"/>
    <xsl:param name="conrefend" as="xs:string?"/>
    <xsl:param name="original-attributes" as="attribute()*"/>
    <xsl:param name="original-element"/>
    <xsl:variable name="topicid" as="xs:string?">
      <xsl:choose>
        <xsl:when test="contains(@class, ' topic/topic ')">
          <xsl:value-of select="@id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="ancestor::*[contains(@class, ' topic/topic ')][1]/@id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="elemid" as="xs:string?">
      <xsl:choose>
        <xsl:when test="contains(@class, ' topic/topic ')"/>
        <xsl:otherwise>
          <xsl:value-of select="@id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <!-- If for some bizarre reason you conref to another element that uses @conref, forget the original and continue here. -->
      <xsl:when test="@conref">
        <xsl:apply-templates select=".">
          <xsl:with-param name="original-element" select="$original-element"/>
          <xsl:with-param name="source-attributes" select="$source-attributes"/>
          <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
          <xsl:with-param name="conref-ids" select="$conref-ids"/>
          <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
          <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="{$original-element}">
          <xsl:apply-templates select="$original-attributes" mode="original-attributes"/>
          <xsl:for-each select="@*">
            <xsl:if test="not(name() = 'id') and not(name() = $source-attributes)">
              <xsl:choose>
                <xsl:when test="name() = 'href'">
                  <xsl:apply-templates select=".">
                    <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                    <xsl:with-param name="conref-filename" select="$conref-filename"/>
                    <xsl:with-param name="topicid" select="$topicid"/>
                    <xsl:with-param name="elemid" select="$elemid"/>
                    <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                    <xsl:with-param name="conref-ids" select="$conref-ids"/>
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:apply-templates select=".">
                    <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                    <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                    <xsl:with-param name="conref-ids" select="$conref-ids"/>
                  </xsl:apply-templates>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>

          </xsl:for-each>

          <!-- Continue processing this element as any other -->
          <xsl:apply-templates select="* | comment() | processing-instruction() | text()">
            <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
            <xsl:with-param name="conref-filename" select="$conref-filename"/>
            <xsl:with-param name="topicid" select="$topicid"/>
            <xsl:with-param name="elemid" select="$elemid"/>
            <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
            <xsl:with-param name="conref-ids" select="$conref-ids"/>
            <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="exists($conrefend)">
        <xsl:for-each select="following-sibling::*[following-sibling::*[@id = $conrefend] or self::*[@id = $conrefend]]">
          <xsl:choose>
            <xsl:when test="@conref">
              <xsl:apply-templates select=".">
                <xsl:with-param name="source-attributes" select="$source-attributes"/>
                <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                <xsl:with-param name="conref-ids" select="$conref-ids"/>
                <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy>
                <xsl:for-each select="@*">
                  <xsl:if test="not(local-name(.) = 'id')">
                    <xsl:choose>
                      <xsl:when test="name() = 'href'">
                        <!--@href need to update, not implement currently. @href may point to local part, but if @href pull into other file,
                      then @href couldn't work correctly. This is the reason why @href need to update. We leave it as the future work.-->
                        <xsl:apply-templates select=".">
                          <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                          <xsl:with-param name="conref-filename" select="$conref-filename"/>
                          <xsl:with-param name="topicid" select="$topicid"/>
                          <xsl:with-param name="elemid" select="$elemid"/>
                          <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                          <xsl:with-param name="conref-ids" select="$conref-ids"/>
                        </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:copy/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
                <xsl:apply-templates select="* | comment() | processing-instruction() | text()">
                  <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                  <xsl:with-param name="conref-filename" select="$conref-filename"/>
                  <xsl:with-param name="topicid" select="$topicid"/>
                  <xsl:with-param name="elemid" select="$elemid"/>
                  <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
                  <xsl:with-param name="conref-ids" select="$conref-ids"/>
                  <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
                </xsl:apply-templates>
              </xsl:copy>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

  <!-- Processing a copy of the original element, that used @conref: apply-templates on
     all of the attributes, though some may be filtered out. -->
  <xsl:template match="*" mode="original-attributes">
    <xsl:apply-templates select="@*" mode="original-attributes"/>
  </xsl:template>
  <xsl:template match="@*" mode="original-attributes">
    <xsl:if test="not(. = '')">
      <!-- XXX: Why ignore empty attribute value? -->
      <xsl:copy/>
    </xsl:if>
  </xsl:template>

  <!-- If an attribute is required, it must be specified on the original source element to avoid parsing errors.
     Such attributes should NOT be copied from the source. Conref should also not be copied. 
     NOTE: if a new specialized element requires attributes, it should be added here. -->

  <!-- DITA 1.1 added the key -dita-use-conref-target, which can be used on required attributes
     to be sure they do not override the same attribute on a target element. -->
  <xsl:template match="@*[. = '-dita-use-conref-target']" mode="original-attributes" priority="11"/>

  <xsl:template match="@conrefend" mode="original-attributes" priority="10"/>
  <xsl:template match="@xtrc | @xtrf" mode="original-attributes" priority="10"/>
  <xsl:template match="@conref" mode="original-attributes" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/image ')]/@href" mode="original-attributes" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/tgroup ')]/@cols" mode="original-attributes" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/boolean ')]/@state" mode="original-attributes" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/state ')]/@name" mode="original-attributes" priority="10"/>
  <xsl:template match="*[contains(@class, ' topic/state ')]/@value" mode="original-attributes" priority="10"/>
  <!-- topichead is specialized from topicref, and requires @navtitle -->
  <xsl:template match="*[contains(@class, ' mapgroup-d/topichead ')]/@navtitle" mode="original-attributes" priority="10"/>

  <xsl:template match="@href">
    <xsl:param name="current-relative-path" as="xs:string"/>
    <xsl:param name="conref-filename" as="xs:string?"/>
    <xsl:param name="topicid" as="xs:string?"/>
    <xsl:param name="elemid" as="xs:string?"/>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:param name="conref-ids" as="xs:string*"/>
    <xsl:attribute name="href">
      <xsl:choose>
        <xsl:when test="../@scope = 'external'">
          <xsl:value-of select="."/>
        </xsl:when>

        <xsl:when test="starts-with(., 'http://') or starts-with(., 'https://') or starts-with(., 'ftp://')">
          <xsl:value-of select="."/>
        </xsl:when>

        <xsl:when test="starts-with(., '#')">
          <xsl:choose>
            <xsl:when test="empty($conref-filename)">
              <!--in the local file -->
              <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
              <!--not in the local file -->
              <xsl:call-template name="generate-href">
                <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
                <xsl:with-param name="conref-filename" select="$conref-filename"/>
                <xsl:with-param name="topicid" select="$topicid"/>
                <xsl:with-param name="elemid" select="$elemid"/>
                <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
              </xsl:call-template>
              <!--experimental code -->
            </xsl:otherwise>
          </xsl:choose>

        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($current-relative-path, .)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@id">
    <xsl:param name="current-relative-path" as="xs:string"/>
    <xsl:param name="conref-filename" as="xs:string?"/>
    <xsl:param name="topicid" as="xs:string?"/>
    <xsl:param name="elemid" as="xs:string?"/>
    <xsl:attribute name="id">
      <xsl:choose>
        <xsl:when test="exists($conref-filename)">
          <xsl:value-of select="generate-id(..)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="generate-href">
    <xsl:param name="current-relative-path" as="xs:string"/>
    <xsl:param name="conref-filename" as="xs:string"/>
    <xsl:param name="topicid" as="xs:string?"/>
    <xsl:param name="elemid" as="xs:string?"/>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:variable name="conref-topicid" as="xs:string">
      <xsl:choose>
        <xsl:when test="empty($topicid)">
          <xsl:value-of select="//*[contains(@class, ' topic/topic ')][1]/@id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$topicid"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="href-topicid" select="dita-ot:get-topic-id(.)" as="xs:string?"/>
    <xsl:variable name="href-elemid" select="dita-ot:get-element-id(.)" as="xs:string?"/>
    <xsl:variable name="conref-gen-id" as="xs:string">
      <xsl:choose>
        <xsl:when test="empty($elemid) or $elemid = $href-elemid">
          <xsl:value-of select="generate-id(key('id', $conref-topicid)[contains(@class, ' topic/topic ')]//*[@id = $href-elemid])"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="generate-id(key('id', $conref-topicid)[contains(@class, ' topic/topic ')]//*[@id = $elemid]//*[@id = $href-elemid])"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="href-gen-id" as="xs:string">
      <xsl:variable name="topic" select="key('id', $href-topicid)"/>
      <xsl:value-of select="generate-id($topic[contains(@class, ' topic/topic ')]//*[@id = $href-elemid][generate-id(ancestor::*[contains(@class, ' topic/topic ')][1]) = generate-id($topic)])"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($conref-gen-id = '') or (not($conref-gen-id = $href-gen-id))">
        <!--href target is not in conref target -->
        <xsl:value-of select="$current-relative-path"/>
        <xsl:value-of select="$conref-filename"/>
        <xsl:value-of select="."/>
      </xsl:when>
      <xsl:when test="$conref-gen-id = $href-gen-id">
        <xsl:text>#</xsl:text>
        <xsl:value-of select="$conref-source-topicid"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="$conref-gen-id"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- 20061018: This template generalizes domain elements, if necessary,
               based on @domains on the original file.
     Example: Element <b class="+ topic/ph hi-d/b ">, domains="(topic hi-d)"
         Result: The "hi-d" domain is valid, leave <b> as <b>
     Example: Element <b class="+ topic/ph hi-d/b ">, domains="(topic pr-d)"
         Result: The "hi-d" domain is NOT valid, generalize <b> to <ph>
     Example: Element <light-b class="+ topic/ph hi-d/b shade/light-b ">, domains="(topic hi-d)"
         Result: The "shade" domain is NOT valid, but "hi-d" is, so generalize <light-b> to <b>
-->
  <xsl:template name="generalize-domain">
    <xsl:param name="class" select="normalize-space(substring-after(@class, '+'))"/>
    <xsl:param name="domains" select="$ORIGINAL-DOMAINS"/>
    <xsl:variable name="evaluateNext" as="xs:string?">
      <xsl:if test="substring-after($class, ' ') != ''">
        <xsl:call-template name="generalize-domain">
          <xsl:with-param name="class" select="substring-after($class, ' ')"/>
          <xsl:with-param name="domains" select="$domains"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$evaluateNext != ''">
        <xsl:value-of select="$evaluateNext"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="testModule" select="substring-before($class, '/')"/>
        <xsl:variable name="testElement" as="xs:string">
          <xsl:choose>
            <xsl:when test="contains($class, ' ')">
              <xsl:value-of select="substring-after(substring-before($class, ' '), '/')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="substring-after($class, '/')"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="contains($domains, concat(' ', $testModule, ')'))">
            <xsl:value-of select="$testElement"/>
          </xsl:when>
          <xsl:when test="$testModule = 'topic' or $testModule = 'map'">
            <xsl:value-of select="$testElement"/>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Match any domain element. This compares @domains in the current topic to @domains in the
     original topic:
     * If the domains match (as it would if all in the same topic), the element name stays the same
     * Otherwise, call generalize-domains. This ensures the element is valid in the result doc. -->
  <xsl:template match="*[starts-with(@class, '+ ')]">
    <xsl:param name="current-relative-path" as="xs:string" select="''"/>
    <xsl:param name="conref-filename" as="xs:string?"/>
    <xsl:param name="topicid" as="xs:string?"/>
    <xsl:param name="elemid" as="xs:string?"/>
    <xsl:param name="WORKDIR" as="xs:string">
      <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
    </xsl:param>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:param name="conref-ids" as="xs:string*" select="()"/>
    <xsl:variable name="domains" select="/*/@domains | /dita/*[@domains][1]/@domains"/>
    <xsl:variable name="generalizedName" as="xs:string">
      <xsl:choose>
        <xsl:when test="$domains = $ORIGINAL-DOMAINS">
          <xsl:value-of select="name()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="generalize-domain"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$generalizedName}">
      <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
        <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
        <xsl:with-param name="conref-filename" select="$conref-filename"/>
        <xsl:with-param name="topicid" select="$topicid"/>
        <xsl:with-param name="elemid" select="$elemid"/>
        <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
        <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
        <xsl:with-param name="conref-ids" select="$conref-ids"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:function name="conref:isValid" as="xs:boolean">
    <xsl:param name="domains" as="xs:string"/>
    <xsl:call-template name="checkValid">
      <xsl:with-param name="sourceDomains" select="normalize-space($ORIGINAL-DOMAINS)"/>
      <xsl:with-param name="targetDomains" select="normalize-space(concat('(topic) ', $domains))"/>
    </xsl:call-template>
  </xsl:function>

  <xsl:template name="checkValid" as="xs:boolean">
    <xsl:param name="sourceDomains"/>
    <xsl:param name="targetDomains"/>

    <!-- format the out -->
    <xsl:variable name="output" as="xs:string">
      <xsl:variable name="out" as="xs:string">
        <xsl:value-of>
          <xsl:for-each select="tokenize($sourceDomains, '\)\s*?')[not(starts-with(normalize-space(.), 'a'))]">
            <xsl:value-of select="concat(., ') ')"/>
          </xsl:for-each>
        </xsl:value-of>
      </xsl:variable>
      <xsl:value-of select="normalize-space($out)"/>
    </xsl:variable>

    <!-- break string into node-set -->
    <xsl:variable name="subDomains" select="reverse(tokenize($output, '\(|\)\s*?\(|\)'))"/>
    <!-- get domains value having constraints e.g [topic simpleSection-c]-->
    <xsl:variable name="constraints" select="$subDomains[contains(., '-c')]"/>
    <xsl:choose>
      <!-- no more constraints -->
      <xsl:when test="empty($constraints)">
        <xsl:sequence select="true()"/>
      </xsl:when>
      <xsl:otherwise>
        <!--get first item in the constraints node set-->
        <xsl:variable name="compareItem" select="$constraints[position() = 1]"/>
        <!-- format the item -->
        <!--e.g (topic hi-d basicHighlight-c)-->
        <xsl:variable name="constraintItem" select="concat('(', $compareItem, ')')"/>
        <!--find out what the original module is. e.g topic, hi-d-->
        <xsl:variable name="originalItem" select="tokenize($compareItem, ' ')[not(contains(., '-c'))]"/>
        <!-- if $compareItem is (topic shortdescReq-c task shortdescTaskReq-c), 
             we should remove the compatible values:shortdescReq-c-->
        <xsl:variable name="lastConstraint" select="tokenize($compareItem, ' ')[contains(., '-c')][position() = last()]"/>
        <!-- cast sequence to string for compare -->
        <xsl:variable name="module" select="normalize-space(string-join($originalItem, ' '))" as="xs:string"/>
        
        <!-- format the string topic hi-d remove tail space to (topic hi-d) -->
        <xsl:variable name="originalModule" select="concat('(', $module, ')')"/>
        <!--remove compatible constraints-->
        <xsl:variable name="editedConstraintItem" select="concat('(', $module, ' ', $lastConstraint)"/>
        <xsl:choose>
          <!-- If the target has constraint item (topic hi-d basicHighlight-c) and there is only one constraint mode left-->
          <!-- If the target has a value that begins with constraint item and there is only one constraint mode left-->
          <xsl:when test="(contains($targetDomains, $constraintItem) and count($constraints) = 1)
                       or (contains($targetDomains, $editedConstraintItem) and count($constraints) = 1) ">
            <xsl:sequence select="true()"/>
          </xsl:when>
          <xsl:when test="count($constraints) > 1 and (contains($targetDomains, $constraintItem) or 
                contains($targetDomains, $editedConstraintItem) ) ">
            <xsl:variable name="remainString" as="xs:string">
              <xsl:value-of>
                <xsl:for-each select="remove($constraints, 1)">
                  <xsl:value-of select="concat('s(', ., ')' , ' ')"/>
                </xsl:for-each>
              </xsl:value-of>
            </xsl:variable>
            <xsl:call-template name="checkValid">
              <xsl:with-param name="sourceDomains" select="normalize-space($remainString)"/>
              <xsl:with-param name="targetDomains" select="$targetDomains"/>
            </xsl:call-template>
          </xsl:when>
          <!--If the target does not have (topic hi-d) and (topic hi-d, continue to test #2-->
          <xsl:when test="not(contains($targetDomains, $originalModule)) and not(contains($targetDomains, substring-before($originalModule, ')' )))">
            <xsl:variable name="remainString" as="xs:string">
              <xsl:value-of>
                <xsl:for-each select="remove($constraints, 1)">
                  <xsl:value-of select="concat('s(', ., ')' , ' ')"/>
                </xsl:for-each>
              </xsl:value-of>
            </xsl:variable>
            <xsl:call-template name="checkValid">
              <xsl:with-param name="sourceDomains" select="normalize-space($remainString)"/>
              <xsl:with-param name="targetDomains" select="$targetDomains"/>
            </xsl:call-template>
          </xsl:when>
          <!--If the target topic has the original module (topic hi-d) but does not have constraintItem (topic hi-d basicHighlight-c)
              or If the target topic has the beginning original module (topic hi-d but does not have constraintItem (topic hi-d basicHighlight-c)-->
          <!--conref is not allowed  -->
          <xsl:when test="(contains($targetDomains, $originalModule) and not(contains($targetDomains, $constraintItem)))
                       or (contains($targetDomains, substring-before($originalModule, ')' )) and not(contains($targetDomains, $constraintItem)))">
            <xsl:sequence select="false()"/>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--copy everything else-->
  <xsl:template match="* | @* | comment() | processing-instruction() | text()">
    <xsl:param name="current-relative-path" as="xs:string" select="''"/>
    <xsl:param name="conref-filename" as="xs:string?"/>
    <xsl:param name="topicid" as="xs:string?"/>
    <xsl:param name="elemid" as="xs:string?"/>
    <xsl:param name="WORKDIR">
      <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
    </xsl:param>
    <xsl:param name="conref-source-topicid" as="xs:string?"/>
    <xsl:param name="conref-ids" as="xs:string*" select="()"/>
    <xsl:copy>
      <xsl:apply-templates select="* | @* | comment() | processing-instruction() | text()">
        <xsl:with-param name="current-relative-path" select="$current-relative-path"/>
        <xsl:with-param name="conref-filename" select="$conref-filename"/>
        <xsl:with-param name="topicid" select="$topicid"/>
        <xsl:with-param name="elemid" select="$elemid"/>
        <xsl:with-param name="WORKDIR" select="$WORKDIR"/>
        <xsl:with-param name="conref-source-topicid" select="$conref-source-topicid"/>
        <xsl:with-param name="conref-ids" select="$conref-ids"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="get-file-uri" as="xs:string">
    <xsl:param name="href" as="xs:string"/>
    <xsl:param name="file-prefix" as="xs:string"/>
    <xsl:value-of>
      <xsl:value-of select="$file-prefix"/>
      <xsl:choose>
        <xsl:when test="starts-with($href, '#')">
          <xsl:value-of select="$file-being-processed"/>
        </xsl:when>
        <xsl:when test="contains($href, '#')">
          <xsl:value-of select="substring-before($href, '#')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$href"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:value-of>
  </xsl:template>

  <xsl:template name="get-original-element" as="xs:string">
    <xsl:value-of select="local-name(.)"/>
  </xsl:template>

  <!-- If the target element does not exist, this template will be called to issue an error -->
  <xsl:template match="*" mode="ditamsg:missing-conref-target-error">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">010</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <!-- If an ID is duplicated, and there are 2 possible targets, issue a warning -->
  <xsl:template match="*" mode="ditamsg:duplicateConrefTarget">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">011</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <!-- Message is no longer used - appeared when domain mismatch prevented conref -->
  <xsl:template match="*" mode="ditamsg:domainMismatch">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">012</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <!-- If this conref has already been followed, stop to prevent an infinite loop -->
  <xsl:template match="*" mode="ditamsg:conrefLoop">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">013</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <!-- Following msg is used on topicref and map -->
  <xsl:template match="*" mode="ditamsg:malformedConrefInMap">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">014</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:malformedConref">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">015</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:parserUnsupported">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">062</xsl:with-param>
      <xsl:with-param name="msgsev">I</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
