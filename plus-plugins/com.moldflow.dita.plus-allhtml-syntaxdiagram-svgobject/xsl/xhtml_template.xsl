<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:syntaxdiagram-svgobject="http://www.moldflow.com/namespace/2008/plus-allhtml-syntaxdiagram-svgobject"
    xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
    xmlns:syntaxdiagram2svg="http://www.moldflow.com/namespace/2008/syntaxdiagram2svg"
    exclude-result-prefixes="syntaxdiagram-svgobject syntaxdiagram2svg">

    <dita:extension id="syntaxdiagram2svg.pull.xsl" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

    <xsl:param name="plus-syntaxdiagram-format" select="'svgobject'"/>
    <xsl:param name="plus-allhtml-syntaxdiagram-svgobject-csspath" select="''"/>
    <xsl:param name="plus-allhtml-syntaxdiagram-svgobject-jspath" select="''"/>

    <xsl:param name="CURRENTDIR"/>
    <xsl:param name="CURRENTFILE"/>

    <!-- Entry point. -->
    <xsl:template match="*[contains(@class, ' pr-d/syntaxdiagram ')]">
        <xsl:choose>
          <xsl:when test="$plus-syntaxdiagram-format = 'svgobject'">
            <xsl:apply-templates select="." mode="syntaxdiagram-svgobject:default"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>syntaxdiagram-svgobject: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Top-level syntax diagram elements. -->
    <xsl:template match="*[contains(@class, ' pr-d/syntaxdiagram ')]"
        mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">syntaxdiagram</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:call-template name="syntaxdiagram-svgobject:process-children"/>
        </div>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' pr-d/synblk ')]" mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">synblk</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:call-template name="syntaxdiagram-svgobject:process-children"/>
        </div>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' pr-d/fragment ')]" mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">fragment</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:call-template name="syntaxdiagram-svgobject:process-children"/>
        </div>
    </xsl:template>

    <!-- Break the syntax diagram into SVG- and HTML-bits. -->
    <xsl:template name="syntaxdiagram-svgobject:process-children">
        <xsl:for-each select="*">
            <xsl:choose>
                <xsl:when
                    test="contains(@class, ' topic/title ')
                    or contains(@class, ' pr-d/syntaxdiagram ')
                    or contains(@class, ' pr-d/synblk ')
                    or contains(@class, ' pr-d/fragment ')">
                    <!-- syntaxdiagram, synblk, fragment all live in HTML land. -->
                    <xsl:apply-templates select="." mode="syntaxdiagram-svgobject:default"/>
                </xsl:when>
                <xsl:when
                    test="count(preceding-sibling::*) = 0 or
                    preceding-sibling::*[1][
                    contains(@class, ' topic/title ')
                    or contains(@class, ' pr-d/syntaxdiagram ')
                    or contains(@class, ' pr-d/synblk ')
                    or contains(@class, ' pr-d/fragment ')]">
                    <!-- Other elements start a syntax diagram. -->
                    <div>
                        <xsl:attribute name="class">syntaxdiagram-piece</xsl:attribute>

                        <xsl:apply-templates select="." mode="svgobject:generate-reference">
                          <xsl:with-param name="content">
                            <xsl:call-template name="syntaxdiagram2svg:create-svg-document">
                               <xsl:with-param name="CSSPATH">
                                   <xsl:call-template name="svgobject:svgobject-reverse-path"/>
                                   <xsl:value-of select="$plus-allhtml-syntaxdiagram-svgobject-csspath"/>
                               </xsl:with-param>
                               <xsl:with-param name="JSPATH">
                                   <xsl:call-template name="svgobject:svgobject-reverse-path"/>
                                   <xsl:value-of select="$plus-allhtml-syntaxdiagram-svgobject-jspath"/>
                               </xsl:with-param>
                               <xsl:with-param name="BASEPATH">
                                   <xsl:call-template name="svgobject:svgobject-reverse-path"/>
                                   <xsl:value-of select="escape-html-uri($CURRENTDIR)"/>
                                   <xsl:text>/</xsl:text>
                                   <xsl:value-of select="replace(escape-html-uri($CURRENTFILE), '\.(xml|dita)$', $OUTEXT, 'i')"/>
                               </xsl:with-param>
                            </xsl:call-template>
                          </xsl:with-param>
                          <xsl:with-param name="make-static" select="'yes'"/>
                        </xsl:apply-templates>
                    </div>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- Title for syntaxdiagram. -->
    <xsl:template
        match="*[contains(@class, ' pr-d/syntaxdiagram ')]/*[contains(@class, ' topic/title ')]"
        mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">syntaxdiagram-title</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- Title for synblk. -->
    <xsl:template match="*[contains(@class, ' pr-d/synblk ')]/*[contains(@class, ' topic/title ')]"
        mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">synblk-title</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- Title for fragment. -->
    <xsl:template
        match="*[contains(@class, ' pr-d/fragment ')]/*[contains(@class, ' topic/title ')]"
        mode="syntaxdiagram-svgobject:default">
        <div>
            <xsl:attribute name="class">fragment-title</xsl:attribute>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setidaname"/>
            <xsl:call-template name="flagcheck"/>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- Override fragref processing: XHTML contents as hyperlink. -->
    <xsl:template match="*[contains(@class, ' pr-d/fragref ')]" mode="syntaxdiagram2svg:body-only">
        <xsl:param name="role" select="'forward'"/>
        <svg:a syntaxdiagram2svg:dispatch="boxed">
            <xsl:attribute name="class">
                <xsl:text>boxed </xsl:text>
                <xsl:value-of select="local-name()"/>
            </xsl:attribute>
            <xsl:attribute name="syntaxdiagram2svg:element">
                <xsl:value-of select="local-name()"/>
            </xsl:attribute>
            <xsl:attribute name="syntaxdiagram2svg:role">
                <xsl:value-of select="$role"/>
            </xsl:attribute>
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="href"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <xsl:call-template name="syntaxdiagram2svg:box-contents"/>
        </svg:a>
    </xsl:template>

    <!-- Override fragref processing: XHTML contents as hyperlink. -->
    <xsl:template match="*[contains(@class, ' pr-d/fragref ')]"
        mode="syntaxdiagram2svg:groupcomp-child">
        <xsl:param name="role" select="'forward'"/>
        <svg:a syntaxdiagram2svg:dispatch="unboxed" syntaxdiagram2svg:role="forward">
            <xsl:attribute name="class">
                <xsl:text>unboxed </xsl:text>
                <xsl:value-of select="local-name()"/>
            </xsl:attribute>
            <xsl:attribute name="syntaxdiagram2svg:element">
                <xsl:value-of select="local-name()"/>
            </xsl:attribute>
            <xsl:attribute name="syntaxdiagram2svg:role">
                <xsl:value-of select="$role"/>
            </xsl:attribute>
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="href"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <xsl:call-template name="syntaxdiagram2svg:box-contents"/>
        </svg:a>
    </xsl:template>

    <!-- Override synnote processing: XHTML contents as hyperlink. -->
    <xsl:template match="*[contains(@class, ' pr-d/synnote ')][not(@id)]"
        mode="syntaxdiagram2svg:note">
        <svg:a syntaxdiagram2svg:dispatch="note" class="note">
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="syntaxdiagram-svgobject:get-footnote-target"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <svg:text>
                <xsl:call-template name="syntaxdiagram2svg:get-callout"/>
            </svg:text>
        </svg:a>
    </xsl:template>

    <!-- Override synnote processing: XHTML contents as hyperlink. -->
    <xsl:template match="*[contains(@class, ' pr-d/synnote ')][not(@id)]"
        mode="syntaxdiagram2svg:groupcomp-child">
        <svg:a syntaxdiagram2svg:dispatch="note" class="note">
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="syntaxdiagram-svgobject:get-footnote-target"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <svg:text>
                <xsl:call-template name="syntaxdiagram2svg:get-callout"/>
            </svg:text>
        </svg:a>
    </xsl:template>

    <xsl:template name="syntaxdiagram-svgobject:get-footnote-target">
        <xsl:text>#fntarg_</xsl:text>
        <xsl:number format="1" count="*[contains(@class, ' topic/fn ')]" from="/*" level="any"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' pr-d/synnoteref ')][@href and @href != '']"
        mode="syntaxdiagram2svg:note">
        <svg:a syntaxdiagram2svg:dispatch="note" class="note">
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="syntaxdiagram-svgobject:get-footnote-reference-target"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <svg:text>
                <xsl:call-template name="syntaxdiagram2svg:get-callout-reference"/>
            </svg:text>
        </svg:a>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' pr-d/synnoteref ')][@href and @href != '']"
        mode="syntaxdiagram2svg:groupcomp-child">
        <svg:a syntaxdiagram2svg:dispatch="note" class="note">
            <xsl:attribute name="xlink:href">
                <xsl:call-template name="syntaxdiagram-svgobject:get-footnote-reference-target"/>
            </xsl:attribute>
<!--
            <xsl:if test="$plus-syntaxdiagram-format = 'svgobject'">
                <xsl:attribute name="target" select="'_parent'"/>
            </xsl:if>
-->
            <svg:text>
                <xsl:call-template name="syntaxdiagram2svg:get-callout-reference"/>
            </svg:text>
        </svg:a>
    </xsl:template>

    <xsl:template name="syntaxdiagram-svgobject:get-footnote-reference-target">
        <!-- To do?: hyperlink to a footnote in a different file. -->
        <xsl:choose>
            <xsl:when test="contains(@href, '#')">
                <xsl:variable name="document" select="substring-before(@href, '#')"/>
                <xsl:choose>
                    <xsl:when test="contains(substring-after(@href, '#'), '/')">
                        <xsl:variable name="topicid"
                            select="substring-before(substring-after(@href, '#'), '/')"/>
                        <xsl:variable name="targetid"
                            select="substring-after(substring-after(@href, '#'), '/')"/>
                        <xsl:value-of select="concat('#', $topicid, '__', $targetid)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>synnoteref points at entire topic.</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>synnoteref href points at entire file.</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
