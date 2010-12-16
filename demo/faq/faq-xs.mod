<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="../../../xml/schemas/ditaV1.2/xml.xsd"/>
  
  <!-- Element declarations for faq.mod -->
  <xs:element name="faq" type="faq.class" substitutionGroup="topic"/>
  <xs:element name="faqbody" abstract="true" substitutionGroup="body"/>
  <xs:element name="faqgroup" abstract="true" substitutionGroup="section"/>
  <xs:element name="faqlist" abstract="true" substitutionGroup="simpletable"/>
  <xs:element name="faqitem" abstract="true" substitutionGroup="strow"/>
  <xs:element name="faqquest" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="faqans" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="faqprop" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="name" abstract="true" substitutionGroup="ph"/>
  <xs:element name="ownerEmail" abstract="true" substitutionGroup="xref"/>
  <!-- Element declarations for faq.mod -->
  <xs:group name="faq-info-types">
    <xs:choice>
      <xs:element ref="faq"/>
    </xs:choice>
  </xs:group>
  <!-- Base type: topic.class -->
  <xs:complexType name="faq.class">
    <xs:complexContent>
      <xs:restriction base="topic.class">
        <xs:sequence>
          <xs:element ref="title"/>
          <xs:element ref="titlealts" minOccurs="0"/>
          <xs:element ref="shortdesc" minOccurs="0"/>
          <xs:element ref="prolog" minOccurs="0"/>
          <xs:element name="faqbody" type="faqbody.class"/>
          <xs:element ref="related-links" minOccurs="0"/>
          <xs:group ref="faq-info-types" minOccurs="0" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attribute name="id" type="xs:ID" use="required"/>
        <xs:attribute name="conref" type="xs:string"/>
        <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.2"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="xml:lang"/>
        <xs:attribute ref="class" default="- topic/topic faq/faq "/>
        <xs:attribute name="domains" type="xs:string" default="(topic ui-d) (topic hi-d) (topic sw-d) (topic pr-d)"/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: body.class -->
  <xs:complexType name="faqbody.class">
    <xs:complexContent>
      <xs:restriction base="body.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element name="faqgroup" type="faqgroup.class" minOccurs="0"/>
          <xs:element name="faqlist" type="faqlist.class"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/body faq/faqbody "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="faqgroup.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="section.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="title"/>
          <xs:element name="faqlist" type="faqlist.class"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/section faq/faqgroup "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: simpletable.class (%faqquest;), (%faqans;), (%faqprop;)?-->
  <xs:complexType name="faqlist.class">
    <xs:complexContent>
      <xs:restriction base="simpletable.class">
        <xs:sequence>
          <xs:element name="faqitem" type="faqitem.class" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attribute name="relcolwidth" type="xs:string"/>
        <xs:attribute name="keycol" type="xs:NMTOKEN"/>
        <xs:attribute name="refcols" type="xs:NMTOKENS"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/simpletable faq/faqlist "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="faqitem.class">
    <xs:complexContent>
      <xs:restriction base="strow.class">
        <xs:sequence>
          <xs:element name="faqquest" type="faqquest.class"/>
          <xs:element name="faqans" type="faqans.class"/>
          <xs:element name="faqprop" type="faqprop.class" minOccurs="0"/>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/strrow faq/faqitem "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="faqquest.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="tblcell.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="specentry" type="xs:string" default="Question"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry faq/faqquest "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="faqans.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="tblcell.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="specentry" type="xs:string" default="Answer"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry faq/faqans "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="faqprop.class">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element name="ownerEmail" type="ownerEmail.class" minOccurs="0"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="specentry" type="xs:string" default="Properties"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry faq/faqprop "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: ph.class -->
  <xs:complexType name="name.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="ph.class">
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/ph faq/name "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="ownerEmail.class">
    <xs:complexContent>
      <xs:restriction base="xref.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element name="name" type="name.class" minOccurs="0"/>
        </xs:choice>
        <xs:attribute name="href" type="xs:string"/>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="type" type="xs:string"/>
        <xs:attribute name="format" type="xs:string" default="mailto"/>
        <xs:attribute name="scope" type="scope-att.class" default="external"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/xref faq/ownerEmail "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
</xs:schema>
