<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Function DTD                                      -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Function Reference Type v0.6.0//EN"
      Delivered as file "cxxFunction.dtd"                              -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Functions                       -->
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
<!ENTITY % cxxFunction                                  "cxxFunction">
<!ENTITY % cxxFunctionDetail                            "cxxFunctionDetail">
<!ENTITY % cxxFunctionDefinition                        "cxxFunctionDefinition">

<!ENTITY % cxxFunctionDeclaredType                      "cxxFunctionDeclaredType">
<!ENTITY % cxxFunctionReturnType                        "cxxFunctionReturnType">
<!ENTITY % cxxFunctionScopedName                        "cxxFunctionScopedName">
<!ENTITY % cxxFunctionPrototype                         "cxxFunctionPrototype">
<!ENTITY % cxxFunctionNameLookup                        "cxxFunctionNameLookup">
<!ENTITY % cxxFunctionReimplemented                     "cxxFunctionReimplemented">

<!-- Parameters -->
<!ENTITY % cxxFunctionParameters                        "cxxFunctionParameters">
<!ENTITY % cxxFunctionParameter                         "cxxFunctionParameter">
<!ENTITY % cxxFunctionParameterDeclaredType             "cxxFunctionParameterDeclaredType">
<!ENTITY % cxxFunctionParameterDeclarationName          "cxxFunctionParameterDeclarationName">
<!ENTITY % cxxFunctionParameterDefinitionName           "cxxFunctionParameterDefinitionName">
<!ENTITY % cxxFunctionParameterDefaultValue             "cxxFunctionParameterDefaultValue">

<!-- Storage class specifiers and other qualifiers. -->
<!ENTITY % cxxFunctionAccessSpecifier                   "cxxFunctionAccessSpecifier">
<!ENTITY % cxxFunctionStorageClassSpecifierExtern       "cxxFunctionStorageClassSpecifierExtern">
<!ENTITY % cxxFunctionStorageClassSpecifierStatic       "cxxFunctionStorageClassSpecifierStatic">
<!ENTITY % cxxFunctionStorageClassSpecifierMutable      "cxxFunctionStorageClassSpecifierMutable">
<!ENTITY % cxxFunctionConst                             "cxxFunctionConst">
<!ENTITY % cxxFunctionVolatile                          "cxxFunctionVolatile">

<!ENTITY % cxxFunctionExplicit                          "cxxFunctionExplicit">
<!ENTITY % cxxFunctionInline                            "cxxFunctionInline">
<!ENTITY % cxxFunctionVirtual                           "cxxFunctionVirtual">
<!ENTITY % cxxFunctionPureVirtual                       "cxxFunctionPureVirtual">
<!ENTITY % cxxFunctionConstructor                       "cxxFunctionConstructor">
<!ENTITY % cxxFunctionDestructor                        "cxxFunctionDestructor">

<!-- Location information -->
<!ENTITY % cxxFunctionAPIItemLocation                   "cxxFunctionAPIItemLocation">
<!ENTITY % cxxFunctionDeclarationFile                   "cxxFunctionDeclarationFile">
<!ENTITY % cxxFunctionDeclarationFileLine               "cxxFunctionDeclarationFileLine">
<!ENTITY % cxxFunctionDefinitionFile                    "cxxFunctionDefinitionFile">
<!ENTITY % cxxFunctionDefinitionFileLineStart           "cxxFunctionDefinitionFileLineStart">
<!ENTITY % cxxFunctionDefinitionFileLineEnd             "cxxFunctionDefinitionFileLineEnd">

<!-- Templates  -->
<!ENTITY % cxxFunctionTemplateParameters            	"cxxFunctionTemplateParameters">
<!ENTITY % cxxFunctionTemplateParameter            		"cxxFunctionTemplateParameter">
<!ENTITY % cxxFunctionTemplateParameterType        		"cxxFunctionTemplateParameterType">

