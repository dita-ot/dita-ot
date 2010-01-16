<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:param name="ENCODING" select="'UTF-8'"/>

    <xsl:template name="insert-content-type">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:choose>
                    <!-- Windows embraces-and-extends these encodings, but still calls them by their old names. -->
                    <xsl:when test="$ENCODING = 'MS932'">
                        <meta http-equiv="Content-Type" content="text/html; charset=shift_jis"/>
                    </xsl:when>
                    <xsl:when test="$ENCODING = 'MS936'">
                        <meta http-equiv="Content-Type" content="text/html; charset=gb2312"/>
                    </xsl:when>
					<xsl:when test="$ENCODING = 'x-windows-949'">
                        <meta http-equiv="Content-Type" content="text/html; charset=euc-kr"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <meta http-equiv="Content-Type" content="text/html; charset={$ENCODING}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="/*[local-name() = 'html']/*[local-name() = 'head']/*[local-name() = 'meta'][@http-equiv = 'Content-Type']" >
        <xsl:call-template name="insert-content-type"/>
    </xsl:template>

    <xsl:template match="/*[local-name() = 'html']/*[local-name() = 'head'][not(*[local-name() = 'meta'][@http-equiv = 'Content-Type'])]">
        <head>
            <xsl:if test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:call-template name="insert-content-type"/>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </head>
    </xsl:template>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:result-document method="html" encoding="{$ENCODING}" include-content-type="no" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd">
                    <xsl:next-match/>
                </xsl:result-document>           
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
