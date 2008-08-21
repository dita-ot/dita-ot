<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Learning Domain                              -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      May 2007                                          -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Learning Domain//EN"
      Delivered as file "learningDomain.mod"                      -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Domain                    -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2008.              -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->

 

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Assessment interactions
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - ENTITY DECLARATIONS FOR DOMAIN SUBSTITUTION
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

<!ENTITY % lcInstructornote "lcInstructornote">
<!ENTITY % lcTrueFalse              "lcTrueFalse">
<!ENTITY % lcSingleSelect           "lcSingleSelect">
<!ENTITY % lcMultipleSelect         "lcMultipleSelect">
<!ENTITY % lcSequencing             "lcSequencing">
<!ENTITY % lcMatching               "lcMatching">
<!ENTITY % lcHotspot                "lcHotspot">
<!ENTITY % lcOpenQuestion           "lcOpenQuestion">

<!ENTITY % lcQuestion               "lcQuestion">
<!ENTITY % lcOpenAnswer             "lcOpenAnswer">
<!ENTITY % lcAnswerOptionGroup    "lcAnswerOptionGroup">
<!ENTITY % lcAsset                  "lcAsset">
<!ENTITY % lcFeedbackCorrect        "lcFeedbackCorrect">
<!ENTITY % lcFeedbackIncorrect      "lcFeedbackIncorrect">
<!ENTITY % lcAnswerOption         "lcAnswerOption">
<!ENTITY % lcAnswerContent          "lcAnswerContent">
<!ENTITY % lcSequenceOptionGroup    "lcSequenceOptionGroup">
<!ENTITY % lcSequenceOption         "lcSequenceOption">
<!ENTITY % lcSequence               "lcSequence">

<!ENTITY % lcMatchTable             "lcMatchTable">
<!ENTITY % lcMatchingHeader         "lcMatchingHeader">
<!ENTITY % lcMatchingPair           "lcMatchingPair">
<!ENTITY % lcItem                   "lcItem">
<!ENTITY % lcMatchingItem           "lcMatchingItem">

<!ENTITY % lcHotspotMap             "lcHotspotMap">
<!ENTITY % lcArea                   "lcArea">

<!ENTITY % lcCorrectResponse        "lcCorrectResponse">
<!ENTITY % lcFeedback               "lcFeedback">


<!ENTITY % lcInstructornote.content
                       "(%note.cnt;)* "
