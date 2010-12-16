<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % tutorial                 "tutorial">
<!ENTITY % tutorialBody             "tutorialBody">
<!ENTITY % tutorialDesc             "tutorialDesc">
<!ENTITY % learningObjectives       "learningObjectives">
<!ENTITY % timeRequired             "timeRequired">
<!ENTITY % tutorial-info-types      "%info-types;">

<!ELEMENT tutorial              ((%title;), (%titlealts;)?, (%shortdesc; | %abstract;), (%prolog;)?, (%tutorialBody;), (%related-links;)?, (%tutorial-info-types;) )>
<!ATTLIST tutorial                id ID #REQUIRED
                                  conref CDATA #IMPLIED
                                  %select-atts;
                                  outputclass CDATA #IMPLIED
                                  %localization-atts;
                                  %arch-atts;
                                  domains CDATA "&included-domains;"
>

<!ELEMENT tutorialBody          ((%tutorialDesc;)?, (%learningObjectives;), (%timeRequired;)?, (%section;)* )>
<!ATTLIST tutorialBody            %id-atts;
                                  %localization-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT tutorialDesc          (%section.notitle.cnt;)* >
<!ATTLIST tutorialDesc            %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT learningObjectives    (%section.cnt;)* >
<!ATTLIST learningObjectives      %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT timeRequired          (%section.cnt;)* >
<!ATTLIST timeRequired            %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST tutorial              %global-atts; class  CDATA "- topic/topic tutorial/tutorial ">
<!ATTLIST tutorialBody          %global-atts; class  CDATA "- topic/body tutorial/tutorialBody ">
<!ATTLIST tutorialDesc          %global-atts; class  CDATA "- topic/section tutorial/tutorialDesc ">
<!ATTLIST learningObjectives    %global-atts; class  CDATA "- topic/section tutorial/learningObjectives ">
<!ATTLIST timeRequired          %global-atts; class  CDATA "- topic/section tutorial/timeRequired ">
