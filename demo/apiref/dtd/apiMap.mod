<!--
 | (C) Copyright IBM Corporation 2005, 2009. All Rights Reserved.
 *-->

<!ENTITY % apiMap      "apiMap">
<!ENTITY % apiItemRef  "apiItemRef">

<!ELEMENT apiMap         ((%topicmeta;)?, (%apiItemRef;)*, (%reltable;)*)>
<!ATTLIST apiMap          title     CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
                          %localization-atts;
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
  collection-type    (choice|unordered|sequence|family | -dita-use-conref-target) #IMPLIED
  type         CDATA     "apiRef"
  scope       (local | peer | external | -dita-use-conref-target) #IMPLIED
  locktitle   (yes|no|-dita-use-conref-target)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none | -dita-use-conref-target) #IMPLIED
  toc         (yes|no | -dita-use-conref-target)   #IMPLIED
  print       (yes|no | -dita-use-conref-target)   #IMPLIED
  search      (yes|no | -dita-use-conref-target)   #IMPLIED
  chunk        CDATA     #IMPLIED
  %select-atts;
>

<!ATTLIST apiMap %global-atts;
    class CDATA "- map/map apiMap/apiMap ">
<!ATTLIST apiItemRef %global-atts;
    class CDATA "- map/topicref apiMap/apiItemRef ">
