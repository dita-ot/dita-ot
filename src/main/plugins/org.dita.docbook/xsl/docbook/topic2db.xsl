<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">



<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - TOPIC ORGANIZATION
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/topic ')]">
  <xsl:param name="childrefs"/>
  <xsl:param name="element" select="'section'"/>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'tpc'"/>
    </xsl:call-template>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]">
      <xsl:with-param name="contextType" select="$element"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="*[not(contains(@class,' topic/prolog '))]"/>
    <xsl:if test="$childrefs">
      <xsl:apply-templates select="$childrefs"/>
    </xsl:if>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/title ')]">
  <xsl:param name="element"  select="'title'"/>
  <xsl:param name="IDPrefix" select="'ttl'"/>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/titlealts ')]">
  <xsl:apply-templates select="*[contains(@class,' topic/searchtitle ')]"/>
  <xsl:apply-templates select="*[contains(@class,' topic/navtitle ')]"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/navtitle ')]">
  <xsl:param name="element"  select="'titleabbrev'"/>
  <xsl:param name="IDPrefix" select="'ttlabbrv'"/>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/searchtitle ')]">
  <xsl:param name="element"  select="'subtitle'"/>
  <xsl:param name="IDPrefix" select="'sbttl'"/>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]" mode="abstract">
  <abstract>
    <xsl:call-template name="makePara">
      <xsl:with-param name="IDPrefix" select="'shrtdsc'"/>
    </xsl:call-template>
  </abstract>
</xsl:template>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]">
  <xsl:call-template name="makePara">
    <xsl:with-param name="IDPrefix" select="'para'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="abstract">
  <abstract>
    <xsl:call-template name="makePara">
      <xsl:with-param name="IDPrefix" select="'shrtdsc'"/>
    </xsl:call-template>
  </abstract>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]">
  <xsl:call-template name="makePara">
    <xsl:with-param name="IDPrefix" select="'para'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/body ')]">
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - META INFORMATION
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/prolog ')]">
  <xsl:param name="contextType" select="'section'"/>
  <xsl:variable name="shortDescNode"
      select="../*[contains(@class,' topic/shortdesc ') or contains(@class, ' topic/abstract ')]"/>
  <xsl:variable name="prologNodes" select="*"/>
  <xsl:if test="$shortDescNode or $prologNodes">
    <xsl:variable name="elementName">
      <xsl:choose>
      <xsl:when test="$contextType='article'">artheader</xsl:when>
      <xsl:when test="$contextType='appendix'">docinfo</xsl:when>
      <xsl:when test="$contextType='book'">bookinfo</xsl:when>
      <xsl:when test="$contextType='chapter'">chapterinfo</xsl:when>
      <xsl:when test="$contextType='glossary'">docinfo</xsl:when>
      <xsl:when test="$contextType='part'">docinfo</xsl:when>
      <xsl:when test="$contextType='section'">sectioninfo</xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>Unknown context type </xsl:text>
          <xsl:value-of select="$contextType"/>
        </xsl:message>
        <xsl:text>sectioninfo</xsl:text>
      </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$elementName}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'prlg'"/>
      </xsl:call-template>
      <xsl:apply-templates select="$shortDescNode" mode="abstract"/>
      <xsl:apply-templates select="$prologNodes"/>
    </xsl:element>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/author ')]">
  <author>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'athr'"/>
    </xsl:call-template>
	<othername>
	    <xsl:apply-templates/>
	</othername>
  </author>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/source ')]"/>

<xsl:template match="*[contains(@class,' topic/publisher ')]">
  <publisher>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'pblshr'"/>
    </xsl:call-template>
   <!-- dhjohnso: publisher must be inside publishername -->
   <publishername>
    <xsl:apply-templates/>
   </publishername>
  </publisher>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/copyright ')]">
  <copyright>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cprght'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </copyright>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/copyrdate ')]">
  <year>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cprdt'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </year>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/copyrholder ')]">
  <holder>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cprhldr'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </holder>
</xsl:template>

<!-- dhjohnso: template added for missing year element -->
<xsl:template match="*[contains(@class,' topic/copyryear ')]">
  <year>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cpryear'"/>
    </xsl:call-template>
    <xsl:value-of select="@year" />
  </year>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/critdates ')]">
  <revhistory>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'crtdts'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </revhistory>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/created ')]">
  <xsl:apply-templates select="@date|@expiry|@golive"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/revised ')]">
  <xsl:apply-templates select="@expiry|@golive|@modified"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/created ')]/@date |
      *[contains(@class,' topic/created ')]/@expiry |
      *[contains(@class,' topic/created ')]/@golive |
      *[contains(@class,' topic/revised ')]/@expiry |
      *[contains(@class,' topic/revised ')]/@golive |
      *[contains(@class,' topic/revised ')]/@modified">
  <revision>
    <revnumber/>
    <date>
      <xsl:value-of select="../@date"/>
    </date>
    <revremark>
      <xsl:choose>
      <xsl:when test="parent::*[contains(@class,' topic/created ')] and
          local-name(.)='date'">
        <xsl:text>created</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="local-name(.)"/>
      </xsl:otherwise>
      </xsl:choose>
    </revremark>
  </revision>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/permissions ')]"/>

