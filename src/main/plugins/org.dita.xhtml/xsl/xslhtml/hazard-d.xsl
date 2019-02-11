<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project. 
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
     xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     exclude-result-prefixes="ditamsg dita-ot xs">
  
  <xsl:variable name="inline-hazard-svg" as="element()">
    <svg class="hazardsymbol" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cc="http://creativecommons.org/ns#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:svg="http://www.w3.org/2000/svg"
      xmlns="http://www.w3.org/2000/svg" version="1.1" height="4em" viewBox="0 0 600 525">
      <metadata id="metadata3085">
        <rdf:RDF>
          <cc:Work rdf:about="">
            <dc:format>image/svg+xml</dc:format>
            <dc:type rdf:resource="http://purl.org/dc/dcmitype/StillImage" />
            <dc:title></dc:title>
          </cc:Work>
        </rdf:RDF>
      </metadata>
      <defs id="defs3073">
        <path
          d="M 2.8117,-1.046 A 3,3 0 0 1 0.5,2.958 V 4.5119 A 10.5,10.5 0 0 1 2,25.3078 v 0.5583 A 15,15 0 0 0 14.7975,8.5433 15,15 0 0 0 23.4007,-11.201 l -0.4835,0.2792 A 10.5,10.5 0 0 1 4.1574,-1.8229 z m 3.4148,8.871 a 10,10 0 0 1 -12.453,0 9.5,9.5 0 0 0 -2.1756,2.7417 13.5,13.5 0 0 0 16.8042,0 A 10,10 0 0 0 6.2265,7.825 z"
          transform="matrix(10,0,0,-10,260,260)" />
      </defs>
      <path
        d="M 597.6,499.6 313.8,8 C 310.9,3 305.6,0 299.9,0 294.2,0 288.9,3.1 286,8 L 2.2,499.6 c -2.9,5 -2.9,11.1 0,16 2.9,5 8.2,8 13.9,8 h 567.6 c 5.7,0 11,-3.1 13.9,-8 2.9,-5 2.9,-11.1 0,-16 z" />
      <polygon
        points="43.875,491.5 299.875,48.2 555.875,491.5 "
        transform="matrix(1,0,0,0.99591458,0.125,2.0332437)"
        style="fill:#f6bd16;fill-opacity:1;stroke:none;overflow:visible" />
      <path
        d="m -384.00937,417.52725 a 38.151581,36.156727 0 1 1 -76.30316,0 38.151581,36.156727 0 1 1 76.30316,0 z"
        transform="matrix(0.99319888,0,0,1.0479962,719.28979,-2.9357862)"
        style="fill:#000000;fill-opacity:1;stroke:#000000;stroke-width:0.62514842;stroke-linecap:square;stroke-miterlimit:4;stroke-opacity:0.4;stroke-dasharray:none;stroke-dashoffset:0" />
      <path
        d="m 300,168.60074 c -20.64745,0 -37.26716,16.97292 -37.26716,38.05658 l 11.01897,133.31318 c 2.10449,17.24457 3.90184,27.0149 11.01898,31.60966 4.64712,2.1172 9.79468,3.32214 15.22921,3.32214 5.40832,0 10.53383,-1.1913 15.16343,-3.28925 7.15697,-4.58178 8.97556,-14.35941 11.08476,-31.64255 l 11.01898,-133.31318 c 0,-21.08366 -16.61973,-38.05658 -37.26717,-38.05658 z"
        style="fill:#000000;fill-opacity:1;stroke:#000000;stroke-width:0.88582677;stroke-linecap:square;stroke-miterlimit:4;stroke-opacity:1;stroke-dashoffset:0" />
    </svg>
  </xsl:variable>
  
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]" mode="get-element-ancestry"><xsl:value-of select="name()"/></xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]/*" mode="get-element-ancestry"><xsl:value-of select="name()"/></xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/hazardstatement ')]">
    <xsl:variable name="type" select="(@type, 'caution')[1]" as="xs:string"/>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]"/>
    <table role="presentation" border="1">
      <xsl:call-template name="commonattributes"/>
      <tr>
        <th colspan="2" class="hazardstatement--{$type}">
          <xsl:if test="$type = ('danger', 'warning', 'caution')">
            <xsl:for-each select="$inline-hazard-svg">
              <xsl:copy>
                <xsl:sequence select="@*"/>
                <xsl:attribute name="height" select="'1em'"/>
                <xsl:sequence select="*"/>
              </xsl:copy>
            </xsl:for-each>
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:choose>
            <xsl:when test="$type='other'"><xsl:value-of select="@othertype"></xsl:value-of></xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="dita-ot:capitalize($type)"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </th>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="*[contains(@class,' hazard-d/hazardsymbol ')]"/>
          <xsl:if test="empty(*[contains(@class,' hazard-d/hazardsymbol ')])">
            <xsl:sequence select="$inline-hazard-svg"/>
          </xsl:if>
        </td>
        <td>
          <xsl:apply-templates select="*[contains(@class,' hazard-d/messagepanel ')]"/>
        </td>
      </tr>
    </table>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]/*">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
</xsl:stylesheet>
