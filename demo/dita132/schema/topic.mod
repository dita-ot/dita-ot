<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: topic.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<!--
  Notes:
x,x   20040309-03 DRD: Change metadata from ? to *; add optional @mapkeyref (as on linkpool) to metadata
?     20040309-05 DRD: Change the body definition (section|example) to parameter entities
x    20040309-06 DRD: Add the id-atts, @translate and @xml-lang atts to the attlist for body
x     20040309-11a DRD: Add <alt> to image for non-attribute based alternate text
     20040309-11b DRD: Enable keyword/term use in previously unconrefed contexts (see words.cnt)
 +-->
<!-- TYPED TOPICS (semantic and structural specialization) ========================= -->
<!--  infotype 'topic'
    | Topic is the archetype from which other typed topics may be derived.
    | Its body has completely optional content, which allows topic to be used as a titled container
    | role: migration target for XHTML, other hierarchically structured source
*-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>

  <!-- ==================== Include Section - Start ======================= -->
  <!-- ======== Table elements ======== -->
  <xs:include schemaLocation="tbl_xml.mod"/>
  <xs:include schemaLocation="tbl_xml.grp"/>

  <!-- ======= MetaData elements, plus keyword and indexterm ======= -->
  <xs:include schemaLocation="meta_xml.mod"/>
  <xs:include schemaLocation="meta_xml.grp"/>

  <!-- ==================== Include Section - End ======================= -->
  <xs:annotation>
    <xs:documentation>Display attributes is a parameter entity declaration in the topic DTD that includes
attributes whose values may be used for affecting the display of a topic or its
selection by search tools.
    </xs:documentation>
  </xs:annotation>

  <xs:attributeGroup name="display-atts">
    <xs:attribute name="scale" type="scale-atts.class"/>
    <xs:attribute name="frame" type="frame-att.class"/>
    <xs:attribute name="expanse" type="expanse-att.class"/>
  </xs:attributeGroup>

  <xs:simpleType name="frame-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="top"/>
      <xs:enumeration value="bottom"/>
      <xs:enumeration value="topbot"/>
      <xs:enumeration value="all"/>
      <xs:enumeration value="sides"/>
      <xs:enumeration value="none"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="expanse-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="page"/>
      <xs:enumeration value="column"/>
      <xs:enumeration value="textline"/>
    </xs:restriction>
  </xs:simpleType>

  <!-- these are common for some classes of resources and exhibits -->
  <xs:annotation>
    <xs:documentation>Relation attributes is a parameter entity declaration in the topic DTD that includes
attributes whose values may be used for conditional processing or for selection by
search tools.
        </xs:documentation>
  </xs:annotation>
  <xs:attributeGroup name="rel-atts">
    <xs:attribute name="type" type="xs:string"/>
    <xs:attribute name="role" type="role-att.class"/>
    <xs:attribute name="otherrole" type="xs:string"/>
  </xs:attributeGroup>

  <xs:annotation>
    <xs:documentation>ID attributes is a parameter entity declaration in the topic DTD that includes
attributes whose values may be used for conditional processing or for selection by
search tools.</xs:documentation>
  </xs:annotation>
  <xs:attributeGroup name="id-atts">
    <xs:attribute name="id" type="xs:NMTOKEN"/>
    <xs:attribute name="conref" type="xs:string"/>
  </xs:attributeGroup>

  <xs:annotation>
    <xs:documentation>Select attributes is a parameter entity declaration in the topic DTD that includes
attributes whose values may be used for affecting the display of a topic or its
selection by search tools.</xs:documentation>
  </xs:annotation>
  <xs:attributeGroup name="select-atts">
    <xs:attribute name="platform" type="xs:string"/>
    <xs:attribute name="product" type="xs:string"/>
    <xs:attribute name="audience" type="xs:string"/>
    <xs:attribute name="otherprops" type="xs:string"/>
    <xs:attribute name="rev" type="xs:string"/>
    <xs:attribute name="importance" type="importance-atts.class"/>
    <xs:attribute name="status" type="status-atts.class"/>
  </xs:attributeGroup>

  <xs:simpleType name="importance-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="obsolete"/>
      <xs:enumeration value="deprecated"/>
      <xs:enumeration value="optional"/>
      <xs:enumeration value="default"/>
      <xs:enumeration value="low"/>
      <xs:enumeration value="normal"/>
      <xs:enumeration value="high"/>
      <xs:enumeration value="recommended"/>
      <xs:enumeration value="required"/>
      <xs:enumeration value="urgent"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="scale-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="50"/>
      <xs:enumeration value="60"/>
      <xs:enumeration value="70"/>
      <xs:enumeration value="80"/>
      <xs:enumeration value="90"/>
      <xs:enumeration value="100"/>
      <xs:enumeration value="110"/>
      <xs:enumeration value="120"/>
      <xs:enumeration value="140"/>
      <xs:enumeration value="160"/>
      <xs:enumeration value="180"/>
      <xs:enumeration value="200"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="status-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="new"/>
      <xs:enumeration value="changed"/>
      <xs:enumeration value="deleted"/>
      <xs:enumeration value="unchanged"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:annotation>
    <xs:documentation>Universal attributes is a parameter entity declaration in the topic DTD that
