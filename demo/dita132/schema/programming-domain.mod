<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: programming-domain.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  
  <xs:group name="pr-d-keyword">
    <xs:choice>
        <xs:element ref="option"/>
        <xs:element ref="parmname"/>
        <xs:element ref="apiname"/>
        <xs:element ref="kwd"/>
    </xs:choice >
  </xs:group >
  
  <xs:group name="pr-d-ph">
    <xs:choice>
      <xs:element ref="codeph" />
      <xs:element ref="var" />
      <xs:element ref="synph" />
      <xs:element ref="oper" />
      <xs:element ref="delim" />
      <xs:element ref="sep" />
      <xs:element ref="repsep" />
    </xs:choice>
  </xs:group>
  
  <xs:group name="pr-d-pre">
    <xs:choice>
        <xs:element ref="codeblock"/>
    </xs:choice >
  </xs:group >
  
  <xs:group name="pr-d-figgroup">
    <xs:choice>
        <xs:element ref="fragment"/>
        <xs:element ref="groupcomp"/>
        <xs:element ref="groupchoice"/>
        <xs:element ref="groupseq"/>
        <xs:element ref="synblk"/>
    </xs:choice >
  </xs:group >
  
  <xs:group name="pr-d-fn">
    <xs:choice>
        <xs:element ref="synnote"/>
    </xs:choice >
  </xs:group >
  
  <xs:group name="pr-d-xref">
    <xs:choice>
        <xs:element ref="fragref"/>
        <xs:element ref="synnoteref"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="pr-d-dl">
    <xs:choice>
        <xs:element ref="parml"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="pr-d-dlentry">
    <xs:choice>
        <xs:element ref="plentry"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="pr-d-dt">
    <xs:choice>
        <xs:element ref="pt"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="pr-d-dd">
    <xs:choice>
        <xs:element ref="pd"/>
    </xs:choice >
  </xs:group >
  
   <xs:group name="pr-d-fig">
    <xs:choice>
        <xs:element ref="syntaxdiagram"/>
    </xs:choice >
  </xs:group >

  <xs:attributeGroup name="univ-atts-no-importance">
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="platform" type="xs:string"/>
    <xs:attribute name="product" type="xs:string"/>
    <xs:attribute name="audience" type="xs:string"/>
    <xs:attribute name="otherprops" type="xs:string"/>
    <xs:attribute name="rev" type="xs:string"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
  </xs:attributeGroup>

  <xs:attributeGroup name="univ-atts-importance-nodefault">
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="platform" type="xs:string"/>
    <xs:attribute name="product" type="xs:string"/>
    <xs:attribute name="audience" type="xs:string"/>
    <xs:attribute name="otherprops" type="xs:string"/>
    <xs:attribute name="rev" type="xs:string"/>
    <xs:attribute name="importance" type="importance-att-nodefault.class"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
  </xs:attributeGroup>

  <xs:simpleType name="importance-att-progdom.class">
    <xs:restriction base="importance-atts.class">
      <xs:enumeration value="optional"/>
      <xs:enumeration value="required"/>
      <xs:enumeration value="default"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="importance-att-nodefault.class">
    <xs:restriction base="importance-atts.class">
      <xs:enumeration value="optional"/>
      <xs:enumeration value="required"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:element name="codeph" type="codeph.class"/>
  <xs:complexType name="codeph.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="basic.ph.notm" />
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/codeph "/>
  </xs:complexType>

  <xs:element name="codeblock" type="codeblock.class" />
  <xs:complexType name="codeblock.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="pre.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="xml:space"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/pre pr-d/codeblock "/>
  </xs:complexType>

  <xs:element name="option" type="option.class" />
  <xs:complexType name="option.class">
    <xs:simpleContent>
       <xs:extension base="xs:string">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword pr-d/option "/>
       </xs:extension>
      </xs:simpleContent>
  </xs:complexType>

  <xs:element name="var" type="var.class"/>
  <xs:complexType name="var.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/var "/>
  </xs:complexType>

  <xs:element name="parmname" type="parmname.class" />
  <xs:complexType name="parmname.class">
  <xs:simpleContent>
       <xs:extension base="xs:string">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword  pr-d/parmname "/>
        </xs:extension>
        </xs:simpleContent>
  </xs:complexType>

  <xs:element name="synph" type="synph.class"/>
  <xs:complexType name="synph.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <!-- ph -->
          <xs:element ref="codeph"/>
          <xs:element ref="var"/>
          <xs:element ref="oper"/>
          <xs:element ref="delim"/>
          <xs:element ref="sep"/>
          <xs:element ref="synph"/>
          <!-- keyword -->
          <xs:element ref="option"/>
          <xs:element ref="parmname"/>
          <xs:element ref="kwd"/>
          <xs:element ref="term"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/synph "/>
  </xs:complexType>

  <xs:element name="oper" type="oper.class" />
  <xs:complexType name="oper.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/oper "/>
  </xs:complexType>

  <xs:element name="delim" type="delim.class"/>
  <xs:complexType name="delim.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-nodefault.class"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/delim "/>
  </xs:complexType>

  <xs:element name="sep" type="sep.class"/>
  <xs:complexType name="sep.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-nodefault.class"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/sep "/>
  </xs:complexType>

  <xs:element name="apiname" type="apiname.class" />
  <xs:complexType name="apiname.class">
     <xs:simpleContent>
       <xs:extension base="xs:string">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/keyword pr-d/apiname "/>
        </xs:extension>
    </xs:simpleContent>
  </xs:complexType>

  <xs:element name="parml" type="parml.class" />
  <xs:complexType name="parml.class">
        <xs:sequence>
          <xs:element ref="plentry" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute name="compact" type="yesno-att.class"/>
        <xs:attribute ref="class" default="+ topic/dl pr-d/parml "/>
  </xs:complexType>

  <xs:element name="plentry" type="plentry.class"/>
  <xs:complexType name="plentry.class">
        <xs:sequence>
          <xs:element ref="pt" minOccurs="0"/>
          <xs:element ref="pd" minOccurs="0"/>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/dlentry pr-d/plentry "/>
  </xs:complexType>

  <xs:element name="pt" type="pt.class"/>
  <xs:complexType name="pt.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="term.cnt"/>
        </xs:choice>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/dt pr-d/pt "/>
  </xs:complexType>

  <xs:element name="pd" type="pd.class"/>
  <xs:complexType name="pd.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="defn.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/dd pr-d/pd "/>
  </xs:complexType>

  <!-- Base form: Syntax Diagram -->
  <xs:element name="syntaxdiagram" type="syntaxdiagram.class"/>
  <xs:complexType name="syntaxdiagram.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0" maxOccurs="1"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
            <xs:element ref="fragment"/>
            <xs:element ref="synblk"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attribute ref="class" default="+ topic/fig pr-d/syntaxdiagram "/>
  </xs:complexType>

  <xs:group name="syntaxdiagram.grp">
    <xs:choice>
      <xs:element ref="groupseq"/>
      <xs:element ref="groupchoice"/>
      <xs:element ref="groupcomp"/>
      <xs:element ref="fragref"/>
      <xs:element ref="synnote"/>
      <xs:element ref="synnoteref"/>
    </xs:choice>
  </xs:group>

  <xs:group name="syntaxdiagramprog.grp">
    <xs:choice>
      <xs:element ref="kwd"/>
      <xs:element ref="var"/>
      <xs:element ref="delim"/>
      <xs:element ref="oper"/>
      <xs:element ref="sep"/>
    </xs:choice>
  </xs:group>

  <xs:element name="synblk" type="synblk.class"/>
  <xs:complexType name="synblk.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
            <xs:element ref="fragment"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/figgroup pr-d/synblk "/>
  </xs:complexType>

  <xs:element name="groupseq" type="groupseq.class"/>
  <xs:complexType name="groupseq.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
            <xs:group ref="syntaxdiagramprog.grp"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/figgroup pr-d/groupseq "/>
  </xs:complexType>

  <xs:element name="groupchoice" type="groupchoice.class" />
  <xs:complexType name="groupchoice.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
            <xs:group ref="syntaxdiagramprog.grp"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/figgroup pr-d/groupchoice "/>
  </xs:complexType>

  <xs:element name="groupcomp" type="groupcomp.class"/>
  <xs:complexType name="groupcomp.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
            <xs:group ref="syntaxdiagramprog.grp"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/figgroup pr-d/groupcomp "/>
  </xs:complexType>

  <xs:element name="fragment" type="fragment.class"/>
  <xs:complexType name="fragment.class">
        <xs:sequence>
          <xs:element ref="title" minOccurs="0"/>
          <xs:choice minOccurs="0" maxOccurs="unbounded">
            <xs:group ref="syntaxdiagram.grp"/>
          </xs:choice>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/figgroup pr-d/fragment "/>
  </xs:complexType>

  <xs:element name="fragref" type="fragref.class" />
  <xs:complexType name="fragref.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="xrefph.cnt"/>
        </xs:choice>
        <xs:attribute name="href" type="xs:string" use="required"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/xref pr-d/fragref "/>
  </xs:complexType>

  <xs:element name="synnote" type="synnote.class"/>
  <xs:complexType name="synnote.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attribute name="callout" type="xs:string"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/fn pr-d/synnote "/>
  </xs:complexType>

  <xs:element name="synnoteref" type="synnoteref.class"/>
  <xs:complexType name="synnoteref.class">
        <xs:attribute name="href" type="xs:string" use="required"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attribute ref="class" default="+ topic/xref pr-d/synnoteref "/>
  </xs:complexType>

  <xs:element name="repsep" type="repsep.class" />
  <xs:complexType name="repsep.class" mixed="true">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="words.cnt"/>
        </xs:choice>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/ph pr-d/repsep "/>
  </xs:complexType>

  <xs:element name="kwd" type="kwd.class" />
  <xs:complexType name="kwd.class">
  <xs:simpleContent>
       <xs:extension base="xs:string">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attributeGroup ref="univ-atts-no-importance"/>
        <xs:attribute name="importance" type="importance-att-progdom.class"/>
        <xs:attribute ref="class" default="+ topic/keyword pr-d/kwd "/>
        </xs:extension>
        </xs:simpleContent>
  </xs:complexType>
</xs:schema>
