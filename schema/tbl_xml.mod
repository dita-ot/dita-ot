<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 | This file is part of the DITA package on IBM's developerWorks site.
 | See license.txt for disclaimers and permissions.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | File: tbl_xml.mod
 |
 | Release history (vrm):
 |   1.0.0 Release 1.2 - Initial XML Schema release on IBM's developerWorks, June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 *-->

<!-- Usage Notes:
 | This file is based on the OASIS XML Exchange Table Model DTD (itself based on
 | the CALS Table DTD) slightly simplified and having DITA content in its cells.
 |
 | This model has had some features elided for simplification.
 |
 | Attributes with a Boolean value can take the declared value "% yesorno ;"
 | where 0 is false and any other value is true.  Those with impliable values
 | have been preset in this application to "0" as the default setting.
 |
 | Other content within this fragment must be declared in the context of
 | the embedding DTD:
 |   the title element (inherits whatever caption-like content is declared there)
 |   the entry element (uses tblcell.cnt content, similar to list item or long quotes)
 |
 | For example, this minimal DITA-like subset might be inherited by this fragment:
 *-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">

  <xs:annotation>
    <xs:documentation>The container element for CALS table elements. See &lt;simpletable>
for a simplified table.model.</xs:documentation>
  </xs:annotation>

  <xs:element name="table" type="table.class"/>
  <xs:complexType name="table.class">
    <xs:sequence>
      <xs:sequence minOccurs="0">
        <xs:group ref="title" minOccurs="0"/>
        <xs:group ref="desc" minOccurs="0"/>
      </xs:sequence>
      <xs:group ref="tgroup" maxOccurs="unbounded"/>
    </xs:sequence>
    <xs:attributeGroup ref="colrowsep-atts"/>
    <xs:attribute name="rowheader" type="rowheader-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="display-atts"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/table "/>
  </xs:complexType>

  <xs:simpleType name="rowheader-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="firstcol"/>
      <xs:enumeration value="norowheader"/>
    </xs:restriction>
  </xs:simpleType>


  <xs:annotation>
    <xs:documentation>The element in a &lt;table> that contains column, row, spanning,
header and footer specifications, and the body (&lt;tbody>) of the table.</xs:documentation>
  </xs:annotation>
  <xs:element name="tgroup" type="tgroup.class"/>
  <xs:complexType name="tgroup.class">
    <xs:sequence>
      <xs:group ref="colspec" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="spanspec" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="thead" minOccurs="0"/>
      <xs:group ref="tfoot" minOccurs="0"/>
      <xs:group ref="tbody"/>
    </xs:sequence>
    <xs:attribute name="cols" type="xs:NMTOKEN" use="required"/>
    <xs:attributeGroup ref="colrowsep-atts"/>
    <xs:attribute name="align" type="align-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/tgroup "/>
  </xs:complexType>

  <xs:attributeGroup name="colrowsep-atts">
    <xs:attribute name="colsep" type="xs:NMTOKEN"/>
    <xs:attribute name="rowsep" type="xs:NMTOKEN"/>
  </xs:attributeGroup>

  <xs:annotation>
    <xs:documentation>This element contains a column specification for a table, including
assigning a column name and number, cell content alignment, and column width.        </xs:documentation>
  </xs:annotation>
  <xs:element name="colspec" type="colspec.class"/>
  <xs:complexType name="colspec.class">
    <xs:attribute name="colnum" type="xs:string"/>
    <xs:attribute name="colname" type="xs:string"/>
    <xs:attribute name="align" type="align-att.class"/>
    <xs:attribute name="colwidth" type="xs:string"/>
    <xs:attributeGroup ref="colrowsep-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/colspec "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>A span specification in a table column or row specifies how two or
more cells are to be combined.</xs:documentation>
  </xs:annotation>
  <xs:element name="spanspec" type="spanspec.class"/>
  <xs:complexType name="spanspec.class">
    <xs:attribute name="namest" type="xs:string" use="required"/>
    <xs:attribute name="nameend" type="xs:string" use="required"/>
    <xs:attribute name="spanname" type="xs:string" use="required"/>
    <xs:attribute name="align" type="align-att.class"/>
    <xs:attributeGroup ref="colrowsep-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/spanspec "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This table header element precedes the table body (&lt;tbody>) element.
 It is currently not used.</xs:documentation>
  </xs:annotation>
  <xs:element name="thead" type="thead.class"/>
  <xs:complexType name="thead.class">
    <xs:sequence>
      <xs:group ref="colspec" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="row" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="valign" type="valign-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/thead "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This table footer element precedes the &lt;table> body (&lt;tbody>)
element. It is currently not used.</xs:documentation>
  </xs:annotation>
  <xs:element name="tfoot" type="tfoot.class"/>
  <xs:complexType name="tfoot.class">
    <xs:sequence>
      <xs:group ref="colspec" minOccurs="0" maxOccurs="unbounded"/>
      <xs:group ref="row" minOccurs="0"/>
    </xs:sequence>
    <xs:attribute name="valign" type="valign-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/tfoot "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains the rows in a &lt;table></xs:documentation>
  </xs:annotation>
  <xs:element name="tbody" type="tbody.class"/>
  <xs:complexType name="tbody.class">
    <xs:choice>
      <xs:group ref="row" minOccurs="0" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="valign" type="valign-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/tbody "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element contains a single row in a table &lt;tgroup>.</xs:documentation>
  </xs:annotation>
  <xs:element name="row" type="row.class"/>
  <xs:complexType name="row.class">
    <xs:choice>
      <xs:group ref="entry" maxOccurs="unbounded"/>
    </xs:choice>
    <xs:attribute name="rowsep" type="xs:NMTOKEN"/>
    <xs:attribute name="valign" type="valign-att.class"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="univ-atts"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/row "/>
  </xs:complexType>

  <xs:annotation>
    <xs:documentation>This element defines an entry (a single cell) in a table row.</xs:documentation>
  </xs:annotation>
  <xs:element name="entry" type="entry.class"/>
  <xs:complexType name="entry.class" mixed="true">
    <xs:choice minOccurs="0" maxOccurs="unbounded">
      <xs:group ref="tblcell.cnt"/>
    </xs:choice>
    <xs:attribute name="namest" type="xs:string"/>
    <xs:attribute name="nameend" type="xs:string"/>
    <xs:attribute name="spanname" type="xs:string"/>
    <xs:attribute name="colname" type="xs:string"/>
    <xs:attribute name="morerows" type="xs:string"/>
    <xs:attributeGroup ref="colrowsep-atts"/>
    <xs:attribute name="align" type="align-att.class"/>
    <xs:attribute name="valign" type="valign-att.class"/>
<!-- added DITA attributes -->
    <xs:attribute name="rev" type="xs:string"/>
    <xs:attribute name="outputclass" type="xs:string"/>
    <xs:attributeGroup ref="global-atts"/>
    <xs:attribute ref="class" default="- topic/tgroup "/>
  </xs:complexType>

  <xs:simpleType name="align-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="left"/>
      <xs:enumeration value="right"/>
      <xs:enumeration value="center"/>
      <xs:enumeration value="justify"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="valign-att.class">
    <xs:restriction base="xs:string">
      <xs:enumeration value="top"/>
      <xs:enumeration value="middle"/>
      <xs:enumeration value="bottom"/>
    </xs:restriction>
  </xs:simpleType>
</xs:schema>
