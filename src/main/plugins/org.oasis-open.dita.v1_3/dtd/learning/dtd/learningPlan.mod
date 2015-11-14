<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA learningPlan                                 -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an   -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Learning Plan//EN"            -->
<!--       Delivered as file "learningPlan.mod                          -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Learning Plan                      -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             May 2007                                          -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2009.              -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % learningPlan
                       "learningPlan"                                >
<!ENTITY % learningPlanbody
                       "learningPlanbody"                            >
<!ENTITY % lcProject   "lcProject"                                   >
<!ENTITY % lcClient    "lcClient"                                    >
<!ENTITY % lcPlanTitle "lcPlanTitle"                                 >
<!ENTITY % lcCIN       "lcCIN"                                       >
<!ENTITY % lcModDate   "lcModDate"                                   >
<!ENTITY % lcDelivDate "lcDelivDate"                                 >
<!ENTITY % lcPlanSubject
                       "lcPlanSubject"                               >
<!ENTITY % lcPlanDescrip
                       "lcPlanDescrip"                               >
<!ENTITY % lcPlanPrereqs
                       "lcPlanPrereqs"                               >
<!ENTITY % lcNeedsAnalysis
                       "lcNeedsAnalysis"                             >
<!ENTITY % lcOrganizational
                       "lcOrganizational"                            >
<!ENTITY % lcGeneralDescription
                       "lcGeneralDescription"                        >
<!ENTITY % lcGoals     "lcGoals"                                     >
<!ENTITY % lcNeeds     "lcNeeds"                                     >
<!ENTITY % lcValues    "lcValues"                                    >
<!ENTITY % lcOrgConstraints
                       "lcOrgConstraints"                            >
<!ENTITY % lcPlanAudience
                       "lcPlanAudience"                              >
<!ENTITY % lcEdLevel   "lcEdLevel"                                   >
<!ENTITY % lcAge       "lcAge"                                       >
<!ENTITY % lcBackground
                       "lcBackground"                                >
<!ENTITY % lcSkills    "lcSkills"                                    >
<!ENTITY % lcKnowledge "lcKnowledge"                                 >
<!ENTITY % lcMotivation
                       "lcMotivation"                                >
<!ENTITY % lcSpecChars "lcSpecChars"                                 >
<!ENTITY % lcWorkEnv   "lcWorkEnv"                                   >
<!ENTITY % lcWorkEnvDescription
                       "lcWorkEnvDescription"                        >
<!ENTITY % lcPlanResources
                       "lcPlanResources"                             >
<!ENTITY % lcProcesses "lcProcesses"                                 >
<!ENTITY % lcTask      "lcTask"                                      >
<!ENTITY % lcTaskItem  "lcTaskItem"                                  >
<!ENTITY % lcAttitude  "lcAttitude"                                  >
<!ENTITY % lcGapAnalysis
                       "lcGapAnalysis"                               >
<!ENTITY % lcGapItem   "lcGapItem"                                   >
<!ENTITY % lcPlanObjective
                       "lcPlanObjective"                             >
<!ENTITY % lcJtaItem   "lcJtaItem"                                   >
<!ENTITY % lcGapItemDelta
                       "lcGapItemDelta"                              >
<!ENTITY % lcIntervention
                       "lcIntervention"                              >
<!ENTITY % lcInterventionItem
                       "lcInterventionItem"                          >
<!ENTITY % lcLearnStrat
                       "lcLearnStrat"                                >
<!ENTITY % lcAssessment
                       "lcAssessment"                                >
<!ENTITY % lcDelivery  "lcDelivery"                                  >
<!ENTITY % lcTechnical "lcTechnical"                                 >
<!ENTITY % lcLMS       "lcLMS"                                       >
<!ENTITY % lcNoLMS     "lcNoLMS"                                     >
<!ENTITY % lcHandouts  "lcHandouts"                                  >
<!ENTITY % lcClassroom "lcClassroom"                                 >
<!ENTITY % lcOJT       "lcOJT"                                       >
<!ENTITY % lcConstraints
                       "lcConstraints"                               >
<!ENTITY % lcW3C       "lcW3C"                                       >
<!ENTITY % lcPlayers   "lcPlayers"                                   >
<!ENTITY % lcGraphics  "lcGraphics"                                  >
<!ENTITY % lcViewers   "lcViewers"                                   >
<!ENTITY % lcResolution
                       "lcResolution"                                >
<!ENTITY % lcFileSizeLimitations
                       "lcFileSizeLimitations"                       >
