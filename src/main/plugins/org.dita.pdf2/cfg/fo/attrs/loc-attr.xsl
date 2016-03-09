<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:rx="http://www.renderx.com/XSL/Extensions"
                version="2.0">

    <xsl:attribute-set name="releaseManagementTable">
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.bookRelease.row">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-size">1.1em</xsl:attribute>
        <xsl:attribute name="background-color">#efefef</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.bookRelease.cell">
        <xsl:attribute name="border-style">solid</xsl:attribute>
        <xsl:attribute name="border-width">1pt</xsl:attribute>
        <xsl:attribute name="padding">3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.bookRelease.content">
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.changeItem.row">
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.changeItem.cell">
        <xsl:attribute name="border-style">solid</xsl:attribute>
        <xsl:attribute name="border-width">1pt</xsl:attribute>
        <xsl:attribute name="padding">3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="releaseManagementTable.changeItem.content">
    </xsl:attribute-set>



</xsl:stylesheet>
