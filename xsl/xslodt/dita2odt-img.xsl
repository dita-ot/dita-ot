<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
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
  version="1.0" 
  xmlns:java="org.dita.dost.util.ImgUtils" exclude-result-prefixes="java">

<xsl:output method="xml"/>
<xsl:output indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="*[contains(@class,' topic/image ')]">
<xsl:if test="@href and not(@href='')">
    
    <!-- image meta data -->
    <xsl:variable name="scale">
      <xsl:choose>
        <xsl:when test="@scale">
          <xsl:value-of select="@scale div 100"/>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="height">
      <xsl:choose>
        <xsl:when test="@height">
          <xsl:choose>
            <xsl:when test="contains(@height, 'in')">
              <xsl:value-of select="substring-before(@height, 'in') * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'cm')">
              <xsl:value-of select="number(round(substring-before(@height, 'cm') div 2.54)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'mm')">
              <xsl:value-of select="number(round(substring-before(@height, 'mm') div 25.4)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'pt')">
              <xsl:value-of select="number(round(substring-before(@height, 'pt') div 72)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'pc')">
              <xsl:value-of select="number(round(substring-before(@height, 'pc') div 6)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'px')">
              <xsl:value-of select="number(round(substring-before(@height, 'px') div 96)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'em')">
              <xsl:value-of select="number(round(substring-before(@height, 'em') div 6)) * $scale"/>
            </xsl:when>
            <!-- default is px -->
            <xsl:otherwise>
              <xsl:value-of select="number(@height div 96) * $scale"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(@href,'://'))">
          <xsl:value-of select="number(java:getHeight($OUTPUTDIR, string(@href)) div 96) * $scale"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="@width">
          <xsl:choose>
            <xsl:when test="contains(@width, 'in')">
              <xsl:value-of select="substring-before(@width, 'in') * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'cm')">
              <xsl:value-of select="number(round(substring-before(@width, 'cm') div 2.54)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'mm')">
              <xsl:value-of select="number(round(substring-before(@width, 'mm') div 25.4)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'pt')">
              <xsl:value-of select="number(round(substring-before(@width, 'pt') div 72)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'pc')">
              <xsl:value-of select="number(round(substring-before(@width, 'pc') div 6)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'px')">
              <xsl:value-of select="number(round(substring-before(@width, 'px') div 96)) * $scale"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'em')">
              <xsl:value-of select="number(round(substring-before(@width, 'em') div 6)) * $scale"/>
            </xsl:when>
            <!-- default is px -->
            <xsl:otherwise>
              <xsl:value-of select="number(@width div 96) * $scale"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(@href,'://'))">
          <xsl:value-of select="number(java:getWidth($OUTPUTDIR, string(@href)) div 96) * $scale"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
  
  <xsl:choose>
    <!-- nested by body or list -->
    <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:choose>
            <xsl:when test="parent::fig[contains(@frame,'top ')]">
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
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <!-- nested by entry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
      <!-- create p tag -->
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:choose>
            <xsl:when test="parent::fig[contains(@frame,'top ')]">
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
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <!-- nested by stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:choose>
            <xsl:when test="parent::fig[contains(@frame,'top ')]">
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
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <!-- nested by other tags -->
    <xsl:otherwise>
      <xsl:element name="text:span">
        <xsl:choose>
          <xsl:when test="parent::fig[contains(@frame,'top ')]">
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
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>

</xsl:if>
</xsl:template>
  
  <xsl:template name="draw_image">
    <xsl:param name="type"/><!-- Deprecated -->
    <xsl:param name="height"/>
    <xsl:param name="width"/>
    
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://')) and ($height &gt; 0) and ($width &gt; 0)">
        <xsl:element name="draw:frame">  
          <xsl:attribute name="text:anchor-type">as-char</xsl:attribute>
          <xsl:attribute name="svg:y">-0.1in</xsl:attribute>
          <xsl:attribute name="svg:width">
            <xsl:choose>
              <xsl:when test="$width &gt; 6">6</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$width"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="'in'"/>
          </xsl:attribute>
          <xsl:attribute name="svg:height"><xsl:value-of select="$height"/>in</xsl:attribute>       
          <xsl:element name="draw:image">
            <xsl:choose>
              <xsl:when test="$ODTIMGEMBED = 'yes'">
                <xsl:element name="office:binary-data">
                  <xsl:value-of select="java:getBASE64($OUTPUTDIR, string(@href))" disable-output-escaping="yes"/>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="xlink:href">
                  <xsl:value-of select="translate(@href, '\', '/')"/>
                </xsl:attribute>  
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:a">
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="translate(@href, '\', '/')"/>
          </xsl:attribute>
          <xsl:attribute name="xlink:type">simple</xsl:attribute>
          <xsl:call-template name="gen-img-txt"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template name="draw_image_odt">
    <xsl:param name="type"/><!-- Deprecated -->
    <xsl:param name="height"/>
    <xsl:param name="width"/>
    <xsl:param name="imgsrc"/>
    <xsl:param name="alttext"/>
    <xsl:choose>
      <xsl:when test="not(contains($imgsrc,'://')) and ($height &gt; 0) and ($width &gt; 0)">
        <xsl:element name="draw:frame">  
          <xsl:attribute name="text:anchor-type">as-char</xsl:attribute>
          <xsl:attribute name="svg:y">-0.1in</xsl:attribute>
          <xsl:attribute name="svg:width">
            <xsl:choose>
              <xsl:when test="$width &gt; 6">6</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$width"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="'in'"/>
          </xsl:attribute>
          <xsl:attribute name="svg:height"><xsl:value-of select="$height"/>in</xsl:attribute>       
          <xsl:element name="draw:image">
            <xsl:choose>
              <xsl:when test="$ODTIMGEMBED = 'yes'">
                <xsl:element name="office:binary-data">
                  <xsl:value-of select="java:getBASE64($OUTPUTDIR, string($imgsrc))" disable-output-escaping="yes"/>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="xlink:href">
                  <xsl:value-of select="translate($imgsrc, '\', '/')"/>
                </xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:span">
          <!-- 
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="translate($imgsrc, '\', '/')"/>
          </xsl:attribute>
          <xsl:attribute name="xlink:type">simple</xsl:attribute>
          -->
          <xsl:call-template name="gen-img-txt">
            <xsl:with-param name="alttext" select="$alttext"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

<xsl:template name="gen-img-txt">
  <xsl:param name="alttext"/>
   <xsl:choose>
     <xsl:when test="$alttext != ''">
       <xsl:call-template name="getStringODT">
         <xsl:with-param name="stringName" select="$alttext"/>
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