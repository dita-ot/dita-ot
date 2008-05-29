<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
    exclude-result-prefixes="related-links">

    <!-- Ungrouped links have a priority of zero.  (Can be overridden.) -->
    <xsl:template match="*[contains(@class, ' topic/link ')]" mode="related-links:get-group-priority"
        name="related-links:group-priority.">
        <xsl:value-of select="0"/>
    </xsl:template>

    <!-- Ungrouped links belong to the no-name group.  (Can be overridden.)  -->
    <xsl:template match="*[contains(@class, ' topic/link ')]" mode="related-links:get-group" name="related-links:group.">
        <xsl:text/>
    </xsl:template>

    <!-- Without a group, links are emitted as-is.  (Can be overridden.) -->
    <xsl:template match="*[contains(@class, ' topic/link ')]" mode="related-links:result-group" name="related-links:group-result.">
        <xsl:param name="links"/>
        <xsl:copy-of select="$links"/>
    </xsl:template>

    <!-- Ungrouped links have the default-mode template applied to them. (Can be overridden.) -->
    <xsl:template match="*[contains(@class, ' topic/link ')]" mode="related-links:link" name="related-links:link.">
        <xsl:apply-templates select="."/>
    </xsl:template>

    <!-- Main entry point. -->
    <xsl:template match="*[contains(@class, ' topic/related-links ')]" mode="related-links:group-unordered-links">
        <!-- Node set.  The set of nodes to group. -->
        <xsl:param name="nodes"/>
        <!-- Sent back to all callback templates as a parameter.-->
        <xsl:param name="tunnel"/>

        <!-- Query all links for their group and priority. -->
        <xsl:variable name="group-priorities">
            <xsl:call-template name="related-links:get-priorities">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="tunnel" select="$tunnel"/>
            </xsl:call-template>
       </xsl:variable>

        <!-- Get order of groups based on priorities. -->
        <xsl:variable name="group-sequence">
            <xsl:call-template name="related-links:get-sequence-from-priorities">
                <xsl:with-param name="priorities" select="$group-priorities"/>
                <xsl:with-param name="tunnel" select="$tunnel"/>
            </xsl:call-template>
        </xsl:variable>

        <!-- Process the links in each group in order. -->
        <xsl:call-template name="related-links:walk-groups">
            <xsl:with-param name="nodes" select="$nodes"/>
            <xsl:with-param name="group-sequence" select="$group-sequence"/>
            <xsl:with-param name="tunnel" select="$tunnel"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Get the priorities and groups of every link. -->
    <!-- Produces a string like "2 task task ;3 concept concept ;1 reference reference ;0  topic ;",
         where the numbers are priorities of each group, and the space-delimited words
         are the groups and link types (link/@type) which belong to that group. -->
    <xsl:template name="related-links:get-priorities">
        <xsl:param name="nodes"/>
        <xsl:param name="tunnel"/>
        <xsl:param name="partial-result" select="''"/>

        <xsl:choose>
            <xsl:when test="count($nodes)">
                <!-- Process each node one at a time. -->
                <xsl:variable name="node" select="$nodes[1]"/>
                <xsl:variable name="node-group">
                    <xsl:apply-templates select="$node" mode="related-links:get-group">
                        <xsl:with-param name="tunnel" select="$tunnel"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:variable name="node-priorty">
                    <xsl:apply-templates select="$node" mode="related-links:get-group-priority">
                        <xsl:with-param name="tunnel" select="$tunnel"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:call-template name="related-links:get-priorities">
                    <xsl:with-param name="nodes" select="$nodes[position() != 1]"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                    <xsl:with-param name="partial-result">
                        <xsl:choose>
                            <!-- This type has already been seen. -->
                            <xsl:when
                                test="contains($partial-result, concat(' ', $node-group, ' '))
                                and contains($partial-result, concat(' ', $node/@type, ' '))">
                                <xsl:value-of select="$partial-result"/>
                            </xsl:when>
                            <!-- This type has not been seen, but the base group has. -->
                            <xsl:when test="contains($partial-result, concat(' ', $node-group, ' '))">
                                <xsl:value-of select="substring-before($partial-result, concat(' ', $node-group, ' '))"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$node-group"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$node/@type"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="substring-after($partial-result, concat(' ', $node-group, ' '))"/>
                            </xsl:when>
                            <!-- Never seen this base group before (nor the type). -->
                            <xsl:otherwise>
                                <xsl:value-of select="$partial-result"/>
                                <xsl:value-of select="$node-priorty"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$node-group"/>
                                <xsl:if test="$node-group != $node/@type">
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select="$node/@type"/>
                                </xsl:if>
                                <xsl:text> </xsl:text>
                                <xsl:text>;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$partial-result"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Sort groups according to their priorities, removing duplicates. -->
    <!-- Takes a string returned by related-links:get-priorities and returns
         the groups and link types in decreasing order of priority
         (e.g., "concept concept;task task;reference reference; topic;"). -->
    <xsl:template name="related-links:get-sequence-from-priorities">
        <xsl:param name="priorities"/>
        <xsl:param name="tunnel"/>
        <xsl:param name="partial-result"/>

        <xsl:choose>
            <xsl:when test="contains($priorities, ';')">
                <xsl:call-template name="related-links:get-best-priority-in-sequence">
                    <xsl:with-param name="priorities" select="$priorities"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                    <xsl:with-param name="partial-result" select="$partial-result"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$partial-result"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Find the highest-priority group remaining in the list of priorities. -->
    <xsl:template name="related-links:get-best-priority-in-sequence">
        <xsl:param name="priorities"/>
        <xsl:param name="tunnel"/>
        <xsl:param name="partial-result"/>
        <xsl:param name="best-group" select="'#none#'"/>
        <xsl:param name="best-priority" select="-1"/>
        <xsl:param name="lesser-priorities"/>

        <xsl:choose>
            <xsl:when test="contains($priorities, ';')">
                <xsl:choose>
                    <!-- First group always wins. -->
                    <xsl:when test="$best-group = '#none#'">
                        <xsl:call-template name="related-links:get-best-priority-in-sequence">
                            <xsl:with-param name="priorities" select="substring-after($priorities, ';')"/>
                            <xsl:with-param name="tunnel" select="$tunnel"/>
                            <xsl:with-param name="partial-result" select="$partial-result"/>
                            <xsl:with-param name="best-priority" select="substring-before(substring-before($priorities, ';'), ' ')"/>
                            <xsl:with-param name="best-group" select="substring-after(substring-before($priorities, ';'), ' ')"/>
                            <xsl:with-param name="lesser-priorities" select="$lesser-priorities"/>
                        </xsl:call-template>
                    </xsl:when>
                    <!-- Higher-priority group found; shunt best-so-far to lesser priorities and continue. -->
                    <xsl:when test="substring-before(substring-before($priorities, ';'), ' ') > $best-priority">
                        <xsl:call-template name="related-links:get-best-priority-in-sequence">
                            <xsl:with-param name="priorities" select="substring-after($priorities, ';')"/>
                            <xsl:with-param name="tunnel" select="$tunnel"/>
                            <xsl:with-param name="partial-result" select="$partial-result"/>
                            <xsl:with-param name="best-priority" select="substring-before(substring-before($priorities, ';'), ' ')"/>
                            <xsl:with-param name="best-group" select="substring-after(substring-before($priorities, ';'), ' ')"/>
                            <xsl:with-param name="lesser-priorities"
                                select="concat($lesser-priorities, $best-priority, ' ', $best-group, ';')"/>
                        </xsl:call-template>
                    </xsl:when>
                    <!-- Best-so-far priority is still supreme. -->
                    <xsl:otherwise>
                        <xsl:call-template name="related-links:get-best-priority-in-sequence">
                            <xsl:with-param name="priorities" select="substring-after($priorities, ';')"/>
                            <xsl:with-param name="tunnel" select="$tunnel"/>
                            <xsl:with-param name="partial-result" select="$partial-result"/>
                            <xsl:with-param name="best-priority" select="$best-priority"/>
                            <xsl:with-param name="best-group" select="$best-group"/>
                            <xsl:with-param name="lesser-priorities"
                                select="concat($lesser-priorities, substring-before($priorities, ';'), ';')"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!-- Best priority found. -->
            <xsl:otherwise>
                <xsl:call-template name="related-links:get-sequence-from-priorities">
                    <xsl:with-param name="priorities" select="$lesser-priorities"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                    <xsl:with-param name="partial-result">
                        <xsl:choose>
                            <!-- Duplicate; just move on.  (Should not happen.) -->
                            <xsl:when test="contains(concat(';', $partial-result), concat(';', $best-group, ';'))">
                                <xsl:value-of select="$partial-result"/>
                            </xsl:when>
                            <!-- Add group to list and move on. -->
                            <xsl:otherwise>
                                <xsl:value-of select="concat($partial-result, $best-group, ';')"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Process each group in turn. -->
    <xsl:template name="related-links:walk-groups">
        <xsl:param name="nodes" select="/.."/>
        <xsl:param name="tunnel"/>
        <xsl:param name="group-sequence" select="''"/>

        <xsl:choose>
            <xsl:when test="contains($group-sequence, ';')">
                <xsl:call-template name="related-links:do-group">
                    <xsl:with-param name="nodes" select="$nodes"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                    <xsl:with-param name="group" select="substring-before($group-sequence, ';')"/>
                </xsl:call-template>
                <xsl:call-template name="related-links:walk-groups">
                    <xsl:with-param name="nodes" select="$nodes"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                    <xsl:with-param name="group-sequence" select="substring-after($group-sequence, ';')"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- Process each group. -->
    <xsl:template name="related-links:do-group">
        <xsl:param name="nodes" select="/.."/>
        <xsl:param name="tunnel"/>
        <xsl:param name="group"/>

        <!-- Process the links belonging to that group.  -->
        <xsl:variable name="group-nodes" select="$nodes[contains(concat(' ', $group), concat(' ', @type, ' '))]"/>
        <!-- Let the group wrap all its links in additional elements. -->
        <xsl:apply-templates select="$group-nodes[1]" mode="related-links:result-group">
            <xsl:with-param name="links">
                <!-- Pass links as result-tree fragments, ordered according to closeness of role. -->
                <xsl:apply-templates select="$group-nodes" mode="related-links:link">
                    <xsl:sort
                        select="
                        10 * number(@role = 'parent') + 
                        9 * number(@role = 'ancestor') + 
                        8 * number(@role = 'child') + 
                        7 * number(@role = 'descendant') + 
                        6 * number(@role = 'next') + 
                        5 * number(@role = 'previous') + 
                        4 * number(@role = 'sibling') + 
                        3 * number(@role = 'cousin') + 
                        2 * number(@role = 'friend') + 
                        1 * number(@role = 'other')"
                        data-type="number" order="descending"/>
                    <!-- All @role='other' have to go together, darn. -->
                    <xsl:sort select="@otherrole" data-type="text"/>
                    <xsl:with-param name="tunnel" select="$tunnel"/>
                </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="tunnel" select="$tunnel"/>
        </xsl:apply-templates>

    </xsl:template>

</xsl:stylesheet>
