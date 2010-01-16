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
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Learning Base//EN"
      Delivered as file "learningBase.mod"                         -->

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
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->

<!ENTITY % learningBase "learningBase">
<!ENTITY % learningBasebody "learningBasebody">
<!ENTITY % lcIntro "lcIntro">
<!ENTITY % lcObjectives "lcObjectives">
<!ENTITY % lcObjectivesStem "lcObjectivesStem">
<!ENTITY % lcObjectivesGroup "lcObjectivesGroup">
<!ENTITY % lcObjective "lcObjective">
<!ENTITY % lcAudience "lcAudience">
<!ENTITY % lcDuration "lcDuration">
<!ENTITY % lcTime "lcTime">
<!ENTITY % lcPrereqs "lcPrereqs">
<!ENTITY % lcSummary "lcSummary">
<!ENTITY % lcNextSteps "lcNextSteps">
<!ENTITY % lcReview "lcReview">
<!ENTITY % lcResources "lcResources">
<!ENTITY % lcChallenge "lcChallenge">
<!ENTITY % lcInstruction "lcInstruction">
<!ENTITY % lcInteraction "lcInteraction">


<!-- declare the structure and content models -->

<!ENTITY % learningBase-info-types "%info-types;">
<!ENTITY included-domains    "" >

<!ENTITY % learningBase.content
                       "((%title;),
                         (%titlealts;)?, 
                         (%shortdesc; | 
                          %abstract;)?,
                         (%prolog;)?,
                         (%learningBasebody;)?,
                         (%related-links;)?,
                         (%learningBase-info-types;)* )"
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
<!ELEMENT learningBase    %learningBase.content;>
<!ATTLIST learningBase
              %learningBase.attributes;
              %arch-atts;
              domains 
                        CDATA
                                  "&included-domains;"
>


<!ENTITY % learningBasebody.content
                       "((%lcAudience; |
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
                          %section;)*)"
>
<!ENTITY % learningBasebody.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT learningBasebody    %learningBasebody.content;>
<!ATTLIST learningBasebody    %learningBasebody.attributes;>


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
<!ELEMENT lcIntro    %lcIntro.content;>
<!ATTLIST lcIntro    %lcIntro.attributes;>


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
<!ELEMENT lcObjectives    %lcObjectives.content;>
<!ATTLIST lcObjectives    %lcObjectives.attributes;>


<!ENTITY % lcObjectivesStem.content
                       "(%ph.cnt;)* "
>
<!ENTITY % lcObjectivesStem.attributes
             "%univ-atts; 
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcObjectivesStem    %lcObjectivesStem.content;>
<!ATTLIST lcObjectivesStem    %lcObjectivesStem.attributes;>


<!ENTITY % lcObjectivesGroup.content
                       "((%lcObjective;)+)"
>
<!ENTITY % lcObjectivesGroup.attributes
             "%univ-atts; 
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcObjectivesGroup    %lcObjectivesGroup.content;>
<!ATTLIST lcObjectivesGroup    %lcObjectivesGroup.attributes;>


<!ENTITY % lcObjective.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcObjective.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcObjective    %lcObjective.content;>
<!ATTLIST lcObjective    %lcObjective.attributes;>


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
<!ELEMENT lcAudience    %lcAudience.content;>
<!ATTLIST lcAudience    %lcAudience.attributes;>


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
<!ELEMENT lcDuration    %lcDuration.content;>
<!ATTLIST lcDuration    %lcDuration.attributes;>


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
<!ELEMENT lcTime    %lcTime.content;>
<!ATTLIST lcTime    %lcTime.attributes;>


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
<!ELEMENT lcPrereqs    %lcPrereqs.content;>
<!ATTLIST lcPrereqs    %lcPrereqs.attributes;>


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
<!ELEMENT lcSummary    %lcSummary.content;>
<!ATTLIST lcSummary    %lcSummary.attributes;>


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
<!ELEMENT lcNextSteps    %lcNextSteps.content;>
<!ATTLIST lcNextSteps    %lcNextSteps.attributes;>


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
<!ELEMENT lcReview    %lcReview.content;>
<!ATTLIST lcReview    %lcReview.attributes;>


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
<!ELEMENT lcResources    %lcResources.content;>
<!ATTLIST lcResources    %lcResources.attributes;>


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
<!ELEMENT lcChallenge    %lcChallenge.content;>
<!ATTLIST lcChallenge    %lcChallenge.attributes;>


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
<!ELEMENT lcInstruction    %lcInstruction.content;>
<!ATTLIST lcInstruction    %lcInstruction.attributes;>


<!ENTITY % lcInteraction.content
                       "(%lcInteractionBase;)*"
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
<!ELEMENT lcInteraction    %lcInteraction.content;>
<!ATTLIST lcInteraction    %lcInteraction.attributes;>


<!--specialization attributes-->
<!ATTLIST learningBase       %global-atts; class CDATA "- topic/topic learningBase/learningBase ">
<!ATTLIST learningBasebody   %global-atts; class CDATA "- topic/body learningBase/learningBasebody ">
<!ATTLIST lcIntro            %global-atts; class CDATA "- topic/section learningBase/lcIntro ">
<!ATTLIST lcObjectives       %global-atts; class CDATA "- topic/section learningBase/lcObjectives ">
<!ATTLIST lcObjectivesStem   %global-atts; class CDATA "- topic/ph learningBase/lcObjectivesStem ">
<!ATTLIST lcObjectivesGroup   %global-atts; class CDATA "- topic/ul learningBase/lcObjectivesGroup ">
<!ATTLIST lcObjective        %global-atts; class CDATA "- topic/li learningBase/lcObjective ">
<!ATTLIST lcAudience         %global-atts; class CDATA "- topic/section learningBase/lcAudience ">
<!ATTLIST lcDuration         %global-atts; class CDATA "- topic/section learningBase/lcDuration ">
<!ATTLIST lcTime             %global-atts; class CDATA "- topic/data learningBase/lcTime ">
<!ATTLIST lcPrereqs          %global-atts; class CDATA "- topic/section learningBase/lcPrereqs ">
<!ATTLIST lcSummary          %global-atts; class CDATA "- topic/section learningBase/lcSummary ">
<!ATTLIST lcNextSteps        %global-atts; class CDATA "- topic/section learningBase/lcNextSteps ">
<!ATTLIST lcReview           %global-atts; class CDATA "- topic/section learningBase/lcReview ">
<!ATTLIST lcResources        %global-atts; class CDATA "- topic/section learningBase/lcResources ">
<!ATTLIST lcChallenge        %global-atts; class CDATA "- topic/section learningBase/lcChallenge ">
<!ATTLIST lcInstruction      %global-atts; class CDATA "- topic/section learningBase/lcInstruction ">
<!ATTLIST lcInteraction      %global-atts; class CDATA "- topic/section learningBase/lcInteraction ">
