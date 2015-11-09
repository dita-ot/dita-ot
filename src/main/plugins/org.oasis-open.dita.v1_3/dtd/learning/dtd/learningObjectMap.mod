<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!-- MODULE:    DITA Learning Object Map                           -->
<!-- VERSION:   1.3                                                 -->
<!-- DATE:      June 2013                                          -->
<!-- ============================================================= -->
<!-- Refer to the latest version of this file by the following public ID: -->
<!-- -//OASIS//ELEMENTS DITA Learning Object Map//EN               -->
<!-- To refer to this specific version, you may use this value:    -->
<!-- -//OASIS//ELEMENTS DITA 1.3 Learning Object Map//EN           -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)      -->
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

<!ENTITY % learningObjectMap
                       "learningObjectMap"                           >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % topicref-atts-for-learningObjectMap
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
<!--                    LONG NAME: Learning Object Map             -->
<!ENTITY % learningObjectMap.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%topicref;)*,
                         (%reltable;)*)"
>
<!ENTITY % learningObjectMap.attributes
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
               %topicref-atts-for-learningObjectMap;
               %select-atts;"
>
<!ELEMENT  learningObjectMap %learningObjectMap.content;>
<!ATTLIST  learningObjectMap %learningObjectMap.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  learningObjectMap %global-atts;  class CDATA "- map/map learningObjectMap/learningObjectMap ">

<!-- ================== End of DITA Learning Object Map Module ==================== -->
 