includes all of the attributes in the select-atts and id-atts attribute groups. </xs:documentation>
  </xs:annotation>
  <xs:attributeGroup name="univ-atts">
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
  </xs:attributeGroup>

  <xs:attributeGroup name="global-atts">
    <xs:attribute name="xtrc" type="xs:string"/>
    <xs:attribute name="xtrf" type="xs:string"/>
  </xs:attributeGroup>

  <!-- =========================================================================== -->
  <!-- COMMON GROUP DECLARATIONS - START ======================================= -->
  <!-- =========================================================================== -->
  <!-- Phrase or inline elements of various classes
    <!ENTITY % basic.ph             "%ph;|%term;|%xref;|%cite;|%q;|%state;|%keyword;|%tm;"> -->
  <xs:group name="basic.ph">
    <xs:choice>
      <xs:group ref="ph"/>
      <xs:group ref="term"/>
      <xs:group ref="xref"/>
      <xs:group ref="cite"/>
      <xs:group ref="q"/>
      <xs:group ref="state"/>
      <xs:group ref="boolean"/>
      <xs:group ref="keyword"/>
      <xs:group ref="tm"/>
    </xs:choice>
  </xs:group>

  <!-- Elements common to most body-like contexts -->
  <xs:group name="basic.block">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="lq"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="fig"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
      <xs:group ref="table"/>
      <xs:group ref="simpletable"/>
    </xs:choice>
  </xs:group>

  <!-- class groupings to preserve in a schema
    <!ENTITY % basic.phandblock     "%basic.ph; | %basic.block;">
    -->
  <xs:group name="basic.phandblock">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block"/>
    </xs:choice>
  </xs:group>

  <!-- Exclusions:.models.modified by removing excluded content -->
  <!-- <!ENTITY % basic.ph.noxref      "%ph;|%term;|              %q;|%state;|%keyword;|%tm">  -->

  <xs:group name="basic.ph.noxref">
    <xs:choice>
      <xs:group ref="ph"/>
      <xs:group ref="term"/>
      <xs:group ref="cite"/>
      <xs:group ref="q"/>
      <xs:group ref="state"/>
      <xs:group ref="boolean"/>
      <xs:group ref="keyword"/>
      <xs:group ref="tm"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.notbl    "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%pre;|%lines;|%fig;|%image;|%object;">  -->
  <xs:group name="basic.block.notbl">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="lq"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="fig"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.nonote   "%p;|%lq;|       %dl;|%ul;|%ol;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">  -->
  <xs:group name="basic.block.nonote">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="lq"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="fig"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
      <xs:group ref="table"/>
      <xs:group ref="simpletable"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.nopara   "    %lq;|%note;|%dl;|%ul;|%ol;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">  -->
  <xs:group name="basic.block.nopara">
    <xs:choice>
      <xs:group ref="lq"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="fig"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
      <xs:group ref="table"/>
      <xs:group ref="simpletable"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.nolq     "%p;|     %note;|%dl;|%ul;|%ol;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">  -->
  <xs:group name="basic.block.nolq">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="fig"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
      <xs:group ref="table"/>
      <xs:group ref="simpletable"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.notbnofg  "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%pre;|%lines;|      %image;|%object;">  -->
  <xs:group name="basic.block.notbnofg">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="lq"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="image"/>
      <xs:group ref="object"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % basic.block.notbfgobj "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%pre;|%lines;|      %image;">  -->
  <xs:group name="basic.block.notbfgobj">
    <xs:choice>
      <xs:group ref="p"/>
      <xs:group ref="lq"/>
      <xs:group ref="note"/>
      <xs:group ref="dl"/>
      <xs:group ref="ul"/>
      <xs:group ref="ol"/>
      <xs:group ref="sl"/>
      <xs:group ref="pre"/>
      <xs:group ref="lines"/>
      <xs:group ref="image"/>
    </xs:choice>
  </xs:group>

  <!-- Phrase or inline elements of various classes
    <!ENTITY % basic.ph             "%ph;|%term;|%xref;|%cite;|%q;|%state;|%keyword;"> -->

  <xs:group name="basic.ph.notm">
    <xs:choice>
      <xs:group ref="ph"/>
      <xs:group ref="term"/>
      <xs:group ref="xref"/>
      <xs:group ref="cite"/>
      <xs:group ref="q"/>
      <xs:group ref="state"/>
      <xs:group ref="boolean"/>
      <xs:group ref="keyword"/>
    </xs:choice>
  </xs:group>

  <!-- Inclusions: defined sets that can be added into appropriate.models -->
  <xs:group name="txt.incl">
    <xs:choice>
      <xs:group ref="draft-comment"/>
      <xs:group ref="required-cleanup"/>
      <xs:group ref="fn"/>
      <xs:group ref="indextermref"/>
      <xs:group ref="indexterm"/>
    </xs:choice>
  </xs:group>

  <!-- =========================================================================== -->
  <!-- COMMON GROUP DECLARATIONS - END ======================================= -->
  <!-- =========================================================================== -->
  <!-- Predefined content.model groups, based on the previous, element-only categories: -->
  <!-- txt.incl is appropriate for any mixed content definitions (those that have PCDATA) -->
  <!-- the context for blocks is implicitly an InfoMaster "containing_division" -->
  <!-- <!ENTITY % body.cnt             "%basic.block;|%required-cleanup;"> -->
  <xs:group name="body.cnt">
    <xs:choice>
      <xs:group ref="basic.block"/>
      <xs:group ref="required-cleanup"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % fig.cnt              "%basic.block.notbnofg; | %simpletable;"> -->
  <xs:group name="fig.cnt">
    <xs:choice>
      <xs:group ref="basic.block.notbnofg"/>
      <xs:group ref="simpletable"/>
      <xs:group ref="xref"/>
      <xs:group ref="fn"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % section.cnt          "#PCDATA | %basic.ph; | %basic.block; | %title; |  %txt.incl;"> -->
  <xs:group name="section.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block"/>
      <xs:group ref="title"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % section.notitle.cnt  "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;"> -->
  <xs:group name="section.notitle.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % desc.cnt             "#PCDATA | %basic.ph; | %basic.block.notbfgobj;">> -->
  <xs:group name="desc.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.notbfgobj"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % note.cnt             "#PCDATA | %basic.ph; | %basic.block.nonote; | %txt.incl;"> -->
  <xs:group name="note.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.nonote"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % fn.cnt               "#PCDATA | %basic.ph; | %basic.block.notbl;"> -->
  <xs:group name="fn.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.notbl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % ph.cnt               "#PCDATA | %basic.ph; | %image;              | %txt.incl;"> -->
  <xs:group name="ph.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="image"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % tblcell.cnt          "#PCDATA | %basic.ph; | %basic.block.notbl;  | %txt.incl;"> -->
  <xs:group name="tblcell.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.notbl"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % itemgroup.cnt        "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;"> -->
  <xs:group name="itemgroup.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % listitem.cnt         "#PCDATA | %basic.ph; | %basic.block; |%itemgroup;| %txt.incl;"> -->
  <xs:group name="listitem.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block"/>
      <xs:group ref="itemgroup"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % para.cnt             "#PCDATA | %basic.ph; | %basic.block.nopara; | %txt.incl;"> -->
  <xs:group name="para.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.nopara"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % longquote.cnt        "#PCDATA | %basic.ph; | %basic.block.nolq;   | %txt.incl;"> -->
  <xs:group name="longquote.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="basic.block.nolq"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % shortquote.cnt       "#PCDATA | %basic.ph;"> -->
  <xs:group name="shortquote.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % defn.cnt             "%listitem.cnt;"> -->
  <xs:group name="defn.cnt">
    <xs:choice>
      <xs:group ref="listitem.cnt"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % pre.cnt              "#PCDATA | %basic.ph; | %txt.incl;"> -->
  <xs:group name="pre.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="txt.incl"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % term.cnt             "#PCDATA | %basic.ph; | image"> -->
  <xs:group name="term.cnt">
    <xs:choice>
      <xs:group ref="basic.ph"/>
      <xs:group ref="image"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % xreftext.cnt         "#PCDATA | %basic.ph.noxref; | %image;"> -->
  <xs:group name="xreftext.cnt">
    <xs:choice>
      <xs:group ref="basic.ph.noxref"/>
      <xs:group ref="image"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % title.cnt            "#PCDATA | %basic.ph.noxref; | %image;"> -->
  <xs:group name="title.cnt">
    <xs:choice>
      <xs:group ref="basic.ph.noxref"/>
      <xs:group ref="image"/>
    </xs:choice>
  </xs:group>

  <!-- <!ENTITY % xrefph.cnt           "#PCDATA | %basic.ph.noxref;"> -->
  <xs:group name="xrefph.cnt">
    <xs:choice>
      <xs:group ref="basic.ph.noxref"/>
    </xs:choice>
  </xs:group>

  <!-- new <!ENTITY % words.cnt               "#PCDATA | %keyword; | %term;"> -->
  <xs:group name="words.cnt">
    <xs:choice>
      <xs:group ref="keyword"/>
      <xs:group ref="term"/>
    </xs:choice>
  </xs:group>

  <!-- STRUCTURAL MEMBERS ======================================================== -->
  <!-- things that can be nested under topic after body - redefined when specializing -->
  <xs:simpleType name="topicreftypes-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="topic"/>
      <xs:enumeration value="concept"/>
      <xs:enumeration value="task"/>
      <xs:enumeration value="reference"/>
      <xs:enumeration value="external"/>
      <xs:enumeration value="local"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="role-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="parent"/>
      <xs:enumeration value="child"/>
      <xs:enumeration value="sibling"/>
      <xs:enumeration value="friend"/>
      <xs:enumeration value="next"/>
      <xs:enumeration value="previous"/>
      <xs:enumeration value="cousin"/>
      <xs:enumeration value="ancestor"/>
      <xs:enumeration value="descendant"/>
      <xs:enumeration value="sample"/>
      <xs:enumeration value="external"/>
      <xs:enumeration value="other"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="xref-type-attlist.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="fig"/>
      <xs:enumeration value="figgroup"/>
      <xs:enumeration value="table"/>
      <xs:enumeration value="li"/>
      <xs:enumeration value="fn"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="view-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="internal"/>
      <xs:enumeration value="classified"/>
      <xs:enumeration value="all"/>
      <xs:enumeration value="entitled"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="workflow-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="author"/>
      <xs:enumeration value="editor"/>
      <xs:enumeration value="reviewer"/>
      <xs:enumeration value="publisher"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="job-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="installing"/>
      <xs:enumeration value="customizing"/>
      <xs:enumeration value="administratoring"/>
      <xs:enumeration value="programming"/>
      <xs:enumeration value="using"/>
      <xs:enumeration value="maintianing"/>
      <xs:enumeration value="troubleshooting"/>
      <xs:enumeration value="evaluating"/>
      <xs:enumeration value="planning"/>
      <xs:enumeration value="other"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="experiencelevel-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="novice"/>
      <xs:enumeration value="general"/>
      <xs:enumeration value="expert"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="disposition-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="issue"/>
      <xs:enumeration value="open"/>
      <xs:enumeration value="accepted"/>
      <xs:enumeration value="rejected"/>
      <xs:enumeration value="deferred"/>
      <xs:enumeration value="duplicate"/>
      <xs:enumeration value="reopened"/>
      <xs:enumeration value="unassigned"/>
      <xs:enumeration value="completed"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="yesno-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="yes"/>
      <xs:enumeration value="no"/>
    </xs:restriction>
  </xs:simpleType>

   <xs:group name="topic-info-types">
      <xs:choice>
        <xs:group ref="topic"/>
      </xs:choice>
    </xs:group>

  <!--  infotype 'topic'
 | Topic is the archetype from which other typed topics may be derived.
 | Its body has completely optional content, which allows topic to be used as a titled container
 | role: migration target for XHTML, other hierarchically structured source
 *-->
  <xs:element name="topic" type="topic.class">
    <xs:annotation>
      <xs:documentation>Topic is the archetype from which other typed topics may be derived. Its body has completely optional content, which allows topic to be used as a titled container role: migration target for XHTML, other hierarchically structured source.

