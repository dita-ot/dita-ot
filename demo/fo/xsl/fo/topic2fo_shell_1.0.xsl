<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:opentopic-i18n="http://www.idiominc.com/opentopic/i18n" xmlns:opentopic-index="http://www.idiominc.com/opentopic/index" xmlns:opentopic="http://www.idiominc.com/opentopic" xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function" xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" exclude-result-prefixes="opentopic-index opentopic opentopic-i18n opentopic-func" version="1.1">

    <xsl:import href="../common/attr-set-reflection.xsl"></xsl:import>
    <xsl:import href="../common/vars.xsl"></xsl:import>

    <xsl:import href="../../cfg/fo/attrs/basic-settings.xsl"></xsl:import>
    <xsl:import href="../../cfg/fo/layout-masters.xsl"></xsl:import>
    <xsl:import href="links.xsl"></xsl:import>
    <xsl:import href="lists.xsl"></xsl:import>
    <xsl:import href="tables.xsl"></xsl:import>
    <xsl:import href="tables_1.0.xsl"></xsl:import>
    <xsl:import href="root-processing.xsl"></xsl:import>
    <xsl:import href="commons.xsl"></xsl:import>
    <xsl:import href="commons_1.0.xsl"></xsl:import>
    <xsl:import href="toc.xsl"></xsl:import>
    <xsl:import href="toc_1.0.xsl"></xsl:import>
    <xsl:import href="bookmarks.xsl"></xsl:import>
    <xsl:import href="bookmarks_1.0.xsl"></xsl:import>
    <xsl:import href="index.xsl"></xsl:import>
    <xsl:import href="index_1.0.xsl"></xsl:import>
    <xsl:import href="front-matter.xsl"></xsl:import>
    <xsl:import href="front-matter_1.0.xsl"></xsl:import>
    <xsl:import href="preface.xsl"></xsl:import>

    <xsl:import href="task-elements.xsl"></xsl:import>

    <xsl:import href="sw-domain.xsl"></xsl:import>
    <xsl:import href="pr-domain.xsl"></xsl:import>
    <xsl:import href="hi-domain.xsl"></xsl:import>
    <xsl:import href="ui-domain.xsl"></xsl:import>

    <xsl:import href="static-content.xsl"></xsl:import>

    <xsl:import href="cfg:fo/attrs/custom.xsl"></xsl:import>
    <xsl:import href="cfg:fo/xsl/custom.xsl"></xsl:import>



    
    <xsl:param name="locale"></xsl:param>
    <xsl:param name="customizationDir"></xsl:param>
    <xsl:param name="artworkPrefix"></xsl:param>
    <xsl:param name="fileProtocolPrefix"></xsl:param>
    <xsl:param name="publishRequiredCleanup"></xsl:param>
    <xsl:param name="DRAFT"></xsl:param>
    <xsl:param name="disableRelatedLinks" select="'yes'"></xsl:param>
    <xsl:param name="pdfFormatter" select="'fop'"></xsl:param>

    
    <xsl:param name="antArgsBookmarkStyle"></xsl:param>
    <xsl:param name="antArgsChapterLayout"></xsl:param>
    <xsl:param name="antArgsIncludeRelatedLinks"></xsl:param>
    <xsl:param name="antArgsGenerateTaskLabels"></xsl:param>

    
    <xsl:param name="tocMaximumLevel" select="'4'"></xsl:param>
    <xsl:param name="ditaVersion" select="/*[contains(@class,' map/map ')]/@ditaarch:DITAArchVersion"></xsl:param>


    <xsl:output method="xml" encoding="utf-8" indent="no"></xsl:output>

    <xsl:template match="/">
        
        <xsl:call-template name="rootTemplate"></xsl:call-template>
    </xsl:template>

</xsl:stylesheet>