<xsl:template match="*[contains(@class,' topic/metadata ')]">
  <xsl:variable name="categoryNodes"
      select="*[contains(@class,' topic/category ')]"/>
  <xsl:if test="$categoryNodes">
    <subjectset>
      <xsl:apply-templates select="$categoryNodes" mode="metadata"/>
    </subjectset>
  </xsl:if>
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/audience ')]"/>

<xsl:template match="*[contains(@class,' topic/category ')]"/>

<xsl:template match="*[contains(@class,' topic/category ')]" mode="metadata">
  <subject>
    <subjectterm>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'ctgry'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </subjectterm>
  </subject>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/keywords ')]">
  <xsl:variable name="keywordNodes"
      select="*[contains(@class,' topic/keyword ')]"/>
  <xsl:variable name="indextermNodes"
      select="*[contains(@class,' topic/indexterm ')]"/>
  <xsl:if test="$keywordNodes">
    <keywordset>
      <xsl:apply-templates select="$keywordNodes" mode="metadata"/>
    </keywordset>
  </xsl:if>
  <xsl:if test="$indextermNodes">
    <itermset>
      <xsl:apply-templates select="$indextermNodes"/>
    </itermset>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/keyword ')]" mode="metadata">
  <keyword>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'kywrd'"/>
    </xsl:call-template>
    <xsl:value-of select="."/>
  </keyword>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prodinfo ')]">
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prodname ')]">
  <productname>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'prdnm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </productname>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/vrmlist ')]">
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/vrm ')]">
  <productnumber>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'vrm'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@version"/>
    <xsl:if test="@version and @release">
      <xsl:text>-</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="@release"/>
    <xsl:if test="(@version or @release) and @modification">
      <xsl:text>-</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="@modification"/>
  </productnumber>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/brand ')]"/>
<xsl:template match="*[contains(@class,' topic/series ')]"/>
<xsl:template match="*[contains(@class,' topic/platform ')]"/>
<xsl:template match="*[contains(@class,' topic/prognum ')]"/>
<xsl:template match="*[contains(@class,' topic/featnum ')]"/>
<xsl:template match="*[contains(@class,' topic/component ')]"/>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/othermeta ')]"/>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/resourceid ')]"/>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - RELATIONSHIPS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/related-links ')]">
  <itemizedlist>
    <title>Related links</title>
    <xsl:apply-templates/>
  </itemizedlist>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/linkpool ')]">
  <xsl:apply-templates/>
</xsl:template>

<!-- ??? should handle title and linkinfo and desc -->
<xsl:template match="*[contains(@class,' topic/linklist ')]">
  <xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>
  <listitem>
    <itemizedlist>
      <xsl:apply-templates select="*[contains(@class,' topic/linklist ') or 
	      contains(@class,' topic/link ')]"/>
    </itemizedlist>
    <xsl:apply-templates select="*[contains(@class,' topic/linkinfo ')]"/>
  </listitem>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/link ')]">
  <listitem>
    <para>
      <xsl:choose>
        <xsl:when test="(not(@format) or @format='dita' or @format='DITA') and
                        (not(@scope)  or @scope='local') and
                        @href and (
                             substring(@href, string-length(@href) - 4) = '.dita' or
                             contains(@href,'.dita#') or
                             substring(@href, string-length(@href) - 3) = '.xml' or
                             contains(@href,'.xml#'))">
          <xsl:apply-templates select="." mode="make-xref-from-link"/>
        </xsl:when>
        <xsl:when test="((@format and @format!='dita' and @format!='DITA') or
                         (@scope  and @scope!='local') or
                         (@href   and
                               substring(@href, string-length(@href) - 4) != '.dita' and
                               not (contains(@href,'.dita#')) and
                               substring(@href, string-length(@href) - 3) != '.xml') and
                               not (contains(@href,'.xml#')))">
          <xsl:apply-templates select="." mode="make-ulink-from-link"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="make-ulink-from-link"/>
        </xsl:otherwise>
      </xsl:choose>
    </para>
    <xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>
  </listitem>
</xsl:template>

<xsl:template match="*" mode="make-ulink-from-link">
  <ulink url="{@href}">
    <xsl:apply-templates select="*[contains(@class,' topic/linktext ')]"/>
  </ulink>
</xsl:template>

<xsl:template match="*" mode="make-xref-from-link">
  <xsl:variable name="linkID">
    <xsl:call-template name="getLinkID"/>
  </xsl:variable>
  <xref linkend="{$linkID}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'xref'"/>
    </xsl:call-template>
  </xref>
</xsl:template>

<!-- Provided by Erik Hennum and dcramer in SF bug report 1385654.
     For use with targets already identified as DITA.
     Does not yet support linking to sub-topic elements; all links go to topic. -->
