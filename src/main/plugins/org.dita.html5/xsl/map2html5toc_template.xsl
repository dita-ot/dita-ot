<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:import href="plugin:org.dita.xhtml:xsl/map2htmltoc.xsl"/>
  <xsl:import href="plugin:org.dita.html5:xsl/xslhtml/dita2html5Impl.xsl"/>
    
  <dita:extension id="dita.xsl.html5.toc" 
        behavior="org.dita.dost.platform.ImportXSLAction" 
        xmlns:dita="http://dita-ot.sourceforge.net"/>

  <xsl:output method="html"
              encoding="UTF-8"
              indent="no"
              doctype-system="about:legacy-compat"
              omit-xml-declaration="yes"/>

</xsl:stylesheet>