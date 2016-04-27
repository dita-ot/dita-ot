<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
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
    
    <xsl:sequence select="dita-ot:generate-id($element/ancestor::*[contains(@class, ' topic/topic ')][1]/@id, $id)"/>
  </xsl:function>

  <xsl:function name="dita-ot:generate-stable-id" as="xs:string">
    <xsl:param name="element" as="element()"/>
    
    <xsl:variable name="topic" select="$element/ancestor-or-self::*[contains(@class, ' topic/topic ')][1]" as="element()"/>
    <xsl:variable name="parent-element" select="$element/ancestor-or-self::*[@id][1][not(. is $topic)]" as="element()?"/>
    <xsl:variable name="closest" select="($parent-element, $topic)[1]" as="element()"/>
    <xsl:variable name="index" select="count($closest/descendant::*[local-name() = local-name($element)][. &lt;&lt; $element]) + 1" as="xs:integer"/>
    
    <xsl:sequence select="dita-ot:generate-id($topic/@id, string-join(($parent-element/@id, local-name($element), string($index)), $HTML_ID_SEPARATOR))"/>
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

  <xsl:function name="table:get-entry-colspec" as="element()">
    <xsl:param name="entry" as="element()"/>

    <xsl:sequence select="
      table:get-current-table($entry)/*/*[contains(@class, ' topic/colspec ')]
      [@colname eq $entry/@colname]
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

    <xsl:variable name="table" as="element()"
      select="simpletable:get-current-table($entry)"/>

    <xsl:sequence select="
      $table/@keycol and xs:integer($table/@keycol) eq count($entry/preceding-sibling::*) + 1
    "/>
  </xsl:function>

</xsl:stylesheet>