<xsl:template name="getLinkID">
  <xsl:param name="href" select="@href"/>
  <xsl:variable name="hasID" select="contains($href,'#')"/>
  <xsl:choose>
    <xsl:when test="contains($href,'#') and contains(substring-after($href,'#'),'/')">
      <xsl:value-of select="substring-before(substring-after(@href,'#'),'/')"/>
    </xsl:when>
    <xsl:when test="contains($href,'#')">
      <xsl:value-of select="substring-after(@href,'#')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of
        select="document($href, /)/*[contains(@class,' topic/topic ')]/@id"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - SECTIONS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/section ')]">
  <xsl:call-template name="makeBlockCont">
    <xsl:with-param name="IDPrefix" select="'sctn'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')]">
  <xsl:choose>
  <xsl:when test="title">
    <example>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'xmp'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </example>
  </xsl:when>
  <xsl:otherwise>
    <informalexample>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'xmp'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </informalexample>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - LISTS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/ul ')]">
  <itemizedlist>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'ul'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@compact" mode="deflate"/>
    <xsl:apply-templates/>
  </itemizedlist>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/ol ')]">
  <orderedlist>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'ol'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@compact" mode="deflate"/>
    <xsl:apply-templates/>
  </orderedlist>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/li ')]">
  <listitem>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'li'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </listitem>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/itemgroup ')]">
  <xsl:variable name="element" select="local-name(.)" />
  <xsl:variable name="id" select="concat('elem', generate-id())" />
  <xsl:call-template name="deflateElementStart">
    <xsl:with-param name="id" select="$id" />
    <xsl:with-param name="element" select="$element" />
    <xsl:with-param name="parentID" select="''" />
  </xsl:call-template>
  <xsl:call-template name="makeBlock" />
  <xsl:call-template name="deflateElementEnd">
    <xsl:with-param name="id" select="$id" />
    <xsl:with-param name="element" select="$element" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dl ')]">
  <variablelist>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dl'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@compact" mode="deflate"/>
    <xsl:if test="@title">
      <title>
        <xsl:value-of select="@title"/>
      </title>
    </xsl:if>
    <xsl:apply-templates/>
  </variablelist>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dlhead ')]">
  <varlistentry>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dlhd'"/>
    </xsl:call-template>
    <xsl:variable name="dtheads" select="*[contains(@class,' topic/dthd ')]"/>
    <xsl:variable name="ddheads" select="*[contains(@class,' topic/ddhd ')]"/>
    <xsl:choose>
    <xsl:when test="$dtheads">
      <xsl:apply-templates select="$dtheads"/>
    </xsl:when>
    <xsl:otherwise>
      <term/>
    </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
    <xsl:when test="$ddheads">
      <xsl:apply-templates select="$ddheads"/>
    </xsl:when>
    <xsl:otherwise>
      <listitem/>
    </xsl:otherwise>
    </xsl:choose>
  </varlistentry>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dthd ')]">
  <term>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dthd'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </term>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/ddhd ')]">
  <listitem>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'ddhd'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </listitem>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dlentry ')]">
  <varlistentry>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dle'"/>
    </xsl:call-template>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' topic/dd ')][2]">
        <xsl:apply-templates select="*[contains(@class,' topic/dt ')]"/>
        <listitem>
          <xsl:for-each select="*[contains(@class,' topic/dd ')]">
           <orderedlist>
             <listitem>
               <xsl:call-template name="setStandardAttr">
                 <xsl:with-param name="IDPrefix" select="'dd'"/>
               </xsl:call-template>
               <xsl:call-template name="makeBlock"/>
             </listitem>
           </orderedlist>
          </xsl:for-each>
        </listitem>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </varlistentry>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dt ')]">
  <term>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dt'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </term>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dd ')]">
  <listitem>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'dd'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </listitem>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sl ')]">
  <simplelist columns="1" type="vert">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sl'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@compact" mode="deflate"/>
    <xsl:apply-templates/>
  </simplelist>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sli ')]">
  <member>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sli'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </member>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - VERBATIM BLOCKS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/pre ')]">
  <xsl:call-template name="wrapTitle">
    <xsl:with-param name="wrapElem" select="'example'"/>
    <xsl:with-param name="coreElem" select="'programlisting'"/>
    <xsl:with-param name="IDPrefix" select="'pre'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/lines ')]">
  <xsl:call-template name="wrapLiteralTitle">
    <xsl:with-param name="classAttr"  select="'normal'"/>
    <xsl:with-param name="IDPrefix"  select="'lns'"/>
  </xsl:call-template>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - FORMATTING BLOCKS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/p ')]">
  <xsl:call-template name="makePara">
    <xsl:with-param name="IDPrefix" select="'para'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/lq ')]">
  <blockquote>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'lq'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </blockquote>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - ADMONITION / PERIL BLOCKS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/note ') and
      (@type='attention' or @type='important' or @type='restriction')]">
  <important>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'important'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </important>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ') and
      (@type='caution' or @type='remember')]">
  <caution>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'caution'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </caution>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ') and @type='danger']">
  <warning>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'warning'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </warning>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ') and
      (@type='tip' or @type='fastpath')]">
  <tip>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'warning'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </tip>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ') and not(@type)]">
  <note>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'note'"/>
    </xsl:call-template>
    <xsl:if test="@title">
      <title>
        <xsl:value-of select="@title"/>
      </title>
    </xsl:if>
    <xsl:call-template name="makeBlock"/>
  </note>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - SEMANTIC PHRASES
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/ph ')]" name="phrase">
  <xsl:param name="IDPrefix" select="'phrs'"/>
  <xsl:param name="role" select="''"/>
  <phrase>
    <xsl:if test="$role!=''">
      <xsl:attribute name="role">
        <xsl:value-of select="$role"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </phrase>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/keyword ')]">
  <xsl:call-template name="phrase">
    <xsl:with-param name="IDPrefix" select="'kywrd'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/q ')]">
  <quote>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'quote'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </quote>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/term ')]">
  <glossterm>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'quote'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </glossterm>
