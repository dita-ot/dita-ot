<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Interaction Base Domain             -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      Sept 2009                                         -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Interaction Base Domain//EN" -->
<!--       Delivered as file "learningInteractionBaseDomain.mod"                      -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Domain                    -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             Sept 2009                                         -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2009.                    -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!-- ============================================================= -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % lcInteractionBase
                       "lcInteractionBase"                           >
<!ENTITY % lcQuestionBase
                       "lcQuestionBase"                              >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Learning interaction base       -->
<!ENTITY % lcInteractionBase.content
                       "((%title;)?,
                         (%lcQuestionBase;),
                         (%fig.cnt;)*)"
>
<!ENTITY % lcInteractionBase.attributes
              "id
                          NMTOKEN
                                    #REQUIRED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInteractionBase %lcInteractionBase.content;>
<!ATTLIST  lcInteractionBase %lcInteractionBase.attributes;>


<!--                    LONG NAME: Learning interaction question base -->
<!ENTITY % lcQuestionBase.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcQuestionBase.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcQuestionBase %lcQuestionBase.content;>
<!ATTLIST  lcQuestionBase %lcQuestionBase.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  lcInteractionBase %global-atts;  class CDATA "+ topic/fig learningInteractionBase-d/lcInteractionBase ">
<!ATTLIST  lcQuestionBase %global-atts;  class CDATA "+ topic/p   learningInteractionBase-d/lcQuestionBase ">

<!-- ================== End of DITA Learning Interaction Base Domain ==================== -->
 