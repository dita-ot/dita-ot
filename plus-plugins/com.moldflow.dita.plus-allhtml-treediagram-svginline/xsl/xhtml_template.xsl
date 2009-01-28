<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:svginline="http://www.moldflow.com/namespace/2008/allhtml-treediagram-svginline"
    xmlns:treediagram2svg="http://www.moldflow.com/namespace/2008/treediagram2svg"
    exclude-result-prefixes="svginline treediagram2svg">

    <dita:extension id="treediagram2svg.pull.xsl" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

    <xsl:param name="plus-treediagram-format" select="'svginline'"/>
    <xsl:param name="plus-allhtml-treediagram-svginline-csspath" select="''"/>
    <xsl:param name="plus-allhtml-treediagram-svginline-jspath" select="''"/>

    <!-- Override for HTML generation. -->
    <xsl:template match="/ | node()" mode="gen-user-scripts">
        <xsl:if test="$plus-treediagram-format = 'svginline' and //*[contains(@class, ' tree-d/tree ')]">
            <xsl:call-template name="svginline:gen-user-scripts"/>
        </xsl:if>
        <xsl:next-match>
            <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>svginline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
            </xsl:fallback>
        </xsl:next-match>
    </xsl:template>

    <xsl:template name="svginline:gen-user-scripts">
      <xsl:call-template name="treediagram2svg:gen-user-scripts">
        <xsl:with-param name="JSPATH" select="concat($PATH2PROJ, $plus-allhtml-treediagram-svginline-jspath)"/>
      </xsl:call-template>
    </xsl:template>

    <!-- Override for HTML generation. -->
    <xsl:template match="/ | node()" mode="gen-user-styles">
        <xsl:if test="$plus-treediagram-format = 'svginline' and //*[contains(@class, ' tree-d/tree ')]">
            <xsl:call-template name="svginline:gen-user-styles"/>
        </xsl:if>
        <xsl:next-match>
            <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>svginline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
            </xsl:fallback>
        </xsl:next-match>
    </xsl:template>

    <!-- Output a link to the CSS needed for tree diagrams. -->
    <xsl:template name="svginline:gen-user-styles">
        <xsl:variable name="urltest">
            <!-- test for URL -->
            <xsl:call-template name="url-string">
                <xsl:with-param name="urltext" select="$plus-allhtml-treediagram-svginline-csspath"/>
            </xsl:call-template>
        </xsl:variable>

        <link>
            <xsl:attribute name="rel">stylesheet</xsl:attribute>
            <xsl:attribute name="type">text/css</xsl:attribute>
            <xsl:attribute name="href">
                <xsl:if test="not($urltest='url')">
                    <xsl:value-of select="$PATH2PROJ"/>
                </xsl:if>
                <xsl:value-of select="$plus-allhtml-treediagram-svginline-csspath"/>
                <xsl:value-of select="$treediagram2svg:css-filename"/>
            </xsl:attribute>
        </link>
    </xsl:template>

    <!-- Entry point. -->
    <xsl:template match="*[contains(@class, ' tree-d/tree ')]">
        <xsl:choose>
          <xsl:when test="$plus-treediagram-format = 'svginline'">
            <xsl:apply-templates select="." mode="svginline:default"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>svginline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Top-level tree diagram elements. -->
    <xsl:template match="*[contains(@class, ' tree-d/tree ')]"
        mode="svginline:default">
        <div>
            <xsl:attribute name="class">treediagram</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <!-- Apply title of tree diagram. -->
            <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="svginline:default"/>
            <!-- Apply body of tree diagram. -->
            <xsl:call-template name="treediagram2svg:create-svg-element"/>
        </div>
    </xsl:template>

    <!-- Title for treediagram. -->
    <xsl:template
        match="*[contains(@class, ' tree-d/tree ')]/*[contains(@class, ' topic/title ')]"
        mode="svginline:default">
        <div>
            <xsl:attribute name="class">treediagram-title</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

</xsl:stylesheet>