</xsl:template>


<!--
DATA-TYPE PHRASES: date time currency char num bin oct dec hex ???
-->

<xsl:template match="*[contains(@class,' topic/boolean ')]"/>
<xsl:template match="*[contains(@class,' topic/state ')]"/>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - LINKING PHRASES
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/xref ')]">
  <xsl:choose>
    <xsl:when test="(not(@format) or @format='dita' or @format='DITA') and
	                (not(@scope)  or @scope='local') and
	                @href and (
	                      substring(@href, string-length(@href) - 4) = '.dita' or
                          contains(@href,'.dita#') or
	                      substring(@href, string-length(@href) - 3) = '.xml' or
                          contains(@href,'.xml#'))">
      <xsl:apply-templates select="." mode="make-xref-from-xref"/>
    </xsl:when>
    <xsl:when test="((@format and @format!='dita' and @format!='DITA') or
	                 (@scope  and @scope!='local') or
	                 (@href   and
	                      substring(@href, string-length(@href) - 4) != '.dita' and
                          not (contains(@href,'.dita#')) and
	                      substring(@href, string-length(@href) - 3) != '.xml' and
                          not (contains(@href,'.xml#')) ))">
      <xsl:apply-templates select="." mode="make-ulink-from-xref"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Unable to handle at this time -->
      <xsl:apply-templates select="." mode="deflate"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--<xsl:template match="*[contains(@class,' topic/xref ') and (
      (@format and @format!='dita' and @format!='DITA') or
	  (@scope  and @scope!='local') or
	  (@href   and
	      substring(@href, string-length(@href) - 5) != '.dita' and
	      substring(@href, string-length(@href) - 4) != '.xml'))]">-->
<xsl:template match="*" mode="make-ulink-from-xref">
  <ulink url="{@href}" type="{@type}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'link'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </ulink>
</xsl:template>

<!--<xsl:template match="*[contains(@class,' topic/xref ') and
      (not(@format) or @format='dita' or @format='DITA') and
	  (not(@scope)  or @scope='local') and
	  @href and (
	      substring(@href, string-length(@href) - 5) = '.dita' or
	      substring(@href, string-length(@href) - 4) = '.xml')]">-->
<xsl:template match="*" mode="make-xref-from-xref">
  <xsl:variable name="linkID">
    <xsl:call-template name="getLinkID"/>
  </xsl:variable>
  <xref linkend="{$linkID}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'xref'"/>
    </xsl:call-template>
  </xref>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/fn ')]" name="footnote">
  <xsl:param name="IDPrefix" select="'fn'"/>
  <footnote>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </footnote>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/cite ')]">
  <xsl:choose>
  <xsl:when test="@ref">
    <xref role="cite" linkend="{@ref}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'cite'"/>
      </xsl:call-template>
    </xref>
  </xsl:when>
  <xsl:otherwise>
    <citation>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="'cite'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </citation>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/indexterm ')]">
  <indexterm>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'indxtrm'"/>
    </xsl:call-template>
    <primary>
      <xsl:apply-templates select="text()"/>
    </primary>
    <xsl:apply-templates select="*" mode="secondary"/>
  </indexterm>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="secondary">
  <secondary>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'indxtrm'"/>
    </xsl:call-template>
    <xsl:apply-templates select="text()"/>
  </secondary>
  <xsl:apply-templates select="*" mode="tertiary"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="tertiary">
  <tertiary>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'indxtrm'"/>
    </xsl:call-template>
    <xsl:apply-templates select="text()"/>
  </tertiary>
  <xsl:apply-templates select="*"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - TABLES
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/table ')]">
  <xsl:param name="titleSpec" select="' topic/title '"/>
  <xsl:param name="titleNode" select="*[contains(@class,$titleSpec)]"/>
  <xsl:variable name="descNode" select="*[contains(@class,' topic/desc ')]"/>
  <xsl:variable name="element">
    <xsl:choose>
    <xsl:when test="$titleNode">
      <xsl:text>table</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>informaltable</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'tbl'"/>
    </xsl:call-template>
    <xsl:if test="@colsep">
      <xsl:copy-of select="@colsep"/>
    </xsl:if>
    <xsl:if test="@frame">
      <xsl:copy-of select="@frame"/>
    </xsl:if>
    <xsl:if test="@rowsep">
        <xsl:copy-of select="@rowsep"/>
    </xsl:if>
    <xsl:apply-templates select="@rowheader|@scale" mode="deflate"/>
    <xsl:if test="$titleNode">
      <xsl:apply-templates select="$titleNode"/>
    </xsl:if>
    <xsl:if test="$descNode">
      <xsl:apply-templates select="$descNode"/>
    </xsl:if>
    <xsl:apply-templates select="*[contains(@class,' topic/tgroup ')]"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tgroup ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'tgroup'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/colspec ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'colspec'"/>
    <xsl:with-param name="hasRemap"    select="false()"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/spanspec ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'spanspec'"/>
    <xsl:with-param name="hasRemap"    select="false()"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/thead ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'thead'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tfoot ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'tfoot'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tbody ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'tbody'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/row ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'row'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/entry ')]">
  <xsl:call-template name="copyAs">
    <xsl:with-param name="elementName" select="'entry'"/>
  </xsl:call-template>
</xsl:template>

