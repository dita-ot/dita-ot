<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % tutorialIntro                "tutorialIntro">
<!ENTITY % tutorialIntroBody            "tutorialIntroBody">
<!ENTITY % longDesc                     "longDesc">
<!ENTITY % skillLevel                   "skillLevel">
<!ENTITY % audienceDesc                 "audienceDesc">
<!ENTITY % systemRequirements           "systemRequirements">
<!ENTITY % prerequisites                "prerequisites">
<!ENTITY % expectedResults              "expectedResults">
<!ENTITY % conventionsUsed              "conventionsUsed">
<!ENTITY % tutorialIntro-info-types     "%info-types;">

<!ELEMENT tutorialIntro         ((%title;), (%titlealts;)?, (%shortdesc; | %abstract;), (%prolog;)?, (%tutorialIntroBody;), (%related-links;)?, (%tutorialIntro-info-types;) )>
<!ATTLIST tutorialIntro           id ID #REQUIRED
                                  conref CDATA #IMPLIED
                                  %select-atts;
                                  outputclass CDATA #IMPLIED
                                  %localization-atts;
                                  %arch-atts;
                                  domains CDATA "&included-domains;"
>
<!ELEMENT tutorialIntroBody     ((%longDesc;)?, (%learningObjectives;), (%timeRequired;), (%skillLevel;)?, (%audienceDesc;)?, (%systemRequirements;)?, (%prerequisites;)?, (%expectedResults;)?, (%conventionsUsed;)? )>
<!ATTLIST tutorialIntroBody       %id-atts;
                                  %localization-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT longDesc              (%section.notitle.cnt;)* >
<!ATTLIST longDesc                %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT skillLevel            (%section.cnt;)* >
<!ATTLIST skillLevel              %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT audienceDesc          (%section.cnt;)* >
<!ATTLIST audienceDesc            %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT systemRequirements    (%section.cnt;)* >
<!ATTLIST systemRequirements      %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT prerequisites         (%section.cnt;)* >
<!ATTLIST prerequisites           %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT expectedResults       (%section.cnt;)* >
<!ATTLIST expectedResults         %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT conventionsUsed       (%section.cnt;)* >
<!ATTLIST conventionsUsed         %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST tutorialIntro         %global-atts; class  CDATA "- topic/topic tutorial/tutorial tutorialIntro/tutorialIntro ">
<!ATTLIST tutorialIntroBody     %global-atts; class  CDATA "- topic/body tutorial/ tutorialIntro/tutorialIntroBody ">
<!ATTLIST longDesc              %global-atts; class  CDATA "- topic/section tutorial/tutorialDesc tutorialIntro/longDesc ">
<!ATTLIST skillLevel            %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/skillLevel ">
<!ATTLIST audienceDesc          %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/audienceDesc ">
<!ATTLIST systemRequirements    %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/systemRequirements ">
<!ATTLIST prerequisites         %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/prerequisites ">
<!ATTLIST expectedResults       %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/expectedResults ">
<!ATTLIST conventionsUsed       %global-atts; class  CDATA "- topic/section tutorial/section tutorialIntro/conventionsUsed ">
