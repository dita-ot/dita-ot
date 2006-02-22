<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % apiRef            "apiRef">
<!ENTITY % apiName           "apiName">
<!ENTITY % apiDetail         "apiDetail">
<!ENTITY % apiSyntax         "apiSyntax">
<!ENTITY % apiSyntaxText     "apiSyntaxText">
<!ENTITY % apiSyntaxItem     "apiSyntaxItem">
<!ENTITY % apiItemName       "apiItemName">
<!ENTITY % apiDefNote        "apiDefNote">
<!ENTITY % apiDef            "apiDef">
<!ENTITY % apiDesc           "apiDesc">
<!ENTITY % apiImpl           "apiImpl">
<!ENTITY % apiDefItem        "apiDefItem">
<!ENTITY % apiQualifier      "apiQualifier">
<!ENTITY % apiRelation       "apiRelation">
<!ENTITY % apiType           "apiType">
<!ENTITY % apiArray          "apiArray">
<!ENTITY % apiData           "apiData">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % apiRef-info-types  "%info-types;">
<!ENTITY included-domains     "">


<!-- ============ Content Models ============ -->
<!ENTITY % apiDefinition.cnt
    "%apiQualifier; | %apiRelation; | %apiType; | %apiArray; | %apiData; | %apiDefNote; | %state; | %keyword; | %ph;">


<!-- ============ Element definitions ============ -->

<!ELEMENT apiRef         ((%apiName;), (%shortdesc;), (%prolog;)?, (%apiDetail;), (%related-links;)?, ( %apiRef-info-types;)*)>
<!ATTLIST apiRef          id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT apiName        (%title.cnt;)* >
<!ATTLIST apiName         %id-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiDetail      ((%apiSyntax;)*, (%apiDef;)*, (%apiDesc;)*, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST apiDetail       %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT apiSyntax      ((%apiSyntaxText;)+, (%apiSyntaxItem;)*) >
<!ATTLIST apiSyntax       spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiSyntaxText  (#PCDATA|%apiRelation;|%apiType;|%apiItemName;|%apiData;|%keyword;|%txt.incl;)*>
<!ATTLIST apiSyntaxText   %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>

<!ELEMENT apiSyntaxItem  ((%apiItemName;)*,(%apiDefNote;)?)>
<!ATTLIST apiSyntaxItem   %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiItemName    (#PCDATA)*>
<!ATTLIST apiItemName     keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiDefNote     (%ph.cnt;)*>
<!ATTLIST apiDefNote      %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiDef         (%apiDefinition.cnt;|%apiDefItem;|%apiItemName;)* >
<!ATTLIST apiDef          spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiDefItem     (%apiItemName;|%apiDefinition.cnt;|%apiDefItem;)* >
<!ATTLIST apiDefItem      keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiQualifier   EMPTY>
<!ATTLIST apiQualifier    name CDATA #REQUIRED
                          value CDATA #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiRelation    (#PCDATA)*>
<!ATTLIST apiRelation     href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiType         EMPTY>
<!ATTLIST apiType         name CDATA "type"
                          value CDATA #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiArray        EMPTY>
<!ATTLIST apiArray        name CDATA "arraysize"
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiData        (%ph.cnt;)*>
<!ATTLIST apiData         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiDesc        ( %section.notitle.cnt; )* >
<!ATTLIST apiDesc         spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT apiImpl         ( %section.notitle.cnt; )* >
<!ATTLIST apiImpl         spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- ============ Class ancestry ============ -->
<!ATTLIST apiRef   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef ">
<!ATTLIST apiName   %global-atts;
    class  CDATA "- topic/title reference/title apiRef/apiName ">
<!ATTLIST apiDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail ">
<!ATTLIST apiSyntax   %global-atts;
    class  CDATA "- topic/section reference/refsyn apiRef/apiSyntax ">
<!ATTLIST apiSyntaxText   %global-atts;
    class  CDATA "- topic/pre reference/pre apiRef/apiSyntaxText ">
<!ATTLIST apiSyntaxItem   %global-atts;
    class  CDATA "- topic/p reference/p apiRef/apiSyntaxItem ">
<!ATTLIST apiItemName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName ">
<!ATTLIST apiDefNote   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefNote ">
<!ATTLIST apiDef   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef ">
<!ATTLIST apiDefItem   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem ">
<!ATTLIST apiQualifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier ">
<!ATTLIST apiRelation   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation ">
<!ATTLIST apiType   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiType ">
<!ATTLIST apiArray   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiArray ">
<!ATTLIST apiData   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiData ">
<!ATTLIST apiDesc   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDesc ">
<!ATTLIST apiImpl   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiImpl ">
