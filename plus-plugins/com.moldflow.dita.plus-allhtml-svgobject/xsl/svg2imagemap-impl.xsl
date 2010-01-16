<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:f = "http://www.moldflow.com/namespace/2008/svg/transform"
    xmlns:math="http://exslt.org/math"
    exclude-result-prefixes="svg xlink"
    extension-element-prefixes="math f xs">

    <!-- If SVG element has intrinsic height/width in absolute units, use these.  -->
    <xsl:param name="xresolution" select="72"/>
    <xsl:param name="yresolution" select="72"/>
    
    <!-- Regular expressions to match the @transform attribute. -->
    <xsl:variable name="regex-number" select="'[-\+]?([0-9]+|[0-9]*\.[0-9]+|[0-9]+\.)([eE][-\+]?[0-9]+)?'"/>
    <xsl:variable name="regex-matrix" select="concat('matrix\s*\(\s*', $regex-number, '(\s+,?\s*|,\s*)',
        $regex-number, '(\s+,?\s*|,\s*)', $regex-number, '(\s+,?\s*|,\s*)',
        $regex-number, '(\s+,?\s*|,\s*)', $regex-number, '(\s+,?\s*|,\s*)',
        $regex-number, '\s*\)' )"/>
    <xsl:variable name="regex-translate" select="concat('translate\s*\(\s*', $regex-number, '((\s+,?\s*|,\s*)', $regex-number, ')?\s*\)')"/>
    <xsl:variable name="regex-scale" select="concat('scale\s*\(\s*', $regex-number, '((\s+,?\s*|,\s*)', $regex-number, ')?\s*\)')"/>
    <xsl:variable name="regex-rotate" select="concat('rotate\s*\(\s*', $regex-number, '((\s+,?\s*|,\s*)', $regex-number, '(\s+,?\s*|,\s*)', $regex-number, ')?\s*\)')"/>
    <xsl:variable name="regex-skewX" select="concat('skewX\s*\(\s*', $regex-number, '\s*\)')"/>
    <xsl:variable name="regex-skewY" select="concat('skewX\s*\(\s*', $regex-number, '\s*\)')"/>
    
    <!-- Trigonometric functions. -->
    <xsl:function name="f:degree-to-radian" as="xs:float" use-when="function-available('math:constant')">
        <xsl:param name="x" as="xs:float"/>
        <xsl:sequence select="$x * math:constant('PI', 10) div 180.0"/>
    </xsl:function>
    
    <xsl:function name="f:cos" as="xs:float" use-when="function-available('math:cos')">
        <xsl:param name="x" as="xs:float"/>
        <xsl:sequence select="math:cos(f:degree-to-radian($x))"/>
    </xsl:function>

    <xsl:function name="f:sin" as="xs:float" use-when="function-available('math:sin')">
        <xsl:param name="x" as="xs:float"/>
        <xsl:sequence select="math:sin(f:degree-to-radian($x))"/>
    </xsl:function>

    <xsl:function name="f:tan" as="xs:float" use-when="function-available('math:tan')">
        <xsl:param name="x" as="xs:float"/>
        <xsl:sequence select="math:tan(f:degree-to-radian($x))"/>
    </xsl:function>
    
    <!-- Convert the first item in @transform into a transformation matrix. -->
    <xsl:function name="f:matrix-of-single-transform" as="xs:float+">
        <xsl:param name="transform" as="xs:string"/>

        <xsl:variable name="numbers" as="xs:float+">
            <xsl:for-each select="tokenize(substring-before(substring-after($transform, '('), ')'), '\s+,?\s*|,\s*')">
                <xsl:sequence select="xs:float(.)"/>
            </xsl:for-each>
        </xsl:variable>
        
        <xsl:choose>
            <xsl:when test="matches($transform, concat('^\s*', $regex-matrix, '\s*$'))">
                <xsl:sequence select="$numbers"/>
            </xsl:when>
            <xsl:when test="matches($transform, concat('^\s*', $regex-translate, '\s*$'))">
                <xsl:sequence select="1, 0, 0, 1, $numbers[1], (if (count($numbers) =1) then 0 else $numbers[2])"/>
            </xsl:when>
            <xsl:when test="matches($transform, concat('^\s*', $regex-scale, '\s*$'))">
                <xsl:sequence select="$numbers[1], 0, 0, if (count($numbers) =1) then $numbers[1] else $numbers[2], 0, 0"/>
            </xsl:when>
            <xsl:when test="matches($transform, concat('^\s*', $regex-rotate, '\s*$'))">
                <xsl:sequence select="f:cos($numbers[1]), f:sin($numbers[1]), -f:sin($numbers[1]), f:cos($numbers[1]),
                    (if (count($numbers) = 1) then 0 else $numbers[2])*(1 - f:cos($numbers[1])) + (if (count($numbers) = 1) then 0 else $numbers[3])*f:sin($numbers[1]),
                    (if (count($numbers) = 1) then 0 else $numbers[3])*(1 - f:cos($numbers[1])) - (if (count($numbers) = 1) then 0 else $numbers[2])*f:sin($numbers[1])"/>
            </xsl:when>
            <xsl:when test="matches($transform, concat('^\s*', $regex-skewX, '\s*$'))">
                <xsl:sequence select="1, 0, f:tan($numbers[1]), 1, 0, 0"/>
            </xsl:when>
            <xsl:when test="matches($transform, concat('^\s*', $regex-skewY, '\s*$'))">
                <xsl:sequence select="1, f:tan($numbers[1]), 0, 1, 0, 0"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">
                    <xsl:text>Unknown transform </xsl:text>
                    <xsl:value-of select="$transform"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <!-- Compute the effective transform of a cumulative chain of transforms in one @transform attribute. -->
    <xsl:function name="f:matrix-of-transform-series" as="xs:float+">
        <xsl:param name="transform" as="xs:string"/>
        
        <xsl:choose>
            <xsl:when test="not(contains($transform, ')'))">
                <!-- All transforms contain a right parenthesis. -->
                <xsl:message terminate="yes">
                    <xsl:text>Invalid transformation </xsl:text>
                    <xsl:value-of select="$transform"/>
                </xsl:message>
            </xsl:when>
            <xsl:when test="matches(substring-after($transform, ')'), '^\s*$')">
                <!-- This is the last (or only) transform. -->
                <xsl:sequence select="f:matrix-of-single-transform($transform)"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- Premultiply the first transform with the remaining transforms. -->
                <xsl:call-template name="multiply-matrix">
                    <xsl:with-param name="L" as="xs:float+" select="f:matrix-of-single-transform(concat(substring-before($transform, ')'), ')'))"/>
                    <xsl:with-param name="R" as="xs:float+">
                        <xsl:variable name="remainder" as="xs:string" select="substring-after($transform, ')')"/>
                        <xsl:choose>
                            <!-- Comma or space separation allowed. -->
                            <xsl:when test="matches($remainder, '^\s*,')">
                                <xsl:sequence select="f:matrix-of-transform-series(substring-after($remainder, ','))"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="f:matrix-of-transform-series($remainder)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <!-- Multiply transforms.  Top 2/3 of a 3x3 matrix multiplication. -->
    <xsl:template name="multiply-matrix" as="xs:float+">
        <xsl:param name="L" as="xs:float+"/>
        <xsl:param name="R" as="xs:float+"/>
        
        <xsl:sequence select="$L[1] * $R[1] + $L[3] * $R[2]"/>
        <xsl:sequence select="$L[2] * $R[1] + $L[4] * $R[2]"/>
        <xsl:sequence select="$L[1] * $R[3] + $L[3] * $R[4]"/>
        <xsl:sequence select="$L[2] * $R[3] + $L[4] * $R[4]"/>
        <xsl:sequence select="$L[1] * $R[5] + $L[3] * $R[6] + $L[5]"/>
        <xsl:sequence select="$L[2] * $R[5] + $L[4] * $R[6] + $L[6]"/>
    </xsl:template>

    <xsl:template name="transform-point" as="xs:float+">
        <xsl:param name="T" as="xs:float+"/>
        <xsl:param name="V" as="xs:float+"/>
        
        <xsl:sequence select="$T[1] * $V[1] + $T[3] * $V[2] + $T[5]"/>
        <xsl:sequence select="$T[2] * $V[1] + $T[4] * $V[2] + $T[6]"/>
    </xsl:template>
    
    <!-- Entry point. --> 
    <xsl:template match="/">
        <map>
            <xsl:apply-templates mode="traverse">
                <xsl:with-param name="current-transform" as="xs:float+" select="(1, 0, 0, 1, 0, 0)" tunnel="yes"/>
            </xsl:apply-templates>
        </map>
    </xsl:template>
    
    <!-- Override this template. -->
    <xsl:template match="node()"/>
    
    <!-- Most elements don't contain nested elements for traversal. -->
    <xsl:template match="node()" mode="traverse" priority="1">
        <xsl:apply-templates select="."/>
    </xsl:template>
    
    <!-- These elements can contain their own drawing elements. -->
    <xsl:template match="svg:svg | svg:g | svg:a" mode="traverse" priority="2">
        <xsl:apply-templates mode="traverse"/>
        <!-- Pass off handling to unmoded template. -->
        <xsl:apply-templates select="."/>
    </xsl:template>
    
    <!-- If a transform attribute is encountered, apply this transform to this and descendants. -->
    <xsl:template match="svg:*[@transform]" mode="traverse" priority="3">
        <xsl:param name="current-transform" tunnel="yes" as="xs:float+"/>
        
       <xsl:next-match>
            <xsl:with-param name="current-transform" as="xs:float+" tunnel="yes">
                <xsl:call-template name="multiply-matrix">
                    <xsl:with-param name="L" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="R" as="xs:float+" select="f:matrix-of-transform-series(string(@transform))"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:next-match>
    </xsl:template>
    
    <!-- svg:a is for hyperlinks. -->
    <xsl:template match="svg:a" mode="traverse" priority="4">
        <xsl:next-match>
            <xsl:with-param name="current-href" select="@xlink:href" tunnel="yes"/>
        </xsl:next-match>
    </xsl:template>
    
    <!-- svg element with viewbox. -->
    <xsl:template match="svg:svg[@viewBox]" priority="4">
        <xsl:param name="current-transform" tunnel="yes" as="xs:float+"/>
        
        <!-- Get viewbox coordinates. -->
        <xsl:variable name="numbers" as="xs:float+">
            <xsl:for-each select="tokenize(string(@viewBox), '\s+,?\s*|,\s*')">
                <xsl:sequence select="xs:float(.)"/>
            </xsl:for-each>
        </xsl:variable>
        
        <!-- Get intrinsic width/height in pixels. -->
        <xsl:variable name="width" as="xs:float">
            <xsl:choose>
                <xsl:when test="matches(@width, '^.*px$')">
                    <xsl:sequence select="number(substring-before(@width, 'px'))"/>
                </xsl:when>
                <xsl:when test="matches(@width, '^.*in$')">
                    <xsl:sequence select="number(substring-before(@width, 'in'))*$xresolution"/>
                </xsl:when>
                <xsl:when test="matches(@width, '^.*mm$')">
                    <xsl:sequence select="number(substring-before(@width, 'in'))*$xresolution*25.4"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        <xsl:text>Root element has unknown width.</xsl:text>
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="height" as="xs:float">
            <xsl:choose>
                <xsl:when test="matches(@height, '^.*px$')">
                    <xsl:sequence select="number(substring-before(@height, 'px'))"/>
                </xsl:when>
                <xsl:when test="matches(@height, '^.*in$')">
                    <xsl:sequence select="number(substring-before(@height, 'in'))*$xresolution"/>
                </xsl:when>
                <xsl:when test="matches(@height, '^.*mm$')">
                    <xsl:sequence select="number(substring-before(@height, 'in'))*$xresolution*25.4"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        <xsl:text>Root element has unknown height.</xsl:text>
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:next-match>
            <xsl:with-param name="current-transform" as="xs:float+" tunnel="yes">
                <xsl:call-template name="multiply-matrix">
                    <xsl:with-param name="L" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="R" as="xs:float+" select="$width div $numbers[3], 0, 0, $height div $numbers[4], -$numbers[1]*($width div $numbers[3]), -$numbers[2]*($height div $numbers[4])"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:next-match>
    </xsl:template>
</xsl:stylesheet>
