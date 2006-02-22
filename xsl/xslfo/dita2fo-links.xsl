<?xml version='1.0'?>
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
      <xsl:when test="@format='dita' or @format='DITA' or not(@format)">
        <fo:block color="blue" text-decoration="underline" 
                  start-indent="{$basic-start-indent}">
          <xsl:text>&bullet; </xsl:text>
          <fo:basic-link>
            <xsl:attribute name="internal-destination">
              <xsl:call-template name="href"/>
            </xsl:attribute>
            <xsl:apply-templates/>
          </fo:basic-link>
        </fo:block>
      </xsl:when>
      <xsl:when test="@format='html' or @format='HTML'">
        <fo:block color="blue" text-decoration="underline" 
                  start-indent="{$basic-start-indent}">
          <xsl:text>&bullet; </xsl:text>
          <fo:basic-link>
            <xsl:attribute name="external-destination">
              <xsl:value-of select="@href"/>
            </xsl:attribute>
            <xsl:apply-templates/>
          </fo:basic-link>
        </fo:block>
      </xsl:when>
      <xsl:otherwise>
        <fo:block color="red" start-indent="{$basic-start-indent}">
          <xsl:text>&bullet; </xsl:text>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">051</xsl:with-param>
            <xsl:with-param name="msgsev">W</xsl:with-param>
            <xsl:with-param name="msgparams">%1=link;%2=<xsl:value-of select="@href"/>;%3=<xsl:value-of select="@format"/></xsl:with-param>
          </xsl:call-template>
          <xsl:apply-templates/>
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
        <xsl:when test="@format='dita' or @format='DITA' or not(@format)">
          <fo:inline color="blue">
          <fo:basic-link>
            <!-- Set the destination to the id attribute of the topic referred to by the href -->
            <xsl:attribute name="internal-destination">
              <xsl:call-template name="href"/>
            </xsl:attribute>
            <!--use content as linktext if it exists, otherwise use href as linktext-->
            <xsl:choose>
              <!--use xref content-->
              <xsl:when test="*|text()">
                <xsl:apply-templates select="*|text()"/>
              </xsl:when>
              <!--use href text-->
              <xsl:otherwise>
                <xsl:call-template name="href"/>
              </xsl:otherwise>
            </xsl:choose>
          </fo:basic-link>
          </fo:inline>
        </xsl:when>
        <xsl:otherwise>
          <!-- If the format attribute is html, then interpret the href as an external link -->
          <!-- (for example, to a website) -->
          <xsl:choose>
            <xsl:when test="@format='html' or @format='HTML'">
              <fo:inline color="blue">                
              <fo:basic-link>
                <xsl:attribute name="external-destination">
                  <xsl:value-of select="@href"/>
                </xsl:attribute>
                <!--use content as linktext if it exists, otherwise use href as linktext-->
                <xsl:choose>
                  <!--use xref content-->
                  <xsl:when test="*|text()">
                    <xsl:apply-templates select="*|text()"/>
                  </xsl:when>
                  <!--use href text-->
                  <xsl:otherwise>
                    <xsl:call-template name="href"/>
                  </xsl:otherwise>
                </xsl:choose>
              </fo:basic-link>
              </fo:inline>
            </xsl:when>
            <xsl:otherwise>
              <!-- xref format not recognized: output xref contents without creating a hyperlink -->
              <xsl:call-template name="output-message">
                <xsl:with-param name="msgnum">051</xsl:with-param>
                <xsl:with-param name="msgsev">W</xsl:with-param>
                <xsl:with-param name="msgparams">%1=xref;%2=<xsl:value-of select="@href"/>;%3=<xsl:value-of select="@format"/></xsl:with-param>
              </xsl:call-template>
              <fo:inline color="red">
                <fo:inline font-weight="bold">[xref to: <xsl:value-of select="@href"/>]</fo:inline>
                <xsl:apply-templates/>
              </fo:inline>
            </xsl:otherwise>
          </xsl:choose>
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
        <xsl:value-of select="document(substring-before(@href,'#'),/)/*/@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="document(@href,/)/*/@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =================== end of related links and xrefs ====================== -->
</xsl:stylesheet>
