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
PUBLIC "-//OASIS//ELEMENTS DITA Learning Overview//EN"
      Delivered as file "learningOverview.mod                      -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Overview                  -->
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

<!ENTITY % learningOverview "learningOverview">
<!ENTITY % learningOverviewbody "learningOverviewbody">


<!ENTITY % learningOverview-info-types "no-topic-nesting">
<!ENTITY included-domains 
                        "" >

<!ENTITY % learningOverview.content
                       "((%title;),
                         (%titlealts;)?,
                         (%shortdesc; |
                          %abstract;)?, 
                         (%prolog;)?,
                         (%learningOverviewbody;), 
                         (%related-links;)?,
                         (%learningOverview-info-types;)* )"
>
<!ENTITY % learningOverview.attributes
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
<!ELEMENT learningOverview    %learningOverview.content;>
<!ATTLIST learningOverview    
              %learningOverview.attributes;
              %arch-atts;
              domains 
                        CDATA
                                  "&included-domains;"
>


<!ENTITY % learningOverviewbody.content
                       "((%lcIntro;)?,
                         (%lcAudience;)*,
                         (%lcDuration;)?,
                         (%lcPrereqs;)?,
                         (%lcObjectives;)?,
                         (%lcResources;)?,
                         (%section;)*)  "
>
<!ENTITY % learningOverviewbody.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT learningOverviewbody    %learningOverviewbody.content;>
<!ATTLIST learningOverviewbody    %learningOverviewbody.attributes;>


<!--specialization attributes-->

<!ATTLIST learningOverview        %global-atts; class CDATA "- topic/topic learningBase/learningBase     learningOverview/learningOverview ">
<!ATTLIST learningOverviewbody    %global-atts; class CDATA "- topic/body  learningBase/learningBasebody learningOverview/learningOverviewbody ">
