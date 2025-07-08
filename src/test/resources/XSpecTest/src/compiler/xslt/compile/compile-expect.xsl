<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/XSL/TransformAlias"
                xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="3.0">

   <!--
      Generate an XSLT template from the expect element.
      
      This generated template, when called, checks the expectation against the actual result of the
      test and constructs the corresponding x:test element for the XML report.
   -->
   <xsl:template name="x:compile-expect" as="element(xsl:template)">
      <xsl:context-item as="element(x:expect)" use="required" />

      <xsl:param name="reason-for-pending" as="xs:string?" required="yes" />

      <!-- URIQualifiedNames of the (required) parameters of the template being generated -->
      <xsl:param name="param-uqnames" as="xs:string*" required="yes" />

      <xsl:element name="xsl:template" namespace="{$x:xsl-namespace}">
         <xsl:attribute name="name" select="x:known-UQName('x:' || @id)" />
         <xsl:attribute name="as" select="'element(' || x:known-UQName('x:test') || ')'" />

         <xsl:element name="xsl:context-item" namespace="{$x:xsl-namespace}">
            <xsl:attribute name="use" select="'absent'" />
         </xsl:element>

         <xsl:for-each select="$param-uqnames">
            <param name="{.}" as="item()*" required="yes" />
         </xsl:for-each>

         <message>
            <xsl:if test="exists($reason-for-pending)">
               <xsl:text>PENDING: </xsl:text>
               <xsl:for-each select="normalize-space($reason-for-pending)[.]">
                  <xsl:text expand-text="yes">({.}) </xsl:text>
               </xsl:for-each>
            </xsl:if>
            <xsl:value-of select="x:label(.) => normalize-space()" />
         </message>

         <xsl:if test="empty($reason-for-pending)">
            <xsl:variable name="xslt-version" as="xs:decimal" select="x:xslt-version(.)" />

            <!-- Set up the $impl:expected variable -->
            <xsl:apply-templates select="." mode="x:declare-variable">
               <xsl:with-param name="comment" select="'expected result'" />
            </xsl:apply-templates>

            <!-- Flags for deq:deep-equal() enclosed in ''. -->
            <xsl:variable name="deep-equal-flags" as="xs:string"
               select="$x:apos || '1'[$xslt-version eq 1] || $x:apos" />

            <xsl:choose>
               <xsl:when test="@test">
                  <xsl:comment> wrap $x:result into a document node if possible </xsl:comment>
                  <!-- This variable declaration could be moved from here (the
                     template generated from x:expect) to the template
                     generated from x:scenario. It depends only on
                     $x:result, so could be computed only once. -->
                  <variable name="{x:known-UQName('impl:test-items')}" as="item()*">
                     <choose>
                        <!-- From trying this out, it seems like it's useful for the test
                           to be able to test the nodes that are generated in the
                           $x:result as if they were *children* of the context node.
                           Have to experiment a bit to see if that really is the case.
                           TODO: To remove. Use directly $x:result instead. (expath/xspec#14) -->
                        <when
                           test="exists(${x:known-UQName('x:result')}) and {x:known-UQName('x:wrappable-sequence')}(${x:known-UQName('x:result')})">
                           <sequence select="{x:known-UQName('x:wrap-nodes')}(${x:known-UQName('x:result')})" />
                        </when>
                        <otherwise>
                           <sequence select="${x:known-UQName('x:result')}" />
                        </otherwise>
                     </choose>
                  </variable>

                  <xsl:comment> evaluate the predicate with $x:result (or its wrapper document node) as context item if it is a single item; if not, evaluate the predicate without context item </xsl:comment>
                  <variable name="{x:known-UQName('impl:test-result')}" as="item()*">
                     <choose>
                        <when test="count(${x:known-UQName('impl:test-items')}) eq 1">
                           <for-each select="${x:known-UQName('impl:test-items')}">
                              <xsl:element name="xsl:sequence" namespace="{$x:xsl-namespace}">
                                 <!-- @test may use namespace prefixes and/or the default namespace
                                    such as xs:QName('foo') -->
                                 <xsl:sequence select="x:copy-of-namespaces(.)" />

                                 <xsl:attribute name="select" select="@test" />
                                 <xsl:attribute name="version" select="$xslt-version" />
                              </xsl:element>
                           </for-each>
                        </when>
                        <otherwise>
                           <xsl:element name="xsl:sequence" namespace="{$x:xsl-namespace}">
                              <!-- @test may use namespace prefixes and/or the default namespace
                                 such as xs:QName('foo') -->
                              <xsl:sequence select="x:copy-of-namespaces(.)" />

                              <xsl:attribute name="select" select="@test" />
                              <xsl:attribute name="version" select="$xslt-version" />
                           </xsl:element>
                        </otherwise>
                     </choose>
                  </variable>

                  <!-- TODO: Remove duality from @test. (expath/xspec#5) -->
                  <variable name="{x:known-UQName('impl:boolean-test')}" as="{x:known-UQName('xs:boolean')}"
                     select="${x:known-UQName('impl:test-result')} instance of {x:known-UQName('xs:boolean')}" />

                  <xsl:comment> did the test pass? </xsl:comment>
                  <variable name="{x:known-UQName('impl:successful')}" as="{x:known-UQName('xs:boolean')}">
                     <choose>
                        <when test="${x:known-UQName('impl:boolean-test')}">
                           <xsl:choose>
                              <xsl:when test="x:has-comparison(.)">
                                 <message terminate="yes">
                                    <xsl:text expand-text="yes">{x:boolean-with-comparison(.)}</xsl:text>
                                 </message>
                              </xsl:when>
                              <xsl:otherwise>
                                 <!-- Without boolean(), Saxon warning SXWN9000 (xspec/xspec#46).
                                    "cast as" does not work (xspec/xspec#153). -->
                                 <sequence select="${x:known-UQName('impl:test-result')} => boolean()" />
                              </xsl:otherwise>
                           </xsl:choose>
                        </when>
                        <otherwise>
                           <xsl:choose>
                              <xsl:when test="x:has-comparison(.)">
                                 <sequence select="{x:known-UQName('deq:deep-equal')}(${x:variable-UQName(.)}, ${x:known-UQName('impl:test-result')}, {$deep-equal-flags})" />
                              </xsl:when>
                              <xsl:otherwise>
                                 <message terminate="yes">
                                    <xsl:text expand-text="yes">{x:non-boolean-without-comparison(.)}</xsl:text>
                                 </message>
                              </xsl:otherwise>
                           </xsl:choose>
                        </otherwise>
                     </choose>
                  </variable>
               </xsl:when>

               <xsl:otherwise>
                  <variable name="{x:known-UQName('impl:successful')}" as="{x:known-UQName('xs:boolean')}"
                     select="{x:known-UQName('deq:deep-equal')}(${x:variable-UQName(.)}, ${x:known-UQName('x:result')}, {$deep-equal-flags})" />
               </xsl:otherwise>
            </xsl:choose>

            <if test="not(${x:known-UQName('impl:successful')})">
               <message>
                  <xsl:text>      FAILED</xsl:text>
               </message>
            </if>
         </xsl:if>

         <!-- <x:test> -->
         <xsl:element name="xsl:element" namespace="{$x:xsl-namespace}">
            <xsl:attribute name="name" select="'test'" />
            <xsl:attribute name="namespace" select="namespace-uri()" />

            <xsl:variable name="test-element-attributes" as="attribute()+">
               <xsl:sequence select="@id" />
               <xsl:sequence select="x:pending-attribute-from-reason($reason-for-pending)" />
            </xsl:variable>
            <xsl:apply-templates select="$test-element-attributes" mode="x:node-constructor" />

            <xsl:if test="empty($reason-for-pending)">
               <!-- @successful must be evaluated at run time -->
               <xsl:element name="xsl:attribute" namespace="{$x:xsl-namespace}">
                  <xsl:attribute name="name" select="'successful'" />
                  <xsl:attribute name="namespace" />
                  <xsl:attribute name="select" select="'$' || x:known-UQName('impl:successful')" />
               </xsl:element>
            </xsl:if>

            <xsl:apply-templates select="x:label(.)" mode="x:node-constructor" />

            <!-- Report -->
            <xsl:if test="empty($reason-for-pending)">
               <xsl:if test="@test">
                  <xsl:call-template name="x:report-test-attribute" />

                  <if test="not(${x:known-UQName('impl:boolean-test')})">
                     <xsl:call-template name="x:call-report-sequence">
                        <xsl:with-param name="sequence-variable-eqname"
                           select="x:known-UQName('impl:test-result')" />
                     </xsl:call-template>
                  </if>
               </xsl:if>

               <xsl:call-template name="x:call-report-sequence">
                  <xsl:with-param name="sequence-variable-eqname" select="x:variable-UQName(.)" />
                  <xsl:with-param name="report-name" select="local-name()" />
               </xsl:call-template>
            </xsl:if>

         <!-- </x:test> -->
         </xsl:element>
      </xsl:element>
   </xsl:template>

</xsl:stylesheet>