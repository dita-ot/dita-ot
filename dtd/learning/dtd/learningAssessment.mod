<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA learningAssessment                           -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Learning Assessment//EN"
      Delivered as file "learningAssessment.mod                    -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Assessment                -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009.              -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->

<!ENTITY % learningAssessment     "learningAssessment">
<!ENTITY % learningAssessmentbody "learningAssessmentbody">

<!ENTITY % learningAssessment-info-types "no-topic-nesting">

<!ENTITY included-domains 
  ""
>

<!--                    LONG NAME: Learning Assessment             -->
<!ENTITY % learningAssessment.content
                        "((%title;),
                          (%titlealts;)?,
                          (%shortdesc; | 
                           %abstract;)?,
                          (%prolog;)?,
                          (%learningAssessmentbody;),
                          (%related-links;)?,
                          (%learningAssessment-info-types;)* )"
>
<!ENTITY % learningAssessment.attributes
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
<!ELEMENT learningAssessment    %learningAssessment.content;>
<!ATTLIST learningAssessment
              %learningAssessment.attributes;
              %arch-atts;
              domains 
                        CDATA
                                  "&included-domains;"
>

<!--                    LONG NAME: Learning Assessment Body        -->
<!ENTITY % learningAssessmentbody.content
                        "((%lcIntro;)?,
                          (%lcObjectives;)?,
                          (%lcDuration;)?,
                          (%lcInteraction;)*,
                          (%section;)*,
                          (%lcSummary;)?)"
>
<!ENTITY % learningAssessmentbody.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT learningAssessmentbody   %learningAssessmentbody.content;>
<!ATTLIST learningAssessmentbody   %learningAssessmentbody.attributes;>

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
 
<!ATTLIST learningAssessment        %global-atts; class CDATA "- topic/topic learningBase/learningBase     learningAssessment/learningAssessment ">
<!ATTLIST learningAssessmentbody    %global-atts; class CDATA "- topic/body  learningBase/learningBasebody learningAssessment/learningAssessmentbody ">




