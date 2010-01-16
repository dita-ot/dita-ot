<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: utilities-domain.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
<xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>

<xs:attributeGroup name="univ-atts-translate-no">
	<xs:attributeGroup ref="id-atts"/>
	<xs:attributeGroup ref="select-atts"/>
	<xs:attribute name="translate" type="yesno-att.class" default="no"/>
	<xs:attribute ref="xml:lang"/>
</xs:attributeGroup>

  <xs:group name="ut-d-fig">
    <xs:choice>
      <xs:element ref="imagemap" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="ut-d-figgroup">
    <xs:choice>
      <xs:element ref="area" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="ut-d-ph">
    <xs:choice>
      <xs:element ref="coords" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="ut-d-keyword">
    <xs:choice>
      <xs:element ref="shape" />
    </xs:choice>
  </xs:group>
  
  <!-- Base form: Imagemap ((%image;), (%area;)+) -->
  <xs:element name="imagemap" type="imagemap.class" />
  <xs:complexType name="imagemap.class">
        <xs:sequence>
          <xs:element ref="image"/>
          <xs:element ref="area" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/fig ut-d/imagemap "/>
  </xs:complexType>

  <xs:element name="area" type="area.class" />
  <xs:complexType name="area.class">
        <xs:sequence>
          <xs:element ref="shape"/>
          <xs:element ref="coords"/>
          <xs:element ref="xref"/>
        </xs:sequence>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/figgroup ut-d/area "/>
  </xs:complexType>

  <xs:element name="shape" type="shape.class"/>
  <xs:complexType name="shape.class" mixed="true">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attributeGroup ref="univ-atts-translate-no"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword ut-d/shape "/>
  </xs:complexType>

  <xs:element name="coords" type="repsep.class"/>
  <xs:complexType name="coords.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attributeGroup ref="univ-atts-translate-no"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph ut-d/coords "/>
  </xs:complexType>

</xs:schema>
