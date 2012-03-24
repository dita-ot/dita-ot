<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  enote2html_shell.xsl
 | DITA domains support for the demo set; extend as needed
 |
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="../../xsl/dita2html.xsl"/>
<xsl:import href="enote2html.xsl"/>

<!-- HTML output with HTML 4.0 syntax) -->
<xsl:output method="html"
            encoding="utf-8"
            indent="no"
            doctype-system="http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd"
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
/>

</xsl:stylesheet>
