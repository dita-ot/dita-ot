<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: task.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fix: make body attributes equivalent across topics
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="xml.xsd"/>
  <!-- Base type: univ-atts -->
  <xs:attributeGroup name="univ-atts-importance-task">
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="platform" type="xs:string"/>
    <xs:attribute name="product" type="xs:string"/>
    <xs:attribute name="audience" type="xs:string"/>
    <xs:attribute name="otherprops" type="xs:string"/>
    <xs:attribute name="rev" type="xs:string"/>
    <xs:attribute name="importance" type="importance-att-nodefault-task.class"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
  </xs:attributeGroup>
  <!-- Base type: importance-atts.class -->
  <xs:simpleType name="importance-att-nodefault-task.class">
    <xs:restriction base="importance-atts.class">
      <xs:enumeration value="optional"/>
      <xs:enumeration value="required"/>
    </xs:restriction>
  </xs:simpleType>
  <!-- Element declarations for task.mod -->
  <xs:element name="task" type="task.class"/>
  <xs:element name="taskbody" type="taskbody.class"/>
  <xs:element name="prereq" type="prereq.class"/>
  <xs:element name="context" type="context.class"/>
  <xs:element name="result" type="result.class"/>
  <xs:element name="postreq" type="postreq.class"/>
  <xs:element name="choice" type="choice.class"/>
  <xs:element name="step" type="step.class"/>
  <xs:element name="substep" type="substep.class"/>
  <xs:element name="steps-unordered" type="steps-unordered.class"/>
  <xs:element name="choices" type="choices.class"/>
  <xs:element name="steps" type="steps.class"/>
  <xs:element name="substeps" type="substeps.class"/>
  <xs:element name="cmd" type="cmd.class"/>
  <xs:element name="info" type="info.class"/>
  <xs:element name="tutorialinfo" type="tutorialinfo.class"/>
  <xs:element name="stepxmp" type="stepxmp.class"/>
  <xs:element name="stepresult" type="stepresult.class"/>
  <xs:element name="choicetable" type="choicetable.class"/>
  <xs:element name="chhead" type="chhead.class"/>
  <xs:element name="chrow" type="chrow.class"/>
  <xs:element name="choptionhd" type="choptionhd.class"/>
  <xs:element name="chdeschd" type="chdeschd.class"/>
  <xs:element name="choption" type="choption.class"/>
  <xs:element name="chdesc" type="chdesc.class"/>

  <!-- Element declarations for task.mod -->
  <xs:group name="task-info-types">
    <xs:choice>
      <xs:group ref="task"/>
    </xs:choice>
  </xs:group>
  <!-- Base type: topic.class -->
  <xs:complexType name="task.class">
    <xs:sequence>
      <xs:group ref="title"/>
      <xs:group ref="titlealts" minOccurs="0"/>
      <xs:group ref="shortdesc" minOccurs="0"/>
      <xs:group ref="prolog" minOccurs="0"/>
      <xs:group ref="taskbody"/>
      <xs:group ref="related-links" minOccurs="0"/>
      <xs:group ref="info-types" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="id" type="xs:ID" use="required"/>
    <xs:attribute name="conref" type="xs:string"/>
    <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.3"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/topic task/task "/>
  </xs:complexType>
  <!-- Base type: body.class -->
  <xs:complexType name="taskbody.class">
    <xs:sequence>
      <xs:group ref="prereq" minOccurs="0"/>
      <xs:group ref="context" minOccurs="0"/>
      <xs:choice minOccurs="0" maxOccurs="1">
        <xs:group ref="steps" minOccurs="0" maxOccurs="unbounded"/>
        <xs:group ref="steps-unordered" minOccurs="0" maxOccurs="unbounded"/>
      </xs:choice>
      <xs:group ref="result" minOccurs="0"/>
      <xs:group ref="example" minOccurs="0"/>
      <xs:group ref="postreq" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attribute name="translate" type="yesno-att.class"/>
    <xs:attribute ref="xml:lang"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/body  task/taskbody "/>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="prereq.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.notitle.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section  task/prereq "/>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="context.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.notitle.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section  task/context "/>
  </xs:complexType>
  <!-- Base type: ol.class -->
  <xs:complexType name="steps.class">
    <xs:choice>
      <xs:group ref="step" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ol  task/steps "/>
  </xs:complexType>
  <!-- Base type: ul.class -->
  <xs:complexType name="steps-unordered.class">
    <xs:choice minOccurs="1" maxOccurs="unbounded">
      <xs:group ref="step" />
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ul     task/steps-unordered "/>
  </xs:complexType>
  <!-- Base type: li.class -->
  <xs:complexType name="step.class">
    <xs:sequence>
      <xs:group ref="cmd"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="info"/>
        <xs:group ref="substeps"/>
        <xs:group ref="tutorialinfo"/>
        <xs:group ref="stepxmp"/>
        <xs:group ref="choicetable"/>
        <xs:group ref="choices"/>
      </xs:choice>
      <xs:group ref="stepresult" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attributeGroup ref="univ-atts-importance-task"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/li     task/step "/>
  </xs:complexType>
  <!-- Base type: ph.class -->
  <xs:complexType name="cmd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="ph.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ph     task/cmd "/>
  </xs:complexType>
  <!-- Base type: itemgroup.class -->
  <xs:complexType name="info.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="itemgroup.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/itemgroup     task/info "/>
  </xs:complexType>
  <!-- Base type: ol.class -->
  <xs:complexType name="substeps.class">
    <xs:choice minOccurs="0">
      <xs:group ref="substep" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts-importance-task"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ol     task/substeps "/>
  </xs:complexType>
  <!-- Base type: li.class -->
  <xs:complexType name="substep.class">
    <xs:sequence>
      <xs:group ref="cmd"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="info" />
        <xs:group ref="tutorialinfo" />
        <xs:group ref="stepxmp" />
      </xs:choice>
      <xs:group ref="stepresult"  minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/li     task/substep "/>
  </xs:complexType>
  <!-- Base type: itemgroup.class -->
  <xs:complexType name="tutorialinfo.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="itemgroup.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/itemgroup task/tutorialinfo "/>
  </xs:complexType>
  <!-- Base type: itemgroup.class -->
  <xs:complexType name="stepxmp.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="itemgroup.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/itemgroup     task/tutorialinfo "/>
  </xs:complexType>
  <!-- Base type: ul.class -->
  <xs:complexType name="choices.class">
    <xs:choice>
      <xs:group ref="choice" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/ul     task/choices "/>
  </xs:complexType>
  <xs:complexType name="choice.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="basic.ph"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/li     task/choice "/>
  </xs:complexType>
  <!-- Base type: itemgroup.class -->
  <xs:complexType name="stepresult.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="itemgroup.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/itemgroup task/stepresult "/>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="result.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.notitle.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section     task/result "/>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="postreq.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="section.notitle.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/section task/postreq "/>
  </xs:complexType>
  <!-- Base type: simpletable.class -->
  <xs:complexType name="choicetable.class">
    <xs:sequence>
      <xs:group ref="chhead" minOccurs="0"/>
      <xs:group ref="chrow" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attribute name="relcolwidth" type="xs:string"/>
    <xs:attribute name="keycol" type="xs:NMTOKEN"/>
    <xs:attribute name="refcols" type="xs:NMTOKENS"/>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="spectitle" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/simpletable task/choicetable "/>
  </xs:complexType>
  <!-- Base type: sthead.class -->
  <xs:complexType name="chhead.class">
    <xs:sequence>
      <xs:group ref="choptionhd"/>
      <xs:group ref="chdeschd"/>
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/sthead task/chhead "/>
  </xs:complexType>
  <xs:complexType name="choptionhd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry task/choptionhd "/>
  </xs:complexType>
  <xs:complexType name="chdeschd.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry task/chdeschd "/>
  </xs:complexType>
  <xs:complexType name="chrow.class">
    <xs:sequence>
      <xs:group ref="choption" />
      <xs:group ref="chdesc" />
    </xs:sequence>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/strow "/>
  </xs:complexType>
  <xs:complexType name="choption.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry task/choption "/>
  </xs:complexType>
  <xs:complexType name="chdesc.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attribute name="specentry" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/stentry task/chdesc "/>
  </xs:complexType>
</xs:schema>
