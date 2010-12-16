<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % tutorialSummary              "tutorialSummary">
<!ENTITY % tutorialSummaryBody          "tutorialSummaryBody">
<!ENTITY % summary                      "summary">
<!ENTITY % lessonsLearned               "lessonsLearned">
<!ENTITY % assessment                   "assessment">
<!ENTITY % resources                    "resources">
<!ENTITY % tutorialSummary-info-types   "%info-types;">

<!ELEMENT tutorialSummary       ((%title;), (%titlealts;)?, (%shortdesc; | %abstract;), (%prolog;)?, (%tutorialSummaryBody;), (%related-links;)?, (%tutorialSummary-info-types;) )>
<!ATTLIST tutorialSummary         id ID #REQUIRED
                                  conref CDATA #IMPLIED
                                  %select-atts;
                                  outputclass CDATA #IMPLIED
                                  %localization-atts;
                                  %arch-atts;
                                  domains CDATA "&included-domains;"
>

<!ELEMENT tutorialSummaryBody   ((%summary;)?, (%lessonsLearned;)?, (%assessment;)?, (%resources;)? )>
<!ATTLIST tutorialSummaryBody     %id-atts;
                                  %localization-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT summary               (%section.notitle.cnt;)* >
<!ATTLIST summary                 %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT lessonsLearned        (%section.cnt;)* >
<!ATTLIST lessonsLearned          %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT assessment            (%section.cnt;)* >
<!ATTLIST assessment              %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT resources             (%section.cnt;)* >
<!ATTLIST resources               %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST tutorialSummary       %global-atts; class  CDATA "- topic/topic tutorialSummary/tutorialSummary ">
<!ATTLIST tutorialSummaryBody   %global-atts; class  CDATA "- topic/body tutorialSummary/tutorialSummaryBody ">
<!ATTLIST summary               %global-atts; class  CDATA "- topic/section tutorialSummary/summary ">
<!ATTLIST lessonsLearned        %global-atts; class  CDATA "- topic/section tutorialSummary/lessonsLearned ">
<!ATTLIST assessment            %global-atts; class  CDATA "- topic/section tutorialSummary/assessment ">
<!ATTLIST resources             %global-atts; class  CDATA "- topic/section tutorialSummary/resources ">
