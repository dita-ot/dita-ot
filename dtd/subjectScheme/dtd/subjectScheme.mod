<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Subject Scheme Map                           -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Subject Scheme Map//EN"
      Delivered as file "subjectScheme.mod"                        -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for DITA Subject Scheme Maps           -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             February 2008                                     -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2008, 2009.              -->
<!--             (C) Copyright IBM Corporation 2005, 2007.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2008.02.13 RDA: Created file based upon prototype from IBM -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % subjectScheme     "subjectScheme">
<!ENTITY % schemeref         "schemeref">
<!ENTITY % hasNarrower       "hasNarrower">
<!ENTITY % hasKind           "hasKind">
<!ENTITY % hasPart           "hasPart">
<!ENTITY % hasInstance       "hasInstance">
<!ENTITY % hasRelated        "hasRelated">
<!ENTITY % subjectdef        "subjectdef">
<!ENTITY % subjectHead       "subjectHead">
<!ENTITY % subjectHeadMeta   "subjectHeadMeta">
<!ENTITY % enumerationdef    "enumerationdef">
<!ENTITY % elementdef        "elementdef">
<!ENTITY % attributedef      "attributedef">
<!ENTITY % defaultSubject    "defaultSubject">
<!ENTITY % relatedSubjects   "relatedSubjects">
<!ENTITY % subjectRelTable   "subjectRelTable">
<!ENTITY % subjectRelHeader  "subjectRelHeader">
<!ENTITY % subjectRel        "subjectRel">
<!ENTITY % subjectRole       "subjectRole">

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!-- This differs from topicref-atts-no-toc only by providing a
     default for @processing-role                                  -->
<!ENTITY % topicref-atts-for-subjectScheme 
             'collection-type 
                        (choice | 
                         family | 
                         sequence | 
                         unordered |
                         -dita-use-conref-target) 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  "resource-only"
              scope 
                        (external | 
                         local | 
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              locktitle 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              linking 
                        (none | 
                         normal | 
                         sourceonly | 
                         targetonly |
                         -dita-use-conref-target) 
                                  #IMPLIED
              toc 
                        (no | 
                         yes | 
                         -dita-use-conref-target)
                                  "no"
              print 
                        (no | 
                         printonly | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              search 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              chunk 
                        CDATA 
                                  #IMPLIED
  '
>

<!--                    LONG NAME: Subject Scheme Map              -->
<!ENTITY % subjectScheme.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         ((%anchor; |
                           %data.elements.incl; |
                           %enumerationdef; |
                           %hasInstance; |
                           %hasKind; |
                           %hasNarrower; |
                           %hasPart; |
                           %hasRelated; |
                           %navref; |
                           %relatedSubjects; |
                           %reltable; |
                           %schemeref; |
                           %subjectdef; |
                           %subjectHead; |
                           %subjectRelTable; |
                           %topicref;)*))
">
<!ENTITY % subjectScheme.attributes
             "id 
                        ID 
                                  #IMPLIED
              %conref-atts;
              anchorref 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              %localization-atts;
              %topicref-atts-for-subjectScheme;
              %select-atts;"
>
<!ELEMENT subjectScheme    %subjectScheme.content;>
<!ATTLIST subjectScheme    
              %subjectScheme.attributes;
              %arch-atts;
              domains 
                        CDATA 
                                  "&included-domains;" 
>

<!--                    LONG NAME: Scheme reference                -->
<!ENTITY % schemeref.content
                       "((%topicmeta;)?,
                         (%data.elements.incl;)*)
">
<!ENTITY % schemeref.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              type 
                        CDATA 
                                  'scheme'
              format 
                        CDATA 
                                  'ditamap'
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT schemeref    %schemeref.content;>
<!ATTLIST schemeref    %schemeref.attributes;>


<!--                    LONG NAME: Has Narrower Relationship       -->
<!ENTITY % hasNarrower.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)
">
<!ENTITY % hasNarrower.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT hasNarrower    %hasNarrower.content;>
<!ATTLIST hasNarrower    %hasNarrower.attributes;>


<!--                    LONG NAME: Has Kind Relationship           -->
<!ENTITY % hasKind.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)
">
<!ENTITY % hasKind.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT hasKind    %hasKind.content;>
<!ATTLIST hasKind    %hasKind.attributes;>


<!--                    LONG NAME: Has Part Relationship           -->
<!ENTITY % hasPart.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)
">
<!ENTITY % hasPart.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT hasPart    %hasPart.content;>
<!ATTLIST hasPart    %hasPart.attributes;>


<!--                    LONG NAME: Has Instance Relationship       -->
<!ENTITY % hasInstance.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)
">
<!ENTITY % hasInstance.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT hasInstance    %hasInstance.content;>
<!ATTLIST hasInstance    %hasInstance.attributes;>