<!-- segmentedlist is a closer match with simpletable 
     but the content model of set is too restrictive -->
<xsl:template match="*[contains(@class,' topic/simpletable ')]">
  <xsl:variable name="colcount">
    <xsl:call-template name="maxsemcol">
      <xsl:with-param name="rows"
          select="*[contains(@class,' topic/strow ')]"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="element">
    <xsl:choose>
    <xsl:when test="@title">
      <xsl:text>table</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>informaltable</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'smtbl'"/>
    </xsl:call-template>
    <xsl:if test="@title">
      <title>
        <xsl:value-of select="@title"/>
      </title>
    </xsl:if>
    <tgroup cols="{$colcount}">
      <xsl:apply-templates select="*[contains(@class,' topic/sthead ')]"/>
      <tbody>
        <xsl:apply-templates select="*[contains(@class,' topic/strow ')]"/>
      </tbody>
    </tgroup>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sthead ')]">
  <thead>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sthd'"/>
    </xsl:call-template>
    <row>
      <xsl:apply-templates/>
    </row>
  </thead>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/strow ')]">
  <row>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'strw'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </row>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/stentry ')]">
  <entry>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'stent'"/>
    </xsl:call-template>
    <xsl:if test="@title">
      <xsl:attribute name="xreflabel">
        <xsl:value-of select="@xreflabel"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
  </entry>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - FIGURES AND MEDIA OBJECTS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/fig ')]">
  <xsl:param name="titleSpec" select="' topic/title '"/>
  <xsl:param name="titleNode" select="*[contains(@class,$titleSpec)]"/>
  <xsl:variable name="descNode" select="*[contains(@class,' topic/desc ')]"/>
  <xsl:variable name="contentNode" select="*[not(contains(@class,$titleSpec)
      or contains(@class,' topic/desc '))]"/>
  <xsl:variable name="element">
    <xsl:choose>
    <xsl:when test="$titleNode or @title">
      <xsl:text>figure</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>informalfigure</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$element}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'fig'"/>
    </xsl:call-template>
    <xsl:apply-templates select="@frame|@scale" mode="deflate"/>
    <xsl:choose>
    <xsl:when test="$titleNode">
      <xsl:apply-templates select="$titleNode"/>
    </xsl:when>
    <xsl:when test="@title">
      <title>
        <xsl:value-of select="@title"/>
      </title>
    </xsl:when>
    </xsl:choose>
    <xsl:if test="$descNode">
      <xsl:apply-templates select="$descNode"/>
    </xsl:if>
    <xsl:choose>
    <xsl:when test="contains($contentNode/@class,' topic/figgroup ') and (
      contains($contentNode/*/@class,' topic/dl ') or
      contains($contentNode/*/@class,' topic/gl ') or
      contains($contentNode/*/@class,' topic/note ') or
      contains($contentNode/*/@class,' topic/notelist ') or
      contains($contentNode/*/@class,' topic/ol ') or
      contains($contentNode/*/@class,' topic/p ') or
      contains($contentNode/*/@class,' topic/qalist ') or
      contains($contentNode/*/@class,' topic/ul '))">
      <blockquote remap="CONTAINER">
        <xsl:apply-templates select="$contentNode"/>
      </blockquote>
    </xsl:when>
    <xsl:when test="contains($contentNode/@class,' topic/dl ') or
      contains($contentNode/@class,' topic/gl ') or
      contains($contentNode/@class,' topic/note ') or
      contains($contentNode/@class,' topic/notelist ') or
      contains($contentNode/@class,' topic/ol ') or
      contains($contentNode/@class,' topic/p ') or
      contains($contentNode/@class,' topic/qalist ') or
      contains($contentNode/@class,' topic/ul ')">
      <blockquote remap="CONTAINER">
        <xsl:apply-templates select="$contentNode"/>
      </blockquote>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="$contentNode"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/figgroup ')]">
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/object ')]">
  <mediaobject>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'obj'"/>
    </xsl:call-template>
    <xsl:apply-templates select="." mode="deflate"/>
  </mediaobject>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/textalt ')]">
  <textobject>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'txtlt'"/>
    </xsl:call-template>
    <xsl:call-template name="makeBlock"/>
  </textobject>
</xsl:template>

<!-- to do -->
<xsl:template match="*[contains(@class,' topic/image ')]">
  <mediaobject>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'img'"/>
    </xsl:call-template>
    <imageobject>
      <imagedata>
        <xsl:if test="@href">
          <xsl:attribute name="fileref">
            <xsl:value-of select="@href"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@height">
          <xsl:attribute name="depth">
            <xsl:value-of select="@height"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@width">
          <xsl:attribute name="width">
            <xsl:value-of select="@width"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@align">
          <xsl:attribute name="align">
            <xsl:value-of select="@align"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="@placement" mode="deflate"/>
      </imagedata>
    </imageobject>
    <xsl:if test="@alt or @longdescref">
      <textobject remap="alt_attribute">
        <xsl:if test="@alt">
          <phrase remap="#PCDATA">
            <xsl:value-of select="@alt"/>
          </phrase>
        </xsl:if>
        <xsl:if test="@longdescref">
          <xref linkend="{@href}">
            <xsl:call-template name="setStandardAttr">
              <xsl:with-param name="IDPrefix" select="'xref'"/>
            </xsl:call-template>
          </xref>
        </xsl:if>
      </textobject>
    </xsl:if>
  </mediaobject>
