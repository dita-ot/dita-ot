<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % javaClass                    "javaClass">
<!ENTITY % javaClassDetail              "javaClassDetail">
<!ENTITY % javaClassDef                 "javaClassDef">
<!ENTITY % javaFinalClass               "javaFinalClass">
<!ENTITY % javaAbstractClass            "javaAbstractClass">
<!ENTITY % javaStaticClass              "javaStaticClass">
<!ENTITY % javaClassAccess              "javaClassAccess">
<!ENTITY % javaBaseClass                "javaBaseClass">
<!ENTITY % javaImplementedInterface     "javaImplementedInterface">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % javaClass-types-default
    "javaClass | javaInterface | javaMethod | javaField">
<!ENTITY % javaClass-info-types  "%javaClass-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT javaClass   ((%apiName;), (%shortdesc;), (%prolog;)?, (%javaClassDetail;), (%related-links;)?, (%javaClass-info-types;)*)>
<!ATTLIST javaClass       id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT javaClassDetail  ((%javaClassDef;)?, (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST javaClassDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT javaClassDef   ((%javaFinalClass;)?, (%javaAbstractClass;)?, (%javaStaticClass;)?, (%javaClassAccess;)?, (%javaBaseClass;)?, (%javaImplementedInterface;)*) >
<!ATTLIST javaClassDef    spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFinalClass  EMPTY>
<!ATTLIST javaFinalClass  name CDATA #FIXED "final"
                          value CDATA #FIXED "final"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaAbstractClass  EMPTY>
<!ATTLIST javaAbstractClass  name CDATA #FIXED "abstract"
                          value CDATA #FIXED "abstract"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaStaticClass  EMPTY>
<!ATTLIST javaStaticClass  name CDATA #FIXED "static"
                          value CDATA #FIXED "static"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaClassAccess  EMPTY>
<!ATTLIST javaClassAccess  name CDATA #FIXED "access"
                          value (public) #FIXED "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaBaseClass  (#PCDATA)*>
<!ATTLIST javaBaseClass   href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaImplementedInterface  (#PCDATA)*>
<!ATTLIST javaImplementedInterface  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST javaClass   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier javaClass/javaClass ">
<!ATTLIST javaClassDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiClassifierDetail javaClass/javaClassDetail ">
<!ATTLIST javaClassDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiClassifier/apiClassifierDef javaClass/javaClassDef ">
<!ATTLIST javaFinalClass   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier javaClass/javaFinalClass ">
<!ATTLIST javaAbstractClass   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier javaClass/javaAbstractClass ">
<!ATTLIST javaStaticClass   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier javaClass/javaStaticClass ">
<!ATTLIST javaClassAccess   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier javaClass/javaClassAccess ">
<!ATTLIST javaBaseClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier javaClass/javaBaseClass ">
<!ATTLIST javaImplementedInterface   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier javaClass/javaImplementedInterface ">
