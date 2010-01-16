<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: map.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">

  <!-- ======= MetaData elements, plus keyword and indexterm ======= -->
  <xs:include schemaLocation="meta_xml.mod"/>
  <xs:include schemaLocation="meta_xml.grp"/>

  <xs:include schemaLocation="map.grp"/>

  <!-- ======= IMPORT - XML attributes and namespace ======= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>





  <!-- new <!ENTITY % words.cnt               "#PCDATA | %keyword; | %term;"> -->
  <xs:group name="words.cnt">
    <xs:choice>
      <xs:group ref="keyword"/>
<!-- DRD:
      <xs:group ref="term"/>
-->
    </xs:choice>
  </xs:group>

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

  <!--[20021217  EAS:  Note: when @importance is not attributed (because it is #IMPLIED), default process  as if "normal"]-->
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

  <!--[20021107-01 DRD: Add status attribute to select-atts: (new|changed|deleted|unchanged) #implied]-->
  <xs:simpleType name="status-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="new"/>
      <xs:enumeration value="changed"/>
      <xs:enumeration value="deleted"/>
      <xs:enumeration value="unchanged"/>
    </xs:restriction>
  </xs:simpleType>

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

  <!-- STRUCTURAL MEMBERS ======================================================== -->
  <!-- things that can be nested under topic after body - redefined when specializing -->
  <xs:simpleType name="topicreftypes.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="topic"/>
      <xs:enumeration value="concept"/>
      <xs:enumeration value="task"/>
      <xs:enumeration value="reference"/>
      <xs:enumeration value="external"/>
      <xs:enumeration value="local"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:attributeGroup name="topicref-atts">
     <xs:attribute name="collection-type" type="collection-type.class"/>
     <xs:attribute name="type" type="xs:string"/>
     <xs:attribute name="scope" type="scope-att.class"/>
     <xs:attribute name="locktitle" type="yesno-att.class"/>
     <xs:attribute name="format" type="xs:string"/>
     <xs:attribute name="linking" type="linkingtypes.class"/>
     <xs:attribute name="toc" type="yesno-att.class"/>
     <xs:attribute name="print" type="yesno-att.class"/>
     <xs:attribute name="search" type="yesno-att.class"/>
     <xs:attribute name="chunk" type="xs:string"/>
  </xs:attributeGroup>
  
  <xs:simpleType name="collection-type.class">
   <xs:restriction base="xs:string">
     <xs:enumeration value="choice"/>
     <xs:enumeration value="unordered"/>
     <xs:enumeration value="sequence"/>
     <xs:enumeration value="family"/>
   </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="linkingtypes.class">
   <xs:restriction base="xs:string">
     <xs:enumeration value="targetonly"/>
     <xs:enumeration value="sourceonly"/>
     <xs:enumeration value="normal"/>
     <xs:enumeration value="none"/>
   </xs:restriction>
  </xs:simpleType>
   

  <xs:attributeGroup name="topicref-atts-no-toc">
     <xs:attribute name="type" type="xs:string"/>
     <xs:attribute name="scope" type="scope-att.class"/>
     <xs:attribute name="locktitle" type="yesno-att.class"/>
     <xs:attribute name="format" type="xs:string"/>
     <xs:attribute name="toc" type="yesno-att.class" default="no"/>
     <xs:attribute name="print" type="yesno-att.class"/>
     <xs:attribute name="search" type="yesno-att.class"/>
     <xs:attribute name="chunk" type="xs:string"/>
  </xs:attributeGroup>

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

   <xs:simpleType name="view-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="internal"/>
      <xs:enumeration value="classified"/>
      <xs:enumeration value="all"/>
      <xs:enumeration value="entitled"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="scope-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="local" />
      <xs:enumeration value="peer" />
      <xs:enumeration value="external" />
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="yesno-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="yes"/>
      <xs:enumeration value="no"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:element name="map" type="map.class"/>
  <xs:complexType name="map.class" >
    <xs:sequence>
      <xs:group ref="topicmeta" minOccurs="0" />
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="navref" />
        <xs:group ref="anchor" />
        <xs:group ref="topicref" />
        <xs:group ref="reltable" />
      </xs:choice>
    </xs:sequence>
    <xs:attribute name="title" type="xs:string" />
    <xs:attribute name="id" type="xs:ID" />
    <xs:attribute name="anchorref" type="xs:string" />
    <xs:attributeGroup ref="topicref-atts" />
    <xs:attributeGroup ref="select-atts" />
    <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.3"/>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/map " />
    <xs:attribute name="domains" type="xs:string" default="(map mapgroup-d)"/>
  </xs:complexType>

  <xs:element name="navref" type="navref.class"/>
  <xs:complexType name="navref.class">
    <xs:attribute name="mapref" type="xs:string"/>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/navref " />
  </xs:complexType>

  <xs:element name="topicref" type="topicref.class"/>
  <xs:complexType name="topicref.class">
    <xs:sequence>
      <xs:group ref="topicmeta" minOccurs="0"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="navref" />
        <xs:group ref="anchor" />
        <xs:group ref="topicref" />
      </xs:choice>
    </xs:sequence>
    <xs:attribute name="navtitle" type="xs:string"/>
    <xs:attribute name="id" type="xs:ID"/>
    <xs:attribute name="href" type="xs:string"/>
    <xs:attribute name="keyref" type="xs:string"/>
    <xs:attribute name="query" type="xs:string"/>
    <xs:attribute name="conref" type="xs:string"/>
    <xs:attribute name="copy-to" type="xs:string"/>
    <xs:attributeGroup ref="topicref-atts" />
    <xs:attributeGroup ref="select-atts" />
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/topicref " />
  </xs:complexType>

  <xs:element name="reltable" type="reltable.class" />
  <xs:complexType name="reltable.class">
    <xs:sequence>
      <xs:group ref="topicmeta" minOccurs="0" />
      <xs:group ref="relheader" minOccurs="0" />
      <xs:group ref="relrow" maxOccurs="unbounded" />
    </xs:sequence>
  	<xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/reltable " />
  </xs:complexType>
  
  <xs:element name="relheader" type="relheader.class" />
  <xs:complexType name="relheader.class">
    <xs:sequence>
        <xs:group ref="relcolspec" maxOccurs="unbounded" />
    </xs:sequence>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/relheader " />
  </xs:complexType>
  
  <xs:element name="relcolspec" type="relcolspec.class" />
  <xs:complexType name="relcolspec.class">
    <xs:sequence>
        <xs:group ref="topicmeta" minOccurs="0" />
    </xs:sequence>
    <xs:attributeGroup ref="select-atts" />
    <xs:attributeGroup ref="topicref-atts" />
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/relcolspec " />
  </xs:complexType>
  
  <xs:element name="relrow" type="relrow.class" />
  <xs:complexType name="relrow.class">
    <xs:sequence>
        <xs:group ref="relcell" maxOccurs="unbounded" />
    </xs:sequence>
    <xs:attributeGroup ref="id-atts" />
    <xs:attributeGroup ref="select-atts" />
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/relrow " />
  </xs:complexType>
  
  <xs:element name="relcell" type="relcell.class" />
  <xs:complexType name="relcell.class">
    <xs:sequence>
        <xs:group ref="topicref" minOccurs="0" maxOccurs="unbounded" />
    </xs:sequence>
    <xs:attributeGroup ref="id-atts" />
    <xs:attributeGroup ref="topicref-atts" />
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/relcell " />
  </xs:complexType>
  

  <xs:element name="anchor" type="anchor.class"/>
  <xs:complexType name="anchor.class">
    <xs:attribute name="id" type="xs:ID" use="required"/>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/anchor " />
  </xs:complexType>

  <xs:element name="topicmeta" type="topicmeta.class"/>
  <xs:complexType name="topicmeta.class" >
    <xs:sequence>
      <xs:group ref="linktext" minOccurs="0"/>
      <xs:group ref="searchtitle" minOccurs="0"/>
      <xs:group ref="shortdesc" minOccurs="0"/>
      <xs:group ref="author" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="source" minOccurs="0"/>
      <xs:group ref="publisher" minOccurs="0"/>
      <xs:group ref="copyright" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="critdates" minOccurs="0"/>
      <xs:group ref="permissions" minOccurs="0"/>
      <xs:group ref="audience" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="category" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="keywords" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="prodinfo" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="othermeta" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="resourceid" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="lockmeta" type="yesno-att.class"/>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/topicmeta " />
  </xs:complexType>

  <xs:element name="linktext" type="linktext.class"/>
  <xs:complexType name="linktext.class" mixed="true">
    <xs:sequence minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:sequence>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/linktext " />
  </xs:complexType>

  <xs:element name="searchtitle" type="searchtitle.class"/>
  <xs:complexType name="searchtitle.class" mixed="true">
    <xs:sequence minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:sequence>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/searchtitle " />
  </xs:complexType >

  <xs:element name="shortdesc" type="shortdesc.class"/>
  <xs:complexType name="shortdesc.class" mixed="true">
    <xs:sequence minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:sequence>
    <xs:attributeGroup ref="global-atts" />
    <xs:attribute ref="class" default="- map/shortdesc " />
  </xs:complexType >

<!-- DRD:
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
-->

  <xs:attribute name="class" type="xs:string" />
</xs:schema>