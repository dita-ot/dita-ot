<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" attributeFormDefault="unqualified">
  <!-- ==================== Import Section ======================= -->
  <xs:import namespace="http://www.w3.org/XML/1998/namespace" schemaLocation="../../../xml/schemas/ditaV1.3/xml.xsd"/>
  <!--DRD: changed type="reference.class"  -->
  <xs:element name="elementref" type="elementref.class" substitutionGroup="reference"/>
  <xs:element name="elementname" abstract="true" substitutionGroup="title"/>
  <xs:element name="elementdesc" abstract="true" substitutionGroup="refbody"/>
  <!-- The element "element" can be substituted wherever the element "term" is used (global).
  	   No abstraction of this is needed. -->
  <xs:annotation>
    <xs:documentation> The element "element" can be substituted wherever the element "term" is used (global).
  	   No abstraction of this is needed. </xs:documentation>
  </xs:annotation>
  <xs:element name="element" substitutionGroup="term"/>
  <xs:element name="longname" abstract="true" substitutionGroup="ph"/>
  <xs:element name="purpose" abstract="true" substitutionGroup="section"/>
  <xs:element name="containedby" abstract="true" substitutionGroup="section"/>
  <xs:element name="contains" abstract="true" substitutionGroup="section"/>
  <xs:element name="attributes" abstract="true" substitutionGroup="section"/>
  <xs:element name="examples" abstract="true" substitutionGroup="example"/>
  <xs:element name="attlist" abstract="true" substitutionGroup="simpletable"/>
  <xs:element name="attribute" abstract="true" substitutionGroup="strow"/>
  <xs:element name="attname" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="attdesc" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="atttype" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="attdefvalue" abstract="true" substitutionGroup="stentry"/>
  <xs:element name="atrequired" abstract="true" substitutionGroup="stentry"/>
  <xs:group name="elementref-info-types">
    <xs:choice>
      <xs:element ref="elementref"/>
    </xs:choice>
  </xs:group>
  <!-- Base type: topic.class reference.class -->
  <xs:complexType name="elementref.class">
    <xs:complexContent>
      <xs:restriction base="reference.class">
        <xs:sequence>
          <xs:element name="elementname" type="elementname.class"/>
          <xs:element ref="titlealts" minOccurs="0"/>
          <xs:element ref="shortdesc" minOccurs="0"/>
          <xs:element ref="prolog" minOccurs="0"/>
          <xs:element name="elementdesc" type="elementdesc.class"/>
          <xs:element ref="related-links" minOccurs="0"/>
          <xs:group ref="elementref-info-types" minOccurs="0" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attribute name="id" type="xs:ID" use="required"/>
        <xs:attribute name="conref" type="xs:string"/>
        <xs:attribute name="DTDVersion" type="xs:string" use="optional" default="V1.1.2"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute ref="xml:lang"/>
        <xs:attributeGroup ref="global-atts"/>
        <!--				<xs:attribute ref="xml:lang"/> -->
        <xs:attribute ref="class" default="- topic/topic reference/reference elementref/elementref "/>
        <xs:attribute name="domains" type="xs:string" default="(topic ui-d) (topic hi-d) (topic sw-d) (topic pr-d) (topic ut-d)"/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: title.class -->
  <xs:complexType name="elementname.class">
    <xs:complexContent>
      <xs:restriction base="title.class">
        <xs:sequence>
          <xs:element name="element" type="element.class" minOccurs="0"/>
          <xs:element name="longname" type="longname.class" minOccurs="0"/>
        </xs:sequence>
        <xs:attributeGroup ref="id-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/title reference/title elementref/elementname "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: refbody.class -->
  <xs:complexType name="elementdesc.class">
    <xs:complexContent>
      <xs:restriction base="refbody.class">
        <xs:sequence>
          <xs:element name="purpose" type="purpose.class" minOccurs="0"/>
          <xs:element name="containedby" type="containedby.class" minOccurs="0"/>
          <xs:element name="contains" type="contains.class" minOccurs="0"/>
          <xs:element name="attributes" type="attributes.class" minOccurs="0"/>
          <xs:element name="examples" type="examples.class" minOccurs="0"/>
        </xs:sequence>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/body elementref/elementdesc "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: term.class -->
  <xs:complexType name="element.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="term.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="tm"/>
        </xs:choice>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/term elementref/element "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: ph.class -->
  <xs:complexType name="longname.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="ph.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="tm"/>
        </xs:choice>
        <xs:attribute name="keyref" type="xs:NMTOKEN"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/term elementref/longname "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="purpose.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="section.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="section.notitle.cnt"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string" default="Purpose"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/section elementref/purpose "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="containedby.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="refsyn.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="section.cnt"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string" default="Contained by"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/section reference/refsyn elementref/containedby "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="contains.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="refsyn.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="section.cnt"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string" default="Contains"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/section reference/refsyn elementref/contains "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: section.class -->
  <xs:complexType name="attributes.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="section.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element name="attlist" type="attlist.class"/>
          <xs:element ref="state"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string" default="Attributes"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/section elementref/attributes "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: example.class -->
  <xs:complexType name="examples.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="example.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="section.cnt"/>
        </xs:choice>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/example elementref/examples "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: simpletable.class -->
  <xs:complexType name="attlist.class">
    <xs:complexContent>
      <xs:restriction base="simpletable.class">
        <xs:sequence>
          <xs:element name="attribute" type="attribute.class" minOccurs="1" maxOccurs="unbounded"/>
        </xs:sequence>
        <xs:attribute name="relcolwidth" type="xs:string"/>
        <xs:attribute name="keycol" type="xs:NMTOKEN"/>
        <xs:attribute name="refcols" type="xs:NMTOKENS"/>
        <xs:attributeGroup ref="display-atts"/>
        <xs:attribute name="outputclass" type="xs:string"/>
        <xs:attribute name="spectitle" type="xs:string"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/simpletable elementref/attlist "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: strow.class -->
  <xs:complexType name="attribute.class">
    <xs:complexContent>
      <xs:restriction base="strow.class">
        <xs:sequence>
          <xs:element name="attname" type="attname.class"/>
          <xs:element name="attdesc" type="attdesc.class"/>
          <xs:element name="atttype" type="atttype.class"/>
          <xs:element name="attdefvalue" type="attdefvalue.class"/>
          <xs:element name="attrequired" type="attrequired.class"/>
        </xs:sequence>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/strow elementref/attribute "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="attname.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attribute name="specentry" type="xs:string" default="Name"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry elementref/attname "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="attdesc.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="desc.cnt"/>
        </xs:choice>
        <xs:attribute name="specentry" type="xs:string" default="Description"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry elementref/attdesc "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="atttype.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attribute name="specentry" type="xs:string" default="Data Type"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry elementref/atttype "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="attdefvalue.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:group ref="basic.ph"/>
        </xs:choice>
        <xs:attribute name="specentry" type="xs:string" default="Default value"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry elementref/attdefvalue "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
  <!-- Base type: stentry.class -->
  <xs:complexType name="attrequired.class" mixed="true">
    <xs:complexContent>
      <xs:restriction base="stentry.class">
        <xs:choice minOccurs="0" maxOccurs="unbounded">
          <xs:element ref="boolean"/>
          <xs:element ref="state"/>
        </xs:choice>
        <xs:attribute name="specentry" type="xs:string" default="Required?"/>
        <xs:attributeGroup ref="univ-atts"/>
        <xs:attributeGroup ref="global-atts"/>
        <xs:attribute ref="class" default="- topic/stentry elementref/attrequired "/>
      </xs:restriction>
    </xs:complexContent>
  </xs:complexType>
</xs:schema>
