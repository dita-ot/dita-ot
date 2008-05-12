<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: reference.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fix: make body attributes equivalent across topics
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  <xs:element name="reference" type="reference.class"/>
  <xs:element name="refbody" type="refbody.class"/>
  <xs:element name="refsyn" type="refsyn.class"/>
  <xs:element name="properties" type="properties.class"/>
  <xs:element name="property" type="property.class"/>
  <xs:element name="propvalue" type="propvalue.class"/>
  <xs:element name="propdesc" type="propdesc.class"/>
  <xs:element name="proptype" type="proptype.class"/>
  <xs:element name="prophead" type="prophead.class"/>
  <xs:element name="propvaluehd" type="propvaluehd.class"/>
  <xs:element name="propdeschd" type="propdeschd.class"/>
  <xs:element name="proptypehd" type="proptypehd.class"/>
  <xs:group name="reference-info-types">
    <xs:choice>
      <xs:group ref="reference"/>
    </xs:choice>
  </xs:group>
  <!-- Base type: topic.class -->
  <xs:complexType name="reference.class">
    <xs:sequence>
      <xs:group ref="title"/>
      <xs:group ref="titlealts" minOccurs="0"/>
      <xs:group ref="shortdesc" minOccurs="0"/>
      <xs:group ref="prolog" minOccurs="0"/>
      <xs:group ref="refbody"/>
      <xs:group ref="related-links" minOccurs="0"/>
      <xs:group ref="info-types" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="id" type="xs:ID" use="required"/>
    <xs:attribute name="conref" type="xs:string"/>
    <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.3"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/topic reference/reference "/>
  </xs:complexType>
  <!-- Base type: body.class -->
  <xs:complexType name="refbody.class">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="refsyn"/>
      <xs:group ref="table"/>
      <xs:group ref="simpletable"/>
      <xs:group ref="properties"/>
      <xs:group ref="section"/>
      <xs:group ref="example"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/body reference/refbody "/>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="refsyn.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.cnt"/>
    </xs:choice>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section reference/refsyn "/>
  </xs:complexType>
  <!-- Base type: simpletable.class -->
  <xs:complexType name="properties.class">
    <xs:sequence>
      <xs:group ref="prophead" minOccurs="0"/>
      <xs:group ref="property" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/simpletable reference/properties "/>
  </xs:complexType>
  <!-- Base type: sthead.class -->
  <xs:complexType name="prophead.class">
    <xs:sequence>
      <xs:group ref="proptypehd" minOccurs="0"/>
      <xs:group ref="propvaluehd" minOccurs="0"/>
      <xs:group ref="propdeschd" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/sthead  reference/prophead "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="proptypehd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry reference/proptypehd "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="propvaluehd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry  reference/propvaluehd "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="propdeschd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="desc.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry  reference/propdeschd "/>
  </xs:complexType>
  <!-- Base type: strow.class -->
  <xs:complexType name="property.class">
    <xs:sequence>
      <xs:group ref="proptype" minOccurs="0"/>
      <xs:group ref="propvalue" minOccurs="0"/>
      <xs:group ref="propdesc" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/strow  reference/property "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="proptype.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry reference/proptype "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="propvalue.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry  reference/propvalue "/>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="propdesc.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="desc.cnt"/>
    </xs:choice>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry  reference/propdesc "/>
  </xs:complexType>
</xs:schema>