</xsl:template>

<!-- to do: imagemap -->


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - MISCELLANEOUS AND DEFAULT
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
  <remark>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sp'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </remark>
</xsl:template>

<xsl:template match="*[ not( @class ) ]">
  <xsl:message>
    <xsl:text>No class attribute for </xsl:text>
    <xsl:value-of select="name(.)"/>
  </xsl:message>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/no-topic-nesting ')]"/>

<xsl:template match="*[@conref]">
  <!-- a well-known Docbook hack -->
  <inlinemediaobject role="{@parse}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'include'"/>
    </xsl:call-template>
    <imageobject>
      <imagedata format="linespecific" fileref="{@conref}"/>
    </imageobject>
  </inlinemediaobject>
</xsl:template>

<!-- default element rule -->
<xsl:template match="*">
<!-- to debug, uncomment the following message -->
<!--
  <xsl:message>
    <xsl:text>unmapped: </xsl:text>
    <xsl:value-of select="local-name(.)"/>
    <xsl:text> spec="</xsl:text>
    <xsl:value-of select="@class"/>
    <xsl:text>"</xsl:text>
  </xsl:message>
-->
  <xsl:apply-templates select="." mode="deflate"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - UTILITIES
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="setStandardAttr">
  <xsl:param name="IDPrefix" select="local-name(.)"/>
  <xsl:if test="not( @id )">
    <xsl:attribute name="id">
      <xsl:call-template name="provideID">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
    </xsl:attribute>
  </xsl:if>
  <xsl:attribute name="remap">
    <xsl:value-of select="local-name(.)"/>
  </xsl:attribute>
  <xsl:for-each select="@*">
    <xsl:call-template name="testStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
  </xsl:for-each>
</xsl:template>

<xsl:template name="testStandardAttr">
  <xsl:param name="IDPrefix"/>
  <xsl:param name="attrName" select="local-name(.)"/>
  <xsl:choose>
  <xsl:when test="$attrName='id'">
    <xsl:attribute name="id">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <xsl:when test="$attrName='spec'">
    <xsl:attribute name="remap">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <xsl:when test="$attrName='platform'">
    <xsl:attribute name="arch">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <xsl:when test="$attrName='product'">
    <xsl:attribute name="os">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <xsl:when test="$attrName='version'">
    <xsl:attribute name="revision">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <xsl:when test="$attrName='audience'">
    <xsl:attribute name="userlevel">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:when>
  <!-- leave non-standard attributes to be handled locally -->
  </xsl:choose>
</xsl:template>

<xsl:template name="provideID">
  <xsl:param name="IDPrefix" select="local-name(.)"/>
  <xsl:choose>
  <xsl:when test="@id">
    <xsl:value-of select="@id"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="concat( $IDPrefix, generate-id() )"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="copyAs">
  <xsl:param name="elementName" select="local-name(.)"/>
  <xsl:param name="hasRemap"    select="true()"/>
  <xsl:element name="{$elementName}">
    <xsl:for-each select="@*">
      <xsl:choose>
      <xsl:when test="local-name(.) = 'spec'">
        <xsl:if test="$hasRemap">
          <xsl:attribute name="remap">
            <xsl:value-of select="."/>
          </xsl:attribute>
        </xsl:if>
      </xsl:when>
      <xsl:when test="local-name(.) = 'class'">
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy/>
      </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template name="makeBlock">
  <xsl:param name="node" select="."/>
  <xsl:call-template name="makeBlockList">
    <xsl:with-param name="nodelist" select="$node/*|$node/text()"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="deflateBlock">
  <xsl:param name="id"       select="concat('elem', generate-id())"/>
  <xsl:param name="element"  select="local-name(.)"/>
  <xsl:param name="nodelist" select="*|text()"/>
  <xsl:call-template name="deflateElementStart">
    <xsl:with-param name="id"      select="$id"/>
    <xsl:with-param name="element" select="$element"/>
  </xsl:call-template>
  <xsl:call-template name="makeBlockList">
    <xsl:with-param name="nodelist" select="$nodelist"/>
  </xsl:call-template>
  <xsl:call-template name="deflateElementEnd">
    <xsl:with-param name="id"      select="$id"/>
    <xsl:with-param name="element" select="$element"/>
  </xsl:call-template>
</xsl:template>


<!-- where block is optional in DITA but required in Docbook,
     wrap PCDATA or phrase elements in para -->
