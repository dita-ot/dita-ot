<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!-- MODULE:    DITA Learning Group Map                            -->
<!-- VERSION:   1.3                                                 -->
<!-- DATE:      June 2013                                          -->
<!-- ============================================================= -->
<!-- Refer to the latest version of this file by the following public ID: -->
<!-- -//OASIS//ELEMENTS DITA Learning Group Map//EN                -->
<!-- To refer to this specific version, you may use this value:    -->
<!-- -//OASIS//ELEMENTS DITA 1.3 Learning Group Map//EN            -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!-- PURPOSE:    Provides a map type for representing a single learning group. -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!-- June 2013                                                     -->
<!-- (C) Copyright OASIS Open                                      -->
<!-- All Rights Reserved.                                           -->
<!-- ============================================================= -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % learningGroupMap
                       "learningGroupMap"                            >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % topicref-atts-for-learningGroupMap
              "collection-type
                          (choice |
                           family |
                           sequence |
                           unordered |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED
               linking
                          (none |
                           normal |
                           sourceonly |
                           targetonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               print
                          (no |
                           printonly |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               chunk
                          CDATA
                                    #IMPLIED"
>
<!--                    LONG NAME: Learning Group Map              -->
<!ENTITY % learningGroupMap.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%topicref;)*,
                         (%reltable;)*)"
>
<!ENTITY % learningGroupMap.attributes
              "id
                          ID
                                    #IMPLIED
               %conref-atts;
               anchorref
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED
               %localization-atts;
               %topicref-atts-for-learningGroupMap;
               %select-atts;"
>
<!ELEMENT  learningGroupMap %learningGroupMap.content;>
<!ATTLIST  learningGroupMap %learningGroupMap.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  learningGroupMap %global-atts;  class CDATA "- map/map learningGroupMap/learningGroupMap ">

<!-- ================== End of DITA Learning Group Map Module ==================== -->
 