<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxFunction-types-default  "no-topic-nesting">
<!ENTITY % cxxFunction-info-types     "%cxxFunction-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT cxxFunction   ( (%apiName;), (%shortdesc;)?, (%prolog;)?, (%cxxFunctionDetail;), (%related-links;)?, (%cxxFunction-info-types;)* )>
<!ATTLIST cxxFunction      id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxFunctionDetail  (%cxxFunctionDefinition;, (%apiDesc;)?, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST cxxFunctionDetail  %id-atts;
                              translate (yes|no) #IMPLIED
                              xml:lang NMTOKEN #IMPLIED
                              outputclass CDATA #IMPLIED>

<!ELEMENT cxxFunctionDefinition   (
                                    (%cxxFunctionAccessSpecifier;)?,
                                    (%cxxFunctionStorageClassSpecifierExtern;)?,
                                    (%cxxFunctionStorageClassSpecifierStatic;)?,
                                    (%cxxFunctionStorageClassSpecifierMutable;)?,
                                    (%cxxFunctionConst;)?,
                                    (%cxxFunctionExplicit;)?,
                                    (%cxxFunctionInline;)?,
                                    (%cxxFunctionVirtual;)?,
                                    (%cxxFunctionPureVirtual;)?,
                                    (%cxxFunctionConstructor;)?,
                                    (%cxxFunctionDestructor;)?,

                                    (%cxxFunctionDeclaredType;)?,
                                    (%cxxFunctionReturnType;)?,

                                    (%cxxFunctionScopedName;)?,
                                    (%cxxFunctionPrototype;)?,
                                    (%cxxFunctionNameLookup;)?,

                                    (%cxxFunctionReimplemented;)?,
									
									(%cxxFunctionTemplateParameters;)?,
                                    (%cxxFunctionParameters;)?,

                                    (%apiDefNote;)?,

                                    (%cxxFunctionAPIItemLocation;)?
                                   )
>
<!ATTLIST cxxFunctionDefinition    spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionAccessSpecifier  EMPTY>
<!ATTLIST cxxFunctionAccessSpecifier  name CDATA #FIXED "access"
                                             value (public|protected|private) "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDeclaredType   (
                                        #PCDATA
                                        | %apiRelation;
                                     )*
