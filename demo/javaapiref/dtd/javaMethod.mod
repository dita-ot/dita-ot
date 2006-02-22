<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % javaMethod              "javaMethod">
<!ENTITY % javaMethodDetail        "javaMethodDetail">
<!ENTITY % javaMethodDef           "javaMethodDef">
<!ENTITY % javaFinalMethod         "javaFinalMethod">
<!ENTITY % javaAbstractMethod      "javaAbstractMethod">
<!ENTITY % javaStaticMethod        "javaStaticMethod">
<!ENTITY % javaNativeMethod        "javaNativeMethod">
<!ENTITY % javaSynchronizedMethod  "javaSynchronizedMethod">
<!ENTITY % javaMethodAccess        "javaMethodAccess">
<!ENTITY % javaMethodClass         "javaMethodClass">
<!ENTITY % javaMethodInterface     "javaMethodInterface">
<!ENTITY % javaMethodPrimitive     "javaMethodPrimitive">
<!ENTITY % javaMethodArray         "javaMethodArray">
<!ENTITY % javaReturn              "javaReturn">
<!ENTITY % javaVoid                "javaVoid">
<!ENTITY % javaConstructorDef      "javaConstructorDef">
<!ENTITY % javaParam               "javaParam">
<!ENTITY % javaException           "javaException">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % javaMethod-types-default  "no-topic-nesting">
<!ENTITY % javaMethod-info-types     "%javaMethod-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT javaMethod   ( (%apiName;), (%shortdesc;), (%prolog;)?, (%javaMethodDetail;), (%related-links;)?, (%javaMethod-info-types;)* )>
<!ATTLIST javaMethod      id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT javaMethodDetail  ((%javaMethodDef;|%javaConstructorDef;), (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST javaMethodDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT javaMethodDef   ((%javaFinalMethod;)?, (%javaAbstractMethod;)?, (%javaStaticMethod;)?, (%javaNativeMethod;)?, (%javaSynchronizedMethod;)?, (%javaMethodAccess;)?, (%javaReturn;|%javaVoid;), (%javaParam;)*, (%javaException;)*) >
<!ATTLIST javaMethodDef    spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaConstructorDef   ((%javaMethodAccess;)?, (%javaParam;)*, (%javaException;)*) >
<!ATTLIST javaConstructorDef  spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaReturn     ((%javaMethodClass;|%javaMethodInterface;|%javaMethodPrimitive;), (%javaMethodArray;)*, (%apiDefNote;)?) >
<!ATTLIST javaReturn      keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaParam      ((%javaMethodClass;|%javaMethodInterface;|%javaMethodPrimitive;),  (%javaMethodArray;)*, (%apiItemName;), (%apiDefNote;)?) >
<!ATTLIST javaParam       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaException  ((%javaMethodClass;), (%apiDefNote;)?) >
<!ATTLIST javaException   keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaFinalMethod  EMPTY>
<!ATTLIST javaFinalMethod  name CDATA #FIXED "final"
                          value CDATA #FIXED "final"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaAbstractMethod  EMPTY>
<!ATTLIST javaAbstractMethod  name CDATA #FIXED "abstract"
                          value CDATA #FIXED "abstract"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaStaticMethod  EMPTY>
<!ATTLIST javaStaticMethod  name CDATA #FIXED "static"
                          value CDATA #FIXED "static"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaNativeMethod  EMPTY>
<!ATTLIST javaNativeMethod  name CDATA #FIXED "native"
                          value CDATA #FIXED "native"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaSynchronizedMethod  EMPTY>
<!ATTLIST javaSynchronizedMethod  name CDATA #FIXED "synchronized"
                          value CDATA #FIXED "synchronized"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT javaMethodAccess  EMPTY>
<!ATTLIST javaMethodAccess  name CDATA #FIXED "access"
                          value (public | protected | private) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaMethodClass  (#PCDATA)*>
<!ATTLIST javaMethodClass  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaMethodInterface  (#PCDATA)*>
<!ATTLIST javaMethodInterface  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaMethodPrimitive  EMPTY>
<!ATTLIST javaMethodPrimitive  name CDATA #FIXED "type"
                          value ( boolean | byte | char | double | float | int | long | short ) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaMethodArray  EMPTY>
<!ATTLIST javaMethodArray  name CDATA "arraysize"
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT javaVoid  EMPTY>
<!ATTLIST javaVoid  name CDATA #FIXED "void"
                          value CDATA #FIXED "void"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST javaMethod   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiOperation/apiOperation javaMethod/javaMethod ">
<!ATTLIST javaMethodDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiOperation/apiOperationDetail javaMethod/javaMethodDetail ">
<!ATTLIST javaMethodDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiOperationDef javaMethod/javaMethodDef ">
<!ATTLIST javaFinalMethod   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaFinalMethod ">
<!ATTLIST javaAbstractMethod   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaAbstractMethod ">
<!ATTLIST javaStaticMethod   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaStaticMethod ">
<!ATTLIST javaNativeMethod   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaNativeMethod ">
<!ATTLIST javaSynchronizedMethod   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaSynchronizedMethod ">
<!ATTLIST javaMethodAccess   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaMethodAccess ">
<!ATTLIST javaReturn   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiReturn javaMethod/javaReturn ">
<!ATTLIST javaParam   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiParam javaMethod/javaParam ">
<!ATTLIST javaException   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiEvent javaMethod/javaException ">
<!ATTLIST javaMethodClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiOperation/apiOperationClassifier javaMethod/javaMethodClass ">
<!ATTLIST javaMethodInterface   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiOperation/apiOperationClassifier javaMethod/javaMethodInterface ">
<!ATTLIST javaMethodPrimitive   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiType apiOperation/apiType javaMethod/javaMethodPrimitive ">
<!ATTLIST javaMethodArray   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiArray apiOperation/apiArray javaMethod/javaMethodArray ">
<!ATTLIST javaVoid   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier javaMethod/javaVoid ">
<!ATTLIST javaConstructorDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiConstructorDef javaMethod/javaConstructorDef ">
