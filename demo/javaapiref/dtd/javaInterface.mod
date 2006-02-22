<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % javaInterface         "javaInterface">
<!ENTITY % javaInterfaceDetail   "javaInterfaceDetail">
<!ENTITY % javaInterfaceDef      "javaInterfaceDef">
<!ENTITY % javaInterfaceAccess   "javaInterfaceAccess">
<!ENTITY % javaBaseInterface     "javaBaseInterface">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % javaInterface-types-default  "javaMethod | javaField">
<!ENTITY % javaInterface-info-types     "%javaInterface-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT javaInterface   ( (%apiName;), (%shortdesc;), (%prolog;)?, (%javaInterfaceDetail;), (%related-links;)?, (%javaInterface-info-types;)* )>
<!ATTLIST javaInterface   id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT javaInterfaceDetail  ((%javaInterfaceDef;)?, (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST javaInterfaceDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!-- MULTIPLE BASE INTERFACES? ADDED FOR NOW -->
<!ELEMENT javaInterfaceDef   ((%javaInterfaceAccess;)?, (%javaBaseInterface;)*) >
<!ATTLIST javaInterfaceDef  spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaInterfaceAccess  EMPTY>
<!ATTLIST javaInterfaceAccess  name CDATA #FIXED "access"
                          value (public) #FIXED "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaBaseInterface  (#PCDATA)*>
<!ATTLIST javaBaseInterface  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST javaInterface   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier javaInterface/javaInterface ">
<!ATTLIST javaInterfaceDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiClassifierDetail javaInterface/javaInterfaceDetail ">
<!ATTLIST javaInterfaceDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiClassifier/apiClassifierDef javaInterface/javaInterfaceDef ">
<!ATTLIST javaInterfaceAccess   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier javaInterface/javaInterfaceAccess ">
<!ATTLIST javaBaseInterface   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier javaInterface/javaBaseInterface ">
