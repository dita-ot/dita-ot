<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Struct DTD                                        -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Struct Reference Type v0.6.0//EN"
      Delivered as file "cxxStruct.dtd"                                   -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Structs                         -->
<!--                                                                   -->
<!-- ORIGINAL CREATION DATE:                                           -->
<!--             November 2009                                         -->
<!--                                                                   -->
<!-- Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies). -->
<!-- All rights reserved.                                              -->
<!--                                                                   -->
<!--  Change History (latest at top):                                  -->
<!--  +++++++++++++++++++++++++++++++                                  -->
<!--  2010-05-14 PaulRoss: Fixed templates.                            -->
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
<!ENTITY % cxxStruct                             "cxxStruct">
<!ENTITY % cxxStructDetail                       "cxxStructDetail">
<!ENTITY % cxxStructDefinition                   "cxxStructDefinition">

<!ENTITY % cxxStructAbstract                     "cxxStructAbstract">
<!ENTITY % cxxStructAccessSpecifier              "cxxStructAccessSpecifier">

<!ENTITY % cxxStructDerivations                  "cxxStructDerivations">
<!ENTITY % cxxStructDerivation                   "cxxStructDerivation">
<!ENTITY % cxxClassDerivation                    "cxxClassDerivation">

<!-- Templates -->
<!ENTITY % cxxStructTemplateParameters           "cxxStructTemplateParameters">
<!ENTITY % cxxStructTemplateParameter            "cxxStructTemplateParameter">
<!ENTITY % cxxStructTemplateParameterType        "cxxStructTemplateParameterType">

<!-- Derivation -->
<!ENTITY % cxxStructDerivationAccessSpecifier    "cxxStructDerivationAccessSpecifier">
<!ENTITY % cxxStructDerivationVirtual            "cxxStructDerivationVirtual">
<!ENTITY % cxxStructBaseClass                    "cxxStructBaseClass">
<!ENTITY % cxxStructBaseStruct                   "cxxStructBaseStruct">
<!ENTITY % cxxStructBaseUnion                    "cxxStructBaseUnion">
<!ENTITY % cxxStructInherits                     "cxxStructInherits">
<!ENTITY % cxxStructInheritsDetail               "cxxStructInheritsDetail">

<!ENTITY % cxxStructFunctionInherited            "cxxStructFunctionInherited">
<!ENTITY % cxxStructVariableInherited            "cxxStructVariableInherited">
<!ENTITY % cxxStructEnumerationInherited         "cxxStructEnumerationInherited">
<!ENTITY % cxxStructEnumeratorInherited          "cxxStructEnumeratorInherited">

<!-- Nested members -->
<!ENTITY % cxxStructNested                       "cxxStructNested">
<!ENTITY % cxxStructNestedDetail                 "cxxStructNestedDetail">
<!ENTITY % cxxStructNestedClass                  "cxxStructNestedClass">
<!ENTITY % cxxStructNestedStruct                 "cxxStructNestedStruct">
<!ENTITY % cxxStructNestedUnion                  "cxxStructNestedUnion">

<!-- Location elements -->
<!ENTITY % cxxStructAPIItemLocation              "cxxStructAPIItemLocation">
<!ENTITY % cxxStructDeclarationFile              "cxxStructDeclarationFile">
<!ENTITY % cxxStructDeclarationFileLine          "cxxStructDeclarationFileLine">
<!ENTITY % cxxStructDefinitionFile               "cxxStructDefinitionFile">
<!ENTITY % cxxStructDefinitionFileLineStart      "cxxStructDefinitionFileLineStart">
<!ENTITY % cxxStructDefinitionFileLineEnd        "cxxStructDefinitionFileLineEnd">

<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxStruct-types-default
    "cxxStructNested | cxxFunction | cxxDefine | cxxVariable | cxxEnumeration | cxxTypedef">
<!ENTITY % cxxStruct-info-types  "%cxxStruct-types-default;">

<!ENTITY % cxxStructNested-types-default "no-topic-nesting">
<!ENTITY % cxxStructNested-info-types  "%cxxStructNested-types-default;">

<!ENTITY included-domains "">

<!-- ============ Topic specializations ============ -->
                        <!-- (%cxxStruct-info-types;)* -->

<!ELEMENT cxxStruct   (
                        (%apiName;),
                        (%shortdesc;)?,
                        (%prolog;)?,
                        (%cxxStructDetail;),
                        (%related-links;)?,
                        (%cxxStruct-info-types;)*,
                        (%cxxStructInherits;)*
                      )
