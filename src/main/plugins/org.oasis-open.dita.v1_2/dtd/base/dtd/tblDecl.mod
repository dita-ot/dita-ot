<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    XML EXCHANGE TABLE MODEL DECLARATION MODULE       -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Corrected the "Delivered as" system ID     -->
<!--    2006.06.07 RDA: Make universal attributes universal        -->
<!--                      (DITA 1.1 proposal #12)                  -->
<!--    2006.11.30 RDA: Add -dita-use-conref-target to enumerated  -->
<!--                      attributes                               -->
<!--    2008.02.13 RDA: Create .content and .attributes entities;  -->
<!--                    requires reorganization of some existing   -->
<!--                    entities provided with the Exchange Model  -->

<!-- ============================================================= -->
<!--  Delivered as file "tblDecl.mod"                              -->
<!-- ============================================================= -->


<!-- XML EXCHANGE TABLE MODEL DECLARATION MODULE                   -->

<!-- OASIS DITA 1.0 notes:
     The Exchange Table Model replaces the original CALS-based model
     used in development versions of the DITA definition.
     This set of declarations defines the XML version of the Exchange
     Table Model as adapted for OASIS DITA version 1.0.
     The base for the DITA version of the Exchange Table Model is
     defined at http://www.oasis-open.org/specs/tm9901.htm .
     The DITA version specifically extends this model with these DITA-
     specific content and attribute adaptations to the DITA processing
     requirements:

STRUCTURE:

Introduce the DITA-unique <desc> element (optional after <title>); this element
enables more consistent presentation of both figures and tables.


ATTRIBUTES:

For frame, align, and valign attributes on any element:
  Add the enumerated value -dita-use-conref-target for DITA 1.1.  

For the <table> element, add:
  %univ-atts; which consists of:
    %select-atts; [for selection, conditional processing]
    %id-atts; [for conref and linking]
    %localization-atts (@translate + @xml:lang) [for NLS processing support]
    %global-atts; (@xtrf + @xtrc) [for tracing and messaging support in processors]
    @class [for specialization support]
  @outputclass [for role designation (ie, styles, future migrations)]
  @rowheader [for accessibility support in processing]
  %display-atts; which consists of:
    @scale [for presentational equivalence with other scaled exhibits: fig, pre, lines, simpletable]
    @frame (already part of table)
    @pgwide (already part of table, same intent as original @expanse)

For <tgroup>, <thead>, <tbody>, and <row>, add:
  %univ-atts;
  %global-atts;
  @class
  @outputclass [for role designation (ie, styles, future migrations)]

For <entry>, add:
  %id-atts;
  %global-atts
  @class
  @outputclass [for role designation (ie, styles, future migrations)]
  @rev [for indication of revised content for flag-based processing]

-->

<!-- DITA specialization support: element redefinition for expansion -->

<!ENTITY % table "table">
<!ENTITY % tgroup "tgroup">
<!ENTITY % colspec "colspec">
<!ENTITY % thead "thead">
<!ENTITY % tbody "tbody">
<!ENTITY % row "row">
<!ENTITY % entry "entry">

<!-- The Formal Public Identifier (FPI) for this DITA adaptation of
     the Exchange Table Model shall be:

     "-//OASIS//ELEMENTS DITA Exchange Table Model//EN"

     This set of declarations may be referred to using a public external
     entity declaration and reference as shown in the following three
     lines:

     <!ENTITY % tableXML
       PUBLIC "-//OASIS//ELEMENTS DITA Exchange Table Model//EN">
       %tableXML;
-->



<!-- In order to use the Exchange table model, various parameter entity
     declarations are required.  A brief description is as follows:

     ENTITY NAME      WHERE USED              WHAT IT IS

     %yesorno         In ATTLIST of:          An attribute declared value
                      almost all elements     for a "boolean" attribute

     %paracon         In content model of:    The "text" (logical content)
                      <entry>                 of the model group for <entry>

     %titles          In content model of:    The "title" part of the model
                      table element           group for the table element

     %tbl.table.name  In declaration of:      The name of the "table"
                      table element           element

     %tbl.table-titles.mdl In content model of: The model group for the title
                      table elements          part of the content model for
                                              table element

     %tbl.table.mdl   In content model of:    The model group for the content
                      table elements          model for table element,
                                              often (and by default) defined
                                              in terms of %tbl.table-titles.mdl
                                              and tgroup

     %tbl.table.att   In ATTLIST of:          Additional attributes on the
                      table element           table element

     %bodyatt         In ATTLIST of:          Additional attributes on the
                      table element           table element (for backward
                                              compatibility with the SGML
                                              model)

     %tbl.tgroup.mdl  In content model of:    The model group for the content
                      <tgroup>                model for <tgroup>

     %tbl.tgroup.att  In ATTLIST of:          Additional attributes on the
                      <tgroup>                <tgroup> element

     %tbl.thead.att   In ATTLIST of:          Additional attributes on the
                      <thead>                 <thead> element

     %tbl.tbody.att   In ATTLIST of:          Additional attributes on the
                      <tbody>                 <tbody> element

     %tbl.colspec.att In ATTLIST of:          Additional attributes on the
                      <colspec>               <colspec> element

     %tbl.row.mdl     In content model of:    The model group for the content
                      <row>                   model for <row>

     %tbl.row.att     In ATTLIST of:          Additional attributes on the
                      <row>                   <row> element

     %tbl.entry.mdl   In content model of:    The model group for the content
                      <entry>                 model for <entry>

     %tbl.entry.att   In ATTLIST of:          Additional attributes on the
                      <entry>                 <entry> element

     This set of declarations will use the default definitions shown below
     for any of these parameter entities that are not declared before this
     set of declarations is referenced.

     Note that DITA properties are added in cumulative declarations at the end.
-->

<!-- These definitions are not directly related to the table model, but are
     used in the default CALS table model and may be defined elsewhere (and
     prior to the inclusion of this table module) in the referencing DTD. -->

<!ENTITY % yesorno 'NMTOKEN'> <!-- no if zero(s), yes if any other value -->
<!ENTITY % titles  'title?'>
<!ENTITY % paracon '%tblcell.cnt;'> <!-- default for use in entry content -->

<!--
The parameter entities as defined below change and simplify the CALS table
model as published (as part of the Example DTD) in MIL-HDBK-28001.  The
resulting simplified DTD has support from the SGML Open vendors and is
therefore more interoperable among different systems.

These following declarations provide the Exchange default definitions
for these entities.  However, these entities can be redefined (by giving
the appropriate parameter entity declaration(s) prior to the reference
to this Table Model declaration set entity) to fit the needs of the
current application.

Note, however, that changes may have significant effect on the ability to
interchange table information.  These changes may manifest themselves
in useability, presentation, and possible structure information degradation.
-->

<!ENTITY % tbl.table.name       "table">
<!ENTITY % tbl.table-titles.mdl "((%title;)?, (%desc;)?)?,">
<!ENTITY % tbl.table-main.mdl   "(%tgroup;)+">
<!ENTITY % tbl.table.mdl        "%tbl.table-titles.mdl; %tbl.table-main.mdl;">
<!ENTITY % tbl.table.att        "
    pgwide      %yesorno;       #IMPLIED ">
<!ENTITY % bodyatt              "">
<!ENTITY % tbl.tgroup.mdl       "(%colspec;)*, (%thead;)?, %tbody;">
<!ENTITY % tbl.tgroup.att       "">
<!ENTITY % tbl.thead.att        "">
<!ENTITY % tbl.tbody.att        "">
<!ENTITY % tbl.colspec.att      "base                        CDATA   #IMPLIED
                                 %base-attribute-extensions;">
<!ENTITY % tbl.row.mdl          "(%entry;)+">
<!ENTITY % tbl.row.att          "">
<!ENTITY % tbl.entry.mdl        "(%paracon;)*">
<!ENTITY % tbl.entry.att        "base                        CDATA   #IMPLIED
                                 %base-attribute-extensions;">

<!-- =====  Element and attribute declarations follow. =====  -->

<!-- ============================================================= -->
<!--                    DITA BEHAVIOR ATTRIBUTES                   -->
<!-- ============================================================= -->

<!ENTITY % dita.table.attributes
       "rowheader   (firstcol | norowheader | 
                         -dita-use-conref-target) #IMPLIED
        scale (50|60|70|80|90|100|110|120|140|160|180|200| 
                         -dita-use-conref-target) #IMPLIED
        %univ-atts;
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.tgroup.attributes
       "%univ-atts;
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.thead.attributes
       "%univ-atts;
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.tbody.attributes
       "%univ-atts;
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.row.attributes
       "%univ-atts;
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.entry.attributes
       "%id-atts;
        %localization-atts;
        rev CDATA #IMPLIED
        outputclass CDATA #IMPLIED">
