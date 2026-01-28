<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Map Domain                          -->
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
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Map Domain//EN"      -->
<!--       Delivered as file "learningMapDomain.mod"                    -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Map Domain                -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009               -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--                                                               -->
<!--  16 Aug 2009: WEK Added learningContentComponentRef per TC    -->
<!--  07 Dec 2009: RDA combined @id, conref-atts, select-atts, and -->
<!--               localization-atts into a single ref to univ-atts -->
<!--                                                               -->
<!--                                                               -->
<!-- =============================================================  -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % learningGroup
                       "learningGroup"                               >
<!ENTITY % learningObject
                       "learningObject"                              >
<!ENTITY % learningPlanRef
                       "learningPlanRef"                             >
<!ENTITY % learningOverviewRef
                       "learningOverviewRef"                         >
<!ENTITY % learningSummaryRef
                       "learningSummaryRef"                          >
<!ENTITY % learningContentRef
                       "learningContentRef"                          >
<!ENTITY % learningContentComponentRef
                       "learningContentComponentRef"                 >
<!ENTITY % learningPreAssessmentRef
                       "learningPreAssessmentRef"                    >
<!ENTITY % learningPostAssessmentRef
                       "learningPostAssessmentRef"                   >
<!ENTITY % learningGroupMapRef
                       "learningGroupMapRef"                         >
<!ENTITY % learningObjectMapRef
                       "learningObjectMapRef"                        >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % learningDomain-topicref-atts-no-chunk
              "navtitle
                          CDATA
                                    #IMPLIED
               href
                          CDATA
                                    #IMPLIED
               keyref
                          CDATA
                                    #IMPLIED
               keys
                          CDATA
                                    #IMPLIED
               keyscope
                          CDATA
                                    #IMPLIED
               query
                          CDATA
                                    #IMPLIED
               copy-to
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED
               cascade
                          CDATA
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    #IMPLIED
               linking
                          (targetonly |
                           sourceonly |
                           normal |
                           none |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    #IMPLIED
               print
                          (yes |
                           no |
                           printonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    #IMPLIED
               %univ-atts;"
>
<!ENTITY % learningDomain-topicref-atts
              "%learningDomain-topicref-atts-no-chunk;
               chunk
                          CDATA
                                    #IMPLIED"
>
<!ENTITY % learningDomain-mapref-atts
              "%learningDomain-topicref-atts-no-chunk;
               format
                          CDATA
                                    #IMPLIED"
>
<!ENTITY % learningObjectComponent-topicref-atts
              "%learningDomain-topicref-atts-no-chunk;
               chunk
                          CDATA
                                    'to-content'"
>
<!--                    LONG NAME: Learning group                  -->
<!ENTITY % learningGroup.content
                       "((%topicmeta;)?,
                         (%learningPlanRef;)?,
                         (%learningOverviewRef; |
                          %learningPreAssessmentRef;)*,
                         (%learningObject; |
                          %learningObjectMapRef; |
                          %learningGroup; |
                          %learningGroupMapRef;)*,
                         (%learningPostAssessmentRef; |
                          %learningSummaryRef;)*)"
>
<!ENTITY % learningGroup.attributes
              "%learningDomain-topicref-atts;
               collection-type
                          (choice |
                           unordered |
                           sequence |
                           family |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningGroup %learningGroup.content;>
<!ATTLIST  learningGroup %learningGroup.attributes;>


<!--                    LONG NAME: Learning object                 -->
<!ENTITY % learningObject.content
                       "((%topicmeta;)?,
                         (%learningPlanRef;)?,
                         (%learningOverviewRef; |
                          %learningPreAssessmentRef;)*,
                         (%learningContentRef;)+,
                         (%learningPostAssessmentRef; |
                          %learningSummaryRef;)*)"
>
<!ENTITY % learningObject.attributes
              "%learningDomain-topicref-atts;
               collection-type
                          (choice |
                           unordered |
                           sequence |
                           family |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningObject %learningObject.content;>
<!ATTLIST  learningObject %learningObject.attributes;>


<!--                    LONG NAME: Learning plan reference         -->
<!ENTITY % learningPlanRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningPlanRef.attributes
              "%learningObjectComponent-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningPlanRef %learningPlanRef.content;>
<!ATTLIST  learningPlanRef %learningPlanRef.attributes;>


<!--                    LONG NAME: Learning overview reference     -->
<!ENTITY % learningOverviewRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningOverviewRef.attributes
              "%learningObjectComponent-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningOverviewRef %learningOverviewRef.content;>
<!ATTLIST  learningOverviewRef %learningOverviewRef.attributes;>


<!--                    LONG NAME: Learning summary reference      -->
<!ENTITY % learningSummaryRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningSummaryRef.attributes
              "%learningObjectComponent-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningSummaryRef %learningSummaryRef.content;>
<!ATTLIST  learningSummaryRef %learningSummaryRef.attributes;>


<!--                    LONG NAME: Learning content reference      -->
<!ENTITY % learningContentRef.content
                       "((%topicmeta;)?,
                         (%learningContentComponentRef;)*)"
>
<!ENTITY % learningContentRef.attributes
              "%learningDomain-topicref-atts-no-chunk;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED
               chunk
                          CDATA
                                    'to-content'"
>
<!ELEMENT  learningContentRef %learningContentRef.content;>
<!ATTLIST  learningContentRef %learningContentRef.attributes;>


<!--                    LONG NAME: Learning content component reference -->
<!ENTITY % learningContentComponentRef.content
                       "((%topicmeta;)?,
                         (%learningContentComponentRef;)*)"
