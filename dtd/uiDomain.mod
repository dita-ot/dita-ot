<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA User Interface Domain                        -->
<!--  VERSION:   1.O                                               -->
<!--  DATE:      February 2005                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA User Interface Domain//EN"
      Delivered as file "ui-domain.mod"                            -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the User Interface Domain          -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005.                    -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
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
<!ELEMENT uicontrol     (%words.cnt; | %image; | %shortcut;)*        >
<!ATTLIST uicontrol       
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Window Title                    -->
<!ELEMENT wintitle      (#PCDATA)                                    >
<!ATTLIST wintitle        
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >



<!--                    LONG NAME: Menu Cascade                    -->
<!ELEMENT menucascade   (%uicontrol;)+                               >
<!ATTLIST menucascade     
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Short Cut                       -->
<!ELEMENT shortcut      (#PCDATA)                                    >
<!ATTLIST shortcut        
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Screen Name                     -->
<!ELEMENT screen        (#PCDATA | %basic.ph.notm; | %txt.incl;)*    >
<!ATTLIST screen          
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             xml:space  (preserve)               #FIXED 'preserve'
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
             

<!ATTLIST menucascade %global-atts;  class CDATA "+ topic/ph ui-d/menucascade "  >
<!ATTLIST screen      %global-atts;  class CDATA "+ topic/pre ui-d/screen "      >
<!ATTLIST shortcut    %global-atts;  class CDATA "+ topic/keyword ui-d/shortcut ">
<!ATTLIST uicontrol   %global-atts;  class CDATA "+ topic/ph ui-d/uicontrol "    >
<!ATTLIST wintitle    %global-atts;  class CDATA "+ topic/keyword ui-d/wintitle ">

 
<!-- ================== End DITA User Interface Domain =========== -->
