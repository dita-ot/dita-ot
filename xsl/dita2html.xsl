<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- ereview.xsl
 | DITA topic to HTML for ereview & webreview

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                xmlns:xt="http://www.jclark.com/xt"
                extension-element-prefixes="saxon xt">

<!-- stylesheet imports -->
<!-- the main dita to xhtml converter -->
<xsl:import href="dita2xhtml.xsl"/>

<xsl:output method="html"
            encoding="UTF-8"
            indent="no"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
/>

</xsl:stylesheet>
