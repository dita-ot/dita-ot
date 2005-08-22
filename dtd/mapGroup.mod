<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Map Group Domain                             -->
<!--  VERSION:   1.O                                               -->
<!--  DATE:      February 2005                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Map Group Domain//EN"
      Delivered as file "mapgroup.mod"                             -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributed    -->
<!--             for Map Group Domain                              -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005.                    -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->

<!ENTITY % topichead    "topichead"                                  >
<!ENTITY % topicgroup   "topicgroup"                                 >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Topic Head                     -->
<!ELEMENT topichead     ((%topicmeta;)?, 
                          (%topicref; | %navref; | %anchor;)* )      >
<!ATTLIST topichead
             navtitle   CDATA                             #REQUIRED
             id         ID                                #IMPLIED
             conref     CDATA                             #IMPLIED
             %topicref-atts;
             %select-atts;                                           >


<!--                    LONG NAME: Topic Group                     -->
<!ELEMENT topicgroup    ((%topicmeta;)?, 
                         (%topicref; | %navref; | %anchor;)* )       >
<!ATTLIST topicgroup
             id         ID                                #IMPLIED
             conref     CDATA                             #IMPLIED
             %topicref-atts;
             %select-atts;                                           >


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST topichead     %global-atts;  class CDATA "+ map/topicref mapgroup-d/topichead ">
<!ATTLIST topicgroup    %global-atts;  class CDATA "+ map/topicref mapgroup-d/topicgroup ">


<!-- ================== DITA Map Group Domain  =================== -->