<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Variables DTD                                     -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Variable Reference Type v0.6.0//EN"
      Delivered as file "cxxVariable.dtd"                              -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Variables                       -->
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
<!ENTITY % cxxVariable                                  "cxxVariable">
<!ENTITY % cxxVariableDetail                            "cxxVariableDetail">
<!ENTITY % cxxVariableDefinition                        "cxxVariableDefinition">

<!ENTITY % cxxVariableAccessSpecifier                   "cxxVariableAccessSpecifier">
<!ENTITY % cxxVariableDeclaredType                      "cxxVariableDeclaredType">
<!ENTITY % cxxVariableScopedName                        "cxxVariableScopedName">
<!ENTITY % cxxVariablePrototype                         "cxxVariablePrototype">
<!ENTITY % cxxVariableNameLookup                        "cxxVariableNameLookup">

<!ENTITY % cxxVariableReimplemented                     "cxxVariableReimplemented">

<!ENTITY % cxxVariableStorageClassSpecifierExtern       "cxxVariableStorageClassSpecifierExtern">
<!ENTITY % cxxVariableStorageClassSpecifierStatic       "cxxVariableStorageClassSpecifierStatic">
<!ENTITY % cxxVariableStorageClassSpecifierMutable      "cxxVariableStorageClassSpecifierMutable">
<!ENTITY % cxxVariableConst                             "cxxVariableConst">
<!ENTITY % cxxVariableVolatile                          "cxxVariableVolatile">

<!-- Location information -->
<!ENTITY % cxxVariableAPIItemLocation                   "cxxVariableAPIItemLocation">
<!ENTITY % cxxVariableDeclarationFile                   "cxxVariableDeclarationFile">
<!ENTITY % cxxVariableDeclarationFileLine               "cxxVariableDeclarationFileLine">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxVariable-types-default  "no-topic-nesting">
<!ENTITY % cxxVariable-info-types     "%cxxVariable-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT cxxVariable       ( (%apiName;), (%shortdesc;)?, (%prolog;)?, (%cxxVariableDetail;), (%related-links;)?, ( %cxxVariable-info-types;)* )>
<!ATTLIST cxxVariable    id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxVariableDetail  ((%cxxVariableDefinition;), (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST cxxVariableDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT cxxVariableDefinition   (
                                    (%cxxVariableAccessSpecifier;)?,
                                    (%cxxVariableStorageClassSpecifierExtern;)?,
                                    (%cxxVariableStorageClassSpecifierStatic;)?,
                                    (%cxxVariableStorageClassSpecifierMutable;)?,
                                    (%cxxVariableConst;)?,
                                    (%cxxVariableVolatile;)?,

                                    (%cxxVariableDeclaredType;)?,                                   

                                    (%cxxVariableScopedName;)?,
                                    (%cxxVariablePrototype;)?,
                                    (%cxxVariableNameLookup;)?,

                                    (%cxxVariableReimplemented;)?,
                                   
                                    (%cxxVariableAPIItemLocation;)?
                                   )
>
<!ATTLIST cxxVariableDefinition   spectitle CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableAccessSpecifier  EMPTY>
<!ATTLIST cxxVariableAccessSpecifier  name CDATA #FIXED "access"
                                      value (public|protected|private) "public"
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableDeclaredType  (
                                        #PCDATA
                                        | %apiRelation;
                                    )*
>
<!ATTLIST cxxVariableDeclaredType    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableScopedName   (#PCDATA)*>
<!ATTLIST cxxVariableScopedName     href CDATA #IMPLIED
                                    keyref CDATA #IMPLIED
                                    type   CDATA  #IMPLIED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariablePrototype   (#PCDATA)*>
<!ATTLIST cxxVariablePrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableNameLookup   (#PCDATA)*>
<!ATTLIST cxxVariableNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableReimplemented  (#PCDATA)*>
<!ATTLIST cxxVariableReimplemented href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!-- Storage class specifiers and other qualifiers. -->
<!ELEMENT cxxVariableStorageClassSpecifierExtern  EMPTY>
<!ATTLIST cxxVariableStorageClassSpecifierExtern  name CDATA #FIXED "extern"
                          value CDATA #FIXED "extern"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableStorageClassSpecifierStatic  EMPTY>
<!ATTLIST cxxVariableStorageClassSpecifierStatic  name CDATA #FIXED "static"
                          value CDATA #FIXED "static"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableStorageClassSpecifierMutable  EMPTY>
<!ATTLIST cxxVariableStorageClassSpecifierMutable  name CDATA #FIXED "mutable"
                          value CDATA #FIXED "mutable"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableConst  EMPTY>
<!ATTLIST cxxVariableConst  name CDATA #FIXED "const"
                          value CDATA #FIXED "const"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableVolatile EMPTY>
<!ATTLIST cxxVariableVolatile  name CDATA #FIXED "volatile"
                          value CDATA #FIXED "volatile"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- Location information -->
<!ELEMENT cxxVariableAPIItemLocation   (
                                            %cxxVariableDeclarationFile;,
                                            %cxxVariableDeclarationFileLine;
                                        )
>
<!ATTLIST cxxVariableAPIItemLocation    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableDeclarationFile  EMPTY>
<!ATTLIST cxxVariableDeclarationFile  name CDATA #FIXED "filePath"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxVariableDeclarationFileLine  EMPTY>
<!ATTLIST cxxVariableDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- ============ Class attributes for type ancestry ============ -->

<!ATTLIST cxxVariable   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiValue/apiValue cxxVariable/cxxVariable ">
<!ATTLIST cxxVariableDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiValue/apiValueDetail cxxVariable/cxxVariableDetail ">
<!ATTLIST cxxVariableDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiValue/apiValueDef cxxVariable/cxxVariableDefinition ">

<!ATTLIST cxxVariableAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableAccessSpecifier ">
<!ATTLIST cxxVariableDeclaredType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxVariable/cxxVariableDeclaredType ">    
<!ATTLIST cxxVariableScopedName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiValue/apiItemName cxxVariable/cxxVariableScopedName ">   
<!ATTLIST cxxVariableNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxVariable/cxxVariableNameLookup ">   
<!ATTLIST cxxVariablePrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxVariable/cxxVariablePrototype ">

<!ATTLIST cxxVariableStorageClassSpecifierExtern   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier  cxxVariable/cxxVariableStorageClassSpecifierExtern ">
<!ATTLIST cxxVariableStorageClassSpecifierStatic   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableStorageClassSpecifierStatic ">
<!ATTLIST cxxVariableStorageClassSpecifierMutable   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableStorageClassSpecifierMutable ">

<!ATTLIST cxxVariableConst   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableConst ">
<!ATTLIST cxxVariableVolatile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableVolatile ">
        
<!ATTLIST cxxVariableAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxVariable/cxxVariableAPIItemLocation">    
<!ATTLIST cxxVariableDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableDeclarationFile ">
<!ATTLIST cxxVariableDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxVariable/cxxVariableDeclarationFileLine ">

<!-- Relation elements -->
<!ATTLIST cxxVariableReimplemented    %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiRelation cxxVariable/cxxVariableReimplemented ">
 
 
