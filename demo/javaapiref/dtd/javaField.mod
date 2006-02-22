<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % javaField              "javaField">
<!ENTITY % javaFieldDetail        "javaFieldDetail">
<!ENTITY % javaFieldDef           "javaFieldDef">
<!ENTITY % javaFinalField         "javaFinalField">
<!ENTITY % javaStaticField        "javaStaticField">
<!ENTITY % javaTransientField     "javaTransientField">
<!ENTITY % javaVolatileField      "javaVolatileField">
<!ENTITY % javaFieldAccess        "javaFieldAccess">
<!ENTITY % javaFieldClass         "javaFieldClass">
<!ENTITY % javaFieldInterface     "javaFieldInterface">
<!ENTITY % javaFieldPrimitive     "javaFieldPrimitive">
<!ENTITY % javaFieldArray         "javaFieldArray">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % javaField-types-default  "no-topic-nesting">
<!ENTITY % javaField-info-types     "%javaField-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT javaField       ( (%apiName;), (%shortdesc;), (%prolog;)?, (%javaFieldDetail;), (%related-links;)?, ( %javaField-info-types;)* )>
<!ATTLIST javaField    id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT javaFieldDetail  ((%javaFieldDef;), (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST javaFieldDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT javaFieldDef   ( (%javaFinalField;)?, (%javaStaticField;)?, (%javaTransientField;)?, (%javaVolatileField;)?, (%javaFieldAccess;)?, (%javaFieldClass; | %javaFieldInterface; | %javaFieldPrimitive; ), (%javaFieldArray;)*, (%apiData;)? ) >
<!ATTLIST javaFieldDef    spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFinalField  EMPTY>
<!ATTLIST javaFinalField  name CDATA #FIXED "final"
                          value CDATA #FIXED "final"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaStaticField  EMPTY>
<!ATTLIST javaStaticField  name CDATA #FIXED "static"
                          value CDATA #FIXED "static"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaTransientField  EMPTY>
<!ATTLIST javaTransientField  name CDATA #FIXED "transient"
                          value CDATA #FIXED "transient"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaVolatileField  EMPTY>
<!ATTLIST javaVolatileField  name CDATA #FIXED "volatile"
                          value CDATA #FIXED "volatile"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaFieldAccess  EMPTY>
<!ATTLIST javaFieldAccess  name CDATA #FIXED "access"
                          value (public | protected | private) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFieldClass  (#PCDATA)*>
<!ATTLIST javaFieldClass  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFieldInterface  (#PCDATA)*>
<!ATTLIST javaFieldInterface  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFieldPrimitive  EMPTY>
<!ATTLIST javaFieldPrimitive  name CDATA #FIXED "type"
                          value ( boolean | byte | char | double | float | int | long | short ) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFieldArray  EMPTY>
<!ATTLIST javaFieldArray  name CDATA "arraysize"
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST javaField   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiValue/apiValue javaField/javaField ">
<!ATTLIST javaFieldDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiValue/apiValueDetail javaField/javaFieldDetail ">
<!ATTLIST javaFieldDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiValue/apiValueDef javaField/javaFieldDef ">
<!ATTLIST javaFinalField   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier javaField/javaFinalField ">
<!ATTLIST javaStaticField   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier javaField/javaStaticField ">
<!ATTLIST javaTransientField   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier javaField/javaTransientField ">
<!ATTLIST javaVolatileField   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier javaField/javaVolatileField ">
<!ATTLIST javaFieldAccess   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier javaField/javaFieldAccess ">
<!ATTLIST javaFieldClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiValueClassifier javaField/javaFieldClass ">
<!ATTLIST javaFieldInterface   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiValueClassifier javaField/javaFieldInterface ">
<!ATTLIST javaFieldPrimitive   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiType apiValue/apiType javaField/javaFieldPrimitive ">
<!ATTLIST javaFieldArray   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiArray apiValue/apiArray javaField/javaFieldArray ">
