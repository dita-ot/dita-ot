<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % apiClassifier         "apiClassifier">
<!ENTITY % apiClassifierDetail   "apiClassifierDetail">
<!ENTITY % apiClassifierDef      "apiClassifierDef">
<!ENTITY % apiBaseClassifier     "apiBaseClassifier">
<!ENTITY % apiClassifierMember   "apiClassifierMember">
<!ENTITY % apiOtherClassifier    "apiOtherClassifier">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % apiClassifier-types-default
    "apiClassifier | apiOperation | apiValue">
<!ENTITY % apiClassifier-info-types "%apiClassifier-types-default;">

<!ENTITY included-domains "">


<!-- ============ Element definitions ============ -->

<!ELEMENT apiClassifier   ( (%apiName;), (%shortdesc;), (%prolog;)?, (%apiClassifierDetail;), (%related-links;)?, (%apiClassifier-info-types;)* )>
<!ATTLIST apiClassifier   id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT apiClassifierDetail  (((%apiSyntax;)*|(%apiClassifierDef;)*), (%apiDesc;)*, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST apiClassifierDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT apiClassifierDef  (%apiBaseClassifier;|%apiDefinition.cnt;|%apiClassifierMember;|%apiItemName;)* >
<!ATTLIST apiClassifierDef  spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiBaseClassifier  (#PCDATA)*>
<!ATTLIST apiBaseClassifier  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiClassifierMember  (%apiItemName;|%apiOtherClassifier;|%apiDefinition.cnt;|%apiClassifierMember;)* >
<!ATTLIST apiClassifierMember  keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiOtherClassifier  (#PCDATA)*>
<!ATTLIST apiOtherClassifier  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!-- ============ Class ancestry ============ -->
<!ATTLIST apiClassifier   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier ">
<!ATTLIST apiClassifierDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiClassifierDetail ">
<!ATTLIST apiClassifierDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiClassifier/apiClassifierDef ">
<!ATTLIST apiBaseClassifier   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier ">
<!ATTLIST apiClassifierMember   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiClassifierMember ">
<!ATTLIST apiOtherClassifier   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiOtherClassifier ">
