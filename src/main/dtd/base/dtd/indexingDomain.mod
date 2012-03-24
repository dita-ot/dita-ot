<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Indexing  Domain                             -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Indexing Domain//EN"
      Delivered as file "indexingDomain.mod"                       -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Indexing Domain           -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             June 2006                                         -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2006, 2009.              -->
<!--             All Rights Reserved.                              -->
<!--  UPDATES:                                                     -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % index-see       "index-see"                               >
<!ENTITY % index-see-also  "index-see-also"                          >
<!ENTITY % index-sort-as   "index-sort-as"                           >


<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->




<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS for IMAGEMAP          -->
<!-- ============================================================= -->

<!--                    LONG NAME: Index See                       -->
<!ENTITY % index-see.content
                       "(%words.cnt; |
                         %indexterm;)*"
>
<!ENTITY % index-see.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT index-see    %index-see.content;>
<!ATTLIST index-see    %index-see.attributes;>


<!--                    LONG NAME: Index See Also                  -->
<!ENTITY % index-see-also.content
                       "(%words.cnt; |
                         %indexterm;)*"
>
<!ENTITY % index-see-also.attributes
             "keyref 
                            CDATA 
                                            #IMPLIED
              %univ-atts;"
>
<!ELEMENT index-see-also    %index-see-also.content;>
<!ATTLIST index-see-also    %index-see-also.attributes;>


<!--                    LONG NAME: Index Sort As                   -->
<!ENTITY % index-sort-as.content
                       "(%words.cnt;)*"
>
<!ENTITY % index-sort-as.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT index-sort-as    %index-sort-as.content;>
<!ATTLIST index-sort-as    %index-sort-as.attributes;>


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST index-see       %global-atts; class CDATA "+ topic/index-base indexing-d/index-see ">
<!ATTLIST index-see-also  %global-atts; class CDATA "+ topic/index-base indexing-d/index-see-also ">
<!ATTLIST index-sort-as   %global-atts; class CDATA "+ topic/index-base indexing-d/index-sort-as ">
 
<!-- ================== End Indexing Domain ====================== -->