This is the top-level DITA element for a single-subject topic or article. Other top-level DITA elements that are more content-specific are &lt;concept&gt;, &lt;task&gt;, and &lt;reference&gt;.
</xs:documentation>
    </xs:annotation>
  </xs:element>
  <xs:complexType name="topic.class">
    <xs:sequence>
      <xs:group ref="title"/>
      <xs:group ref="titlealts" minOccurs="0"/>
      <xs:group ref="shortdesc" minOccurs="0"/>
      <xs:group ref="prolog" minOccurs="0"/>
      <xs:group ref="body"/>
      <xs:group ref="related-links" minOccurs="0"/>
      <xs:group ref="info-types" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="id" type="xs:ID" use="required"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="conref"/>
    <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.4"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/topic "/>
  </xs:complexType>

  <!-- Rename null to no-topic-nesting -->
  <xs:element name="no-topic-nesting" type="no-topic-nesting.class"/>
  <xs:complexType name="no-topic-nesting.class">
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/no-topic-nesting "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a heading or label for the main parts of a document such as
&lt;topic&gt;, &lt;section&gt;, and &lt;example&gt; and for the exhibit elements such as figure
&lt;fig&gt; and &lt;table&gt;.
        </xs:documentation>
  </xs:annotation>
  <!-- This is referenced inside CALS tables -->
  <xs:element name="title" type="title.class"/>
  <xs:complexType name="title.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="title.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/title "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="titlealts" type="titlealts.class"/>
  <xs:complexType name="titlealts.class">
    <xs:sequence>
      <xs:group ref="navtitle" minOccurs="0"/>
      <xs:group ref="searchtitle" minOccurs="0"/>
    </xs:sequence>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/titlealts "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="navtitle" type="navtitle.class"/>
  <xs:complexType name="navtitle.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/navtitle "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="searchtitle" type="searchtitle.class"/>
  <xs:complexType name="searchtitle.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/searchtitle "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a short description of a topic which indicates the content or
