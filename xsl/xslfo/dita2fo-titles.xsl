<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>
 


<!-- =================== start of element rules ====================== -->

<!-- NESTED TOPIC TITLES (sensitive to nesting depth, but are still processed for contained markup) -->

<!-- h1 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="2">
  <fo:block xsl:use-attribute-sets="topictitle1" padding-top="1.4pc">
    <fo:block border-top-color="black" border-top-width="3pt" line-height="100%"
              border-left-width="0pt" border-right-width="0pt">
      <xsl:call-template name="get-title"/>
    </fo:block>
  </fo:block>
</xsl:template>

<!-- h2 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="3">
  <fo:block xsl:use-attribute-sets="topictitle2" padding-top="1pc">
    <fo:block border-top-color="black" border-top-width="1pt"
              border-left-width="0pt" border-right-width="0pt">
      <xsl:call-template name="get-title"/>
    </fo:block>
  </fo:block>
</xsl:template>

<!-- h3 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="4">
  <fo:block xsl:use-attribute-sets="topictitle3">
    <xsl:call-template name="get-title"/>
  </fo:block>
</xsl:template>

<!-- h4 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="5">
  <fo:block xsl:use-attribute-sets="topictitle4">
    <xsl:if test="$trace='yes'"><fo:inline color="purple"></fo:inline></xsl:if>
    <xsl:call-template name="get-title"/>
  </fo:block>
</xsl:template>

<!-- h5 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="6">
  <fo:block xsl:use-attribute-sets="topictitle5">
    <xsl:call-template name="get-title"/><xsl:text>: </xsl:text>
  </fo:block>
</xsl:template>

<!-- h6 -->
<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" priority="7">
  <fo:block xsl:use-attribute-sets="topictitle6">
    <xsl:call-template name="get-title"/><xsl:text>: </xsl:text>
  </fo:block>
</xsl:template>


<!-- section/title handling -->

<xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/title ')]">
  <fo:block font-weight="bold">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- example/title handing -->

<xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
  <fo:block font-weight="bold">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- table/title handling -->

<xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]">
  <xsl:variable name="tbl-pfx-txt">
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Table'"/>
    </xsl:call-template>
    <xsl:value-of select="count(preceding::*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')])+1"/>
  </xsl:variable>
  <fo:block font-weight="bold">
    <fo:inline color="red"><xsl:value-of select="$tbl-pfx-txt"/>. </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- fig/title handling -->

<xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
  <xsl:variable name="fig-pfx-txt">
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Figure'"/>
    </xsl:call-template>
    <xsl:value-of select="count(preceding::*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')])+1"/>
  </xsl:variable>
  <fo:block font-weight="bold">
    <fo:inline color="red"><xsl:value-of select="$fig-pfx-txt"/>. </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>




<xsl:template name="place-tbl-lbl">
<xsl:variable name="tbl-count">                  <!-- Number of table/title's before this one -->
 <xsl:number count="*/table/title" level="multiple"/> <!-- was ANY-->
</xsl:variable>
<xsl:variable name="tbl-count-actual">           <!-- Number of table/title's including this one -->
 <xsl:choose>
   <xsl:when test="not($tbl-count&gt;0) and not($tbl-count=0) and not($tbl-count&lt;0)">1</xsl:when>
   <xsl:otherwise><xsl:value-of select="$tbl-count+1"/></xsl:otherwise>
 </xsl:choose>
</xsl:variable>
  <xsl:choose>
    <xsl:when test="*[contains(@class,' topic/title ')]">
      <fo:block><fo:inline font-weight="bold">
        <xsl:call-template name="getString">
         <xsl:with-param name="stringName" select="'Table'"/>
        </xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$tbl-count-actual"/>.<xsl:text> </xsl:text>
        <xsl:apply-templates select="*[contains(@class,' topic/title ')]" mode="exhibittitle"/>
      </fo:inline>
      <xsl:if test="*[contains(@class,' topic/desc ')]">
        <xsl:text>. </xsl:text><xsl:apply-templates select="*[contains(@class,' topic/desc ')]" mode="exhibitdesc"/>
      </xsl:if>
      </fo:block>
    </xsl:when>
    <xsl:when test="*[contains(@class,' topic/desc ')]">
      <fo:block>****<xsl:value-of select="*[contains(@class,' topic/desc ')]"/></fo:block>
    </xsl:when>
  </xsl:choose>
