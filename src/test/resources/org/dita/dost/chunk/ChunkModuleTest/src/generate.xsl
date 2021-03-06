<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
                exclude-result-prefixes="xs"
                version="3.1">

  <xsl:template match="/">
    <xsl:variable name="tests" as="element()*">
      <test name="combine">
        <topicref href="a.dita" chunk="combine">
          <topicref href="b.dita">
            <topicref href="c.dita"/>
          </topicref>
        </topicref>
      </test>
      <test name="multiple"/>
    </xsl:variable>
    <xsl:call-template name="map"/>
  </xsl:template>
  
  <xsl:template name="map">
    <map class="- map/map " ditaarch:DITAArchVersion="2.0">
      <topicref class="- map/topicref " href="a.dita" chunk="combine">
        <topicref class="- map/topicref " href="b.dita">
          <topicref class="- map/topicref " href="c.dita"/>
        </topicref>
      </topicref>
    </map>
  </xsl:template>

</xsl:stylesheet>