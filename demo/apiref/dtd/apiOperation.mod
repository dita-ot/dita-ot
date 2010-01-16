<!--
 | (C) Copyright IBM Corporation 2005, 2009. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % apiOperation            "apiOperation">
<!ENTITY % apiOperationDetail      "apiOperationDetail">
<!ENTITY % apiOperationDef         "apiOperationDef">
<!ENTITY % apiConstructorDef       "apiConstructorDef">
<!ENTITY % apiReturn               "apiReturn">
<!ENTITY % apiParam                "apiParam">
<!ENTITY % apiEvent                "apiEvent">
<!ENTITY % apiOperationDefItem     "apiOperationDefItem">
<!ENTITY % apiOperationClassifier  "apiOperationClassifier">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % apiOperation-types-default
    "apiClassifier | apiOperation | apiValue">
<!ENTITY % apiOperation-info-types  "%apiOperation-types-default;">

<!ENTITY included-domains "">


<!-- ============ Element definitions ============ -->
<!ELEMENT apiOperation   ( (%apiName;), (%shortdesc; | %abstract;), (%prolog;)?, (%apiOperationDetail;), (%related-links;)?, (%apiOperation-info-types;)* )>
<!ATTLIST apiOperation    id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          %localization-atts;
                          %select-atts;
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT apiOperationDetail  (((%apiSyntax;)*|(%apiConstructorDef;)*|(%apiOperationDef;)*), (%apiDesc;)*, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST apiOperationDetail  %id-atts;
                          %localization-atts;
                          outputclass CDATA #IMPLIED>


<!ELEMENT apiOperationDef (%apiReturn;|%apiParam;|%apiEvent;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;|%apiItemName;)* >
<!ATTLIST apiOperationDef  spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiConstructorDef (%apiParam;|%apiEvent;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;|%apiItemName;)* >
<!ATTLIST apiConstructorDef  spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiReturn      (%apiItemName;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;)* >
<!ATTLIST apiReturn       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiParam       (%apiItemName;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;)* >
<!ATTLIST apiParam        keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiEvent       (%apiItemName;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;)* >
<!ATTLIST apiEvent        keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiOperationDefItem  (%apiItemName;|%apiOperationClassifier;|%apiDefinition.cnt;|%apiOperationDefItem;)* >
<!ATTLIST apiOperationDefItem  keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiOperationClassifier  (#PCDATA)*>
<!ATTLIST apiOperationClassifier  href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external | -dita-use-conref-target) #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class ancestry ============ -->
<!ATTLIST apiOperation   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiOperation/apiOperation ">
<!ATTLIST apiOperationDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiOperation/apiOperationDetail ">
<!ATTLIST apiOperationDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiOperationDef ">
<!ATTLIST apiConstructorDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiConstructorDef ">
<!ATTLIST apiReturn   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiReturn ">
<!ATTLIST apiParam   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiParam ">
<!ATTLIST apiEvent   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiEvent ">
<!ATTLIST apiOperationDefItem   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiOperationDefItem ">
<!ATTLIST apiOperationClassifier   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiOperation/apiOperationClassifier ">
