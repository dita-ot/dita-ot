<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % apiValue               "apiValue">
<!ENTITY % apiValueDetail         "apiValueDetail">
<!ENTITY % apiValueDef            "apiValueDef">
<!ENTITY % apiValueMember         "apiValueMember">
<!ENTITY % apiValueClassifier     "apiValueClassifier">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % apiValue-types-default  "apiOperation | apiValue">
<!ENTITY % apiValue-info-types     "%apiValue-types-default;">

<!ENTITY included-domains "">


<!-- ============ Element definitions ============ -->
<!ELEMENT apiValue       ((%apiName;), (%shortdesc;), (%prolog;)?, (%apiValueDetail;), (%related-links;)?, (%apiValue-info-types;)*)>
<!ATTLIST apiValue        id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT apiValueDetail  (((%apiSyntax;)*|(%apiValueDef;)*), (%apiDesc;)*, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST apiValueDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT apiValueDef    (%apiValueClassifier;|%apiDefinition.cnt;|%apiValueMember;|%apiItemName;)* >
<!ATTLIST apiValueDef     spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiValueMember  (%apiItemName;|%apiValueClassifier;|%apiDefinition.cnt;|%apiValueMember;)* >
<!ATTLIST apiValueMember  keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiValueClassifier  (#PCDATA)*>
<!ATTLIST apiValueClassifier  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class ancestry ============ -->
<!ATTLIST apiValue   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiValue/apiValue ">
<!ATTLIST apiValueDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiValue/apiValueDetail ">
<!ATTLIST apiValueDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiValue/apiValueDef ">
<!ATTLIST apiValueMember   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiValueMember ">
<!ATTLIST apiValueClassifier   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiValueClassifier ">
