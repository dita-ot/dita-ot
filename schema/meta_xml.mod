<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: meta_xml.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->
<!--
  Notes:
x    20040309-07 DRD: Replace keywords/@conref with a id-atts (contains both @conref and @id)
     20040309-11b DRD: Enable keyword/term use in previously unconrefed contexts (see words.cnt)
 +-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
<!-- <xs:include schemaLocation="topic.mod"/> -->
  <xs:annotation>
    <xs:documentation>This metadata element can contain the name and address of the topic's
author, or it can be left empty and point to another location where the author
is defined.</xs:documentation>
  </xs:annotation>
  <xs:element name="author" type="author.class"/>
  <xs:complexType name="author.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attribute name="href" type="xs:string"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attribute name="type" type="author-type-atts.class"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/author "/>
  </xs:complexType>

  <xs:simpleType name="author-type-atts.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="creator"/>
      <xs:enumeration value="contributor"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:annotation>
    <xs:documentation>This element contains a reference to a resource from which the present
topic is derived, either completely or in part.  The reference can be a string
or a cross-reference.</xs:documentation>
  </xs:annotation>
  <xs:element name="source" type="source.class"/>
  <xs:complexType name="source.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attribute name="href" type="xs:string" use="required"/>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/source "/>
  </xs:complexType>

  <xs:annotation>
      <xs:documentation>This metadata element contains the name of the person, company,
or organization responsible for making the resource available.</xs:documentation>
  </xs:annotation>
  <xs:element name="publisher" type="publisher.class"/>
  <xs:complexType name="publisher.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/publisher "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This is the container element for a single copyright entry. It includes
the copyright years and the copyright holder.  Multiple &lt;copyright> statements
are allowed.</xs:documentation>
  </xs:annotation>
  <xs:element name="copyright" type="copyright.class"/>
  <xs:complexType name="copyright.class">
    <xs:sequence>
      <xs:element ref="copyryear" maxOccurs="unbounded"/>
      <xs:element ref="copyrholder"/>
    </xs:sequence>
    <xs:attribute name="type" type="copyright-type-att.class"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/copyright "/>
  </xs:complexType>

  <xs:simpleType name="copyright-type-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="primary"/>
      <xs:enumeration value="secondary"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:annotation>
    <xs:documentation>This empty element contains the copyright date or dates as specified
by the year attribute.      </xs:documentation>
  </xs:annotation>
  <xs:element name="copyryear" type="copyryear.class"/>
  <xs:complexType name="copyryear.class">
    <xs:attribute name="year" type="xs:gYear" use="required"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/copyyear "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element indicates the ownership of the information contained
in the topic.</xs:documentation>
  </xs:annotation>
  <xs:element name="copyrholder" type="copyholder.class"/>
  <xs:complexType name="copyholder.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/copyholder "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains the critical dates in a document life cycle,
such as the creation, revision, and publication dates.</xs:documentation>
  </xs:annotation>
  <xs:element name="critdates" type="critdates.class"/>
  <xs:complexType name="critdates.class">
    <xs:sequence>
      <xs:element ref="created"/>
      <xs:element ref="revised" minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/critdates "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty prolog element contains tracking dates that are important
in a topic development cycle.</xs:documentation>
  </xs:annotation>
  <xs:element name="revised" type="revised.class"/>
  <xs:complexType name="revised.class">
    <xs:attribute name="modified" type="xs:string" use="required"/>
    <xs:attribute name="golive" type="xs:string"/>
    <xs:attribute name="expiry" type="xs:string"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/revised "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty element specifies the document creation date using the
date attribute.</xs:documentation>
  </xs:annotation>
  <xs:element name="created" type="created.class"/>
  <xs:complexType name="created.class">
    <xs:attribute name="date" type="xs:string" use="required"/>
    <xs:attribute name="golive" type="xs:string"/>
    <xs:attribute name="expiry" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/created "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation> More Information to be added</xs:documentation>
  </xs:annotation>
  <xs:element name="resourceid" type="resourceid.class"/>
  <xs:complexType name="resourceid.class">
    <xs:attribute name="id" type="xs:string" use="required"/>
    <xs:attribute name="appname" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/resourceid "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty metadata element indicates the intended audience for a
topic using its type attribute. Since a topic can have multiple audiences,
you can include multiple audience elements. For each audience you specify,
you can identify the high-level task they are trying to accomplish (job) and
the level of experience expected.</xs:documentation>
  </xs:annotation>
  <xs:element name="audience" type="audience.class"/>
  <xs:complexType name="audience.class">
    <xs:attribute name="type" type="audience-type-att.class"/>
    <xs:attribute name="othertype" type="xs:string"/>
    <xs:attribute name="job" type="job-att.class"/>
    <xs:attribute name="otherjob" type="xs:string"/>
    <xs:attribute name="experiencelevel" type="experiencelevel-att.class"/>
    <xs:attribute name="name" type="xs:NMTOKEN"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/audience "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element can represent any category by which a topic might
be classified for retrieval or navigation; for example, the categories could
be used to group topics in a generated navigation bar. Topics can belong to
multiple categories.</xs:documentation>
  </xs:annotation>
  <xs:element name="category" type="category.class"/>
  <xs:complexType name="category.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/category "/>
  </xs:complexType>

  <!-- Base form: Index entry  -->
  <xs:annotation>
    <xs:documentation>An index entry. You can nest entries to create multi-level indexes.</xs:documentation>
  </xs:annotation>
  <xs:element name="indexterm" type="indexterm.class"/>
  <xs:complexType name="indexterm.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
      <xs:element ref="indexterm"/>
    </xs:choice>
    <xs:attribute name="keyref" type="xs:NMTOKEN"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/indexterm "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a list of keywords, separated by commas, that
