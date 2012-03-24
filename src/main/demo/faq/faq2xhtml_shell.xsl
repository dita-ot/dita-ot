<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  
 | Specific override stylesheet for faq (demo)
 | This demonstrates the XSLT override mechanism tied to a specialization.
 |
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="faq2html_shellImpl.xsl"/>

<xsl:output
    method="xml"
    encoding="utf-8"
    indent="no"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
/>

<xsl:param name="OUTEXT" select="'xml'"/>

</xsl:stylesheet>
