<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:modular="http://www.moldflow.com/namespace/2008/dita/plus-htmlhelp-hhp-modular">

    <!-- Override from map2hhp.xsl in DITA-OT. -->
    <xsl:template match="/">
        <xsl:apply-templates select="." mode="modular:options-section"/>
        <xsl:apply-templates select="." mode="modular:windows-section"/>
        <xsl:apply-templates select="." mode="modular:mergefiles-section"/>
        <xsl:apply-templates select="." mode="modular:files-section"/>
        <xsl:apply-templates select="." mode="modular:textpopups-section"/>
        <xsl:apply-templates select="." mode="modular:infotypes-section"/>
        <xsl:apply-templates select="." mode="modular:map-section"/>
        <xsl:apply-templates select="." mode="modular:alias-section"/>
        <xsl:apply-templates select="." mode="modular:subsets-section"/>
    </xsl:template>
    
    <!-- [OPTIONS] section -->
    <xsl:template match="/ | node()" mode="modular:options-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:options">
                <xsl:with-param name="modular:dita-ot-base" select="'yes'"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[OPTIONS]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:options">
        <!-- If the modular:dita-ot-base parameter has been set to "no" by a prior template in the match chain,
               don't add the default output from the base DITA-OT map2hhp template. -->
        <xsl:param name="modular:dita-ot-base" select="'yes'"/>
        <xsl:if test="$modular:dita-ot-base = 'yes'">
            <xsl:variable name="dita-ot-base-content">
                <xsl:call-template name="setup-options"/>
            </xsl:variable>
            <xsl:value-of select="substring-after($dita-ot-base-content, '[OPTIONS]')"/>
        </xsl:if>
    </xsl:template>
    
    <!-- [WINDOWS] section -->
    <xsl:template match="/ | node()" mode="modular:windows-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:windows"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[WINDOWS]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/ | node()" mode="modular:windows"/>
    
    <!-- [MERGE FILES] section -->
    <xsl:template match="/ | node()" mode="modular:mergefiles-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:mergefiles"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[MERGE FILES]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/ | node()" mode="modular:mergefiles"/>
    
    <!-- [FILES] section -->
    <xsl:template match="/ | node()" mode="modular:files-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:files">
                <xsl:with-param name="modular:dita-ot-base" select="'yes'"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[FILES]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:files">
        <!-- If the modular:dita-ot-base parameter has been set to "no" by a prior template in the match chain,
            don't add the default output from the base DITA-OT map2hhp template. -->
        <xsl:param name="modular:dita-ot-base" select="'yes'"/>
        <xsl:if test="$modular:dita-ot-base = 'yes'">
            <xsl:variable name="dita-ot-base-content">
                <xsl:call-template name="output-filenames"/>
            </xsl:variable>
            <xsl:value-of select="substring-after($dita-ot-base-content, '[FILES]')"/>
        </xsl:if>
    </xsl:template>
    
    <!-- [TEXT POPUPS] section -->
    <xsl:template match="/ | node()" mode="modular:textpopups-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:textpopups"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[TEXT POPUPS]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:textpopups"/>
    
    <!-- [INFOTYPES] section -->
    <xsl:template match="/ | node()" mode="modular:infotypes-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:infotypes"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[INFOTYPES]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:infotypes"/>

    <!-- [MAP] section -->
    <xsl:template match="/ | node()" mode="modular:map-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:map"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[MAP]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:map">
        <!-- Just re-implement the code from the DITA-OT base end-hhp template. -->
        <xsl:if test="string-length($HELPMAP)>0">
            <xsl:text>#include </xsl:text><xsl:value-of select="$HELPMAP"/><xsl:text>
</xsl:text>
        </xsl:if>        
    </xsl:template>

    <!-- [ALIAS] section -->
    <xsl:template match="/ | node()" mode="modular:alias-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:alias"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[ALIAS]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:alias">
        <!-- Just re-implement the code from the DITA-OT base end-hhp template. -->
        <xsl:if test="string-length($HELPALIAS)>0">
            <xsl:text>#include </xsl:text><xsl:value-of select="$HELPALIAS"/><xsl:text>
</xsl:text>
        </xsl:if>        
    </xsl:template>
    
    <!-- [SUBSETS] section -->
    <xsl:template match="/ | node()" mode="modular:subsets-section">
        <xsl:variable name="content">
            <xsl:apply-templates select="." mode="modular:subsets"/>
        </xsl:variable>
        <xsl:if test="string-length($content) &gt; 0">
            <xsl:text>
[SUBSETS]                
</xsl:text>
            <xsl:value-of select="$content"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/ | node()" mode="modular:subsets"/>
    
</xsl:stylesheet>