>
<!ENTITY % lcInstructornote.attributes
             "spectitle
                        CDATA
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcInstructornote    %lcInstructornote.content;>
<!ATTLIST lcInstructornote    %lcInstructornote.attributes;>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - INTERACTION DEFINITIONS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!ENTITY % lcTrueFalse.content
                       "((%title;)?,
                         (%lcQuestion;), 
                         (%lcAsset;)?,
                         (%lcAnswerOptionGroup;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcTrueFalse.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcTrueFalse    %lcTrueFalse.content;>
<!ATTLIST lcTrueFalse    %lcTrueFalse.attributes;>


<!ENTITY % lcSingleSelect.content
                       "((%title;)?,
                         (%lcQuestion;), 
                         (%lcAsset;)?,
                         (%lcAnswerOptionGroup;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcSingleSelect.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcSingleSelect    %lcSingleSelect.content;>
<!ATTLIST lcSingleSelect    %lcSingleSelect.attributes;>


<!ENTITY % lcMultipleSelect.content
                       "((%title;)?,
                         (%lcQuestion;), 
                         (%lcAsset;)?,
                         (%lcAnswerOptionGroup;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcMultipleSelect.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMultipleSelect    %lcMultipleSelect.content;>
<!ATTLIST lcMultipleSelect    %lcMultipleSelect.attributes;>


<!ENTITY % lcSequencing.content
                       "((%title;)?,
                         (%lcQuestion;),
                         (%lcAsset;)?,
                         (%lcSequenceOptionGroup;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcSequencing.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcSequencing    %lcSequencing.content;>
<!ATTLIST lcSequencing    %lcSequencing.attributes;>


<!ENTITY % lcMatching.content
                       "((%title;)?,
                         (%lcQuestion;),
                         (%lcAsset;)?,
                         (%lcMatchTable;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcMatching.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMatching    %lcMatching.content;>
<!ATTLIST lcMatching    %lcMatching.attributes;>


<!ENTITY % lcHotspot.content
                       "((%title;)?,
                         (%lcQuestion;),
                         (%lcHotspotMap;),
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcHotspot.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcHotspot    %lcHotspot.content;>
<!ATTLIST lcHotspot    %lcHotspot.attributes;>


<!ENTITY % lcOpenQuestion.content
                       "((%title;)?,
                         (%lcQuestion;), 
                         (%lcAsset;)?,
                         (%lcOpenAnswer;)?,
                         (%lcFeedbackIncorrect;)?,
                         (%lcFeedbackCorrect;)?,
                         (%data;)*)"
>
<!ENTITY % lcOpenQuestion.attributes
             "id
                        NMTOKEN
                                  #REQUIRED
              %conref-atts;
              %select-atts;
              %localization-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcOpenQuestion    %lcOpenQuestion.content;>
<!ATTLIST lcOpenQuestion    %lcOpenQuestion.attributes;>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - OPTION DEFINITIONS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!ENTITY % lcQuestion.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcQuestion.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcQuestion    %lcQuestion.content;>
<!ATTLIST lcQuestion    %lcQuestion.attributes;>


<!ENTITY % lcOpenAnswer.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcOpenAnswer.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcOpenAnswer    %lcOpenAnswer.content;>
<!ATTLIST lcOpenAnswer    %lcOpenAnswer.attributes;>


<!ENTITY % lcAnswerOptionGroup.content
                       "((%lcAnswerOption;)+)"
>
<!ENTITY % lcAnswerOptionGroup.attributes
             "%univ-atts; 
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcAnswerOptionGroup    %lcAnswerOptionGroup.content;>
<!ATTLIST lcAnswerOptionGroup    %lcAnswerOptionGroup.attributes;>


<!ENTITY % lcSequenceOptionGroup.content
                       "((%lcSequenceOption;)+)"
>
<!ENTITY % lcSequenceOptionGroup.attributes
             "%univ-atts; 
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcSequenceOptionGroup    %lcSequenceOptionGroup.content;>
<!ATTLIST lcSequenceOptionGroup    %lcSequenceOptionGroup.attributes;>


									
<!ENTITY % lcAsset.content
                       "((%imagemap; | 
                          %image; | 
                          %object;)*)"
>
<!ENTITY % lcAsset.attributes
             "%univ-atts; 
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcAsset    %lcAsset.content;>
<!ATTLIST lcAsset    %lcAsset.attributes;>


<!ENTITY % lcSequenceOption.content
                       "((%lcAnswerContent;),
                         (%lcSequence;))"
>
<!ENTITY % lcSequenceOption.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcSequenceOption    %lcSequenceOption.content;>
<!ATTLIST lcSequenceOption    %lcSequenceOption.attributes;>


<!ENTITY % lcFeedback.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcFeedback.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcFeedback    %lcFeedback.content;>
<!ATTLIST lcFeedback    %lcFeedback.attributes;>


<!ENTITY % lcFeedbackCorrect.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcFeedbackCorrect.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcFeedbackCorrect    %lcFeedbackCorrect.content;>
<!ATTLIST lcFeedbackCorrect    %lcFeedbackCorrect.attributes;>


<!ENTITY % lcFeedbackIncorrect.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcFeedbackIncorrect.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcFeedbackIncorrect    %lcFeedbackIncorrect.content;>
<!ATTLIST lcFeedbackIncorrect    %lcFeedbackIncorrect.attributes;>


<!ENTITY % lcAnswerOption.content
                       "((%lcAnswerContent;),
                         (%lcCorrectResponse;)?,
                         (%lcFeedback;)? )"
>
<!ENTITY % lcAnswerOption.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcAnswerOption    %lcAnswerOption.content;>
<!ATTLIST lcAnswerOption    %lcAnswerOption.attributes;>


<!ENTITY % lcAnswerContent.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcAnswerContent.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcAnswerContent    %lcAnswerContent.content;>
<!ATTLIST lcAnswerContent    %lcAnswerContent.attributes;>


<!ENTITY % lcMatchTable.content
                       "((%lcMatchingHeader;)?,
                         (%lcMatchingPair;)+)"
>
<!ENTITY % lcMatchTable.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMatchTable    %lcMatchTable.content;>
<!ATTLIST lcMatchTable    %lcMatchTable.attributes;>


<!ENTITY % lcMatchingHeader.content
                       "((%lcItem;),
                         (%lcMatchingItem;))"
>
<!ENTITY % lcMatchingHeader.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMatchingHeader    %lcMatchingHeader.content;>
<!ATTLIST lcMatchingHeader    %lcMatchingHeader.attributes;>


<!ENTITY % lcMatchingPair.content
                       "((%lcItem;),
                         (%lcMatchingItem;))">
<!ENTITY % lcMatchingPair.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMatchingPair    %lcMatchingPair.content;>
<!ATTLIST lcMatchingPair    %lcMatchingPair.attributes;>


<!ENTITY % lcItem.content
                       "(%ph.cnt;)*"
>
<!ENTITY % lcItem.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcItem    %lcItem.content;>
<!ATTLIST lcItem    %lcItem.attributes;>


<!ENTITY % lcMatchingItem.content
                       "(%ph.cnt; )*"
>
<!ENTITY % lcMatchingItem.attributes
             "%univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcMatchingItem    %lcMatchingItem.content;>
<!ATTLIST lcMatchingItem    %lcMatchingItem.attributes;>


<!ENTITY % lcHotspotMap.content
                       "((%image;),
                         (%lcArea;)+)"
>
<!ENTITY % lcHotspotMap.attributes
             "%display-atts;
              spectitle
                        CDATA
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcHotspotMap    %lcHotspotMap.content;>
<!ATTLIST lcHotspotMap    %lcHotspotMap.attributes;>


