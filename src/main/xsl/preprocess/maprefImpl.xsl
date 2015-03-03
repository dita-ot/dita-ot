<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
    <!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:mappull="http://dita-ot.sourceforge.net/ns/200704/mappull"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                exclude-result-prefixes="xs dita-ot mappull ditamsg">

  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="../common/output-message.xsl"/>
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:param name="file-being-processed"/>

  <!-- list of attributes that can be overided. -->
  <xsl:variable name="special-atts" select="('href', 'copy-to', 'class', 'linking', 'toc', 'print', 'audience', 'product', 'platform', 'otherprops', 'props')" as="xs:string*"/>

  <!-- the xsl:key to get all maprefs in the document in order to get reltable -->
  <xsl:key name="reltable" match="//*[contains(@class, ' map/topicref ')]" use="@format"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicref ')][@format = 'ditamap'][@scope = ('peer', 'external')]" priority="15">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicref ')][@format = 'ditamap']" priority="10">
    <xsl:param name="refclass" select="@class"/>
    <!-- get the current element's @class value -->
    <xsl:param name="relative-path">#none#</xsl:param>
    <!-- need this to resolve multiple mapref -->
    <xsl:param name="parent-linking">#none#</xsl:param>
    <xsl:param name="parent-toc">#none#</xsl:param>
    <xsl:param name="parent-print">#none#</xsl:param>
    <xsl:param name="parent-audience">#none#</xsl:param>
    <xsl:param name="parent-product">#none#</xsl:param>
    <xsl:param name="parent-platform">#none#</xsl:param>
    <xsl:param name="parent-otherprops">#none#</xsl:param>
    <xsl:param name="parent-props">#none#</xsl:param>
    <xsl:param name="parent-processing-role">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <!-- record each target's id of mapref to prevent loop reference -->
    <!-- params to tell refer type:whole map file or just a branch -->
    <xsl:param name="referTypeFlag">#none#</xsl:param>
    <xsl:param name="parent-importance">#none#</xsl:param>
    <xsl:param name="parent-search">#none#</xsl:param>
    <xsl:param name="parent-rev">#none#</xsl:param>

    <xsl:choose>
      <xsl:when test="generate-id(.) = $mapref-id-path">
        <!-- it is mapref but it didn't pass the loop dependency check -->
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">053</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="filename">
          <xsl:choose>
            <!-- resolve the file name, if the @href contains :// then don't do anything -->
            <xsl:when test="contains(@href,'://')">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:when test="starts-with(@href,'#')">
              <xsl:value-of select="$file-being-processed"/>
            </xsl:when>
            <!-- if @href contains # get the part before # -->
            <xsl:when test="contains(@href,'#')">
              <xsl:value-of select="substring-before(@href,'#')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="element-id">
          <xsl:choose>
            <xsl:when test="contains(@href,'#')">
              <xsl:value-of select="substring-after(@href,'#')"/>
            </xsl:when>
            <xsl:otherwise>#none#</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="WORKDIR">
          <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
        </xsl:variable>
        <!-- update mapref id path -->
        <xsl:variable name="updated-id-path" select="($mapref-id-path, generate-id(.))"/>
        <!-- get the file handle by getting workdir and use document() to get the file -->
        <xsl:variable name="fileurl-origin">
          <xsl:choose>
            <xsl:when test="contains(@href,'://')">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="starts-with(@href,'#')">
                  <xsl:value-of select="concat($WORKDIR, $file-being-processed)"/>
                </xsl:when>
                <xsl:when test="contains(@href, '#')">
                  <xsl:value-of select="concat($WORKDIR, substring-before(@href, '#'))"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat($WORKDIR, @href)"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="fileurl">
          <xsl:call-template name="replace-blank">
            <xsl:with-param name="file-origin" select="$fileurl-origin"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="file" select="document($fileurl,/)"/>
        <xsl:choose>
          <!-- verify the validity of $file -->
          <xsl:when test="empty($file) or empty($file/*/*)">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">031</xsl:with-param>
              <xsl:with-param name="msgsev">E</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="$filename"/></xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="contents" as="node()*">
              <xsl:choose>
                <!-- see whether it is reference to a file or a reference to specific element -->
                <xsl:when test="not(contains(@href,'://') or $element-id='#none#' or $file/*[contains(@class,' map/map ')][@id = $element-id])">
                  <!-- reference to an element -->
                  <xsl:sequence select="$file//*[contains(@class,' map/topicref ')][@id=$element-id]"/>
                </xsl:when>
                <xsl:otherwise>
                  <!-- reference to file -->
                  <xsl:sequence select="$file/*/*[contains(@class,' map/topicref ')] | $file/*/processing-instruction()"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <submap class="+ map/topicref mapgroup-d/topicgroup ditaot-d/submap ">
              <xsl:apply-templates select="$contents">
                <xsl:with-param name="refclass" select="$refclass"/>
                <xsl:with-param name="mapref-id-path" select="$updated-id-path"/>
                <xsl:with-param name="relative-path">
                  <xsl:choose>
                    <xsl:when test="not($relative-path='#none#' or $relative-path='')">
                      <xsl:value-of select="$relative-path"/>
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="@href"/>
                      </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="find-relative-path">
                        <xsl:with-param name="remainingpath" select="@href"/>
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-linking">
                  <xsl:choose>
                    <xsl:when test="not($parent-linking='#none#')">
                      <xsl:value-of select="$parent-linking"/>
                    </xsl:when>
                    <xsl:when test="@linking and not(@linking='')">
                      <xsl:value-of select="@linking"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-toc">
                  <xsl:choose>
                    <xsl:when test="not($parent-toc='#none#')">
                      <xsl:value-of select="$parent-toc"/>
                    </xsl:when>
                    <xsl:when test="@toc and not(@toc='')">
                      <xsl:value-of select="@toc"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-print">
                  <xsl:choose>
                    <xsl:when test="not($parent-print='#none#')">
                      <xsl:value-of select="$parent-print"/>
                    </xsl:when>
                    <xsl:when test="@print and not(@print='')">
                      <xsl:value-of select="@print"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-audience">
                  <xsl:choose>
                    <xsl:when test="not($parent-audience='#none#')">
                      <xsl:value-of select="$parent-audience"/>
                    </xsl:when>
                    <xsl:when test="@audience and not(@audience='')">
                      <xsl:value-of select="@audience"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-product">
                  <xsl:choose>
                    <xsl:when test="not($parent-product='#none#')">
                      <xsl:value-of select="$parent-product"/>
                    </xsl:when>
                    <xsl:when test="@product and not(@product='')">
                      <xsl:value-of select="@product"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-platform">
                  <xsl:choose>
                    <xsl:when test="not($parent-platform='#none#')">
                      <xsl:value-of select="$parent-platform"/>
                    </xsl:when>
                    <xsl:when test="@platform and not(@platform='')">
                      <xsl:value-of select="@platform"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-otherprops">
                  <xsl:choose>
                    <xsl:when test="not($parent-otherprops='#none#')">
                      <xsl:value-of select="$parent-otherprops"/>
                    </xsl:when>
                    <xsl:when test="@otherprops and not(@otherprops='')">
                      <xsl:value-of select="@otherprops"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-props">
                  <xsl:choose>
                    <xsl:when test="not($parent-props='#none#')">
                      <xsl:value-of select="$parent-props"/>
                    </xsl:when>
                    <xsl:when test="@props and not(@props='')">
                      <xsl:value-of select="@props"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="parent-processing-role">
                  <xsl:choose>
                    <xsl:when test="not($parent-processing-role='#none#')">
                      <xsl:value-of select="$parent-processing-role"/>
                    </xsl:when>
                    <xsl:when test="@processing-role and not(@processing-role='')">
                      <xsl:value-of select="@processing-role"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="referTypeFlag" select="'element'"/>
                <!-- importance -->
                <xsl:with-param name="parent-importance">
                  <xsl:choose>
                    <xsl:when test="not($parent-importance='#none#')">
                      <xsl:value-of select="$parent-importance"/>
                    </xsl:when>
                    <xsl:when test="@importance and not(@importance='')">
                      <xsl:value-of select="@importance"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <!-- search -->
                <xsl:with-param name="parent-search">
                  <xsl:choose>
                    <xsl:when test="not($parent-search='#none#')">
                      <xsl:value-of select="$parent-search"/>
                    </xsl:when>
                    <xsl:when test="@search and not(@search='')">
                      <xsl:value-of select="@search"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
                <!-- rev -->
                <xsl:with-param name="parent-rev">
                  <xsl:choose>
                    <xsl:when test="not($parent-rev='#none#')">
                      <xsl:value-of select="$parent-rev"/>
                    </xsl:when>
                    <xsl:when test="@rev and not(@rev='')">
                      <xsl:value-of select="@rev"/>
                    </xsl:when>
                    <xsl:otherwise>#none#</xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
              </xsl:apply-templates>
            </submap>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="*[contains(@class, ' map/topicref ')]">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">068</xsl:with-param>
            <xsl:with-param name="msgsev">W</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/topicref ')]" priority="5">
    <xsl:param name="refclass" select="@class"/>
    <!-- get the current element's @class value -->
    <xsl:param name="relative-path">#none#</xsl:param>
    <!-- need this to resolve multiple mapref -->
    <xsl:param name="parent-linking">#none#</xsl:param>
    <xsl:param name="parent-toc">#none#</xsl:param>
    <xsl:param name="parent-print">#none#</xsl:param>
    <xsl:param name="parent-audience">#none#</xsl:param>
    <xsl:param name="parent-product">#none#</xsl:param>
    <xsl:param name="parent-platform">#none#</xsl:param>
    <xsl:param name="parent-otherprops">#none#</xsl:param>
    <xsl:param name="parent-props">#none#</xsl:param>
    <xsl:param name="parent-processing-role">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <!-- record each target's id of mapref to prevent loop reference -->
    <!-- params to tell refer type:whole map file or just a branch -->
    <xsl:param name="referTypeFlag">#none#</xsl:param>
    <xsl:param name="parent-importance">#none#</xsl:param>
    <xsl:param name="parent-search">#none#</xsl:param>
    <xsl:param name="parent-rev">#none#</xsl:param>

    <xsl:copy>
      <xsl:for-each select="@href | @copy-to">
        <xsl:choose>
          <xsl:when test=". = ''"/>
          <xsl:when test="$relative-path = '#none#' or contains(.,'://')">
            <xsl:attribute name="{name()}">
              <xsl:value-of select="."/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
            </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="contains($refclass, ' mapgroup-d/mapref ')">
           <xsl:value-of select="@class"/>
          </xsl:when>
          <!-- if the element is not at the top level of reference target, @class equals to $refclass -->
          <xsl:when test="not(contains(@class,substring($refclass, 3)))">
            <xsl:value-of select="$refclass"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@class"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <!-- linking and following attributes processed in the same way -->
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'linking'"/>
        <xsl:with-param name="parent-value" select="$parent-linking"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'toc'"/>
        <xsl:with-param name="parent-value" select="$parent-toc"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'print'"/>
        <xsl:with-param name="parent-value" select="$parent-print"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'audience'"/>
        <xsl:with-param name="parent-value" select="$parent-audience"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'product'"/>
        <xsl:with-param name="parent-value" select="$parent-product"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'platform'"/>
        <xsl:with-param name="parent-value" select="$parent-platform"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'otherprops'"/>
        <xsl:with-param name="parent-value" select="$parent-otherprops"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'props'"/>
        <xsl:with-param name="parent-value" select="$parent-props"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'processing-role'"/>
        <xsl:with-param name="parent-value" select="$parent-processing-role"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'importance'"/>
        <xsl:with-param name="parent-value" select="$parent-importance"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'search'"/>
        <xsl:with-param name="parent-value" select="$parent-search"/>
      </xsl:call-template>
      <xsl:call-template name="generate-attribute">
        <xsl:with-param name="referTypeFlag" select="$referTypeFlag"/>
        <xsl:with-param name="name" select="'rev'"/>
        <xsl:with-param name="parent-value" select="$parent-rev"/>
      </xsl:call-template>
      <xsl:apply-templates select="@*[not(local-name() = $special-atts)] | node()">
        <xsl:with-param name="relative-path" select="$relative-path"/>
        <!-- pass the relative-path to sub elements -->
        <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
        <!-- pass the mapref-id-path to sub elements -->
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template name="generate-attribute">
    <!-- params to tell refer type:whole map file or just a branch -->
    <xsl:param name="referTypeFlag">#none#</xsl:param>
    <!-- need this to resolve multiple mapref -->
    <xsl:param name="parent-value">#none#</xsl:param>
    <xsl:param name="name"/>
    
    <xsl:variable name="current-attr" select="@*[local-name() = $name]" as="attribute()?"/>
    <xsl:choose>
      <!-- refer to a map file -->
      <xsl:when test="$referTypeFlag = 'file'">
        <xsl:choose>
          <!-- first use local attribute -->
          <xsl:when test="$current-attr and not($current-attr = '')">
            <xsl:attribute name="{$name}">
              <xsl:value-of select="$current-attr"/>
            </xsl:attribute>
          </xsl:when>
          <!-- second use attribute in referencing file-->
          <xsl:when test="not($parent-value='#none#')">
            <xsl:attribute name="{$name}">
              <xsl:value-of select="$parent-value"/>
            </xsl:attribute>
          </xsl:when>
          <!-- third use attribute set on map tag -->
          <xsl:when test="ancestor-or-self::*[contains(@class, ' map/map ')]/@*[local-name() = $name]">
            <xsl:attribute name="{$name}">
              <xsl:value-of select="ancestor-or-self::*[contains(@class, ' map/map ')]/@*[local-name() = $name]"/>
            </xsl:attribute>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <!-- refer to an element in map file -->
      <xsl:when test="$referTypeFlag = 'element'">
        <!-- second use attribute in referencing file to override local one-->
        <xsl:choose>
          <xsl:when test="not($parent-value='#none#')">
            <xsl:attribute name="{$name}">
              <xsl:value-of select="$parent-value"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:when test="$current-attr and not($current-attr = '')">
            <xsl:attribute name="{$name}">
              <xsl:value-of select="$current-attr"/>
            </xsl:attribute>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <!-- for sub element -->
      <xsl:otherwise>
        <xsl:if test="$current-attr and not($current-attr = '')">
          <xsl:attribute name="{$name}">
            <xsl:value-of select="$current-attr"/>
          </xsl:attribute>
        </xsl:if>
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
    <xsl:param name="relative-path">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:apply-templates select="key('reltable','ditamap')" mode="mapref">
      <xsl:with-param name="relative-path" select="$relative-path"/>
      <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="*[contains(@class,' map/topicref ')]" mode="mapref">
    <xsl:param name="relative-path">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:variable name="linking">
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
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">053</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
          <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="not($linking='none') and @href and not(contains(@href,'#'))">
        <xsl:variable name="update-id-path" select="($mapref-id-path, generate-id(.))"/>
        <xsl:variable name="href">
          <xsl:call-template name="replace-blank">
            <xsl:with-param name="file-origin" select="@href"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:apply-templates select="document($href,/)/*[contains(@class,' map/map ')]" mode="mapref">
          <xsl:with-param name="relative-path">
            <xsl:choose>
              <xsl:when test="not($relative-path='#none#' or $relative-path='')">
                <xsl:value-of select="$relative-path"/>
                <xsl:call-template name="find-relative-path">
                  <xsl:with-param name="remainingpath" select="@href"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="find-relative-path">
                  <xsl:with-param name="remainingpath" select="@href"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
          <xsl:with-param name="mapref-id-path" select="$update-id-path"/>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' map/map ')]" mode="mapref">
    <xsl:param name="relative-path">#none#</xsl:param>
    <xsl:param name="mapref-id-path" as="xs:string*"/>
    <xsl:apply-templates select="*[contains(@class,' map/reltable ')]" mode="reltable-copy">
      <xsl:with-param name="relative-path" select="$relative-path"/>
    </xsl:apply-templates>
    <!--xsl:copy-of select="*[contains(@class,' map/reltable ')]"/-->
    <xsl:call-template name="gen-reltable">
      <xsl:with-param name="relative-path" select="$relative-path"/>
      <xsl:with-param name="mapref-id-path" select="$mapref-id-path"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="@* | node()" mode="reltable-copy">
    <xsl:param name="relative-path">#none#</xsl:param>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="reltable-copy">
        <xsl:with-param name="relative-path" select="$relative-path"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@href | @copy-to" mode="reltable-copy">
    <xsl:param name="relative-path">#none#</xsl:param>
    <xsl:attribute name="{name()}">
      <xsl:choose>
        <xsl:when test="not(contains(.,'://') or ../@scope = 'external' or $relative-path = ('#none#', ''))">
          <xsl:value-of select="dita-ot:normalize-uri(concat($relative-path, .))"/>
        </xsl:when>
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
      <xsl:attribute name="{$attrib}">
        <xsl:value-of select="$inherited-value"/>
      </xsl:attribute>
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
      <xsl:attribute name="format">
        <xsl:value-of select="$inherited-value"/>
      </xsl:attribute>
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
      <xsl:apply-templates select="@*[local-name()=$attrib]" mode="mappull:inherit-attribute"/>
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
            <xsl:apply-templates select="ancestor::*[contains(@class, ' map/reltable ')]" mode="mappull:inherit-attribute">
              <xsl:with-param name="attrib" select="$attrib"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="mappull:inherit-attribute">
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

  <!-- Find the relative path to another topic or map -->
  <xsl:template name="find-relative-path">
    <xsl:param name="remainingpath"/>
    <xsl:if test="contains($remainingpath,'/')">
      <xsl:value-of select="substring-before($remainingpath,'/')"/>/<xsl:text/>
      <xsl:call-template name="find-relative-path">
        <xsl:with-param name="remainingpath" select="substring-after($remainingpath,'/')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
