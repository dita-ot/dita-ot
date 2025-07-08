<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:local="urn:x-xspec:common:wrap:local"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="3.0">

   <!-- Returns true if every item in sequence can be wrapped in document node.
      Empty sequence is considered to be able to be wrapped. -->
   <xsl:function name="x:wrappable-sequence" as="xs:boolean">
      <xsl:param name="sequence" as="item()*" />

      <xsl:sequence select="every $item in $sequence satisfies local:wrappable-node($item)" />
   </xsl:function>

   <!-- Returns true if item is node and can be wrapped in document node -->
   <xsl:function name="local:wrappable-node" as="xs:boolean">
      <xsl:param name="item" as="item()" />

      <!-- Document node cannot wrap attribute node or namespace node:
         https://www.w3.org/TR/xslt-30/#err-XTDE0420 -->
      <xsl:sequence
         select="
            $item instance of node()
            and not($item instance of attribute()
                    or $item instance of namespace-node())" />
   </xsl:function>

   <!-- Wraps nodes in document node with their type annotations kept -->
   <xsl:function name="x:wrap-nodes" as="document-node()">
      <xsl:param name="nodes" as="node()*" />

      <!-- $wrap aims to create an implicit document node as described
         in https://www.w3.org/TR/xslt-30/#temporary-trees.
         So its xsl:variable must not have @as or @select.
         Do not use xsl:document or xsl:copy-of: xspec/xspec#47 -->
      <xsl:variable name="wrap">
         <xsl:sequence select="$nodes" />
      </xsl:variable> 
      <xsl:sequence select="$wrap" />
   </xsl:function>

</xsl:stylesheet>