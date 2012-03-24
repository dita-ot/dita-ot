<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Enumeration DTD                                   -->
<!--  VERSION:   0.6.0                                                 -->
<!--  DATE:      May 2010                                              -->
<!--                                                                   -->
<!-- ================================================================= -->

<!-- ================================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION                -->
<!--                    TYPICAL INVOCATION                             -->
<!--                                                                   -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//NOKIA//DTD DITA C++ API Enumeration Reference Type v0.6.0//EN"
      Delivered as file "cxxEnumeration.dtd"                           -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Enumation and Enumerators       -->
<!--                                                                   -->
<!-- ORIGINAL CREATION DATE:                                           -->
<!--             November 2009                                         -->
<!--                                                                   -->
<!-- Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). -->
<!-- All rights reserved.                                              -->
<!--                                                                   -->
<!--  Change History (latest at top):                                  -->
<!--  +++++++++++++++++++++++++++++++                                  -->
<!--  2010-02-16 VOG: Updated.                                         -->
<!--  2010-02-10 PaulRoss: Updated.                                    -->
<!--  2009-11-16 PaulRoss: Initial design.                             -->
<!--                                                                   -->
<!-- ================================================================= -->

<!--
Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies).
All rights reserved.
-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % cxxEnumeration                                   "cxxEnumeration">
<!ENTITY % cxxEnumerationDetail                             "cxxEnumerationDetail">
<!ENTITY % cxxEnumerationDefinition                         "cxxEnumerationDefinition">
<!ENTITY % cxxEnumerationAccessSpecifier                    "cxxEnumerationAccessSpecifier">
<!ENTITY % cxxEnumerationScopedName                         "cxxEnumerationScopedName">
<!ENTITY % cxxEnumerationPrototype                          "cxxEnumerationPrototype">
<!ENTITY % cxxEnumerationNameLookup                         "cxxEnumerationNameLookup">
<!ENTITY % cxxEnumerationReimplemented                      "cxxEnumerationReimplemented">
<!ENTITY % cxxEnumerator                                    "cxxEnumerator">
                                                            
<!ENTITY % cxxEnumerators                                    "cxxEnumerators">
<!ENTITY % cxxEnumeratorScopedName                          "cxxEnumeratorScopedName">
<!ENTITY % cxxEnumeratorPrototype                           "cxxEnumeratorPrototype">
<!ENTITY % cxxEnumeratorNameLookup                          "cxxEnumeratorNameLookup">
<!ENTITY % cxxEnumeratorInitialiser                         "cxxEnumeratorInitialiser">

<!-- Location information -->
<!ENTITY % cxxEnumerationAPIItemLocation                    "cxxEnumerationAPIItemLocation">
<!ENTITY % cxxEnumerationDeclarationFile                    "cxxEnumerationDeclarationFile">
<!ENTITY % cxxEnumerationDeclarationFileLine                "cxxEnumerationDeclarationFileLine">
<!ENTITY % cxxEnumerationDefinitionFile                     "cxxEnumerationDefinitionFile">
<!ENTITY % cxxEnumerationDefinitionFileLineStart            "cxxEnumerationDefinitionFileLineStart">
<!ENTITY % cxxEnumerationDefinitionFileLineEnd              "cxxEnumerationDefinitionFileLineEnd">

<!ENTITY % cxxEnumeratorAPIItemLocation                     "cxxEnumeratorAPIItemLocation">
<!ENTITY % cxxEnumeratorDeclarationFile                     "cxxEnumeratorDeclarationFile">
<!ENTITY % cxxEnumeratorDeclarationFileLine                 "cxxEnumeratorDeclarationFileLine">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxEnumeration-types-default  "no-topic-nesting">
<!ENTITY % cxxEnumeration-info-types     "%cxxEnumeration-types-default;">

