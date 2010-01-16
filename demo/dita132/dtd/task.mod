<!--
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Task//EN"
 |
 | Release history (vrm):
 |   1.0.0 Initial release on developerWorks, March 2001 (dita00.zip)
 |   1.0.1 fix 1 on developerWorks, October 2001 (dita01.zip)
 |   1.0.2 consolidated redesign December 2001
 |   1.0.3 fix 1, dtd freeze for UCD-1 January 2002
 |   1.1.0 Release 1 March 2002 (dita10.zip)
 |   1.1.1 Release 1.1 December 2002
 |   1.1.2 Release 1.2 June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fix: revised "DTDVersion" to match release version (consistency);
 |                   revised conbody attlist to match other infotype's body attlists (consistency)
 *-->

<!ENTITY DTDVersion 'V1.1.3' >



<!-- ============ Specialization of declared elements ============ -->
<!ENTITY % taskClasses SYSTEM "task_class.ent">
<!--%taskClasses;-->

<!ENTITY % task        "task">
<!ENTITY % taskbody    "taskbody">
<!ENTITY % steps       "steps">
<!ENTITY % steps-unordered "steps-unordered">
<!ENTITY % step        "step">
<!ENTITY % cmd         "cmd">
<!ENTITY % substeps    "substeps">
<!ENTITY % substep     "substep">
<!ENTITY % tutorialinfo "tutorialinfo">
<!ENTITY % info        "info">
<!ENTITY % stepxmp     "stepxmp">
<!ENTITY % stepresult  "stepresult">
<!ENTITY % choices     "choices">
<!ENTITY % choice      "choice">
<!ENTITY % result      "result">
<!ENTITY % prereq      "prereq">
<!ENTITY % postreq     "postreq">
<!ENTITY % context     "context">
<!ENTITY % choicetable "choicetable">
<!ENTITY % chhead      "chhead">
<!ENTITY % chrow       "chrow">
<!ENTITY % choptionhd  "choptionhd">
<!ENTITY % chdeschd    "chdeschd">
<!ENTITY % choption    "choption">
<!ENTITY % chdesc      "chdesc">


<!-- provide an alternative set of univ-atts that allows importance to be redefined locally-->
<!ENTITY % univ-atts-no-importance-task
                         '%id-atts;
                          platform CDATA #IMPLIED
                          product CDATA #IMPLIED
                          audience CDATA #IMPLIED
                          otherprops CDATA #IMPLIED
                          rev CDATA #IMPLIED
                          status (new|changed|deleted|unchanged) #IMPLIED
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED'
>

<!ENTITY % task-info-types "%info-types;">
<!ENTITY included-domains "">

<!ELEMENT task           (%title;, (%titlealts;)?, (%shortdesc;)?, (%prolog;)?, %taskbody;, (%related-links;)?, (%task-info-types;)* )>
<!ATTLIST task            id ID #REQUIRED
                          conref CDATA #IMPLIED
                          %select-atts;
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          DTDVersion CDATA #FIXED "&DTDVersion;"
                          domains CDATA "&included-domains;"
>

<!ELEMENT taskbody       ((%prereq;)?, (%context;)?, (%steps;|%steps-unordered;)?, (%result;)?, (%example;)?, (%postreq;)?) >
<!ATTLIST taskbody        %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT prereq         (%section.notitle.cnt;)* >
<!ATTLIST prereq          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT context        (%section.notitle.cnt;)* >
<!ATTLIST context         %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!ELEMENT steps          ((%step;)+)>
<!ATTLIST steps           %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT steps-unordered ((%step;)+)>
<!ATTLIST steps-unordered %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT step           (%cmd;, (%info;|%substeps;|%tutorialinfo;|%stepxmp;|%choicetable;|%choices;)*, (%stepresult;)?) >
<!ATTLIST step            importance (optional | required) #IMPLIED
                          %univ-atts-no-importance-task;
                          outputclass CDATA #IMPLIED
>
<!--ATTLIST step          importance ( optional | required ) #IMPLIED-->

<!ELEMENT cmd            (%ph.cnt;)* >
<!ATTLIST cmd             keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT info           (%itemgroup.cnt;)* >
<!ATTLIST info            %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT substeps       (%substep;)+ >
<!ATTLIST substeps        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT substep        (%cmd;,  (%info;|%tutorialinfo;|%stepxmp;)*, (%stepresult;)?)>
<!ATTLIST substep         importance (optional | required) #IMPLIED
                          %univ-atts-no-importance-task;
                          outputclass CDATA #IMPLIED
