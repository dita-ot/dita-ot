<?xml version="1.0" encoding="UTF-8"?>
<!--  ============================================================= DITAVAL Reference -->
<!--       Domain =============================================================  -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % ditavalref  "ditavalref"                                  >
<!ENTITY % ditavalmeta "ditavalmeta"                                 >
<!ENTITY % dvrResourcePrefix
                       "dvrResourcePrefix"                           >
<!ENTITY % dvrResourceSuffix
                       "dvrResourceSuffix"                           >
<!ENTITY % dvrKeyscopePrefix
                       "dvrKeyscopePrefix"                           >
<!ENTITY % dvrKeyscopeSuffix
                       "dvrKeyscopeSuffix"                           >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % ditavalref-univ-atts
              "id
                          NMTOKEN
                                    #IMPLIED
               conref
                          CDATA
                                    #IMPLIED
               conrefend
                          CDATA
                                    #IMPLIED
               conaction
                          (mark |
                           pushafter |
                           pushbefore |
                           pushreplace |
                           -dita-use-conref-target)
                                    #IMPLIED
               %select-atts;
               %localization-atts;"
>
<!--                    LONG NAME: DITAVAL Reference               -->
<!ENTITY % ditavalref.content
                       "(%ditavalmeta;)*"
>
<!ENTITY % ditavalref.attributes
              "navtitle
                          CDATA
                                    #IMPLIED
               href
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               format
                          CDATA
                                    'ditaval'
               processing-role
                          CDATA
                                    'resource-only'
               %ditavalref-univ-atts;"
>
<!ELEMENT  ditavalref %ditavalref.content;>
<!ATTLIST  ditavalref %ditavalref.attributes;>


<!--                    LONG NAME: Ditavalmeta                     -->
<!ENTITY % ditavalmeta.content
                       "((%navtitle;)?,
                         ((%dvrResourcePrefix;)?,
                          (%dvrResourceSuffix;)?,
                          (%dvrKeyscopePrefix;)?,
                          (%dvrKeyscopeSuffix;)?))"
>
<!ENTITY % ditavalmeta.attributes
              "lockmeta
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               %ditavalref-univ-atts;"
>
<!ELEMENT  ditavalmeta %ditavalmeta.content;>
<!ATTLIST  ditavalmeta %ditavalmeta.attributes;>


<!--                    LONG NAME: DvrResourcePrefix               -->
<!ENTITY % dvrResourcePrefix.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % dvrResourcePrefix.attributes
              "name
                          (dvrResourcePrefix)
                                    'dvrResourcePrefix'
               %ditavalref-univ-atts;"
>
<!ELEMENT  dvrResourcePrefix %dvrResourcePrefix.content;>
<!ATTLIST  dvrResourcePrefix %dvrResourcePrefix.attributes;>


<!--                    LONG NAME: DvrResourceSuffix               -->
<!ENTITY % dvrResourceSuffix.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % dvrResourceSuffix.attributes
              "name
                          (dvrResourceSuffix)
                                    'dvrResourceSuffix'
               %ditavalref-univ-atts;"
>
<!ELEMENT  dvrResourceSuffix %dvrResourceSuffix.content;>
<!ATTLIST  dvrResourceSuffix %dvrResourceSuffix.attributes;>


<!--                    LONG NAME: DvrKeyscopePrefix               -->
<!ENTITY % dvrKeyscopePrefix.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % dvrKeyscopePrefix.attributes
              "name
                          (dvrKeyscopePrefix)
                                    'dvrKeyscopePrefix'
               %ditavalref-univ-atts;"
>
<!ELEMENT  dvrKeyscopePrefix %dvrKeyscopePrefix.content;>
<!ATTLIST  dvrKeyscopePrefix %dvrKeyscopePrefix.attributes;>


<!--                    LONG NAME: DvrKeyscopeSuffix               -->
<!ENTITY % dvrKeyscopeSuffix.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % dvrKeyscopeSuffix.attributes
              "name
                          (dvrKeyscopeSuffix)
                                    'dvrKeyscopeSuffix'
               %ditavalref-univ-atts;"
>
<!ELEMENT  dvrKeyscopeSuffix %dvrKeyscopeSuffix.content;>
<!ATTLIST  dvrKeyscopeSuffix %dvrKeyscopeSuffix.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  ditavalref   %global-atts;  class CDATA "+ map/topicref ditavalref-d/ditavalref ">
<!ATTLIST  ditavalmeta  %global-atts;  class CDATA "+ map/topicmeta ditavalref-d/ditavalmeta ">
<!ATTLIST  dvrResourcePrefix %global-atts;  class CDATA "+ topic/data ditavalref-d/dvrResourcePrefix ">
<!ATTLIST  dvrResourceSuffix %global-atts;  class CDATA "+ topic/data ditavalref-d/dvrResourceSuffix ">
<!ATTLIST  dvrKeyscopePrefix %global-atts;  class CDATA "+ topic/data ditavalref-d/dvrKeyscopePrefix ">
<!ATTLIST  dvrKeyscopeSuffix %global-atts;  class CDATA "+ topic/data ditavalref-d/dvrKeyscopeSuffix ">

<!-- ================== End of DITAVAL Reference Domain ==================== -->
 