<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Delayed Resolution Domain                    -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Delayed Resolution Domain//EN"
      Delivered as file "delayResolutionDomain.mod"                -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization attributes     -->
<!--             for Delayed Resolution Domain                     -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             February 2008                                     -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2008, 2009.              -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % exportanchors "exportanchors"                             >
<!ENTITY % anchorid      "anchorid"                                  >
<!ENTITY % anchorkey     "anchorkey"                                 >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Export Anchor List              -->
<!ENTITY % exportanchors.content
                       "(%anchorid; | 
                         %anchorkey;)*"
>
<!ENTITY % exportanchors.attributes
             "%univ-atts;"
>
<!ELEMENT exportanchors    %exportanchors.content;>
<!ATTLIST exportanchors    %exportanchors.attributes;>


<!--                    LONG NAME: Anchor ID                       -->
<!ENTITY % anchorid.content
                       "EMPTY"
>
<!ENTITY % anchorid.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              id 
                        NMTOKEN 
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT anchorid    %anchorid.content;>
<!ATTLIST anchorid    %anchorid.attributes;>


<!--                    LONG NAME: Anchor Key                       -->
<!ENTITY % anchorkey.content
                       "EMPTY"
>
<!ENTITY % anchorkey.attributes
             "keyref 
                        CDATA 
                                  #REQUIRED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT anchorkey    %anchorkey.content;>
<!ATTLIST anchorkey    %anchorkey.attributes;>



<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST exportanchors %global-atts;  class CDATA "+ topic/keywords delay-d/exportanchors "  >
<!ATTLIST anchorid      %global-atts;  class CDATA "+ topic/keyword delay-d/anchorid "  >
<!ATTLIST anchorkey     %global-atts;  class CDATA "+ topic/keyword delay-d/anchorkey "  >

<!-- ================== End Delayed Resolution Domain  =========== -->
