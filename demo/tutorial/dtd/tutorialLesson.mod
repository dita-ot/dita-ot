<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % tutorialLesson             "tutorialLesson">
<!ENTITY % tutorialLesson-info-types  "%info-types;">

<!ELEMENT tutorialLesson      ((%title;), (%titlealts;)?, (%shortdesc; | %abstract;), (%prolog;)?, (%taskbody;), (%tutorialLesson-info-types;) )>
<!ATTLIST tutorialLesson       id ID #REQUIRED
                               conref CDATA #IMPLIED
                               %select-atts;
                               outputclass CDATA #IMPLIED
                               %localization-atts;
                               %arch-atts;
                               domains CDATA "&included-domains;"
>

<!--specialization attributes-->

<!ATTLIST tutorialLesson       %global-atts; class  CDATA "- topic/topic task/task tutorialLesson/tutorialLesson ">
