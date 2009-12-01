<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Software Domain                              -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Software Domain//EN"
      Delivered as file "softwareDomain.mod"                       -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the Software Domain                -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Corrected the PURPOSE in this comment      -->
<!--    2005.11.15 RDA: Corrected the "Delivered as" system ID     -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.02.12 RDA: Add text to msgnum, cmdname, varname       -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->


<!ENTITY % msgph       "msgph"                                       >
<!ENTITY % msgblock    "msgblock"                                    >
<!ENTITY % msgnum      "msgnum"                                      >
<!ENTITY % cmdname     "cmdname"                                     >
<!ENTITY % varname     "varname"                                     >
<!ENTITY % filepath    "filepath"                                    >
<!ENTITY % userinput   "userinput"                                   >
<!ENTITY % systemoutput 
                       "systemoutput"                                >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Message Phrase                  -->
<!ENTITY % msgph.content
                       "(%words.cnt;)*"
>
<!ENTITY % msgph.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT msgph    %msgph.content;>
<!ATTLIST msgph    %msgph.attributes;>



<!--                    LONG NAME: Message Block                   -->
<!ENTITY % msgblock.content
                       "(%words.cnt;)*"
>
<!ENTITY % msgblock.attributes
             "%display-atts;
              spectitle 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED 
              xml:space 
                        (preserve) 
                                  #FIXED 'preserve'"
>
<!ELEMENT msgblock    %msgblock.content;>
<!ATTLIST msgblock    %msgblock.attributes;>



<!--                    LONG NAME: Message Number                  -->
<!ENTITY % msgnum.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % msgnum.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT msgnum    %msgnum.content;>
<!ATTLIST msgnum    %msgnum.attributes;>



<!--                    LONG NAME: Command Name                    -->
<!ENTITY % cmdname.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % cmdname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT cmdname    %cmdname.content;>
<!ATTLIST cmdname    %cmdname.attributes;>



<!--                    LONG NAME: Variable Name                   -->
<!ENTITY % varname.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % varname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT varname    %varname.content;>
<!ATTLIST varname    %varname.attributes;>


<!--                    LONG NAME: File Path                       -->
<!ENTITY % filepath.content
                       "(%words.cnt;)*"
>
<!ENTITY % filepath.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT filepath    %filepath.content;>
<!ATTLIST filepath    %filepath.attributes;>



<!--                    LONG NAME: User Input                      -->
<!ENTITY % userinput.content
                       "(%words.cnt;)*"
>
<!ENTITY % userinput.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT userinput    %userinput.content;>
<!ATTLIST userinput    %userinput.attributes;>



<!--                    LONG NAME: System Output                   -->
<!ENTITY % systemoutput.content
                       "(%words.cnt;)*"
>
<!ENTITY % systemoutput.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT systemoutput    %systemoutput.content;>
<!ATTLIST systemoutput    %systemoutput.attributes;>

 

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
 

<!ATTLIST cmdname     %global-atts;  class CDATA "+ topic/keyword sw-d/cmdname ">
<!ATTLIST filepath    %global-atts;  class CDATA "+ topic/ph sw-d/filepath "    >
<!ATTLIST msgblock    %global-atts;  class CDATA "+ topic/pre sw-d/msgblock "   >
<!ATTLIST msgnum      %global-atts;  class CDATA "+ topic/keyword sw-d/msgnum " >
<!ATTLIST msgph       %global-atts;  class CDATA "+ topic/ph sw-d/msgph "       >
<!ATTLIST systemoutput
                      %global-atts;  class CDATA "+ topic/ph sw-d/systemoutput ">
<!ATTLIST userinput   %global-atts;  class CDATA "+ topic/ph sw-d/userinput "   >
<!ATTLIST varname     %global-atts;  class CDATA "+ topic/keyword sw-d/varname ">

 
<!-- ================== End Software Domain ====================== -->