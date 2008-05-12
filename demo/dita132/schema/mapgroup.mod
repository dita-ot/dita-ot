<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: mapgroup.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">


    <xs:element name="topichead" type="topichead.class"  substitutionGroup="topicref"/>
    <xs:element name="topicgroup" type="topicgroup.class"  substitutionGroup="topicref"/>

    <xs:complexType name="topichead.class">
    <xs:complexContent>
      <xs:restriction base="topicref.class">
        <xs:sequence>
          <xs:element ref="topicmeta" minOccurs="0" />
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="navref" />
            <xs:group ref="anchor" />
            <xs:group ref="topicref" />
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="navtitle" type="xs:string" use="required"/>
        <xs:attribute name="id" type="xs:ID"/>
        <xs:attribute name="conref" type="xs:string"/>
        <xs:attributeGroup ref="topicref-atts" />
        <xs:attributeGroup ref="select-atts" />
        <xs:attributeGroup ref="global-atts" />
        <xs:attribute ref="class" default="+ map/topicref mapgroup/topichead " />
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>

  <xs:complexType name="topicgroup.class">
    <xs:complexContent>
      <xs:restriction base="topicref.class">
        <xs:sequence>
          <xs:group ref="topicmeta" minOccurs="0" />
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="navref" />
            <xs:group ref="anchor" />
            <xs:group ref="topicref" />
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="id" type="xs:ID"/>
        <xs:attribute name="conref" type="xs:string"/>
        <xs:attributeGroup ref="topicref-atts" />
        <xs:attributeGroup ref="select-atts" />
        <xs:attributeGroup ref="global-atts" />
        <xs:attribute ref="class" default="+ map/topicref mapgroup/topicgroup " />
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>

</xs:schema>