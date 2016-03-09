<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="2.0">

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-item ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-person ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-organization ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-revisionid ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-reference ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-revisionid ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-system ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-id ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-started ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-completed ')]">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-summary ')]">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
