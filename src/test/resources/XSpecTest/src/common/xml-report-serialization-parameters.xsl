<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="3.0">

   <!-- Serialization parameters for the test result report XML
      TODO: Put @parameter-document="xml-report-serialization-parameters.xml" and remove the other
         parameters after the fixes for https://saxonica.plan.io/issues/4599 and
         https://saxonica.plan.io/issues/4602 are made available on all the supported versions of
         Saxon. -->
   <xsl:output name="x:xml-report-serialization-parameters" method="xml" indent="yes" />

</xsl:stylesheet>