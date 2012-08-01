<?xml version="1.0" encoding="UTF-8"?>

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
  xmlns:rx="http://www.renderx.com/XSL/Extensions"
  version="2.0">

  <xsl:attribute-set name="properties" use-attribute-sets="base-font">
    <xsl:attribute name="width">100%</xsl:attribute>
    <xsl:attribute name="space-before">8pt</xsl:attribute>
    <xsl:attribute name="space-after">10pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="properties__body">
  </xsl:attribute-set>

  <xsl:attribute-set name="property">
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry__keycol-content" use-attribute-sets="common.table.body.entry common.table.head.entry">
      <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry__content" use-attribute-sets="common.table.body.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead__row">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry__keycol-content" use-attribute-sets="common.table.body.entry common.table.head.entry">
      <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry__content" use-attribute-sets="common.table.body.entry common.table.head.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="reference">
  </xsl:attribute-set>

  <xsl:attribute-set name="refbody" use-attribute-sets="body">
  </xsl:attribute-set>

  <xsl:attribute-set name="refsyn">
  </xsl:attribute-set>

</xsl:stylesheet>