<!-- TODO: Check this. -->
<!ENTITY % cxxEnumerator-types-default  "no-topic-nesting">
<!ENTITY % cxxEnumerator-info-types     "%cxxEnumerator-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT cxxEnumeration       ( (%apiName;), (%shortdesc;)?, (%prolog;)?, (%cxxEnumerationDetail;), (%related-links;)?, ( %cxxEnumeration-info-types;)* )>
<!ATTLIST cxxEnumeration    id ID #REQUIRED
                              conref CDATA #IMPLIED
                              outputclass CDATA #IMPLIED
                              xml:lang NMTOKEN #IMPLIED
                              %arch-atts;
                              domains CDATA "&included-domains;"
>

<!ELEMENT cxxEnumerationDetail  ((%cxxEnumerationDefinition;), (%apiDesc;)?, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST cxxEnumerationDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT cxxEnumerationDefinition   (
                                    (%cxxEnumerationAccessSpecifier;)?,
                                    (%cxxEnumerationScopedName;)?,
                                    (%cxxEnumerationPrototype;)?,
                                    (%cxxEnumerationNameLookup;)?,
                                    (%cxxEnumerationReimplemented;)?,
                                    (%cxxEnumerators;)?,
                                    (%cxxEnumerationAPIItemLocation;)?
                                   )
>
<!ATTLIST cxxEnumerationDefinition    spectitle CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationAccessSpecifier  EMPTY>
<!ATTLIST cxxEnumerationAccessSpecifier  name CDATA #FIXED "access"
                                             value (public|protected|private) "public"
                                          %univ-atts;
                                          outputclass CDATA #IMPLIED
>

<!-- cxxEnumerationScopedName need keyrefs and it can refer to other types (if not a fundemental type) -->
<!ELEMENT cxxEnumerationScopedName   (#PCDATA)*>
<!ATTLIST cxxEnumerationScopedName     href CDATA #IMPLIED
                                    keyref CDATA #IMPLIED
                                    type   CDATA  #IMPLIED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationPrototype   (#PCDATA)*>
<!ATTLIST cxxEnumerationPrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationNameLookup   (#PCDATA)*>
<!ATTLIST cxxEnumerationNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationReimplemented   (#PCDATA)*>
<!ATTLIST cxxEnumerationReimplemented     href CDATA #IMPLIED
                                        keyref CDATA #IMPLIED
                                        type   CDATA  #IMPLIED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>


<!ELEMENT cxxEnumerationAPIItemLocation   (
                                            %cxxEnumerationDeclarationFile;,
                                            %cxxEnumerationDeclarationFileLine;,
                                            %cxxEnumerationDefinitionFile;,
                                            %cxxEnumerationDefinitionFileLineStart;,
                                            %cxxEnumerationDefinitionFileLineEnd;
                                        )
>
<!ATTLIST cxxEnumerationAPIItemLocation    %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationDeclarationFile  EMPTY>
<!ATTLIST cxxEnumerationDeclarationFile  name CDATA #FIXED "filePath"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationDeclarationFileLine  EMPTY>
<!ATTLIST cxxEnumerationDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationDefinitionFile  EMPTY>
<!ATTLIST cxxEnumerationDefinitionFile  name CDATA #FIXED "filePath"
                                      value CDATA #REQUIRED
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationDefinitionFileLineStart  EMPTY>
<!ATTLIST cxxEnumerationDefinitionFileLineStart  name CDATA #FIXED "lineNumber"
                                                value CDATA #REQUIRED
                                                %univ-atts;
                                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerationDefinitionFileLineEnd  EMPTY>
<!ATTLIST cxxEnumerationDefinitionFileLineEnd  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- Enumerator elements -->
<!ELEMENT cxxEnumerators   (%cxxEnumerator;)* >
<!ATTLIST cxxEnumerators    %univ-atts;
                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumerator     (       (%apiName;),
                                    (%cxxEnumeratorScopedName;)?,
                                    (%cxxEnumeratorPrototype;)?,
                                    (%cxxEnumeratorNameLookup;)?,
                                    (%cxxEnumeratorInitialiser;)?,
                                    (%cxxEnumeratorAPIItemLocation;)?,
                                    (%apiDesc;)?
														)
>

<!ATTLIST cxxEnumerator     spectitle CDATA #IMPLIED
                            %univ-atts;
                            outputclass CDATA #IMPLIED
>

<!-- cxxEnumeratorScopedName need keyrefs and it can refer to other types (if not a fundemental type) -->
<!ELEMENT cxxEnumeratorScopedName   (#PCDATA)*>
<!ATTLIST cxxEnumeratorScopedName     href CDATA #IMPLIED
                                    keyref CDATA #IMPLIED
                                    type   CDATA  #IMPLIED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumeratorPrototype   (#PCDATA)*>
<!ATTLIST cxxEnumeratorPrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumeratorNameLookup   (#PCDATA)*>
<!ATTLIST cxxEnumeratorNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumeratorInitialiser  EMPTY>
<!ATTLIST cxxEnumeratorInitialiser  name CDATA #FIXED "value"
                                    value CDATA #REQUIRED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!-- Enumerator - location information -->
<!ELEMENT cxxEnumeratorAPIItemLocation   (
                                            %cxxEnumeratorDeclarationFile;,
                                            %cxxEnumeratorDeclarationFileLine;
                                        )
>
<!ATTLIST cxxEnumeratorAPIItemLocation    %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumeratorDeclarationFile  EMPTY>
<!ATTLIST cxxEnumeratorDeclarationFile  name CDATA #FIXED "filePath"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxEnumeratorDeclarationFileLine  EMPTY>
<!ATTLIST cxxEnumeratorDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!-- Enumeration -->

<!ATTLIST cxxEnumeration   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiValue/apiValue cxxEnumeration/cxxEnumeration ">
<!ATTLIST cxxEnumerationDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiValue/apiValueDetail cxxEnumeration/cxxEnumerationDetail ">
<!ATTLIST cxxEnumerationDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiValue/apiValueDef cxxEnumeration/cxxEnumerationDefinition ">

<!-- Enumeration elements -->

<!ATTLIST cxxEnumerationAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationAccessSpecifier ">
<!ATTLIST cxxEnumerationAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumerationAPIItemLocation ">

<!ATTLIST cxxEnumerationDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationDeclarationFile ">
<!ATTLIST cxxEnumerationDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationDeclarationFileLine ">

<!ATTLIST cxxEnumerationDefinitionFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationDefinitionFile ">
<!ATTLIST cxxEnumerationDefinitionFileLineStart   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationDefinitionFileLineStart ">
<!ATTLIST cxxEnumerationDefinitionFileLineEnd   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumerationDefinitionFileLineEnd ">
        
<!ATTLIST cxxEnumerationScopedName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiValue/apiItemName cxxEnumeration/cxxEnumerationScopedName ">   
<!ATTLIST cxxEnumerationNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumerationNameLookup ">   
<!ATTLIST cxxEnumerationPrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumerationPrototype ">     


<!-- Enumerators -->

<!ATTLIST cxxEnumerators   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumerators "> 
    
<!ATTLIST cxxEnumerator   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumerator ">


<!-- Enumerator elements -->

<!ATTLIST cxxEnumeratorAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumeratorAPIItemLocation ">
<!ATTLIST cxxEnumeratorDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumeratorDeclarationFile ">
<!ATTLIST cxxEnumeratorDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxEnumeration/cxxEnumeratorDeclarationFileLine ">

<!ATTLIST cxxEnumeratorScopedName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiValue/apiItemName cxxEnumeration/cxxEnumeratorScopedName ">   
<!ATTLIST cxxEnumeratorNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumeratorNameLookup ">   
<!ATTLIST cxxEnumeratorPrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxEnumeration/cxxEnumeratorPrototype ">     
<!ATTLIST cxxEnumeratorInitialiser   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiData apiValue/apiData cxxEnumeration/cxxEnumeratorInitialiser ">     


<!-- Relation elements -->
<!ATTLIST cxxEnumerationReimplemented   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiRelation cxxEnumeration/cxxEnumerationReimplemented ">
