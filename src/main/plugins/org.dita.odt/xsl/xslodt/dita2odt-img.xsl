<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<!-- 
  <draw:frame text:anchor-type="as-char"
  svg:width="6in" svg:height="3.2736in" draw:z-index="0">
  <draw:image xlink:href="Pictures/100000000000027A0000015A35A01F85.jpg"
  xlink:type="simple" xlink:show="embed" xlink:actuate="onLoad"/>
  </draw:frame>

-->
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
  xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
  xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
  xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
  xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
  xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
  xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
  xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
  xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools" 
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  version="2.0" 
  exclude-result-prefixes="xs dita-ot">

  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:function name="dita-ot:to-inch" as="xs:double">
    <xsl:param name="length" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="contains($length, 'in')">
        <xsl:sequence select="number(substring-before($length, 'in'))"/>
      </xsl:when>
      <xsl:when test="contains($length, 'cm')">
        <xsl:sequence select="number(substring-before($length, 'cm')) div 2.54"/>
      </xsl:when>
      <xsl:when test="contains($length, 'mm')">
        <xsl:sequence select="number(substring-before($length, 'mm')) div 25.4"/>
      </xsl:when>
      <xsl:when test="contains($length, 'pt')">
        <xsl:sequence select="number(substring-before($length, 'pt')) div 72"/>
      </xsl:when>
      <xsl:when test="contains($length, 'pc')">
        <xsl:sequence select="number(substring-before($length, 'pc')) div 6"/>
      </xsl:when>
      <xsl:when test="contains($length, 'px')">
        <xsl:sequence select="number(substring-before($length, 'px')) div 96"/>
      </xsl:when>
      <xsl:when test="contains($length, 'em')">
        <xsl:sequence select="number(substring-before($length, 'em')) div 6"/>
      </xsl:when>
      <!-- default is px -->
      <xsl:otherwise>
        <xsl:sequence select="number($length) div 96"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:template match="*[contains(@class,' topic/image ')]">
  <xsl:if test="@href and not(@href='')">
    <!-- image meta data -->
      <xsl:variable name="scale" as="xs:double">
        <xsl:choose>
          <xsl:when test="@scale">
            <xsl:sequence select="@scale div 100"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="1"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="height">
        <xsl:choose>
          <xsl:when test="@height">
            <xsl:value-of select="dita-ot:to-inch(@height) * $scale"/>
          </xsl:when>
          <xsl:when test="@dita-ot:image-height">
            <xsl:value-of select="dita-ot:to-inch(@dita-ot:image-height) * $scale"/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="width">
        <xsl:choose>
          <xsl:when test="@width">
            <xsl:value-of select="dita-ot:to-inch(@width) * $scale"/>
          </xsl:when>
          <xsl:when test="@dita-ot:image-width">
            <xsl:value-of select="dita-ot:to-inch(@dita-ot:image-width) * $scale"/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <!-- nested by body or list -->
        <xsl:when test="parent::*[contains(@class, ' topic/body ')] or        parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
          <text:p>
            <text:span>
              <xsl:choose>
                <!-- FIXME: this will never match due to parent xsl:when -->
                <xsl:when test="parent::*[contains(@class, ' topic/fig ')][contains(@frame,'top ')]">
                  <!-- NOP if there is already a break implied by a parent property -->
                </xsl:when>
                <xsl:otherwise>
                  <xsl:choose>
                    <xsl:when test="(@placement='break')">
                      <!-- start add flagging styles -->
                      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="flagcheck"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="draw_image">
                <xsl:with-param name="height" select="$height"/>
                <xsl:with-param name="width" select="$width"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </text:span>
          </text:p>
        </xsl:when>
        <!-- nested by entry -->
        <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
          <!-- create p tag -->
          <text:p>
            <text:span>
              <xsl:choose>
                <!-- FIXME: this will never match due to parent xsl:when -->
                <xsl:when test="parent::*[contains(@class, ' topic/fig ')][contains(@frame,'top ')]">
                  <!-- NOP if there is already a break implied by a parent property -->
                </xsl:when>
                <xsl:otherwise>
                  <xsl:choose>
                    <xsl:when test="(@placement='break')">
                      <!-- start add flagging styles -->
                      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="flagcheck"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="draw_image">
                <xsl:with-param name="height" select="$height"/>
                <xsl:with-param name="width" select="$width"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </text:span>
          </text:p>
        </xsl:when>
        <!-- nested by stentry -->
        <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
          <text:p>
            <text:span>
              <xsl:choose>
                <!-- FIXME: this will never match due to parent xsl:when -->
                <xsl:when test="parent::*[contains(@class, ' topic/fig ')][contains(@frame,'top ')]">
                  <!-- NOP if there is already a break implied by a parent property -->
                </xsl:when>
                <xsl:otherwise>
                  <xsl:choose>
                    <xsl:when test="(@placement='break')">
                      <!-- start add flagging styles -->
                      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="flagcheck"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="draw_image">
                <xsl:with-param name="height" select="$height"/>
                <xsl:with-param name="width" select="$width"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </text:span>
          </text:p>
        </xsl:when>
        <!-- nested by other tags -->
        <xsl:otherwise>
          <text:span>
            <xsl:choose>
              <xsl:when test="parent::*[contains(@class, ' topic/fig ')][contains(@frame,'top ')]">
                <!-- NOP if there is already a break implied by a parent property -->
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="(@placement='break')">
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="flagcheck"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="draw_image">
              <xsl:with-param name="height" select="$height"/>
              <xsl:with-param name="width" select="$width"/>
            </xsl:call-template>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="draw_image">
    <xsl:param name="height"/>
    <xsl:param name="width"/>
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://')) and $height and $width">
        <draw:frame text:anchor-type="as-char" svg:y="-0.1in">  
          <xsl:if test="string(number($height)) != 'NaN'">
            <xsl:attribute name="svg:width">
              <xsl:value-of select="$height"/>
              <xsl:text>in</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="string(number($width)) != 'NaN'">
            <xsl:attribute name="svg:width">
              <xsl:choose>
                <xsl:when test="$width &gt; 6">6</xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$width"/>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:text>in</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <draw:image xlink:href="{translate(@href, '\', '/')}">
          </draw:image>
        </draw:frame>
      </xsl:when>
      <xsl:otherwise>
        <text:a xlink:href="{translate(@href, '\', '/')}" xlink:type="simple">
          <xsl:call-template name="gen-img-txt"/>
        </text:a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="draw_image_odt">
    <xsl:param name="height"/>
    <xsl:param name="width"/>
    <xsl:param name="imgsrc"/>
    <xsl:param name="alttext"/>
    <xsl:choose>
      <xsl:when test="not(contains($imgsrc,'://')) and $height and $width">
        <draw:frame text:anchor-type="as-char" svg:y="-0.1in">
          <xsl:if test="string(number($height)) != 'NaN'">
            <xsl:attribute name="svg:width">
              <xsl:value-of select="$height"/>
              <xsl:text>in</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="string(number($width)) != 'NaN'">
            <xsl:attribute name="svg:width">
              <xsl:choose>
                <xsl:when test="$width &gt; 6">6</xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$width"/>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:text>in</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <draw:image xlink:href="{translate($imgsrc, '\', '/')}">
          </draw:image>
        </draw:frame>
      </xsl:when>
      <xsl:otherwise>
        <text:span>
          <!-- 
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="translate($imgsrc, '\', '/')"/>
          </xsl:attribute>
          <xsl:attribute name="xlink:type">simple</xsl:attribute>
          -->
          <xsl:call-template name="gen-img-txt">
            <xsl:with-param name="alttext" select="$alttext"/>
          </xsl:call-template>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="gen-img-txt">
    <xsl:param name="alttext"/>
    <xsl:choose>
      <xsl:when test="$alttext != ''">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="$alttext"/>
        </xsl:call-template>
      </xsl:when>
     <xsl:when test="*[contains(@class,' topic/alt ')]">
       <xsl:value-of select="*[contains(@class,' topic/alt ')]"/>
     </xsl:when>
     <xsl:when test="startflag/alt-text">
       <xsl:value-of select="startflag/alt-text"/>
     </xsl:when>
     <xsl:when test="@alt and not(@alt='')">
       <xsl:value-of select="@alt"/>
     </xsl:when>
     <xsl:when test="text() or *">
       <xsl:apply-templates/>
     </xsl:when>
     <xsl:when test="@href">
       <xsl:value-of select="@href"/>
     </xsl:when>
     <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>