>
<!--ATTLIST substep       importance ( optional | required ) #IMPLIED-->

<!ELEMENT tutorialinfo   (%itemgroup.cnt;)* >
<!ATTLIST tutorialinfo    %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT stepxmp        (%itemgroup.cnt;)* >
<!ATTLIST stepxmp         %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT choices        ((%choice;)+) >
<!ATTLIST choices         %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT choice         (#PCDATA|%basic.ph;)*>


<!ELEMENT choicetable    ((%chhead;)?, (%chrow;)+) >
<!ATTLIST choicetable     relcolwidth CDATA #IMPLIED
                          keycol NMTOKEN "1"
                          refcols NMTOKENS #IMPLIED
                          %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT chhead         ((%choptionhd;), (%chdeschd;))>
<!ATTLIST chhead          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT choptionhd     (%tblcell.cnt;)*>
<!ATTLIST choptionhd      %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT chdeschd       (%tblcell.cnt;)*>
<!ATTLIST chdeschd        %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT chrow          ((%choption;), (%chdesc;))>
<!ATTLIST chrow           %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT choption       (%tblcell.cnt;)*>
<!ATTLIST choption        %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT chdesc         (%tblcell.cnt;)*>
<!ATTLIST chdesc          %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!ELEMENT stepresult     (%itemgroup.cnt;)* >
<!ATTLIST stepresult      %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT result         (%section.notitle.cnt;)* >
<!ATTLIST result          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT postreq        (%section.notitle.cnt;)* >
<!ATTLIST postreq         %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!--specialization attributes-->

<!ATTLIST task         %global-atts; class  CDATA "- topic/topic task/task ">
<!ATTLIST taskbody     %global-atts; class  CDATA "- topic/body task/taskbody ">
<!ATTLIST steps        %global-atts; class  CDATA "- topic/ol task/steps ">
<!ATTLIST steps-unordered %global-atts; class  CDATA "- topic/ul task/steps-unordered ">
<!ATTLIST step         %global-atts; class  CDATA "- topic/li task/step ">
<!ATTLIST cmd          %global-atts; class  CDATA "- topic/ph task/cmd ">
<!ATTLIST substeps     %global-atts; class  CDATA "- topic/ol task/substeps ">
<!ATTLIST substep      %global-atts; class  CDATA "- topic/li task/substep ">
<!ATTLIST tutorialinfo %global-atts; class  CDATA "- topic/itemgroup task/tutorialinfo ">
<!ATTLIST info         %global-atts; class  CDATA "- topic/itemgroup task/info ">
<!ATTLIST stepxmp      %global-atts; class  CDATA "- topic/itemgroup task/stepxmp ">
<!ATTLIST stepresult   %global-atts; class  CDATA "- topic/itemgroup task/stepresult ">

<!ATTLIST choices      %global-atts; class  CDATA "- topic/ul task/choices ">
<!ATTLIST choice       %global-atts; class  CDATA "- topic/li task/choice ">

<!ATTLIST result       %global-atts; class  CDATA "- topic/section task/result ">
<!ATTLIST prereq       %global-atts; class  CDATA "- topic/section task/prereq ">
<!ATTLIST postreq      %global-atts; class  CDATA "- topic/section task/postreq ">
<!ATTLIST context      %global-atts; class  CDATA "- topic/section task/context ">

<!ATTLIST choicetable  %global-atts; class  CDATA "- topic/simpletable task/choicetable ">
<!ATTLIST chhead       %global-atts; class  CDATA "- topic/sthead task/chhead ">
<!ATTLIST chrow        %global-atts; class  CDATA "- topic/strow task/chrow ">
<!ATTLIST choptionhd   %global-atts; class  CDATA "- topic/stentry task/choptionhd ">
<!ATTLIST chdeschd     %global-atts; class  CDATA "- topic/stentry task/chdeschd ">
<!ATTLIST choption     %global-atts; class  CDATA "- topic/stentry task/choption ">
<!ATTLIST chdesc       %global-atts; class  CDATA "- topic/stentry task/chdesc ">