intent of the topic more completely than the title. This element does not allow
paragraphs; use &lt;longdesc&gt; if paragraphs are needed for a longer description. One
possible use of this element is to provide a link preview or hover-help for links to
this topic from other topics or documents.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="shortdesc" type="shortdesc.class"/>
  <xs:complexType name="shortdesc.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="title.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/shortdesc "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This is the containing element for the main content of a &lt;topic&gt;.</xs:documentation>
  </xs:annotation>
  <xs:element name="body" type="body.class"/>
  <xs:complexType name="body.class">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="body.cnt"/>
      <xs:group ref="section"/>
      <xs:group ref="example"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <!--xs:attributeGroup ref="univ-atts"/-->
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/body "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation> This element is a division in a &lt;topic&gt;. Use sections for organizing subsets of
information that are directly related to the topic. For example, Syntax, Usage, and
Example might all be sections within a topic about a command-line process.
Sections within a&lt;topic&gt; do not represent a hierarchy, but rather parallel divisions
of that &lt;topic&gt; so they cannot be nested. If you want to accomplish nesting (for
example, documenting options within a process each of which has its own syntax,
usage and example), do so by creating subtopics insteads. Section &lt;title&gt;s are
optional and should be used first in a section.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="section" type="section.class"/>
  <xs:complexType name="section.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.cnt"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element is a section that has the specific role of containing examples that
illustrate or support the current topic. &lt;example&gt; has the same content.model as
&lt;section&gt;.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="example" type="example.class"/>
  <xs:complexType name="example.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.cnt"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/example "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains the description of a figure, table, object, or linkgroup. A
8lt;desc&gt; should provide more information than the title.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="desc" type="desc.class">
    <xs:annotation>
      <xs:documentation>This could be trex, xpath, relax, xsd, etc.</xs:documentation>
    </xs:annotation>
  </xs:element>
  <xs:complexType name="desc.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="desc.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/desc "/>
  </xs:complexType>

  <!-- PROLOG (metadata for topics) =================================== -->
  <!-- TYPED DATA ELEMENTS: ======================================================= -->
  <!-- typed content definitions  -->
  <!-- typed, localizable content -->
  <xs:annotation>
    <xs:documentation>The prolog contains information about the whole topic (for example, author
information, subject category, and relationships to other topics) that is either
entered by the author or machine-maintained. Much of the metadata will not be
displayed with the topic on output, but may be used by processes generating
search indexes, or customizing navigation.

Links defined in the prolog are typically displayed as part of the topic on output,
but their placement in the output will be dependent on the process, and is not
directly controlled by the author.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="prolog" type="prolog.class"/>
  <xs:complexType name="prolog.class">
    <xs:sequence>
      <xs:group ref="author" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="source" minOccurs="0"/>
      <xs:group ref="publisher" minOccurs="0"/>
      <xs:group ref="copyright" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="critdates" minOccurs="0"/>
      <xs:group ref="permissions" minOccurs="0"/>
      <xs:group ref="metadata" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="resourceid" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/prolog "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           The metadata section of the &lt;prolog&gt; contains information about a topic such as
audience and product information. Metadata can be used by processes to select
particular topics or to prepare search indexes or customize navigation.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="metadata" type="metadata.class"/>
  <xs:complexType name="metadata.class">
    <xs:sequence>
      <xs:group ref="audience" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="category" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="keywords" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="prodinfo" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="othermeta" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="mapkeyref" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/metadata "/>
  </xs:complexType>

  <!-- ================ LINKS GROUPING - START ================ -->
  <xs:annotation>
    <xs:documentation>This element allows multiple links to be specified in a &lt;prolog&gt;. At processing