>
<!ENTITY % learningContentComponentRef.attributes
              "%learningDomain-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningContentComponentRef %learningContentComponentRef.content;>
<!ATTLIST  learningContentComponentRef %learningContentComponentRef.attributes;>


<!--                    LONG NAME: Learning pre-assessment reference -->
<!ENTITY % learningPreAssessmentRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningPreAssessmentRef.attributes
              "%learningObjectComponent-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningPreAssessmentRef %learningPreAssessmentRef.content;>
<!ATTLIST  learningPreAssessmentRef %learningPreAssessmentRef.attributes;>


<!--                    LONG NAME: Learning post-assessment reference -->
<!ENTITY % learningPostAssessmentRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningPostAssessmentRef.attributes
              "%learningObjectComponent-topicref-atts;
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningPostAssessmentRef %learningPostAssessmentRef.content;>
<!ATTLIST  learningPostAssessmentRef %learningPostAssessmentRef.attributes;>


<!--                    LONG NAME: Learning group map reference    -->
<!ENTITY % learningGroupMapRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningGroupMapRef.attributes
              "%learningDomain-mapref-atts;
               type
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningGroupMapRef %learningGroupMapRef.content;>
<!ATTLIST  learningGroupMapRef %learningGroupMapRef.attributes;>


<!--                    LONG NAME: Learning object map reference   -->
<!ENTITY % learningObjectMapRef.content
                       "(%topicmeta;)?"
>
<!ENTITY % learningObjectMapRef.attributes
              "%learningDomain-mapref-atts;
               type
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningObjectMapRef %learningObjectMapRef.content;>
<!ATTLIST  learningObjectMapRef %learningObjectMapRef.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  learningObject %global-atts;  class CDATA "+ map/topicref learningmap-d/learningObject ">
<!ATTLIST  learningGroup %global-atts;  class CDATA "+ map/topicref learningmap-d/learningGroup ">
<!ATTLIST  learningGroupMapRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningGroupMapRef ">
<!ATTLIST  learningObjectMapRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningObjectMapRef ">
<!ATTLIST  learningPlanRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningPlanRef ">
<!ATTLIST  learningOverviewRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningOverviewRef ">
<!ATTLIST  learningContentRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningContentRef ">
<!ATTLIST  learningContentComponentRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningContentComponentRef ">
<!ATTLIST  learningSummaryRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningSummaryRef ">
<!ATTLIST  learningPreAssessmentRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningPreAssessmentRef ">
<!ATTLIST  learningPostAssessmentRef %global-atts;  class CDATA "+ map/topicref learningmap-d/learningPostAssessmentRef ">

<!-- ================== End of DITA Learning Map Domain ==================== -->
 