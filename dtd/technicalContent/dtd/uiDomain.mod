<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA User Interface Domain                        -->
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
PUBLIC "-//OASIS//ELEMENTS DITA User Interface Domain//EN"
      Delivered as file "uiDomain.mod"                             -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the User Interface Domain          -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Corrected LONG NAME for screen             -->
<!--    2005.11.15 RDA: Corrected the "Delivered as" system ID     -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.02.12 RDA: Add text to wintitle, shortcut             -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

 
<!ENTITY % uicontrol   "uicontrol"                                   >
<!ENTITY % wintitle    "wintitle"                                    >
<!ENTITY % menucascade "menucascade"                                 >
<!ENTITY % shortcut    "shortcut"                                    >
<!ENTITY % screen      "screen"                                      >


<!-- ============================================================= -->
<!--                    UI KEYWORD TYPES ELEMENT DECLARATIONS      -->
<!-- ============================================================= -->


<!--                    LONG NAME: User Interface Control          -->
<!ENTITY % uicontrol.content
                       "(%words.cnt; | 
                         %image; | 
                         %shortcut;)*"
>
<!ENTITY % uicontrol.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT uicontrol    %uicontrol.content;>
<!ATTLIST uicontrol    %uicontrol.attributes;>



<!--                    LONG NAME: Window Title                    -->
<!ENTITY % wintitle.content
                       "(#PCDATA |
                         %text;)*
">
<!ENTITY % wintitle.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT wintitle    %wintitle.content;>
<!ATTLIST wintitle    %wintitle.attributes;>




<!--                    LONG NAME: Menu Cascade                    -->
<!ENTITY % menucascade.content
                       "(%uicontrol;)+"
>
<!ENTITY % menucascade.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT menucascade    %menucascade.content;>
<!ATTLIST menucascade    %menucascade.attributes;>



<!--                    LONG NAME: Short Cut                       -->
<!ENTITY % shortcut.content
                       "(#PCDATA |
                        %text;)*
">
<!ENTITY % shortcut.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT shortcut    %shortcut.content;>
<!ATTLIST shortcut    %shortcut.attributes;>



<!--                    LONG NAME: Text Screen Capture             -->
<!ENTITY % screen.content
                       "(#PCDATA | 
                         %basic.ph.notm; |
                         %data.elements.incl; | 
                         %foreign.unknown.incl; | 
                         %txt.incl;)*"
>
<!ENTITY % screen.attributes
             "%display-atts;
              spectitle 
                        CDATA 
                                  #IMPLIED
              xml:space 
                        (preserve) 
                                  #FIXED 'preserve'
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT screen    %screen.content;>
<!ATTLIST screen    %screen.attributes;>

 

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
 

<!ATTLIST menucascade %global-atts;  class CDATA "+ topic/ph ui-d/menucascade "  >
<!ATTLIST screen      %global-atts;  class CDATA "+ topic/pre ui-d/screen "      >
<!ATTLIST shortcut    %global-atts;  class CDATA "+ topic/keyword ui-d/shortcut ">
<!ATTLIST uicontrol   %global-atts;  class CDATA "+ topic/ph ui-d/uicontrol "    >
<!ATTLIST wintitle    %global-atts;  class CDATA "+ topic/keyword ui-d/wintitle ">

 
<!-- ================== End DITA User Interface Domain =========== -->