can be used by a search engine.</xs:documentation>
  </xs:annotation>
  <xs:element name="keywords" type="keywords.class"/>
  <xs:complexType name="keywords.class">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:element ref="indexterm"/>
      <xs:element ref="keyword"/>
    </xs:choice>
    <xs:attributeGroup ref="id-atts"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/keywords "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>Identifies a key word of some sort. Can be used in specialized topic
types as a base for particular kinds of keywords, which can then be processed
in particular ways (formatted differently, automatically indexed, etc.). If
the keyref attribute is used, the keyword may be turned into a hyperlink on
output.</xs:documentation>
  </xs:annotation>
  <xs:element name="keyword" type="keyword.class"/>
  <xs:complexType name="keyword.class">
    <xs:simpleContent>
      <xs:extension base="xs:string">
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/keyword "/>
      </xs:extension>
    </xs:simpleContent>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element can be used to identify properties not otherwise included
in &lt;metadata> and assign  property/pair values to those properties. The
name attribute identifies the property and the content attribute specifies
the property's value.</xs:documentation>
  </xs:annotation>
  <xs:element name="othermeta" type="othermeta.class"/>
  <xs:complexType name="othermeta.class">
    <xs:attribute name="name" type="xs:string"/>
    <xs:attribute name="content" type="xs:string"/>
    <xs:attribute name="translate-content" type="yesno-att.class"/>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/othermeta "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty prolog element can indicate any preferred controls for
access to a topic. Permissions can be used as viewing filters.  Standard DITA
processing does not use this element.</xs:documentation>
  </xs:annotation>
  <xs:element name="permissions" type="permissions.class"/>
  <xs:complexType name="permissions.class">
    <xs:attribute name="view" type="view-att.class" use="required"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/permissions "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This metadata element in the prolog contains information about the
product or products that are the subject matter of the current topic.</xs:documentation>
  </xs:annotation>
  <xs:element name="prodinfo" type="prodinfo.class"/>
  <xs:complexType name="prodinfo.class">
    <xs:sequence>
      <xs:element ref="prodname"/>
      <xs:element ref="vrmlist"/>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:group ref="prodinfo.cnt"/>
      </xs:choice>
    </xs:sequence>
    <xs:attributeGroup ref="select-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/prodinfo "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element indicates the manufacturer or brand associated with
the current product.</xs:documentation>
  </xs:annotation>
  <xs:element name="brand" type="brand.class"/>
  <xs:complexType name="brand.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/brand "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This metadata element contains information about the product series
that the topic supports.</xs:documentation>
  </xs:annotation>
  <xs:element name="series" type="series.class"/>
  <xs:complexType name="series.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/series "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This  metadata element contains a description of the operating system
and hardware that comprise a platform.</xs:documentation>
  </xs:annotation>
  <xs:element name="platform" type="platform.class"/>
  <xs:complexType name="platform.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/platform "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This metadata element identifies the program number of the associated
program product.  This is typically an order number or a product tracking
code that could be replaced by an order number when a product completes development.</xs:documentation>
  </xs:annotation>
  <xs:element name="prognum" type="prognum.class"/>
  <xs:complexType name="prognum.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/prognum "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains the feature number of a product in the document
metadata.</xs:documentation>
  </xs:annotation>
  <xs:element name="featnum" type="featnum.class"/>
  <xs:complexType name="featnum.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/featnum "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element is used to identify a piece of documentation or of a
product which is associated with the current topic. For example, a product
might be made up of many components, each of which is installable separately.
Components might also be shared by several products so that the same component
could be available for installation with many products. This identification
could be used to check cross-component dependencies when some components are
installed and not others.  http://www.w3.org/TR/REC-html40/struct/objects.html</xs:documentation>
  </xs:annotation>
  <xs:element name="component" type="component.class"/>
  <xs:complexType name="component.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/component "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This metadata element contains the names of the products that are
supported by the information in this topic.</xs:documentation>
  </xs:annotation>
  <xs:element name="prodname" type="prodname.class"/>
  <xs:complexType name="prodname.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="words.cnt"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/prodname "/>
  </xs:complexType>

  <xs:group name="prodinfo.cnt">
    <xs:choice>
      <xs:element ref="brand"/>
      <xs:element ref="series"/>
      <xs:element ref="platform"/>
      <xs:element ref="prognum"/>
      <xs:element ref="featnum"/>
      <xs:element ref="component"/>
    </xs:choice>
  </xs:group>
 
  <xs:annotation>
    <xs:documentation>This is the container for a list (&lt;vrm>) containing the version,
release, and.modification information for multiple products or versions of
products to which the topic applies.</xs:documentation>
  </xs:annotation>
  <xs:element name="vrmlist" type="vrmlist.class"/>
  <xs:complexType name="vrmlist.class">
    <xs:choice>
      <xs:element ref="vrm" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/vrmlist "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This empty element contains information about a single product's
version,.modification, and release, to which the current topic applies.</xs:documentation>
  </xs:annotation>
  <xs:element name="vrm" type="vrm.class"/>
  <xs:complexType name="vrm.class">
    <xs:attribute name="version" type="xs:string"/>
    <xs:attribute name="release" type="xs:string"/>
    <xs:attribute name="modification" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/vrm "/>
  </xs:complexType>

  <xs:simpleType name="audience-type-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="user"/>
      <xs:enumeration value="purchaser"/>
      <xs:enumeration value="administrator"/>
      <xs:enumeration value="programmer"/>
      <xs:enumeration value="executive"/>
      <xs:enumeration value="services"/>
      <xs:enumeration value="other"/>
    </xs:restriction>
  </xs:simpleType>
</xs:schema>
