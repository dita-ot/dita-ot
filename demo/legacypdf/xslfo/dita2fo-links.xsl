<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY bullet        "&#x2022;"><!--check these two for better assignments -->
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
  <!--==== related-links content subset ====-->
  <xsl:template match="*[contains(@class,' topic/related-links ')]">
    <xsl:if test="$output-related-links='yes'">
      <fo:block>
        <fo:block font-weight="bold">
          <xsl:text>Related Links</xsl:text>
        </fo:block>
        <fo:block>
          <xsl:attribute name="start-indent">
            <xsl:value-of select="$basic-start-indent"/>
          </xsl:attribute>
          <xsl:apply-templates/>
        </fo:block>
      </fo:block>
    </xsl:if>
  </xsl:template>
  <xsl:template 
    match="*[contains(@class,' topic/linklist ')]/*[contains(@class,' topic/title ')]">
    <fo:block font-weight="bold">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linklist ')]">
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linkpool ')]">
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linktext ')]">
    <fo:inline>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <!-- rule for when the link is still not pointing at anything -->
  <xsl:template match="*[contains(@class,' topic/link ')][@href = '']">
    <fo:block color="red" 
      start-indent="{$basic-start-indent}">
      <xsl:text>&bullet; </xsl:text>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <!-- if there is an href, make it look like a link and remove prompt -->
  <xsl:template match="*[contains(@class,' topic/link ')][not(@href = '')]">
    <xsl:choose>
      <xsl:when test="not(@scope='external') and (@format='dita' or @format='DITA' or not(@format))">
        <fo:block color="blue" text-decoration="underline" 
                  start-indent="{$basic-start-indent}">
          <xsl:text>&bullet; </xsl:text>
          <fo:basic-link>
            <xsl:attribute name="internal-destination">
              <xsl:call-template name="href"/>
            </xsl:attribute>
            <xsl:apply-templates select="*[contains(@class,' topic/linktext ')]|text()"/>
            <!--use linktext as linktext if it exists, otherwise use href as linktext-->
            <xsl:choose>
              <xsl:when test="*[contains(@class, ' topic/linktext ')]">
                <xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]"/>
              </xsl:when>
              <xsl:otherwise>
                <!--use href text-->
                <xsl:value-of select="@href"/>
              </xsl:otherwise>
            </xsl:choose>
          </fo:basic-link>
        </fo:block>
      </xsl:when>
      <xsl:otherwise>
        <fo:block color="blue" text-decoration="underline" 
                  start-indent="{$basic-start-indent}">
          <xsl:text>&bullet; </xsl:text>
          <fo:basic-link>
            <xsl:attribute name="external-destination">
              <xsl:value-of select="@href"/>
            </xsl:attribute>
            <!--use linktext as linktext if it exists, otherwise use href as linktext-->
            <xsl:choose>
              <xsl:when test="*[contains(@class, ' topic/linktext ')]">
                <xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]"/>
              </xsl:when>
              <xsl:otherwise>
                <!-- use href text -->
                <xsl:value-of select="@href"/>
              </xsl:otherwise>
            </xsl:choose>
          </fo:basic-link>
        </fo:block>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- ===============  end of related links markup  ================= -->
  <!-- ===============  xref section (these occur in body) ================= -->
  <!-- rule for when the xref is still not pointing at anything -->
  <xsl:template match="*[contains(@class,' topic/xref ')][@href = '']">
    <fo:inline color="red">
      <fo:inline font-weight="bold">[xref to: <xsl:value-of 
        select="@href"/>]</fo:inline>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <!-- if the xref element contains an href attribute, then create a hyperlink -->
  <xsl:template match="*[contains(@class,' topic/xref ')][not(@href='')]">
      <xsl:choose>
        <!-- If the format attribute is dita, or is unspecified, then interpret the href as a topic -->
        <!-- Create an internal hyperlink to the topic -->
        <xsl:when test="not(@scope='external') and (@format='dita' or @format='DITA' or not(@format))">
          <fo:inline color="blue">
          <fo:basic-link>
            <!-- Set the destination to the id attribute of the topic referred to by the href -->
            <xsl:attribute name="internal-destination">
              <xsl:call-template name="href"/>
            </xsl:attribute>
            <!--use content as linktext if it exists, otherwise use href as linktext-->
            <xsl:choose>
              <!--use xref content-->
              <xsl:when test="text()">
                <xsl:apply-templates select="text()"/>
              </xsl:when>
              <!--use href text-->
              <xsl:otherwise>
                <xsl:value-of select="@href"/>
              </xsl:otherwise>
            </xsl:choose>
          </fo:basic-link>
          </fo:inline>
          <fo:inline>
            <xsl:variable name="href-id">
              <xsl:call-template name="href"/>
            </xsl:variable>
            <xsl:text> on page </xsl:text><fo:page-number-citation ref-id="{$href-id}"/>
          </fo:inline>          
        </xsl:when>
        <xsl:otherwise>
          <fo:inline color="blue">
            <fo:basic-link>
              <xsl:attribute name="external-destination">
                <xsl:value-of select="@href"/>
              </xsl:attribute>
              <!--use content as linktext if it exists, otherwise use href as linktext-->
              <xsl:choose>
                <!--use xref content-->
                <xsl:when test="text()">
                  <xsl:apply-templates select="text()"/>
                </xsl:when>
                <!--use href text-->
                <xsl:otherwise>
                  <xsl:value-of select="@href"/>
                </xsl:otherwise>
              </xsl:choose>
            </fo:basic-link>
          </fo:inline>
        </xsl:otherwise>
      </xsl:choose>

  </xsl:template>
  
  <xsl:template name="href">
    <xsl:choose>
      <!-- If the href contains a # character, then the topic file name is the preceding substring -->
      <xsl:when test="starts-with(@href,'#')">
        <xsl:choose>
          <xsl:when test="contains(substring-after(@href,'#'),'/')">
            <xsl:value-of 
              select="substring-before(substring-after(@href,'#'),'/')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-after(@href,'#')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="contains(@href,'#')">
        <xsl:value-of select="document(substring-before(@href,'#'),/)//descendant::*[contains(@class,' topic/topic ')][1]/@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="document(@href,/)/descendant::*[contains(@class,' topic/topic ')][1]/@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =================== end of related links and xrefs ====================== -->
</xsl:stylesheet>