>
<!ATTLIST cxxFunctionDeclaredType    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionScopedName   (#PCDATA)*>
<!ATTLIST cxxFunctionScopedName     href CDATA #IMPLIED
                                    keyref CDATA #IMPLIED
                                    type   CDATA  #IMPLIED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionPrototype   (#PCDATA)*>
<!ATTLIST cxxFunctionPrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionNameLookup   (#PCDATA)*>
<!ATTLIST cxxFunctionNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionReimplemented  (#PCDATA)*>
<!ATTLIST cxxFunctionReimplemented href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!-- Function Parameters -->
<!ELEMENT cxxFunctionParameters   (%cxxFunctionParameter;)* >
<!ATTLIST cxxFunctionParameters    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionParameter  (
                                    %cxxFunctionParameterDeclaredType;,
                                    (%cxxFunctionParameterDeclarationName;)?,
                                    (%cxxFunctionParameterDefinitionName;)?,
                                    (%cxxFunctionParameterDefaultValue;)?,
                                    (%apiDefNote;)?
                                )
>
<!ATTLIST cxxFunctionParameter  %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionParameterDeclaredType  (
                                                #PCDATA
                                                | %apiRelation;
                                            )*
>
<!ATTLIST cxxFunctionParameterDeclaredType  %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionParameterDeclarationName  (#PCDATA)*>
<!ATTLIST cxxFunctionParameterDeclarationName  %univ-atts;
                                                outputclass CDATA #IMPLIED
>


<!ELEMENT cxxFunctionParameterDefinitionName  (#PCDATA)*>
<!ATTLIST cxxFunctionParameterDefinitionName  %univ-atts;
                                                outputclass CDATA #IMPLIED
>

<!-- TODO: This encloses PCDATA but linkifyTextDITA() is called. -->
<!ELEMENT cxxFunctionParameterDefaultValue  (
                                                #PCDATA
                                                | %apiRelation;
                                            )*
>
<!ATTLIST cxxFunctionParameterDefaultValue  %univ-atts;
                                            outputclass CDATA #IMPLIED
>


<!ELEMENT cxxFunctionReturnType     (
                                        (%cxxFunctionDeclaredType;),
                                     (%apiDefNote;)?
                                    )
>
<!ATTLIST cxxFunctionReturnType      keyref CDATA #IMPLIED
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!-- Storage class specifiers and other qualifiers. -->
<!ELEMENT cxxFunctionStorageClassSpecifierExtern  EMPTY>
<!ATTLIST cxxFunctionStorageClassSpecifierExtern  name CDATA #FIXED "extern"
                          value CDATA #FIXED "extern"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionStorageClassSpecifierStatic  EMPTY>
<!ATTLIST cxxFunctionStorageClassSpecifierStatic  name CDATA #FIXED "static"
                          value CDATA #FIXED "static"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionStorageClassSpecifierMutable  EMPTY>
<!ATTLIST cxxFunctionStorageClassSpecifierMutable  name CDATA #FIXED "mutable"
                          value CDATA #FIXED "mutable"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionConst  EMPTY>
<!ATTLIST cxxFunctionConst  name CDATA #FIXED "const"
                          value CDATA #FIXED "const"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionVolatile EMPTY>
<!ATTLIST cxxFunctionVolatile  name CDATA #FIXED "volatile"
                          value CDATA #FIXED "volatile"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionExplicit  EMPTY>
<!ATTLIST cxxFunctionExplicit  name CDATA #FIXED "explicit"
                          value CDATA #FIXED "explicit"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionInline  EMPTY>
<!ATTLIST cxxFunctionInline  name CDATA #FIXED "inline"
                          value CDATA #FIXED "inline"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionConstructor  EMPTY>
<!ATTLIST cxxFunctionConstructor  name CDATA #FIXED "constructor"
                          value CDATA #FIXED "constructor"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDestructor  EMPTY>
<!ATTLIST cxxFunctionDestructor  name CDATA #FIXED "destructor"
                          value CDATA #FIXED "destructor"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionVirtual  EMPTY>
<!ATTLIST cxxFunctionVirtual  name CDATA #FIXED "virtual"
                          value CDATA #FIXED "virtual"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionPureVirtual  EMPTY>
<!ATTLIST cxxFunctionPureVirtual  name CDATA #FIXED "pure virtual"
                          value CDATA #FIXED "pure virtual"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Location information -->
<!ELEMENT cxxFunctionAPIItemLocation   (
                                            %cxxFunctionDeclarationFile;,
                                            %cxxFunctionDeclarationFileLine;,
                                            (%cxxFunctionDefinitionFile;)?,
                                            (%cxxFunctionDefinitionFileLineStart;)?,
                                            (%cxxFunctionDefinitionFileLineEnd;)?
                                        )
>
<!ATTLIST cxxFunctionAPIItemLocation    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDeclarationFile  EMPTY>
<!ATTLIST cxxFunctionDeclarationFile  name CDATA #FIXED "filePath"
                                      value CDATA #REQUIRED
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDeclarationFileLine  EMPTY>
<!ATTLIST cxxFunctionDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDefinitionFile  EMPTY>
<!ATTLIST cxxFunctionDefinitionFile  name CDATA #FIXED "filePath"
                                      value CDATA #REQUIRED
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDefinitionFileLineStart  EMPTY>
<!ATTLIST cxxFunctionDefinitionFileLineStart  name CDATA #FIXED "lineNumber"
                                                value CDATA #REQUIRED
                                                %univ-atts;
                                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionDefinitionFileLineEnd  EMPTY>
<!ATTLIST cxxFunctionDefinitionFileLineEnd  name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- Templates -->
<!ELEMENT cxxFunctionTemplateParameters   (%cxxFunctionTemplateParameter;)+ >
<!ATTLIST cxxFunctionTemplateParameters    %univ-atts;
                                           outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionTemplateParameter   	( %cxxFunctionTemplateParameterType;,
											(%apiDefNote;)?
																			)
>
<!ATTLIST cxxFunctionTemplateParameter    %univ-atts;
											outputclass CDATA #IMPLIED
>

<!ELEMENT cxxFunctionTemplateParameterType   (#PCDATA | %apiRelation;)*>
<!ATTLIST cxxFunctionTemplateParameterType    %univ-atts;
											outputclass CDATA #IMPLIED
>

<!-- ============ Class attributes for type ancestry ============ -->

<!ATTLIST cxxFunction   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiOperation/apiOperation cxxFunction/cxxFunction ">
<!ATTLIST cxxFunctionDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiOperation/apiOperationDetail cxxFunction/cxxFunctionDetail ">
<!ATTLIST cxxFunctionDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiOperationDef cxxFunction/cxxFunctionDefinition ">
    
<!ATTLIST cxxFunctionScopedName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiOperation/apiItemName cxxFunction/cxxFunctionScopedName ">   
<!ATTLIST cxxFunctionAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionAccessSpecifier ">
<!ATTLIST cxxFunctionNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionNameLookup ">   
<!ATTLIST cxxFunctionPrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionPrototype ">     
<!ATTLIST cxxFunctionReimplemented   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiOperation/apiRelation cxxFunction/cxxFunctionReimplemented ">
    
<!-- Type information -->
<!ATTLIST cxxFunctionDeclaredType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionDeclaredType ">    
<!ATTLIST cxxFunctionReturnType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionReturnType ">     

<!-- Operation qualifiers -->
<!ATTLIST cxxFunctionStorageClassSpecifierExtern   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionStorageClassSpecifierExtern ">
<!ATTLIST cxxFunctionStorageClassSpecifierStatic   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionStorageClassSpecifierStatic ">
<!ATTLIST cxxFunctionStorageClassSpecifierMutable   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionStorageClassSpecifierMutable ">
<!ATTLIST cxxFunctionConst   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionConst ">
<!ATTLIST cxxFunctionVolatile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionVolatile ">
<!ATTLIST cxxFunctionExplicit   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionExplicit ">
<!ATTLIST cxxFunctionInline   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionInline ">
<!ATTLIST cxxFunctionConstructor   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionConstructor ">
<!ATTLIST cxxFunctionDestructor   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDestructor ">
<!ATTLIST cxxFunctionVirtual   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionVirtual ">
<!ATTLIST cxxFunctionPureVirtual   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionPureVirtual ">
    
<!-- Location elements -->
<!ATTLIST cxxFunctionAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionAPIItemLocation ">
<!ATTLIST cxxFunctionDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDeclarationFile ">
<!ATTLIST cxxFunctionDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDeclarationFileLine ">
<!ATTLIST cxxFunctionDefinitionFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDefinitionFile ">
<!ATTLIST cxxFunctionDefinitionFileLineStart   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDefinitionFileLineStart ">
<!ATTLIST cxxFunctionDefinitionFileLineEnd   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxFunction/cxxFunctionDefinitionFileLineEnd ">
        
    
<!-- Parameter elements -->
<!ATTLIST cxxFunctionParameters   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionParameters "> 
<!ATTLIST cxxFunctionParameter   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionParameter ">     
<!ATTLIST cxxFunctionParameterDeclarationName   %global-atts;
    class  CDATA "-  topic/keyword reference/keyword apiRef/apiItemName apiOperation/apiItemName cxxFunction/cxxFunctionParameterDeclarationName ">
<!ATTLIST cxxFunctionParameterDefinitionName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiOperation/apiItemName cxxFunction/cxxFunctionParameterDefinitionName ">
<!ATTLIST cxxFunctionParameterDefaultValue   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiData apiOperation/apiData cxxFunction/cxxFunctionParameterDefaultValue ">   
<!ATTLIST cxxFunctionParameterDeclaredType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionParameterDeclaredType ">    
    
<!-- Templates -->
<!ATTLIST cxxFunctionTemplateParameters   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionTemplateParameters ">
<!ATTLIST cxxFunctionTemplateParameter   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionTemplateParameter ">
<!ATTLIST cxxFunctionTemplateParameterType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxFunction/cxxFunctionTemplateParameterType ">
