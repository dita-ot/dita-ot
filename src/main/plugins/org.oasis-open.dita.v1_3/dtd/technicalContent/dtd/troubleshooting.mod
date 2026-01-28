<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Troubleshooting Domain                                       -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                        -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an  -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Troubleshooting//EN"          -->
<!--       Delivered as file "troubleshooting.mod"                                 -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Troubleshootings                     -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2014                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2014                     -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % troubleshooting
                       "troubleshooting"                             >
<!ENTITY % troublebody "troublebody"                                 >
<!ENTITY % cause       "cause"                                       >
<!ENTITY % condition   "condition"                                   >
<!ENTITY % remedy      "remedy"                                      >
<!ENTITY % responsibleParty
                       "responsibleParty"                            >
<!ENTITY % troubleSolution
                       "troubleSolution"                             >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % troubleshooting-info-types
              "%info-types;"
>
<!ENTITY % section.blocks.only.cnt
              "((%title;)?,
                (%basic.block; |
                 %data.elements.incl; |
                 %foreign.unknown.incl; |
                 %sectiondiv; |
                 %txt.incl;)*)"
>
<!--                    LONG NAME: Troubleshooting                 -->
<!ENTITY % troubleshooting.content
                       "((%title;),
                         (%titlealts;)?,
                         (%abstract; |
                          %shortdesc;)?,
                         (%prolog;)?,
                         (%troublebody;)?,
                         (%related-links;)?,
                         (%troubleshooting-info-types;)*)"
>
<!ENTITY % troubleshooting.attributes
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
<!ELEMENT  troubleshooting %troubleshooting.content;>
<!ATTLIST  troubleshooting %troubleshooting.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>


<!--                    LONG NAME: Troubleshooting Body            -->
<!ENTITY % troublebody.content
                       "((%condition;)?,
                         (%troubleSolution;)+)?"
>
<!ENTITY % troublebody.attributes
              "%id-atts;
               %localization-atts;
               base
                          CDATA
                                    #IMPLIED
               %base-attribute-extensions;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  troublebody %troublebody.content;>
<!ATTLIST  troublebody %troublebody.attributes;>


<!--                    LONG NAME: Cause                           -->
<!ENTITY % cause.content
                       "(%section.blocks.only.cnt;)?"
>
<!ENTITY % cause.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  cause %cause.content;>
<!ATTLIST  cause %cause.attributes;>


<!--                    LONG NAME: Condition                       -->
<!ENTITY % condition.content
                       "(%section.blocks.only.cnt;)?"
>
<!ENTITY % condition.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  condition %condition.content;>
<!ATTLIST  condition %condition.attributes;>


<!--                    LONG NAME: Remedy                          -->
<!ENTITY % remedy.content
                       "((%title;)?,
                         (%responsibleParty;)?,
                         (%steps; |
                          %steps-unordered; |
                          %steps-informal;))"
>
<!ENTITY % remedy.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  remedy %remedy.content;>
<!ATTLIST  remedy %remedy.attributes;>


<!--                    LONG NAME: Responsible Party               -->
<!ENTITY % responsibleParty.content
                       "(%para.cnt;)*"
>
<!ENTITY % responsibleParty.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  responsibleParty %responsibleParty.content;>
<!ATTLIST  responsibleParty %responsibleParty.attributes;>


<!--                    LONG NAME: Trouble Solution                -->
<!ENTITY % troubleSolution.content
                       "(                         (%cause;)*,
                         (%remedy;)*)"
>
<!ENTITY % troubleSolution.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  troubleSolution %troubleSolution.content;>
<!ATTLIST  troubleSolution %troubleSolution.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  troubleshooting %global-atts;  class CDATA "- topic/topic troubleshooting/troubleshooting ">
<!ATTLIST  troublebody  %global-atts;  class CDATA "- topic/body troubleshooting/troublebody ">
<!ATTLIST  troubleSolution %global-atts;  class CDATA "- topic/bodydiv troubleshooting/troubleSolution ">
<!ATTLIST  cause        %global-atts;  class CDATA "- topic/section troubleshooting/cause ">
<!ATTLIST  condition    %global-atts;  class CDATA "- topic/section troubleshooting/condition ">
<!ATTLIST  remedy       %global-atts;  class CDATA "- topic/section troubleshooting/remedy ">
<!ATTLIST  responsibleParty %global-atts;  class CDATA "- topic/p troubleshooting/responsibleParty ">

<!-- ================== End of DITA Troubleshooting Domain ==================== -->
 