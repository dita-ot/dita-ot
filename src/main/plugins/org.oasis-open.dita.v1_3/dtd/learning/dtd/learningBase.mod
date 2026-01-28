<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA learningBase                                 -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Base//EN"            -->
<!--       Delivered as file "learningBase.mod"                         -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Base                      -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009.              -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  CHANGE LOG:                                                  -->
<!--                                                               -->
<!--    Sept 2009: WEK: Make learningBasebody optional per         -->
<!--    TC decision.                                               -->
<!-- =============================================================   -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % learningBase
                       "learningBase"                                >
<!ENTITY % learningBasebody
                       "learningBasebody"                            >
<!ENTITY % lcIntro     "lcIntro"                                     >
<!ENTITY % lcObjectives
                       "lcObjectives"                                >
<!ENTITY % lcObjectivesStem
                       "lcObjectivesStem"                            >
<!ENTITY % lcObjectivesGroup
                       "lcObjectivesGroup"                           >
<!ENTITY % lcObjective "lcObjective"                                 >
<!ENTITY % lcAudience  "lcAudience"                                  >
<!ENTITY % lcDuration  "lcDuration"                                  >
<!ENTITY % lcTime      "lcTime"                                      >
<!ENTITY % lcPrereqs   "lcPrereqs"                                   >
<!ENTITY % lcSummary   "lcSummary"                                   >
<!ENTITY % lcNextSteps "lcNextSteps"                                 >
<!ENTITY % lcReview    "lcReview"                                    >
<!ENTITY % lcResources "lcResources"                                 >
<!ENTITY % lcChallenge "lcChallenge"                                 >
<!ENTITY % lcInstruction
                       "lcInstruction"                               >
<!ENTITY % lcInteraction
                       "lcInteraction"                               >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % learningBase-info-types
              "%info-types;"
>
<!--                    LONG NAME: Learning Base                   -->
<!ENTITY % learningBase.content
                       "((%title;),
                         (%titlealts;)?,
                         (%shortdesc; |
                          %abstract;)?,
                         (%prolog;)?,
                         (%learningBasebody;)?,
                         (%related-links;)?,
                         (%learningBase-info-types;)*)"
>
<!ENTITY % learningBase.attributes
              "id
                          ID
                                    #REQUIRED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningBase %learningBase.content;>
<!ATTLIST  learningBase %learningBase.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>


<!--                    LONG NAME: Learning base body              -->
<!ENTITY % learningBasebody.content
                       "(%lcAudience; |
                         %lcChallenge; |
                         %lcDuration; |
                         %lcInstruction; |
                         %lcInteraction; |
                         %lcIntro; |
                         %lcNextSteps; |
                         %lcObjectives; |
                         %lcPrereqs; |
                         %lcResources; |
                         %lcReview; |
                         %lcSummary; |
                         %section;)*"
>
<!ENTITY % learningBasebody.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningBasebody %learningBasebody.content;>
<!ATTLIST  learningBasebody %learningBasebody.attributes;>


<!--                    LONG NAME: Learning intro section          -->
<!ENTITY % lcIntro.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcIntro.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcIntro %lcIntro.content;>
<!ATTLIST  lcIntro %lcIntro.attributes;>


<!--                    LONG NAME: Learning objectives section     -->
<!ENTITY % lcObjectives.content
                       "((%title;)?,
                         (%lcObjectivesStem;)?,
                         (%lcObjectivesGroup;)*)"
>
<!ENTITY % lcObjectives.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcObjectives %lcObjectives.content;>
<!ATTLIST  lcObjectives %lcObjectives.attributes;>


<!--                    LONG NAME: Learning objectives stem        -->
<!ENTITY % lcObjectivesStem.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcObjectivesStem.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcObjectivesStem %lcObjectivesStem.content;>
<!ATTLIST  lcObjectivesStem %lcObjectivesStem.attributes;>


<!--                    LONG NAME: Learning objectives group       -->
<!ENTITY % lcObjectivesGroup.content
                       "((%data; |
                          %data-about;)*,
                         (%lcObjective;)+)"
>
<!ENTITY % lcObjectivesGroup.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcObjectivesGroup %lcObjectivesGroup.content;>
<!ATTLIST  lcObjectivesGroup %lcObjectivesGroup.attributes;>


<!--                    LONG NAME: Learning objective              -->
<!ENTITY % lcObjective.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcObjective.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcObjective %lcObjective.content;>
<!ATTLIST  lcObjective %lcObjective.attributes;>


<!--                    LONG NAME: Audience                        -->
<!ENTITY % lcAudience.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcAudience.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAudience %lcAudience.content;>
<!ATTLIST  lcAudience %lcAudience.attributes;>


