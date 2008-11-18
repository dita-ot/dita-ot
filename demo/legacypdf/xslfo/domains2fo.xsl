<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  domains2fo.xsl
 | DITA domains support for the demo set; extend as needed

 *-->

<xsl:transform version="1.0"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" version="1.0"
            indent="yes"/>


<!-- Start of UI domain -->

<xsl:template match="*[contains(@class,' ui-d/uicontrol ')]">
  <!-- insert an arrow before all but the first uicontrol in a menucascade -->
  <xsl:if test="ancestor::*[contains(@class,' ui-d/menucascade ')]">
    <xsl:variable name="uicontrolcount"><xsl:number count="*[contains(@class,' ui-d/uicontrol ')]"/></xsl:variable>
    <xsl:if test="$uicontrolcount&gt;'1'">
      <xsl:text> --> </xsl:text>
    </xsl:if>
  </xsl:if>
  <fo:inline font-weight="bold">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' ui-d/wintitle ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' ui-d/menucascade ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' ui-d/shortcut ')]">
  <fo:inline  text-decoration="underline">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' ui-d/screen ')]">
  <xsl:call-template name="gen-att-label"/>
  <fo:block xsl:use-attribute-sets="pre">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="setscale"/>
<!-- rules have to be applied within the scope of the PRE box; else they start from page margin! -->
    <xsl:if test="contains(@frame,'top')"><fo:block><fo:leader leader-pattern="rule" leader-length="5.65in" /></fo:block></xsl:if>
    <xsl:apply-templates/>
    <xsl:if test="contains(@frame,'bot')"><fo:block><fo:leader leader-pattern="rule" leader-length="5.65in" /></fo:block></xsl:if>
  </fo:block>
</xsl:template>


<!-- start of highlighting domain -->

<xsl:template match="*[contains(@class,' hi-d/b ')]">
  <fo:inline font-weight="bold">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' hi-d/i ')]">
  <fo:inline font-style="italic">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' hi-d/u ')]">
  <fo:inline text-decoration="underline">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' hi-d/tt ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' hi-d/sup ')]">
  <fo:inline baseline-shift="super" font-size="75%">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' hi-d/sub ')]">
  <fo:inline baseline-shift="sub" font-size="75%">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<!-- start of programming domain -->

<xsl:template match="*[contains(@class,' pr-d/codeph ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/codeblock ')]">
  <xsl:call-template name="gen-att-label"/>
  <fo:block xsl:use-attribute-sets="pre">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="setscale"/>
<!-- rules have to be applied within the scope of the PRE box; else they start from page margin! -->
    <xsl:if test="contains(@frame,'top')"><fo:block><fo:leader leader-pattern="rule" leader-length="5.65in" /></fo:block></xsl:if>
    <xsl:apply-templates/>
    <xsl:if test="contains(@frame,'bot')"><fo:block><fo:leader leader-pattern="rule" leader-length="5.65in" /></fo:block></xsl:if>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/option ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/var ')]">
  <fo:inline font-style="italic">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/parmname ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/synph ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/oper ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/delim ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/sep ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>



<xsl:template match="*[contains(@class,' pr-d/apiname ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/parml ')]">
  <xsl:call-template name="gen-att-label"/>
  <fo:block>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/plentry ')]">
  <xsl:apply-templates/>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/pt ')]">
 <fo:block xsl:use-attribute-sets="dt">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:choose>
      <xsl:when test="*"> <!-- tagged content - do not default to bold -->
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <fo:inline font-weight="bold"><xsl:call-template name="apply-for-phrases"/></fo:inline> <!-- text only - bold it -->
      </xsl:otherwise>
    </xsl:choose>
 </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/pd ')]">
  <fo:block xsl:use-attribute-sets="dd">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
   <xsl:apply-templates />
  </fo:block>
</xsl:template>


<!-- syntax diagram -->

<xsl:template match="*[contains(@class,' pr-d/synblk ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template name="gen-synnotes">
  <fo:block font-weight="bold">Notes:</fo:block>
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
 <!--a name="{@id}"-->{<xsl:value-of select="$callout"/>}<!--/a-->
<!--
 <table border="1" cellpadding="6">
   <tr><td bgcolor="LightGrey">
     <xsl:apply-templates/>
   </td></tr>
 </table>
-->
 <fo:block><xsl:apply-templates/></fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/synnoteref ')]">
<fo:inline baseline-shift="super" font-size="75%">
<!--
  <xsl:element name="a">
  <xsl:attribute name="href">#FNsrc_<xsl:value-of select="@refid"/>
  </xsl:attribute>
-->
    [<xsl:value-of select="@refid"/>]
