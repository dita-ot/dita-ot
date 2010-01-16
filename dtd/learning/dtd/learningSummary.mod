<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA learningOverview                             -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Learning Summary//EN"
      Delivered as file "learningSummary.mod                      -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Summary                   -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009               -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->

<!ENTITY % learningSummary "learningSummary">
<!ENTITY % learningSummarybody "learningSummarybody">

<!-- declare the structure and content models -->

<!-- declare the class derivations -->

<!ENTITY % learningSummary-info-types "no-topic-nesting">
<!ENTITY included-domains        "" >

<!ENTITY % learningSummary.content
                        "((%title;),
                          (%titlealts;)?,
                          (%shortdesc; | 
                           %abstract;)?,
                          (%prolog;)?,
                          (%learningSummarybody;),
                          (%related-links;)?,
                          (%learningSummary-info-types;)* )"
>
<!ENTITY % learningSummary.attributes
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
<!ELEMENT learningSummary    %learningSummary.content;>
<!ATTLIST learningSummary
              %learningSummary.attributes;
              %arch-atts;
              domains 
                        CDATA
                                  "&included-domains;"
>


<!ENTITY % learningSummarybody.content
                        "(((%lcSummary;) |
                          (%lcObjectives;) |
                          (%lcReview;) |
                          (%lcNextSteps;) |
                          (%lcResources;) |
                          (%section;))*)"
>
<!ENTITY % learningSummarybody.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT learningSummarybody     %learningSummarybody.content;>
<!ATTLIST learningSummarybody     %learningSummarybody.attributes;>

<!--specialization attributes-->

<!ATTLIST learningSummary        %global-atts; class CDATA "- topic/topic learningBase/learningBase     learningSummary/learningSummary ">
<!ATTLIST learningSummarybody    %global-atts; class CDATA "- topic/body  learningBase/learningBasebody learningSummary/learningSummarybody ">
