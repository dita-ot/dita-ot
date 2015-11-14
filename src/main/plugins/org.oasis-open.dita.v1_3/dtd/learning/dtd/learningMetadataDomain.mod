<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Metadata Domain                     -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Metadata Domain//EN" -->
<!--       Delivered as file "learningMetadataDomain.mod"               -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Metadata                  -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009.              -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- - LOM Metadata                                                -->
<!-- - Based on IEEE LOM. Scott Hudson                             -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % lcLom       "lcLom"                                       >
<!ENTITY % lomStructure
                       "lomStructure"                                >
<!ENTITY % lomCoverage "lomCoverage"                                 >
<!ENTITY % lomAggregationLevel
                       "lomAggregationLevel"                         >
<!ENTITY % lomTechRequirement
                       "lomTechRequirement"                          >
<!ENTITY % lomInstallationRemarks
                       "lomInstallationRemarks"                      >
<!ENTITY % lomOtherPlatformRequirements
                       "lomOtherPlatformRequirements"                >
<!ENTITY % lomInteractivityType
                       "lomInteractivityType"                        >
<!ENTITY % lomLearningResourceType
                       "lomLearningResourceType"                     >
<!ENTITY % lomInteractivityLevel
                       "lomInteractivityLevel"                       >
<!ENTITY % lomSemanticDensity
                       "lomSemanticDensity"                          >
<!ENTITY % lomIntendedUserRole
                       "lomIntendedUserRole"                         >
<!ENTITY % lomContext  "lomContext"                                  >
<!ENTITY % lomTypicalAgeRange
                       "lomTypicalAgeRange"                          >
<!ENTITY % lomDifficulty
                       "lomDifficulty"                               >
<!ENTITY % lomTypicalLearningTime
                       "lomTypicalLearningTime"                      >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: LcLom                           -->
<!ENTITY % lcLom.content
                       "(((%lomStructure;)?,
                          (%lomCoverage;)?,
                          (%lomAggregationLevel;)?,
                          (%lomTechRequirement;)?,
                          (%lomInstallationRemarks;)?,
                          (%lomOtherPlatformRequirements;)?,
                          (%lomInteractivityType;)?,
                          (%lomLearningResourceType;)?,
                          (%lomInteractivityLevel;)?,
                          (%lomSemanticDensity;)?,
                          (%lomIntendedUserRole;)?,
                          (%lomContext;)?,
                          (%lomTypicalAgeRange;)?,
                          (%lomDifficulty;)?,
                          (%lomTypicalLearningTime;)?),
                         (%data;)*)"
>
<!ENTITY % lcLom.attributes
              "%univ-atts;
               mapkeyref
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcLom %lcLom.content;>
<!ATTLIST  lcLom %lcLom.attributes;>


