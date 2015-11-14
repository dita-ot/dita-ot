<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Task                                         -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                        -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an  -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Task//EN"                     -->
<!--       Delivered as file "task.mod"                                 -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Tasks                     -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Removed old declaration for                -->
<!--                    taskClasses entity                         -->
<!--    2005.11.15 RDA: Corrected LONG NAME for chdeschd           -->
<!--    2006.06.06 RDA: Changed model of choice to listitem.cnt    -->
<!--                    for completeness                           -->
<!--    2006.06.07 RDA: Added <abstract> element                   -->
<!--    2006.06.07 RDA: Make universal attributes universal        -->
<!--                      (DITA 1.1 proposal #12)                  -->
<!--    2006.11.30 RDA: Add -dita-use-conref-target to enumerated  -->
<!--                      attributes                               -->
<!--    2006.11.30 RDA: Remove #FIXED from DITAArchVersion         -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2        -->
<!--    2008.01.30 RDA: Replace @conref defn. with %conref-atts;   -->
<!--    2008.02.06 RDA: Add note and itemgroup to step, substep    -->
<!--    2008.02.06 RDA: Loosen content model of taskbody; add      -->
<!--                    process and stepsection elements           -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!--    2008.05.06 RDA: Added sectiondiv to section specializations -->
<!--    2008.12.02 RDA: Rename process to steps-informal           -->
<!--    2014.04.16 WEK: Redefined as RNG                           -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % task        "task"                                        >
<!ENTITY % taskbody    "taskbody"                                    >
<!ENTITY % prereq      "prereq"                                      >
<!ENTITY % context     "context"                                     >
<!ENTITY % steps-informal
                       "steps-informal"                              >
<!ENTITY % steps       "steps"                                       >
<!ENTITY % steps-unordered
                       "steps-unordered"                             >
<!ENTITY % stepsection "stepsection"                                 >
<!ENTITY % step        "step"                                        >
<!ENTITY % cmd         "cmd"                                         >
<!ENTITY % info        "info"                                        >
<!ENTITY % substeps    "substeps"                                    >
<!ENTITY % substep     "substep"                                     >
<!ENTITY % tutorialinfo
                       "tutorialinfo"                                >
<!ENTITY % stepxmp     "stepxmp"                                     >
<!ENTITY % choices     "choices"                                     >
<!ENTITY % choice      "choice"                                      >
<!ENTITY % choicetable "choicetable"                                 >
<!ENTITY % chhead      "chhead"                                      >
<!ENTITY % choptionhd  "choptionhd"                                  >
<!ENTITY % chdeschd    "chdeschd"                                    >
<!ENTITY % chrow       "chrow"                                       >
<!ENTITY % choption    "choption"                                    >
<!ENTITY % chdesc      "chdesc"                                      >
<!ENTITY % stepresult  "stepresult"                                  >
<!ENTITY % steptroubleshooting
                       "steptroubleshooting"                         >
<!ENTITY % tasktroubleshooting
                       "tasktroubleshooting"                         >
<!ENTITY % result      "result"                                      >
<!ENTITY % postreq     "postreq"                                     >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % task-info-types
              "%info-types;"
>
<!ENTITY % univ-atts-no-importance-task
              "%id-atts;
               %filter-atts;
               base
                          CDATA
                                    #IMPLIED
               %base-attribute-extensions;
               rev
                          CDATA
                                    #IMPLIED
               status
                          (new |
                           changed |
                           deleted |
                           unchanged |
                           -dita-use-conref-target)
                                    #IMPLIED
               %localization-atts;"
>
<!--                    LONG NAME: Task                            -->
<!ENTITY % task.content
                       "((%title;),
                         (%titlealts;)?,
                         (%abstract; |
                          %shortdesc;)?,
                         (%prolog;)?,
                         (%taskbody;)?,
                         (%related-links;)?,
                         (%task-info-types;)*)"
>
<!ENTITY % task.attributes
              "id
                          ID
                                    #REQUIRED
               %conref-atts;
               %select-atts;
               %localization-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  task %task.content;>
