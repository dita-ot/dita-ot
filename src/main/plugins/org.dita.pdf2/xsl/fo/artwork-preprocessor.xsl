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
<!-- Deprecated: left for backward compatibility, and it will be removed in a 
     later release -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="2.0">

    <xsl:param name="artworkPrefix"/>

	<xsl:param name="image-infos-file"/>

	<xsl:variable name="image-infos" select="document($image-infos-file)/ImageInfos/ImageInfo"/>

    <xsl:template match="*|@*|comment()|processing-instruction()|text()" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/image ')]">
		<xsl:copy>
            <xsl:choose>
                <xsl:when test="$image-infos">
                    <xsl:apply-templates select="@*[not(name() = 'width' or name()='height')]"/>

                    <xsl:variable name="newWidthAndHeight">
                        <xsl:call-template name="calculateNewImageDimentions">
                            <xsl:with-param name="theImageInfos" select="$image-infos"/>
                            <xsl:with-param name="theWidth" select="@height"/>
                            <xsl:with-param name="theHeight" select="@width"/>
                            <xsl:with-param name="theFilename" select="@href"/>
                        </xsl:call-template>
                    </xsl:variable>

                    <xsl:attribute name="height">
                        <xsl:value-of select="substring-after($newWidthAndHeight,',')"/>
                    </xsl:attribute>
                    <xsl:attribute name="width">
                        <xsl:value-of select="substring-before($newWidthAndHeight,',')"/>
                    </xsl:attribute>

                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="@*"/>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
		</xsl:copy>
    </xsl:template>
 
    <xsl:template match="*[contains(@class, ' topic/image ')]/@href">
        <xsl:variable name="newHrefValue" select="concat($artworkPrefix, '/', .)"/>
        <xsl:attribute name="href">
            <xsl:value-of select="$newHrefValue"/>
        </xsl:attribute>
    </xsl:template>

	<!--Image dimentions calculation template-->
	<xsl:template name="calculateNewImageDimentions">
		<xsl:param name="theImageInfos"/>
		<xsl:param name="theWidth"/>
		<xsl:param name="theHeight"/>
		<!-- These two params used for new image size calculation with data from external file -->
		<xsl:param name="theKoef"/>
		<xsl:param name="theFilename"/>

		<xsl:variable name="result">
			<xsl:variable name="image-info" select="$theImageInfos[ @Name = $theFilename ]"/>
			<!--It's better check our 'six' before start-->
			<xsl:choose>
				<xsl:when test="not($image-info)">
					<xsl:message>
						Image info not found for file '<xsl:value-of select="$theFilename"/>'
					</xsl:message>
					1in,1in <!-- Set default value "1in x 1in" by Eric's request -->
				</xsl:when>
				<xsl:otherwise>
					<!--It's OK. Ready to go-->
					<xsl:call-template name="calculateWidthAndHeightWithKoef">
						<xsl:with-param name="theImageInfo" select="$image-info"/>
						<xsl:with-param name="theKoef">
							<xsl:choose>
								<xsl:when test="number($theKoef)">
									<xsl:value-of select="$theKoef"/>
								</xsl:when>
								<xsl:when test="number($image-info/@Width-Pixels) &gt; 680">200</xsl:when>
								<xsl:otherwise>152</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
        <xsl:value-of select="$result"/>
	</xsl:template>

	<xsl:template name="calculateWidthAndHeightWithKoef">
		<xsl:param name="theImageInfo"/>
		<xsl:param name="theKoef"/>

		<xsl:variable name="new-width" select="number($theImageInfo/@Width-Pixels) div number($theKoef)"/>
		<xsl:variable name="new-height" select="number($theImageInfo/@Height-Pixels) div number($theKoef)"/>

		<xsl:value-of select="concat($new-width, 'in,', $new-height, 'in')"/>
	</xsl:template>

</xsl:stylesheet>