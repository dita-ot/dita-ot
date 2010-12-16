<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Define DTD                                      -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Define Reference Type v0.6.0//EN"
      Delivered as file "cxxDefine.dtd"                              -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Defines                       -->
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
<!--  2010-02-10 PaulRoss: Updated.                                   -->
<!--  2009-11-16 PaulRoss: Initial design.                             -->
<!--                                                                   -->
<!-- ================================================================= -->

<!--
Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies).
All rights reserved.
-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % cxxDefine                                  "cxxDefine">
<!ENTITY % cxxDefineDetail                            "cxxDefineDetail">
<!ENTITY % cxxDefineDefinition                        "cxxDefineDefinition">

<!ENTITY % cxxDefinePrototype                         "cxxDefinePrototype">
<!ENTITY % cxxDefineNameLookup                        "cxxDefineNameLookup">
<!ENTITY % cxxDefineReimplemented                     "cxxDefineReimplemented">

<!-- Parameters -->
<!ENTITY % cxxDefineParameters                        "cxxDefineParameters">
<!ENTITY % cxxDefineParameter                         "cxxDefineParameter">
<!ENTITY % cxxDefineParameterDeclarationName          "cxxDefineParameterDeclarationName">

<!ENTITY % cxxDefineAccessSpecifier                   "cxxDefineAccessSpecifier">

<!-- Location information -->
<!ENTITY % cxxDefineAPIItemLocation                   "cxxDefineAPIItemLocation">
<!ENTITY % cxxDefineDeclarationFile                   "cxxDefineDeclarationFile">
<!ENTITY % cxxDefineDeclarationFileLine               "cxxDefineDeclarationFileLine">

<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxDefine-types-default  "no-topic-nesting">
<!ENTITY % cxxDefine-info-types     "%cxxDefine-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT cxxDefine   ( (%apiName;), (%shortdesc;)?, (%prolog;)?, (%cxxDefineDetail;), (%related-links;)?, (%cxxDefine-info-types;)* )>
<!ATTLIST cxxDefine      id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxDefineDetail  (%cxxDefineDefinition;, (%apiDesc;)?, (%example; | %section; | %apiImpl;)*)>
<!ATTLIST cxxDefineDetail  %id-atts;
                              translate (yes|no) #IMPLIED
                              xml:lang NMTOKEN #IMPLIED
                              outputclass CDATA #IMPLIED>

<!ELEMENT cxxDefineDefinition   (
                                    (%cxxDefineAccessSpecifier;)?,

                                    (%cxxDefinePrototype;)?,
                                    (%cxxDefineNameLookup;)?,
                                    
                                    (%cxxDefineReimplemented;)?,

                                    (%cxxDefineParameters;)?,

                                    (%cxxDefineAPIItemLocation;)?
                                   )
>
<!ATTLIST cxxDefineDefinition    spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!ELEMENT cxxDefineAccessSpecifier  EMPTY>
<!ATTLIST cxxDefineAccessSpecifier  name CDATA #FIXED "access"
                                    value CDATA #FIXED "public"
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefinePrototype   (#PCDATA)*>
<!ATTLIST cxxDefinePrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineNameLookup   (#PCDATA)*>
<!ATTLIST cxxDefineNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineReimplemented  (#PCDATA)*>
<!ATTLIST cxxDefineReimplemented href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineParameters   (%cxxDefineParameter;)* >
<!ATTLIST cxxDefineParameters    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineParameter  (
                                    (%cxxDefineParameterDeclarationName;)?,
                                    (%apiDefNote;)?
                                )
>
<!ATTLIST cxxDefineParameter  %univ-atts;
                                outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineParameterDeclarationName  (#PCDATA)*>
<!ATTLIST cxxDefineParameterDeclarationName  %univ-atts;
                                                outputclass CDATA #IMPLIED
>

<!-- Location information -->
<!ELEMENT cxxDefineAPIItemLocation   (%cxxDefineDeclarationFile;, %cxxDefineDeclarationFileLine;)
>
<!ATTLIST cxxDefineAPIItemLocation    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineDeclarationFile  EMPTY>
<!ATTLIST cxxDefineDeclarationFile  name CDATA #FIXED "filePath"
                                      value CDATA #REQUIRED
                                      %univ-atts;
                                      outputclass CDATA #IMPLIED
>

<!ELEMENT cxxDefineDeclarationFileLine  EMPTY>
<!ATTLIST cxxDefineDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST cxxDefine   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiOperation/apiOperation cxxDefine/cxxDefine ">
<!ATTLIST cxxDefineDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiOperation/apiOperationDetail cxxDefine/cxxDefineDetail ">
<!ATTLIST cxxDefineDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiOperation/apiOperationDef cxxDefine/cxxDefineDefinition ">

<!-- Location elements -->
<!ATTLIST cxxDefineAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxDefine/cxxDefineAPIItemLocation ">
<!ATTLIST cxxDefineDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxDefine/cxxDefineDeclarationFile ">
<!ATTLIST cxxDefineDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxDefine/cxxDefineDeclarationFileLine ">

<!ATTLIST cxxDefineAccessSpecifier   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiOperation/apiQualifier cxxDefine/cxxDefineAccessSpecifier ">

<!ATTLIST cxxDefineNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxDefine/cxxDefineNameLookup ">   
<!ATTLIST cxxDefinePrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxDefine/cxxDefinePrototype ">     
<!ATTLIST cxxDefineParameters   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxDefine/cxxDefineParameters "> 
<!ATTLIST cxxDefineParameter   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiOperation/apiDefItem cxxDefine/cxxDefineParameter ">     
<!ATTLIST cxxDefineParameterDeclarationName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiOperation/apiItemName cxxDefine/cxxDefineParameterDeclarationName ">

<!ATTLIST cxxDefineReimplemented    %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiOperation/apiRelation cxxDefine/cxxDefineReimplemented  ">
