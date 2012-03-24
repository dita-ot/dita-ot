<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % tutorialModule             "tutorialModule">
<!ENTITY % tutorialModuleBody         "tutorialModuleBody">
<!ENTITY % tutorialModule-info-types  "%info-types;">

<!ELEMENT tutorialModule         ((%title;), (%titlealts;)?, (%shortdesc; | %abstract;), (%prolog;)?, (%tutorialModuleBody;), (%tutorialModule-info-types;) ) >
<!ATTLIST tutorialModule          id ID #REQUIRED
                                  conref CDATA #IMPLIED
                                  %select-atts;
                                  outputclass CDATA #IMPLIED
                                  %localization-atts;
                                  %arch-atts;
                                  domains CDATA "&included-domains;"
>

<!ELEMENT tutorialModuleBody     ((%longDesc;)?, (%learningObjectives;), (%timeRequired;), (%prerequisites;)? ) >
<!ATTLIST tutorialModuleBody      %id-atts;
                                  %localization-atts;
                                  outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST tutorialModule          %global-atts; class  CDATA "- topic/topic tutorial/tutorial tutorialIntro/tutorialIntro tutorialModule/tutorialModule ">
<!ATTLIST tutorialModuleBody      %global-atts; class  CDATA "- topic/body tutorial/tutorialBody tutorialIntro/tutorialIntroBody tutorialModule/tutorialModuleBody ">