time, they may be displayed elsewhere (for example, at the end of the topic). By
relegating links to the prolog, it becomes easier to reuse the content of the topic in
new collections or delivery contexts, where the related topics may not be available.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="related-links" type="related-links.class"/>
  <xs:complexType name="related-links.class">
    <xs:choice maxOccurs="unbounded">
      <xs:group ref="link"/>
      <xs:group ref="linklist"/>
      <xs:group ref="linkpool"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="rel-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attribute name="format" type="xs:string"/>
    <xs:attribute name="scope" type="scope-att.class"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/related-links "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="linklist" type="linklist.class"/>
  <xs:complexType name="linklist.class">
    <xs:sequence>
      <xs:group ref="title" minOccurs="0"/>
      <xs:group ref="desc" minOccurs="0"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="linklist"/>
        <xs:group ref="link"/>
      </xs:choice>
      <xs:group ref="linkinfo" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="collection-type" type="collection-type.class"/>
    <xs:attribute name="duplicates" type="yesno-att.class"/>
    <xs:attribute name="mapkeyref" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="format" type="xs:string"/>
    <xs:attribute name="scope" type="scope-att.class"/>
    <xs:attributeGroup ref="rel-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/linklist "/>
  </xs:complexType>

  <xs:element name="linkinfo" type="linkinfo.class"/>
  <xs:complexType name="linkinfo.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="desc.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/linkinfo "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="linkpool" type="linkpool.class"/>
  <xs:complexType name="linkpool.class">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="linkpool"/>
      <xs:group ref="link"/>
    </xs:choice>
    <xs:attribute name="collection-type" type="collection-type.class"/>
    <xs:attribute name="duplicates" type="yesno-att.class"/>
    <xs:attribute name="mapkeyref" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="format" type="xs:string"/>
    <xs:attribute name="scope" type="scope-att.class"/>
    <xs:attributeGroup ref="rel-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/linkpool "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="linktext" type="linktext.class"/>
  <xs:complexType name="linktext.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/linktext "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element defines a hyperlink to another topic (either in the same file or a
different file). If you leave the title empty, the text of the hyperlink will be derived
from the title of the target. The link placement on output will depend on the type
of the link and the logic of the output process. Often hyperlinks are displayed in a
group at the end of the topic, even though they are defined in a topics prolog.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="link" type="link.class"/>
  <xs:complexType name="link.class">
    <xs:sequence>
      <xs:group ref="linktext" minOccurs="0"/>
      <xs:group ref="desc" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="href" type="xs:string"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="format" type="xs:string"/>
    <xs:attribute name="scope" type="scope-att.class"/>
    <xs:attributeGroup ref="rel-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/link "/>
  </xs:complexType>

  <xs:simpleType name="collection-type.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="choice"/>
      <xs:enumeration value="unordered"/>
      <xs:enumeration value="sequence"/>
      <xs:enumeration value="family"/>
    </xs:restriction>
  </xs:simpleType>

  <!-- ================ LINKS GROUPING - END ================ -->
  <xs:annotation>
    <xs:documentation>The &lt;ph&gt; element logically groups a set of words or phrase-level elements, for
selection by property or formatting according to the typestyle attribute. Phrases
can define containment structures to associate one element with another, such as
associating a footnote with a specific sentence, or they can be used as a base for
specialization to create specific kinds of phrases. When specializing, do not include
the typestyle attribute: once you have identified a semantic class of information,
you should associate formatting using a stylesheet. Storing formatting instructions
in topic content limits the reusability of the information, and is only appropriate
when authoring at a base level, where there aren't enough semantic elements for a
stylesheet to operate on.

The &lt;ph&gt; element can also be used to associate a specific property with a specific
phrase. For example, you can associate a revision or version level with a phrase, or
identify a word as a particular type of data for special processing.

This element is also used during source migration when it is not apparent which
new elements should be used for tagging.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="ph" type="ph.class"/>
  <xs:complexType name="ph.class" mixed="true">
    <xs:annotation>
      <xs:documentation>var &amp; keyword defined by syntax diagram</xs:documentation>
    </xs:annotation>
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ph "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element identifies a term that is specific to a particular vocabulary. It may be
used to generate keyword links to a glossary, or to generate a glossary based on
terms used in a particular collection of topics.
        </xs:documentation>
  </xs:annotation>
  <xs:element name="term" type="term.class"/>
  <xs:complexType name="term.class" mixed="true">
    <xs:annotation>
      <xs:documentation>Inline content (prhases)</xs:documentation>
    </xs:annotation>
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tm"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/term "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="tm" type="tm.class"/>
  <xs:complexType name="tm.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tm"/>
    </xs:choice>
    <xs:attribute name="trademark" type="xs:string"/>
    <xs:attribute name="tmowner" type="xs:string"/>
    <xs:attribute name="tmtype" type="tmtype-type-att.class" use="required"/>
    <xs:attribute name="tmclass" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/tm "/>
  </xs:complexType>

  <xs:simpleType name="tmtype-type-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="tm"/>
      <xs:enumeration value="reg"/>
      <xs:enumeration value="service"/>
    </xs:restriction>
  </xs:simpleType>
  
    <xs:annotation>
    <xs:documentation>Use this element to express yes or no values, or true or false
values. The element itself is empty; you store the value of the element in
its state attribute. This element is primarily for specialization, where it
could be used to require a true | false choice in a particular part of the
document. For example, a specialized application program interface (API) topic
type could include an &lt;abstractclass> element as a specialization of &lt;boolean>,
to allow authors to specify whether the interface being documented is abstract or
concrete.</xs:documentation>
  </xs:annotation>
  <xs:element name="boolean" type="boolean.class"/>
  <xs:complexType name="boolean.class">
    <xs:attribute name="state" type="yesno-att.class" use="required"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/boolean "/>
  </xs:complexType>


  <xs:annotation>
    <xs:documentation>This empty element can specify a name/value pair. It is primarily
intended for use by specializations, which can create specific kinds of state
elements with fixed name values and a choice of values.  For example, a specialized
&lt;accesstype> element could have a fixed name of "Access Type" (defined
in a side file for translation purposes) and enumerated values of public,
protected, and private.</xs:documentation>
  </xs:annotation>
  <xs:element name="state" type="state.class"/>
  <xs:complexType name="state.class">
    <xs:attribute name="name" type="xs:string" use="required"/>
    <xs:attribute name="value" type="xs:string" use="required"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/state "/>
  </xs:complexType>

  <!---->
  <!-- MIME type should follow the convention xxx/xxx -->
  <!-- =========================================================================== -->
  <!-- BASIC DOCUMENT ELEMENT DECLARATIONS (rich text) =========================== -->
  <!-- =========================================================================== -->
  <!-- Base form: Paragraph -->
  <xs:annotation>
    <xs:documentation> A paragraph is a block of text containing a single main idea.</xs:documentation>
  </xs:annotation>
  <xs:element name="p" type="p.class"/>
  <xs:complexType name="p.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="para.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/p "/>
  </xs:complexType>

  <!-- Base form: Excerpt -->
  <xs:annotation>
    <xs:documentation> This element indicates content quoted from another source. Use &lt;q>