<!ENTITY % lcDownloadTime
                       "lcDownloadTime"                              >
<!ENTITY % lcSecurity  "lcSecurity"                                  >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % learningPlan-info-types
              "no-topic-nesting"
>
<!--                    LONG NAME: Learning plan                   -->
<!ENTITY % learningPlan.content
                       "((%title;),
                         (%titlealts;)?,
                         (%shortdesc; |
                          %abstract;)?,
                         (%prolog;)?,
                         (%learningPlanbody;),
                         (%related-links;)?,
                         (%learningPlan-info-types;)*)"
>
<!ENTITY % learningPlan.attributes
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
<!ELEMENT  learningPlan %learningPlan.content;>
<!ATTLIST  learningPlan %learningPlan.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>


<!--                    LONG NAME: Learning plan body              -->
<!ENTITY % learningPlanbody.content
                       "((%lcProject;)?,
                         (%lcNeedsAnalysis;)?,
                         (%lcGapAnalysis;)?,
                         (%lcIntervention;)?,
                         (%lcTechnical;)?,
                         (%section;)*)"
>
<!ENTITY % learningPlanbody.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  learningPlanbody %learningPlanbody.content;>
<!ATTLIST  learningPlanbody %learningPlanbody.attributes;>


<!--                    LONG NAME: Project plan description        -->
<!ENTITY % lcProject.content
                       "((%title;)?,
                         (%lcClient;)?,
                         (%lcPlanTitle;)?,
                         (%lcCIN;)?,
                         (%lcModDate;)?,
                         (%lcDelivDate;)?,
                         (%lcPlanSubject;)?,
                         (%lcPlanDescrip;)?,
                         (%lcPlanPrereqs;)?)"
>
<!ENTITY % lcProject.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcProject %lcProject.content;>
<!ATTLIST  lcProject %lcProject.attributes;>


<!--                    LONG NAME: Learning Client                 -->
<!ENTITY % lcClient.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcClient.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcClient %lcClient.content;>
<!ATTLIST  lcClient %lcClient.attributes;>


<!--                    LONG NAME: Plan title                      -->
<!ENTITY % lcPlanTitle.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcPlanTitle.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanTitle %lcPlanTitle.content;>
<!ATTLIST  lcPlanTitle %lcPlanTitle.attributes;>


<!--                    LONG NAME: Project alternate identifier    -->
<!ENTITY % lcCIN.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcCIN.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcCIN %lcCIN.content;>
<!ATTLIST  lcCIN %lcCIN.attributes;>


<!--                    LONG NAME: Project modification date       -->
<!ENTITY % lcModDate.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcModDate.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcModDate %lcModDate.content;>
<!ATTLIST  lcModDate %lcModDate.attributes;>


<!--                    LONG NAME: Project delivery date           -->
<!ENTITY % lcDelivDate.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcDelivDate.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcDelivDate %lcDelivDate.content;>
<!ATTLIST  lcDelivDate %lcDelivDate.attributes;>


<!--                    LONG NAME: Plan subject                    -->
<!ENTITY % lcPlanSubject.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcPlanSubject.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanSubject %lcPlanSubject.content;>
<!ATTLIST  lcPlanSubject %lcPlanSubject.attributes;>


<!--                    LONG NAME: Plan description                -->
<!ENTITY % lcPlanDescrip.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcPlanDescrip.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanDescrip %lcPlanDescrip.content;>
<!ATTLIST  lcPlanDescrip %lcPlanDescrip.attributes;>


<!--                    LONG NAME: Plan prerequisites              -->
<!ENTITY % lcPlanPrereqs.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcPlanPrereqs.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanPrereqs %lcPlanPrereqs.content;>
<!ATTLIST  lcPlanPrereqs %lcPlanPrereqs.attributes;>


<!--                    LONG NAME: Needs analysis                  -->
<!ENTITY % lcNeedsAnalysis.content
                       "((%title;)?,
                         (%lcOrganizational;)?,
                         (%lcPlanAudience;)?,
                         (%lcWorkEnv;)?,
                         (%lcTask;)*)"
>
<!ENTITY % lcNeedsAnalysis.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcNeedsAnalysis %lcNeedsAnalysis.content;>
<!ATTLIST  lcNeedsAnalysis %lcNeedsAnalysis.attributes;>


