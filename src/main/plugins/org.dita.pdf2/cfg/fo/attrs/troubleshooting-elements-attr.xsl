<?xml version='1.0'?>

<!-- 20170503 SCH: Add support for troubleshooting elements. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="2.0">

    <xsl:attribute-set name="troubleshooting">
    </xsl:attribute-set>

    <xsl:attribute-set name="troublebody" use-attribute-sets="body">
    </xsl:attribute-set>
    
    <xsl:attribute-set name="troubleSolution">
        <xsl:attribute name="leader-pattern">rule</xsl:attribute>
        <xsl:attribute name="leader-length">25%</xsl:attribute>
        <xsl:attribute name="rule-thickness">0.5pt</xsl:attribute>
        <xsl:attribute name="rule-style">solid</xsl:attribute>
        <xsl:attribute name="color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="condition" use-attribute-sets="section">
    </xsl:attribute-set>
    <xsl:attribute-set name="condition__content" use-attribute-sets="section__content">
    </xsl:attribute-set>

    <xsl:attribute-set name="cause" use-attribute-sets="section">
    </xsl:attribute-set>
    <xsl:attribute-set name="cause__content" use-attribute-sets="section__content">
    </xsl:attribute-set>
    
    <xsl:attribute-set name="remedy" use-attribute-sets="section">
    </xsl:attribute-set>
    <xsl:attribute-set name="remedy__content" use-attribute-sets="section__content">
    </xsl:attribute-set>
    
    <xsl:attribute-set name="responsibleParty" use-attribute-sets="section">
    </xsl:attribute-set>
    <xsl:attribute-set name="responsibleParty__content" use-attribute-sets="section__content">
    </xsl:attribute-set>

    <xsl:attribute-set name="tasktroubleshooting" use-attribute-sets="section">
    </xsl:attribute-set>
    <xsl:attribute-set name="tasktroubleshooting__content" use-attribute-sets="section__content">
    </xsl:attribute-set>

    <xsl:attribute-set name="task.example" use-attribute-sets="example">
    </xsl:attribute-set>
    <xsl:attribute-set name="task.example__content" use-attribute-sets="example__content">
    </xsl:attribute-set>

    <xsl:attribute-set name="steptroubleshooting">
    </xsl:attribute-set>

</xsl:stylesheet>