<!--                    LONG NAME: Has Related Relationship        -->
<!ENTITY % hasRelated.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)
">
<!ENTITY % hasRelated.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              collection-type 
                        (choice |
                         sequence |
                         unordered |
                         -dita-use-conref-target) 
                                  'choice'
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              scope 
                        (external |
                         local |
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT hasRelated    %hasRelated.content;>
<!ATTLIST hasRelated    %hasRelated.attributes;>


<!--                    LONG NAME: Subject definition              -->
<!ENTITY % subjectdef.content
                       "((%topicmeta;)?,
                         (%data.elements.incl; |
                          %hasInstance; |
                          %hasKind; |
                          %hasNarrower; |
                          %hasPart; |
                          %hasRelated; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)"
>
<!ENTITY % subjectdef.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              copy-to 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              collection-type 
                        (choice | 
                         family | 
                         sequence | 
                         unordered |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local |
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              locktitle 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              linking 
                        (none | 
                         normal | 
                         sourceonly | 
                         targetonly |
                         -dita-use-conref-target) 
                                  #IMPLIED
              toc 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT subjectdef    %subjectdef.content;>
<!ATTLIST subjectdef    %subjectdef.attributes;>


<!--                    LONG NAME: Subject Heading                 -->
<!-- SKOS equivalent: concept collection -->
<!ENTITY % subjectHead.content
                       "((%subjectHeadMeta;)?,
                         (%data.elements.incl; |
                          %subjectdef; |
                          %subjectHead; |
                          %topicref;)*)"
>
<!ENTITY % subjectHead.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              collection-type 
                        (sequence | 
                         unordered |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              linking 
                        (normal) 
                                  'normal'
              toc 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT subjectHead    %subjectHead.content;>
<!ATTLIST subjectHead    %subjectHead.attributes;>

<!--                    LONG NAME: Subject Heading Metadata        -->
<!ENTITY % subjectHeadMeta.content
                       "((%navtitle;)?,
                         (%shortdesc;)?)"
>
<!ENTITY % subjectHeadMeta.attributes
             "lockmeta 
                       (no | 
                        yes | 
                        -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT subjectHeadMeta    %subjectHeadMeta.content;>
<!ATTLIST subjectHeadMeta    %subjectHeadMeta.attributes;>

<!--                    LONG NAME: Enumeration definition          -->
<!ENTITY % enumerationdef.content
                       "((%elementdef;)?,
                         (%attributedef;),
                         (%subjectdef;)+,
                         (%defaultSubject;)?,
                         (%data.elements.incl;)*)
">
<!ENTITY % enumerationdef.attributes
             "%id-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED
              status 
                        (changed | 
                         deleted | 
                         new | 
                         unchanged | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              base 
                         CDATA 
                                  #IMPLIED
              %base-attribute-extensions;"
>
<!ELEMENT enumerationdef    %enumerationdef.content;>
<!ATTLIST enumerationdef    %enumerationdef.attributes;>


<!--                    LONG NAME: Element definition              -->
<!ENTITY % elementdef.content
                       "((%data.elements.incl;)*)"
>
<!ENTITY % elementdef.attributes
             "%id-atts;
              name
                        CDATA
                                  #REQUIRED
              outputclass 
                        CDATA 
                                  #IMPLIED
              status 
                        (changed | 
                         deleted | 
                         new | 
                         unchanged | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              translate 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  'no'
              base 
                         CDATA 
                                  #IMPLIED
              %base-attribute-extensions;"
>
<!ELEMENT elementdef    %elementdef.content;>
<!ATTLIST elementdef    %elementdef.attributes;>


<!--                    LONG NAME: Attribute definition            -->
<!ENTITY % attributedef.content
                       "((%data.elements.incl;)*)"
>
<!ENTITY % attributedef.attributes
             "%id-atts;
              name
                        CDATA
                                  #REQUIRED
              outputclass 
                        CDATA 
                                  #IMPLIED
              status 
                        (changed | 
                         deleted | 
                         new | 
                         unchanged | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              translate 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  'no'
              base 
                         CDATA 
                                  #IMPLIED
              %base-attribute-extensions;"
>
<!ELEMENT attributedef    %attributedef.content;>
<!ATTLIST attributedef    %attributedef.attributes;>


<!--                    LONG NAME: Default Subject                 -->
<!ENTITY % defaultSubject.content
                       "((%data.elements.incl;)*)"
>
<!ENTITY % defaultSubject.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              copy-to 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local | 
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              locktitle 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              linking 
                        (none | 
                         normal | 
                         sourceonly | 
                         targetonly |
                         -dita-use-conref-target) 
                                  #IMPLIED
              toc 
                        (no | 
                         yes | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT defaultSubject    %defaultSubject.content;>
<!ATTLIST defaultSubject    %defaultSubject.attributes;>


<!--                    LONG NAME: Related Subjects                -->
<!-- To define roles within a relationship, you can specialize
     the relatedSubjects container and its contained subjectdef elements,
     possibly setting the linking attribute to targetonly or sourceonly.
     For instance, a dependency relationship could contain depended-on
     and dependent subjects.
     -->
<!ENTITY % relatedSubjects.content
                       "((%data.elements.incl; | 
                          %subjectdef; | 
                          %topicref;)*)
">
<!ENTITY % relatedSubjects.attributes
             "navtitle 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              keys 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              collection-type 
                        (choice | 
                         family | 
                         sequence | 
                         unordered |
                         -dita-use-conref-target) 
                                  'family'
              processing-role
                        (normal |
                         resource-only |
                         -dita-use-conref-target)
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local | 
                         peer |
                         -dita-use-conref-target) 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              linking 
                        (none | 
                         normal | 
                         sourceonly | 
                         targetonly |
                         -dita-use-conref-target) 
                                  'normal'
              %univ-atts;
">
<!ELEMENT relatedSubjects    %relatedSubjects.content;>
<!ATTLIST relatedSubjects    %relatedSubjects.attributes;>


<!--                    LONG NAME: Subject Relationship Table      -->
<!-- Where there are many instances of a subject relationship in which
     different subjects have defined roles within the relationship,
     you can use or specialize the subjectRelTable.
     Note that each row matrixes relationships across columns such that
     a subject receives relationships to every subject in other columns
     within the same row. -->
<!ENTITY % subjectRelTable.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%subjectRelHeader;)?,
                         (%subjectRel;)+)
