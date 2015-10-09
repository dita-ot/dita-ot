<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning 2 Domain                              -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                        -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning 2 Domain//EN"        -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Domain                    -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2014                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2014                     -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!-- =============================================================   -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % lcInstructornote2
                       "lcInstructornote2"                           >
<!ENTITY % lcTrueFalse2
                       "lcTrueFalse2"                                >
<!ENTITY % lcSingleSelect2
                       "lcSingleSelect2"                             >
<!ENTITY % lcMultipleSelect2
                       "lcMultipleSelect2"                           >
<!ENTITY % lcSequencing2
                       "lcSequencing2"                               >
<!ENTITY % lcMatching2 "lcMatching2"                                 >
<!ENTITY % lcHotspot2  "lcHotspot2"                                  >
<!ENTITY % lcOpenQuestion2
                       "lcOpenQuestion2"                             >
<!ENTITY % lcQuestion2 "lcQuestion2"                                 >
<!ENTITY % lcOpenAnswer2
                       "lcOpenAnswer2"                               >
<!ENTITY % lcAnswerOptionGroup2
                       "lcAnswerOptionGroup2"                        >
<!ENTITY % lcSequenceOptionGroup2
                       "lcSequenceOptionGroup2"                      >
<!ENTITY % lcAsset2    "lcAsset2"                                    >
<!ENTITY % lcSequenceOption2
                       "lcSequenceOption2"                           >
<!ENTITY % lcFeedback2 "lcFeedback2"                                 >
<!ENTITY % lcFeedbackCorrect2
                       "lcFeedbackCorrect2"                          >
<!ENTITY % lcFeedbackIncorrect2
                       "lcFeedbackIncorrect2"                        >
<!ENTITY % lcAnswerOption2
                       "lcAnswerOption2"                             >
<!ENTITY % lcAnswerContent2
                       "lcAnswerContent2"                            >
<!ENTITY % lcMatchTable2
                       "lcMatchTable2"                               >
<!ENTITY % lcMatchingHeader2
                       "lcMatchingHeader2"                           >
<!ENTITY % lcMatchingPair2
                       "lcMatchingPair2"                             >
<!ENTITY % lcItem2     "lcItem2"                                     >
<!ENTITY % lcMatchingItem2
                       "lcMatchingItem2"                             >
<!ENTITY % lcMatchingItemFeedback2
                       "lcMatchingItemFeedback2"                     >
<!ENTITY % lcHotspotMap2
                       "lcHotspotMap2"                               >
<!ENTITY % lcArea2     "lcArea2"                                     >
<!ENTITY % lcAreaShape2
                       "lcAreaShape2"                                >
<!ENTITY % lcAreaCoords2
                       "lcAreaCoords2"                               >
<!ENTITY % lcCorrectResponse2
                       "lcCorrectResponse2"                          >
<!ENTITY % lcSequence2 "lcSequence2"                                 >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Instructor note                 -->
<!ENTITY % lcInstructornote2.content
                       "(%note.cnt;)*"
>
<!ENTITY % lcInstructornote2.attributes
              "spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInstructornote2 %lcInstructornote2.content;>
<!ATTLIST  lcInstructornote2 %lcInstructornote2.attributes;>


<!--                    LONG NAME: True or false interaction       -->
<!ENTITY % lcTrueFalse2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcAnswerOptionGroup2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcTrueFalse2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcTrueFalse2 %lcTrueFalse2.content;>
<!ATTLIST  lcTrueFalse2 %lcTrueFalse2.attributes;>


<!--                    LONG NAME: Single selection interaction    -->
<!ENTITY % lcSingleSelect2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcAnswerOptionGroup2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcSingleSelect2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSingleSelect2 %lcSingleSelect2.content;>
<!ATTLIST  lcSingleSelect2 %lcSingleSelect2.attributes;>


<!--                    LONG NAME: Multiple selection interaction  -->
<!ENTITY % lcMultipleSelect2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcAnswerOptionGroup2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcMultipleSelect2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMultipleSelect2 %lcMultipleSelect2.content;>
<!ATTLIST  lcMultipleSelect2 %lcMultipleSelect2.attributes;>


<!--                    LONG NAME: Sequencing interaction          -->
<!ENTITY % lcSequencing2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcSequenceOptionGroup2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcSequencing2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSequencing2 %lcSequencing2.content;>
<!ATTLIST  lcSequencing2 %lcSequencing2.attributes;>


<!--                    LONG NAME: Matching interaction            -->
<!ENTITY % lcMatching2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcMatchTable2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcMatching2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatching2 %lcMatching2.content;>
<!ATTLIST  lcMatching2 %lcMatching2.attributes;>


