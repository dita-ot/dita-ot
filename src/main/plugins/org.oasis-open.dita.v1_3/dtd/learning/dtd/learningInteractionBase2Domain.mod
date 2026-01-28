<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Interaction Base 2 Domain             -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                         -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Interaction Base 2 Domain//EN" -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Domain                    -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2014                                         -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2014.                    -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!-- ============================================================= -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % lcInteractionBase2
                       "lcInteractionBase2"                          >
<!ENTITY % lcInteractionLabel2
                       "lcInteractionLabel2"                         >
<!ENTITY % lcQuestionBase2
                       "lcQuestionBase2"                             >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Learning interaction base 2     -->
<!ENTITY % lcInteractionBase2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestionBase2;),
                         (%basic.block; |
                          %basic.ph; |
                          %data.elements.incl; |
                          %foreign.unknown.incl; |
                          %txt.incl;)*)"
>
<!ENTITY % lcInteractionBase2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInteractionBase2 %lcInteractionBase2.content;>
<!ATTLIST  lcInteractionBase2 %lcInteractionBase2.attributes;>


<!--                    LONG NAME: Learning interaction question base -->
<!ENTITY % lcInteractionLabel2.content
                       "(%title.cnt;)*"
>
<!ENTITY % lcInteractionLabel2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInteractionLabel2 %lcInteractionLabel2.content;>
<!ATTLIST  lcInteractionLabel2 %lcInteractionLabel2.attributes;>


<!--                    LONG NAME: Learning interaction question base 2 -->
<!ENTITY % lcQuestionBase2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcQuestionBase2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcQuestionBase2 %lcQuestionBase2.content;>
<!ATTLIST  lcQuestionBase2 %lcQuestionBase2.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  lcInteractionBase2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 ">
<!ATTLIST  lcInteractionLabel2 %global-atts;  class CDATA "+ topic/p   learningInteractionBase2-d/lcInteractionLabel2 ">
<!ATTLIST  lcQuestionBase2 %global-atts;  class CDATA "+ topic/div   learningInteractionBase2-d/lcQuestionBase2 ">

<!-- ================== End of DITA Learning Interaction Base 2 Domain ==================== -->
 