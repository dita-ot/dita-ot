<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:x="http://www.jenitennison.com/xslt/xspec" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">

    <!--
        $stylesheet-doc is for ../../bin/xspec.* who can pass a document node as a stylesheet
        parameter but can not handle URI natively.
        Those who can pass a URI as a stylesheet parameter natively will probably prefer
        $stylesheet-uri.
    -->
    <xsl:param name="stylesheet-doc" as="document-node()?" />

    <!--
        document-uri($stylesheet-doc) returns an empty sequence on Saxon 11 when $stylesheet-doc is
        provided by the '+' command line parameter. (probably related to
        https://saxonica.plan.io/issues/4837)
        That's why base-uri() is used here in @select.
    -->
    <xsl:param name="stylesheet-uri" as="xs:string" select="base-uri($stylesheet-doc)" />

    <xsl:include href="../common/common-utils.xsl" />
    <xsl:include href="../common/namespace-vars.xsl" />
    <xsl:include href="../common/trim.xsl" />
    <xsl:include href="../common/uqname-utils.xsl" />
    <xsl:include href="../common/uri-utils.xsl" />
    <xsl:include href="../common/user-content-utils.xsl" />
    <xsl:include href="../common/yes-no-utils.xsl" />
    <xsl:include href="../compiler/base/resolve-import/resolve-import.xsl" />
    <xsl:include href="../compiler/base/util/compiler-misc-utils.xsl" />

    <xsl:output indent="yes" />

    <xsl:variable name="errors" as="xs:string+" select="'error', 'fatal'" />
    <xsl:variable name="warns" as="xs:string+" select="'warn', 'warning'" />
    <xsl:variable name="infos" as="xs:string+" select="'info', 'information'" />

    <!--
        mode="#default"
    -->
    <xsl:mode on-multiple-match="fail" on-no-match="fail" />

    <xsl:template match="document-node(element(x:description))"
        as="document-node(element(x:description))">
        <!-- Similar to the default mode template in ../compiler/base/main.xsl -->

        <!-- Resolve x:import and gather all the children of x:description -->
        <xsl:variable name="specs" as="node()+" select="x:resolve-import(x:description)" />

        <!-- Combine all the children of x:description into a single x:description -->
        <xsl:document>
            <xsl:for-each select="x:description">
                <!-- @name must not have a prefix. @inherit-namespaces must be no. Otherwise
                    the namespaces created for /x:description will pollute its descendants derived
                    from the other trees. -->
                <xsl:element name="{local-name()}" namespace="{namespace-uri()}"
                    inherit-namespaces="no">
                    <!-- Do not set all the attributes. Each imported x:description has its own set of
                        attributes. Set only the attributes that are truly global over all the XSpec
                        documents. -->

                    <!-- Global XSpec attributes -->
                    <xsl:sequence select="@measure-time | @run-as" />

                    <!-- Global Schematron attributes -->
                    <xsl:attribute name="original-xspec" select="x:document-actual-uri(/)" />
                    <xsl:attribute name="schematron" select="resolve-uri(@schematron, base-uri())" />

                    <!-- Global XSLT attributes.
                        @xslt-version can be set, because it has already been propagated from each
                        imported x:description to its descendants in mode="x:gather-specs". -->
                    <xsl:sequence select="@result-file-threshold | @threads | @xslt-version" />
                    <xsl:attribute name="stylesheet" select="$stylesheet-uri" />

                    <xsl:sequence select="$specs" />
                </xsl:element>
            </xsl:for-each>
        </xsl:document>
    </xsl:template>

    <!--
        mode="x:gather-specs"
        Adds some templates to the included mode
    -->

    <!-- The "skeleton" Schematron implementation requires a document node -->
    <xsl:template match="x:context[
        parent::*/x:expect-assert | parent::*/x:expect-not-assert |
        parent::*/x:expect-report | parent::*/x:expect-not-report |
        parent::*/x:expect-valid | ancestor::x:description[@schematron] ]"
        as="element(x:context)"
        mode="x:gather-specs">
        <xsl:copy>
            <xsl:apply-templates select="attribute()" mode="#current" />
            <xsl:where-populated>
                <xsl:attribute name="select">
                    <xsl:choose>
                        <xsl:when test="@select">
                            <xsl:text expand-text="yes">if (({@select}) => {x:known-UQName('x:wrappable-sequence')}())</xsl:text>
                            <xsl:text expand-text="yes"> then {x:known-UQName('x:wrap-nodes')}(({@select}))</xsl:text>

                            <!-- Some Schematron implementations might possibly be able to handle
                                non-document nodes. Just generate a warning and pass @select as is. -->
                            <xsl:text expand-text="yes"> else trace(({@select}), 'WARNING: Failed to wrap {name()}/@select')</xsl:text>
                        </xsl:when>

                        <xsl:when test="not(@href)">
                            <xsl:text>self::document-node()</xsl:text>
                        </xsl:when>

                        <!-- If x:context has @href but no @select, no need to construct @select in output,
                            so xsl:otherwise is omitted and xsl:where-populated produces nothing. -->
                    </xsl:choose>
                </xsl:attribute>
            </xsl:where-populated>

            <xsl:apply-templates select="node()" mode="#current" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="x:expect-assert" as="element(x:expect)" mode="x:gather-specs">
        <xsl:call-template name="create-expect">
            <xsl:with-param name="test">
                <xsl:value-of select="if (@count) then 'count' else 'exists'" />
                <xsl:text expand-text="yes">({x:known-UQName('svrl:schematron-output')}/{x:known-UQName('svrl:failed-assert')}</xsl:text>
                <xsl:apply-templates select="@*" mode="make-predicate" />
                <xsl:apply-templates select=".[normalize-space()]" mode="make-text-predicate" />
                <xsl:text>)</xsl:text>
                <xsl:value-of select="@count ! (' eq ' || .)" />
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="x:expect-not-assert" as="element(x:expect)" mode="x:gather-specs">
        <xsl:call-template name="create-expect">
            <xsl:with-param name="test">
                <xsl:text expand-text="yes">{x:known-UQName('svrl:schematron-output')}[{x:known-UQName('svrl:fired-rule')}] and empty({x:known-UQName('svrl:schematron-output')}/{x:known-UQName('svrl:failed-assert')}</xsl:text>
                <xsl:apply-templates select="@*" mode="make-predicate" />
                <xsl:text>)</xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="x:expect-report" as="element(x:expect)" mode="x:gather-specs">
        <xsl:call-template name="create-expect">
            <xsl:with-param name="test">
                <xsl:value-of select="if (@count) then 'count' else 'exists'" />
                <xsl:text expand-text="yes">({x:known-UQName('svrl:schematron-output')}/{x:known-UQName('svrl:successful-report')}</xsl:text>
                <xsl:apply-templates select="@*" mode="make-predicate" />
                <xsl:apply-templates select=".[normalize-space()]" mode="make-text-predicate" />
                <xsl:text>)</xsl:text>
                <xsl:value-of select="@count ! (' eq ' || .)" />
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="x:expect-not-report" as="element(x:expect)" mode="x:gather-specs">
        <xsl:call-template name="create-expect">
            <xsl:with-param name="test">
                <xsl:text expand-text="yes">{x:known-UQName('svrl:schematron-output')}[{x:known-UQName('svrl:fired-rule')}] and empty({x:known-UQName('svrl:schematron-output')}/{x:known-UQName('svrl:successful-report')}</xsl:text>
                <xsl:apply-templates select="@*" mode="make-predicate" />
                <xsl:text>)</xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="x:expect-valid" as="element(x:expect)" mode="x:gather-specs">
        <xsl:variable name="bad-roles" as="xs:string"
            select="
                ($errors ! ($x:apos || . || $x:apos))
                => string-join(', ')" />

        <xsl:call-template name="create-expect">
            <xsl:with-param name="label" select="'valid'"/>
            <xsl:with-param name="test">
                <xsl:text expand-text="yes">{x:known-UQName('svrl:schematron-output')}[{x:known-UQName('svrl:fired-rule')}] and empty({x:known-UQName('svrl:schematron-output')}/({x:known-UQName('svrl:failed-assert')} | {x:known-UQName('svrl:successful-report')})[empty(@role) or (lower-case(@role) = ({$bad-roles}))])</xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="x:expect-rule" as="element(x:expect)" mode="x:gather-specs">
        <xsl:call-template name="create-expect">
            <xsl:with-param name="test">
                <xsl:value-of select="if (@count) then 'count' else 'exists'" />
                <xsl:text expand-text="yes">({x:known-UQName('svrl:schematron-output')}/{x:known-UQName('svrl:fired-rule')}</xsl:text>
                <xsl:apply-templates select="@*" mode="make-predicate" />
                <xsl:text>)</xsl:text>
                <xsl:value-of select="@count ! (' eq ' || .)" />
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        mode="make-predicate"
    -->
    <xsl:mode name="make-predicate" on-multiple-match="fail" on-no-match="fail" />

    <xsl:template match="@location" as="text()" mode="make-predicate">
        <xsl:value-of expand-text="yes">
            <xsl:text>[</xsl:text>

            <xsl:text>${x:known-UQName('x:context')}/root()/({.})</xsl:text>
            <xsl:text> => </xsl:text>
            <xsl:text>{x:known-UQName('x:node-or-error')}</xsl:text>
            <xsl:text>(</xsl:text>
            <xsl:text>{x:quote-with-apos(.)}, </xsl:text>
            <xsl:text>'{parent::element() => name()}/@{name()}'</xsl:text>
            <xsl:text>)</xsl:text>

            <xsl:text> is </xsl:text>

            <xsl:text>{x:known-UQName('x:select-node')}</xsl:text>
            <xsl:text>(</xsl:text>
            <xsl:text>${x:known-UQName('x:context')}/root(), </xsl:text>
            <xsl:text>@location, </xsl:text>
            <xsl:text>preceding-sibling::{x:known-UQName('svrl:ns-prefix-in-attribute-values')}, </xsl:text>
            <xsl:text>{parent::element() => x:xslt-version()}</xsl:text>
            <xsl:text>)</xsl:text>
            <xsl:text> => </xsl:text>
            <xsl:text>{x:known-UQName('x:node-or-error')}</xsl:text>
            <xsl:text>(</xsl:text>
            <xsl:text>@location, </xsl:text>
            <xsl:text>name() || '/@location'</xsl:text>
            <xsl:text>)</xsl:text>

            <xsl:text>]</xsl:text>
        </xsl:value-of>
    </xsl:template>

    <xsl:template match="@id | @role" as="text()" mode="make-predicate">
        <xsl:text expand-text="yes">[(@{local-name()}, preceding-sibling::{x:known-UQName('svrl:fired-rule')}[1]/@{local-name()}, preceding-sibling::{x:known-UQName('svrl:active-pattern')}[1]/@{local-name()})[1] = '{.}']</xsl:text>
    </xsl:template>

    <xsl:template match="(@id | @context)[parent::x:expect-rule]" as="text()" mode="make-predicate">
        <xsl:text expand-text="yes">[@{local-name()} = '{.}']</xsl:text>
    </xsl:template>

    <xsl:template match="@count | @label | @pending" as="empty-sequence()" mode="make-predicate" />

    <xsl:template match="x:expect-assert | x:expect-report" as="text()" mode="make-text-predicate">
        <xsl:variable name="x-expect-text-content-wrapped" as="xs:string"
            select="normalize-space(.) => x:quote-with-apos()"/>
        <xsl:text expand-text="yes">[
            (
            {x:known-UQName('svrl:text')},
            {x:known-UQName('svrl:diagnostic-reference')},
            {x:known-UQName('svrl:property-reference')}
            ) ! normalize-space(.)
            = {$x-expect-text-content-wrapped}
            ]</xsl:text>
    </xsl:template>

    <!--
        Named templates
    -->

    <xsl:template name="create-expect" as="element(x:expect)">
        <!-- Context item is a Schematron-specific x:expect-* element -->
        <xsl:context-item as="element()" use="required" />

        <xsl:param name="label" as="xs:string"
            select="
                (
                    @label,
                    tokenize(local-name(), '-')[. = ('report', 'assert', 'not', 'rule')],
                    @id,
                    @role,
                    @location,
                    @context,
                    (@count ! ('count:', .)),
                    (normalize-space()[.] ! ('text:', .))
                )
                => string-join(' ')" />
        <xsl:param name="test" as="xs:string" required="yes" />

        <!-- Use x:xspec-name() for the element name so that the namespace for the name of the
            created element does not pollute the namespaces. -->
        <xsl:element name="{x:xspec-name('expect', .)}" namespace="{namespace-uri()}">
            <!-- @test may use namespace prefixes and/or the default namespace such as
                xs:QName('foo') -->
            <xsl:sequence select="x:copy-of-namespaces(.)" />

            <xsl:sequence select="@pending" />
            <xsl:attribute name="label" select="$label" />
            <xsl:attribute name="test" select="$test" />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