<!--                    LONG NAME: Organization's learning requirements -->
<!ENTITY % lcOrganizational.content
                       "((%title;)?,
                         (%lcGeneralDescription;)?,
                         (%lcGoals;)?,
                         (%lcNeeds;)?,
                         (%lcValues;)?,
                         (%lcOrgConstraints;)?)"
>
<!ENTITY % lcOrganizational.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcOrganizational %lcOrganizational.content;>
<!ATTLIST  lcOrganizational %lcOrganizational.attributes;>


<!--                    LONG NAME: General description             -->
<!ENTITY % lcGeneralDescription.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcGeneralDescription.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGeneralDescription %lcGeneralDescription.content;>
<!ATTLIST  lcGeneralDescription %lcGeneralDescription.attributes;>


<!--                    LONG NAME: Learning goals                  -->
<!ENTITY % lcGoals.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcGoals.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGoals %lcGoals.content;>
<!ATTLIST  lcGoals %lcGoals.attributes;>


<!--                    LONG NAME: Learning needs                  -->
<!ENTITY % lcNeeds.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcNeeds.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcNeeds %lcNeeds.content;>
<!ATTLIST  lcNeeds %lcNeeds.attributes;>


<!--                    LONG NAME: Values                          -->
<!ENTITY % lcValues.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcValues.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcValues %lcValues.content;>
<!ATTLIST  lcValues %lcValues.attributes;>


<!--                    LONG NAME: Organizational constraints      -->
<!ENTITY % lcOrgConstraints.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcOrgConstraints.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcOrgConstraints %lcOrgConstraints.content;>
<!ATTLIST  lcOrgConstraints %lcOrgConstraints.attributes;>


<!--                    LONG NAME: Audience                        -->
<!ENTITY % lcPlanAudience.content
                       "((%title;)?,
                         (%lcGeneralDescription;)?,
                         (%lcEdLevel;)?,
                         (%lcAge;)?,
                         (%lcBackground;)?,
                         (%lcSkills;)?,
                         (%lcKnowledge;)?,
                         (%lcMotivation;)?,
                         (%lcSpecChars;)?)"
>
<!ENTITY % lcPlanAudience.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanAudience %lcPlanAudience.content;>
<!ATTLIST  lcPlanAudience %lcPlanAudience.attributes;>


<!--                    LONG NAME: Education level                 -->
<!ENTITY % lcEdLevel.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcEdLevel.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcEdLevel %lcEdLevel.content;>
<!ATTLIST  lcEdLevel %lcEdLevel.attributes;>


<!--                    LONG NAME: Age range                       -->
<!ENTITY % lcAge.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcAge.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAge %lcAge.content;>
<!ATTLIST  lcAge %lcAge.attributes;>


<!--                    LONG NAME: Professional background         -->
<!ENTITY % lcBackground.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcBackground.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcBackground %lcBackground.content;>
<!ATTLIST  lcBackground %lcBackground.attributes;>


<!--                    LONG NAME: Skills                          -->
<!ENTITY % lcSkills.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcSkills.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSkills %lcSkills.content;>
<!ATTLIST  lcSkills %lcSkills.attributes;>


<!--                    LONG NAME: Knowledge                       -->
<!ENTITY % lcKnowledge.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcKnowledge.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcKnowledge %lcKnowledge.content;>
<!ATTLIST  lcKnowledge %lcKnowledge.attributes;>


<!--                    LONG NAME: Motivation                      -->
<!ENTITY % lcMotivation.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcMotivation.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcMotivation %lcMotivation.content;>
<!ATTLIST  lcMotivation %lcMotivation.attributes;>


<!--                    LONG NAME: Special characteristics         -->
<!ENTITY % lcSpecChars.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcSpecChars.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSpecChars %lcSpecChars.content;>
<!ATTLIST  lcSpecChars %lcSpecChars.attributes;>


<!--                    LONG NAME: Work environment                -->
<!ENTITY % lcWorkEnv.content
                       "((%title;)?,
                         (%lcWorkEnvDescription;)?,
                         (%lcPlanResources;)?,
                         (%lcProcesses;)?)"
>
<!ENTITY % lcWorkEnv.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcWorkEnv %lcWorkEnv.content;>
<!ATTLIST  lcWorkEnv %lcWorkEnv.attributes;>


<!--                    LONG NAME: Work environment description    -->
<!ENTITY % lcWorkEnvDescription.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcWorkEnvDescription.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcWorkEnvDescription %lcWorkEnvDescription.content;>
<!ATTLIST  lcWorkEnvDescription %lcWorkEnvDescription.attributes;>


