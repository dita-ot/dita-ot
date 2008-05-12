<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: highlight-domain.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">

 <xs:group name="hi-d-ph">
    <xs:choice>
      <xs:element ref="sup" />
      <xs:element ref="sub" />
      <xs:element ref="tt" />
      <xs:element ref="b" />
      <xs:element ref="u" />
      <xs:element ref="i" />
    </xs:choice>
  </xs:group>
  
   <!-- Basic form: Single Effect Formatting Phrases -->
  <xs:element name="sup" type="sup.class" />
  <xs:complexType name="sup.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/sup "/>
  </xs:complexType>

  <xs:element name="sub" type="sub.class"/>
  <xs:complexType name="sub.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/sub "/>
  </xs:complexType>

  <xs:element name="tt" type="tt.class" />
  <xs:complexType name="tt.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/tt "/>
  </xs:complexType>

  <xs:element name="b" type="b.class" />
  <xs:complexType name="b.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/b "/>
  </xs:complexType>

  <xs:element name="u" type="u.class"/>
  <xs:complexType name="u.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/u "/>
  </xs:complexType>

  <xs:element name="i" type="i.class" />
  <xs:complexType name="i.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="class" default="+ topic/ph hi-d/i "/>
  </xs:complexType>

</xs:schema>