<!--                    LONG NAME: LomStructure                    -->
<!ENTITY % lomStructure.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomStructure.attributes
              "name
                          CDATA
                                    'lomStructure'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (atomic |
                           branched |
                           collection |
                           hierarchical |
                           linear |
                           mixed |
                           networked |
                           parceled |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomStructure %lomStructure.content;>
<!ATTLIST  lomStructure %lomStructure.attributes;>


<!--                    LONG NAME: LomCoverage                     -->
<!ENTITY % lomCoverage.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomCoverage.attributes
              "name
                          CDATA
                                    'lomCoverage'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                          CDATA
                                    ''"
>
<!ELEMENT  lomCoverage %lomCoverage.content;>
<!ATTLIST  lomCoverage %lomCoverage.attributes;>


<!--                    LONG NAME: LomAggregationLevel             -->
<!ENTITY % lomAggregationLevel.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomAggregationLevel.attributes
              "name
                          CDATA
                                    'lomAggregationLevel'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED
               value
                                                     (1 |
                           2 |
                           3 |
                           4 |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomAggregationLevel %lomAggregationLevel.content;>
<!ATTLIST  lomAggregationLevel %lomAggregationLevel.attributes;>


<!--                    LONG NAME: LomTechRequirement              -->
<!ENTITY % lomTechRequirement.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomTechRequirement.attributes
              "name
                          CDATA
                                    'lomTechRequirement'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED
               value
                                                     (pc-dos |
                           ms-windows |
                           macos |
                           unix |
                           multi-os |
                           none |
                           any |
                           netscapecommunicator |
                           ms-internetexplorer |
                           opera |
                           amaya |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomTechRequirement %lomTechRequirement.content;>
<!ATTLIST  lomTechRequirement %lomTechRequirement.attributes;>


<!--                    LONG NAME: LomInstallationRemarks          -->
<!ENTITY % lomInstallationRemarks.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomInstallationRemarks.attributes
              "name
                          CDATA
                                    'lomInstallationRemarks'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                          CDATA
                                    ''"
>
<!ELEMENT  lomInstallationRemarks %lomInstallationRemarks.content;>
<!ATTLIST  lomInstallationRemarks %lomInstallationRemarks.attributes;>


<!--                    LONG NAME: LomOtherPlatformRequirements    -->
<!ENTITY % lomOtherPlatformRequirements.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomOtherPlatformRequirements.attributes
              "name
                          CDATA
                                    'lomOtherPlatformRequirements'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                          CDATA
                                    ''"
>
<!ELEMENT  lomOtherPlatformRequirements %lomOtherPlatformRequirements.content;>
<!ATTLIST  lomOtherPlatformRequirements %lomOtherPlatformRequirements.attributes;>


<!--                    LONG NAME: LomInteractivityType            -->
<!ENTITY % lomInteractivityType.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomInteractivityType.attributes
              "name
                          CDATA
                                    'lomInteractivityType'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (active |
                           expositive |
                           mixed |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomInteractivityType %lomInteractivityType.content;>
<!ATTLIST  lomInteractivityType %lomInteractivityType.attributes;>


<!--                    LONG NAME: LomLearningResourceType         -->
<!ENTITY % lomLearningResourceType.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomLearningResourceType.attributes
              "name
                          CDATA
                                    'lomLearningResourceType'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (exercise |
                           simulation |
                           questionnaire |
                           diagram |
                           figure |
                           graph |
                           index |
                           slide |
                           table |
                           narrativetext |
                           exam |
                           experiment |
                           problemstatement |
                           selfassessment |
                           lecture |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomLearningResourceType %lomLearningResourceType.content;>
<!ATTLIST  lomLearningResourceType %lomLearningResourceType.attributes;>


<!--                    LONG NAME: LomInteractivityLevel           -->
<!ENTITY % lomInteractivityLevel.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomInteractivityLevel.attributes
              "name
                          CDATA
                                    'lomInteractivityLevel'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (verylow |
                           low |
                           medium |
                           high |
                           veryhigh |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomInteractivityLevel %lomInteractivityLevel.content;>
<!ATTLIST  lomInteractivityLevel %lomInteractivityLevel.attributes;>


<!--                    LONG NAME: LomSemanticDensity              -->
<!ENTITY % lomSemanticDensity.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomSemanticDensity.attributes
              "name
                          CDATA
                                    'lomSemanticDensity'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (verylow |
                           low |
                           medium |
                           high |
                           veryhigh |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomSemanticDensity %lomSemanticDensity.content;>
<!ATTLIST  lomSemanticDensity %lomSemanticDensity.attributes;>


<!--                    LONG NAME: LomIntendedUserRole             -->
<!ENTITY % lomIntendedUserRole.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomIntendedUserRole.attributes
              "name
                          CDATA
                                    'lomIntendedUserRole'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (teacher |
                           author |
                           learner |
                           manager |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomIntendedUserRole %lomIntendedUserRole.content;>
<!ATTLIST  lomIntendedUserRole %lomIntendedUserRole.attributes;>


<!--                    LONG NAME: LomContext                      -->
<!ENTITY % lomContext.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomContext.attributes
              "name
                          CDATA
                                    'lomContext'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (school |
                           highereducation |
                           training |
                           other |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomContext %lomContext.content;>
<!ATTLIST  lomContext %lomContext.attributes;>


<!--                    LONG NAME: LomTypicalAgeRange              -->
<!ENTITY % lomTypicalAgeRange.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomTypicalAgeRange.attributes
              "name
                          CDATA
                                    'lomTypicalAgeRange'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                          CDATA
                                    #REQUIRED"
>
<!ELEMENT  lomTypicalAgeRange %lomTypicalAgeRange.content;>
<!ATTLIST  lomTypicalAgeRange %lomTypicalAgeRange.attributes;>


<!--                    LONG NAME: LomDifficulty                   -->
<!ENTITY % lomDifficulty.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomDifficulty.attributes
              "name
                          CDATA
                                    'lomDifficulty'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                                                     (veryeasy |
                           easy |
                           medium |
                           difficult |
                           verydifficult |
                           -dita-use-conref-target)
                                    #REQUIRED"
>
<!ELEMENT  lomDifficulty %lomDifficulty.content;>
<!ATTLIST  lomDifficulty %lomDifficulty.attributes;>


<!--                    LONG NAME: LomTypicalLearningTime          -->
<!ENTITY % lomTypicalLearningTime.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lomTypicalLearningTime.attributes
              "name
                          CDATA
                                    'lomTypicalLearningTime'
               datatype
                          CDATA
                                    ''
               %univ-atts;
               value
                          CDATA
                                    ''"
>
<!ELEMENT  lomTypicalLearningTime %lomTypicalLearningTime.content;>
<!ATTLIST  lomTypicalLearningTime %lomTypicalLearningTime.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  lcLom        %global-atts;  class CDATA "+ topic/metadata learningmeta-d/lcLom ">
<!ATTLIST  lomCoverage  %global-atts;  class CDATA "+ topic/data learningmeta-d/lomCoverage ">
<!ATTLIST  lomStructure %global-atts;  class CDATA "+ topic/data learningmeta-d/lomStructure ">
<!ATTLIST  lomAggregationLevel %global-atts;  class CDATA "+ topic/data learningmeta-d/lomAggregationLevel ">
<!ATTLIST  lomTechRequirement %global-atts;  class CDATA "+ topic/data learningmeta-d/lomTechRequirement ">
<!ATTLIST  lomInstallationRemarks %global-atts;  class CDATA "+ topic/data learningmeta-d/lomInstallationRemarks ">
<!ATTLIST  lomOtherPlatformRequirements %global-atts;  class CDATA "+ topic/data learningmeta-d/lomOtherPlatformRequirements ">
<!ATTLIST  lomInteractivityType %global-atts;  class CDATA "+ topic/data learningmeta-d/lomInteractivityType ">
<!ATTLIST  lomLearningResourceType %global-atts;  class CDATA "+ topic/data learningmeta-d/lomLearningResourceType ">
<!ATTLIST  lomInteractivityLevel %global-atts;  class CDATA "+ topic/data learningmeta-d/lomInteractivityLevel ">
<!ATTLIST  lomSemanticDensity %global-atts;  class CDATA "+ topic/data learningmeta-d/lomSemanticDensity ">
<!ATTLIST  lomIntendedUserRole %global-atts;  class CDATA "+ topic/data learningmeta-d/lomIntendedUserRole ">
<!ATTLIST  lomContext   %global-atts;  class CDATA "+ topic/data learningmeta-d/lomContext ">
<!ATTLIST  lomTypicalAgeRange %global-atts;  class CDATA "+ topic/data learningmeta-d/lomTypicalAgeRange ">
<!ATTLIST  lomDifficulty %global-atts;  class CDATA "+ topic/data learningmeta-d/lomDifficulty ">
<!ATTLIST  lomTypicalLearningTime %global-atts;  class CDATA "+ topic/data learningmeta-d/lomTypicalLearningTime ">

<!-- ================== End of DITA Learning Metadata Domain ==================== -->
 