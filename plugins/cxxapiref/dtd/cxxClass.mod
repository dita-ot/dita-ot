<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Class DTD                                        -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Class Reference Type v0.6.0//EN"
      Delivered as file "cxxClass.dtd"                                 -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Classes                         -->
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
<!ENTITY % cxxClass                             "cxxClass">
<!ENTITY % cxxClassDetail                       "cxxClassDetail">
<!ENTITY % cxxClassDefinition                   "cxxClassDefinition">

<!ENTITY % cxxClassAbstract                     "cxxClassAbstract">
<!ENTITY % cxxClassAccessSpecifier              "cxxClassAccessSpecifier">

<!ENTITY % cxxClassDerivations                  "cxxClassDerivations">
<!ENTITY % cxxClassDerivation                   "cxxClassDerivation">
<!ENTITY % cxxStructDerivation                  "cxxStructDerivation">

<!-- Templates -->
<!ENTITY % cxxClassTemplateParameters           "cxxClassTemplateParameters">
<!ENTITY % cxxClassTemplateParameter            "cxxClassTemplateParameter">
<!ENTITY % cxxClassTemplateParameterType        "cxxClassTemplateParameterType">

<!-- Derivation -->
<!ENTITY % cxxClassDerivationAccessSpecifier    "cxxClassDerivationAccessSpecifier">
<!ENTITY % cxxClassDerivationVirtual            "cxxClassDerivationVirtual">
<!ENTITY % cxxClassBaseClass                    "cxxClassBaseClass">
<!ENTITY % cxxClassBaseStruct                   "cxxClassBaseStruct">
<!ENTITY % cxxClassBaseUnion                    "cxxClassBaseUnion">
<!ENTITY % cxxClassInherits                     "cxxClassInherits">
<!ENTITY % cxxClassInheritsDetail               "cxxClassInheritsDetail">

<!ENTITY % cxxClassFunctionInherited            "cxxClassFunctionInherited">
<!ENTITY % cxxClassVariableInherited            "cxxClassVariableInherited">
<!ENTITY % cxxClassEnumerationInherited         "cxxClassEnumerationInherited">
<!ENTITY % cxxClassEnumeratorInherited         "cxxClassEnumeratorInherited">

<!-- Nested members -->
<!ENTITY % cxxClassNested                       "cxxClassNested">
<!ENTITY % cxxClassNestedDetail                 "cxxClassNestedDetail">
<!ENTITY % cxxClassNestedClass                  "cxxClassNestedClass">
<!ENTITY % cxxClassNestedStruct                 "cxxClassNestedStruct">
<!ENTITY % cxxClassNestedUnion                  "cxxClassNestedUnion">

<!-- Location elements -->
<!ENTITY % cxxClassAPIItemLocation              "cxxClassAPIItemLocation">
<!ENTITY % cxxClassDeclarationFile              "cxxClassDeclarationFile">
<!ENTITY % cxxClassDeclarationFileLine          "cxxClassDeclarationFileLine">
<!ENTITY % cxxClassDefinitionFile               "cxxClassDefinitionFile">
<!ENTITY % cxxClassDefinitionFileLineStart      "cxxClassDefinitionFileLineStart">
<!ENTITY % cxxClassDefinitionFileLineEnd        "cxxClassDefinitionFileLineEnd">

<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxClass-types-default
    "cxxClassNested | cxxFunction | cxxDefine | cxxVariable | cxxEnumeration | cxxTypedef">
<!ENTITY % cxxClass-info-types  "%cxxClass-types-default;">

<!ENTITY % cxxClassNested-types-default "no-topic-nesting">
<!ENTITY % cxxClassNested-info-types  "%cxxClassNested-types-default;">

<!ENTITY included-domains "">

<!-- ============ Topic specializations ============ -->
                        <!-- (%cxxClass-info-types;)* -->

<!ELEMENT cxxClass   (
                        (%apiName;),
                        (%shortdesc;)?,
                        (%prolog;)?,
                        (%cxxClassDetail;),
                        (%related-links;)?,
                        (%cxxClass-info-types;)*,
                        (%cxxClassInherits;)*
                      )