<!ATTLIST  task %task.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>


<!--                    LONG NAME: Task Body                       -->
<!ENTITY % taskbody.content
                       "((%prereq; |
                          %context; |
                          %section;)*,
                         (%steps; |
                          %steps-unordered; |
                          %steps-informal;)?,
                         (%result;)?,
                         (%tasktroubleshooting;)?,
                         (%example;)*,
                         (%postreq;)*)"
>
<!ENTITY % taskbody.attributes
              "%id-atts;
               %localization-atts;
               base
                          CDATA
                                    #IMPLIED
               %base-attribute-extensions;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  taskbody %taskbody.content;>
<!ATTLIST  taskbody %taskbody.attributes;>


<!--                    LONG NAME: Prerequisites                   -->
<!ENTITY % prereq.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % prereq.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  prereq %prereq.content;>
<!ATTLIST  prereq %prereq.attributes;>


<!--                    LONG NAME: Context                         -->
<!ENTITY % context.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % context.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  context %context.content;>
<!ATTLIST  context %context.attributes;>


<!--                    LONG NAME: Informal Steps                  -->
<!ENTITY % steps-informal.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % steps-informal.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  steps-informal %steps-informal.content;>
<!ATTLIST  steps-informal %steps-informal.attributes;>


<!--                    LONG NAME: Steps                           -->
<!ENTITY % steps.content
                       "((%data; |
                          %data-about;)*,
                         ((%stepsection;)?,
                          (%step;))+)"
>
<!ENTITY % steps.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  steps %steps.content;>
<!ATTLIST  steps %steps.attributes;>


<!--                    LONG NAME: Unordered steps                 -->
<!ENTITY % steps-unordered.content
                       "((%data; |
                          %data-about;)*,
                         ((%stepsection;)?,
                          (%step;))+)"
>
<!ENTITY % steps-unordered.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  steps-unordered %steps-unordered.content;>
<!ATTLIST  steps-unordered %steps-unordered.attributes;>


<!--                    LONG NAME: Step section                    -->
<!ENTITY % stepsection.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % stepsection.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  stepsection %stepsection.content;>
<!ATTLIST  stepsection %stepsection.attributes;>


<!--                    LONG NAME: Step                            -->
<!ENTITY % step.content
                       "((%note;)*,
                         (%cmd;),
                         (%choices; |
                          %choicetable; |
                          %info; |
                          %itemgroup; |
                          %stepxmp; |
                          %substeps; |
                          %tutorialinfo;)*,
                         (%stepresult;)?,
                         (%steptroubleshooting;)?)"
>
<!ENTITY % step.attributes
              "importance
                          (optional |
                           required |
                           -dita-use-conref-target)
                                    #IMPLIED
               %univ-atts-no-importance-task;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  step %step.content;>
<!ATTLIST  step %step.attributes;>


<!--                    LONG NAME: Command                         -->
<!ENTITY % cmd.content
                       "(%ph.cnt;)*"
>
<!ENTITY % cmd.attributes
              "keyref
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  cmd %cmd.content;>
<!ATTLIST  cmd %cmd.attributes;>


<!--                    LONG NAME: Information                     -->
<!ENTITY % info.content
                       "(%itemgroup.cnt;)*"
>
<!ENTITY % info.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  info %info.content;>
<!ATTLIST  info %info.attributes;>


<!--                    LONG NAME: Sub-steps                       -->
<!ENTITY % substeps.content
                       "((%data; |
                          %data-about;)*,
                         (%substep;)+)"
>
<!ENTITY % substeps.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  substeps %substeps.content;>
<!ATTLIST  substeps %substeps.attributes;>


<!--                    LONG NAME: Sub-step                        -->
<!ENTITY % substep.content
                       "((%note;)*,
                         (%cmd;),
                         (%info; |
                          %itemgroup; |
                          %stepxmp; |
                          %tutorialinfo;)*,
                         (%stepresult;)?)"
