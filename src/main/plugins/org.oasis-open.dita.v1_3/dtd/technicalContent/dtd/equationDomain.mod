<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!-- DITA Equation Domain                                          -->
<!-- Purpose: Provides elements for identifying equations as       -->
<!--          equations independent of how the equation itself     -->
<!--          is defined (e.g., as a graphic, using MathML, etc.). -->
<!--                                                               -->
<!-- Creation Date: March 2014                                     -->
<!-- Copyright (c) OASIS Open 2014                                 -->
<!-- =============================================================       -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % equation-inline
                       "equation-inline"                             >
<!ENTITY % equation-block
                       "equation-block"                              >
<!ENTITY % equation-number
                       "equation-number"                             >
<!ENTITY % equation-figure
                       "equation-figure"                             >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % equation.cnt
              "%ph.cnt;"
>
<!--                    LONG NAME: Inline equation                 -->
<!ENTITY % equation-inline.content
                       "(%equation.cnt;)*"
>
<!ENTITY % equation-inline.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  equation-inline %equation-inline.content;>
<!ATTLIST  equation-inline %equation-inline.attributes;>


<!--                    LONG NAME: Block equation                  -->
<!ENTITY % equation-block.content
                       "(%equation.cnt; |
                         %equation-number;)*"
>
<!ENTITY % equation-block.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  equation-block %equation-block.content;>
<!ATTLIST  equation-block %equation-block.attributes;>


<!--                    LONG NAME: Equation number                 -->
<!ENTITY % equation-number.content
                       "(#PCDATA |
                         %ph; |
                         %text;)*"
>
<!ENTITY % equation-number.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  equation-number %equation-number.content;>
<!ATTLIST  equation-number %equation-number.attributes;>


<!--                    LONG NAME: Equation figure                 -->
<!ENTITY % equation-figure.content
                       "((%title;)?,
                         (%desc;)?,
                         (%figgroup; |
                          %fig.cnt;)*)"
>
<!ENTITY % equation-figure.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  equation-figure %equation-figure.content;>
<!ATTLIST  equation-figure %equation-figure.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  equation-inline %global-atts;  class CDATA "+ topic/ph equation-d/equation-inline ">
<!ATTLIST  equation-block %global-atts;  class CDATA "+ topic/div equation-d/equation-block ">
<!ATTLIST  equation-number %global-atts;  class CDATA "+ topic/ph equation-d/equation-number ">
<!ATTLIST  equation-figure %global-atts;  class CDATA "+ topic/fig equation-d/equation-figure ">

<!-- ================== End of DITA Equation Domain ==================== -->
 