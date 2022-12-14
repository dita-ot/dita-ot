<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <!-- Logical containers -->
  
  <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ') or contains(@class,' syntaxdiagram-d/syntaxdiagram ')]">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setidaname"/>
      <xsl:apply-templates mode="process-syntaxdiagram"/>
    </div>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/fragment ') or contains(@class,' syntaxdiagram-d/fragment ')]" mode="process-syntaxdiagram">
    <div>
      <a name="{*[contains(@class,' topic/title ')]}"> </a>
      <xsl:apply-templates mode="#current"/>
    </div>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/synblk ')]" mode="process-syntaxdiagram">
    <span>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates select="@id"/>
      <xsl:call-template name="apply-for-phrases"/>
    </span>
  </xsl:template>

  <!-- titles for logical containers -->
  
  <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ') or contains(@class,' syntaxdiagram-d/syntaxdiagram ')]/
                       *[contains(@class,' topic/title ')]"
                mode="process-syntaxdiagram">
    <h3>
      <xsl:value-of select="."/>
    </h3>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/fragment ') or contains(@class,' syntaxdiagram-d/fragment ')]/
                       *[contains(@class,' topic/title ')]" 
                mode="process-syntaxdiagram">
    <h4><xsl:apply-templates mode="#current"/></h4>
  </xsl:template>
  
  
  <!-- This should test to see if there's a fragment with matching title 
  and if so, produce an associative link. -->
  <xsl:template match="*[contains(@class,' pr-d/fragref ') or contains(@class,' syntaxdiagram-d/fragref ')]" mode="process-syntaxdiagram">
    <kbd>
        <a href="#{.}">
    &lt;<xsl:value-of select="."/>&gt;</a>
    </kbd>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' pr-d/var ') or contains(@class,' syntaxdiagram-d/var ')]" mode="process-syntaxdiagram">
   <var>
    <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
      <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
    </xsl:if>
    <xsl:if test="@importance='optional'"> [</xsl:if>
    <xsl:choose>
      <xsl:when test="@importance='default'"><u><xsl:value-of select="."/></u></xsl:when>
      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
    <xsl:if test="@importance='optional'">] </xsl:if>
   </var>
  </xsl:template>
  
  
  <!-- fragment block and title (echo same for syntaxdiagram?) -->
  
  <xsl:template match="*[contains(@class,' pr-d/fragment ') or contains(@class,' syntaxdiagram-d/fragment ')]/groupcomp |
                       *[contains(@class,' pr-d/fragment ') or contains(@class,' syntaxdiagram-d/fragment ')]/groupchoice |
                       *[contains(@class,' pr-d/fragment ') or contains(@class,' syntaxdiagram-d/fragment ')]/groupseq"
                mode="process-syntaxdiagram">
    <blockquote>
      <xsl:call-template name="dogroup"/>
    </blockquote>
  </xsl:template>
  
  
  
  <!-- GROUP CONTAINER PROCESSING, ALL PERMUTAIONS -->
  
  
  <!-- set up group containers (similar to same area management as for syntaxdiagram, synblk,  and fragment) -->
  
  <xsl:template match="syntaxdiagram/*[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')] |
                       syntaxdiagram/*[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')] |
                       syntaxdiagram/*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  
  <!-- handle group titles (by skipping over them for now -->
  
  <xsl:template match="*[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]/*[contains(@class,' topic/title ')] |
                       *[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]/*[contains(@class,' topic/title ')] |
                       *[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]/*[contains(@class,' topic/title ')]"
                mode="process-syntaxdiagram"/>  <!-- Consume title -->
  
  
  <!-- okay, here we have to work each permutation because figgroup/figroup fallback is too general -->
  <xsl:template match="*[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]/
                       *[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]/
                       *[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]/
                       *[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]/
                       *[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]/
                       *[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]/
                       *[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]/
                       *[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]/
                       *[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/groupseq ') or contains(@class,' syntaxdiagram-d/groupseq ')]/
                       *[contains(@class,' pr-d/groupcomp ') or contains(@class,' syntaxdiagram-d/groupcomp ')]"
                mode="process-syntaxdiagram">
    <xsl:call-template name="dogroup"/>
  </xsl:template>
  
  
  <xsl:template name="dogroup">
      <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
        <xsl:if test="count(preceding-sibling::*)!=0"> |</xsl:if>
      </xsl:if>
    <xsl:if test="@importance='optional'"> [</xsl:if>
    <xsl:if test="contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')"> {</xsl:if>
      <xsl:text> </xsl:text><xsl:apply-templates mode="process-syntaxdiagram"/><xsl:text> </xsl:text>
  <!-- repid processed here before -->
    <xsl:if test="contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')">} </xsl:if>
    <xsl:if test="@importance='optional'">] </xsl:if>
  </xsl:template>
  
  
  <!-- these cases are valid also outside of syntax diagram; we test for context 
    to ensure contextually correct rendering when in a diagram -->
  
  <!-- Basically, we want to hide his content. -->
  <xsl:template match="*[contains(@class,' pr-d/repsep ') or contains(@class,' syntaxdiagram-d/repsep ')]"  mode="process-syntaxdiagram"/>
  
  
  <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ') or contains(@class,' syntaxdiagram-d/syntaxdiagram ')]//
                          *[contains(@class,' pr-d/kwd ') or contains(@class,' syntaxdiagram-d/kwd ')] | 
                       *[contains(@class,' pr-d/synph ') or contains(@class,' syntaxdiagram-d/synph ')]//
                          *[contains(@class,' pr-d/kwd ') or contains(@class,' syntaxdiagram-d/kwd ')]" 
                mode="process-syntaxdiagram">
  <kbd><b>
    <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
      <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
    </xsl:if>
    <xsl:if test="@importance='optional'"> [</xsl:if>
    <xsl:choose>
      <xsl:when test="@importance='default'"><u><xsl:value-of select="."/></u></xsl:when>
      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
    <xsl:if test="@importance='optional'">] </xsl:if>
  </b>&#32;</kbd> <!-- force a space to follow the bold endtag, which has a concat behavior otherwise -->
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' pr-d/oper ') or contains(@class,' syntaxdiagram-d/oper ')]"  mode="process-syntaxdiagram">
    <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
      <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
    </xsl:if>
    <kbd>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates select="@id"/>
      <xsl:call-template name="apply-for-phrases"/>
    </kbd>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' pr-d/delim ') or contains(@class,' syntaxdiagram-d/delim ')]" mode="process-syntaxdiagram">
    <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
      <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
    </xsl:if>
    <kbd>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates select="@id"/>
      <xsl:call-template name="apply-for-phrases"/>
    </kbd>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' pr-d/sep ') or contains(@class,' syntaxdiagram-d/sep ')]" mode="process-syntaxdiagram">
    <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ') or contains(@class,' syntaxdiagram-d/groupchoice ')]">
      <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
    </xsl:if>
    <kbd>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates select="@id"/>
      <xsl:call-template name="apply-for-phrases"/>
    </kbd>
  </xsl:template>
  
  <!-- annotation related to syntaxdiagram -->
  
  <xsl:template name="gen-synnotes">
    <h3>Notes:</h3>
    <xsl:for-each select="//*[contains(@class,' pr-d/synnote ') or contains(@class,' syntaxdiagram-d/synnote ')]">
      <xsl:call-template name="dosynnt"/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="dosynnt"> <!-- creates a list of endnotes of synnt content -->
   <xsl:variable name="callout">
    <xsl:choose>
     <xsl:when test="@callout"><xsl:value-of select="@callout"/></xsl:when>
     <xsl:otherwise><xsl:value-of select="@id"/></xsl:otherwise>
    </xsl:choose>
   </xsl:variable>
   <a name="{@id}">{<xsl:value-of select="$callout"/>}</a>
   <table border="1" cellpadding="6">
     <tr><td bgcolor="LightGrey">
       <xsl:apply-templates mode="process-syntaxdiagram"/>
     </td></tr>
   </table>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' pr-d/synnoteref ') or contains(@class,' syntaxdiagram-d/synnoteref ')]" mode="process-syntaxdiagram">
    <sup>
      <a href="#FNsrc_{@refid}">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="@refid"/>
        <xsl:text>]</xsl:text>
      </a>
    </sup>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' pr-d/synnote ') or contains(@class,' syntaxdiagram-d/synnote ')]" mode="process-syntaxdiagram">
    <xsl:choose>
      <xsl:when test="not(@id='')"> <!-- case of an explicit id -->
        <sup>(explicit id <xsl:value-of select="@id"/>)
          <a name="FNsrc_{@id}" href="#FNtarg_{@id}">
            <xsl:value-of select="@id"/>
          </a>
        </sup>
      </xsl:when>
      <xsl:when test="not(@callout='')"> <!-- case of an explicit callout (presume id for now) -->
        <sup>(callout <xsl:value-of select="@callout"/>)
          <a name="FNsrc_{@id}" href="#FNtarg_{@id}">
            <xsl:value-of select="@callout"/>
          </a>
        </sup>
      </xsl:when>
      <xsl:otherwise>
          <a href="#">
            <xsl:attribute name="onMouseOver">
              <xsl:text>alert('</xsl:text><xsl:apply-templates mode="#current"/><xsl:text>')</xsl:text>
            </xsl:attribute>
            <xsl:text>*</xsl:text>
          </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- generate null filler if the phrase is evidently empty -->
  <xsl:template name="apply-for-phrases">
    <xsl:choose>
      <xsl:when test="not(text()[normalize-space(.)] | *)"><xsl:comment>null</xsl:comment></xsl:when>
      <xsl:otherwise><xsl:apply-templates mode="process-syntaxdiagram"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>