<!ENTITY % lcArea.content
                       "((%shape;),
                         (%coords;),
                         (%xref;)?, 
                         (%lcCorrectResponse;)?,
                         (%lcFeedback;)?)"
>
<!ENTITY % lcArea.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcArea    %lcArea.content;>
<!ATTLIST lcArea    %lcArea.attributes;>



<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - CHOICE DEFINITIONS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!ENTITY % lcCorrectResponse.content
                       "EMPTY">
<!ENTITY % lcCorrectResponse.attributes
             "name
                        CDATA
                                  'lcCorrectResponse'
              value
                        CDATA
                                  'lcCorrectResponse'
              %univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcCorrectResponse    %lcCorrectResponse.content;>
<!ATTLIST lcCorrectResponse    %lcCorrectResponse.attributes;>


<!ENTITY % lcSequence.content
                       "EMPTY">
<!ENTITY % lcSequence.attributes
             "name
                       CDATA
                                 'lcSequence'
              value
                        CDATA
                                  #REQUIRED
              %univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT lcSequence    %lcSequence.content;>
<!ATTLIST lcSequence    %lcSequence.attributes;>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - CLASS ATTRIBUTES FOR ANCESTRY DECLARATION
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!ATTLIST lcInstructornote        %global-atts; 
    class CDATA "+ topic/note learning-d/lcInstructornote ">

<!ATTLIST lcTrueFalse %global-atts;
    class CDATA "+ topic/fig learning-d/lcTrueFalse ">
<!ATTLIST lcSingleSelect %global-atts;
    class CDATA "+ topic/fig learning-d/lcSingleSelect ">
<!ATTLIST lcMultipleSelect %global-atts;
    class CDATA "+ topic/fig learning-d/lcMultipleSelect ">
<!ATTLIST lcSequencing %global-atts;
    class CDATA "+ topic/fig learning-d/lcSequencing ">
<!ATTLIST lcMatching %global-atts;
    class CDATA "+ topic/fig learning-d/lcMatching ">
<!ATTLIST lcHotspot %global-atts;
    class CDATA "+ topic/fig learning-d/lcHotspot ">
<!ATTLIST lcOpenQuestion %global-atts;
    class CDATA "+ topic/fig learning-d/lcOpenQuestion ">


<!ATTLIST lcQuestion %global-atts;
    class CDATA "+ topic/p learning-d/lcQuestion ">
<!ATTLIST lcOpenAnswer %global-atts;
    class CDATA "+ topic/p learning-d/lcOpenAnswer ">
<!ATTLIST lcAsset %global-atts;
    class CDATA "+ topic/p learning-d/lcAsset ">
<!ATTLIST lcFeedback %global-atts;
    class CDATA "+ topic/p learning-d/lcFeedback ">
<!ATTLIST lcFeedbackCorrect %global-atts;
    class CDATA "+ topic/p learning-d/lcFeedbackCorrect ">
<!ATTLIST lcFeedbackIncorrect %global-atts;
    class CDATA "+ topic/p learning-d/lcFeedbackIncorrect ">
<!ATTLIST lcAnswerOption %global-atts;
    class CDATA "+ topic/li learning-d/lcAnswerOption ">
<!ATTLIST lcAnswerOptionGroup     %global-atts; 
    class CDATA "+ topic/ul learning-d/lcAnswerOptionGroup ">
<!ATTLIST lcAnswerContent %global-atts;
    class CDATA "+ topic/p learning-d/lcAnswerContent ">
<!ATTLIST lcMatchTable %global-atts;
    class CDATA "+ topic/simpletable learning-d/lcMatchTable ">
<!ATTLIST lcMatchingHeader %global-atts;
    class CDATA "+ topic/sthead learning-d/lcMatchingHeader ">
<!ATTLIST lcMatchingPair %global-atts;
    class CDATA "+ topic/strow learning-d/lcMatchingPair ">
<!ATTLIST lcItem %global-atts;
    class CDATA "+ topic/stentry learning-d/lcItem ">
<!ATTLIST lcMatchingItem %global-atts;
    class CDATA "+ topic/stentry learning-d/lcMatchingItem ">
<!ATTLIST lcSequenceOptionGroup     %global-atts; 
    class CDATA "+ topic/ol learning-d/lcSequenceOptionGroup ">
<!ATTLIST lcSequenceOption %global-atts;
    class CDATA "+ topic/li learning-d/lcSequenceOption ">
<!ATTLIST lcSequence %global-atts;
    class CDATA "+ topic/data learning-d/lcSequence ">
<!ATTLIST lcCorrectResponse %global-atts;
    class CDATA "+ topic/data learning-d/lcCorrectResponse ">

<!ATTLIST lcHotspotMap %global-atts; 
   class CDATA "+ topic/fig learning-d/lcHotspotMap " >
<!ATTLIST lcArea       %global-atts; 
   class CDATA "+ topic/figgroup learning-d/lcArea ">
