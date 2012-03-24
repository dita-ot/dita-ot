<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output
    method="xml"
    indent="yes"
    omit-xml-declaration="no"
    standalone="no"
    doctype-public="-//OASIS//DTD DocBook XML V4.1.2//EN"
    doctype-system="docbookx.dtd"/>

<xsl:template match="*[contains(@class,' pr-d/codeblock ')]">
  <programlisting>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cdblk'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </programlisting>
</xsl:template>

<!-- parml handled by base dl processing -->

<xsl:template match="*[contains(@class,' pr-d/apiname ')]">
  <function>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'apinm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </function>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/codeph ')]">
  <literal>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cdph'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </literal>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/option ')]">
  <option>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'optn'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </option>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/parmname ')]">
  <parameter>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'prmnm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </parameter>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - SYNTAX
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]">
  <xsl:call-template name="wrapTitle">
    <xsl:with-param name="wrapElem"  select="'blockquote'"/>
    <xsl:with-param name="coreElem"  select="'synopsis'"/>
    <xsl:with-param name="titleSpec" select="' topic/title '"/>
    <xsl:with-param name="IDPrefix"  select="'syntx'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')] /
      *[contains(@class,' topic/title ')] |
      *[contains(@class,' pr-d/synblk ')] /
      *[contains(@class,' topic/title ')]">
  <emphasis role="bold">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'synttl'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synblk ')]">
  <synopsis>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'synblk'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </synopsis>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupseq ')]">
  <synopsis>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'grpsq'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </synopsis>
</xsl:template>

<!-- each subelement might be wrapped in an optional element -->
<xsl:template match="*[contains(@class,' pr-d/groupchoice ')]">
  <synopsis>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'grpchc'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </synopsis>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/groupcomp ')]">
  <synopsis>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'grpcmp'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </synopsis>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragref ')]">
  <xref role="fragref" linkend="{@href}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'frgrf'"/>
    </xsl:call-template>
  </xref>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/fragment ')]">
  <synopsis>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'frgmnt'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </synopsis>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synnote ')]">
  <xsl:call-template name="footnote">
    <xsl:with-param name="IDPrefix" select="'synnt'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synnoteref ')]">
  <footnoteref role="synnoteref" linkend="{@href}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'frgrf'"/>
    </xsl:call-template>
  </footnoteref>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]">
  <symbol>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'var'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </symbol>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/kwd ')]">
  <xsl:call-template name="programtext">
    <xsl:with-param name="IDPrefix" select="'kwd'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/oper ')]">
  <literal>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'oper'"/>
    </xsl:call-template>
    <xsl:if test="@optreq">
      <xsl:attribute name="role">
        <xsl:value-of select="@optreq"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
  </literal>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/delim ')]">
  <literal>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'delm'"/>
    </xsl:call-template>
    <xsl:if test="@optreq">
      <xsl:attribute name="role">
        <xsl:value-of select="@optreq"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@convar|@startend" mode="deflate"/>
    <xsl:apply-templates/>
  </literal>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/sep ')]">
  <xsl:call-template name="programtext">
    <xsl:with-param name="IDPrefix" select="'sep'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/repsep ')]">
  <xsl:call-template name="programtext">
    <xsl:with-param name="IDPrefix" select="'repsp'"/>
  </xsl:call-template>
</xsl:template>

<!-- DITA synopsis is a phrase, DocBook synopsis a block -->
<xsl:template match="*[contains(@class,' pr-d/synph ')]">
  <literal>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'snps'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </literal>
</xsl:template>


</xsl:stylesheet>
