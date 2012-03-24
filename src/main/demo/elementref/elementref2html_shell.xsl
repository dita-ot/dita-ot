<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  
 | Specific override stylesheet for elementref (demo)
 | This demonstrates the XSLT override mechanism tied to a specialization.
 |
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="elementref2html_shellImpl.xsl"/>

<!-- HTML output with HTML 4.0 syntax) -->
<xsl:output method="html"
            encoding="utf-8"
            indent="no"
            doctype-system="http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd"
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
/>

<xsl:param name="OUTEXT" select="'html'"/>


</xsl:stylesheet>
