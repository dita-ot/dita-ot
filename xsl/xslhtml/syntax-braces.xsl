<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- syntax diagram -->


<!-- set up global vars and parms -->



<!-- Logical containers -->

<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]">
	<div style="display: block; border: 1 black solid; padding: 2pt; color: maroon; margin-bottom: 6pt;">
	<xsl:apply-templates/>
	</div>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragment ')]">
	<div>
	<a><xsl:attribute name="name"><xsl:value-of select="*[contains(@class,' topic/title ')]"/></xsl:attribute> </a>
	<xsl:apply-templates/>
	</div>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synblk ')]">
  <span>
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </span>
</xsl:template>



<!-- titles for logical containers -->

<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' topic/title ')]">
	<h3>
	<xsl:value-of select="."/>
	</h3>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' topic/title ')]">
	<h4><xsl:apply-templates/></h4>
</xsl:template>


<!-- This should test to see if there's a fragment with matching title 
and if so, produce an associative link. -->
<xsl:template match="*[contains(@class,' pr-d/fragref ')]" priority="100">
	<kbd>
      <a><xsl:attribute name="href">#<xsl:value-of select="."/></xsl:attribute>
	&lt;<xsl:value-of select="."/>&gt;</a>
	</kbd>
</xsl:template>

<!-- Where is the template for var with a priority of 50? -->
<xsl:template match="*[contains(@class,' pr-d/var ')]" priority="51">
 <var>
  <xsl:if test="name(parent::*)='groupchoice'"> | </xsl:if>
  <xsl:if test="@importance='optional'"> [</xsl:if>
  <xsl:choose>
    <xsl:when test="@importance='default'"><u><xsl:value-of select="."/></u></xsl:when>
    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@importance='optional'">] </xsl:if>
 </var>
</xsl:template>


<!-- fragment block and title (echo same for syntaxdiagram?) -->

<xsl:template match="*[contains(@class,' pr-d/fragment ')]/groupcomp|*[contains(@class,' pr-d/fragment ')]/groupchoice|*[contains(@class,' pr-d/fragment ')]/groupseq">
	<blockquote>
	<xsl:call-template name="dogroup"/>
	</blockquote>
</xsl:template>



<!-- GROUP CONTAINER PROCESSING, ALL PERMUTAIONS -->


<!-- set up group containers (similar to same area management as for syntaxdiagram, synblk,  and fragment) -->

<xsl:template match="syntaxdiagram/*[contains(@class,' pr-d/groupcomp ')]|syntaxdiagram/*[contains(@class,' pr-d/groupseq ')]|syntaxdiagram/*[contains(@class,' pr-d/groupchoice ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>


<!-- handle group titles (by skipping over them for now -->

<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' topic/title ')]|*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' topic/title ')]|*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' topic/title ')]"/>  <!-- Consume title -->


<!-- okay, here we have to work each permutation because figgroup/figroup fallback is too general -->
<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupcomp ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupchoice ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupseq ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupcomp ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>
<xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupseq ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupchoice ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupseq ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupchoice ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupcomp ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>



<xsl:template name="dogroup">
	<xsl:if test="name(parent::*)='groupchoice'">
		<xsl:if test="count(preceding-sibling::*)!=0"> |</xsl:if>
	</xsl:if>
	<xsl:if test="@importance='optional'"> [</xsl:if>
	<xsl:if test="name()='groupchoice'"> {</xsl:if>
	  <xsl:text> </xsl:text><xsl:apply-templates/><xsl:text> </xsl:text>
<!-- repid processed here before -->
	<xsl:if test="name()='groupchoice'">} </xsl:if>
	<xsl:if test="@importance='optional'">] </xsl:if>
</xsl:template>






<!-- these cases are valid also outside of syntax diagram; we test for context 
  to ensure contextually correct rendering when in a diagram -->

<!-- Basically, we want to hide his content. -->
<xsl:template match="*[contains(@class,' pr-d/repsep ')]" />


<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]//*[contains(@class,' pr-d/kwd ')] | *[contains(@class,' pr-d/synph ')]//*[contains(@class,' pr-d/kwd ')]" priority="3">
<kbd><b>
  <xsl:if test="name(parent::*)='groupchoice'"><xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if></xsl:if>
  <xsl:if test="@importance='optional'"> [</xsl:if>
  <xsl:choose>
    <xsl:when test="@importance='default'"><u><xsl:value-of select="."/></u></xsl:when>

    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@importance='optional'">] </xsl:if>
</b>&#32;</kbd> <!-- force a space to follow the bold endtag, which has a concat behavior otherwise -->
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/var ')]" >
  <xsl:if test="name(parent::*)='groupchoice'"><xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if></xsl:if>
  <var>
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </var>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/oper ')]" >
  <xsl:if test="name(parent::*)='groupchoice'"><xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if></xsl:if>
  <kbd>
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </kbd>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/delim ')]" >
  <xsl:if test="name(parent::*)='groupchoice'"><xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if></xsl:if>
  <kbd>
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </kbd>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/sep ')]" >
  <xsl:if test="name(parent::*)='groupchoice'"><xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if></xsl:if>
  <kbd>
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </kbd>
</xsl:template>



<!-- annotation related to syntaxdiagram -->


<xsl:template name="gen-synnotes">
  <h3>Notes:</h3>
  <xsl:for-each select="//*[contains(@class,' pr-d/synnote ')]">
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
     <xsl:apply-templates/>
   </td></tr>
 </table>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/synnoteref ')]">
<sup>
  <xsl:element name="a">
  <xsl:attribute name="href">#FNsrc_<xsl:value-of select="@refid"/>
  </xsl:attribute>
    [<xsl:value-of select="@refid"/>]
  </xsl:element>
</sup>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/synnote ')]">
  <xsl:choose>
    <xsl:when test="not(@id='')"> <!-- case of an explicit id -->
      <sup>(explicit id <xsl:value-of select="@id"/>)
        <xsl:element name="a">
          <xsl:attribute name="name">FNsrc_<xsl:value-of select="@id"/></xsl:attribute>
          <xsl:attribute name="href">#FNtarg_<xsl:value-of select="@id"/></xsl:attribute>
            <xsl:value-of select="@id"/>
        </xsl:element>
      </sup>
    </xsl:when>
    <xsl:when test="not(@callout='')"> <!-- case of an explicit callout (presume id for now) -->
      <sup>(callout <xsl:value-of select="@callout"/>)
        <xsl:element name="a">
          <xsl:attribute name="name">FNsrc_<xsl:value-of select="@id"/></xsl:attribute>
          <xsl:attribute name="href">#FNtarg_<xsl:value-of select="@id"/></xsl:attribute>
            <xsl:value-of select="@callout"/>
        </xsl:element>
      </sup>
    </xsl:when>
    <xsl:otherwise>
        <xsl:element name="a">
          <xsl:attribute name="href">#</xsl:attribute>
          <xsl:attribute name="onMouseOver">
            <xsl:text>alert('</xsl:text><xsl:apply-templates/><xsl:text>')</xsl:text>
          </xsl:attribute>*</xsl:element>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- generate null filler if the phrase is evidently empty -->
<xsl:template name="apply-for-phrases">
  <xsl:choose>
    <xsl:when test="not(text()[normalize-space(.)] | *)"><xsl:comment>null</xsl:comment></xsl:when>
    <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>