<?xml version="1.0" encoding="UTF-8"?>
<!--                                                               -->
<!-- =============================================================  -->
<!-- MODULE:    DITA MathML Domain - RNG                           -->
<!-- VERSION:   1.3                                                 -->
<!-- DATE:      May 2013                                           -->
<!-- =============================================================  -->
<!-- =============================================================  -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)      -->
<!-- PURPOSE:    Provides elements for including MathML Markup     -->
<!--            in DITA documents.                                 -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!-- May 2013                                                      -->
<!-- (C) Copyright OASIS Open 2013                                 -->
<!-- All Rights Reserved.                                           -->
<!-- =============================================================     -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % mathmlref   "mathmlref"                                   >
<!ENTITY % mathml      "mathml"                                      >

<!ENTITY % mathml3-ditadriver
   PUBLIC "-//OASIS//ELEMENTS DITA 1.3 Mathml3 Driver//EN"
          "mathml/mathml3-ditadriver.dtd"
>%mathml3-ditadriver;

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: MathML reference                -->
<!ENTITY % mathmlref.content
                       "EMPTY"
>
<!ENTITY % mathmlref.attributes
              "href
                          CDATA
                                    #IMPLIED
               keyref
                          CDATA
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               format
                          CDATA
                                    'mml'
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  mathmlref %mathmlref.content;>
<!ATTLIST  mathmlref %mathmlref.attributes;>


<!--                    LONG NAME: MathML container                -->
<!ENTITY % mathml.content
                       "(m:math |
                         %mathmlref; |
                         %data; |
                         %data-about;)*"
>
<!ENTITY % mathml.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  mathml %mathml.content;>
<!ATTLIST  mathml %mathml.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  mathml       %global-atts;  class CDATA "+ topic/foreign mathml-d/mathml ">
<!ATTLIST  mathmlref    %global-atts;  class CDATA "+ topic/xref mathml-d/mathmlref ">

<!-- ================== End of DITA MathML Domain ==================== -->
 