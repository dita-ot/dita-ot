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
        <xsl:text>change-person: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-organization ')]">
        <xsl:text>change-organization: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-revisionid ')]">
        <xsl:text>change-revisionid: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-reference ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-revisionid ')]">
        <xsl:text>change-revisionid: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-system ')]">
        <xsl:text>change-request-system: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-request-id ')]">
        <xsl:text>change-request-id: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-started ')]">
        <xsl:text>change-started: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-completed ')]">
        <xsl:text>change-completed: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' relmgmt-d/change-summary ')]">
        <xsl:text>change-summary: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