</xsl:template>


<xsl:template name="place-fig-lbl">
<xsl:variable name="fig-count">                 <!-- Number of fig/title's before this one -->
 <xsl:number count="*/fig/title" level="multiple"/>
</xsl:variable>
<xsl:variable name="fig-count-actual">          <!-- Number of fig/title's including this one -->
 <xsl:choose>
   <xsl:when test="not($fig-count&gt;0) and not($fig-count=0) and not($fig-count&lt;0)">1</xsl:when>
   <xsl:otherwise><xsl:value-of select="$fig-count+1"/></xsl:otherwise>
 </xsl:choose>
</xsl:variable>
  <xsl:choose>
    <xsl:when test="*[contains(@class,' topic/title ')]">
      <fo:block><fo:inline font-weight="bold">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Figure'"/>
        </xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$fig-count-actual"/>.<xsl:text> </xsl:text>
        <xsl:apply-templates select="*[contains(@class,' topic/title ')]" mode="exhibittitle"/>
      </fo:inline>
      <xsl:if test="desc">
        <xsl:text>. </xsl:text><xsl:apply-templates select="*[contains(@class,' topic/desc ')]" mode="exhibitdesc"/>
      </xsl:if>
      </fo:block>
    </xsl:when>
    <xsl:when test="*[contains(@class, ' topic/desc ')]">
      <fo:block>****<xsl:value-of select="*[contains(@class,' topic/desc ')]"/></fo:block>
    </xsl:when>
  </xsl:choose>
</xsl:template>



<!-- ======== NAMED TEMPLATES for labels and titles related to topic structures ======== -->

<xsl:template name="get-title"><!-- get fully-processed title content by whatever mechanism -->
   <!-- insert anchor for PDF bookmark, using id attribute of topic element if id exists,
        otherwise, generate it base on the topic element -->
   <!-- inserting the anchor here ensures that it is on the same page as the topic title, not the page before -->
   <xsl:choose>
     <xsl:when test="parent::*/@id">
       <xsl:apply-templates select="parent::*/@id"/>
     </xsl:when>
     <!-- only generate id for topic/title -->
     <xsl:when test="parent::*[contains(@class, ' topic/topic ')]">
       <fo:inline id="{generate-id(parent::*)}"></fo:inline>
     </xsl:when>
     <xsl:otherwise/>
   </xsl:choose>
   <xsl:choose>
   <!-- add keycol here once implemented -->
   <xsl:when test="@spectitle">
     <xsl:value-of select="@spectitle"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates/> <!-- select="title|*[contains(@class,' topic/title ')]"/-->
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="get-sect-heading">
     <xsl:choose>
      <!-- replace with keyref once implemented -->
      <xsl:when test="@spectitle">
        <xsl:value-of select="@spectitle"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="title"/>
      </xsl:otherwise>
     </xsl:choose>
</xsl:template>


<xsl:template name="sect-heading">
  <xsl:param name="deftitle" select="."/> <!-- get param by reference -->
  <xsl:variable name="heading">
     <xsl:choose>
      <xsl:when test="*[contains(@class,' topic/title ')]">
        <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
      </xsl:when>
      <xsl:when test="@spectitle">
        <xsl:value-of select="@spectitle"/>
      </xsl:when>
      <xsl:otherwise/>
     </xsl:choose>
  </xsl:variable>

  <!-- based on graceful defaults, build an appropriate section-level heading -->
  <xsl:choose>
    <xsl:when test="not($heading='')">
      <xsl:if test="normalize-space($heading)=''">
        <!-- hack: a title with whitespace ALWAYS overrides as null -->
        <!--xsl:comment>no heading</xsl:comment-->
      </xsl:if>
      <!--xsl:call-template name="proc-ing"/--><xsl:value-of select="$heading"/>
    </xsl:when>
    <xsl:when test="$deftitle">
      <xsl:value-of select="$deftitle"/>
    </xsl:when>
    <xsl:otherwise><!-- no heading title, output section starting with a break --></xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>