<!--                    LONG NAME: Hotspot interaction             -->
<!ENTITY % lcHotspot2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcHotspotMap2;),
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcHotspot2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcHotspot2 %lcHotspot2.content;>
<!ATTLIST  lcHotspot2 %lcHotspot2.attributes;>


<!--                    LONG NAME: Open-ended question interaction -->
<!ENTITY % lcOpenQuestion2.content
                       "((%lcInteractionLabel2;)?,
                         (%lcQuestion2;),
                         (%lcAsset2;)*,
                         (%lcOpenAnswer2;)?,
                         (%lcFeedbackIncorrect2;)?,
                         (%lcFeedbackCorrect2;)?,
                         (%data;)*)"
>
<!ENTITY % lcOpenQuestion2.attributes
              "id
                          NMTOKEN
                                    #IMPLIED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcOpenQuestion2 %lcOpenQuestion2.content;>
<!ATTLIST  lcOpenQuestion2 %lcOpenQuestion2.attributes;>


<!--                    LONG NAME: Question (prompt)               -->
<!ENTITY % lcQuestion2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcQuestion2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcQuestion2 %lcQuestion2.content;>
<!ATTLIST  lcQuestion2 %lcQuestion2.attributes;>


<!--                    LONG NAME: Open answer                     -->
<!ENTITY % lcOpenAnswer2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcOpenAnswer2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcOpenAnswer2 %lcOpenAnswer2.content;>
<!ATTLIST  lcOpenAnswer2 %lcOpenAnswer2.attributes;>


<!--                    LONG NAME: Answer option group             -->
<!ENTITY % lcAnswerOptionGroup2.content
                       "((%data; |
                          %data-about;)*,
                         (%lcAnswerOption2;)+)"
>
<!ENTITY % lcAnswerOptionGroup2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAnswerOptionGroup2 %lcAnswerOptionGroup2.content;>
<!ATTLIST  lcAnswerOptionGroup2 %lcAnswerOptionGroup2.attributes;>


<!--                    LONG NAME: Sequence option group           -->
<!ENTITY % lcSequenceOptionGroup2.content
                       "((%data; |
                          %data-about;)*,
                         (%lcSequenceOption2;)+)"
>
<!ENTITY % lcSequenceOptionGroup2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSequenceOptionGroup2 %lcSequenceOptionGroup2.content;>
<!ATTLIST  lcSequenceOptionGroup2 %lcSequenceOptionGroup2.attributes;>


<!--                    LONG NAME: Interaction asset               -->
<!ENTITY % lcAsset2.content
                       "(%imagemap; |
                         %image; |
                         %object;)*"
>
<!ENTITY % lcAsset2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAsset2 %lcAsset2.content;>
<!ATTLIST  lcAsset2 %lcAsset2.attributes;>


<!--                    LONG NAME: Sequence option                 -->
<!ENTITY % lcSequenceOption2.content
                       "((%lcAnswerContent2;),
                         (%lcSequence2;))"
>
<!ENTITY % lcSequenceOption2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSequenceOption2 %lcSequenceOption2.content;>
<!ATTLIST  lcSequenceOption2 %lcSequenceOption2.attributes;>


<!--                    LONG NAME: Feedback                        -->
<!ENTITY % lcFeedback2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcFeedback2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcFeedback2 %lcFeedback2.content;>
<!ATTLIST  lcFeedback2 %lcFeedback2.attributes;>


<!--                    LONG NAME: Correct-response feedback       -->
<!ENTITY % lcFeedbackCorrect2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcFeedbackCorrect2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcFeedbackCorrect2 %lcFeedbackCorrect2.content;>
<!ATTLIST  lcFeedbackCorrect2 %lcFeedbackCorrect2.attributes;>


<!--                    LONG NAME: Incorrect-response feedback     -->
<!ENTITY % lcFeedbackIncorrect2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcFeedbackIncorrect2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcFeedbackIncorrect2 %lcFeedbackIncorrect2.content;>
<!ATTLIST  lcFeedbackIncorrect2 %lcFeedbackIncorrect2.attributes;>


<!--                    LONG NAME: Answer option                   -->
<!ENTITY % lcAnswerOption2.content
                       "((%lcAnswerContent2;),
                         (%lcCorrectResponse2;)?,
                         (%lcFeedback2;)?)"
>
<!ENTITY % lcAnswerOption2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAnswerOption2 %lcAnswerOption2.content;>
<!ATTLIST  lcAnswerOption2 %lcAnswerOption2.attributes;>


