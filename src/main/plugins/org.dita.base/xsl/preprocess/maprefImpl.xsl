<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2006 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->

<!-- This stylesheet is called twice during preprocessing:

     * In the "mapref" stage, applied to each .ditamap file
     * In the "keyref" stage, applied only to the root .ditamap file

     "mapref" processing behaves as follows:

     * Each .ditamap file independently and recursively inlines all of its descendant .ditamap files.
     * A .ditamap file will never inline another .ditamap file that was already processed by "mapref".
       * The DITA-OT uses a custom <xslt> Ant task (XsltModule.java) that avoids overwriting the
         original files until all files are processed.
     * $relative-path tracks the "current directory" as submaps are recursively inlined.
       * The initial value is '#none#', which is relative to the location of the $file-being-processed.
       * An empty string also represents the location of the $file-being-processed.
       * Other values are the relative paths of files being inlined, including the trailing '/'
         (to allow direct concatenation of subsequent path components).
     * $mapref-id-path contains the sequence of encountered map-reference node IDs.
       * If a previously encountered node ID is encountered again, there is a reference loop. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="3.0"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:mappull="http://dita-ot.sourceforge.net/ns/200704/mappull"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                exclude-result-prefixes="xs dita-ot mappull ditamsg">

  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>

  <xsl:param name="file-being-processed" as="xs:string"/>
  <xsl:param name="child-topicref-warning" as="xs:string" select="'true'"/>

  <!-- list of attributes that can be overidden. -->
  <xsl:variable name="special-atts" select="('href', 'copy-to', 'class', 'linking', 'toc', 'print', 'audience', 'product', 'platform', 'otherprops', 'props')" as="xs:string*"/>

  <!-- the xsl:key to get all maprefs in the document in order to get reltable -->
  <xsl:key name="reltable" match="//*[contains(@class, ' map/topicref ')]" use="@format"/>
  <xsl:key name="reltable" match="//*[contains(@class, ' map/topicref ')]" use="@dita-ot:orig-format"/>

  <!-- baseline identity transform -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()">
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@conref">
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:attribute name="conref">
      <xsl:choose>
        <!-- path is relative to $file-being-processed or is a bare fragment reference - use as-is -->
        <xsl:when test="$relative-path = ('#none#', '') or starts-with(.,'#')">
          <xsl:sequence select="."/>
        </xsl:when>
        <!-- path is relative to the current map/submap file - resolve it -->
        <xsl:otherwise>
          <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="*[not(contains(@class,' map/topicref '))]/@href">
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:attribute name="href">
      <xsl:choose>
        <!-- path is relative to the current map/submap file - resolve it -->
        <xsl:when test="not(contains(.,'://') or ../@scope = 'external' or $relative-path = ('#none#', ''))">
          <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
        </xsl:when>
        <!-- path is relative to $file-being-processed or is external - use as-is -->
        <xsl:otherwise>
          <xsl:sequence select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <!-- This template processes:

       * References to @scope=('peer', 'external') .ditamap files
       * In "keyref" stage, <submap> containers previously created by "mapref" stage

       The referenced map is not inlined, but templates are applied to the contents. -->
  <xsl:template match="*[contains(@class, ' map/topicref ')][(@format, @dita-ot:orig-format) = 'ditamap']
                        [empty(@href(: | @dita-ot:orig-href:)) or
                         (:@processing-role = 'resource-only' or:)
                         @scope = ('peer', 'external')]" priority="15">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()">
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <!-- This template processes:

       * References to local .ditamap files included for processing
         * @scope=('peer', 'external') references are handled by the preceding template

       The element is processed as follows:

       * The referenced map is inlined into a temporary <submap> container.
         * The <submap> is a specialization of <topicgroup>.
         * The <submap> is unwrapped at the end of preprocessing.
       * Templates are applied to the inlined map content.
         * This results in recursive inlining of any descendant map references.
       * The submap title is preserved in a <submap-title> element.
       * The submap metadata is preserved in a <submap-topicmeta-container> element. -->
  <xsl:template match="*[contains(@class, ' map/topicref ')][(@format, @dita-ot:orig-format) = 'ditamap']" priority="10">
    <xsl:param name="refclass" select="(@dita-ot:orig-class, @class)[1]" as="xs:string"/>
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:param name="referTypeFlag" as="xs:string">#none#</xsl:param>
 
    <xsl:variable name="href" select="(@href, @dita-ot:orig-href)[1]" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="generate-id(.) = $mapref-id-path">
        <!-- this element was already processed during earlier recursive processing - inform the user of a reference loop -->
        <xsl:call-template name="output-message">
          <xsl:with-param name="id" select="'DOTX053E'"/>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="$href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="updated-id-path" select="($mapref-id-path, generate-id(.))" as="xs:string*"/>
        <xsl:variable name="file" as="document-node()?">
          <xsl:variable name="fileurl" as="xs:string?">
            <xsl:variable name="WORKDIR" as="xs:string">
              <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="empty($href)"/>
              <xsl:when test="contains($href, '://')">
                <xsl:value-of select="$href"/>
              </xsl:when>
              <xsl:when test="starts-with($href, '#')">
                <xsl:value-of select="concat($WORKDIR, $file-being-processed)"/>
              </xsl:when>
              <xsl:when test="contains($href, '#')">
                <xsl:value-of select="concat($WORKDIR, substring-before($href, '#'))"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($WORKDIR, $href)"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:if test="exists($fileurl)">
            <xsl:sequence select="document($fileurl, /)"/>
          </xsl:if>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="empty($file)">
            <xsl:variable name="filename" as="xs:string?">
              <xsl:choose>
                <xsl:when test="empty($href)"/>
                <!-- resolve the file name, if the @href contains :// then don't do anything -->
                <xsl:when test="contains($href,'://')">
                  <xsl:value-of select="$href"/>
                </xsl:when>
                <xsl:when test="starts-with($href,'#')">
                  <xsl:value-of select="$file-being-processed"/>
                </xsl:when>
                <!-- if @href contains # get the part before # -->
                <xsl:when test="contains($href,'#')">
                  <xsl:value-of select="substring-before($href,'#')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$href"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="output-message">
              <xsl:with-param name="id" select="'DOTX031E'"/>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="$filename"/></xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="element-id" as="xs:string?">
              <xsl:if test="contains($href, '#')">
                <xsl:value-of select="substring-after($href, '#')"/>
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="target" as="element()?">
              <xsl:choose>
                <xsl:when test="exists($element-id)">
                  <xsl:sequence select="$file//*[@id = $element-id]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:sequence select="$file/*"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="targetTitleAndTopicmeta" as="element()*"
              select="$file/*/*[contains(@class,' topic/title ') or contains(@class,' map/topicmeta ')]"/>  <!-- submap title and topicmeta -->
            <xsl:variable name="maprefTopicmeta" as="element()?"
              select="*[contains(@class,' map/topicmeta ')]"/>  <!-- mapref topicmeta -->
            <xsl:variable name="contents" as="node()*">
              <xsl:choose>
                <xsl:when test="not(contains($href,'://') or empty($element-id) or $file/*[contains(@class,' map/map ')][@id = $element-id])">
                  <xsl:sequence select="$file//*[contains(@class,' map/topicref ')][@id = $element-id]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:sequence select="$file/*/*[contains(@class,' map/topicref ') or contains(@class,' map/navref ') or contains(@class,' map/anchor ')] |
                                        $file/*/processing-instruction()"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <!-- retain key definition as a separate element -->
            <xsl:if test="@keys">
              <keydef class="+ map/topicref mapgroup-d/keydef ditaot-d/keydef " processing-role="resource-only">
                <xsl:apply-templates select="@* except (@class | @processing-role | @href)"/>
                <xsl:if test="@href">
                  <xsl:choose>
                    <!-- path is relative to the current map/submap file - resolve it -->
                    <xsl:when test="$relative-path != '#none#'">
                      <xsl:attribute name="href" select="concat($relative-path, @href)"/>
                    </xsl:when>
                    <!-- path is relative to $file-being-processed - use as-is -->
                    <xsl:otherwise>
                      <xsl:apply-templates select="@href"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
                <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]"/>
              </keydef>
            </xsl:if>
            <!-- href and format need to be retained for keyref processing but must be put to an internal namespace to prevent other modules to interact with this element -->
            <submap class="+ map/topicref mapgroup-d/topicgroup ditaot-d/submap "
                    dita-ot:orig-href="{$href}"
                    dita-ot:orig-format="{(@format, @dita-ot:orig-format)[1]}"
                    dita-ot:orig-class="{(@class, @dita-ot:orig-class)[1]}">
              <xsl:attribute name="dita-ot:orig-href">
                <!-- if path is relative to the current map/submap file, remember it -->
                <xsl:if test="not($relative-path = ('#none#', ''))">
                  <xsl:value-of select="$relative-path"/>
                </xsl:if>
                <xsl:value-of select="$href"/>
              </xsl:attribute>
              <xsl:if test="@keyscope | $target[@keyscope and contains(@class, ' map/map ')]">
                <xsl:variable name="keyscope">
                  <xsl:value-of select="@keyscope"/>
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="$target[contains(@class, ' map/map ')]/@keyscope"/>
                </xsl:variable>
                <xsl:attribute name="keyscope" select="string-join(distinct-values(tokenize(normalize-space($keyscope), '\s+')), ' ')"/>
              </xsl:if>
              <xsl:apply-templates select="$target/@chunk"/>
              <xsl:apply-templates select="@* except (@class, @href, @dita-ot:orig-href, @format, @dita-ot:orig-format, @keys, @keyscope, @type)"/>
              <xsl:apply-templates select="$target/@*" mode="preserve-submap-attributes"/>
              <xsl:apply-templates select="$targetTitleAndTopicmeta" mode="preserve-submap-title-and-topicmeta">
                <xsl:with-param name="relative-path" tunnel="yes">
                  <xsl:choose>
                    <!-- path is relative to the current map/submap file - resolve it -->
                    <xsl:when test="not($relative-path = ('#none#', ''))">
                      <xsl:value-of select="$relative-path"/>
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="$href"/>
                      </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                    <!-- path is relative to $file-being-processed - use as-is -->
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="$href"/>
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
              </xsl:apply-templates>
              <xsl:apply-templates select="$maprefTopicmeta" mode="preserve-mapref-topicmeta"/>
              <xsl:apply-templates select="*[contains(@class, ' ditavalref-d/ditavalref ')]"/>
              <xsl:apply-templates select="$contents">
                <xsl:with-param name="refclass" select="$refclass"/>
                <xsl:with-param name="mapref-id-path" select="$updated-id-path"/>
                <xsl:with-param name="relative-path" tunnel="yes">
                  <xsl:choose>
                    <!-- path is relative to the current map/submap file - resolve it -->
                    <xsl:when test="not($relative-path = ('#none#', ''))">
                      <xsl:value-of select="$relative-path"/>
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="$href"/>
                      </xsl:call-template>
                    </xsl:when>
                    <!-- path is relative to $file-being-processed - use as-is -->
                    <xsl:otherwise>
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="$href"/>
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="referTypeFlag" select="'element'"/>
              </xsl:apply-templates>
            </submap>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$child-topicref-warning = 'true' and *[contains(@class, ' map/topicref ')]
                                                            [not(contains(@class, ' ditavalref-d/ditavalref '))]">
          <xsl:call-template name="output-message">
            <xsl:with-param name="id" select="'DOTX068W'"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- This template processes:

       * Non-.ditamap file references and elements, such as:
         * References to .dita files
         * References to other files (.pdf, .mp4, etc.)
         * <ditaval> references
         * <keydef> key definitions
         * Elements with no @href reference (such as <topicgroup> and <topichead>)
         * Subject scheme elements (such as <subjectdef> and <enumerationdef>)

       The element is processed as follows:

       * @href and @copy-to references are resolved relative to the current file being processed, if needed.
       * For first-level elements in submaps, @class inherits the value of any non-<mapref> submap reference element. -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]" priority="5">
    <!-- if we're not inheriting the enclosing submap's @class, then use this element's class -->
    <xsl:param name="refclass" select="@class"/>
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:param name="referTypeFlag" as="xs:string">#none#</xsl:param>
    <xsl:copy>
      <xsl:for-each select="@href | @copy-to">
        <xsl:choose>
          <!-- empty attribute value - delete the attribute -->
          <xsl:when test=". = ''"/>
          <!-- path is relative to $file-being-processed or is external - use as-is -->
          <xsl:when test="$relative-path = '#none#' or dita-ot:is-external(.)">
            <xsl:attribute name="{name()}" select="."/>
          </xsl:when>
          <!-- path is relative to the current map/submap file - resolve it -->
          <xsl:otherwise>
            <xsl:attribute name="{name()}" select="dita-ot:normalize-uri(concat($relative-path, .))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:attribute name="class">
        <xsl:choose>
          <!-- for first-level elements inside <mapref> submaps (and its specializations), keep the element's original @class value; don't inherit the <mapref> class -->
          <xsl:when test="contains($refclass, ' mapgroup-d/mapref ')">
           <xsl:value-of select="@class"/>
          </xsl:when>
          <!-- for first-level elements inside a more specific submap reference (like a <topicref> inside a <chapter> submap), inherit the submap reference's @class value -->
          <xsl:when test="not(contains(@class, substring($refclass, 3)))">
            <xsl:value-of select="$refclass"/>
          </xsl:when>
          <!-- not either special case above; just copy this element's @class value -->
          <xsl:otherwise>
            <xsl:value-of select="@class"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <!-- first-level elements inside a submap inherit the following attributes from the submap's root element
           (because the root submap element is dissolved and only the contents are inlined -->
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'linking'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'toc'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'print'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'audience'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'product'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'platform'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'otherprops'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'props'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'processing-role'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'importance'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'search'"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'rev'"/>
      </xsl:call-template>
      <xsl:apply-templates select="@*[not(local-name() = $special-atts)] | node()">
        <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
        <!-- don't propagate 'refclass'; it is used only for the first-level elements inside a submap -->
        <!-- don't propagate 'referTypeFlag'; it is used only for the first-level elements inside a submap -->
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <!-- when inlining a submap, inherit attributes from submap's root map element -->
  <xsl:template name="generate-attribute">
    <xsl:param name="referTypeFlag" as="xs:string">#none#</xsl:param>
    <xsl:param name="name"/>
    
    <xsl:choose>
      <!-- refer to an element in map file -->
      <xsl:when test="$referTypeFlag = 'element'">
        <xsl:copy-of select="ancestor-or-self::*[@*[local-name() = $name]][1]/@*[local-name() = $name]"/>
      </xsl:when>
      <!-- for sub element -->
      <xsl:otherwise>
        <xsl:copy-of select="@*[local-name() = $name]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' map/map ')]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
      <xsl:call-template name="gen-reltable"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="gen-reltable">
    <xsl:param name="relative-path" as="xs:string">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:apply-templates select="key('reltable','ditamap')" mode="mapref">
      <xsl:with-param name="relative-path" select="$relative-path"/>
      <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' map/topicref ')]" mode="mapref">
    <xsl:param name="relative-path" as="xs:string">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:variable name="linking" as="xs:string?">
      <xsl:choose>
        <xsl:when test="empty(@linking)">
          <xsl:call-template name="inherit">
            <xsl:with-param name="attrib">linking</xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@linking"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="generate-id(.) = $mapref-id-path">
        <!-- this element was already processed during earlier recursive processing - inform the user of a reference loop -->
        <xsl:call-template name="output-message">
          <xsl:with-param name="id" select="'DOTX053E'"/>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="not($linking='none') and @href and not(contains(@href,'#')) and not(@scope = 'peer')">
        <xsl:variable name="update-id-path" select="($mapref-id-path, generate-id(.))"/>
        <xsl:variable name="href" select="@href" as="xs:string?"/>
        <xsl:apply-templates select="document($href, /)/*[contains(@class,' map/map ')]" mode="#current">
          <xsl:with-param name="parentMaprefKeyscope" select="@keyscope" tunnel="yes"/>
          <xsl:with-param name="relative-path">
            <xsl:choose>
              <!-- path is relative to the current map/submap file - resolve it -->
              <xsl:when test="not($relative-path = '#none#' or $relative-path='')">
                <xsl:value-of select="$relative-path"/>
                <xsl:call-template name="find-relative-path">
                  <xsl:with-param name="remainingpath" select="@href"/>
                </xsl:call-template>
              </xsl:when>
              <!-- path is relative to $file-being-processed - use as-is -->
              <xsl:otherwise>
                <xsl:call-template name="find-relative-path">
                  <xsl:with-param name="remainingpath" select="@href"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
          <xsl:with-param name="mapref-id-path" select="$update-id-path"/>  <!-- propagate the updated mapref-id-path downward -->
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' map/map ')]" mode="mapref">
    <xsl:param name="relative-path" as="xs:string">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:param name="parentMaprefKeyscope" tunnel="yes" as="attribute()?"/>
    <xsl:apply-templates select="*[contains(@class,' map/reltable ')]" mode="reltable-copy">
      <xsl:with-param name="relative-path" select="$relative-path" tunnel="yes"/>
      <xsl:with-param name="keyscope" select="string-join((@keyscope, $parentMaprefKeyscope), ' ')"/>
    </xsl:apply-templates>
    <!--xsl:copy-of select="*[contains(@class,' map/reltable ')]"/-->
    <xsl:call-template name="gen-reltable">
      <xsl:with-param name="relative-path" select="$relative-path"/>
      <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="@*" mode="preserve-submap-attributes">
    <xsl:attribute name="dita-ot:submap-{local-name()}" select="."/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="preserve-submap-title-and-topicmeta">
    <submap-topicmeta class="+ map/topicmeta ditaot-d/submap-topicmeta ">
      <submap-title class="+ topic/navtitle ditaot-d/submap-title ">
        <xsl:apply-templates select="@*" mode="preserve-submap-attributes"/>
        <xsl:apply-templates/>
      </submap-title>
    </submap-topicmeta>
  </xsl:template>
  <xsl:template match="*[contains(@class,' map/topicmeta ')]" mode="preserve-submap-title-and-topicmeta">
    <submap-topicmeta-container class="+ topic/foreign ditaot-d/submap-topicmeta-container ">
      <xsl:apply-templates select="@*" mode="preserve-submap-attributes"/>
      <xsl:apply-templates/>
    </submap-topicmeta-container>
  </xsl:template>
  <xsl:template match="*[contains(@class,' map/topicmeta ')]" mode="preserve-mapref-topicmeta">
    <mapref-topicmeta-container class="+ topic/foreign ditaot-d/mapref-topicmeta-container ">
      <xsl:apply-templates/>
    </mapref-topicmeta-container>
  </xsl:template>

  <xsl:template match="*" mode="reltable-copy" priority="10">
    <xsl:param name="keyscope" as="xs:string?"/>
    <xsl:copy>
      <xsl:if test="$keyscope">
        <xsl:attribute name="keyscope" select="$keyscope"/>
      </xsl:if>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@* | node()" mode="reltable-copy">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@href | @copy-to" mode="reltable-copy">
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:attribute name="{name()}">
      <xsl:choose>
        <!-- path is relative to $file-being-processed - use as-is -->
        <xsl:when test="dita-ot:is-external(.) or $relative-path = ('#none#', '')">
          <xsl:value-of select="."/>
        </xsl:when>
        <!-- path is relative to the current map/submap file - resolve it -->
        <xsl:otherwise>
          <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@conref" mode="reltable-copy">
    <xsl:param name="relative-path" as="xs:string" tunnel="yes">#none#</xsl:param>
    <xsl:attribute name="conref">
      <xsl:choose>
        <!-- path is relative to the current map/submap file - resolve it -->
        <xsl:when test="not($relative-path = ('#none#', ''))">
          <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
        </xsl:when>
        <!-- path is relative to $file-being-processed - use as-is -->
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <!-- RDA: FUNCTIONS TO IMPROVE OVERRIDE CAPABILITIES FOR INHERITING ATTRIBUTES -->

  <!-- Original function: processing moved to matching templates with mode="mappull:inherit-attribute"
        Result is the same as in original code: if the attribute is present or inherited, return
        the inherited value; if it is not available in the ancestor-or-self tree, return #none# -->
  <xsl:template name="inherit">
    <xsl:param name="attrib"/>
    <xsl:apply-templates select="." mode="mappull:inherit-from-self-then-ancestor">
      <xsl:with-param name="attrib" select="$attrib"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Similar to the template above, but saves duplicated processing by setting the
        inherited attribute when the inherited value != #none# -->
  <xsl:template match="*" mode="mappull:inherit-and-set-attribute">
    <xsl:param name="attrib"/>
    <xsl:variable name="inherited-value">
      <xsl:apply-templates select="." mode="mappull:inherit-from-self-then-ancestor">
        <xsl:with-param name="attrib" select="$attrib"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:if test="$inherited-value!='#none#'">
      <xsl:attribute name="{$attrib}" select="$inherited-value"/>
    </xsl:if>
  </xsl:template>

  <!-- Same as above, but for @format only. Allows us to warn if the inherited value seems wrong. -->
  <xsl:template match="*" mode="mappull:inherit-and-set-format-attribute">
    <xsl:variable name="inherited-value">
      <xsl:apply-templates select="." mode="mappull:inherit-from-self-then-ancestor">
        <xsl:with-param name="attrib" select="'format'"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:if test="$inherited-value!='#none#'">
      <xsl:attribute name="format" select="$inherited-value"/>
    </xsl:if>
  </xsl:template>

  <!-- Match the attribute which we are trying to inherit.
        If an attribute should never inherit, add this template to an override:
        <xsl:template match="@attributeName" mode="mappull:inherit-attribute"/>
        If an attribute should never inherit for a specific element, add this to an override:
        <xsl:template match="*[contains(@class,' spec/elem ')]/@attributeName" mode="mappull:inherit-attribute"/>  -->
  <xsl:template match="@*" mode="mappull:inherit-attribute">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- Some elements should not pass an attribute to children, but they SHOULD set the
        attribute locally. If it is specified locally, use it. Otherwise, go to parent. This
        template should ONLY be called from the actual element that is trying to set attributes.
        For example, when <specialGroup format="group"> should keep @format locally, but should
        never pass that value to children. -->
  <xsl:template match="*" mode="mappull:inherit-from-self-then-ancestor">
    <xsl:param name="attrib"/>
    <xsl:variable name="attrib-here">
      <xsl:if test="@*[local-name()=$attrib]">
        <xsl:value-of select="@*[local-name()=$attrib]"/>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <!-- Any time the attribute is specified on this element, use it -->
      <xsl:when test="$attrib-here!=''">
        <xsl:value-of select="$attrib-here"/>
      </xsl:when>
      <!-- Otherwise, use normal inheritance fallback -->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="mappull:inherit-attribute">
          <xsl:with-param name="attrib" select="$attrib"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Match an element when trying to inherit an attribute. Put the value of the attribute in $attrib-here.
        * If the attribute is present and should be used ($attrib=here!=''), then use it
        * If we are at the root element, attribute can't be inherited, so return #none#
        * If in relcell: try to inherit from self, row, or column, then move to table
        * Anything else, move on to parent                                                     -->
  <xsl:template match="*" mode="mappull:inherit-attribute">
    <!--@importance|@linking|@toc|@print|@search|@format|@scope-->
    <xsl:param name="attrib"/>
    <xsl:variable name="attrib-here">
      <xsl:apply-templates select="@*[local-name()=$attrib]" mode="#current"/>
    </xsl:variable>
    <xsl:choose>
      <!-- Any time the attribute is specified on this element, use it -->
      <xsl:when test="$attrib-here!=''">
        <xsl:value-of select="$attrib-here"/>
      </xsl:when>
      <!-- If this is not the first time thru the map, all attributes are already inherited, so do not check ancestors -->
      <xsl:when test="/processing-instruction('reparse')">#none#</xsl:when>
      <!-- No ancestors left to check, so the value is not available. -->
      <xsl:when test="not(parent::*)">#none#</xsl:when>
      <!-- When in a relcell, check inheritance in this order: row, then colspec,
                then proceed normally with the table. The value is not specified here on the entry,
                or it would have been caught in the first xsl:when test. -->
      <xsl:when test="contains(@class,' map/relcell ')">
        <xsl:variable name="position" select="1+count(preceding-sibling::*)"/>
        <xsl:variable name="row">
          <xsl:apply-templates select=".." mode="mappull:inherit-one-level">
            <xsl:with-param name="attrib" select="$attrib"/>
          </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="colspec">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position ]"
                               mode="mappull:inherit-one-level">
            <xsl:with-param name="attrib" select="$attrib"/>
          </xsl:apply-templates>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$row!=''">
            <xsl:value-of select="$row"/>
          </xsl:when>
          <xsl:when test="$colspec!=''">
            <xsl:value-of select="$colspec"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="ancestor::*[contains(@class, ' map/reltable ')]" mode="#current">
              <xsl:with-param name="attrib" select="$attrib"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="#current">
          <xsl:with-param name="attrib" select="$attrib"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Check if an attribute can be inherited from a specific element, without
        looking at ancestors. For example, check if can inherit from relrow; next
        comes relcolspec, which is not in the normal inheritance order. -->
  <xsl:template match="*" mode="mappull:inherit-one-level">
    <xsl:param name="attrib"/>
    <xsl:if test="@*[local-name()=$attrib]">
      <xsl:value-of select="@*[local-name()=$attrib]"/>
    </xsl:if>
  </xsl:template>

  <!-- RDA: END FUNCTIONS TO IMPROVE OVERRIDE CAPABILITIES FOR INHERITING ATTRIBUTES -->

  <!-- Returns the directory path of a file reference (typically a relative path)
       * Given '/a/b/c.dita', returns '/a/b/'
       * Given 'a/b/c.dita', returns 'a/b/'
       * Given 'a/b/c.dita#fragment', returns 'a/b/'
       * Given 'c.dita', returns '' -->
  <xsl:template name="find-relative-path">
    <xsl:param name="remainingpath"/>
    <xsl:if test="contains($remainingpath,'/')">
      <xsl:value-of select="substring-before($remainingpath,'/')"/>
      <xsl:text>/</xsl:text>
      <xsl:call-template name="find-relative-path">
        <xsl:with-param name="remainingpath" select="substring-after($remainingpath,'/')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
