<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: ui-domain.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- Import namespace for xml:space attribute for element screen -->
 <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
 
   <xs:group name="ui-d-ph">
    <xs:choice>
      <xs:element ref="uicontrol" />
      <xs:element ref="menucascade" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="ui-d-keyword">
    <xs:choice>
      <xs:element ref="shortcut" />
      <xs:element ref="wintitle" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="ui-d-pre">
    <xs:choice>
      <xs:element ref="screen" />
    </xs:choice>
  </xs:group>

  <xs:element name="uicontrol" type="uicontrol.class"/>
  <xs:complexType name="uicontrol.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:element ref="image"/>
      <xs:element ref="shortcut"/>
      <!-- <xs:group ref="words.cnt"/> -->
      <xs:element ref="term"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attribute ref="class" default="+ topic/ph ui-d/uicontrol "/>
  </xs:complexType>

  <xs:element name="shortcut" type="shortcut.class" />
  <xs:complexType name="shortcut.class"  mixed="true">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
  <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword ui-d/shortcut "/>
  </xs:complexType>

  <xs:element name="wintitle" type="wintitle.class" />
  <xs:complexType name="wintitle.class"  mixed="true">
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attribute ref="class" default="+ topic/keyword ui-d/wintitle "/>
  </xs:complexType>

  <xs:element name="menucascade" type="menucascade.class" />
   <xs:complexType name="menucascade.class" mixed="true">
        <xs:choice>
          <xs:element ref="uicontrol" maxOccurs="unbounded"/>
        </xs:choice>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
  <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/ph ui-d/menucascade "/>
  </xs:complexType>

  <!-- 20021217 Add new element screen -->
  <xs:element name="screen" type="screen.class" />
  <xs:complexType name="screen.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph.notm"/>
          <xs:group ref="txt.incl"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
  <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attribute ref="xml:space"/>
        <xs:attribute ref="class" default="+ topic/pre ui-d/screen "/>
     </xs:complexType>
</xs:schema>
