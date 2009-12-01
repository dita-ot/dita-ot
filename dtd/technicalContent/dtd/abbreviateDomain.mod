<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Abbreviated Form Domain                      -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Abbreviated Form Domain//EN"
      Delivered as file "abbreviateDomain.mod"                     -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the Abbreviated Form Domain        -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             June 2008                                         -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2008, 2009.              -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

 
<!ENTITY % abbreviated-form   "abbreviated-form"                     >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Abbreviated Form                -->
<!ENTITY % abbreviated-form.content
                       "EMPTY"
>
<!ENTITY % abbreviated-form.attributes
             "keyref 
                        CDATA 
                                  #REQUIRED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT abbreviated-form    %abbreviated-form.content;>
<!ATTLIST abbreviated-form    %abbreviated-form.attributes;>

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
 

<!ATTLIST abbreviated-form %global-atts;  class CDATA "+ topic/term abbrev-d/abbreviated-form "  >
 
<!-- ================== End DITA Abbreviated Form Domain ========= -->