<!ENTITY % dita.colspec.attributes
       "%id-atts;
        %localization-atts;">

<!-- ============================================================= -->
<!--                   ORIGINAL ELEMENT AND ATTRIBUTE DECLARATIONS -->
<!-- ============================================================= -->

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.table.name       "table"
     ENTITY % tbl.table-titles.mdl "%titles;,"
     ENTITY % tbl.table.mdl        "%tbl.table-titles; tgroup+"
     ENTITY % tbl.table.att        "
                        pgwide          %yesorno;       #IMPLIED "
-->

<!ENTITY % table.content     "(%tbl.table.mdl;)">
<!ENTITY % table.attributes
       "frame           (top|bottom|topbot|all|sides|none| 
                         -dita-use-conref-target)               #IMPLIED
        colsep          %yesorno;                               #IMPLIED
        rowsep          %yesorno;                               #IMPLIED
        %tbl.table.att;
        %bodyatt;
        %dita.table.attributes;">
<!ELEMENT %tbl.table.name;    %table.content;>
<!ATTLIST %tbl.table.name;    %table.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.tgroup.mdl    "colspec*,thead?,tbody"
     ENTITY % tbl.tgroup.att    ""
-->

<!ENTITY % tgroup.content     "(%tbl.tgroup.mdl;)">
<!ENTITY % tgroup.attributes
       "cols            NMTOKEN                                 #REQUIRED
        colsep          %yesorno;                               #IMPLIED
        rowsep          %yesorno;                               #IMPLIED
        align           (left|right|center|justify|char| 
                         -dita-use-conref-target)               #IMPLIED
        %tbl.tgroup.att;
        %dita.tgroup.attributes;">
