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
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Learning Metadata Domain//EN"
      Delivered as file "learningMetadataDomain.mod"               -->

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
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - LOM Metadata
   - Based on IEEE LOM. Scott Hudson
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!ENTITY % lcLom    "lcLom">
<!ENTITY % lomStructure      "lomStructure">
<!ENTITY % lomCoverage "lomCoverage">
<!ENTITY % lomAggregationLevel "lomAggregationLevel">
<!ENTITY % lomTechRequirement "lomTechRequirement">
<!ENTITY % lomInstallationRemarks "lomInstallationRemarks">
<!ENTITY % lomOtherPlatformRequirements "lomOtherPlatformRequirements">
<!ENTITY % lomInteractivityType "lomInteractivityType">
<!ENTITY % lomLearningResourceType "lomLearningResourceType">
<!ENTITY % lomInteractivityLevel "lomInteractivityLevel">
<!ENTITY % lomSemanticDensity "lomSemanticDensity">
<!ENTITY % lomIntendedUserRole "lomIntendedUserRole">
<!ENTITY % lomContext "lomContext">
<!ENTITY % lomTypicalAgeRange "lomTypicalAgeRange">
<!ENTITY % lomDifficulty "lomDifficulty">
<!ENTITY % lomTypicalLearningTime "lomTypicalLearningTime">

<!ENTITY % lcLom.content
                       "((
                         (%lomStructure;)?, 
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
                         (%data;)* )"
>
<!ENTITY % lcLom.attributes
             "%univ-atts; 
              mapkeyref
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcLom    %lcLom.content;>
<!ATTLIST lcLom    %lcLom.attributes;>