>
<!ATTLIST cxxStruct       id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxStructDetail  ((%cxxStructDefinition;)?, (%apiDesc;)?, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST cxxStructDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDefinition   (
                                    (%cxxStructAccessSpecifier;)?,
                                    (%cxxStructAbstract;)?,
                                    (%cxxStructDerivations;)?,
                                    (%cxxStructTemplateParameters;)?,
                                    (%cxxStructAPIItemLocation;)
                               )
>
<!ATTLIST cxxStructDefinition    spectitle CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructAccessSpecifier  EMPTY>
<!ATTLIST cxxStructAccessSpecifier  name CDATA #FIXED "access"
                          value (public|protected|private) "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructAbstract  EMPTY>
<!ATTLIST cxxStructAbstract  name CDATA #FIXED "abstract"
                            value CDATA #FIXED "abstract"
                            %univ-atts;
                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDerivations   (%cxxClassDerivation; | %cxxStructDerivation;)+ >
<!ATTLIST cxxStructDerivations    %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDerivation   (
                                    %cxxStructDerivationAccessSpecifier;,
                                    (%cxxStructDerivationVirtual;)*,
                                    (
                                        %cxxStructBaseClass;
                                        | %cxxStructBaseStruct;
                                        | %cxxStructBaseUnion;
                                     )
                               )
>
<!ATTLIST cxxStructDerivation    %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructInherits   (
                                %cxxStructInheritsDetail;
                              )
>

<!ATTLIST cxxStructInherits    %univ-atts;
                                outputclass CDATA #IMPLIED
                                domains CDATA "&included-domains;"
>

<!ELEMENT cxxStructInheritsDetail   (
                                (
                                    %cxxStructFunctionInherited;
                                    | %cxxStructVariableInherited;
                                    | %cxxStructEnumerationInherited;
                                    | %cxxStructEnumeratorInherited;
                                )+
                              )
>

<!ATTLIST cxxStructInheritsDetail    %univ-atts;
                                outputclass CDATA #IMPLIED
                                domains CDATA "&included-domains;"
>

<!ELEMENT cxxStructFunctionInherited  (#PCDATA)*>
<!ATTLIST cxxStructFunctionInherited   href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructVariableInherited  (#PCDATA)*>
<!ATTLIST cxxStructVariableInherited   href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructEnumerationInherited  (#PCDATA)*>
<!ATTLIST cxxStructEnumerationInherited   href CDATA #IMPLIED
                                          keyref CDATA #IMPLIED
                                          type   CDATA  #IMPLIED
                                          %univ-atts;
                                          format        CDATA   #IMPLIED
                                          scope (local | peer | external) #IMPLIED
                                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructEnumeratorInherited  (#PCDATA)*>
<!ATTLIST cxxStructEnumeratorInherited   href CDATA #IMPLIED
                                          keyref CDATA #IMPLIED
                                          type   CDATA  #IMPLIED
                                          %univ-atts;
                                          format        CDATA   #IMPLIED
                                          scope (local | peer | external) #IMPLIED
                                          outputclass CDATA #IMPLIED
>


<!ELEMENT cxxStructDerivationAccessSpecifier  EMPTY>
<!ATTLIST cxxStructDerivationAccessSpecifier  name CDATA #FIXED "access"
                                             value (public | protected | private) "public"
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDerivationVirtual  EMPTY>
<!ATTLIST cxxStructDerivationVirtual  name CDATA #FIXED "virtual"
                                      value CDATA #FIXED "true"
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructBaseClass  (#PCDATA)*>
<!ATTLIST cxxStructBaseClass   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructBaseStruct  (#PCDATA)*>
<!ATTLIST cxxStructBaseStruct   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructBaseUnion  (#PCDATA)*>
<!ATTLIST cxxStructBaseUnion   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!-- Templates-->
<!ELEMENT cxxStructTemplateParameters   (%cxxStructTemplateParameter;)+ >
<!ATTLIST cxxStructTemplateParameters    %univ-atts;
										outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructTemplateParameter   	(%cxxStructTemplateParameterType;,
										(%apiDefNote;)?
										)
>
<!ATTLIST cxxStructTemplateParameter    %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructTemplateParameterType   (#PCDATA | %apiRelation;)*>
<!ATTLIST cxxStructTemplateParameterType    %univ-atts;
											outputclass CDATA #IMPLIED
>

<!-- Location -->
<!ELEMENT cxxStructAPIItemLocation   (
                                        %cxxStructDeclarationFile;,
                                        %cxxStructDeclarationFileLine;,
                                        (%cxxStructDefinitionFile;)?,
                                        (%cxxStructDefinitionFileLineStart;)?,
                                        (%cxxStructDefinitionFileLineEnd;)?
                                     )
>
<!ATTLIST cxxStructAPIItemLocation    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDeclarationFile  EMPTY>
<!ATTLIST cxxStructDeclarationFile  name CDATA #FIXED "filePath"
                                  value CDATA #REQUIRED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDeclarationFileLine  EMPTY>
<!ATTLIST cxxStructDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDefinitionFile  EMPTY>
<!ATTLIST cxxStructDefinitionFile  name CDATA #FIXED "filePath"
                                  value CDATA #REQUIRED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDefinitionFileLineStart  EMPTY>
<!ATTLIST cxxStructDefinitionFileLineStart  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructDefinitionFileLineEnd  EMPTY>
<!ATTLIST cxxStructDefinitionFileLineEnd  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- Nested members -->
<!ELEMENT cxxStructNested (
                        (%cxxStructNestedDetail;),
                        (%cxxStructNested-info-types;)*
                         )
>
<!ATTLIST cxxStructNested  conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxStructNestedDetail  ( (%cxxStructNestedClass;) | (%cxxStructNestedStruct;) | (%cxxStructNestedUnion;) )+>
<!ATTLIST cxxStructNestedDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!ELEMENT cxxStructNestedClass  (#PCDATA)*>
<!ATTLIST cxxStructNestedClass  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructNestedStruct  (#PCDATA)*>
<!ATTLIST cxxStructNestedStruct  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxStructNestedUnion  (#PCDATA)*>
<!ATTLIST cxxStructNestedUnion  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST cxxStruct   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxStruct/cxxStruct ">
<!ATTLIST cxxStructDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiClassifierDetail cxxStruct/cxxStructDetail ">
<!ATTLIST cxxStructDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiClassifier/apiClassifierDef cxxStruct/cxxStructDefinition ">
<!ATTLIST cxxStructAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructAccessSpecifier ">
<!ATTLIST cxxStructAbstract   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructAbstract ">

<!-- Representing inheritance -->
<!ATTLIST cxxStructDerivations   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructDerivations ">
<!ATTLIST cxxStructDerivation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructDerivation ">
    
<!ATTLIST cxxStructDerivationAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDerivationAccessSpecifier ">
<!ATTLIST cxxStructDerivationVirtual   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDerivationVirtual ">
<!ATTLIST cxxStructBaseClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxStruct/cxxStructBaseClass ">
<!ATTLIST cxxStructBaseStruct   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxStruct/cxxStructBaseStruct ">
<!ATTLIST cxxStructBaseUnion   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxStruct/cxxStructBaseUnion ">

<!-- Templates -->
<!ATTLIST cxxStructTemplateParameters   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructTemplateParameters ">
<!ATTLIST cxxStructTemplateParameter   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructTemplateParameter ">
<!ATTLIST cxxStructTemplateParameterType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructTemplateParameterType ">
    
<!-- Nested members  -->
<!ATTLIST cxxStructNested   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxStruct/cxxStructNested ">
<!ATTLIST cxxStructNestedDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiDetail cxxStruct/cxxStructNestedDetail ">    
<!ATTLIST cxxStructNestedClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructNestedClass ">
<!ATTLIST cxxStructNestedStruct   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructNestedStruct ">
<!ATTLIST cxxStructNestedUnion   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructNestedUnion ">

<!-- Location elements -->
<!ATTLIST cxxStructAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxStruct/cxxStructAPIItemLocation ">
<!ATTLIST cxxStructDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDeclarationFile ">
<!ATTLIST cxxStructDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDeclarationFileLine ">
<!ATTLIST cxxStructDefinitionFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDefinitionFile ">
<!ATTLIST cxxStructDefinitionFileLineStart   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDefinitionFileLineStart ">
<!ATTLIST cxxStructDefinitionFileLineEnd   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxStruct/cxxStructDefinitionFileLineEnd ">

<!-- Inheritance sub-topics -->
<!ATTLIST cxxStructInherits   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxStruct/cxxStructInherits ">  
<!ATTLIST cxxStructInheritsDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiDetail cxxStruct/cxxStructInheritsDetail ">    
    
<!ATTLIST cxxStructEnumerationInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructEnumerationInherited ">
<!ATTLIST cxxStructEnumeratorInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructEnumeratorInherited ">
<!ATTLIST cxxStructFunctionInherited    %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructFunctionInherited  ">
<!ATTLIST cxxStructVariableInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxStruct/cxxStructVariableInherited ">  
     
