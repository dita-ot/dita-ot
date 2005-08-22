<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Concept                                      -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Concept//EN"
      Delivered as file "concept.mod"                              -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributed    -->
<!--             for Concepts                                      -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005.                    -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ARCHITECTURE ENTITIES                       -->
<!-- ============================================================= -->

<!-- default namespace prefix for DITAArchVersion attribute can be
     overridden through predefinition in the document type shell   -->
<!ENTITY % DITAArchNSPrefix
                       "ditaarch"                                    >

<!-- must be instanced on each topic type                          -->
<!ENTITY % arch-atts "
             xmlns:%DITAArchNSPrefix; 
                        CDATA                              #FIXED
                       'http://dita.oasis-open.org/architecture/2005/'
             %DITAArchNSPrefix;:DITAArchVersion
                        CDATA                              #FIXED
                       '1.0'"                                        >


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->


<!ENTITY % conceptClasses SYSTEM "concept_class.ent"                 >


<!ENTITY % concept-info-types "%info-types;">


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->
 

<!ENTITY % concept     "concept"                                     >
<!ENTITY % conbody     "conbody"                                     >


<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->


<!ENTITY included-domains ""                                         >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Concept                         -->
<!ELEMENT concept       (%title;, (%titlealts;)?, (%shortdesc;)?, 
                         (%prolog;)?, (%conbody;)?, (%related-links;)?,
                         (%concept-info-types;)* )                   >
<!ATTLIST concept        
             id         ID                               #REQUIRED
             conref     CDATA                            #IMPLIED
             %select-atts;
             xml:lang   NMTOKEN                          #IMPLIED
             %arch-atts;
             outputclass 
                        CDATA                            #IMPLIED
             domains    CDATA                "&included-domains;"    >


<!--                    LONG NAME: Concept Body                    -->
<!ELEMENT conbody       ((%body.cnt;)*, (%section;|%example;)* )     >
<!ATTLIST conbody         
             %id-atts;
             translate  (yes | no)                       #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST concept     %global-atts;  class CDATA "- topic/topic concept/concept ">
<!ATTLIST conbody     %global-atts;  class CDATA "- topic/body  concept/conbody ">

 
<!-- ================== End DITA Concept  ======================== -->