<!--                    LONG NAME: Resource requirements           -->
<!ENTITY % lcPlanResources.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcPlanResources.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanResources %lcPlanResources.content;>
<!ATTLIST  lcPlanResources %lcPlanResources.attributes;>


<!--                    LONG NAME: Processes                       -->
<!ENTITY % lcProcesses.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcProcesses.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcProcesses %lcProcesses.content;>
<!ATTLIST  lcProcesses %lcProcesses.attributes;>


<!--                    LONG NAME: Task                            -->
<!ENTITY % lcTask.content
                       "((%title;)?,
                         (%lcTaskItem;)*,
                         (%lcKnowledge;)?,
                         (%lcSkills;)?,
                         (%lcAttitude;)?)"
>
<!ENTITY % lcTask.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcTask %lcTask.content;>
<!ATTLIST  lcTask %lcTask.attributes;>


<!--                    LONG NAME: Task item                       -->
<!ENTITY % lcTaskItem.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcTaskItem.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcTaskItem %lcTaskItem.content;>
<!ATTLIST  lcTaskItem %lcTaskItem.attributes;>


<!--                    LONG NAME: Mental attitude                 -->
<!ENTITY % lcAttitude.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcAttitude.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAttitude %lcAttitude.content;>
<!ATTLIST  lcAttitude %lcAttitude.attributes;>


<!--                    LONG NAME: Gap analysis                    -->
<!ENTITY % lcGapAnalysis.content
                       "((%title;)?,
                         (%lcGapItem;)*)"
>
<!ENTITY % lcGapAnalysis.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGapAnalysis %lcGapAnalysis.content;>
<!ATTLIST  lcGapAnalysis %lcGapAnalysis.attributes;>


<!--                    LONG NAME: Gap item                        -->
<!ENTITY % lcGapItem.content
                       "((%title;)?,
                         (%lcPlanObjective;)?,
                         (%lcJtaItem;)?,
                         (%lcGapItemDelta;)?)"
>
<!ENTITY % lcGapItem.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGapItem %lcGapItem.content;>
<!ATTLIST  lcGapItem %lcGapItem.attributes;>


<!--                    LONG NAME: Plan objective                  -->
<!ENTITY % lcPlanObjective.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcPlanObjective.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlanObjective %lcPlanObjective.content;>
<!ATTLIST  lcPlanObjective %lcPlanObjective.attributes;>


<!--                    LONG NAME: Job task analysis item          -->
<!ENTITY % lcJtaItem.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcJtaItem.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcJtaItem %lcJtaItem.content;>
<!ATTLIST  lcJtaItem %lcJtaItem.attributes;>


<!--                    LONG NAME: Gap item delta                  -->
<!ENTITY % lcGapItemDelta.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcGapItemDelta.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGapItemDelta %lcGapItemDelta.content;>
<!ATTLIST  lcGapItemDelta %lcGapItemDelta.attributes;>


<!--                    LONG NAME: Intervention                    -->
<!ENTITY % lcIntervention.content
                       "((%title;)?,
                         (%lcInterventionItem;)*)"
>
<!ENTITY % lcIntervention.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcIntervention %lcIntervention.content;>
<!ATTLIST  lcIntervention %lcIntervention.attributes;>


<!--                    LONG NAME: Intervention item               -->
<!ENTITY % lcInterventionItem.content
                       "((%title;)?,
                         (%lcLearnStrat;)?,
                         (%lcPlanObjective;)?,
                         (%lcAssessment;)?,
                         (%lcDelivery;)?)"
>
<!ENTITY % lcInterventionItem.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcInterventionItem %lcInterventionItem.content;>
<!ATTLIST  lcInterventionItem %lcInterventionItem.attributes;>


<!--                    LONG NAME: Learning strategy               -->
<!ENTITY % lcLearnStrat.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcLearnStrat.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcLearnStrat %lcLearnStrat.content;>
<!ATTLIST  lcLearnStrat %lcLearnStrat.attributes;>


<!--                    LONG NAME: Assessment plans                -->
<!ENTITY % lcAssessment.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcAssessment.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcAssessment %lcAssessment.content;>
<!ATTLIST  lcAssessment %lcAssessment.attributes;>


<!--                    LONG NAME: Delivery method description     -->
<!ENTITY % lcDelivery.content
                       "(%para.cnt;)*"
>
<!ENTITY % lcDelivery.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcDelivery %lcDelivery.content;>
<!ATTLIST  lcDelivery %lcDelivery.attributes;>