for quotations that are too long for inline use. You can store a link to the
source of the quotation in the href attribute. </xs:documentation>
  </xs:annotation>
  <xs:element name="lq" type="lq.class"/>
  <xs:complexType name="lq.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="longquote.cnt"/>
    </xs:choice>
    <xs:attribute name="href" type="xs:string" />
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="type" type="type-lq-atts.class"/>
    <xs:attribute name="reftitle" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/lq "/>
  </xs:complexType>

  <xs:simpleType name="type-lq-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="internal"/>
      <xs:enumeration value="external"/>
      <xs:enumeration value="bibliographic"/>
    </xs:restriction>
  </xs:simpleType>

  <!-- Base form: Note -->
  <xs:annotation>
    <xs:documentation>A note contains information, differentiated from the main text, which
expands on or calls attention to a particular point.</xs:documentation>
  </xs:annotation>
  <xs:element name="note" type="note.class"/>
  <xs:complexType name="note.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="note.cnt"/>
    </xs:choice>
    <xs:attribute name="type" type="type-note-atts.class"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="othertype" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/note "/>
  </xs:complexType>

  <xs:simpleType name="type-note-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="note"/>
      <xs:enumeration value="tip"/>
      <xs:enumeration value="fastpath"/>
      <xs:enumeration value="restriction"/>
      <xs:enumeration value="important"/>
      <xs:enumeration value="remember"/>
      <xs:enumeration value="attention"/>
      <xs:enumeration value="caution"/>
      <xs:enumeration value="danger"/>
      <xs:enumeration value="other"/>
    </xs:restriction>
  </xs:simpleType>

  <!-- Base form: Quoted text -->
  <xs:annotation>
    <xs:documentation> A quotation phrase indicates content quoted from another source.
 This element is used inline; use &lt;lq> for long quotations set off from
the surrounding text.</xs:documentation>
  </xs:annotation>
  <xs:element name="q" type="q.class"/>
  <xs:complexType name="q.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="shortquote.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/q "/>
  </xs:complexType>

  <!-- Base form: Unordered list -->
  <xs:annotation>
    <xs:documentation>This is an unordered list where the order of the list items is not
significant.</xs:documentation>
  </xs:annotation>
  <xs:element name="ul" type="ul.class"/>
  <xs:complexType name="ul.class">
    <xs:choice>
      <xs:group ref="li" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="compact" type="yesno-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ul "/>
  </xs:complexType>

  <!-- Base form: Ordered list -->
  <xs:annotation>
    <xs:documentation>An ordered list is a list of items sorted by sequence or order of
importance.</xs:documentation>
  </xs:annotation>
  <xs:element name="ol" type="ol.class"/>
  <xs:complexType name="ol.class">
    <xs:choice>
      <xs:group ref="li" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="compact" type="yesno-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ol "/>
  </xs:complexType>

  <!-- Base form: Simple list -->
  <xs:annotation>
    <xs:documentation>An ordered list is a list of items sorted by sequence or order of
importance.</xs:documentation>
  </xs:annotation>
  <xs:element name="sl" type="sl.class"/>
  <xs:complexType name="sl.class">
    <xs:choice>
      <xs:group ref="sli" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="compact" type="yesno-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/sl "/>
  </xs:complexType>

  <!-- Base form: Simple List Item -->
  <xs:element name="sli" type="sli.class"/>
  <xs:complexType name="sli.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/sli "/>
  </xs:complexType>

  <!-- Base form: List Item -->
  <xs:annotation>
    <xs:documentation>A list item is a single item in an ordered &lt;ol> or unordered &lt;ul>
list. Numbers and alpha characters are usually output with list items in ordered
lists; bullets and dashes are usually output with list items in unordered
lists.</xs:documentation>
  </xs:annotation>
  <xs:element name="li" type="li.class"/>
  <xs:complexType name="li.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="listitem.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/li "/>
  </xs:complexType>

  <!-- Base form: List Item Section-->
  <xs:annotation>
    <xs:documentation>This element allows specialization in a list item.  For example,
if you want to create a new element that represents part of a list item (for
example, an "additional information" section of a task step), you can specialize
from lisection. In topic, lisection has no purpose other than to logically
group content within a list item; it has no intended display characteristics. </xs:documentation>
  </xs:annotation>
  <xs:element name="itemgroup" type="itemgroup.class"/>
  <xs:complexType name="itemgroup.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="itemgroup.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/itemgroup "/>
  </xs:complexType>

  <!-- Base form: Definition List -->
  <xs:annotation>
    <xs:documentation>A definition list is a list of terms and corresponding definitions.
 The term (&lt;dt>) is usually flush left.  The description or definition
(&lt;dd>) is usually either indented and on the next line, or on the same
line to the right of the term.You may also provide an optional heading
for the terms and definitions, using the &lt;dlhead> element, which contains
header elements for those columns.  The default formatting looks like a table
with a heading row.</xs:documentation>
  </xs:annotation>
  <xs:element name="dl" type="dl.class"/>
  <xs:complexType name="dl.class">
    <xs:sequence>
      <xs:group ref="dlhead" minOccurs="0"/>
      <xs:group ref="dlentry" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="compact" type="yesno-att.class"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dl "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>A &lt;dlhead> contains optional headings for the term and description
columns in a definition list.  &lt;dlhead> contains a heading &lt;dthd> for
the column of terms and an optional heading &lt;ddhd> for the column of descriptions.
The default formatting looks like a table with a heading row.</xs:documentation>
  </xs:annotation>
  <xs:element name="dlhead" type="dlhead.class"/>
  <xs:complexType name="dlhead.class">
    <xs:sequence>
      <xs:group ref="dthd" minOccurs="0"/>
      <xs:group ref="ddhd" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dlhead "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element can contain an optional heading or title for a column
of descriptions or definitions in a definition list.</xs:documentation>
  </xs:annotation>
  <xs:element name="ddhd" type="ddhd.class"/>
  <xs:complexType name="ddhd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="title.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ddhd "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element is contained in a definition or description list head
