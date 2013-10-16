<?xml version="1.0" encoding="UTF-8"?>
<!--
  This file is part of the DITA Open Toolkit project.
  See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:suitesol="http://suite-sol.com/namespaces/mapcounts"
                exclude-result-prefixes="suitesol"
                version="2.0">

  <!-- FOP crashes if changebar elements appear in fo:block or fo:inline,
       which is where all are currently generated -->
  <xsl:template match="suitesol:changebar-start"/>
  <xsl:template match="suitesol:changebar-end"/>      

</xsl:stylesheet>