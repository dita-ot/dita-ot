<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA learningContent                              -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Learning Content//EN"
      Delivered as file "learningContent.mod                       -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Content                   -->
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

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % learningContent "learningContent">
<!ENTITY % learningContentbody "learningContentbody">

<!-- declare the structure and content models -->

<!-- declare the class derivations -->

<!ENTITY % learningContent-info-types "no-topic-nesting">
<!ENTITY included-domains    "" >

<!ENTITY % learningContent.content
                       "((%title;),
                         (%titlealts;)?,
                         (%shortdesc; | 
                          %abstract;)?,
                         (%prolog;)?,
                         (%learningContentbody;),
                         (%related-links;)?,
                         (%learningContent-info-types;)* )"
>
<!ENTITY % learningContent.attributes
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
<!ELEMENT learningContent    %learningContent.content;>
<!ATTLIST learningContent
              %learningContent.attributes;
              %arch-atts;
              domains 
                        CDATA
                                  "&included-domains;"
>


<!ENTITY % learningContentbody.content
                       "(((%lcIntro;) |
                          (%lcDuration;) |
                          (%lcObjectives;))*,
                         (%lcChallenge;)?,
                         (%lcInstruction;)?,
                         (%section;)*)  "
>
<!ENTITY % learningContentbody.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT learningContentbody    %learningContentbody.content;>
<!ATTLIST learningContentbody    %learningContentbody.attributes;>



<!--specialization attributes-->

<!ATTLIST learningContent        %global-atts; class CDATA "- topic/topic learningBase/learningBase learningContent/learningContent ">
<!ATTLIST learningContentbody    %global-atts; class CDATA "- topic/body  learningBase/learningBasebody learningContent/learningContentbody ">
