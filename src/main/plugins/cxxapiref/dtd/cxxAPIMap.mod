<!--
Copyright (c) 2009-2010 Nokia Corporation and/or its subsidiary(-ies).
All rights reserved.

Generated from APIMap.script

-->

<!ENTITY % cxxFileRef "cxxFileRef">
<!ENTITY % cxxClassRef "cxxClassRef">
<!ENTITY % cxxStructRef "cxxStructRef">
<!ENTITY % cxxUnionRef "cxxUnionRef">
<!ENTITY % cxxDefineRef "cxxDefineRef">
<!ENTITY % cxxFunctionRef "cxxFunctionRef">
<!ENTITY % cxxTypedefRef "cxxTypedefRef">
<!ENTITY % cxxVariableRef "cxxVariableRef">
<!ENTITY % cxxEnumerationRef "cxxEnumerationRef">

<!ELEMENT cxxAPIMap      (
                            (%topicmeta;)?,
                            (%topicref; | %cxxFileRef; | %cxxClassRef; | %cxxStructRef; | %cxxUnionRef;)*,
                            (%reltable;)*
                            )>

<!ATTLIST cxxAPIMap       title     CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
                          translate  (yes | no)   #IMPLIED
                          xml:lang   NMTOKEN      #IMPLIED
                          %arch-atts;
                          domains    CDATA "&included-domains;"
>

<!ELEMENT cxxFileRef (
                        (%topicmeta;)?,
                        (%cxxDefineRef;)?,
                        (%cxxEnumerationRef;)?,
                        (%cxxFunctionRef;)?,
                        (%cxxTypedefRef;)?,
                        (%cxxVariableRef;)?,
                        (%topicref;)*
                        )>
<!ATTLIST cxxFileRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxFile"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>


<!ELEMENT cxxClassRef (
                            (%topicmeta;)?,
                            (
                                %cxxClassRef; 
                                | %cxxStructRef; 
                                | %cxxUnionRef;
                            )*,
                            (%topicref;)*)>
<!ATTLIST cxxClassRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxClass"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxStructRef (
                            (%topicmeta;)?,
                            (
                                %cxxClassRef; 
                                | %cxxStructRef; 
                                | %cxxUnionRef;
                            )*,
                            (%topicref;)*)>
<!ATTLIST cxxStructRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxStruct"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxUnionRef (
                            (%topicmeta;)?,
                            (
                                %cxxClassRef; 
                                | %cxxStructRef; 
                                | %cxxUnionRef;
                            )*,
                            (%topicref;)*)>
<!ATTLIST cxxUnionRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxUnion"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxFunctionRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST cxxFunctionRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxFunction"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxVariableRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST cxxVariableRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxVariable"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxDefineRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST cxxDefineRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxDefine"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxEnumerationRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST cxxEnumerationRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxEnumerator"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT cxxTypedefRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST cxxTypedefRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "cxxTypedef"
  scope       (local | peer | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  toc         (yes|no)   #IMPLIED
  print       (yes|no)   #IMPLIED
  search      (yes|no)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>
 
<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST cxxAPIMap %global-atts;
    class CDATA "- map/map apiMap/apiMap cxxAPIMap/cxxAPIMap ">
<!ATTLIST cxxFileRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxFileRef ">
<!ATTLIST cxxClassRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxClassRef ">
<!ATTLIST cxxStructRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxStructRef ">
<!ATTLIST cxxUnionRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxUnionRef ">
<!ATTLIST cxxDefineRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxDefineRef ">
<!ATTLIST cxxFunctionRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxFunctionRef ">
<!ATTLIST cxxTypedefRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxTypedefRef ">
<!ATTLIST cxxVariableRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxVariableRef ">
<!ATTLIST cxxEnumerationRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef cxxAPIMap/cxxEnumerationRef ">
