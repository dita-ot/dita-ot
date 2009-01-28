<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
    xmlns:hash="com.moldflow.xslt.hash"
    exclude-result-prefixes="hash">

    <xsl:param name="plus-svgobject-format" select="'object'"/>
    <xsl:param name="plus-svgobject-raster-mimetype" select="'image/png'"/>
    <xsl:param name="plus-svgobject-object-convert-to-path" select="'yes'"/>
    <xsl:param name="plus-svgobject-raster-imagemap" select="'yes'"/>
    <xsl:param name="plus-svgobject-path" select="'svgobject'"/>

    <xsl:template name="svgobject:svgobject-reverse-path">
      <xsl:call-template name="svgobject:reverse-path">
        <xsl:with-param name="path" select="$plus-svgobject-path"/>
      </xsl:call-template>
    </xsl:template>

    <!-- Find the inverse of a path (correct number of ../). -->
    <xsl:template name="svgobject:reverse-path">
      <xsl:param name="path"/>
      <xsl:choose>
        <xsl:when test="$path = ''">
          <xsl:text>./</xsl:text>
        </xsl:when>
        <xsl:when test="$path = '.'">
          <xsl:text>./</xsl:text>
        </xsl:when>
        <xsl:when test="$path = '..'">
          <xsl:message terminate="yes">Cannot reverse a path containing ".."</xsl:message>
        </xsl:when>
        <xsl:when test="not(contains($path, '/'))">
          <xsl:text>../</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with($path, './')">
          <xsl:call-template name="svgobject:reverse-path">
            <xsl:with-param name="path" select="substring-after($path, '/')"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="starts-with($path, '../')">
          <xsl:message terminate="yes">Cannot reverse a path containing ".."</xsl:message>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>../</xsl:text>
          <xsl:call-template name="svgobject:reverse-path">
            <xsl:with-param name="path" select="substring-after($path, '/')"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>


    <xsl:template match="*" mode="svgobject:generate-reference">

       <xsl:param name="doctype-public" select="'-//W3C//DTD SVG 1.1//EN'"/>
       <xsl:param name="doctype-system" select="'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'"/>
       <xsl:param name="content"/>
       <xsl:param name="suffix" select="'.svg'"/>
       <xsl:param name="make-static" select="'no'"/>
       <xsl:param name="alt" select="/.."/>
       <xsl:param name="baseline-shift" select="'no'"/>
       <xsl:param name="convert-to-path" select="'yes'"/>
       <xsl:param name="imagemap" select="'yes'"/>

       <xsl:if test="number(system-property('xsl:version')) &lt; 2.0">
           <xsl:message terminate="yes">
               <xsl:text>External SVG links require an XSLT 2.0 processor.</xsl:text>
           </xsl:message>
       </xsl:if>

       <xsl:variable name="external-svg-name">
           <xsl:choose>
             <xsl:when test="function-available('hash:md5')" use-when="function-available('hash:md5')">
       	<xsl:value-of select="concat(hash:md5(document-uri(/)), '_', generate-id(.))"/>
             </xsl:when>
             <xsl:when test="true()">
       	<!-- Last 30 characters of document URI should be ample to make it unique. -->
       	<xsl:variable name="document-unique-string">
       	  <xsl:choose>
       	    <xsl:when test="string-length(document-uri(/)) &gt; 30">
       	      <xsl:value-of select="substring(document-uri(/), string-length(document-uri(/)) - 30)"/>
       	    </xsl:when>
       	    <xsl:otherwise>
       	      <xsl:value-of select="document-uri(/)"/>
       	    </xsl:otherwise>
       	  </xsl:choose>
       	</xsl:variable>
       	<xsl:value-of select="concat(translate(encode-for-uri($document-unique-string), '%', ''), '_', generate-id(.))"/>
             </xsl:when>
           </xsl:choose>
       </xsl:variable>

       <!-- Additional suffix bits which indicate back to the Ant code that the image
            needs to undergo more transformations (make static, convert to path). -->
       <xsl:variable name="topath-suffix">
         <xsl:choose>
           <xsl:when test="$plus-svgobject-object-convert-to-path = 'yes' and $convert-to-path = 'yes'">
             <xsl:text>_topath</xsl:text>
           </xsl:when>
         </xsl:choose>
       </xsl:variable>

       <xsl:variable name="dynamic-suffix">
         <xsl:choose>
           <xsl:when test="$make-static = 'yes'">
             <xsl:text>_dynamic</xsl:text>
           </xsl:when>
         </xsl:choose>
       </xsl:variable>

       <xsl:result-document
           href="{$PATH2PROJ}{$plus-svgobject-path}/{$external-svg-name}{$topath-suffix}{$dynamic-suffix}{$suffix}"
           doctype-public="{$doctype-public}" doctype-system="{$doctype-system}">
           <xsl:copy-of select="$content"/>
       </xsl:result-document>

       <xsl:variable name="image-element">
           <img>
               <xsl:attribute name="src">
                   <xsl:value-of
                       select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name)"/>
                   <xsl:choose>
                       <xsl:when test="$plus-svgobject-raster-mimetype = 'image/png'">
                         <xsl:text>.png</xsl:text>
                       </xsl:when>
                       <xsl:when test="$plus-svgobject-raster-mimetype = 'image/jpeg'">
                         <xsl:text>.jpg</xsl:text>
                       </xsl:when>
                       <xsl:when test="$plus-svgobject-raster-mimetype = 'image/tiff'">
                         <xsl:text>.tif</xsl:text>
                       </xsl:when>
                   </xsl:choose>
               </xsl:attribute>
               <xsl:if test="$imagemap = 'yes' and $plus-svgobject-raster-imagemap = 'yes'">
                 <xsl:attribute name="svgobject:usemap">
                   <xsl:value-of
                       select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, $topath-suffix, '.svg.imagemap')"/>
                 </xsl:attribute>
               </xsl:if>
               <xsl:if test="$alt">
                 <xsl:apply-templates select="$alt"/>
               </xsl:if>
           </img>
           <xsl:if test="$imagemap = 'yes' and $plus-svgobject-raster-imagemap = 'yes'">
             <xsl:processing-instruction name="plus-svgobject-raster-imagemap">
               <xsl:value-of
                 select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, $topath-suffix, '.svg.imagemap')"/>
             </xsl:processing-instruction>
           </xsl:if>
       </xsl:variable>
       <xsl:choose>
           <xsl:when test="$plus-svgobject-format='object'">
               <span>
                   <xsl:if test="$baseline-shift = 'yes'">
                     <xsl:attribute name="svgobject:baseline-shift">
                       <xsl:value-of
                           select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, $topath-suffix, $dynamic-suffix, '.svg.baseline')"/>
                     </xsl:attribute>
                   </xsl:if>
                   <object>
                       <!-- Leave placeholders for width/height to fix up in later pipeline stage,
                            perhaps after JavaScript has resized the SVG's bounding box. -->
                       <xsl:attribute name="svgobject:width"></xsl:attribute>
                       <xsl:attribute name="svgobject:height"></xsl:attribute>
                       <xsl:attribute name="svgobject:target">
                           <xsl:value-of
                               select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, $topath-suffix, $dynamic-suffix, '.svg')"/>
                       </xsl:attribute>
                       <xsl:attribute name="data">
                           <xsl:value-of
                             select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, '.svg')"
                           />
                       </xsl:attribute>
                       <xsl:attribute name="type">image/svg+xml</xsl:attribute>
                       <xsl:copy-of select="$image-element"/>
                   </object>
               </span>
           </xsl:when>
           <xsl:when test="$plus-svgobject-format='raster'">
               <span>
                   <xsl:if test="$baseline-shift = 'yes'">
                     <xsl:attribute name="svgobject:baseline-shift">
                       <xsl:value-of
                           select="concat($PATH2PROJ, $plus-svgobject-path, '/', $external-svg-name, $topath-suffix, $dynamic-suffix, '.svg.baseline')"/>
                     </xsl:attribute>
                   </xsl:if>
                   <xsl:copy-of select="$image-element"/>
               </span>
           </xsl:when>
           <xsl:otherwise>
               <xsl:message terminate="yes">
                   <xsl:text>Unknown svgobject format </xsl:text>
                   <xsl:value-of select="$plus-svgobject-format"></xsl:value-of>
               </xsl:message>
           </xsl:otherwise>
       </xsl:choose>

    </xsl:template>

</xsl:stylesheet>