(&lt;dlhead>) and provides an optional heading for the column of terms in
a description list.</xs:documentation>
  </xs:annotation>
  <xs:element name="dthd" type="dthd.class"/>
  <xs:complexType name="dthd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="title.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dthd "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a single entry in a definition list that includes
a term &lt;dt> and one or more definitions or descriptions &lt;dd> of that
term.</xs:documentation>
  </xs:annotation>
  <xs:element name="dlentry" type="dlentry.class"/>
  <xs:complexType name="dlentry.class">
    <xs:sequence>
      <xs:group ref="dt" maxOccurs="unbounded"/>
      <xs:group ref="dd" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dlentry "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a term in a description (definition) list.</xs:documentation>
  </xs:annotation>
  <xs:element name="dt" type="dt.class"/>
  <xs:complexType name="dt.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="term.cnt"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dt "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains the description of a term in a description/definition
list.</xs:documentation>
  </xs:annotation>
  <xs:element name="dd" type="dd.class"/>
  <xs:complexType name="dd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="defn.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/dd "/>
  </xs:complexType>

  <!-- Base form: Figure -->
  <xs:annotation>
    <xs:documentation>This block element contains images or other displays or objects along
with an optional title and description.</xs:documentation>
  </xs:annotation>
  <xs:element name="fig" type="fig.class"/>
  <xs:complexType name="fig.class">
    <xs:sequence>
      <xs:group ref="title" minOccurs="0"/>
      <xs:group ref="desc" minOccurs="0"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="figgroup"/>
        <xs:group ref="fig.cnt"/>
      </xs:choice>
    </xs:sequence>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/fig "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>
           More Information to be added
        </xs:documentation>
  </xs:annotation>
  <xs:element name="figgroup" type="figgroup.class"/>
  <xs:complexType name="figgroup.class">
    <xs:sequence>
      <xs:group ref="title" minOccurs="0"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="figgroup"/>
        <xs:group ref="xref"/>
        <xs:group ref="fn"/>
        <xs:group ref="ph"/>
        <xs:group ref="keyword"/>
      </xs:choice>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/figgroup "/>
  </xs:complexType>

  <!-- Base form: Preformatted Text -->
  <xs:annotation>
    <xs:documentation>A &lt;pre> is a block element indicating text that has been formatted
for the screen and is rendered using a fixed-width font.  All whitespace,
including multiple spaces, tabs, carriage returns and line feeds, is interpreted
literally and retained in the display. Use this element for computer listings
and program content.</xs:documentation>
  </xs:annotation>
  <xs:element name="pre" type="pre.class"/>
  <xs:complexType name="pre.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="pre.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attribute ref="xml:space"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/pre "/>
  </xs:complexType>

  <!-- Base form: Lines Respecting Text -->
  <xs:annotation>
    <xs:documentation>Lines are like paragraphs (&lt;p>), except that line endings are
significant and are preserved.</xs:documentation>
  </xs:annotation>
  <xs:element name="lines" type="lines.class"/>
  <xs:complexType name="lines.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="pre.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attribute ref="xml:space"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/lines "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty element specifies a non-text object such as an image,
vector graphic, or video clip. This element includes an alternate text description
&lt;alt> that can be read as an alternative to viewing the object itself.
Linking to a multi-media object is done by referencing the ID of the containing
element, for example, &lt;fig>.</xs:documentation>
  </xs:annotation>
  <xs:element name="image" type="image.class"/>
  <xs:complexType name="image.class">
    <xs:choice>
      <xs:group ref="alt" minOccurs="0"/>
    </xs:choice>
    <xs:attribute name="href" type="xs:string" use="required"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="alt" type="xs:string"/>
    <xs:attribute name="longdescref" type="xs:string"/>
    <xs:attribute name="height" type="xs:NMTOKEN"/>
    <xs:attribute name="width" type="xs:NMTOKEN"/>
    <xs:attribute name="align" type="image-align-att.class"/>
    <xs:attribute name="placement" type="image-placement-att.class" default="inline"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/image "/>
  </xs:complexType>

  <xs:simpleType name="image-placement-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="inline"/>
      <xs:enumeration value="break"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="image-align-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="left"/>
      <xs:enumeration value="center"/>
      <xs:enumeration value="right"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:annotation>
    <xs:documentation>
           describe new alt element here
        </xs:documentation>
  </xs:annotation>
  <xs:element name="alt" type="alt.class"/>
  <xs:complexType name="alt.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/alt "/>
  </xs:complexType>

<xs:annotation>
    <xs:documentation>This element corresponds to the HTML &lt;object> element.  &lt;object>
allows authors to include (embed) images, applets, plug-ins, ActiveX controls,
video clips, and so on in a topic for rendering after transformation to HTML.
(You can also use the &lt;image> element for graphics.) The &lt;object>
element can contain attributes, a description, and parameters</xs:documentation>
  </xs:annotation>
  <xs:element name="object" type="object.class"/>
  <xs:complexType name="object.class">
    <xs:sequence>
      <xs:group ref="desc" minOccurs="0"/>
      <xs:group ref="param" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="declare" type="xs:string" fixed="declare"/>
    <xs:attribute name="classid" type="xs:string"/>
    <xs:attribute name="codebase" type="xs:string"/>
    <xs:attribute name="data" type="xs:string"/>
    <xs:attribute name="type" type="xs:string"/>
    <xs:attribute name="codetype" type="xs:string"/>
    <xs:attribute name="archive" type="xs:string"/>
    <xs:attribute name="standby" type="xs:string"/>
    <xs:attribute name="height" type="xs:NMTOKEN"/>
    <xs:attribute name="width" type="xs:NMTOKEN"/>
    <xs:attribute name="usemap" type="xs:string"/>
    <xs:attribute name="name" type="xs:string"/>
    <xs:attribute name="tabindex" type="xs:string"/>
    <xs:attribute name="longdescref" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/object "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty element specifies a set of values that may be required
