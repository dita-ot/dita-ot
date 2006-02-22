<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!ENTITY % apiMap      "apiMap">
<!ENTITY % apiItemRef  "apiItemRef">

<!ELEMENT apiMap         ((%topicmeta;)?, (%apiItemRef;)*, (%reltable;)*)>
<!ATTLIST apiMap          title     CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
             translate  (yes | no)                        #IMPLIED
             xml:lang   NMTOKEN                           #IMPLIED
             %arch-atts;
                          domains    CDATA "&included-domains;"
>

<!ELEMENT apiItemRef ((%topicmeta;)?, (%topicref;)*, (%apiItemRef;)*)>
<!ATTLIST apiItemRef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     "apiRef"
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

<!ATTLIST apiMap %global-atts;
    class CDATA "- map/map apiMap/apiMap ">
<!ATTLIST apiItemRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef ">
