<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!--                     HEADER                                      -->
<!--  =============================================================  -->
<!--   MODULE:    DITA Markup Name Mention Domain                              -->
<!--   VERSION:   1.3                                                 -->
<!--   DATE:      March 2014                                        -->
<!--                                                                 -->
<!--  =============================================================  -->
<!--  =============================================================       -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % markupname  "markupname"                                  >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Markup name                     -->
<!ENTITY % markupname.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % markupname.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  markupname %markupname.content;>
<!ATTLIST  markupname %markupname.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  markupname   %global-atts;  class CDATA "+ topic/keyword markup-d/markupname ">

<!-- ================== End of DITA Markup Name Mention Domain ==================== -->
 