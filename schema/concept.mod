<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: concept.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fix: make body attributes equivalent across topics
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  
  <!--Embed concept to get specific elements -->
  <xs:element name="concept" type="concept.class" />
  <xs:element name="conbody" type="conbody.class" />

  <xs:group name="concept-info-types">
    <xs:choice>
      <xs:group ref="concept"/>
    </xs:choice>
  </xs:group>

  <!-- Base type: topic.class -->
  <xs:complexType name="concept.class">
        <xs:sequence>
          <xs:group ref="title"/>
          <xs:group ref="titlealts" minOccurs="0"/>
          <xs:group ref="shortdesc" minOccurs="0"/>
          <xs:group ref="prolog" minOccurs="0"/>
          <xs:group ref="conbody"/>
          <xs:group ref="related-links" minOccurs="0"/>
          <xs:group ref="info-types" minOccurs="0" maxOccurs="unbounded"/>
        </xs:sequence>
         <xs:attribute name="id" type="xs:ID" use="required"/>
        <xs:attribute name="conref" type="xs:string"/>
        <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.3"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="xml:lang"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/topic concept/concept"/>
  </xs:complexType>

  <!-- Base type: body.class -->
  <xs:complexType name="conbody.class">
        <xs:sequence>
          <xs:group ref="body.cnt" minOccurs="0" maxOccurs="unbounded"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="section"/>
            <xs:group ref="example"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="id-atts"/>
        <xs:attribute name="translate" type="yesno-att.class"/>
        <xs:attribute ref="xml:lang"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/body concept/conbody"/>
  </xs:complexType>
</xs:schema>