<xsl:template name="makeBlockList">
  <xsl:param name="currnode" select="1"/>
  <xsl:param name="nodelist"/>
  <!-- default value must match $FindAny variable -->
  <xsl:param name="findTarget" select="1"/>
  <xsl:variable name="FindAny"      select="1"/>
  <xsl:variable name="FindBlock"    select="2"/>
  <xsl:variable name="FindNonBlock" select="3"/>
  <xsl:if test="count($nodelist)>=$currnode">
    <xsl:variable name="node" select="$nodelist[position()=$currnode]"/>
    <xsl:variable name="isBlock">
      <xsl:call-template name="isBlock">
        <xsl:with-param name="nodelist" select="$node"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
    <xsl:when test="$isBlock=1">
      <xsl:if test="$findTarget!=$FindNonBlock">
        <xsl:apply-templates select="$node"/>
        <xsl:call-template name="makeBlockList">
          <xsl:with-param name="nodelist"   select="$nodelist"/>
          <xsl:with-param name="currnode"   select="$currnode + 1"/>
          <xsl:with-param name="findTarget" select="$FindAny"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
      <xsl:when test="$findTarget=$FindAny">
        <xsl:variable name="nonblock">
          <xsl:apply-templates select="$node"/>
          <xsl:call-template name="makeBlockList">
            <xsl:with-param name="nodelist"   select="$nodelist"/>
            <xsl:with-param name="currnode"   select="$currnode + 1"/>
            <xsl:with-param name="findTarget" select="$FindNonBlock"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="normalize-space(string($nonblock))!=''">
          <para remap="#PCDATA">
            <xsl:copy-of select="$nonblock"/>
          </para>
        </xsl:if>
        <xsl:call-template name="makeBlockList">
          <xsl:with-param name="nodelist"   select="$nodelist"/>
          <xsl:with-param name="currnode"   select="$currnode + 1"/>
          <xsl:with-param name="findTarget" select="$FindBlock"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$findTarget=$FindNonBlock">
        <xsl:apply-templates select="$node"/>
        <xsl:call-template name="makeBlockList">
          <xsl:with-param name="nodelist"   select="$nodelist"/>
          <xsl:with-param name="currnode"   select="$currnode + 1"/>
          <xsl:with-param name="findTarget" select="$FindNonBlock"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$findTarget=$FindBlock">
        <xsl:call-template name="makeBlockList">
          <xsl:with-param name="nodelist"   select="$nodelist"/>
          <xsl:with-param name="currnode"   select="$currnode + 1"/>
          <xsl:with-param name="findTarget" select="$FindBlock"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>Unknown target type for find: </xsl:text>
          <xsl:value-of select="$findTarget"/>
        </xsl:message>
      </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template name="isBlock">
  <xsl:param name="nodelist" select="."/>
  <xsl:choose>
  <xsl:when test="boolean($nodelist[
      contains(@class,' topic/dl ')
      or contains(@class,' topic/fig ')
      or contains(@class,' topic/itemgroup ')
      or contains(@class,' topic/lines ')
      or contains(@class,' topic/lq ')
      or contains(@class,' topic/note ')
      or contains(@class,' topic/ol ')
      or contains(@class,' topic/p ')
      or contains(@class,' topic/pre ')
      or contains(@class,' topic/simpletable ')
      or contains(@class,' topic/table ')
      or contains(@class,' topic/ul ')
  ])">
    <xsl:value-of select="1"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="0"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="makeBlockCont">
  <xsl:param name="titleSpec" select="' topic/title '"/>
  <xsl:param name="titleNode" select="*[contains(@class,$titleSpec)]"/>
  <xsl:param name="isContReq" select="false()"/>
  <xsl:param name="IDPrefix"/>
  <xsl:variable name="hasBlocks">
    <xsl:call-template name="isBlock">
      <xsl:with-param name="nodelist" select="*"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
  <xsl:when test="$hasBlocks=1">
    <xsl:choose>
    <xsl:when test="$isContReq or $titleNode">
     <xsl:choose>
      <xsl:when test="count(*[not(contains(@class,$titleSpec))]) =
          count(*[contains(@class,' topic/qalist ')])">
        <qandaset>
          <xsl:call-template name="setStandardAttr">
            <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
          </xsl:call-template>
          <xsl:if test="$titleNode">
            <xsl:apply-templates select="$titleNode"/>
          </xsl:if>
          <xsl:apply-templates select="*[contains(@class,' topic/qalist ')]">
            <xsl:with-param name="element" select="'qandadiv'"/>
          </xsl:apply-templates>
        </qandaset>
      </xsl:when>
      <xsl:otherwise>
        <!-- probably need to add other mappings -->
        <xsl:variable name="element">
          <xsl:text>sidebar</xsl:text>
        </xsl:variable>
        <xsl:element name="{$element}">
          <xsl:call-template name="setStandardAttr">
            <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
          </xsl:call-template>
          <xsl:if test="$titleNode">
            <xsl:apply-templates select="$titleNode"/>
          </xsl:if>
          <xsl:call-template name="makeBlockList">
            <xsl:with-param name="nodelist"
                select="*[not(contains(@class,$titleSpec))]|text()"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="deflateBlock">
        <xsl:with-param name="nodelist"
            select="*[not(contains(@class,$titleSpec))]|text()"/>
      </xsl:call-template>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="makePara">
      <xsl:with-param name="titleSpec" select="$titleSpec"/>
      <xsl:with-param name="titleNode" select="$titleNode"/>
      <xsl:with-param name="IDPrefix"  select="$IDPrefix"/>
    </xsl:call-template>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="makePara">
  <xsl:param name="titleSpec" select="' topic/title '"/>
  <xsl:param name="titleNode" select="*[contains(@class,$titleSpec)]"/>
  <xsl:param name="role"      select="''"/>
  <xsl:param name="IDPrefix"/>
  <xsl:choose>
  <xsl:when test="$titleNode or @title">
    <formalpara>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:choose>
      <xsl:when test="$role!=''">
        <xsl:attribute name="role">
          <xsl:value-of select="$role"/>
        </xsl:attribute>
      </xsl:when>
      </xsl:choose>
      <xsl:choose>
      <xsl:when test="$titleNode">
        <xsl:apply-templates select="@title" mode="deflate"/>
        <title>
          <xsl:apply-templates select="$titleNode/*|$titleNode/text()"/>
        </title>
      </xsl:when>
      <xsl:otherwise>
        <title>
          <xsl:value-of select="@title"/>
        </title>
      </xsl:otherwise>
      </xsl:choose>
      <para>
        <xsl:apply-templates select="*[not(contains(@class,$titleSpec))]"/>
      </para>
    </formalpara>
  </xsl:when>
  <xsl:otherwise>
    <para>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </para>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="makeTitle">
  <xsl:param name="elemTitled"/>
  <xsl:param name="elemUntitled" select="$elemTitled"/>
  <xsl:param name="titleSpec"    select="' topic/title '"/>
  <xsl:param name="titleNode"    select="*[contains(@class,$titleSpec)]"/>
  <xsl:param name="role"         select="''"/>
  <xsl:param name="IDPrefix"/>
  <xsl:choose>
  <xsl:when test="$titleNode or @title">
    <xsl:element name="{$elemTitled}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:choose>
      <xsl:when test="$role!=''">
        <xsl:attribute name="role">
          <xsl:value-of select="$role"/>
        </xsl:attribute>
      </xsl:when>
      </xsl:choose>
      <xsl:choose>
      <xsl:when test="$titleNode">
        <xsl:apply-templates select="@title" mode="deflate"/>
        <title>
          <xsl:apply-templates select="$titleNode/*|$titleNode/text()"/>
        </title>
      </xsl:when>
      <xsl:otherwise>
        <title>
          <xsl:value-of select="@title"/>
        </title>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*[not(contains(@class,$titleSpec))]"/>
    </xsl:element>
  </xsl:when>
  <xsl:otherwise>
    <xsl:element name="{$elemUntitled}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="wrapTitle">
  <xsl:param name="wrapElem"/>
  <xsl:param name="coreElem"/>
  <xsl:param name="titleSpec" select="' topic/title '"/>
  <xsl:param name="titleNode" select="*[contains(@class,$titleSpec)]"/>
  <xsl:param name="role"      select="''"/>
  <xsl:param name="IDPrefix"/>
  <xsl:choose>
  <xsl:when test="$titleNode or @title">
    <xsl:element name="{$wrapElem}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:choose>
      <xsl:when test="$role!=''">
        <xsl:attribute name="role">
          <xsl:value-of select="$role"/>
        </xsl:attribute>
      </xsl:when>
      </xsl:choose>
      <xsl:choose>
      <xsl:when test="$titleNode">
        <xsl:apply-templates select="@title" mode="deflate"/>
        <title>
          <xsl:apply-templates select="$titleNode/*|$titleNode/text()"/>
        </title>
      </xsl:when>
      <xsl:otherwise>
        <title>
          <xsl:value-of select="@title"/>
        </title>
      </xsl:otherwise>
      </xsl:choose>
      <xsl:element name="{$coreElem}">
        <xsl:apply-templates select="*[not(contains(@class,$titleSpec))]"/>
      </xsl:element>
    </xsl:element>
  </xsl:when>
  <xsl:otherwise>
    <xsl:element name="{$coreElem}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="wrapLiteralTitle">
  <xsl:param name="classAttr"/>
  <xsl:param name="IDPrefix"/>
  <xsl:choose>
  <xsl:when test="@title">
    <blockquote>
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <title>
        <xsl:value-of select="@title"/>
      </title>
      <literallayout class="{$classAttr}">
        <xsl:apply-templates/>
      </literallayout>
    </blockquote>
  </xsl:when>
  <xsl:otherwise>
    <literallayout class="{$classAttr}">
      <xsl:call-template name="setStandardAttr">
        <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </literallayout>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="programtext">
  <xsl:param name="IDPrefix" select="'prgtxt'"/>
  <literal>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:if test="@optreq">
      <xsl:attribute name="role">
        <xsl:value-of select="@optreq"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@convar" mode="deflate"/>
    <xsl:apply-templates/>
  </literal>
</xsl:template>

<xsl:template name="countsemcols">
  <xsl:param name="row"/>
  <xsl:variable name="cols" select="$row/*[
      contains(@class,' topic/stentry ') or
      contains(@class,' topic/sthead ')]"/>
  <xsl:value-of select="count($cols)"/>
</xsl:template>

<xsl:template name="maxsemcol">
  <xsl:param name="rows"/>
  <xsl:param name="colcount" select="0"/>
  <xsl:variable name="firstcount">
    <xsl:call-template name="countsemcols">
      <xsl:with-param name="row" select="$rows[position() = 1]"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="newcount">
    <xsl:choose>
    <xsl:when test="$firstcount > $colcount">
      <xsl:value-of select="$firstcount"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$colcount"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="restrows" select="$rows[position() > 1]"/>
  <xsl:choose>
  <xsl:when test="$restrows">
    <xsl:call-template name="maxsemcol">
      <xsl:with-param name="rows" select="$restrows"/>
      <xsl:with-param name="colcount" select="$newcount"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$newcount"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Add for "New <data> element (#9)" in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/data ')]"/>

<!-- Add for "Support foreign content vocabularies such as 
     MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]"/>

</xsl:stylesheet>
