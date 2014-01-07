<?xml version='1.0'?>

<!--
Copyright © 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:opentopic-i18n="http://www.idiominc.com/opentopic/i18n"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
    exclude-result-prefixes="opentopic-index opentopic opentopic-i18n opentopic-func"
    version="2.0">

    <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
    <xsl:import href="plugin:org.dita.base:xsl/common/dita-textonly.xsl"/>

    <xsl:import href="../common/attr-set-reflection.xsl"/>
    <xsl:import href="../common/vars.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/basic-settings.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/layout-masters-attr.xsl"/>
    <xsl:import href="../../cfg/fo/layout-masters.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/links-attr.xsl"/>
    <xsl:import href="links.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/lists-attr.xsl"/>
    <xsl:import href="lists.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/tables-attr.xsl"/>
    <xsl:import href="tables.xsl"/>
    <xsl:import href="root-processing.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/commons-attr.xsl"/>
    <xsl:import href="commons.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/toc-attr.xsl"/>
    <xsl:import href="toc.xsl"/>
    <xsl:import href="bookmarks.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/index-attr.xsl"/>
    <xsl:import href="index.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/front-matter-attr.xsl"/>
    <xsl:import href="front-matter.xsl"/>
    <xsl:import href="preface.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/map-elements-attr.xsl"/>
    <xsl:import href="map-elements.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/task-elements-attr.xsl"/>
    <xsl:import href="task-elements.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/reference-elements-attr.xsl"/>
    <xsl:import href="reference-elements.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/sw-domain-attr.xsl"/>
    <xsl:import href="sw-domain.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/pr-domain-attr.xsl"/>
    <xsl:import href="pr-domain.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/hi-domain-attr.xsl"/>
    <xsl:import href="hi-domain.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/ui-domain-attr.xsl"/>
    <xsl:import href="ui-domain.xsl"/>
    <xsl:import href="abbrev-domain.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/static-content-attr.xsl"/>
    <xsl:import href="static-content.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/glossary-attr.xsl"/>
    <xsl:import href="glossary.xsl"/>
    <xsl:import href="../../cfg/fo/attrs/lot-lof-attr.xsl"/>
    <xsl:import href="lot-lof.xsl"/>

    <xsl:import href="../../cfg/fo/attrs/learning-elements-attr.xsl"/>
    <xsl:import href="learning-elements.xsl"/>

    <xsl:import href="flagging.xsl"/>

<!--    <xsl:strip-space elements="*"/>-->

    <!-- Parameters in the following group are always passed in from Ant. -->
    <xsl:param name="locale"/>
    <xsl:param name="customizationDir.url"/>
    <xsl:param name="artworkPrefix"/>
    <xsl:param name="publishRequiredCleanup"/>
    <xsl:param name="DRAFT"/>
    <xsl:param name="output.dir.url"/>
    <xsl:param name="work.dir.url"/>
    <xsl:param name="input.dir.url"/>
    <xsl:param name="pdfFormatter" select="'fop'"/>

    <!-- Parameters in the following group are passed in from Ant only to
         change defaults for related XSLT parameters. -->
    <xsl:param name="antArgsBookmarkStyle"/>
    <xsl:param name="antArgsChapterLayout"/>
    <xsl:param name="include.rellinks"/>
    <xsl:param name="antArgsGenerateTaskLabels"/>

    <!-- Remaining parameters are not passed in with the default Ant code. -->
    <xsl:param name="tocMaximumLevel" select="4"/>
    <xsl:param name="ditaVersion" select="number(/*[contains(@class,' map/map ')]/@ditaarch:DITAArchVersion)"/>


    <xsl:output method="xml" encoding="utf-8" indent="no"/>

</xsl:stylesheet>