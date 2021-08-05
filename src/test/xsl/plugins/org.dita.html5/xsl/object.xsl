<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable"
                xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table simpletable">
  
  <xsl:import href="../../../../../main/plugins/org.dita.base/xsl/common/output-message.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.base/xsl/common/dita-utilities.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.base/xsl/common/functions.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.base/xsl/common/related-links.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.html5/xsl/rel-links.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.html5/xsl/topic.xsl"/>

  <!-- Mocks -->
  
  <xsl:param name="DEFAULTLANG" as="xs:string" select="'en'"/>
 
</xsl:stylesheet>
