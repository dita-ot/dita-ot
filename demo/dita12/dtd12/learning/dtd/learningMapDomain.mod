<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Map Domain                          -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      May 2007                                          -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Learning Map Domain//EN"
      Delivered as file "learningMapDomain.mod"                    -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Map Domain                -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2008               -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->
<!ENTITY % learningObject             "learningObject">
<!ENTITY % learningOverviewRef        "learningOverviewRef">
<!ENTITY % learningPlanRef            "learningPlanRef">
<!ENTITY % learningContentRef         "learningContentRef">
<!ENTITY % learningSummaryRef         "learningSummaryRef">
<!ENTITY % learningPreAssessmentRef   "learningPreAssessmentRef">
<!ENTITY % learningPostAssessmentRef  "learningPostAssessmentRef">

<!ENTITY % learningGroup              "learningGroup">

<!-- Attributes that are common to each topicref specialization in this domain -->
<!ENTITY % learningDomain-topicref-atts
             "navtitle
                        CDATA
                                  #IMPLIED
              id
                        ID
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
              query
                        CDATA
                                  #IMPLIED
              %conref-atts;
              copy-to
                        CDATA
                                  #IMPLIED
              outputclass
                        CDATA
                                  #IMPLIED
              scope
                        (external |
                         local | 
                         peer | 
                         -dita-use-conref-target)
                                  #IMPLIED
              linking
                        (targetonly|
                         sourceonly|
                         normal|
                         none | 
                         -dita-use-conref-target)
                                  #IMPLIED
              locktitle
                        (yes|
                         no | 
                         -dita-use-conref-target)
                                  #IMPLIED
              toc
                        (yes|
                         no | 
                         -dita-use-conref-target)
                                  #IMPLIED
              print
                        (yes|
                         no | 
                         -dita-use-conref-target)
                                   #IMPLIED
              search
                        (yes|
                         no | 
                         -dita-use-conref-target)
                                   #IMPLIED
              chunk
                        CDATA
                                  #IMPLIED
              %select-atts;
              %localization-atts;"
>

<!ENTITY % learningGroup.content
                       "((%topicmeta;)?,
                         (%learningPlanRef;)?,
                         ((%learningOverviewRef;) | 
                          (%learningPreAssessmentRef;))*,
                         ((%learningObject;) | 
                          (%learningGroup;))*,
                         ((%learningPostAssessmentRef;) | 
                          (%learningSummaryRef;))* )"
>
<!ENTITY % learningGroup.attributes
             "%learningDomain-topicref-atts;
              collection-type
                        (choice|
                         unordered|
                         sequence|
                         family | 
                         -dita-use-conref-target)
                                   #IMPLIED
              type
                        CDATA
                                  #IMPLIED
              format
                        CDATA
                                  #IMPLIED
">
<!ELEMENT learningGroup    %learningGroup.content;>
<!ATTLIST learningGroup    %learningGroup.attributes;>


<!ENTITY % learningObject.content
                       "((%topicmeta;)?,
                         (%learningPlanRef;)?,
                         ((%learningOverviewRef;) |
                          (%learningPreAssessmentRef;))*,
                         (%learningContentRef;)+,
                         ((%learningPostAssessmentRef;) |
                          (%learningSummaryRef;))*)"
>
<!ENTITY % learningObject.attributes
             "%learningDomain-topicref-atts;
              collection-type
                        (choice|
                         unordered|
                         sequence|
                         family | 
                         -dita-use-conref-target)
                                   #IMPLIED
              type
                        CDATA
                                  #IMPLIED
              format
                        CDATA
                                  #IMPLIED
">
<!ELEMENT learningObject    %learningObject.content;>
<!ATTLIST learningObject    %learningObject.attributes;>


<!ENTITY % learningPlanRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningPlanRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  'learningPlan'
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningPlanRef    %learningPlanRef.content;>
<!ATTLIST learningPlanRef    %learningPlanRef.attributes;>

<!ENTITY % learningOverviewRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningOverviewRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  'learningOverview'
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningOverviewRef    %learningOverviewRef.content;>
<!ATTLIST learningOverviewRef    %learningOverviewRef.attributes;>


<!ENTITY % learningSummaryRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningSummaryRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  'learningSummary'
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningSummaryRef    %learningSummaryRef.content;>
<!ATTLIST learningSummaryRef    %learningSummaryRef.attributes;>


<!ENTITY % learningContentRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningContentRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  #IMPLIED
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningContentRef    %learningContentRef.content;>
<!ATTLIST learningContentRef    %learningContentRef.attributes;>


<!ENTITY % learningPreAssessmentRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningPreAssessmentRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  'learningAssessment'
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningPreAssessmentRef    %learningPreAssessmentRef.content;>
<!ATTLIST learningPreAssessmentRef    %learningPreAssessmentRef.attributes;>


<!ENTITY % learningPostAssessmentRef.content
                       "((%topicmeta;)?)"
>
<!ENTITY % learningPostAssessmentRef.attributes
             "%learningDomain-topicref-atts;
              type
                        CDATA
                                  'learningAssessment'
              format
                        CDATA
                                  'dita'"
>
<!ELEMENT learningPostAssessmentRef    %learningPostAssessmentRef.content;>
<!ATTLIST learningPostAssessmentRef    %learningPostAssessmentRef.attributes;>


<!ATTLIST learningObject %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningObject ">
<!ATTLIST learningGroup %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningGroup ">
<!ATTLIST learningPlanRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningPlanRef ">
<!ATTLIST learningOverviewRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningOverviewRef ">
<!ATTLIST learningContentRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningContentRef ">
<!ATTLIST learningSummaryRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningSummaryRef ">
<!ATTLIST learningPreAssessmentRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningPreAssessmentRef ">
<!ATTLIST learningPostAssessmentRef %global-atts;
    class CDATA "+ map/topicref learningmap-d/learningPostAssessmentRef ">

