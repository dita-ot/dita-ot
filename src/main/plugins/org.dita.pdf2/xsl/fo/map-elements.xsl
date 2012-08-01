<?xml version='1.0' encoding="UTF-8"?>

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
  version="2.0">

  <xsl:template match="*[contains(@class,' map/topicmeta ')]/*[contains(@class,' map/searchtitle ')]"/>

  <xsl:template match="*[contains(@class, ' topic/topicmeta ')]">
    <!--
    <fo:block xsl:use-attribute-sets="topicmeta">
      <xsl:apply-templates/>
    </fo:block>
    -->
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/map ')]/*[contains(@class, ' map/reltable ')]">
    <fo:table-and-caption>
      <fo:table-caption>
        <fo:block xsl:use-attribute-sets="reltable__title">
          <xsl:value-of select="@title"/>
        </fo:block>
      </fo:table-caption>

      <fo:table xsl:use-attribute-sets="reltable">
        <xsl:call-template name="topicrefAttsNoToc"/>
        <xsl:call-template name="selectAtts"/>
        <xsl:call-template name="globalAtts"/>
        <xsl:apply-templates select="relheader"/>
        <fo:table-body>
          <xsl:apply-templates select="relrow"/>
        </fo:table-body>
      </fo:table>
    </fo:table-and-caption>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/relheader ')]">
    <fo:table-header xsl:use-attribute-sets="relheader">
      <xsl:call-template name="globalAtts"/>
      <xsl:apply-templates/>
    </fo:table-header>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/relcolspec ')]">
    <fo:table-cell xsl:use-attribute-sets="relcolspec">
      <xsl:apply-templates/>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/relrow ')]">
    <fo:table-row xsl:use-attribute-sets="relrow">
      <xsl:call-template name="globalAtts"/>
      <xsl:apply-templates/>
    </fo:table-row>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/relcell ')]">
    <fo:table-cell xsl:use-attribute-sets="relcell">
      <xsl:call-template name="globalAtts"/>
      <xsl:call-template name="topicrefAtts"/>
      <xsl:apply-templates/>
    </fo:table-cell>
  </xsl:template>

</xsl:stylesheet>