>
<!ENTITY % substep.attributes
              "importance
                          (optional |
                           required |
                           -dita-use-conref-target)
                                    #IMPLIED
               %univ-atts-no-importance-task;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  substep %substep.content;>
<!ATTLIST  substep %substep.attributes;>


<!--                    LONG NAME: Tutorial Information            -->
<!ENTITY % tutorialinfo.content
                       "(%itemgroup.cnt;)*"
>
<!ENTITY % tutorialinfo.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  tutorialinfo %tutorialinfo.content;>
<!ATTLIST  tutorialinfo %tutorialinfo.attributes;>


<!--                    LONG NAME: Step Example                    -->
<!ENTITY % stepxmp.content
                       "(%itemgroup.cnt;)*"
>
<!ENTITY % stepxmp.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  stepxmp %stepxmp.content;>
<!ATTLIST  stepxmp %stepxmp.attributes;>


<!--                    LONG NAME: Choices                         -->
<!ENTITY % choices.content
                       "((%data; |
                          %data-about;)*,
                         (%choice;)+)"
>
<!ENTITY % choices.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  choices %choices.content;>
<!ATTLIST  choices %choices.attributes;>


<!--                    LONG NAME: Choice                          -->
<!ENTITY % choice.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % choice.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  choice %choice.content;>
<!ATTLIST  choice %choice.attributes;>


<!--                    LONG NAME: Choice Table                    -->
<!ENTITY % choicetable.content
                       "((%chhead;)?,
                         (%chrow;)+)"
>
<!ENTITY % choicetable.attributes
              "relcolwidth
                          CDATA
                                    #IMPLIED
               keycol
                          NMTOKEN
                                    '1'
               refcols
                          NMTOKENS
                                    #IMPLIED
               spectitle
                          CDATA
                                    #IMPLIED
               %display-atts;
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  choicetable %choicetable.content;>
<!ATTLIST  choicetable %choicetable.attributes;>


<!--                    LONG NAME: Choice Head                     -->
<!ENTITY % chhead.content
                       "((%choptionhd;),
                         (%chdeschd;))"
>
<!ENTITY % chhead.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  chhead %chhead.content;>
<!ATTLIST  chhead %chhead.attributes;>


<!--                    LONG NAME: Choice Option Head              -->
<!ENTITY % choptionhd.content
                       "(%tblcell.cnt;)*"
>
<!ENTITY % choptionhd.attributes
              "specentry
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  choptionhd %choptionhd.content;>
<!ATTLIST  choptionhd %choptionhd.attributes;>


<!--                    LONG NAME: Choice Description Head         -->
<!ENTITY % chdeschd.content
                       "(%tblcell.cnt;)*"
>
<!ENTITY % chdeschd.attributes
              "specentry
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  chdeschd %chdeschd.content;>
<!ATTLIST  chdeschd %chdeschd.attributes;>


<!--                    LONG NAME: Choice Row                      -->
<!ENTITY % chrow.content
                       "((%choption;),
                         (%chdesc;))"
>
<!ENTITY % chrow.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  chrow %chrow.content;>
<!ATTLIST  chrow %chrow.attributes;>


<!--                    LONG NAME: Choice Option                   -->
<!ENTITY % choption.content
                       "(%tblcell.cnt;)*"
>
<!ENTITY % choption.attributes
              "specentry
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  choption %choption.content;>
<!ATTLIST  choption %choption.attributes;>


<!--                    LONG NAME: Choice Description              -->
<!ENTITY % chdesc.content
                       "(%tblcell.cnt;)*"
>
<!ENTITY % chdesc.attributes
              "specentry
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  chdesc %chdesc.content;>
<!ATTLIST  chdesc %chdesc.attributes;>


<!--                    LONG NAME: Step Result                     -->
<!ENTITY % stepresult.content
                       "(%itemgroup.cnt;)*"
>
<!ENTITY % stepresult.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  stepresult %stepresult.content;>
<!ATTLIST  stepresult %stepresult.attributes;>


<!--                    LONG NAME: Step Troubleshooting            -->
<!ENTITY % steptroubleshooting.content
                       "(%itemgroup.cnt;)*"
