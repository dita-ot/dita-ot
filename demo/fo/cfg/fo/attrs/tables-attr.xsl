<?xml version="1.0"?>

<!-- 
Copyright © 2004-2006 by Idiom Technologies, Inc. All rights reserved. 
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other 
trademarks are the property of their respective owners. 

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE &quot;AS IS,&quot; WITH 
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
Software or its derivatives. In no event shall Idiom Technologies, Inc.&apos;s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net. 
See the accompanying license.txt file for applicable licenses.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

    <xsl:attribute-set name="table.title">
        <xsl:attribute name="font-family">Sans</xsl:attribute>
        <xsl:attribute name="font-size"><xsl:value-of select="$default-font-size"/></xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-before.optimum">10pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">10pt</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__tableframe__none"/>

    <xsl:attribute-set name="__tableframe__top">
        <xsl:attribute name="border-top-style">solid</xsl:attribute>
        <xsl:attribute name="border-top-width">1pt</xsl:attribute>
        <xsl:attribute name="border-top-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__tableframe__bottom">
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-width">1pt</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
        <xsl:attribute name="border-after-width.conditionality">retain</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="thead__tableframe__bottom">
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-width">2pt</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__tableframe__left">
        <xsl:attribute name="border-left-style">solid</xsl:attribute>
        <xsl:attribute name="border-left-width">1pt</xsl:attribute>
        <xsl:attribute name="border-left-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__tableframe__right">
        <xsl:attribute name="border-right-style">solid</xsl:attribute>
        <xsl:attribute name="border-right-width">1pt</xsl:attribute>
        <xsl:attribute name="border-right-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table">
        <!--It is a table container -->
        <xsl:attribute name="font-size"><xsl:value-of select="$default-font-size"/></xsl:attribute>
        <xsl:attribute name="space-after.optimum">10pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table.tgroup">
        <!--It is a table-->
        <xsl:attribute name="table-layout">fixed</xsl:attribute>
        <xsl:attribute name="width">100%</xsl:attribute>
        <!--xsl:attribute name=&quot;inline-progression-dimension&quot;&gt;auto&lt;/xsl:attribute-->
