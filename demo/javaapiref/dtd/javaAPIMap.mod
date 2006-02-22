<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!ENTITY % javaAPIMap             "javaAPIMap">
<!ENTITY % javaPackageRef         "javaPackageRef">
<!ENTITY % javaInterfaceRef       "javaInterfaceRef">
<!ENTITY % javaClassRef           "javaClassRef">
<!ENTITY % javaExceptionClassRef  "javaExceptionClassRef">
<!ENTITY % javaErrorClassRef      "javaErrorClassRef">

<!ELEMENT javaAPIMap      ((%topicmeta;)?, (%javaPackageRef;)*, (%reltable;)*)>
<!ATTLIST javaAPIMap      title     CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
             translate  (yes | no)                        #IMPLIED
             xml:lang   NMTOKEN                           #IMPLIED
             %arch-atts;
                          domains    CDATA "&included-domains;"
>

<!ELEMENT javaPackageRef ((%topicmeta;)?, (%topicref;)*, (%javaPackageRef;)*, (%javaInterfaceRef;)*, (%javaClassRef;)*, (%javaExceptionClassRef;)*, (%javaErrorClassRef;)*)>
<!ATTLIST javaPackageRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "javaPackage"
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
<!ELEMENT javaInterfaceRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST javaInterfaceRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "javaInterface"
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
<!ELEMENT javaClassRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST javaClassRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "javaClass"
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
<!ELEMENT javaExceptionClassRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST javaExceptionClassRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "javaClass"
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
<!ELEMENT javaErrorClassRef ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST javaErrorClassRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "javaClass"
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

<!ATTLIST javaAPIMap %global-atts;
    class CDATA "- map/map apiMap/apiMap javaAPIMap/javaAPIMap ">
<!ATTLIST javaPackageRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef javaAPIMap/javaPackageRef ">
<!ATTLIST javaInterfaceRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef javaAPIMap/javaInterfaceRef ">
<!ATTLIST javaClassRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef javaAPIMap/javaClassRef ">
<!ATTLIST javaExceptionClassRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef javaAPIMap/javaExceptionClassRef ">
<!ATTLIST javaErrorClassRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef javaAPIMap/javaErrorClassRef ">
