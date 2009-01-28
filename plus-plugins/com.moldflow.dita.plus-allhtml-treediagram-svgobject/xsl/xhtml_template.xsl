<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:treediagram-svgobject="http://www.moldflow.com/namespace/2008/allhtml-treediagram-svgobject"
    xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
    xmlns:treediagram2svg="http://www.moldflow.com/namespace/2008/treediagram2svg"
    exclude-result-prefixes="treediagram-svgobject treediagram2svg">

    <dita:extension id="treediagram2svg.pull.xsl" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

    <xsl:param name="plus-treediagram-format" select="'svgobject'"/>
    <xsl:param name="plus-allhtml-treediagram-svgobject-csspath" select="''"/>
    <xsl:param name="plus-allhtml-treediagram-svgobject-jspath" select="''"/>

    <!-- Entry point. -->
    <xsl:template match="*[contains(@class, ' tree-d/tree ')]">
        <xsl:choose>
          <xsl:when test="$plus-treediagram-format = 'svgobject'">
            <xsl:apply-templates select="." mode="treediagram-svgobject:default"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>treediagram-svgobject: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Top-level tree diagram elements. -->
    <xsl:template match="*[contains(@class, ' tree-d/tree ')]"
        mode="treediagram-svgobject:default">
        <div>
            <xsl:attribute name="class">treediagram</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <!-- Apply title of tree diagram. -->
            <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="treediagram-svgobject:default"/>
            <!-- Apply body of tree diagram. -->
            <xsl:apply-templates select="." mode="svgobject:generate-reference">
              <xsl:with-param name="content">
                <xsl:call-template name="treediagram2svg:create-svg-document">
                   <xsl:with-param name="CSSPATH">
                       <xsl:call-template name="svgobject:svgobject-reverse-path"/>
                       <xsl:value-of select="$plus-allhtml-treediagram-svgobject-csspath"/>
                   </xsl:with-param>
                   <xsl:with-param name="JSPATH">
                       <xsl:call-template name="svgobject:svgobject-reverse-path"/>
                       <xsl:value-of select="$plus-allhtml-treediagram-svgobject-jspath"/>
                   </xsl:with-param>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="make-static" select="'yes'"/>
            </xsl:apply-templates>
        </div>
    </xsl:template>

    <!-- Title for treediagram. -->
    <xsl:template
        match="*[contains(@class, ' tree-d/tree ')]/*[contains(@class, ' topic/title ')]"
        mode="treediagram-svgobject:default">
        <div>
            <xsl:attribute name="class">treediagram-title</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

</xsl:stylesheet>
