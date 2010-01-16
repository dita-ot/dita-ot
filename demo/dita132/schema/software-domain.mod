<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: software-domain.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">

  <!-- 20021227 EAS: Use xml:space (preserve) for pre derivs: codeblock, msgblock, screen -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>

  <xs:group name="sw-d-ph">
    <xs:choice>
      <xs:element ref="msgph" />
      <xs:element ref="filepath" />
      <xs:element ref="userinput" />
      <xs:element ref="systemoutput" />
    </xs:choice>
  </xs:group>

  <xs:group name="sw-d-keyword">
    <xs:choice>
      <xs:element ref="msgnum" />
      <xs:element ref="varname" />
      <xs:element ref="cmdname" />
    </xs:choice>
  </xs:group>

  <xs:group name="sw-d-pre">
    <xs:choice>
      <xs:element ref="msgblock" />
    </xs:choice>
  </xs:group>


  <xs:element name="msgph" type="msgph.class"/>
  <xs:complexType name="msgph.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="varname"/>
          <xs:element ref="msgnum"/>
          <xs:element ref="term"/>
          <!-- <xs:group ref="words.cnt"/> -->
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph sw-d/msgph "/>
  </xs:complexType>

  <xs:element name="msgnum" type="msgnum.class"/>
   <xs:complexType name="msgnum.class"  mixed="true">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword sw-d/msgnum "/>
  </xs:complexType>

  <xs:element name="varname" type="varname.class" />
  <xs:complexType name="varname.class"  mixed="true">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword sw-d/varname "/>
  </xs:complexType>

  <xs:element name="msgblock" type="msgblock.class" />
  <xs:complexType name="msgblock.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:element ref="varname"/>
            <xs:element ref="msgnum"/>
            <xs:element ref="term"/>
            <!-- <xs:group ref="words.cnt"/> -->
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attribute ref="xml:space"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/pre sw-d/msgblock "/>
  </xs:complexType>

  <xs:element name="cmdname" type="cmdname.class" />
  <xs:complexType name="cmdname.class"  mixed="true">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword sw-d/cmdname "/>
  </xs:complexType>

  <xs:element name="filepath" type="filepath.class"/>
  <xs:complexType name="filepath.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="varname"/>
          <xs:element ref="term"/>
          <!-- <xs:group ref="words.cnt"/> -->
    </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph sw-d/filepath "/>
  </xs:complexType>

  <xs:element name="userinput" type="userinput.class"/>
  <xs:complexType name="userinput.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="varname"/>
          <xs:element ref="term"/>
          <!-- <xs:group ref="words.cnt"/> -->
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph sw-d/userinput "/>
  </xs:complexType>

  <xs:element name="systemoutput" type="systemoutput.class"/>
  <xs:complexType name="systemoutput.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">

          <xs:element ref="varname"/>
          <xs:element ref="term"/>
          <!-- <xs:group ref="words.cnt"/> -->

        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph sw-d/systemoutput "/>

  </xs:complexType>
</xs:schema>
