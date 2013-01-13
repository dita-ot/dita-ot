<?xml version="1.0" encoding="UTF-8"?>
<!-- 
This file is part of the DITA Open Toolkit project. 
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="2.0">

  <xsl:attribute-set name="index.see-also-entry__content">
    <xsl:attribute name="start-indent">18pt + from-parent(start-indent)</xsl:attribute>
  </xsl:attribute-set>
  
</xsl:stylesheet>