>
<!ENTITY % steptroubleshooting.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  steptroubleshooting %steptroubleshooting.content;>
<!ATTLIST  steptroubleshooting %steptroubleshooting.attributes;>


<!--                    LONG NAME: Task Troubleshooting            -->
<!ENTITY % tasktroubleshooting.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % tasktroubleshooting.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  tasktroubleshooting %tasktroubleshooting.content;>
<!ATTLIST  tasktroubleshooting %tasktroubleshooting.attributes;>


<!--                    LONG NAME: Result                          -->
<!ENTITY % result.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % result.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  result %result.content;>
<!ATTLIST  result %result.attributes;>


<!--                    LONG NAME: Post Requirements               -->
<!ENTITY % postreq.content
                       "(%section.notitle.cnt;)*"
>
<!ENTITY % postreq.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  postreq %postreq.content;>
<!ATTLIST  postreq %postreq.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  task         %global-atts;  class CDATA "- topic/topic task/task ">
<!ATTLIST  taskbody     %global-atts;  class CDATA "- topic/body task/taskbody ">
<!ATTLIST  steps        %global-atts;  class CDATA "- topic/ol task/steps ">
<!ATTLIST  steps-unordered %global-atts;  class CDATA "- topic/ul task/steps-unordered ">
<!ATTLIST  stepsection  %global-atts;  class CDATA "- topic/li task/stepsection ">
<!ATTLIST  step         %global-atts;  class CDATA "- topic/li task/step ">
<!ATTLIST  cmd          %global-atts;  class CDATA "- topic/ph task/cmd ">
<!ATTLIST  substeps     %global-atts;  class CDATA "- topic/ol task/substeps ">
<!ATTLIST  substep      %global-atts;  class CDATA "- topic/li task/substep ">
<!ATTLIST  tutorialinfo %global-atts;  class CDATA "- topic/itemgroup task/tutorialinfo ">
<!ATTLIST  info         %global-atts;  class CDATA "- topic/itemgroup task/info ">
<!ATTLIST  stepxmp      %global-atts;  class CDATA "- topic/itemgroup task/stepxmp ">
<!ATTLIST  stepresult   %global-atts;  class CDATA "- topic/itemgroup task/stepresult ">
<!ATTLIST  steptroubleshooting %global-atts;  class CDATA "- topic/itemgroup task/steptroubleshooting ">
<!ATTLIST  choices      %global-atts;  class CDATA "- topic/ul task/choices ">
<!ATTLIST  choice       %global-atts;  class CDATA "- topic/li task/choice ">
<!ATTLIST  result       %global-atts;  class CDATA "- topic/section task/result ">
<!ATTLIST  tasktroubleshooting %global-atts;  class CDATA "- topic/section task/tasktroubleshooting ">
<!ATTLIST  prereq       %global-atts;  class CDATA "- topic/section task/prereq ">
<!ATTLIST  postreq      %global-atts;  class CDATA "- topic/section task/postreq ">
<!ATTLIST  context      %global-atts;  class CDATA "- topic/section task/context ">
<!ATTLIST  steps-informal %global-atts;  class CDATA "- topic/section task/steps-informal ">
<!ATTLIST  choicetable  %global-atts;  class CDATA "- topic/simpletable task/choicetable ">
<!ATTLIST  chhead       %global-atts;  class CDATA "- topic/sthead task/chhead ">
<!ATTLIST  chrow        %global-atts;  class CDATA "- topic/strow task/chrow ">
<!ATTLIST  choptionhd   %global-atts;  class CDATA "- topic/stentry task/choptionhd ">
<!ATTLIST  chdeschd     %global-atts;  class CDATA "- topic/stentry task/chdeschd ">
<!ATTLIST  choption     %global-atts;  class CDATA "- topic/stentry task/choption ">
<!ATTLIST  chdesc       %global-atts;  class CDATA "- topic/stentry task/chdesc ">

<!-- ================== End of DITA Task ==================== -->
 