<!--        &lt;xsl:attribute name=&quot;background-color&quot;&gt;white&lt;/xsl:attribute&gt;-->
        <xsl:attribute name="space-before.optimum">5pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">5pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table__tableframe__all">
        <xsl:attribute name="border-top-style">solid</xsl:attribute>
        <xsl:attribute name="border-top-width">1pt</xsl:attribute>
        <xsl:attribute name="border-top-color">black</xsl:attribute>
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-width">1pt</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
        <xsl:attribute name="border-left-style">solid</xsl:attribute>
        <xsl:attribute name="border-left-width">1pt</xsl:attribute>
        <xsl:attribute name="border-left-color">black</xsl:attribute>
        <xsl:attribute name="border-right-style">solid</xsl:attribute>
        <xsl:attribute name="border-right-width">1pt</xsl:attribute>
        <xsl:attribute name="border-right-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table__tableframe__topbot">
        <xsl:attribute name="border-top-style">solid</xsl:attribute>
        <xsl:attribute name="border-top-width">1pt</xsl:attribute>
        <xsl:attribute name="border-top-color">black</xsl:attribute>
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-width">1pt</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table__tableframe__top">
        <xsl:attribute name="border-top-style">solid</xsl:attribute>
        <xsl:attribute name="border-top-width">1pt</xsl:attribute>
        <xsl:attribute name="border-top-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table__tableframe__bottom">
        <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
        <xsl:attribute name="border-bottom-width">1pt</xsl:attribute>
        <xsl:attribute name="border-bottom-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="table__tableframe__sides">
        <xsl:attribute name="border-left-style">solid</xsl:attribute>
        <xsl:attribute name="border-left-width">1pt</xsl:attribute>
        <xsl:attribute name="border-left-color">black</xsl:attribute>
        <xsl:attribute name="border-right-style">solid</xsl:attribute>
        <xsl:attribute name="border-right-width">1pt</xsl:attribute>
        <xsl:attribute name="border-right-color">black</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="tgroup.tbody">
        <!--Table body-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tgroup.thead">
        <!--Table head-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tgroup.tfoot">
        <!--Table footer-->
    </xsl:attribute-set>

    <xsl:attribute-set name="thead.row">
        <!--Head row-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tfoot.row">
        <!--Table footer-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tbody.row">
        <!--Table body row-->
    </xsl:attribute-set>

    <xsl:attribute-set name="thead.row.entry">
        <!--head cell-->
        <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="thead.row.entry__content">
        <!--head cell contents-->
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="tfoot.row.entry">
        <!--footer cell-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tfoot.row.entry__content">
        <!--footer cell contents-->
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="tbody.row.entry">
        <!--body cell-->
    </xsl:attribute-set>

    <xsl:attribute-set name="tbody.row.entry__content">
        <!--body cell contents-->
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dl">
        <!--DL is a table-->
        <xsl:attribute name="width">100%</xsl:attribute>
        <xsl:attribute name="space-before.optimum">5pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">5pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dl__body">
    </xsl:attribute-set>

    <xsl:attribute-set name="dl.dlhead">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlentry">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlentry.dt">
        <xsl:attribute name="relative-align">baseline</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dlentry.dt__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dlentry.dd">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlentry.dd__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dl.dlhead__row">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlhead.dthd__cell">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlhead.dthd__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="dlhead.ddhd__cell">
    </xsl:attribute-set>

    <xsl:attribute-set name="dlhead.ddhd__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

	<xsl:attribute-set name="simpletable">
		<!--It is a table container -->
        <xsl:attribute name="width">100%</xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$default-font-size"/></xsl:attribute>
		<xsl:attribute name="space-before.optimum">8pt</xsl:attribute>
		<xsl:attribute name="space-after.optimum">10pt</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="simpletable__body">
	</xsl:attribute-set>

	<xsl:attribute-set name="sthead">
	</xsl:attribute-set>

	<xsl:attribute-set name="sthead__row">
	</xsl:attribute-set>

	<xsl:attribute-set name="strow">
	</xsl:attribute-set>

	<xsl:attribute-set name="sthead.stentry">
	</xsl:attribute-set>

	<xsl:attribute-set name="sthead.stentry__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="sthead.stentry__keycol-content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="background-color">antiquewhite</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="strow.stentry__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="strow.stentry__keycol-content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="background-color">antiquewhite</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="strow.stentry">
	</xsl:attribute-set>

    <xsl:attribute-set name="properties">
        <!--It is a table container -->
        <xsl:attribute name="font-size"><xsl:value-of select="$default-font-size"/></xsl:attribute>
        <xsl:attribute name="width">100%</xsl:attribute>
        <xsl:attribute name="space-before.optimum">8pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">10pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="properties__body">
    </xsl:attribute-set>

    <xsl:attribute-set name="property">
    </xsl:attribute-set>

    <xsl:attribute-set name="property.entry">
    </xsl:attribute-set>

    <xsl:attribute-set name="property.entry__keycol-content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="property.entry__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="prophead">
    </xsl:attribute-set>

    <xsl:attribute-set name="prophead__row">
    </xsl:attribute-set>

    <xsl:attribute-set name="prophead.entry">
    </xsl:attribute-set>

    <xsl:attribute-set name="prophead.entry__keycol-content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="prophead.entry__content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

	<xsl:attribute-set name="choicetable">
		<!--It is a table container -->
        <xsl:attribute name="width">100%</xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$default-font-size"/></xsl:attribute>
		<xsl:attribute name="space-after.optimum">10pt</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="choicetable__body">
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead">
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead__row">
	</xsl:attribute-set>

	<xsl:attribute-set name="chrow">
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead.choptionhd">
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead.choptionhd__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead.chdeschd">
	</xsl:attribute-set>

	<xsl:attribute-set name="chhead.chdeschd__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="chrow.choption">
	</xsl:attribute-set>

    <xsl:attribute-set name="chrow.choption__keycol-content">
        <xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

	<xsl:attribute-set name="chrow.choption__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="chrow.chdesc">
	</xsl:attribute-set>

	<xsl:attribute-set name="chrow.chdesc__keycol-content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="chrow.chdesc__content">
		<xsl:attribute name="margin">3pt 3pt 3pt 3pt</xsl:attribute>
	</xsl:attribute-set>


	<xsl:attribute-set name="reltable">

	</xsl:attribute-set>

	<xsl:attribute-set name="reltable__title">

	</xsl:attribute-set>

	<xsl:attribute-set name="relheader">

	</xsl:attribute-set>

	<xsl:attribute-set name="relcolspec">

	</xsl:attribute-set>

	<xsl:attribute-set name="relcell">

	</xsl:attribute-set>

	<xsl:attribute-set name="relrow">

	</xsl:attribute-set>


</xsl:stylesheet>