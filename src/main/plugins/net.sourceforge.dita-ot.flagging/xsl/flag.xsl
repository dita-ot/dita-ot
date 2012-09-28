<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
 Sourceforge.net. See the accompanying license.txt file for 
 applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2012 -->
<!-- PURPOSE: Replace the XHTML based flagging routines with a common routine.
     Logic for determining what to flag is the same.
     When flags are active:
     * For images: can have multiple per element, and the ditaval information is well structured.
                   Copy in active ditaval prop and revprop information as sub-elements,
                   nested within a pseudo-foreign specialization.
     * For styles: only one style can be set per element. Because we already have code to calculate
                   CSS style here, and many output formats can potentially use that, add it
                   as @outputclass on the startprop <foreign> specialization
              -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0">

  <xsl:import href="flagImpl.xsl"/>

</xsl:stylesheet>