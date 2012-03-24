<!-- ================================================================= -->
<!--                    HEADER                                         -->
<!-- ================================================================= -->
<!--  MODULE:    C++ Typedefs DTD                                      -->
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
PUBLIC "-//NOKIA//DTD DITA C++ API Typedef Reference Type v0.6.0//EN"
      Delivered as file "cxxTypedef.dtd"                               -->
 
<!-- ================================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)         -->
<!--                                                                   -->
<!-- PURPOSE:    C++ API Reference for Typedefs                        -->
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
<!ENTITY % cxxTypedef                                  "cxxTypedef">
<!ENTITY % cxxTypedefDetail                            "cxxTypedefDetail">
<!ENTITY % cxxTypedefDefinition                        "cxxTypedefDefinition">

<!ENTITY % cxxTypedefAccessSpecifier                   "cxxTypedefAccessSpecifier">
<!ENTITY % cxxTypedefDeclaredType                      "cxxTypedefDeclaredType">
<!ENTITY % cxxTypedefScopedName                        "cxxTypedefScopedName">
<!ENTITY % cxxTypedefPrototype                         "cxxTypedefPrototype">
<!ENTITY % cxxTypedefNameLookup                        "cxxTypedefNameLookup">

<!ENTITY % cxxTypedefReimplemented                     "cxxTypedefReimplemented">

<!ENTITY % cxxTypedefAPIItemLocation                   "cxxTypedefAPIItemLocation">
<!ENTITY % cxxTypedefDeclarationFile                   "cxxTypedefDeclarationFile">
<!ENTITY % cxxTypedefDeclarationFileLine               "cxxTypedefDeclarationFileLine">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % cxxTypedef-types-default  "no-topic-nesting">
<!ENTITY % cxxTypedef-info-types     "%cxxTypedef-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT cxxTypedef       ( (%apiName;), (%shortdesc;)?, (%prolog;)?, (%cxxTypedefDetail;), (%related-links;)?, ( %cxxTypedef-info-types;)* )>
<!ATTLIST cxxTypedef    id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT cxxTypedefDetail  ((%cxxTypedefDefinition;), (%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST cxxTypedefDetail  %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT cxxTypedefDefinition   (
                                    (%cxxTypedefAccessSpecifier;)?,

                                    (%cxxTypedefDeclaredType;)?,

                                    (%cxxTypedefScopedName;)?,
                                    (%cxxTypedefPrototype;)?,
                                    (%cxxTypedefNameLookup;)?,
                                    (%cxxTypedefReimplemented;)?,
                                   
                                    (%cxxTypedefAPIItemLocation;)?
                                   )
>
<!ATTLIST cxxTypedefDefinition    spectitle CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefAccessSpecifier  EMPTY>
<!ATTLIST cxxTypedefAccessSpecifier  name CDATA #FIXED "access"
                                             value (public | protected | private) "public"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefDeclaredType  (
                                        #PCDATA
                                        | %apiRelation;
                                    )*
>
<!ATTLIST cxxTypedefDeclaredType    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefScopedName   (#PCDATA)*>
<!ATTLIST cxxTypedefScopedName     href CDATA #IMPLIED
                                    keyref CDATA #IMPLIED
                                    type   CDATA  #IMPLIED
                                    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefPrototype   (#PCDATA)*>
<!ATTLIST cxxTypedefPrototype    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefNameLookup   (#PCDATA)*>
<!ATTLIST cxxTypedefNameLookup    %univ-atts;
                                    outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefReimplemented  (#PCDATA)*>
<!ATTLIST cxxTypedefReimplemented href CDATA #IMPLIED
                                      keyref CDATA #IMPLIED
                                      type   CDATA  #IMPLIED
                                      %univ-atts;
                                      format        CDATA   #IMPLIED
                                      scope (local | peer | external) #IMPLIED
                                      outputclass CDATA #IMPLIED
>


<!-- Location information -->
<!ELEMENT cxxTypedefAPIItemLocation   (
                                            %cxxTypedefDeclarationFile;,
                                            %cxxTypedefDeclarationFileLine;
                                        )
>
<!ATTLIST cxxTypedefAPIItemLocation    %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefDeclarationFile  EMPTY>
<!ATTLIST cxxTypedefDeclarationFile  name CDATA #FIXED "filePath"
                                        value CDATA #REQUIRED
                                        %univ-atts;
                                        outputclass CDATA #IMPLIED
>

<!ELEMENT cxxTypedefDeclarationFileLine  EMPTY>
<!ATTLIST cxxTypedefDeclarationFileLine   name CDATA #FIXED "lineNumber"
                                            value CDATA #REQUIRED
                                            %univ-atts;
                                            outputclass CDATA #IMPLIED
>

<!-- ============ Class attributes for type ancestry ============ -->

<!ATTLIST cxxTypedef   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiValue/apiValue cxxTypedef/cxxTypedef ">
<!ATTLIST cxxTypedefDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiValue/apiValueDetail cxxTypedef/cxxTypedefDetail ">
<!ATTLIST cxxTypedefDefinition   %global-atts;
    class  CDATA "- topic/section reference/section apiRef/apiDef apiValue/apiValueDef cxxTypedef/cxxTypedefDefinition ">

<!ATTLIST cxxTypedefAccessSpecifier   %global-atts;                                                      
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxTypedef/cxxTypedefAccessSpecifier "> 


<!ATTLIST cxxTypedefAPIItemLocation   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxTypedef/cxxTypedefAPIItemLocation">    
<!ATTLIST cxxTypedefDeclarationFile   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxTypedef/cxxTypedefDeclarationFile ">
<!ATTLIST cxxTypedefDeclarationFileLine   %global-atts;
    class  CDATA "- topic/state reference/state apiRef/apiQualifier apiValue/apiQualifier cxxTypedef/cxxTypedefDeclarationFileLine ">

<!ATTLIST cxxTypedefDeclaredType   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxTypedef/cxxTypedefDeclaredType ">    
<!ATTLIST cxxTypedefScopedName   %global-atts;
    class  CDATA "- topic/keyword reference/keyword apiRef/apiItemName apiValue/apiItemName cxxTypedef/cxxTypedefScopedName ">   
<!ATTLIST cxxTypedefNameLookup   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxTypedef/cxxTypedefNameLookup ">   
<!ATTLIST cxxTypedefPrototype   %global-atts;
    class  CDATA "- topic/ph reference/ph apiRef/apiDefItem apiValue/apiDefItem cxxTypedef/cxxTypedefPrototype ">
  
<!ATTLIST cxxTypedefReimplemented   %global-atts;
    class  CDATA "- topic/xref reference/xref apiRef/apiRelation apiValue/apiRelation cxxTypedef/cxxTypedefReimplemented ">
