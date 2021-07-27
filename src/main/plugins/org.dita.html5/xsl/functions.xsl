<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2016 Eero Helenius

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table">

  <xsl:variable name="HTML_ID_SEPARATOR" select="'__'"/>

  <xsl:function name="dita-ot:generate-html-id" as="xs:string">
    <xsl:param name="element" as="element()"/>
    
    <xsl:sequence
      select="if (exists($element/@id))
              then dita-ot:get-prefixed-id($element, $element/@id)
              else dita-ot:generate-stable-id($element)"/>
  </xsl:function>
  
  <xsl:function name="dita-ot:generate-id" as="xs:string">
    <xsl:param name="topic" as="xs:string?"/>
    <xsl:param name="element" as="xs:string?"/>
    
    <xsl:value-of select="string-join(($topic, $element), $HTML_ID_SEPARATOR)"/>
  </xsl:function>

  <xsl:function name="dita-ot:get-prefixed-id" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="id" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="contains($element/@class, ' topic/topic')">
        <xsl:value-of select="$element/@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="dita-ot:generate-id($element/ancestor::*[contains(@class, ' topic/topic ')][1]/@id, $id)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="dita-ot:generate-stable-id" as="xs:string">
    <xsl:param name="element" as="element()"/>
    
    <xsl:variable name="topic" select="$element/ancestor-or-self::*[contains(@class, ' topic/topic ')][1]" as="element()?"/>
    <xsl:choose>
      <xsl:when test="empty($topic)">
        <xsl:sequence select="generate-id($element)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="parent-element" select="$element/ancestor-or-self::*[@id][1][not(. is $topic)]" as="element()?"/>
        <xsl:variable name="closest" select="($parent-element, $topic)[1]" as="element()"/>
        <xsl:variable name="index" select="count($closest/descendant::*[local-name() = local-name($element)][. &lt;&lt; $element]) + 1" as="xs:integer"/>
        <xsl:sequence select="dita-ot:generate-id($topic/@id, string-join(($parent-element/@id, local-name($element), string($index)), $HTML_ID_SEPARATOR))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="table:is-tbody-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/entry ') and contains($el/../../@class, ' topic/tbody ')
    "/>
  </xsl:function>

  <xsl:function name="table:is-thead-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/entry ') and contains($el/../../@class, ' topic/thead ')
    "/>
  </xsl:function>

  <xsl:function name="table:get-current-table" as="element()">
    <xsl:param name="node" as="node()"/>

    <xsl:sequence select="
      $node/ancestor-or-self::*[contains(@class, ' topic/table ')][1]
    "/>
  </xsl:function>

  <xsl:function name="table:get-current-tgroup" as="element()">
    <xsl:param name="node" as="node()"/>

    <xsl:sequence select="
      $node/ancestor-or-self::*[contains(@class, ' topic/tgroup ')][1]
    "/>
  </xsl:function>

  <xsl:function name="table:is-row-header" as="xs:boolean">
    <xsl:param name="entry" as="element()"/>

    <xsl:sequence select="
      table:get-current-table($entry)/@rowheader eq 'firstcol'
      and xs:integer($entry/@dita-ot:x) eq 1
    "/>
  </xsl:function>

  <xsl:function name="table:get-entry-colspec" as="element()?">
    <xsl:param name="entry" as="element()"/>

    <xsl:sequence select="
      table:get-current-tgroup($entry)/*[contains(@class, ' topic/colspec ')]
      [@colname eq $entry/@colname]
    "/>
  </xsl:function>
  
  <xsl:function name="table:get-ending-colspec" as="element()?">
    <xsl:param name="entry" as="element()"/>
    
    <xsl:sequence select="
      table:get-current-tgroup($entry)/*[contains(@class, ' topic/colspec ')]
      [@colname eq $entry/@nameend]
      "/>
  </xsl:function>

  <xsl:function name="table:get-entry-align" as="attribute(align)?">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      ($el/@align,
       table:get-current-tgroup($el)/@align,
       table:get-entry-colspec($el)/@align)[1]
    "/>
  </xsl:function>

  <xsl:function name="table:get-entry-colsep" as="attribute(colsep)?">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      ($el/@colsep,
       table:get-entry-colspec($el)/@colsep,
       table:get-current-table($el)/@colsep,
       table:get-current-tgroup($el)/@colsep)[1]
    "/>
  </xsl:function>

  <xsl:function name="table:get-entry-rowsep" as="attribute(rowsep)?">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      ($el/@rowsep,
       table:get-entry-colspec($el)/@rowsep,
       table:get-current-table($el)/@rowsep,
       table:get-current-tgroup($el)/@rowsep)[1]
    "/>
  </xsl:function>
  
  <xsl:function name="table:find-entry-end-column" as="xs:integer">
    <xsl:param name="ctx" as="element()"/>
    <xsl:choose>
      <xsl:when test="$ctx/@nameend">
        <xsl:sequence select="xs:integer(table:get-ending-colspec($ctx)/@colnum)"/>
      </xsl:when>
      <xsl:when test="$ctx/@dita-ot:x">
        <xsl:sequence select="xs:integer($ctx/@dita-ot:x)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="count($ctx/preceding-sibling::*[contains(@class,' topic/entry ')])+1"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!-- Return true if an entry is entirely within the X or Y span of the header.
       If entry ends before the header, or starts after the header, no match, otherwise there is overlap. -->
  <xsl:function name="table:entry-within-range" as="xs:boolean">
    <xsl:param name="entrystart" as="xs:integer"/>
    <xsl:param name="entryend" as="xs:integer"/>
    <xsl:param name="headerstart" as="xs:integer"/>
    <xsl:param name="headerend" as="xs:integer"/>
    <xsl:sequence select="not($entryend lt $headerstart or $entrystart gt $headerend)"/>
  </xsl:function>
  
  <xsl:function name="table:get-matching-thead-headers" as="xs:string*">
    <xsl:param name="ctx" as="element()"/>
    <xsl:variable name="startposition"
                  select="if ($ctx/@dita-ot:x)
                          then xs:integer($ctx/@dita-ot:x)
                          else count($ctx/preceding-sibling::*[contains(@class,' topic/entry ')]) + 1"/>
    <xsl:variable name="endposition" select="table:find-entry-end-column($ctx)"/>
    <xsl:for-each select="$ctx/../../../*[contains(@class,' topic/thead ')]/*[contains(@class,' topic/row ')]/*[contains(@class,' topic/entry ')]">
      <xsl:variable name="headstart"
                    select="if (@dita-ot:x)
                            then xs:integer(@dita-ot:x)
                            else count(preceding-sibling::*[contains(@class,' topic/entry ')]) + 1"/>
      <xsl:variable name="headend" select="table:find-entry-end-column(.)"/>
      <xsl:if test="table:entry-within-range($startposition, $endposition, $headstart, $headend)">
        <xsl:value-of select="dita-ot:generate-html-id(.)"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>
  
  <xsl:function name="table:get-matching-row-headers" as="xs:string*">
    <xsl:param name="ctx" as="element()"/>
    <xsl:if test="table:get-current-table($ctx)/@rowheader='firstcol' and 
                  $ctx/@dita-ot:x != '1' and
                  not(table:is-thead-entry($ctx))">
      <xsl:variable name="startposition"
                    select="if ($ctx/@dita-ot:y)
                            then xs:integer($ctx/@dita-ot:y)
                            else count($ctx/parent::*/preceding-sibling::*[contains(@class,' topic/row ')]) + 1"/>
      <xsl:variable name="endposition"
                    select="if ($ctx/@morerows)
                            then ($startposition + xs:integer($ctx/@morerows))
                            else $startposition"/>
      <xsl:choose>
        <xsl:when test="($startposition = $endposition) and $ctx/preceding-sibling::*[contains(@class,' topic/entry ')][@dita-ot:x = '1']">
          <!-- Quick result for common simplest case: no spanning and first-col row header is in this row -->
          <xsl:value-of select="dita-ot:generate-html-id($ctx/preceding-sibling::*[contains(@class,' topic/entry ')][@dita-ot:x ='1'])"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$ctx/../../*[contains(@class,' topic/row ')]/*[contains(@class,' topic/entry ')][@dita-ot:x='1']">
            <xsl:variable name="headstart" select="xs:integer(@dita-ot:y)"/>
            <xsl:variable name="headend" select="if (@morerows) then ($headstart + xs:integer(@morerows)) else $headstart"/>
            <xsl:if test="table:entry-within-range($startposition, $endposition, $headstart, $headend)">
              <xsl:value-of select="dita-ot:generate-html-id(.)"/>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:function>

  <xsl:function name="simpletable:is-body-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/stentry ') and contains($el/../@class, ' topic/strow ')
    "/>
  </xsl:function>

  <xsl:function name="simpletable:is-head-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/stentry ') and contains($el/../@class, ' topic/sthead ')
    "/>
  </xsl:function>

  <xsl:function name="simpletable:get-current-table" as="element()">
    <xsl:param name="node" as="node()"/>

    <xsl:sequence select="
      $node/ancestor-or-self::*[contains(@class, ' topic/simpletable ')][1]
    "/>
  </xsl:function>

  <xsl:function name="simpletable:is-keycol-entry" as="xs:boolean">
    <xsl:param name="entry" as="element()"/>

    <xsl:variable name="keycol" as="xs:integer?"
      select="simpletable:get-current-table($entry)/@keycol/xs:integer(.)"/>

    <xsl:sequence select="$keycol = $entry/@dita-ot:x/xs:integer(.)"/>
  </xsl:function>
  
  <xsl:function name="dita-ot:normalize-href" as="xs:string?">
    <xsl:param name="href" as="xs:string"/>
    <xsl:value-of select="replace(translate($href, '\', '/'), ' ', '%20')"/>
  </xsl:function>

</xsl:stylesheet>