<!--
  </xsl:element>
-->
</fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/synnote ')]">
<fo:inline baseline-shift="super" font-size="75%">
  <xsl:choose>
    <xsl:when test="not(@id='')"> <!-- case of an explicit id -->
            <xsl:value-of select="@id"/>
    </xsl:when>
    <xsl:when test="not(@callout='')"> <!-- case of an explicit callout (presume id for now) -->
            <xsl:value-of select="@callout"/>
    </xsl:when>
    <xsl:otherwise>
          <xsl:text>*</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</fo:inline>
</xsl:template>



<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]">
<fo:block>
  <xsl:apply-templates/>
</fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragment ')]">
<fo:block>
  <xsl:value-of select="*[contains(@class,' topic/title ')]"/><xsl:text> </xsl:text>
  <xsl:apply-templates/>
</fo:block>
</xsl:template>

<!-- Title is optional-->
<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' topic/title ')]">
<fo:block font-weight="bold">
  <xsl:value-of select="."/>
</fo:block>
</xsl:template>

<!-- Basically, we want to hide his content. -->
<xsl:template match="*[contains(@class,' pr-d/repsep ')]"/>


<xsl:template match="*[contains(@class,' pr-d/kwd ')]">
<fo:inline font-family="Courier">
  <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ')]">
    <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
  </xsl:if>
  <xsl:if test="@importance='optional'"> [</xsl:if>
  <xsl:choose>
    <xsl:when test="@importance='default'">
      <fo:inline text-decoration="underline"><xsl:value-of select="."/></fo:inline>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@importance='optional'">] </xsl:if>
</fo:inline>
</xsl:template>

<!-- This should test to see if there's a fragment with matching title 
and if so, produce an associative link. -->
<xsl:template match="*[contains(@class,' pr-d/fragref ')]" priority="100">
<fo:inline font-family="Courier">
      <!--a><xsl:attribute name="href">#<xsl:value-of select="."/></xsl:attribute-->
  &lt;<xsl:value-of select="."/>&gt;<!--/a-->
</fo:inline>
</xsl:template>

<!-- Where is the template for var with a priority of 50? -->
<xsl:template match="*[contains(@class,' pr-d/var ')]" priority="51">
 <fo:inline font-style="italic">
  <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ')]">
    <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
  </xsl:if>
  <xsl:if test="@importance='optional'"> [</xsl:if>
  <xsl:choose>
    <xsl:when test="@importance='default'">
      <fo:inline text-decoration="underline"><xsl:value-of select="."/></fo:inline>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@importance='optional'">] </xsl:if>
 </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' topic/title ')]">
	<fo:block font-weight="bold"><xsl:apply-templates/></fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupcomp ')]|*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupchoice ')]|*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupseq ')]">
	<fo:block><!--indent this?-->
	<xsl:call-template name="dogroup"/>
	</fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupcomp ')]|*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupseq ')]|*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupchoice ')]">
	<xsl:call-template name="dogroup"/>
</xsl:template>


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
	<xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ')]">
		<xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
	</xsl:if>
	<xsl:if test="@importance='optional'">[</xsl:if>
	<xsl:if test="name()='groupchoice'">{</xsl:if>
	  <xsl:text> </xsl:text><xsl:apply-templates/><xsl:text> </xsl:text>
<!-- repid processed here before -->
	<xsl:if test="name()='groupchoice'">}</xsl:if>
	<xsl:if test="@importance='optional'">]</xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/title|*[contains(@class,' pr-d/groupseq ')]/title|*[contains(@class,' pr-d/groupseq ')]/title"/>  <!-- Consume title -->


<!-- start of software domain elements -->

<xsl:template match="*[contains(@class,' sw-d/msgph ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>



<xsl:template match="*[contains(@class,' sw-d/msgblock ')]">
  <xsl:if test="contains(@frame,'top')"><hr /></xsl:if>
  <xsl:call-template name="gen-att-label"/>
  <fo:block> <!-- use pre style -->
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:if test="@scale">
      <!--xsl:attribute name="style">font-size: <xsl:value-of select="@scale"/>%;</xsl:attribute-->
    </xsl:if>
    <xsl:apply-templates/>
  </fo:block>
  <xsl:if test="contains(@frame,'bot')"><hr /></xsl:if>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/msgnum ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/cmdname ')]">
  <fo:inline>
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/varname ')]">
  <fo:inline font-style="italic">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/filepath ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/userinput ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' sw-d/systemoutput ')]">
  <fo:inline font-family="Courier">
    <xsl:call-template name="setclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:call-template name="apply-for-phrases"/>
  </fo:inline>
</xsl:template>


</xsl:transform>

