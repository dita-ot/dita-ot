<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  >

    <xsl:param name="ENCODING" select="'UTF-8'"/>

    <xsl:template name="insert-content-type">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:choose>
                    <!-- Windows embraces-and-extends these encodings, but still calls them by their old names. -->
                    <xsl:when test="$ENCODING = 'MS932'">
                        <xsl:element name="meta" namespace="{$XHTML-NAMESPACE}">
                          <xsl:attribute name="http-equiv"><xsl:text>Content-Type</xsl:text></xsl:attribute>
                          <xsl:attribute name="content"><xsl:text>text/html; charset=shift_jis</xsl:text></xsl:attribute>
                        </xsl:element>
                    </xsl:when>
                    <xsl:when test="$ENCODING = 'MS936'">
                        <xsl:element name="meta" namespace="{$XHTML-NAMESPACE}">
                          <xsl:attribute name="http-equiv"><xsl:text>Content-Type</xsl:text></xsl:attribute>
                          <xsl:attribute name="content"><xsl:text>text/html; charset=gb2312</xsl:text></xsl:attribute>
                        </xsl:element>
                    </xsl:when>
					<xsl:when test="$ENCODING = 'x-windows-949'">
                        <xsl:element name="meta" namespace="{$XHTML-NAMESPACE}">
                          <xsl:attribute name="http-equiv"><xsl:text>Content-Type</xsl:text></xsl:attribute>
                          <xsl:attribute name="content"><xsl:text>text/html; charset=euc-kr</xsl:text></xsl:attribute>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="meta" namespace="{$XHTML-NAMESPACE}">
                          <xsl:attribute name="http-equiv"><xsl:text>Content-Type</xsl:text></xsl:attribute>
                          <xsl:attribute name="content"><xsl:text>text/html; charset=</xsl:text><xsl:value-of select="$ENCODING"/></xsl:attribute>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="/*[local-name() = 'html']/*[local-name() = 'head']/*[local-name() = 'meta'][@http-equiv = 'Content-Type']" >
        <xsl:call-template name="insert-content-type"/>
    </xsl:template>

    <xsl:template match="/*[local-name() = 'html']/*[local-name() = 'head'][not(*[local-name() = 'meta'][@http-equiv = 'Content-Type'])]">
        <xsl:element name="head" namespace="{$XHTML-NAMESPACE}">
            <xsl:if test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:call-template name="insert-content-type"/>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:result-document method="xhtml" encoding="{$ENCODING}" include-content-type="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" omit-xml-declaration="yes">
                    <xsl:next-match/>
                </xsl:result-document>           
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