">
<!ENTITY % subjectRelTable.attributes
             "%topicref-atts-no-toc;
              %univ-atts;
">
<!ELEMENT subjectRelTable    %subjectRelTable.content;>
<!ATTLIST subjectRelTable    %subjectRelTable.attributes;>


<!--                    LONG NAME: Subject Table Header            -->
<!-- The header defines the role of subjects in each column
     The role definition can be an informal navtitle or 
         a formal reference -->
<!ENTITY % subjectRelHeader.content
                       "((%subjectRole;)+)
">
<!ENTITY % subjectRelHeader.attributes
             "%univ-atts;
">
<!ELEMENT subjectRelHeader    %subjectRelHeader.content;>
<!ATTLIST subjectRelHeader    %subjectRelHeader.attributes;>


<!--                    LONG NAME: Subject Table Row               -->
<!ENTITY % subjectRel.content
                       "((%subjectRole;)+)
">
<!ENTITY % subjectRel.attributes
             "%univ-atts;
">
<!ELEMENT subjectRel    %subjectRel.content;>
<!ATTLIST subjectRel    %subjectRel.attributes;>


<!--                    LONG NAME: Subject Role                    -->
<!ENTITY % subjectRole.content
                       "((%data.elements.incl; |
                          %subjectdef;|
                          %topicref;)*)
">
<!ENTITY % subjectRole.attributes
             "%topicref-atts;
              %univ-atts;
">
<!ELEMENT subjectRole    %subjectRole.content;>
<!ATTLIST subjectRole    %subjectRole.attributes;>


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST subjectScheme %global-atts;
    class CDATA "- map/map subjectScheme/subjectScheme ">
<!ATTLIST schemeref %global-atts;
    class CDATA "- map/topicref subjectScheme/schemeref ">
<!ATTLIST hasNarrower %global-atts;
    class CDATA "- map/topicref subjectScheme/hasNarrower ">
<!ATTLIST hasKind %global-atts;
    class CDATA "- map/topicref subjectScheme/hasKind ">
<!ATTLIST hasPart %global-atts;
    class CDATA "- map/topicref subjectScheme/hasPart ">
<!ATTLIST hasInstance %global-atts;
    class CDATA "- map/topicref subjectScheme/hasInstance ">
<!ATTLIST hasRelated %global-atts;
    class CDATA "- map/topicref subjectScheme/hasRelated ">
<!ATTLIST enumerationdef %global-atts;
    class CDATA "- map/topicref subjectScheme/enumerationdef ">
<!ATTLIST elementdef %global-atts;
    class CDATA "- topic/data subjectScheme/elementdef ">
<!ATTLIST attributedef %global-atts;
    class CDATA "- topic/data subjectScheme/attributedef ">
<!ATTLIST defaultSubject %global-atts;
    class CDATA "- map/topicref subjectScheme/defaultSubject ">
<!ATTLIST subjectHead %global-atts;
    class CDATA "- map/topicref subjectScheme/subjectHead ">
<!ATTLIST subjectHeadMeta %global-atts;
    class CDATA "- map/topicmeta subjectScheme/subjectHeadMeta ">
<!ATTLIST subjectdef %global-atts;
    class CDATA "- map/topicref subjectScheme/subjectdef ">
<!ATTLIST relatedSubjects %global-atts;
    class CDATA "- map/topicref subjectScheme/relatedSubjects ">
<!ATTLIST subjectRelTable %global-atts;
    class CDATA "- map/reltable subjectScheme/subjectRelTable ">
<!ATTLIST subjectRelHeader %global-atts;
    class CDATA "- map/relrow subjectScheme/subjectRelHeader ">
<!ATTLIST subjectRel %global-atts;
    class CDATA "- map/relrow subjectScheme/subjectRel ">
<!ATTLIST subjectRole %global-atts;
    class CDATA "- map/relcell subjectScheme/subjectRole ">

<!-- ================== End DITA Subject Scheme Map ============== -->