<!--                    LONG NAME: Technical requirements          -->
<!ENTITY % lcTechnical.content
                       "((%title;)?,
                         (%lcLMS;)?,
                         (%lcNoLMS;)?,
                         (%lcHandouts;)?,
                         (%lcClassroom;)?,
                         (%lcOJT;)?,
                         (%lcConstraints;)?,
                         (%lcW3C;)?,
                         (%lcPlayers;)?,
                         (%lcGraphics;)?,
                         (%lcViewers;)?,
                         (%lcResolution;)?,
                         (%lcFileSizeLimitations;)?,
                         (%lcDownloadTime;)?,
                         (%lcSecurity;)?)"
>
<!ENTITY % lcTechnical.attributes
              "%univ-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcTechnical %lcTechnical.content;>
<!ATTLIST  lcTechnical %lcTechnical.attributes;>


<!--                    LONG NAME: Learning management system      -->
<!ENTITY % lcLMS.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcLMS.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcLMS %lcLMS.content;>
<!ATTLIST  lcLMS %lcLMS.attributes;>


<!--                    LONG NAME: No Learning management system   -->
<!ENTITY % lcNoLMS.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcNoLMS.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcNoLMS %lcNoLMS.content;>
<!ATTLIST  lcNoLMS %lcNoLMS.attributes;>


<!--                    LONG NAME: Handouts                        -->
<!ENTITY % lcHandouts.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcHandouts.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcHandouts %lcHandouts.content;>
<!ATTLIST  lcHandouts %lcHandouts.attributes;>


<!--                    LONG NAME: Classroom environment           -->
<!ENTITY % lcClassroom.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcClassroom.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcClassroom %lcClassroom.content;>
<!ATTLIST  lcClassroom %lcClassroom.attributes;>


<!--                    LONG NAME: On the job training             -->
<!ENTITY % lcOJT.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcOJT.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcOJT %lcOJT.content;>
<!ATTLIST  lcOJT %lcOJT.attributes;>


<!--                    LONG NAME: Constraints                     -->
<!ENTITY % lcConstraints.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcConstraints.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcConstraints %lcConstraints.content;>
<!ATTLIST  lcConstraints %lcConstraints.attributes;>


<!--                    LONG NAME: Web standards requirements      -->
<!ENTITY % lcW3C.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcW3C.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcW3C %lcW3C.content;>
<!ATTLIST  lcW3C %lcW3C.attributes;>


<!--                    LONG NAME: Players required                -->
<!ENTITY % lcPlayers.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcPlayers.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcPlayers %lcPlayers.content;>
<!ATTLIST  lcPlayers %lcPlayers.attributes;>


<!--                    LONG NAME: Graphics standards              -->
<!ENTITY % lcGraphics.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcGraphics.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcGraphics %lcGraphics.content;>
<!ATTLIST  lcGraphics %lcGraphics.attributes;>


<!--                    LONG NAME: Viewers required                -->
<!ENTITY % lcViewers.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcViewers.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcViewers %lcViewers.content;>
<!ATTLIST  lcViewers %lcViewers.attributes;>


<!--                    LONG NAME: Screen resolution               -->
<!ENTITY % lcResolution.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcResolution.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcResolution %lcResolution.content;>
<!ATTLIST  lcResolution %lcResolution.attributes;>


<!--                    LONG NAME: File size limitations           -->
<!ENTITY % lcFileSizeLimitations.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcFileSizeLimitations.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcFileSizeLimitations %lcFileSizeLimitations.content;>
<!ATTLIST  lcFileSizeLimitations %lcFileSizeLimitations.attributes;>


<!--                    LONG NAME: Maximum download time           -->
<!ENTITY % lcDownloadTime.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcDownloadTime.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcDownloadTime %lcDownloadTime.content;>
<!ATTLIST  lcDownloadTime %lcDownloadTime.attributes;>


<!--                    LONG NAME: Security requirements           -->
<!ENTITY % lcSecurity.content
                       "((%title;)?,
                         (%fig.cnt;)*)"
>
<!ENTITY % lcSecurity.attributes
              "%display-atts;
               spectitle
                          CDATA
                                    #IMPLIED
               %univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  lcSecurity %lcSecurity.content;>