<!--                    LONG NAME: Answer content                  -->
<!ENTITY % lcAnswerContent2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcAnswerContent2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAnswerContent2 %lcAnswerContent2.content;>
<!ATTLIST  lcAnswerContent2 %lcAnswerContent2.attributes;>


<!--                    LONG NAME: Matching table                  -->
<!ENTITY % lcMatchTable2.content
                       "((%lcMatchingHeader2;)?,
                         (%lcMatchingPair2;)+)"
>
<!ENTITY % lcMatchTable2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatchTable2 %lcMatchTable2.content;>
<!ATTLIST  lcMatchTable2 %lcMatchTable2.attributes;>


<!--                    LONG NAME: Matching table header           -->
<!ENTITY % lcMatchingHeader2.content
                       "((%lcItem2;),
                         (%lcMatchingItem2;))"
>
<!ENTITY % lcMatchingHeader2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatchingHeader2 %lcMatchingHeader2.content;>
<!ATTLIST  lcMatchingHeader2 %lcMatchingHeader2.attributes;>


<!--                    LONG NAME: Matching assessment pair        -->
<!ENTITY % lcMatchingPair2.content
                       "((%lcItem2;),
                         (%lcMatchingItem2;),
                         (%lcMatchingItemFeedback2;)?)"
>
<!ENTITY % lcMatchingPair2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatchingPair2 %lcMatchingPair2.content;>
<!ATTLIST  lcMatchingPair2 %lcMatchingPair2.attributes;>


<!--                    LONG NAME: Item to be matched              -->
<!ENTITY % lcItem2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcItem2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcItem2 %lcItem2.content;>
<!ATTLIST  lcItem2 %lcItem2.attributes;>


<!--                    LONG NAME: Matching item                   -->
<!ENTITY % lcMatchingItem2.content
                       "(%div.cnt;)*"
>
<!ENTITY % lcMatchingItem2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatchingItem2 %lcMatchingItem2.content;>
<!ATTLIST  lcMatchingItem2 %lcMatchingItem2.attributes;>


<!--                    LONG NAME: Matching item feedback          -->
<!ENTITY % lcMatchingItemFeedback2.content
                       "(%lcFeedback2; |
                         %lcFeedbackCorrect2; |
                         %lcFeedbackIncorrect2;)*"
>
<!ENTITY % lcMatchingItemFeedback2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMatchingItemFeedback2 %lcMatchingItemFeedback2.content;>
<!ATTLIST  lcMatchingItemFeedback2 %lcMatchingItemFeedback2.attributes;>


<!--                    LONG NAME: Hot spot map interaction        -->
<!ENTITY % lcHotspotMap2.content
                       "((%image;),
                         (%lcArea2;)+)"
>
<!ENTITY % lcHotspotMap2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcHotspotMap2 %lcHotspotMap2.content;>
<!ATTLIST  lcHotspotMap2 %lcHotspotMap2.attributes;>


<!--                    LONG NAME: Hotspot area                    -->
<!ENTITY % lcArea2.content
                       "((%lcAreaShape2;),
                         (%lcAreaCoords2;),
                         (%xref;)?,
                         (%lcCorrectResponse2;)?,
                         (%lcFeedback2;)?)"
>
<!ENTITY % lcArea2.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcArea2 %lcArea2.content;>
<!ATTLIST  lcArea2 %lcArea2.attributes;>


<!--                    LONG NAME: Hotspot area shape              -->
<!ENTITY % lcAreaShape2.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % lcAreaShape2.attributes
              "keyref
                          CDATA
                                    #IMPLIED
               %univ-atts-translate-no;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAreaShape2 %lcAreaShape2.content;>
<!ATTLIST  lcAreaShape2 %lcAreaShape2.attributes;>


<!--                    LONG NAME: Hotspot area coordinates        -->
<!ENTITY % lcAreaCoords2.content
                       "(%words.cnt;)*"
>
<!ENTITY % lcAreaCoords2.attributes
              "keyref
                          CDATA
                                    #IMPLIED
               %univ-atts-translate-no;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAreaCoords2 %lcAreaCoords2.content;>
<!ATTLIST  lcAreaCoords2 %lcAreaCoords2.attributes;>


<!--                    LONG NAME: Correct response                -->
<!ENTITY % lcCorrectResponse2.content
                       "EMPTY"
>
<!ENTITY % lcCorrectResponse2.attributes
              "name
                          CDATA
                                    'lcCorrectResponse2'
               value
                          CDATA
                                    'lcCorrectResponse2'
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcCorrectResponse2 %lcCorrectResponse2.content;>
<!ATTLIST  lcCorrectResponse2 %lcCorrectResponse2.attributes;>