<!--                    LONG NAME: Duration                        -->
<!ENTITY % lcDuration.content
                       "((%title;)?,
                         (%lcTime;)?)"
>
<!ENTITY % lcDuration.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcDuration %lcDuration.content;>
<!ATTLIST  lcDuration %lcDuration.attributes;>


<!--                    LONG NAME: Time to complete                -->
<!ENTITY % lcTime.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcTime.attributes
              "name
                          CDATA
                                    'lcTime'
               datatype
                          CDATA
                                    'TimeValue'
               value
                          CDATA
                                    #REQUIRED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcTime %lcTime.content;>
<!ATTLIST  lcTime %lcTime.attributes;>


<!--                    LONG NAME: Prerequisites                   -->
<!ENTITY % lcPrereqs.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcPrereqs.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPrereqs %lcPrereqs.content;>
<!ATTLIST  lcPrereqs %lcPrereqs.attributes;>


<!--                    LONG NAME: Summary section                 -->
<!ENTITY % lcSummary.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcSummary.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSummary %lcSummary.content;>
<!ATTLIST  lcSummary %lcSummary.attributes;>


<!--                    LONG NAME: Next steps section              -->
<!ENTITY % lcNextSteps.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcNextSteps.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcNextSteps %lcNextSteps.content;>
<!ATTLIST  lcNextSteps %lcNextSteps.attributes;>


<!--                    LONG NAME: Review sections                 -->
<!ENTITY % lcReview.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcReview.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcReview %lcReview.content;>
<!ATTLIST  lcReview %lcReview.attributes;>


<!--                    LONG NAME: Related resources               -->
<!ENTITY % lcResources.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcResources.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcResources %lcResources.content;>
<!ATTLIST  lcResources %lcResources.attributes;>


<!--                    LONG NAME: Learning challenge              -->
<!ENTITY % lcChallenge.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcChallenge.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcChallenge %lcChallenge.content;>
<!ATTLIST  lcChallenge %lcChallenge.attributes;>


<!--                    LONG NAME: Learning instruction section    -->
<!ENTITY % lcInstruction.content
                       "(%section.cnt;)*"
>
<!ENTITY % lcInstruction.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInstruction %lcInstruction.content;>
<!ATTLIST  lcInstruction %lcInstruction.attributes;>


<!--                    LONG NAME: Interactions                    -->
<!ENTITY % lcInteraction.content
                       "(%lcInteractionBase; |
                         %lcInteractionBase2;)*"
>
<!ENTITY % lcInteraction.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInteraction %lcInteraction.content;>
<!ATTLIST  lcInteraction %lcInteraction.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  learningBase %global-atts;  class CDATA "- topic/topic learningBase/learningBase ">
<!ATTLIST  learningBasebody %global-atts;  class CDATA "- topic/body learningBase/learningBasebody ">
<!ATTLIST  lcIntro      %global-atts;  class CDATA "- topic/section learningBase/lcIntro ">
<!ATTLIST  lcObjectives %global-atts;  class CDATA "- topic/section learningBase/lcObjectives ">
<!ATTLIST  lcObjectivesStem %global-atts;  class CDATA "- topic/ph learningBase/lcObjectivesStem ">
<!ATTLIST  lcObjectivesGroup %global-atts;  class CDATA "- topic/ul learningBase/lcObjectivesGroup ">
<!ATTLIST  lcObjective  %global-atts;  class CDATA "- topic/li learningBase/lcObjective ">
<!ATTLIST  lcAudience   %global-atts;  class CDATA "- topic/section learningBase/lcAudience ">
<!ATTLIST  lcDuration   %global-atts;  class CDATA "- topic/section learningBase/lcDuration ">
<!ATTLIST  lcTime       %global-atts;  class CDATA "- topic/data learningBase/lcTime ">
<!ATTLIST  lcPrereqs    %global-atts;  class CDATA "- topic/section learningBase/lcPrereqs ">
<!ATTLIST  lcSummary    %global-atts;  class CDATA "- topic/section learningBase/lcSummary ">
<!ATTLIST  lcNextSteps  %global-atts;  class CDATA "- topic/section learningBase/lcNextSteps ">
<!ATTLIST  lcReview     %global-atts;  class CDATA "- topic/section learningBase/lcReview ">
<!ATTLIST  lcResources  %global-atts;  class CDATA "- topic/section learningBase/lcResources ">
<!ATTLIST  lcChallenge  %global-atts;  class CDATA "- topic/section learningBase/lcChallenge ">
<!ATTLIST  lcInstruction %global-atts;  class CDATA "- topic/section learningBase/lcInstruction ">
<!ATTLIST  lcInteraction %global-atts;  class CDATA "- topic/section learningBase/lcInteraction ">

<!-- ================== End of DITA learningBase ==================== -->
 