<!ENTITY % lomStructure.content
                       "(#PCDATA)*"
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
                        (atomic|
                         collection|
                         networked|
                         hierarchical|
                         linear| 
                         -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomStructure    %lomStructure.content;>
<!ATTLIST lomStructure    %lomStructure.attributes;>



<!-- IMS LOM metadata -->
<!ENTITY % lomCoverage.content
                       "(#PCDATA)*"
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
<!ELEMENT lomCoverage    %lomCoverage.content;>
<!ATTLIST lomCoverage    %lomCoverage.attributes;>


<!ENTITY % lomAggregationLevel.content
                       "(#PCDATA)*"
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
<!ELEMENT lomAggregationLevel    %lomAggregationLevel.content;>
<!ATTLIST lomAggregationLevel    %lomAggregationLevel.attributes;>


<!ENTITY % lomTechRequirement.content
                       "(#PCDATA)*"
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
                        (pc-dos
                         | ms-windows
                         | macos
                         | unix
                         | multi-os
                         | none
                         | any
                         | netscapecommunicator
                         | ms-internetexplorer
                         | opera
                         | amaya 
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomTechRequirement    %lomTechRequirement.content;>
<!ATTLIST lomTechRequirement    %lomTechRequirement.attributes;>



<!ENTITY % lomInstallationRemarks.content
                       "(#PCDATA)*"
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
<!ELEMENT lomInstallationRemarks    %lomInstallationRemarks.content;>
<!ATTLIST lomInstallationRemarks    %lomInstallationRemarks.attributes;>


<!ENTITY % lomOtherPlatformRequirements.content
                       "(#PCDATA)*"
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
<!ELEMENT lomOtherPlatformRequirements    %lomOtherPlatformRequirements.content;>
<!ATTLIST lomOtherPlatformRequirements    %lomOtherPlatformRequirements.attributes;>


<!ENTITY % lomInteractivityType.content
                       "(#PCDATA)*"
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
                        (active|
                         expositive|
                         mixed| 
                         -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomInteractivityType    %lomInteractivityType.content;>
<!ATTLIST lomInteractivityType    %lomInteractivityType.attributes;>



<!ENTITY % lomLearningResourceType.content
                       "(#PCDATA)*"
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
                        (exercise
                         | simulation
                         | questionnaire
                         | diagram
                         | figure
                         | graph
                         | index
                         | slide
                         | table
                         | narrativetext
                         | exam
                         | experiment
                         | problemstatement
                         | selfassessment
                         | lecture 
                         | -dita-use-conref-target)
                                  #REQUIRED">
<!ELEMENT lomLearningResourceType    %lomLearningResourceType.content;>
<!ATTLIST lomLearningResourceType    %lomLearningResourceType.attributes;>



<!ENTITY % lomInteractivityLevel.content
                       "(#PCDATA)*"
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
                        (verylow
                         | low
                         | medium
                         | high
                         | veryhigh 
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomInteractivityLevel    %lomInteractivityLevel.content;>
<!ATTLIST lomInteractivityLevel    %lomInteractivityLevel.attributes;>


<!ENTITY % lomSemanticDensity.content
                       "(#PCDATA)*                                   ">
<!ENTITY % lomSemanticDensity.attributes
             "name
                        CDATA
                                  'lomSemanticDensity'
              datatype
                        CDATA
                                  ''
              %univ-atts;
              value
                        (verylow
                         | low
                         | medium
                         | high
                         | veryhigh
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomSemanticDensity    %lomSemanticDensity.content;>
<!ATTLIST lomSemanticDensity    %lomSemanticDensity.attributes;>


<!ENTITY % lomIntendedUserRole.content
                       "(#PCDATA)*"
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
                        (teacher
                         | author
                         | learner
                         | manager
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomIntendedUserRole    %lomIntendedUserRole.content;>
<!ATTLIST lomIntendedUserRole    %lomIntendedUserRole.attributes;>



<!ENTITY % lomContext.content
                       "(#PCDATA)*"
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
                        (school
                         |highereducation
                         |training
                         |other 
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomContext    %lomContext.content;>
<!ATTLIST lomContext    %lomContext.attributes;>



<!ENTITY % lomTypicalAgeRange.content
                       "(#PCDATA)*"
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
<!ELEMENT lomTypicalAgeRange    %lomTypicalAgeRange.content;>
<!ATTLIST lomTypicalAgeRange    %lomTypicalAgeRange.attributes;>



<!ENTITY % lomDifficulty.content
                       "(#PCDATA)*"
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
                        (veryeasy
                         | easy
                         | medium
                         | difficult
                         | verydifficult
                         | -dita-use-conref-target)
                                  #REQUIRED"
>
<!ELEMENT lomDifficulty    %lomDifficulty.content;>
<!ATTLIST lomDifficulty    %lomDifficulty.attributes;>



<!ENTITY % lomTypicalLearningTime.content
                       "(#PCDATA)*"
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
<!ELEMENT lomTypicalLearningTime    %lomTypicalLearningTime.content;>
<!ATTLIST lomTypicalLearningTime    %lomTypicalLearningTime.attributes;>


 
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - ENTITY DECLARATIONS FOR IMS LOM as ELEMENTS IN PROLOG
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
<!ATTLIST lcLom                         %global-atts; 
    class CDATA "+ topic/metadata learningmeta-d/lcLom "  >
<!ATTLIST lomCoverage                   %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomCoverage "  >
<!ATTLIST lomStructure                  %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomStructure "  >
<!ATTLIST lomAggregationLevel           %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomAggregationLevel "  >
<!ATTLIST lomTechRequirement            %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomTechRequirement "  >
<!ATTLIST lomInstallationRemarks        %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomInstallationRemarks "  >
<!ATTLIST lomOtherPlatformRequirements  %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomOtherPlatformRequirements "  >
<!ATTLIST lomInteractivityType          %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomInteractivityType "  >
<!ATTLIST lomLearningResourceType       %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomLearningResourceType "  >
<!ATTLIST lomInteractivityLevel         %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomInteractivityLevel "  >
<!ATTLIST lomSemanticDensity            %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomSemanticDensity "  >
<!ATTLIST lomIntendedUserRole           %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomIntendedUserRole "  >
<!ATTLIST lomContext                    %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomContext "  >
<!ATTLIST lomTypicalAgeRange            %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomTypicalAgeRange "  >
<!ATTLIST lomDifficulty                 %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomDifficulty "  >
<!ATTLIST lomTypicalLearningTime        %global-atts; 
    class CDATA "+ topic/data learningmeta-d/lomTypicalLearningTime "  >