<!ATTLIST  lcSecurity %lcSecurity.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  learningPlan %global-atts;  class CDATA "- topic/topic learningBase/learningBase learningPlan/learningPlan ">
<!ATTLIST  learningPlanbody %global-atts;  class CDATA "- topic/body  learningBase/learningBasebody learningPlan/learningPlanbody ">
<!ATTLIST  lcProject    %global-atts;  class CDATA "- topic/section  learningBase/section learningPlan/lcProject ">
<!ATTLIST  lcNeedsAnalysis %global-atts;  class CDATA "- topic/section  learningBase/section learningPlan/lcNeedsAnalysis ">
<!ATTLIST  lcGapAnalysis %global-atts;  class CDATA "- topic/section  learningBase/section learningPlan/lcGapAnalysis ">
<!ATTLIST  lcIntervention %global-atts;  class CDATA "- topic/section  learningBase/section learningPlan/lcIntervention ">
<!ATTLIST  lcTechnical  %global-atts;  class CDATA "- topic/section  learningBase/section learningPlan/lcTechnical ">
<!ATTLIST  lcClient     %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcClient ">
<!ATTLIST  lcPlanTitle  %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlanTitle ">
<!ATTLIST  lcCIN        %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcCIN ">
<!ATTLIST  lcModDate    %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcModDate ">
<!ATTLIST  lcDelivDate  %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcDelivDate ">
<!ATTLIST  lcPlanSubject %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlanSubject ">
<!ATTLIST  lcPlanDescrip %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlanDescrip ">
<!ATTLIST  lcPlanPrereqs %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlanPrereqs ">
<!ATTLIST  lcOrganizational %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcOrganizational ">
<!ATTLIST  lcGoals      %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcGoals ">
<!ATTLIST  lcNeeds      %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcNeeds ">
<!ATTLIST  lcValues     %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcValues ">
<!ATTLIST  lcOrgConstraints %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcOrgConstraints ">
<!ATTLIST  lcPlanAudience %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlanAudience ">
<!ATTLIST  lcGeneralDescription %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcGeneralDescription ">
<!ATTLIST  lcEdLevel    %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcEdLevel ">
<!ATTLIST  lcAge        %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcAge ">
<!ATTLIST  lcBackground %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcBackground ">
<!ATTLIST  lcSkills     %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcSkills ">
<!ATTLIST  lcKnowledge  %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcKnowledge ">
<!ATTLIST  lcMotivation %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcMotivation ">
<!ATTLIST  lcSpecChars  %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcSpecChars ">
<!ATTLIST  lcWorkEnv    %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcWorkEnv ">
<!ATTLIST  lcWorkEnvDescription %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcWorkEnvDescription ">
<!ATTLIST  lcPlanResources %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcPlanResources ">
<!ATTLIST  lcProcesses  %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcProcesses ">
<!ATTLIST  lcTask       %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcTask ">
<!ATTLIST  lcTaskItem   %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcTaskItem ">
<!ATTLIST  lcAttitude   %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcAttitude ">
<!ATTLIST  lcGapItem    %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcGapItem ">
<!ATTLIST  lcPlanObjective %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcPlanObjective ">
<!ATTLIST  lcJtaItem    %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcJtaItem ">
<!ATTLIST  lcGapItemDelta %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcGapItemDelta ">
<!ATTLIST  lcInterventionItem %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcInterventionItem ">
<!ATTLIST  lcLearnStrat %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcLearnStrat ">
<!ATTLIST  lcAssessment %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcAssessment ">
<!ATTLIST  lcDelivery   %global-atts;  class CDATA "- topic/p  learningBase/p learningPlan/lcDelivery ">
<!ATTLIST  lcLMS        %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcLMS ">
<!ATTLIST  lcNoLMS      %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcNoLMS ">
<!ATTLIST  lcHandouts   %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcHandouts ">
<!ATTLIST  lcClassroom  %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcClassroom ">
<!ATTLIST  lcOJT        %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcOJT ">
<!ATTLIST  lcConstraints %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcConstraints ">
<!ATTLIST  lcW3C        %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcW3C ">
<!ATTLIST  lcPlayers    %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcPlayers ">
<!ATTLIST  lcGraphics   %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcGraphics ">
<!ATTLIST  lcViewers    %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcViewers ">
<!ATTLIST  lcResolution %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcResolution ">
<!ATTLIST  lcFileSizeLimitations %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcFileSizeLimitations ">
<!ATTLIST  lcDownloadTime %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcDownloadTime ">
<!ATTLIST  lcSecurity   %global-atts;  class CDATA "- topic/fig  learningBase/fig learningPlan/lcSecurity ">

<!-- ================== End of DITA learningPlan ==================== -->
 