>
<!ATTLIST cxxClass       id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxClassDetail  ((%cxxClassDefinition;)?, (%apiDesc;)?, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST cxxClassDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDefinition   (
                                    (%cxxClassAccessSpecifier;)?,
                                    (%cxxClassAbstract;)?,
                                    (%cxxClassDerivations;)?,
                                    (%cxxClassTemplateParameters;)?,
                                    (%cxxClassAPIItemLocation;)
                               )
>
<!ATTLIST cxxClassDefinition    spectitle CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassAccessSpecifier  EMPTY>
<!ATTLIST cxxClassAccessSpecifier  name CDATA #FIXED "access"
                          value (public|protected|private) "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassAbstract  EMPTY>
<!ATTLIST cxxClassAbstract  name CDATA #FIXED "abstract"
                            value CDATA #FIXED "abstract"
                            %univ-atts;
                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDerivations   (%cxxClassDerivation; | %cxxStructDerivation;)+ >
<!ATTLIST cxxClassDerivations    %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDerivation   (
                                    %cxxClassDerivationAccessSpecifier;,
                                    (%cxxClassDerivationVirtual;)*,
                                    (
                                        %cxxClassBaseClass;
                                        | %cxxClassBaseStruct;
                                        | %cxxClassBaseUnion;
                                     )
                               )
>
<!ATTLIST cxxClassDerivation    %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassInherits   (
                                %cxxClassInheritsDetail;
                              )
>

<!ATTLIST cxxClassInherits    %univ-atts;
                                outputclass CDATA #IMPLIED
                                domains CDATA "&included-domains;"
>

<!ELEMENT cxxClassInheritsDetail   (
                                (
                                    %cxxClassFunctionInherited;
                                    | %cxxClassVariableInherited;
                                    | %cxxClassEnumerationInherited;
                                    | %cxxClassEnumeratorInherited;
                                )+
                              )
>

<!ATTLIST cxxClassInheritsDetail    %univ-atts;
                                outputclass CDATA #IMPLIED
                                domains CDATA "&included-domains;"
>

<!ELEMENT cxxClassFunctionInherited  (#PCDATA)*>
<!ATTLIST cxxClassFunctionInherited   href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassVariableInherited  (#PCDATA)*>
<!ATTLIST cxxClassVariableInherited   href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassEnumerationInherited  (#PCDATA)*>
<!ATTLIST cxxClassEnumerationInherited   href CDATA #IMPLIED
                                          keyref CDATA #IMPLIED
                                          type   CDATA  #IMPLIED
                                          %univ-atts;
                                          format        CDATA   #IMPLIED
                                          scope (local | peer | external) #IMPLIED
                                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassEnumeratorInherited  (#PCDATA)*>
<!ATTLIST cxxClassEnumeratorInherited   href CDATA #IMPLIED
                                          keyref CDATA #IMPLIED
                                          type   CDATA  #IMPLIED
                                          %univ-atts;
                                          format        CDATA   #IMPLIED
                                          scope (local | peer | external) #IMPLIED
                                          outputclass CDATA #IMPLIED
>


<!ELEMENT cxxClassDerivationAccessSpecifier  EMPTY>
<!ATTLIST cxxClassDerivationAccessSpecifier  name CDATA #FIXED "access"
                                             value (public | protected | private) "public"
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDerivationVirtual  EMPTY>
<!ATTLIST cxxClassDerivationVirtual  name CDATA #FIXED "virtual"
                                      value CDATA #FIXED "true"
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassBaseClass  (#PCDATA)*>
<!ATTLIST cxxClassBaseClass   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassBaseStruct  (#PCDATA)*>
<!ATTLIST cxxClassBaseStruct   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassBaseUnion  (#PCDATA)*>
<!ATTLIST cxxClassBaseUnion   href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!-- Templates-->
<!ELEMENT cxxClassTemplateParameters   (%cxxClassTemplateParameter;)+ >
<!ATTLIST cxxClassTemplateParameters    %univ-atts;
										outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassTemplateParameter   	(%cxxClassTemplateParameterType;,
										(%apiDefNote;)?
										)
>
<!ATTLIST cxxClassTemplateParameter    %univ-atts;
										outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassTemplateParameterType   (#PCDATA | %apiRelation;)*>
<!ATTLIST cxxClassTemplateParameterType    %univ-atts;
											outputclass CDATA #IMPLIED
>

<!-- Location -->
<!ELEMENT cxxClassAPIItemLocation   (
                                        %cxxClassDeclarationFile;,
                                        %cxxClassDeclarationFileLine;,
                                        (%cxxClassDefinitionFile;)?,
                                        (%cxxClassDefinitionFileLineStart;)?,
                                        (%cxxClassDefinitionFileLineEnd;)?
                                     )
>
<!ATTLIST cxxClassAPIItemLocation    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDeclarationFile  EMPTY>
<!ATTLIST cxxClassDeclarationFile  name CDATA #FIXED "filePath"
                                  value CDATA #REQUIRED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDeclarationFileLine  EMPTY>
<!ATTLIST cxxClassDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDefinitionFile  EMPTY>
<!ATTLIST cxxClassDefinitionFile  name CDATA #FIXED "filePath"
                                  value CDATA #REQUIRED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDefinitionFileLineStart  EMPTY>
<!ATTLIST cxxClassDefinitionFileLineStart  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassDefinitionFileLineEnd  EMPTY>
<!ATTLIST cxxClassDefinitionFileLineEnd  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- Nested members -->
<!ELEMENT cxxClassNested (
                        (%cxxClassNestedDetail;),
                        (%cxxClassNested-info-types;)*
                         )
>
<!ATTLIST cxxClassNested  conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxClassNestedDetail  ( (%cxxClassNestedClass;) | (%cxxClassNestedStruct;) | (%cxxClassNestedUnion;) )+>
<!ATTLIST cxxClassNestedDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!ELEMENT cxxClassNestedClass  (#PCDATA)*>
<!ATTLIST cxxClassNestedClass  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassNestedStruct  (#PCDATA)*>
<!ATTLIST cxxClassNestedStruct  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>

<!ELEMENT cxxClassNestedUnion  (#PCDATA)*>
<!ATTLIST cxxClassNestedUnion  href CDATA #IMPLIED
                              keyref CDATA #IMPLIED
                              type   CDATA  #IMPLIED
                              %univ-atts;
                              format        CDATA   #IMPLIED
                              scope (local | peer | external) #IMPLIED
                              outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST cxxClass   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxClass/cxxClass ">
<!ATTLIST cxxClassDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiClassifierDetail cxxClass/cxxClassDetail ">
<!ATTLIST cxxClassDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiClassifier/apiClassifierDef cxxClass/cxxClassDefinition ">
<!ATTLIST cxxClassAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassAccessSpecifier ">
<!ATTLIST cxxClassAbstract   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassAbstract ">

<!-- Representing inheritance -->
<!ATTLIST cxxClassDerivations   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassDerivations ">
<!ATTLIST cxxClassDerivation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassDerivation ">
    
<!ATTLIST cxxClassDerivationAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDerivationAccessSpecifier ">
<!ATTLIST cxxClassDerivationVirtual   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDerivationVirtual ">
<!ATTLIST cxxClassBaseClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxClass/cxxClassBaseClass ">
<!ATTLIST cxxClassBaseStruct   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxClass/cxxClassBaseStruct ">
<!ATTLIST cxxClassBaseUnion   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiBaseClassifier cxxClass/cxxClassBaseUnion ">

<!-- Templates -->
<!ATTLIST cxxClassTemplateParameters   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassTemplateParameters ">
<!ATTLIST cxxClassTemplateParameter   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassTemplateParameter ">
<!ATTLIST cxxClassTemplateParameterType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassTemplateParameterType ">
    
<!-- Nested members  -->
<!ATTLIST cxxClassNested   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxClass/cxxClassNested ">
<!ATTLIST cxxClassNestedDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiDetail cxxClass/cxxClassNestedDetail ">    
<!ATTLIST cxxClassNestedClass   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassNestedClass ">
<!ATTLIST cxxClassNestedStruct   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassNestedStruct ">
<!ATTLIST cxxClassNestedUnion   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassNestedUnion ">

<!-- Location elements -->
<!ATTLIST cxxClassAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiClassifier/apiDefItem cxxClass/cxxClassAPIItemLocation ">
<!ATTLIST cxxClassDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDeclarationFile ">
<!ATTLIST cxxClassDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDeclarationFileLine ">
<!ATTLIST cxxClassDefinitionFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDefinitionFile ">
<!ATTLIST cxxClassDefinitionFileLineStart   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDefinitionFileLineStart ">
<!ATTLIST cxxClassDefinitionFileLineEnd   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiClassifier/apiQualifier cxxClass/cxxClassDefinitionFileLineEnd ">

<!-- Inheritance sub-topics -->
<!ATTLIST cxxClassInherits   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiClassifier/apiClassifier cxxClass/cxxClassInherits ">  
<!ATTLIST cxxClassInheritsDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiClassifier/apiDetail cxxClass/cxxClassInheritsDetail ">    
    
<!ATTLIST cxxClassEnumerationInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassEnumerationInherited ">
<!ATTLIST cxxClassEnumeratorInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassEnumeratorInherited ">
<!ATTLIST cxxClassFunctionInherited    %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassFunctionInherited  ">
<!ATTLIST cxxClassVariableInherited   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiClassifier/apiRelation cxxClass/cxxClassVariableInherited ">  
     
