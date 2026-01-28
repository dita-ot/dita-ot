<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!--                     HEADER                                      -->
<!--  =============================================================  -->
<!--   MODULE:    DITA XML Mention Domain                              -->
<!--   VERSION:   1.3                                                 -->
<!--   DATE:      November 2013                                       -->
<!--                                                                 -->
<!--  =============================================================  -->
<!--  =============================================================       -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % numcharref  "numcharref"                                  >
<!ENTITY % parameterentity
                       "parameterentity"                             >
<!ENTITY % textentity  "textentity"                                  >
<!ENTITY % xmlatt      "xmlatt"                                      >
<!ENTITY % xmlelement  "xmlelement"                                  >
<!ENTITY % xmlnsname   "xmlnsname"                                   >
<!ENTITY % xmlpi       "xmlpi"                                       >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Numeric character reference (&#10;, &#x0a;) -->
<!ENTITY % numcharref.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % numcharref.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  numcharref %numcharref.content;>
<!ATTLIST  numcharref %numcharref.attributes;>


<!--                    LONG NAME: Parameter entity reference (%p.content;) -->
<!ENTITY % parameterentity.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % parameterentity.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  parameterentity %parameterentity.content;>
<!ATTLIST  parameterentity %parameterentity.attributes;>


<!--                    LONG NAME: Text entity (&prodname;)        -->
<!ENTITY % textentity.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % textentity.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  textentity %textentity.content;>
<!ATTLIST  textentity %textentity.attributes;>


<!--                    LONG NAME: XML attribute                   -->
<!ENTITY % xmlatt.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % xmlatt.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  xmlatt %xmlatt.content;>
<!ATTLIST  xmlatt %xmlatt.attributes;>


<!--                    LONG NAME: XML element                     -->
<!ENTITY % xmlelement.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % xmlelement.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  xmlelement %xmlelement.content;>
<!ATTLIST  xmlelement %xmlelement.attributes;>


<!--                    LONG NAME: XML namespace name (aka "namespace URI") -->
<!ENTITY % xmlnsname.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % xmlnsname.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  xmlnsname %xmlnsname.content;>
<!ATTLIST  xmlnsname %xmlnsname.attributes;>


<!--                    LONG NAME: XML processing instruction (PI) -->
<!ENTITY % xmlpi.content
                       "(#PCDATA |
                         %draft-comment; |
                         %required-cleanup; |
                         %text;)*"
>
<!ENTITY % xmlpi.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  xmlpi %xmlpi.content;>
<!ATTLIST  xmlpi %xmlpi.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  numcharref   %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/numcharref ">
<!ATTLIST  parameterentity %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/parameterentity ">
<!ATTLIST  textentity   %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/textentity ">
<!ATTLIST  xmlatt       %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/xmlatt ">
<!ATTLIST  xmlelement   %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/xmlelement ">
<!ATTLIST  xmlnsname    %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/xmlnsname ">
<!ATTLIST  xmlpi        %global-atts;  class CDATA "+ topic/keyword markup-d/markupname xml-d/xmlpi ">

<!-- ================== End of DITA XML Construct Domain ==================== -->
 