<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Highlight Domain                             -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Highlight Domain//EN"
      Delivered as file "highlight-domain.nod"                     -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributed    -->
<!--             for Highlight Domain                              -->
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

<!ENTITY % b           "b"                                           >
<!ENTITY % i           "i"                                           >
<!ENTITY % u           "u"                                           >     
<!ENTITY % tt          "tt"                                          >
<!ENTITY % sup         "sup"                                         >
<!ENTITY % sub         "sub"                                         >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Bold                            -->
<!ELEMENT b             (#PCDATA | %basic.ph;)*                      >
<!ATTLIST b              
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Bold                            -->
<!ELEMENT u             (#PCDATA | %basic.ph;)*                      >
<!ATTLIST u              
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Bold                            -->
<!ELEMENT i             (#PCDATA | %basic.ph;)*                      >
<!ATTLIST i             
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Bold                            -->
<!ELEMENT tt            (#PCDATA | %basic.ph;)*                      >
<!ATTLIST tt            
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Bold                            -->
<!ELEMENT sup           (#PCDATA | %basic.ph;)*                      >
<!ATTLIST sup          
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Bold                            -->
<!ELEMENT sub           (#PCDATA | %basic.ph;)*                      >
<!ATTLIST sub           
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST b           %global-atts;  class CDATA "+ topic/ph hi-d/b "  >
<!ATTLIST i           %global-atts;  class CDATA "+ topic/ph hi-d/i "  >
<!ATTLIST sub         %global-atts;  class CDATA "+ topic/ph hi-d/sub ">
<!ATTLIST sup         %global-atts;  class CDATA "+ topic/ph hi-d/sup ">
<!ATTLIST tt          %global-atts;  class CDATA "+ topic/ph hi-d/tt " >
<!ATTLIST u           %global-atts;  class CDATA "+ topic/ph hi-d/u "  >


<!-- ================== DITA Highlight Domain ==================== -->