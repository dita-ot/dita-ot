<?xml version='1.0'?>

<!-- 
Copyright © 2004-2005 by Idiom Technologies, Inc. All rights reserved. 
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

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes"
    doctype-public="-//IBM//DTD DITA Composite//EN" doctype-system="ditabase.dtd"/>

    <xsl:param name="previewType"/>

    <xsl:template name="processTopic">

        <xsl:variable name="count" select="count(self::node()//prolog/adobemetadata/outputs)"/>   
        <xsl:variable name="type" select="self::node()//prolog/adobemetadata/outputs/output/@type"/>

        <xsl:if test="$count = 0 or $type = $previewType">
            <xsl:element name="{local-name()}">
                <xsl:copy-of select="@*"/>
                <xsl:apply-templates mode="typeMatched"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/task ') or contains(@class, ' concept/concept ') or contains(@class, ' reference/reference ')]">
        <xsl:call-template name="processTopic"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/task ') or contains(@class, ' concept/concept ') or contains(@class, ' reference/reference ')]" mode="typeMatched">
        <xsl:call-template name="processTopic"/>
    </xsl:template>

    <xsl:template match="comment()|processing-instruction()" mode="typeMatched">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="*" mode="typeMatched">
  <xsl:element name="{local-name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="typeMatched"/>
  </xsl:element>
    </xsl:template>

    <xsl:template match="*">
  <xsl:element name="{local-name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="typeMatched"/>
  </xsl:element>
    </xsl:template>

</xsl:stylesheet>
