<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Utilities Domain                             -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Utilities Domain//EN"
      Delivered as file "utilities-domain.dtd"                     -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Utilities Domain          -->
<!--             (e.g., message lists and imagemaps)               -->
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

<!ENTITY % imagemap    "imagemap"                                    >
<!ENTITY % area        "area"                                        >
<!ENTITY % shape       "shape"                                       >
<!ENTITY % coords      "coords"                                      >


<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->


<!--                    Provide an alternative univ-atts that sets 
                        translate to default 'no'                  -->
<!ENTITY % univ-atts-translate-no
            '%id-atts;
             platform CDATA #IMPLIED
             product CDATA #IMPLIED
             audience CDATA #IMPLIED
             otherprops CDATA #IMPLIED
             importance (obsolete | deprecated | 
                         optional | default | low | 
                         normal | high | 
                         recommended | required | 
                         urgent)                         #IMPLIED
             rev        CDATA                            #IMPLIED
             status     (new | changed | deleted |  
                         unchanged)                      #IMPLIED
             translate  (yes | no)                       "no"
             xml:lang   NMTOKEN                          #IMPLIED'   >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS for IMAGEMAP          -->
<!-- ============================================================= -->


<!--                    LONG NAME: Imagemap                        -->
<!ELEMENT imagemap      ((%image;), (%area;)+)                       >           
<!ATTLIST imagemap        
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Hoptspot Area Description       -->
<!ELEMENT area          ((%shape;), (%coords;), (%xref;))            >
<!ATTLIST area         
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Shape of the Hotspot            -->
<!ELEMENT shape         (#PCDATA)                                    >
<!ATTLIST shape           
             keyref     CDATA                            #IMPLIED
             %univ-atts-translate-no;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Coordinates of the Hotspot      -->
<!ELEMENT coords        (%words.cnt;)*                               >
<!ATTLIST coords          
             keyref     CDATA                            #IMPLIED
             %univ-atts-translate-no;
             outputclass 
                        CDATA                            #IMPLIED    >
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST imagemap %global-atts;  class CDATA "+ topic/fig ut-d/imagemap " >
<!ATTLIST area     %global-atts;  class CDATA "+ topic/figgroup ut-d/area ">
<!ATTLIST shape    %global-atts;  class CDATA "+ topic/keyword ut-d/shape ">
<!ATTLIST coords   %global-atts;  class CDATA "+ topic/ph ut-d/coords "    >

 
<!-- ================== End Utilities Domain ====================== -->