<!--                    LONG NAME: Sequence position               -->
<!ENTITY % lcSequence2.content
                       "EMPTY"
>
<!ENTITY % lcSequence2.attributes
              "name
                          CDATA
                                    'lcSequence2'
               value
                          CDATA
                                    #REQUIRED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSequence2 %lcSequence2.content;>
<!ATTLIST  lcSequence2 %lcSequence2.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  lcInstructornote2 %global-atts;  class CDATA "+ topic/note learningInteractionBase2-d/note learning2-d/lcInstructornote2 ">
<!ATTLIST  lcTrueFalse2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcTrueFalse2 ">
<!ATTLIST  lcSingleSelect2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSingleSelect2 ">
<!ATTLIST  lcMultipleSelect2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMultipleSelect2 ">
<!ATTLIST  lcSequencing2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSequencing2 ">
<!ATTLIST  lcMatching2  %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMatching2 ">
<!ATTLIST  lcHotspot2   %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcHotspot2 ">
<!ATTLIST  lcOpenQuestion2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcOpenQuestion2 ">
<!ATTLIST  lcQuestion2  %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/lcQuestionBase2 learning2-d/lcQuestion2 ">
<!ATTLIST  lcOpenAnswer2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcOpenAnswer2 ">
<!ATTLIST  lcAsset2     %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcAsset2 ">
<!ATTLIST  lcFeedback2  %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedback2 ">
<!ATTLIST  lcFeedbackCorrect2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackCorrect2 ">
<!ATTLIST  lcFeedbackIncorrect2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackIncorrect2 ">
<!ATTLIST  lcAnswerOption2 %global-atts;  class CDATA "+ topic/li learningInteractionBase2-d/li learning2-d/lcAnswerOption2 ">
<!ATTLIST  lcAnswerOptionGroup2 %global-atts;  class CDATA "+ topic/ul learningInteractionBase2-d/ul learning2-d/lcAnswerOptionGroup2 ">
<!ATTLIST  lcAnswerContent2 %global-atts;  class CDATA "+ topic/div learningInteractionBase2-d/div learning2-d/lcAnswerContent2 ">
<!ATTLIST  lcMatchTable2 %global-atts;  class CDATA "+ topic/simpletable learningInteractionBase2-d/simpletable learning2-d/lcMatchTable2 ">
<!ATTLIST  lcMatchingHeader2 %global-atts;  class CDATA "+ topic/sthead learningInteractionBase2-d/sthead learning2-d/lcMatchingHeader2 ">
<!ATTLIST  lcMatchingPair2 %global-atts;  class CDATA "+ topic/strow learningInteractionBase2-d/strow learning2-d/lcMatchingPair2 ">
<!ATTLIST  lcItem2      %global-atts;  class CDATA "+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcItem2 ">
<!ATTLIST  lcMatchingItem2 %global-atts;  class CDATA "+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItem2 ">
<!ATTLIST  lcMatchingItemFeedback2 %global-atts;  class CDATA "+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItemFeedback2 ">
<!ATTLIST  lcSequenceOptionGroup2 %global-atts;  class CDATA "+ topic/ol learningInteractionBase2-d/ol learning2-d/lcSequenceOptionGroup2 ">
<!ATTLIST  lcSequenceOption2 %global-atts;  class CDATA "+ topic/li learningInteractionBase2-d/li learning2-d/lcSequenceOption2 ">
<!ATTLIST  lcSequence2  %global-atts;  class CDATA "+ topic/data learningInteractionBase2-d/data learning2-d/lcSequence2 ">
<!ATTLIST  lcCorrectResponse2 %global-atts;  class CDATA "+ topic/data learningInteractionBase2-d/data learning2-d/lcCorrectResponse2 ">
<!ATTLIST  lcHotspotMap2 %global-atts;  class CDATA "+ topic/fig learningInteractionBase2-d/figgroup learning2-d/lcHotspotMap2 ">
<!ATTLIST  lcArea2      %global-atts;  class CDATA "+ topic/figgroup learningInteractionBase2-d/figgroup learning2-d/lcArea2 ">
<!ATTLIST  lcAreaShape2 %global-atts;  class CDATA "+ topic/keyword learningInteractionBase2-d/keyword learning2-d/lcAreaShape2 ">
<!ATTLIST  lcAreaCoords2 %global-atts;  class CDATA "+ topic/ph learningInteractionBase2-d/ph learning2-d/lcAreaCoords2 ">

<!-- ================== End of DITA Learning 2 Domain ==================== -->
 