<!ELEMENT tgroup    %tgroup.content;>
<!ATTLIST tgroup    %tgroup.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.colspec.att   ""
-->

<!ENTITY % colspec.content     "EMPTY">
<!ENTITY % colspec.attributes
       "colnum          NMTOKEN                                 #IMPLIED
        colname         NMTOKEN                                 #IMPLIED
        colwidth        CDATA                                   #IMPLIED
        colsep          %yesorno;                               #IMPLIED
        rowsep          %yesorno;                               #IMPLIED
        align           (left|right|center|justify|char| 
                         -dita-use-conref-target)               #IMPLIED
        char            CDATA                                   #IMPLIED
        charoff         NMTOKEN                                 #IMPLIED
        %tbl.colspec.att;
        %dita.colspec.attributes;">
<!ELEMENT colspec    %colspec.content;>
<!ATTLIST colspec    %colspec.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.thead.att      ""
-->

<!ENTITY % thead.content     "((%row;)+)">
<!ENTITY % thead.attributes
       "valign          (top|middle|bottom|
                         -dita-use-conref-target)               #IMPLIED
        %tbl.thead.att;
        %dita.thead.attributes;">
<!ELEMENT thead    %thead.content;>
<!ATTLIST thead    %thead.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.tbody.att     ""
-->

<!ENTITY % tbody.content     "(%row;)+">
<!ENTITY % tbody.attributes
       "valign          (top|middle|bottom| 
                         -dita-use-conref-target)               #IMPLIED
        %tbl.tbody.att;
        %dita.tbody.attributes;">
<!ELEMENT tbody    %tbody.content;>
<!ATTLIST tbody    %tbody.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % tbl.row.mdl       "entry+"
     ENTITY % tbl.row.att       ""
-->

<!ENTITY % row.content     "(%tbl.row.mdl;)">
<!ENTITY % row.attributes
       "rowsep          %yesorno;                               #IMPLIED
        valign          (top|middle|bottom| 
                         -dita-use-conref-target)               #IMPLIED
        %tbl.row.att;
        %dita.row.attributes;">
<!ELEMENT row    %row.content;>
<!ATTLIST row    %row.attributes;>

<!--
     Default declarations previously defined in this entity and
     referenced below include:
     ENTITY % paracon           "#PCDATA"
     ENTITY % tbl.entry.mdl     "(%paracon;)*"
     ENTITY % tbl.entry.att     ""
-->

<!ENTITY % entry.content     "%tbl.entry.mdl;">
<!ENTITY % entry.attributes
       "colname         NMTOKEN                                 #IMPLIED
        namest          NMTOKEN                                 #IMPLIED
        nameend         NMTOKEN                                 #IMPLIED
        morerows        NMTOKEN                                 #IMPLIED
        colsep          %yesorno;                               #IMPLIED
        rowsep          %yesorno;                               #IMPLIED
        align           (left|right|center|justify|char| 
                         -dita-use-conref-target)               #IMPLIED
        char                                CDATA                                   #IMPLIED
        charoff         NMTOKEN                                 #IMPLIED
        valign          (top|middle|bottom| 
                         -dita-use-conref-target)               #IMPLIED
        %tbl.entry.att;
        %dita.entry.attributes;">
<!ELEMENT entry    %entry.content;>
<!ATTLIST entry    %entry.attributes;>


<!-- ============================================================= -->
<!--                    DITA SPECIALIZATION ATTRIBUTE DECLARATIONS -->
<!-- ============================================================= -->

<!ATTLIST table    %global-atts;  class CDATA "- topic/table "       >
<!ATTLIST tgroup   %global-atts;  class CDATA "- topic/tgroup "      >
<!ATTLIST colspec  %global-atts;  class CDATA "- topic/colspec "     >
<!ATTLIST thead    %global-atts;  class CDATA "- topic/thead "       >
<!ATTLIST tbody    %global-atts;  class CDATA "- topic/tbody "       >
<!ATTLIST row      %global-atts;  class CDATA "- topic/row "         >
<!ATTLIST entry    %global-atts;  class CDATA "- topic/entry "       >


<!-- ================== End XML Exchange Table Model ============= -->
