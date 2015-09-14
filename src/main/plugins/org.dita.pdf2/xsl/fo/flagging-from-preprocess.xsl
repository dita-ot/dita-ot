<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="2.0">

  <!-- For reference, flagging info as it appears in the topics: -->
  <!--<ditaval-startprop class="+ topic/foreign ditaot-d/ditaval-startprop ">
    <prop action="flag" att="audience" val="p">
      <startflag imageref="yukface.jpg"><alt-text>Start P</alt-text></startflag>
    </prop>
    <prop action="flag" att="audience" backcolor="aqua" color="blue" style="italics" val="meep"/>
    <revprop action="flag" backcolor="maroon" changebar="|" color="olive" style="underline" val="testrev"/>
  </ditaval-startprop>
  <ditaval-endprop class="+ topic/foreign ditaot-d/ditaval-endprop ">
    <prop action="flag" att="audience" val="p">
      <endflag imageref="smileface.jpg"><alt-text>End P</alt-text></endflag>
    </prop>
  </ditaval-endprop>
  -->

  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ') or contains(@class,' ditaot-d/ditaval-endprop ')]
                        [parent::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ') or
                                   contains(@class,' topic/sl ')]]" priority="10">
    <!-- Process with "outofline" in lists.xsl -->
  </xsl:template>
  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ') or contains(@class,' ditaot-d/ditaval-endprop ')]
                        [parent::*[contains(@class,' topic/dl ') or contains(@class,' topic/dlhead ') or
                                   contains(@class,' topic/dlentry ')]]" priority="10">
    <!-- Process with "outofline" in tables.xsl -->
  </xsl:template>
  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ') or contains(@class,' ditaot-d/ditaval-endprop ')]
                        [parent::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ') or
                                   contains(@class,' topic/tgroup ') or contains(@class,' topic/tbody ') or
                                   contains(@class,' topic/thead ') or contains(@class,' topic/sthead ') or
                                   contains(@class,' topic/row ') or contains(@class,' topic/strow ')]]" priority="10">
    <!-- Process with "outofline" in tables.xsl -->
  </xsl:template>
  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ') or contains(@class,' ditaot-d/ditaval-endprop ')]
                        [parent::*[contains(@class,' topic/image ')]]" priority="10">
    <!-- Process with "outofline" in commons.xsl -->
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/stentry ')]" mode="ancestor-start-flag">
    <!-- If first stentry in a row, pick up start flag from the row -->
    <xsl:if test="not(preceding-sibling::*[contains(@class,' topic/stentry ')])">
      <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/stentry ')]" mode="ancestor-end-flag">
    <!-- If last stentry in a row, pick up end flag from the row -->
    <xsl:if test="not(following-sibling::*[contains(@class,' topic/stentry ')])">
      <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/entry ')]" mode="ancestor-start-flag">
    <xsl:if test="not(preceding-sibling::*[contains(@class,' topic/entry ')])">
      <!-- check tgroup, tbody or thead, row -->
      <xsl:apply-templates select="../../../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
      <xsl:apply-templates select="../../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
      <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/entry ')]" mode="ancestor-end-flag">
    <xsl:if test="not(following-sibling::*[contains(@class,' topic/entry ')])">
      <!-- check row, tbody or thead, tgroup -->
      <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline"/>
      <xsl:apply-templates select="../../*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline"/>
      <xsl:apply-templates select="../../../*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline"/>
    </xsl:if>
  </xsl:template>

    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')] |
                         *[contains(@class,' ditaot-d/ditaval-endprop ')]">
      <!-- Style flags called from common attributes -->
      <!--<xsl:apply-templates select="." mode="flag-attributes"/>-->
      <xsl:apply-templates select="revprop[@changebar]" mode="changebar">
        <xsl:with-param name="changebar-id" select="concat(generate-id(parent::*),'-cbar')"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="." mode="flag-images"/>
    </xsl:template>
    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')] |
                         *[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline">
      <xsl:apply-templates select="revprop[@changebar]" mode="changebar">
        <xsl:with-param name="changebar-id" select="concat(generate-id(parent::*),'-cbar')"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="." mode="flag-images"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="flag-attributes">
      <xsl:apply-templates select=".//prop/@backcolor | .//prop/@color | .//prop/@style |
                                   .//revprop/@backcolor | .//revprop/@color | .//revprop/@style" mode="flag-attributes"/>
    </xsl:template>
    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="flag-attributes">
      <!-- No flagging at end -->
    </xsl:template>

    <xsl:template match="@backcolor" mode="flag-attributes">
      <xsl:attribute name="background-color"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>
    <xsl:template match="@color" mode="flag-attributes">
      <xsl:attribute name="color"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>
    <xsl:template match="@style" mode="flag-attributes">
      <xsl:choose>
        <xsl:when test=".='bold'">
          <xsl:attribute name="font-weight">bold</xsl:attribute>
        </xsl:when>
        <xsl:when test=".='italics' or .='italic'">
          <xsl:attribute name="font-style">italic</xsl:attribute>
        </xsl:when>
        <xsl:when test=".='double-underline'">
          <xsl:attribute name="text-decoration">underline</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="text-decoration"><xsl:value-of select="."/></xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop" mode="changebar">
      <xsl:param name="changebar-id"/>
      <xsl:param name="changebar-style">
        <xsl:choose>
          <xsl:when test="@changebar='none' or @changebar='hidden' or @changebar='dotted' or
                          @changebar='dashed' or @changebar='solid' or @changebar='double' or
                          @changebar='groove' or @changebar='ridge' or @changebar='inset' or @changebar='outset'">
            <xsl:value-of select="@changebar"/>
          </xsl:when>
          <xsl:otherwise>groove</xsl:otherwise>
        </xsl:choose>
      </xsl:param>
      <xsl:param name="changebar-color">
        <!-- Could take color from @changebar, but for now take from @color to allow @changebar to set style; bar color matches text -->
        <xsl:choose>
          <xsl:when test="@color"><xsl:value-of select="@color"/></xsl:when>
          <xsl:otherwise>black</xsl:otherwise>
        </xsl:choose>
      </xsl:param>
      <fo:change-bar-begin 
        change-bar-class="{$changebar-id}" 
        change-bar-style="{$changebar-style}" 
        change-bar-color="{$changebar-color}" 
        change-bar-offset="2mm"/>
    </xsl:template>
    <xsl:template match="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop" mode="changebar">
      <xsl:param name="changebar-id"/>
      <fo:change-bar-end change-bar-class="{$changebar-id}"/>
    </xsl:template>

    <xsl:template match="*" mode="flag-images">
      <xsl:if test="*//startflag|*//endflag">
        <xsl:variable name="flags" as="element()*">
          <xsl:for-each select=".//startflag|.//endflag">
            <xsl:choose>
              <xsl:when test="@imageref">
                <image class="+ topic/image ditaot-d/flagimage " href="{@imageref}" placement="inline">
                  <alt class="- topic/alt "><xsl:value-of select="alt-text"/></alt>
                </image>
              </xsl:when>
              <xsl:when test="alt-text">
                <text class="+ topic/text ditaot-d/flagtext "> [<xsl:value-of select="alt-text"/>] </text>
              </xsl:when>
            </xsl:choose>
          </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class,' topic/dl ') or
                                    contains(@class,' topic/image ')]">
            <fo:inline xsl:use-attribute-sets="image__inline">
              <xsl:apply-templates select="$flags"/>
            </fo:inline>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="$flags"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:template>

</xsl:stylesheet>