by an &lt;object> at run-time. Any number of &lt;param> elements may appear
in the content of an &lt;object> in any order, but must be placed at the start
of the content of the enclosing &lt;object>. This element is comparable to
the XHMTL &lt;param> element. More information about &lt;param> can be found
at </xs:documentation>
  </xs:annotation>
  <xs:element name="param" type="param.class"/>
  <xs:complexType name="param.class">
    <xs:attribute name="id" type="xs:ID"/>
    <xs:attribute name="name" type="xs:string" use="required"/>
    <xs:attribute name="value" type="xs:string"/>
    <xs:attribute name="valuetype" type="valuetype-att.class"/>
    <xs:attribute name="type" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/param "/>
  </xs:complexType>

  <!-- Base form: Simple Table -->
  <xs:annotation>
    <xs:documentation>This is a container element for a simple table used to present
information in unspanned columns and rows, when a CALS &lt;table> is too complex.
A simple table can be labeled either across the top or down a column, or both.</xs:documentation>
  </xs:annotation>
  <xs:element name="simpletable" type="simpletable.class"/>
  <xs:complexType name="simpletable.class">
    <xs:sequence>
      <xs:group ref="sthead" minOccurs="0"/>
      <xs:group ref="strow" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="relcolwidth" type="xs:string"/>
    <xs:attribute name="keycol" type="xs:NMTOKEN" />
    <xs:attribute name="refcols" type="xs:NMTOKENS"/>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/simpletable "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element indicates the top row of a semantic table and contains
the column heads.</xs:documentation>
  </xs:annotation>
  <xs:element name="sthead" type="sthead.class"/>
  <xs:complexType name="sthead.class">
    <xs:choice>
      <xs:group ref="stentry" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/sthead "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element specifies a row in a semantic table, like &lt;row> in
&lt;table>.</xs:documentation>
  </xs:annotation>
  <xs:element name="strow" type="strow.class"/>
  <xs:complexType name="strow.class">
    <xs:choice>
      <xs:group ref="stentry" minOccurs="0" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/strow "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element in a simple table represents a table cell, like &lt;entry>
in a &lt;table>.</xs:documentation>
  </xs:annotation>
  <xs:element name="stentry" type="stentry.class"/>
  <xs:complexType name="stentry.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry "/>
  </xs:complexType>

  <!-- Base form: Required Cleanup Block -->
  <!-- ===============================
        CLEAN UP "ANY" CONTENT MODEL
    ================================ -->
  <xs:annotation>
    <xs:documentation>This element is specifically for containing discussions that are
needed during the document review process. A reviewer can open an issue identifying
himself with the reviewer attribute. Discussion of a review comment can be
added using the &lt;discussion> element, again with author identified using
the reviewer attribute on &lt;discussion>. Tracking and disposition of review
comments can be handled with the &lt;review-comment> attributes.

It is recommended that output stylesheets (other than those specifically for
review purposes) strip out remaining &lt;draft-comment> elements prior to
publication and create a log file containing them.       </xs:documentation>
  </xs:annotation>
  <xs:element name="draft-comment" type="draft-comment.class"/>
  <xs:complexType name="draft-comment.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="basic.phandblock"/>
    </xs:choice>
    <xs:attribute name="disposition" type="disposition-att.class"/>
    <xs:attribute name="author" type="xs:string"/>
    <xs:attribute name="time" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/draft-comment "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element is a place-holder element for migrated elements that
cannot be appropriately tagged without writer intervention.

It is recommended
that output stylesheets other than those for review purposes strip out remaining
&lt;required-cleanup> elements prior to publication and create a log file
containing them.
</xs:documentation>
  </xs:annotation>
  <xs:element name="required-cleanup" type="required-cleanup.class"/>
  <xs:complexType name="required-cleanup.class" mixed="true">
    <xs:choice>
      <xs:any processContents="skip" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="remap" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/required-cleanup "/>
  </xs:complexType>

  <!-- Base form: Footnote -->
  <xs:annotation>
    <xs:documentation>A footnote is used for indicating a source or including text that
is not appropriate for inclusion in-line.  This element generates a number
by default or a character according to the callout attribute value. To refer
to the same footnote again, use a &lt;link> with the attribute type set to</xs:documentation>
  </xs:annotation>
  <xs:element name="fn" type="fn.class"/>
  <xs:complexType name="fn.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="fn.cnt"/>
    </xs:choice>
    <xs:attribute name="callout" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/fn "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>A reference to an abstract index entry in a lookup table used by
the indexing process. The current index location will be added to the abstract
index entry on output. </xs:documentation>
  </xs:annotation>
  <xs:element name="indextermref" type="indextermref.class"/>
  <xs:complexType name="indextermref.class">
    <xs:attribute name="keyref" type="xs:NMTOKEN" use="required"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/indextermref "/>
  </xs:complexType>

  <!-- Base form: Citation (from a bibliographic source) -->
  <xs:annotation>
    <xs:documentation>Use this element to refer to another document, book, or website that
is not part of your collection of topics. The citation can reference the document
directly (through an internal or external URL) or indirectly (through a reference
to a bibliographic entry in the same or another topic).</xs:documentation>
  </xs:annotation>
  <xs:element name="cite" type="cite.class"/>
  <xs:complexType name="cite.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="xrefph.cnt"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/cite "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>A cross-reference is a link to another topic, a significant figure
or table in another topic, or an external web site.</xs:documentation>
  </xs:annotation>
  <xs:element name="xref" type="xref.class"/>
  <xs:complexType name="xref.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="xreftext.cnt"/>
    </xs:choice>
    <xs:attribute name="href" type="xs:string"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="type" type="xs:string"/>
    <xs:attribute name="format" type="xs:string"/>
    <xs:attribute name="scope" type="scope-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/xref "/>
  </xs:complexType>

  <xs:simpleType name="xref-type-att.class">
    <xs:union memberTypes="xref-type-attlist.class topicreftypes-att.class"/>
  </xs:simpleType>
  <xs:simpleType name="scope-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="local"/>
      <xs:enumeration value="peer"/>
      <xs:enumeration value="external"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="valuetype-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="data"/>
      <xs:enumeration value="ref"/>
      <xs:enumeration value="object"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:attribute name="class" type="xs:string"/>
</xs:schema>
