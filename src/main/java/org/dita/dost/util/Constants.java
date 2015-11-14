/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.*;

/**
 * This class contains all the constants used in DITA-OT.
 * 
 * @version 1.0 2005-06-22
 * 
 * @author Wu, Zhi Qiang
 */
public final class Constants {

    /**.gif extension.*/
    public static final String FILE_EXTENSION_GIF = ".gif";
    /**.dita extension.*/
    public static final String FILE_EXTENSION_DITA = ".dita";
    /**.xml extension.*/
    public static final String FILE_EXTENSION_XML = ".xml";
    /**.html extension.*/
    public static final String FILE_EXTENSION_HTML = ".html";
    /**.htm extension.*/
    public static final String FILE_EXTENSION_HTM = ".htm";
    /**.hhp extension.*/
    public static final String FILE_EXTENSION_HHP = ".hhp";
    /**.hhc extension.*/
    public static final String FILE_EXTENSION_HHC = ".hhc";
    /**.hhk extension.*/
    public static final String FILE_EXTENSION_HHK = ".hhk";
    /**.jpg extension.*/
    public static final String FILE_EXTENSION_JPG = ".jpg";
    /**.swf extension.*/
    public static final String FILE_EXTENSION_SWF = ".swf";
    /**.eps extension.*/
    public static final String FILE_EXTENSION_EPS = ".eps";
    /**.ditamap extension.*/
    public static final String FILE_EXTENSION_DITAMAP = ".ditamap";
    /**.temp extension.*/
    public static final String FILE_EXTENSION_TEMP = ".temp";
    /**.jpeg extension.*/
    public static final String FILE_EXTENSION_JPEG = ".jpeg";
    /**.png extension.*/
    public static final String FILE_EXTENSION_PNG = ".png";
    /**.svg extension.*/
    public static final String FILE_EXTENSION_SVG = ".svg";
    /**.tiff extension.*/
    public static final String FILE_EXTENSION_TIFF = ".tiff";
    /**.tif extension.*/
    public static final String FILE_EXTENSION_TIF = ".tif";
    /**.pdf extension.*/
    public static final String FILE_EXTENSION_PDF = ".pdf";

    public static final String RESOURCES_DIR = "resources";

    public static final DitaClass ABBREV_D_ABBREVIATED_FORM = new DitaClass("+ topic/term abbrev-d/abbreviated-form ");
    public static final DitaClass BOOKMAP_ABBREVLIST = new DitaClass("- map/topicref bookmap/abbrevlist ");
    public static final DitaClass BOOKMAP_AMENDMENTS = new DitaClass("- map/topicref bookmap/amendments ");
    public static final DitaClass BOOKMAP_APPENDICES = new DitaClass("- map/topicref bookmap/appendices ");
    public static final DitaClass BOOKMAP_APPENDIX = new DitaClass("- map/topicref bookmap/appendix ");
    public static final DitaClass BOOKMAP_APPROVED = new DitaClass("- topic/data bookmap/approved ");
    public static final DitaClass BOOKMAP_BACKMATTER = new DitaClass("- map/topicref bookmap/backmatter ");
    public static final DitaClass BOOKMAP_BIBLIOLIST = new DitaClass("- map/topicref bookmap/bibliolist ");
    public static final DitaClass BOOKMAP_BOOKABSTRACT = new DitaClass("- map/topicref bookmap/bookabstract ");
    public static final DitaClass BOOKMAP_BOOKCHANGEHISTORY = new DitaClass("- topic/data bookmap/bookchangehistory ");
    public static final DitaClass BOOKMAP_BOOKEVENT = new DitaClass("- topic/data bookmap/bookevent ");
    public static final DitaClass BOOKMAP_BOOKEVENTTYPE = new DitaClass("- topic/data bookmap/bookeventtype ");
    public static final DitaClass BOOKMAP_BOOKID = new DitaClass("- topic/data bookmap/bookid ");
    public static final DitaClass BOOKMAP_BOOKLIBRARY = new DitaClass("- topic/ph bookmap/booklibrary ");
    public static final DitaClass BOOKMAP_BOOKLIST = new DitaClass("- map/topicref bookmap/booklist ");
    public static final DitaClass BOOKMAP_BOOKLISTS = new DitaClass("- map/topicref bookmap/booklists ");
    public static final DitaClass BOOKMAP_BOOKMAP = new DitaClass("- map/map bookmap/bookmap ");
    public static final DitaClass BOOKMAP_BOOKMETA = new DitaClass("- map/topicmeta bookmap/bookmeta ");
    public static final DitaClass BOOKMAP_BOOKNUMBER = new DitaClass("- topic/data bookmap/booknumber ");
    public static final DitaClass BOOKMAP_BOOKOWNER = new DitaClass("- topic/data bookmap/bookowner ");
    public static final DitaClass BOOKMAP_BOOKPARTNO = new DitaClass("- topic/data bookmap/bookpartno ");
    public static final DitaClass BOOKMAP_BOOKRESTRICTION = new DitaClass("- topic/data bookmap/bookrestriction ");
    public static final DitaClass BOOKMAP_BOOKRIGHTS = new DitaClass("- topic/data bookmap/bookrights ");
    public static final DitaClass BOOKMAP_BOOKTITLE = new DitaClass("- topic/title bookmap/booktitle ");
    public static final DitaClass BOOKMAP_BOOKTITLEALT = new DitaClass("- topic/ph bookmap/booktitlealt ");
    public static final DitaClass BOOKMAP_CHAPTER = new DitaClass("- map/topicref bookmap/chapter ");
    public static final DitaClass BOOKMAP_COLOPHON = new DitaClass("- map/topicref bookmap/colophon ");
    public static final DitaClass BOOKMAP_COMPLETED = new DitaClass("- topic/ph bookmap/completed ");
    public static final DitaClass BOOKMAP_COPYRFIRST = new DitaClass("- topic/data bookmap/copyrfirst ");
    public static final DitaClass BOOKMAP_COPYRLAST = new DitaClass("- topic/data bookmap/copyrlast ");
    public static final DitaClass BOOKMAP_DAY = new DitaClass("- topic/ph bookmap/day ");
    public static final DitaClass BOOKMAP_DEDICATION = new DitaClass("- map/topicref bookmap/dedication ");
    public static final DitaClass BOOKMAP_DRAFTINTRO = new DitaClass("- map/topicref bookmap/draftintro ");
    public static final DitaClass BOOKMAP_EDITED = new DitaClass("- topic/data bookmap/edited ");
    public static final DitaClass BOOKMAP_EDITION = new DitaClass("- topic/data bookmap/edition ");
    public static final DitaClass BOOKMAP_FIGURELIST = new DitaClass("- map/topicref bookmap/figurelist ");
    public static final DitaClass BOOKMAP_FRONTMATTER = new DitaClass("- map/topicref bookmap/frontmatter ");
    public static final DitaClass BOOKMAP_GLOSSARYLIST = new DitaClass("- map/topicref bookmap/glossarylist ");
    public static final DitaClass BOOKMAP_INDEXLIST = new DitaClass("- map/topicref bookmap/indexlist ");
    public static final DitaClass BOOKMAP_ISBN = new DitaClass("- topic/data bookmap/isbn ");
    public static final DitaClass BOOKMAP_MAINBOOKTITLE = new DitaClass("- topic/ph bookmap/mainbooktitle ");
    public static final DitaClass BOOKMAP_MAINTAINER = new DitaClass("- topic/data bookmap/maintainer ");
    public static final DitaClass BOOKMAP_MONTH = new DitaClass("- topic/ph bookmap/month ");
    public static final DitaClass BOOKMAP_NOTICES = new DitaClass("- map/topicref bookmap/notices ");
    public static final DitaClass BOOKMAP_ORGANIZATION = new DitaClass("- topic/data bookmap/organization ");
    public static final DitaClass BOOKMAP_PART = new DitaClass("- map/topicref bookmap/part ");
    public static final DitaClass BOOKMAP_PERSON = new DitaClass("- topic/data bookmap/person ");
    public static final DitaClass BOOKMAP_PREFACE = new DitaClass("- map/topicref bookmap/preface ");
    public static final DitaClass BOOKMAP_PRINTLOCATION = new DitaClass("- topic/data bookmap/printlocation ");
    public static final DitaClass BOOKMAP_PUBLISHED = new DitaClass("- topic/data bookmap/published ");
    public static final DitaClass BOOKMAP_PUBLISHERINFORMATION = new DitaClass("- topic/publisher bookmap/publisherinformation ");
    public static final DitaClass BOOKMAP_PUBLISHTYPE = new DitaClass("- topic/data bookmap/publishtype ");
    public static final DitaClass BOOKMAP_REVIEWED = new DitaClass("- topic/data bookmap/reviewed ");
    public static final DitaClass BOOKMAP_REVISIONID = new DitaClass("- topic/ph bookmap/revisionid ");
    public static final DitaClass BOOKMAP_STARTED = new DitaClass("- topic/ph bookmap/started ");
    public static final DitaClass BOOKMAP_SUMMARY = new DitaClass("- topic/ph bookmap/summary ");
    public static final DitaClass BOOKMAP_TABLELIST = new DitaClass("- map/topicref bookmap/tablelist ");
    public static final DitaClass BOOKMAP_TESTED = new DitaClass("- topic/data bookmap/tested ");
    public static final DitaClass BOOKMAP_TOC = new DitaClass("- map/topicref bookmap/toc ");
    public static final DitaClass BOOKMAP_TRADEMARKLIST = new DitaClass("- map/topicref bookmap/trademarklist ");
    public static final DitaClass BOOKMAP_VOLUME = new DitaClass("- topic/data bookmap/volume ");
    public static final DitaClass BOOKMAP_YEAR = new DitaClass("- topic/ph bookmap/year ");
    public static final DitaClass CLASSIFY_D_SUBJECTCELL = new DitaClass("+ map/relcell classify-d/subjectCell ");
    public static final DitaClass CLASSIFY_D_SUBJECTREF = new DitaClass("+ map/topicref classify-d/subjectref ");
    public static final DitaClass CLASSIFY_D_TOPICAPPLY = new DitaClass("+ map/topicref classify-d/topicapply ");
    public static final DitaClass CLASSIFY_D_TOPICCELL = new DitaClass("+ map/relcell classify-d/topicCell ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECT = new DitaClass("+ map/topicref classify-d/topicsubject ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTHEADER = new DitaClass("+ map/relrow classify-d/topicSubjectHeader ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTROW = new DitaClass("+ map/relrow classify-d/topicSubjectRow ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTTABLE = new DitaClass("+ map/reltable classify-d/topicSubjectTable ");
    public static final DitaClass CONCEPT_CONBODY = new DitaClass("- topic/body concept/conbody ");
    public static final DitaClass CONCEPT_CONBODYDIV = new DitaClass("- topic/bodydiv concept/conbodydiv ");
    public static final DitaClass CONCEPT_CONCEPT = new DitaClass("- topic/topic concept/concept ");
    public static final DitaClass DELAY_D_ANCHORID = new DitaClass("+ topic/keyword delay-d/anchorid ");
    public static final DitaClass DELAY_D_ANCHORKEY = new DitaClass("+ topic/keyword delay-d/anchorkey ");
    public static final DitaClass DELAY_D_EXPORTANCHORS = new DitaClass("+ topic/keywords delay-d/exportanchors ");
    public static final DitaClass GLOSSENTRY_GLOSSABBREVIATION = new DitaClass("- topic/title concept/title glossentry/glossAbbreviation ");
    public static final DitaClass GLOSSENTRY_GLOSSACRONYM = new DitaClass("- topic/title concept/title glossentry/glossAcronym ");
    public static final DitaClass GLOSSENTRY_GLOSSALT = new DitaClass("- topic/section concept/section glossentry/glossAlt ");
    public static final DitaClass GLOSSENTRY_GLOSSALTERNATEFOR = new DitaClass("- topic/xref concept/xref glossentry/glossAlternateFor ");
    public static final DitaClass GLOSSENTRY_GLOSSBODY = new DitaClass("- topic/body concept/conbody glossentry/glossBody ");
    public static final DitaClass GLOSSENTRY_GLOSSDEF = new DitaClass("- topic/abstract concept/abstract glossentry/glossdef ");
    public static final DitaClass GLOSSENTRY_GLOSSENTRY = new DitaClass("- topic/topic concept/concept glossentry/glossentry ");
    public static final DitaClass GLOSSENTRY_GLOSSPARTOFSPEECH = new DitaClass("- topic/data concept/data glossentry/glossPartOfSpeech ");
    public static final DitaClass GLOSSENTRY_GLOSSPROPERTY = new DitaClass("- topic/data concept/data glossentry/glossProperty ");
    public static final DitaClass GLOSSENTRY_GLOSSSCOPENOTE = new DitaClass("- topic/note concept/note glossentry/glossScopeNote ");
    public static final DitaClass GLOSSENTRY_GLOSSSHORTFORM = new DitaClass("- topic/title concept/title glossentry/glossShortForm ");
    public static final DitaClass GLOSSENTRY_GLOSSSTATUS = new DitaClass("- topic/data concept/data glossentry/glossStatus ");
    public static final DitaClass GLOSSENTRY_GLOSSSURFACEFORM = new DitaClass("- topic/p concept/p glossentry/glossSurfaceForm ");
    public static final DitaClass GLOSSENTRY_GLOSSSYMBOL = new DitaClass("- topic/image concept/image glossentry/glossSymbol ");
    public static final DitaClass GLOSSENTRY_GLOSSSYNONYM = new DitaClass("- topic/title concept/title glossentry/glossSynonym ");
    public static final DitaClass GLOSSENTRY_GLOSSTERM = new DitaClass("- topic/title concept/title glossentry/glossterm ");
    public static final DitaClass GLOSSENTRY_GLOSSUSAGE = new DitaClass("- topic/note concept/note glossentry/glossUsage ");
    public static final DitaClass GLOSSGROUP_GLOSSGROUP = new DitaClass("- topic/topic concept/concept glossgroup/glossgroup ");
    public static final DitaClass GLOSSREF_D_GLOSSREF = new DitaClass("+ map/topicref glossref-d/glossref ");
    public static final DitaClass HAZARD_D_CONSEQUENCE = new DitaClass("+ topic/li hazard-d/consequence ");
    public static final DitaClass HAZARD_D_HAZARDSTATEMENT = new DitaClass("+ topic/note hazard-d/hazardstatement ");
    public static final DitaClass HAZARD_D_HAZARDSYMBOL = new DitaClass("+ topic/image hazard-d/hazardsymbol ");
    public static final DitaClass HAZARD_D_HOWTOAVOID = new DitaClass("+ topic/li hazard-d/howtoavoid ");
    public static final DitaClass HAZARD_D_MESSAGEPANEL = new DitaClass("+ topic/ul hazard-d/messagepanel ");
    public static final DitaClass HAZARD_D_TYPEOFHAZARD = new DitaClass("+ topic/li hazard-d/typeofhazard ");
    public static final DitaClass HI_D_B = new DitaClass("+ topic/ph hi-d/b ");
    public static final DitaClass HI_D_I = new DitaClass("+ topic/ph hi-d/i ");
    public static final DitaClass HI_D_SUB = new DitaClass("+ topic/ph hi-d/sub ");
    public static final DitaClass HI_D_SUP = new DitaClass("+ topic/ph hi-d/sup ");
    public static final DitaClass HI_D_TT = new DitaClass("+ topic/ph hi-d/tt ");
    public static final DitaClass HI_D_U = new DitaClass("+ topic/ph hi-d/u ");
    public static final DitaClass INDEXING_D_INDEX_SEE = new DitaClass("+ topic/index-base indexing-d/index-see ");
    public static final DitaClass INDEXING_D_INDEX_SEE_ALSO = new DitaClass("+ topic/index-base indexing-d/index-see-also ");
    public static final DitaClass INDEXING_D_INDEX_SORT_AS = new DitaClass("+ topic/index-base indexing-d/index-sort-as ");
    public static final DitaClass LEARNINGASSESSMENT_LEARNINGASSESSMENT = new DitaClass("- topic/topic learningBase/learningBase learningAssessment/learningAssessment ");
    public static final DitaClass LEARNINGASSESSMENT_LEARNINGASSESSMENTBODY = new DitaClass("- topic/body learningBase/learningBasebody learningAssessment/learningAssessmentbody ");
    public static final DitaClass LEARNINGBASE_LCAUDIENCE = new DitaClass("- topic/section learningBase/lcAudience ");
    public static final DitaClass LEARNINGBASE_LCCHALLENGE = new DitaClass("- topic/section learningBase/lcChallenge ");
    public static final DitaClass LEARNINGBASE_LCDURATION = new DitaClass("- topic/section learningBase/lcDuration ");
    public static final DitaClass LEARNINGBASE_LCINSTRUCTION = new DitaClass("- topic/section learningBase/lcInstruction ");
    public static final DitaClass LEARNINGBASE_LCINTERACTION = new DitaClass("- topic/section learningBase/lcInteraction ");
    public static final DitaClass LEARNINGBASE_LCINTRO = new DitaClass("- topic/section learningBase/lcIntro ");
    public static final DitaClass LEARNINGBASE_LCNEXTSTEPS = new DitaClass("- topic/section learningBase/lcNextSteps ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVE = new DitaClass("- topic/li learningBase/lcObjective ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVES = new DitaClass("- topic/section learningBase/lcObjectives ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVESGROUP = new DitaClass("- topic/ul learningBase/lcObjectivesGroup ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVESSTEM = new DitaClass("- topic/ph learningBase/lcObjectivesStem ");
    public static final DitaClass LEARNINGBASE_LCPREREQS = new DitaClass("- topic/section learningBase/lcPrereqs ");
    public static final DitaClass LEARNINGBASE_LCRESOURCES = new DitaClass("- topic/section learningBase/lcResources ");
    public static final DitaClass LEARNINGBASE_LCREVIEW = new DitaClass("- topic/section learningBase/lcReview ");
    public static final DitaClass LEARNINGBASE_LCSUMMARY = new DitaClass("- topic/section learningBase/lcSummary ");
    public static final DitaClass LEARNINGBASE_LCTIME = new DitaClass("- topic/data learningBase/lcTime ");
    public static final DitaClass LEARNINGBASE_LEARNINGBASE = new DitaClass("- topic/topic learningBase/learningBase ");
    public static final DitaClass LEARNINGBASE_LEARNINGBASEBODY = new DitaClass("- topic/body learningBase/learningBasebody ");
    public static final DitaClass LEARNINGCONTENT_LEARNINGCONTENT = new DitaClass("- topic/topic learningBase/learningBase learningContent/learningContent ");
    public static final DitaClass LEARNINGCONTENT_LEARNINGCONTENTBODY = new DitaClass("- topic/body learningBase/learningBasebody learningContent/learningContentbody ");
    public static final DitaClass LEARNINGINTERACTIONBASE_D_LCINTERACTIONBASE = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase ");
    public static final DitaClass LEARNINGINTERACTIONBASE_D_LCQUESTIONBASE = new DitaClass("+ topic/p learningInteractionBase-d/lcQuestionBase ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGCONTENTCOMPONENTREF = new DitaClass("+ map/topicref learningmap-d/learningContentComponentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGCONTENTREF = new DitaClass("+ map/topicref learningmap-d/learningContentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGGROUP = new DitaClass("+ map/topicref learningmap-d/learningGroup ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGOBJECT = new DitaClass("+ map/topicref learningmap-d/learningObject ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGOVERVIEWREF = new DitaClass("+ map/topicref learningmap-d/learningOverviewRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPLANREF = new DitaClass("+ map/topicref learningmap-d/learningPlanRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPOSTASSESSMENTREF = new DitaClass("+ map/topicref learningmap-d/learningPostAssessmentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPREASSESSMENTREF = new DitaClass("+ map/topicref learningmap-d/learningPreAssessmentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGSUMMARYREF = new DitaClass("+ map/topicref learningmap-d/learningSummaryRef ");
    public static final DitaClass LEARNINGMETA_D_LCLOM = new DitaClass("+ topic/metadata learningmeta-d/lcLom ");
    public static final DitaClass LEARNINGMETA_D_LOMAGGREGATIONLEVEL = new DitaClass("+ topic/data learningmeta-d/lomAggregationLevel ");
    public static final DitaClass LEARNINGMETA_D_LOMCONTEXT = new DitaClass("+ topic/data learningmeta-d/lomContext ");
    public static final DitaClass LEARNINGMETA_D_LOMCOVERAGE = new DitaClass("+ topic/data learningmeta-d/lomCoverage ");
    public static final DitaClass LEARNINGMETA_D_LOMDIFFICULTY = new DitaClass("+ topic/data learningmeta-d/lomDifficulty ");
    public static final DitaClass LEARNINGMETA_D_LOMINSTALLATIONREMARKS = new DitaClass("+ topic/data learningmeta-d/lomInstallationRemarks ");
    public static final DitaClass LEARNINGMETA_D_LOMINTENDEDUSERROLE = new DitaClass("+ topic/data learningmeta-d/lomIntendedUserRole ");
    public static final DitaClass LEARNINGMETA_D_LOMINTERACTIVITYLEVEL = new DitaClass("+ topic/data learningmeta-d/lomInteractivityLevel ");
    public static final DitaClass LEARNINGMETA_D_LOMINTERACTIVITYTYPE = new DitaClass("+ topic/data learningmeta-d/lomInteractivityType ");
    public static final DitaClass LEARNINGMETA_D_LOMLEARNINGRESOURCETYPE = new DitaClass("+ topic/data learningmeta-d/lomLearningResourceType ");
    public static final DitaClass LEARNINGMETA_D_LOMOTHERPLATFORMREQUIREMENTS = new DitaClass("+ topic/data learningmeta-d/lomOtherPlatformRequirements ");
    public static final DitaClass LEARNINGMETA_D_LOMSEMANTICDENSITY = new DitaClass("+ topic/data learningmeta-d/lomSemanticDensity ");
    public static final DitaClass LEARNINGMETA_D_LOMSTRUCTURE = new DitaClass("+ topic/data learningmeta-d/lomStructure ");
    public static final DitaClass LEARNINGMETA_D_LOMTECHREQUIREMENT = new DitaClass("+ topic/data learningmeta-d/lomTechRequirement ");
    public static final DitaClass LEARNINGMETA_D_LOMTYPICALAGERANGE = new DitaClass("+ topic/data learningmeta-d/lomTypicalAgeRange ");
    public static final DitaClass LEARNINGMETA_D_LOMTYPICALLEARNINGTIME = new DitaClass("+ topic/data learningmeta-d/lomTypicalLearningTime ");
    public static final DitaClass LEARNINGOVERVIEW_LEARNINGOVERVIEW = new DitaClass("- topic/topic learningBase/learningBase learningOverview/learningOverview ");
    public static final DitaClass LEARNINGOVERVIEW_LEARNINGOVERVIEWBODY = new DitaClass("- topic/body learningBase/learningBasebody learningOverview/learningOverviewbody ");
    public static final DitaClass LEARNINGPLAN_LCAGE = new DitaClass("- topic/p learningBase/p learningPlan/lcAge ");
    public static final DitaClass LEARNINGPLAN_LCASSESSMENT = new DitaClass("- topic/p learningBase/p learningPlan/lcAssessment ");
    public static final DitaClass LEARNINGPLAN_LCATTITUDE = new DitaClass("- topic/p learningBase/p learningPlan/lcAttitude ");
    public static final DitaClass LEARNINGPLAN_LCBACKGROUND = new DitaClass("- topic/p learningBase/p learningPlan/lcBackground ");
    public static final DitaClass LEARNINGPLAN_LCCIN = new DitaClass("- topic/fig learningBase/fig learningPlan/lcCIN ");
    public static final DitaClass LEARNINGPLAN_LCCLASSROOM = new DitaClass("- topic/fig learningBase/fig learningPlan/lcClassroom ");
    public static final DitaClass LEARNINGPLAN_LCCLIENT = new DitaClass("- topic/fig learningBase/fig learningPlan/lcClient ");
    public static final DitaClass LEARNINGPLAN_LCCONSTRAINTS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcConstraints ");
    public static final DitaClass LEARNINGPLAN_LCDELIVDATE = new DitaClass("- topic/fig learningBase/fig learningPlan/lcDelivDate ");
    public static final DitaClass LEARNINGPLAN_LCDELIVERY = new DitaClass("- topic/p learningBase/p learningPlan/lcDelivery ");
    public static final DitaClass LEARNINGPLAN_LCDOWNLOADTIME = new DitaClass("- topic/fig learningBase/fig learningPlan/lcDownloadTime ");
    public static final DitaClass LEARNINGPLAN_LCEDLEVEL = new DitaClass("- topic/p learningBase/p learningPlan/lcEdLevel ");
    public static final DitaClass LEARNINGPLAN_LCFILESIZELIMITATIONS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcFileSizeLimitations ");
    public static final DitaClass LEARNINGPLAN_LCGAPANALYSIS = new DitaClass("- topic/section learningBase/section learningPlan/lcGapAnalysis ");
    public static final DitaClass LEARNINGPLAN_LCGAPITEM = new DitaClass("- topic/fig learningBase/fig learningPlan/lcGapItem ");
    public static final DitaClass LEARNINGPLAN_LCGAPITEMDELTA = new DitaClass("- topic/p learningBase/p learningPlan/lcGapItemDelta ");
    public static final DitaClass LEARNINGPLAN_LCGENERALDESCRIPTION = new DitaClass("- topic/p learningBase/p learningPlan/lcGeneralDescription ");
    public static final DitaClass LEARNINGPLAN_LCGOALS = new DitaClass("- topic/p learningBase/p learningPlan/lcGoals ");
    public static final DitaClass LEARNINGPLAN_LCGRAPHICS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcGraphics ");
    public static final DitaClass LEARNINGPLAN_LCHANDOUTS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcHandouts ");
    public static final DitaClass LEARNINGPLAN_LCINTERVENTION = new DitaClass("- topic/section learningBase/section learningPlan/lcIntervention ");
    public static final DitaClass LEARNINGPLAN_LCINTERVENTIONITEM = new DitaClass("- topic/fig learningBase/fig learningPlan/lcInterventionItem ");
    public static final DitaClass LEARNINGPLAN_LCJTAITEM = new DitaClass("- topic/p learningBase/p learningPlan/lcJtaItem ");
    public static final DitaClass LEARNINGPLAN_LCKNOWLEDGE = new DitaClass("- topic/p learningBase/p learningPlan/lcKnowledge ");
    public static final DitaClass LEARNINGPLAN_LCLEARNSTRAT = new DitaClass("- topic/p learningBase/p learningPlan/lcLearnStrat ");
    public static final DitaClass LEARNINGPLAN_LCLMS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcLMS ");
    public static final DitaClass LEARNINGPLAN_LCMODDATE = new DitaClass("- topic/fig learningBase/fig learningPlan/lcModDate ");
    public static final DitaClass LEARNINGPLAN_LCMOTIVATION = new DitaClass("- topic/p learningBase/p learningPlan/lcMotivation ");
    public static final DitaClass LEARNINGPLAN_LCNEEDS = new DitaClass("- topic/p learningBase/p learningPlan/lcNeeds ");
    public static final DitaClass LEARNINGPLAN_LCNEEDSANALYSIS = new DitaClass("- topic/section learningBase/section learningPlan/lcNeedsAnalysis ");
    public static final DitaClass LEARNINGPLAN_LCNOLMS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcNoLMS ");
    public static final DitaClass LEARNINGPLAN_LCOJT = new DitaClass("- topic/fig learningBase/fig learningPlan/lcOJT ");
    public static final DitaClass LEARNINGPLAN_LCORGANIZATIONAL = new DitaClass("- topic/fig learningBase/fig learningPlan/lcOrganizational ");
    public static final DitaClass LEARNINGPLAN_LCORGCONSTRAINTS = new DitaClass("- topic/p learningBase/p learningPlan/lcOrgConstraints ");
    public static final DitaClass LEARNINGPLAN_LCPLANAUDIENCE = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlanAudience ");
    public static final DitaClass LEARNINGPLAN_LCPLANDESCRIP = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlanDescrip ");
    public static final DitaClass LEARNINGPLAN_LCPLANOBJECTIVE = new DitaClass("- topic/p learningBase/p learningPlan/lcPlanObjective ");
    public static final DitaClass LEARNINGPLAN_LCPLANPREREQS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlanPrereqs ");
    public static final DitaClass LEARNINGPLAN_LCPLANRESOURCES = new DitaClass("- topic/p learningBase/p learningPlan/lcPlanResources ");
    public static final DitaClass LEARNINGPLAN_LCPLANSUBJECT = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlanSubject ");
    public static final DitaClass LEARNINGPLAN_LCPLANTITLE = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlanTitle ");
    public static final DitaClass LEARNINGPLAN_LCPLAYERS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcPlayers ");
    public static final DitaClass LEARNINGPLAN_LCPROCESSES = new DitaClass("- topic/p learningBase/p learningPlan/lcProcesses ");
    public static final DitaClass LEARNINGPLAN_LCPROJECT = new DitaClass("- topic/section learningBase/section learningPlan/lcProject ");
    public static final DitaClass LEARNINGPLAN_LCRESOLUTION = new DitaClass("- topic/fig learningBase/fig learningPlan/lcResolution ");
    public static final DitaClass LEARNINGPLAN_LCSECURITY = new DitaClass("- topic/fig learningBase/fig learningPlan/lcSecurity ");
    public static final DitaClass LEARNINGPLAN_LCSKILLS = new DitaClass("- topic/p learningBase/p learningPlan/lcSkills ");
    public static final DitaClass LEARNINGPLAN_LCSPECCHARS = new DitaClass("- topic/p learningBase/p learningPlan/lcSpecChars ");
    public static final DitaClass LEARNINGPLAN_LCTASK = new DitaClass("- topic/fig learningBase/fig learningPlan/lcTask ");
    public static final DitaClass LEARNINGPLAN_LCTASKITEM = new DitaClass("- topic/p learningBase/p learningPlan/lcTaskItem ");
    public static final DitaClass LEARNINGPLAN_LCTECHNICAL = new DitaClass("- topic/section learningBase/section learningPlan/lcTechnical ");
    public static final DitaClass LEARNINGPLAN_LCVALUES = new DitaClass("- topic/p learningBase/p learningPlan/lcValues ");
    public static final DitaClass LEARNINGPLAN_LCVIEWERS = new DitaClass("- topic/fig learningBase/fig learningPlan/lcViewers ");
    public static final DitaClass LEARNINGPLAN_LCW3C = new DitaClass("- topic/fig learningBase/fig learningPlan/lcW3C ");
    public static final DitaClass LEARNINGPLAN_LCWORKENV = new DitaClass("- topic/fig learningBase/fig learningPlan/lcWorkEnv ");
    public static final DitaClass LEARNINGPLAN_LCWORKENVDESCRIPTION = new DitaClass("- topic/p learningBase/p learningPlan/lcWorkEnvDescription ");
    public static final DitaClass LEARNINGPLAN_LEARNINGPLAN = new DitaClass("- topic/topic learningBase/learningBase learningPlan/learningPlan ");
    public static final DitaClass LEARNINGPLAN_LEARNINGPLANBODY = new DitaClass("- topic/body learningBase/learningBasebody learningPlan/learningPlanbody ");
    public static final DitaClass LEARNINGSUMMARY_LEARNINGSUMMARY = new DitaClass("- topic/topic learningBase/learningBase learningSummary/learningSummary ");
    public static final DitaClass LEARNINGSUMMARY_LEARNINGSUMMARYBODY = new DitaClass("- topic/body learningBase/learningBasebody learningSummary/learningSummarybody ");
    public static final DitaClass LEARNING_D_LCANSWERCONTENT = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcAnswerContent ");
    public static final DitaClass LEARNING_D_LCANSWEROPTION = new DitaClass("+ topic/li learningInteractionBase-d/li learning-d/lcAnswerOption ");
    public static final DitaClass LEARNING_D_LCANSWEROPTIONGROUP = new DitaClass("+ topic/ul learningInteractionBase-d/ul learning-d/lcAnswerOptionGroup ");
    public static final DitaClass LEARNING_D_LCAREA = new DitaClass("+ topic/figgroup learningInteractionBase-d/figgroup learning-d/lcArea ");
    public static final DitaClass LEARNING_D_LCAREACOORDS = new DitaClass("+ topic/ph learningInteractionBase-d/ph learning-d/lcAreaCoords ");
    public static final DitaClass LEARNING_D_LCAREASHAPE = new DitaClass("+ topic/keyword learningInteractionBase-d/keyword learning-d/lcAreaShape ");
    public static final DitaClass LEARNING_D_LCASSET = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcAsset ");
    public static final DitaClass LEARNING_D_LCCORRECTRESPONSE = new DitaClass("+ topic/data learningInteractionBase-d/data learning-d/lcCorrectResponse ");
    public static final DitaClass LEARNING_D_LCFEEDBACK = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcFeedback ");
    public static final DitaClass LEARNING_D_LCFEEDBACKCORRECT = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackCorrect ");
    public static final DitaClass LEARNING_D_LCFEEDBACKINCORRECT = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackIncorrect ");
    public static final DitaClass LEARNING_D_LCHOTSPOT = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcHotspot ");
    public static final DitaClass LEARNING_D_LCHOTSPOTMAP = new DitaClass("+ topic/fig learningInteractionBase-d/figgroup learning-d/lcHotspotMap ");
    public static final DitaClass LEARNING_D_LCINSTRUCTORNOTE = new DitaClass("+ topic/note learningInteractionBase-d/note learning-d/lcInstructornote ");
    public static final DitaClass LEARNING_D_LCITEM = new DitaClass("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcItem ");
    public static final DitaClass LEARNING_D_LCMATCHING = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMatching ");
    public static final DitaClass LEARNING_D_LCMATCHINGHEADER = new DitaClass("+ topic/sthead learningInteractionBase-d/sthead learning-d/lcMatchingHeader ");
    public static final DitaClass LEARNING_D_LCMATCHINGITEM = new DitaClass("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItem ");
    public static final DitaClass LEARNING_D_LCMATCHINGITEMFEEDBACK = new DitaClass("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItemFeedback ");
    public static final DitaClass LEARNING_D_LCMATCHINGPAIR = new DitaClass("+ topic/strow learningInteractionBase-d/strow learning-d/lcMatchingPair ");
    public static final DitaClass LEARNING_D_LCMATCHTABLE = new DitaClass("+ topic/simpletable learningInteractionBase-d/simpletable learning-d/lcMatchTable ");
    public static final DitaClass LEARNING_D_LCMULTIPLESELECT = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMultipleSelect ");
    public static final DitaClass LEARNING_D_LCOPENANSWER = new DitaClass("+ topic/p learningInteractionBase-d/p learning-d/lcOpenAnswer ");
    public static final DitaClass LEARNING_D_LCOPENQUESTION = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcOpenQuestion ");
    public static final DitaClass LEARNING_D_LCQUESTION = new DitaClass("+ topic/p learningInteractionBase-d/lcQuestionBase learning-d/lcQuestion ");
    public static final DitaClass LEARNING_D_LCSEQUENCE = new DitaClass("+ topic/data learningInteractionBase-d/data learning-d/lcSequence ");
    public static final DitaClass LEARNING_D_LCSEQUENCEOPTION = new DitaClass("+ topic/li learningInteractionBase-d/li learning-d/lcSequenceOption ");
    public static final DitaClass LEARNING_D_LCSEQUENCEOPTIONGROUP = new DitaClass("+ topic/ol learningInteractionBase-d/ol learning-d/lcSequenceOptionGroup ");
    public static final DitaClass LEARNING_D_LCSEQUENCING = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSequencing ");
    public static final DitaClass LEARNING_D_LCSINGLESELECT = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSingleSelect ");
    public static final DitaClass LEARNING_D_LCTRUEFALSE = new DitaClass("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcTrueFalse ");
    public static final DitaClass MAPGROUP_D_ANCHORREF = new DitaClass("+ map/topicref mapgroup-d/anchorref ");
    public static final DitaClass MAPGROUP_D_KEYDEF = new DitaClass("+ map/topicref mapgroup-d/keydef ");
    public static final DitaClass MAPGROUP_D_MAPREF = new DitaClass("+ map/topicref mapgroup-d/mapref ");
    public static final DitaClass MAPGROUP_D_TOPICGROUP = new DitaClass("+ map/topicref mapgroup-d/topicgroup ");
    public static final DitaClass MAPGROUP_D_TOPICHEAD = new DitaClass("+ map/topicref mapgroup-d/topichead ");
    public static final DitaClass MAPGROUP_D_TOPICSET = new DitaClass("+ map/topicref mapgroup-d/topicset ");
    public static final DitaClass MAPGROUP_D_TOPICSETREF = new DitaClass("+ map/topicref mapgroup-d/topicsetref ");
    public static final DitaClass MAP_ANCHOR = new DitaClass("- map/anchor ");
    public static final DitaClass MAP_LINKTEXT = new DitaClass("- map/linktext ");
    public static final DitaClass MAP_MAP = new DitaClass("- map/map ");
    public static final DitaClass MAP_NAVREF = new DitaClass("- map/navref ");
    public static final DitaClass MAP_RELCELL = new DitaClass("- map/relcell ");
    public static final DitaClass MAP_RELCOLSPEC = new DitaClass("- map/relcolspec ");
    public static final DitaClass MAP_RELHEADER = new DitaClass("- map/relheader ");
    public static final DitaClass MAP_RELROW = new DitaClass("- map/relrow ");
    public static final DitaClass MAP_RELTABLE = new DitaClass("- map/reltable ");
    public static final DitaClass MAP_SEARCHTITLE = new DitaClass("- map/searchtitle ");
    public static final DitaClass MAP_SHORTDESC = new DitaClass("- map/shortdesc ");
    public static final DitaClass MAP_TOPICMETA = new DitaClass("- map/topicmeta ");
    public static final DitaClass MAP_TOPICREF = new DitaClass("- map/topicref ");
    public static final DitaClass PR_D_APINAME = new DitaClass("+ topic/keyword pr-d/apiname ");
    public static final DitaClass PR_D_CODEBLOCK = new DitaClass("+ topic/pre pr-d/codeblock ");
    public static final DitaClass PR_D_CODEPH = new DitaClass("+ topic/ph pr-d/codeph ");
    public static final DitaClass PR_D_CODEREF = new DitaClass("+ topic/xref pr-d/coderef ");
    public static final DitaClass PR_D_DELIM = new DitaClass("+ topic/ph pr-d/delim ");
    public static final DitaClass PR_D_FRAGMENT = new DitaClass("+ topic/figgroup pr-d/fragment ");
    public static final DitaClass PR_D_FRAGREF = new DitaClass("+ topic/xref pr-d/fragref ");
    public static final DitaClass PR_D_GROUPCHOICE = new DitaClass("+ topic/figgroup pr-d/groupchoice ");
    public static final DitaClass PR_D_GROUPCOMP = new DitaClass("+ topic/figgroup pr-d/groupcomp ");
    public static final DitaClass PR_D_GROUPSEQ = new DitaClass("+ topic/figgroup pr-d/groupseq ");
    public static final DitaClass PR_D_KWD = new DitaClass("+ topic/keyword pr-d/kwd ");
    public static final DitaClass PR_D_OPER = new DitaClass("+ topic/ph pr-d/oper ");
    public static final DitaClass PR_D_OPTION = new DitaClass("+ topic/keyword pr-d/option ");
    public static final DitaClass PR_D_PARML = new DitaClass("+ topic/dl pr-d/parml ");
    public static final DitaClass PR_D_PARMNAME = new DitaClass("+ topic/keyword pr-d/parmname ");
    public static final DitaClass PR_D_PD = new DitaClass("+ topic/dd pr-d/pd ");
    public static final DitaClass PR_D_PLENTRY = new DitaClass("+ topic/dlentry pr-d/plentry ");
    public static final DitaClass PR_D_PT = new DitaClass("+ topic/dt pr-d/pt ");
    public static final DitaClass PR_D_REPSEP = new DitaClass("+ topic/ph pr-d/repsep ");
    public static final DitaClass PR_D_SEP = new DitaClass("+ topic/ph pr-d/sep ");
    public static final DitaClass PR_D_SYNBLK = new DitaClass("+ topic/figgroup pr-d/synblk ");
    public static final DitaClass PR_D_SYNNOTE = new DitaClass("+ topic/fn pr-d/synnote ");
    public static final DitaClass PR_D_SYNNOTEREF = new DitaClass("+ topic/xref pr-d/synnoteref ");
    public static final DitaClass PR_D_SYNPH = new DitaClass("+ topic/ph pr-d/synph ");
    public static final DitaClass PR_D_SYNTAXDIAGRAM = new DitaClass("+ topic/fig pr-d/syntaxdiagram ");
    public static final DitaClass PR_D_VAR = new DitaClass("+ topic/ph pr-d/var ");
    public static final DitaClass REFERENCE_PROPDESC = new DitaClass("- topic/stentry reference/propdesc ");
    public static final DitaClass REFERENCE_PROPDESCHD = new DitaClass("- topic/stentry reference/propdeschd ");
    public static final DitaClass REFERENCE_PROPERTIES = new DitaClass("- topic/simpletable reference/properties ");
    public static final DitaClass REFERENCE_PROPERTY = new DitaClass("- topic/strow reference/property ");
    public static final DitaClass REFERENCE_PROPHEAD = new DitaClass("- topic/sthead reference/prophead ");
    public static final DitaClass REFERENCE_PROPTYPE = new DitaClass("- topic/stentry reference/proptype ");
    public static final DitaClass REFERENCE_PROPTYPEHD = new DitaClass("- topic/stentry reference/proptypehd ");
    public static final DitaClass REFERENCE_PROPVALUE = new DitaClass("- topic/stentry reference/propvalue ");
    public static final DitaClass REFERENCE_PROPVALUEHD = new DitaClass("- topic/stentry reference/propvaluehd ");
    public static final DitaClass REFERENCE_REFBODY = new DitaClass("- topic/body reference/refbody ");
    public static final DitaClass REFERENCE_REFBODYDIV = new DitaClass("- topic/bodydiv reference/refbodydiv ");
    public static final DitaClass REFERENCE_REFERENCE = new DitaClass("- topic/topic reference/reference ");
    public static final DitaClass REFERENCE_REFSYN = new DitaClass("- topic/section reference/refsyn ");
    public static final DitaClass SUBJECTSCHEME_ATTRIBUTEDEF = new DitaClass("- topic/data subjectScheme/attributedef ");
    public static final DitaClass SUBJECTSCHEME_DEFAULTSUBJECT = new DitaClass("- map/topicref subjectScheme/defaultSubject ");
    public static final DitaClass SUBJECTSCHEME_ELEMENTDEF = new DitaClass("- topic/data subjectScheme/elementdef ");
    public static final DitaClass SUBJECTSCHEME_ENUMERATIONDEF = new DitaClass("- map/topicref subjectScheme/enumerationdef ");
    public static final DitaClass SUBJECTSCHEME_HASINSTANCE = new DitaClass("- map/topicref subjectScheme/hasInstance ");
    public static final DitaClass SUBJECTSCHEME_HASKIND = new DitaClass("- map/topicref subjectScheme/hasKind ");
    public static final DitaClass SUBJECTSCHEME_HASNARROWER = new DitaClass("- map/topicref subjectScheme/hasNarrower ");
    public static final DitaClass SUBJECTSCHEME_HASPART = new DitaClass("- map/topicref subjectScheme/hasPart ");
    public static final DitaClass SUBJECTSCHEME_HASRELATED = new DitaClass("- map/topicref subjectScheme/hasRelated ");
    public static final DitaClass SUBJECTSCHEME_RELATEDSUBJECTS = new DitaClass("- map/topicref subjectScheme/relatedSubjects ");
    public static final DitaClass SUBJECTSCHEME_SCHEMEREF = new DitaClass("- map/topicref subjectScheme/schemeref ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTDEF = new DitaClass("- map/topicref subjectScheme/subjectdef ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTHEAD = new DitaClass("- map/topicref subjectScheme/subjectHead ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTHEADMETA = new DitaClass("- map/topicmeta subjectScheme/subjectHeadMeta ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTREL = new DitaClass("- map/relrow subjectScheme/subjectRel ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTRELHEADER = new DitaClass("- map/relrow subjectScheme/subjectRelHeader ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTRELTABLE = new DitaClass("- map/reltable subjectScheme/subjectRelTable ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTROLE = new DitaClass("- map/relcell subjectScheme/subjectRole ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTSCHEME = new DitaClass("- map/map subjectScheme/subjectScheme ");
    public static final DitaClass SW_D_CMDNAME = new DitaClass("+ topic/keyword sw-d/cmdname ");
    public static final DitaClass SW_D_FILEPATH = new DitaClass("+ topic/ph sw-d/filepath ");
    public static final DitaClass SW_D_MSGBLOCK = new DitaClass("+ topic/pre sw-d/msgblock ");
    public static final DitaClass SW_D_MSGNUM = new DitaClass("+ topic/keyword sw-d/msgnum ");
    public static final DitaClass SW_D_MSGPH = new DitaClass("+ topic/ph sw-d/msgph ");
    public static final DitaClass SW_D_SYSTEMOUTPUT = new DitaClass("+ topic/ph sw-d/systemoutput ");
    public static final DitaClass SW_D_USERINPUT = new DitaClass("+ topic/ph sw-d/userinput ");
    public static final DitaClass SW_D_VARNAME = new DitaClass("+ topic/keyword sw-d/varname ");
    public static final DitaClass TASKREQ_D_CLOSEREQS = new DitaClass("+ topic/section task/postreq taskreq-d/closereqs ");
    public static final DitaClass TASKREQ_D_ESTTIME = new DitaClass("+ topic/li task/li taskreq-d/esttime ");
    public static final DitaClass TASKREQ_D_NOCONDS = new DitaClass("+ topic/li task/li taskreq-d/noconds ");
    public static final DitaClass TASKREQ_D_NOSAFETY = new DitaClass("+ topic/li task/li taskreq-d/nosafety ");
    public static final DitaClass TASKREQ_D_NOSPARES = new DitaClass("+ topic/data task/data taskreq-d/nospares ");
    public static final DitaClass TASKREQ_D_NOSUPEQ = new DitaClass("+ topic/data task/data taskreq-d/nosupeq ");
    public static final DitaClass TASKREQ_D_NOSUPPLY = new DitaClass("+ topic/data task/data taskreq-d/nosupply ");
    public static final DitaClass TASKREQ_D_PERSCAT = new DitaClass("+ topic/li task/li taskreq-d/perscat ");
    public static final DitaClass TASKREQ_D_PERSKILL = new DitaClass("+ topic/li task/li taskreq-d/perskill ");
    public static final DitaClass TASKREQ_D_PERSONNEL = new DitaClass("+ topic/li task/li taskreq-d/personnel ");
    public static final DitaClass TASKREQ_D_PRELREQS = new DitaClass("+ topic/section task/prereq taskreq-d/prelreqs ");
    public static final DitaClass TASKREQ_D_REQCOND = new DitaClass("+ topic/li task/li taskreq-d/reqcond ");
    public static final DitaClass TASKREQ_D_REQCONDS = new DitaClass("+ topic/ol task/ol taskreq-d/reqconds ");
    public static final DitaClass TASKREQ_D_REQCONTP = new DitaClass("+ topic/li task/li taskreq-d/reqcontp ");
    public static final DitaClass TASKREQ_D_REQPERS = new DitaClass("+ topic/ol task/ol taskreq-d/reqpers ");
    public static final DitaClass TASKREQ_D_SAFECOND = new DitaClass("+ topic/li task/li taskreq-d/safecond ");
    public static final DitaClass TASKREQ_D_SAFETY = new DitaClass("+ topic/ol task/ol taskreq-d/safety ");
    public static final DitaClass TASKREQ_D_SPARE = new DitaClass("+ topic/li task/li taskreq-d/spare ");
    public static final DitaClass TASKREQ_D_SPARES = new DitaClass("+ topic/p task/p taskreq-d/spares ");
    public static final DitaClass TASKREQ_D_SPARESLI = new DitaClass("+ topic/ul task/ul taskreq-d/sparesli ");
    public static final DitaClass TASKREQ_D_SUPEQLI = new DitaClass("+ topic/ul task/ul taskreq-d/supeqli ");
    public static final DitaClass TASKREQ_D_SUPEQUI = new DitaClass("+ topic/li task/li taskreq-d/supequi ");
    public static final DitaClass TASKREQ_D_SUPEQUIP = new DitaClass("+ topic/p task/p taskreq-d/supequip ");
    public static final DitaClass TASKREQ_D_SUPPLIES = new DitaClass("+ topic/p task/p taskreq-d/supplies ");
    public static final DitaClass TASKREQ_D_SUPPLY = new DitaClass("+ topic/li task/li taskreq-d/supply ");
    public static final DitaClass TASKREQ_D_SUPPLYLI = new DitaClass("+ topic/ul task/ul taskreq-d/supplyli ");
    public static final DitaClass TASK_CHDESC = new DitaClass("- topic/stentry task/chdesc ");
    public static final DitaClass TASK_CHDESCHD = new DitaClass("- topic/stentry task/chdeschd ");
    public static final DitaClass TASK_CHHEAD = new DitaClass("- topic/sthead task/chhead ");
    public static final DitaClass TASK_CHOICE = new DitaClass("- topic/li task/choice ");
    public static final DitaClass TASK_CHOICES = new DitaClass("- topic/ul task/choices ");
    public static final DitaClass TASK_CHOICETABLE = new DitaClass("- topic/simpletable task/choicetable ");
    public static final DitaClass TASK_CHOPTION = new DitaClass("- topic/stentry task/choption ");
    public static final DitaClass TASK_CHOPTIONHD = new DitaClass("- topic/stentry task/choptionhd ");
    public static final DitaClass TASK_CHROW = new DitaClass("- topic/strow task/chrow ");
    public static final DitaClass TASK_CMD = new DitaClass("- topic/ph task/cmd ");
    public static final DitaClass TASK_CONTEXT = new DitaClass("- topic/section task/context ");
    public static final DitaClass TASK_INFO = new DitaClass("- topic/itemgroup task/info ");
    public static final DitaClass TASK_POSTREQ = new DitaClass("- topic/section task/postreq ");
    public static final DitaClass TASK_PREREQ = new DitaClass("- topic/section task/prereq ");
    public static final DitaClass TASK_RESULT = new DitaClass("- topic/section task/result ");
    public static final DitaClass TASK_STEP = new DitaClass("- topic/li task/step ");
    public static final DitaClass TASK_STEPRESULT = new DitaClass("- topic/itemgroup task/stepresult ");
    public static final DitaClass TASK_STEPS = new DitaClass("- topic/ol task/steps ");
    public static final DitaClass TASK_STEPSECTION = new DitaClass("- topic/li task/stepsection ");
    public static final DitaClass TASK_STEPS_INFORMAL = new DitaClass("- topic/section task/steps-informal ");
    public static final DitaClass TASK_STEPS_UNORDERED = new DitaClass("- topic/ul task/steps-unordered ");
    public static final DitaClass TASK_STEPXMP = new DitaClass("- topic/itemgroup task/stepxmp ");
    public static final DitaClass TASK_SUBSTEP = new DitaClass("- topic/li task/substep ");
    public static final DitaClass TASK_SUBSTEPS = new DitaClass("- topic/ol task/substeps ");
    public static final DitaClass TASK_TASK = new DitaClass("- topic/topic task/task ");
    public static final DitaClass TASK_TASKBODY = new DitaClass("- topic/body task/taskbody ");
    public static final DitaClass TASK_TUTORIALINFO = new DitaClass("- topic/itemgroup task/tutorialinfo ");
    public static final DitaClass TOPIC_ABSTRACT = new DitaClass("- topic/abstract ");
    public static final DitaClass TOPIC_ALT = new DitaClass("- topic/alt ");
    public static final DitaClass TOPIC_AUDIENCE = new DitaClass("- topic/audience ");
    public static final DitaClass TOPIC_AUTHOR = new DitaClass("- topic/author ");
    public static final DitaClass TOPIC_BODY = new DitaClass("- topic/body ");
    public static final DitaClass TOPIC_BODYDIV = new DitaClass("- topic/bodydiv ");
    public static final DitaClass TOPIC_BOOLEAN = new DitaClass("- topic/boolean ");
    public static final DitaClass TOPIC_BRAND = new DitaClass("- topic/brand ");
    public static final DitaClass TOPIC_CATEGORY = new DitaClass("- topic/category ");
    public static final DitaClass TOPIC_CITE = new DitaClass("- topic/cite ");
    public static final DitaClass TOPIC_COLSPEC = new DitaClass("- topic/colspec ");
    public static final DitaClass TOPIC_COMPONENT = new DitaClass("- topic/component ");
    public static final DitaClass TOPIC_COPYRHOLDER = new DitaClass("- topic/copyrholder ");
    public static final DitaClass TOPIC_COPYRIGHT = new DitaClass("- topic/copyright ");
    public static final DitaClass TOPIC_COPYRYEAR = new DitaClass("- topic/copyryear ");
    public static final DitaClass TOPIC_CREATED = new DitaClass("- topic/created ");
    public static final DitaClass TOPIC_CRITDATES = new DitaClass("- topic/critdates ");
    public static final DitaClass TOPIC_DATA = new DitaClass("- topic/data ");
    public static final DitaClass TOPIC_DATA_ABOUT = new DitaClass("- topic/data-about ");
    public static final DitaClass TOPIC_DD = new DitaClass("- topic/dd ");
    public static final DitaClass TOPIC_DDHD = new DitaClass("- topic/ddhd ");
    public static final DitaClass TOPIC_DESC = new DitaClass("- topic/desc ");
    public static final DitaClass TOPIC_DL = new DitaClass("- topic/dl ");
    public static final DitaClass TOPIC_DLENTRY = new DitaClass("- topic/dlentry ");
    public static final DitaClass TOPIC_DLHEAD = new DitaClass("- topic/dlhead ");
    public static final DitaClass TOPIC_DRAFT_COMMENT = new DitaClass("- topic/draft-comment ");
    public static final DitaClass TOPIC_DT = new DitaClass("- topic/dt ");
    public static final DitaClass TOPIC_DTHD = new DitaClass("- topic/dthd ");
    public static final DitaClass TOPIC_ENTRY = new DitaClass("- topic/entry ");
    public static final DitaClass TOPIC_EXAMPLE = new DitaClass("- topic/example ");
    public static final DitaClass TOPIC_FEATNUM = new DitaClass("- topic/featnum ");
    public static final DitaClass TOPIC_FIG = new DitaClass("- topic/fig ");
    public static final DitaClass TOPIC_FIGGROUP = new DitaClass("- topic/figgroup ");
    public static final DitaClass TOPIC_FN = new DitaClass("- topic/fn ");
    public static final DitaClass TOPIC_FOREIGN = new DitaClass("- topic/foreign ");
    public static final DitaClass TOPIC_IMAGE = new DitaClass("- topic/image ");
    public static final DitaClass TOPIC_INDEXTERM = new DitaClass("- topic/indexterm ");
    public static final DitaClass TOPIC_INDEXTERMREF = new DitaClass("- topic/indextermref ");
    public static final DitaClass TOPIC_INDEX_BASE = new DitaClass("- topic/index-base ");
    public static final DitaClass TOPIC_ITEMGROUP = new DitaClass("- topic/itemgroup ");
    public static final DitaClass TOPIC_KEYWORD = new DitaClass("- topic/keyword ");
    public static final DitaClass TOPIC_KEYWORDS = new DitaClass("- topic/keywords ");
    public static final DitaClass TOPIC_LI = new DitaClass("- topic/li ");
    public static final DitaClass TOPIC_LINES = new DitaClass("- topic/lines ");
    public static final DitaClass TOPIC_LINK = new DitaClass("- topic/link ");
    public static final DitaClass TOPIC_LINKINFO = new DitaClass("- topic/linkinfo ");
    public static final DitaClass TOPIC_LINKLIST = new DitaClass("- topic/linklist ");
    public static final DitaClass TOPIC_LINKPOOL = new DitaClass("- topic/linkpool ");
    public static final DitaClass TOPIC_LINKTEXT = new DitaClass("- topic/linktext ");
    public static final DitaClass TOPIC_LONGDESCREF = new DitaClass("- topic/longdescref ");
    public static final DitaClass TOPIC_LONGQUOTEREF = new DitaClass("- topic/longquoteref ");
    public static final DitaClass TOPIC_LQ = new DitaClass("- topic/lq ");
    public static final DitaClass TOPIC_METADATA = new DitaClass("- topic/metadata ");
    public static final DitaClass TOPIC_NAVTITLE = new DitaClass("- topic/navtitle ");
    public static final DitaClass TOPIC_NOTE = new DitaClass("- topic/note ");
    public static final DitaClass TOPIC_NO_TOPIC_NESTING = new DitaClass("- topic/no-topic-nesting ");
    public static final DitaClass TOPIC_OBJECT = new DitaClass("- topic/object ");
    public static final DitaClass TOPIC_OL = new DitaClass("- topic/ol ");
    public static final DitaClass TOPIC_OTHERMETA = new DitaClass("- topic/othermeta ");
    public static final DitaClass TOPIC_P = new DitaClass("- topic/p ");
    public static final DitaClass TOPIC_PARAM = new DitaClass("- topic/param ");
    public static final DitaClass TOPIC_PERMISSIONS = new DitaClass("- topic/permissions ");
    public static final DitaClass TOPIC_PH = new DitaClass("- topic/ph ");
    public static final DitaClass TOPIC_PLATFORM = new DitaClass("- topic/platform ");
    public static final DitaClass TOPIC_PRE = new DitaClass("- topic/pre ");
    public static final DitaClass TOPIC_PRODINFO = new DitaClass("- topic/prodinfo ");
    public static final DitaClass TOPIC_PRODNAME = new DitaClass("- topic/prodname ");
    public static final DitaClass TOPIC_PROGNUM = new DitaClass("- topic/prognum ");
    public static final DitaClass TOPIC_PROLOG = new DitaClass("- topic/prolog ");
    public static final DitaClass TOPIC_PUBLISHER = new DitaClass("- topic/publisher ");
    public static final DitaClass TOPIC_Q = new DitaClass("- topic/q ");
    public static final DitaClass TOPIC_RELATED_LINKS = new DitaClass("- topic/related-links ");
    public static final DitaClass TOPIC_REQUIRED_CLEANUP = new DitaClass("- topic/required-cleanup ");
    public static final DitaClass TOPIC_RESOURCEID = new DitaClass("- topic/resourceid ");
    public static final DitaClass TOPIC_REVISED = new DitaClass("- topic/revised ");
    public static final DitaClass TOPIC_ROW = new DitaClass("- topic/row ");
    public static final DitaClass TOPIC_SEARCHTITLE = new DitaClass("- topic/searchtitle ");
    public static final DitaClass TOPIC_SECTION = new DitaClass("- topic/section ");
    public static final DitaClass TOPIC_SECTIONDIV = new DitaClass("- topic/sectiondiv ");
    public static final DitaClass TOPIC_SERIES = new DitaClass("- topic/series ");
    public static final DitaClass TOPIC_SHORTDESC = new DitaClass("- topic/shortdesc ");
    public static final DitaClass TOPIC_SIMPLETABLE = new DitaClass("- topic/simpletable ");
    public static final DitaClass TOPIC_SL = new DitaClass("- topic/sl ");
    public static final DitaClass TOPIC_SLI = new DitaClass("- topic/sli ");
    public static final DitaClass TOPIC_SOURCE = new DitaClass("- topic/source ");
    public static final DitaClass TOPIC_STATE = new DitaClass("- topic/state ");
    public static final DitaClass TOPIC_STENTRY = new DitaClass("- topic/stentry ");
    public static final DitaClass TOPIC_STHEAD = new DitaClass("- topic/sthead ");
    public static final DitaClass TOPIC_STROW = new DitaClass("- topic/strow ");
    public static final DitaClass TOPIC_TABLE = new DitaClass("- topic/table ");
    public static final DitaClass TOPIC_TBODY = new DitaClass("- topic/tbody ");
    public static final DitaClass TOPIC_TERM = new DitaClass("- topic/term ");
    public static final DitaClass TOPIC_TEXT = new DitaClass("- topic/text ");
    public static final DitaClass TOPIC_TGROUP = new DitaClass("- topic/tgroup ");
    public static final DitaClass TOPIC_THEAD = new DitaClass("- topic/thead ");
    public static final DitaClass TOPIC_TITLE = new DitaClass("- topic/title ");
    public static final DitaClass TOPIC_TITLEALTS = new DitaClass("- topic/titlealts ");
    public static final DitaClass TOPIC_TM = new DitaClass("- topic/tm ");
    public static final DitaClass TOPIC_TOPIC = new DitaClass("- topic/topic ");
    public static final DitaClass TOPIC_UL = new DitaClass("- topic/ul ");
    public static final DitaClass TOPIC_UNKNOWN = new DitaClass("- topic/unknown ");
    public static final DitaClass TOPIC_VRM = new DitaClass("- topic/vrm ");
    public static final DitaClass TOPIC_VRMLIST = new DitaClass("- topic/vrmlist ");
    public static final DitaClass TOPIC_XREF = new DitaClass("- topic/xref ");
    public static final DitaClass UI_D_MENUCASCADE = new DitaClass("+ topic/ph ui-d/menucascade ");
    public static final DitaClass UI_D_SCREEN = new DitaClass("+ topic/pre ui-d/screen ");
    public static final DitaClass UI_D_SHORTCUT = new DitaClass("+ topic/keyword ui-d/shortcut ");
    public static final DitaClass UI_D_UICONTROL = new DitaClass("+ topic/ph ui-d/uicontrol ");
    public static final DitaClass UI_D_WINTITLE = new DitaClass("+ topic/keyword ui-d/wintitle ");
    public static final DitaClass UT_D_AREA = new DitaClass("+ topic/figgroup ut-d/area ");
    public static final DitaClass UT_D_COORDS = new DitaClass("+ topic/ph ut-d/coords ");
    public static final DitaClass UT_D_IMAGEMAP = new DitaClass("+ topic/fig ut-d/imagemap ");
    public static final DitaClass UT_D_SHAPE = new DitaClass("+ topic/keyword ut-d/shape ");
    public static final DitaClass XNAL_D_ADDRESSDETAILS = new DitaClass("+ topic/ph xnal-d/addressdetails ");
    public static final DitaClass XNAL_D_ADMINISTRATIVEAREA = new DitaClass("+ topic/ph xnal-d/administrativearea ");
    public static final DitaClass XNAL_D_AUTHORINFORMATION = new DitaClass("+ topic/author xnal-d/authorinformation ");
    public static final DitaClass XNAL_D_CONTACTNUMBER = new DitaClass("+ topic/data xnal-d/contactnumber ");
    public static final DitaClass XNAL_D_CONTACTNUMBERS = new DitaClass("+ topic/data xnal-d/contactnumbers ");
    public static final DitaClass XNAL_D_COUNTRY = new DitaClass("+ topic/ph xnal-d/country ");
    public static final DitaClass XNAL_D_EMAILADDRESS = new DitaClass("+ topic/data xnal-d/emailaddress ");
    public static final DitaClass XNAL_D_EMAILADDRESSES = new DitaClass("+ topic/data xnal-d/emailaddresses ");
    public static final DitaClass XNAL_D_FIRSTNAME = new DitaClass("+ topic/data xnal-d/firstname ");
    public static final DitaClass XNAL_D_GENERATIONIDENTIFIER = new DitaClass("+ topic/data xnal-d/generationidentifier ");
    public static final DitaClass XNAL_D_HONORIFIC = new DitaClass("+ topic/data xnal-d/honorific ");
    public static final DitaClass XNAL_D_LASTNAME = new DitaClass("+ topic/data xnal-d/lastname ");
    public static final DitaClass XNAL_D_LOCALITY = new DitaClass("+ topic/ph xnal-d/locality ");
    public static final DitaClass XNAL_D_LOCALITYNAME = new DitaClass("+ topic/ph xnal-d/localityname ");
    public static final DitaClass XNAL_D_MIDDLENAME = new DitaClass("+ topic/data xnal-d/middlename ");
    public static final DitaClass XNAL_D_NAMEDETAILS = new DitaClass("+ topic/data xnal-d/namedetails ");
    public static final DitaClass XNAL_D_ORGANIZATIONINFO = new DitaClass("+ topic/data xnal-d/organizationinfo ");
    public static final DitaClass XNAL_D_ORGANIZATIONNAME = new DitaClass("+ topic/ph xnal-d/organizationname ");
    public static final DitaClass XNAL_D_ORGANIZATIONNAMEDETAILS = new DitaClass("+ topic/ph xnal-d/organizationnamedetails ");
    public static final DitaClass XNAL_D_OTHERINFO = new DitaClass("+ topic/data xnal-d/otherinfo ");
    public static final DitaClass XNAL_D_PERSONINFO = new DitaClass("+ topic/data xnal-d/personinfo ");
    public static final DitaClass XNAL_D_PERSONNAME = new DitaClass("+ topic/data xnal-d/personname ");
    public static final DitaClass XNAL_D_POSTALCODE = new DitaClass("+ topic/ph xnal-d/postalcode ");
    public static final DitaClass XNAL_D_THOROUGHFARE = new DitaClass("+ topic/ph xnal-d/thoroughfare ");
    public static final DitaClass XNAL_D_URL = new DitaClass("+ topic/data xnal-d/url ");
    public static final DitaClass XNAL_D_URLS = new DitaClass("+ topic/data xnal-d/urls ");
    public static final DitaClass DITAVAREF_D_DITAVALREF = new DitaClass("+ map/topicref ditavalref-d/ditavalref ");
    public static final DitaClass DITAVAREF_D_DITAVALMETA = new DitaClass("+ map/topicmeta ditavalref-d/ditavalmeta ");
    public static final DitaClass DITAVAREF_D_DVR_RESOURCEPREFIX = new DitaClass("+ topic/data ditavalref-d/dvrResourcePrefix ");
    public static final DitaClass DITAVAREF_D_DVR_RESOURCESUFFIX = new DitaClass("+ topic/data ditavalref-d/dvrResourceSuffix ");
    public static final DitaClass DITAVAREF_D_DVR_KEYSCOPEPREFIX = new DitaClass("+ topic/data ditavalref-d/dvrKeyscopePrefix ");
    public static final DitaClass DITAVAREF_D_DVR_KEYSCOPESUFFIX = new DitaClass("+ topic/data ditavalref-d/dvrKeyscopeSuffix ");
    
    public static final DitaClass SUBMAP = new DitaClass("+ map/topicref mapgroup-d/topicgroup ditaot-d/submap ");

    /**maplinks element.*/
    public static final String ELEMENT_NAME_MAPLINKS = "maplinks";
    /**prop element.*/
    public static final String ELEMENT_NAME_PROP = "prop";
    /**map element.*/
    public static final String ELEMENT_NAME_ACTION = "action";
    /**action element.*/
    public static final String ELEMENT_NAME_DITA = "dita";

    public static final String ATTRIBUTE_NAME_ALIGN = "align";
    /**conref attribute.*/
    public static final String ATTRIBUTE_NAME_CONREF = "conref";
    public static final String ATTRIBUTE_NAME_CONREFEND = "conrefend";
    /**href attribute.*/
    public static final String ATTRIBUTE_NAME_HREF = "href";
    /**mapref attribute.*/
    public static final String ATTRIBUTE_NAME_MAPREF = "mapref";
    /**navtitle attribute.*/
    public static final String ATTRIBUTE_NAME_NAVTITLE = "navtitle";
    /**format attribute.*/
    public static final String ATTRIBUTE_NAME_FORMAT = "format";
    /**charset attribute.*/
    public static final String ATTRIBUTE_NAME_CHARSET = "charset";
    /**charset attribute.*/
    public static final String ATTRIBUTE_NAME_LANG = "lang";
    /**att attribute.*/
    public static final String ATTRIBUTE_NAME_ATT = "att";
    /**val attribute.*/
    public static final String ATTRIBUTE_NAME_VAL = "val";
    /**id attribute.*/
    public static final String ATTRIBUTE_NAME_ID = "id";
    /**class attribute.*/
    public static final String ATTRIBUTE_NAME_CLASS = "class";
    /**colname attribute.*/
    public static final String ATTRIBUTE_NAME_COLNAME = "colname";
    /**morerows attribute.*/
    public static final String ATTRIBUTE_NAME_MOREROWS = "morerows";
    /**namest attribute.*/
    public static final String ATTRIBUTE_NAME_NAMEST = "namest";
    /**nameend attribute.*/
    public static final String ATTRIBUTE_NAME_NAMEEND = "nameend";
    /**xml:lang attribute.*/
    public static final String ATTRIBUTE_NAME_XML_LANG = "xml:lang";
    /**domains attribute.*/
    public static final String ATTRIBUTE_NAME_DOMAINS = "domains";
    /**props attribute.*/
    public static final String ATTRIBUTE_NAME_PROPS = "props";
    /**audience attribute.*/
    public static final String ATTRIBUTE_NAME_AUDIENCE = "audience";
    /**platform attribute.*/
    public static final String ATTRIBUTE_NAME_PLATFORM = "platform";
    /**product attribute.*/
    public static final String ATTRIBUTE_NAME_PRODUCT = "product";
    /**otherprops attribute.*/
    public static final String ATTRIBUTE_NAME_OTHERPROPS = "otherprops";
    /**scope attribute.*/
    public static final String ATTRIBUTE_NAME_SCOPE = "scope";
    /**type attribute.*/
    public static final String ATTRIBUTE_NAME_TYPE = "type";
    /**img attribute.*/
    public static final String ATTRIBUTE_NAME_IMG = "img";
    /**copy-to attribute.*/
    public static final String ATTRIBUTE_NAME_COPY_TO = "copy-to";
    /**data attribute.*/
    public static final String ATTRIBUTE_NAME_DATA = "data";
    /**codebase attribute.*/
    public static final String ATTRIBUTE_NAME_CODEBASE = "codebase";
    public static final String ATTRIBUTE_NAME_ARCHIVE = "archive";
    public static final String ATTRIBUTE_NAME_CLASSID = "classid";
    /**imageref attribute.*/
    public static final String ATTRIBUTE_NAME_IMAGEREF = "imageref";
    /**start attribute.*/
    public static final String ATTRIBUTE_NAME_START="start";
    /**conref attribute.*/
    public static final String ATTRIBUTE_NAME_END="end";
    /**conaction attribute.*/
    public static final String ATTRIBUTE_NAME_CONACTION="conaction";
    /**keyref attribute.*/
    public static final String ATTRIBUTE_NAME_KEYREF = "keyref";
    /**conkeyref attribute.*/
    public static final String ATTRIBUTE_NAME_CONKEYREF ="conkeyref";
    public static final String ATTRIBUTE_NAME_ARCHIVEKEYREFS = "archivekeyrefs";
    public static final String ATTRIBUTE_NAME_CLASSIDKEYREF = "classidkeyref";
    public static final String ATTRIBUTE_NAME_CODEBASEKEYREF = "codebasekeyref";
    public static final String ATTRIBUTE_NAME_DATAKEYREF = "datakeyref";
    /**keys attribute.*/
    public static final String ATTRIBUTE_NAME_KEYS = "keys";
    /**keys attribute.*/
    public static final String ATTRIBUTE_NAME_KEYSCOPE = "keyscope";
    /**xtrf attribute.*/
    public static final String ATTRIBUTE_NAME_XTRF = "xtrf";
    /**xtrc attribute.*/
    public static final String ATTRIBUTE_NAME_XTRC = "xtrc";
    /**processing-role attribute.*/
    public static final String ATTRIBUTE_NAME_PROCESSING_ROLE = "processing-role";
    /**toc attribute.*/
    public static final String ATTRIBUTE_NAME_TOC = "toc";
    /**print attribute.*/
    public static final String ATTRIBUTE_NAME_PRINT = "print";
    public static final String ATTRIBUTE_NAME_DELIVERYTARGET = "deliveryTarget";
    /**cascade attribute.*/
    public static final String ATTRIBUTE_NAME_CASCADE = "cascade";
    public static final String ATTRIBUTE_NAME_COLS = "cols";
    public static final String ATTRIBUTE_NAME_VALUE = "value";
    public static final String ATTRIBUTE_NAME_VALUETYPE = "valuetype";

    public static final String ATTRIBUTE_VALUETYPE_VALUE_REF = "ref";

    public static final String ATTRIBUTE_CASCADE_VALUE_MERGE = "merge";
    public static final String ATTRIBUTE_CASCADE_VALUE_NOMERGE = "nomerge";
    
    /** URI path separator. */
    public static final String URI_SEPARATOR = "/";
    /** UNIX path separator. */
    public static final String UNIX_SEPARATOR = "/";
    /** Windows path separator. */
    public static final String WINDOWS_SEPARATOR = "\\";

    /** Constants for index type(javahelp).*/
    public static final String INDEX_TYPE_JAVAHELP = "javahelp";
    /** Constants for index type(htmlhelp).*/
    public static final String INDEX_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for index type(eclipsehelp).*/
    public static final String INDEX_TYPE_ECLIPSEHELP = "eclipsehelp";

    /** Constants for transform type(pdf).*/
    public static final String TRANS_TYPE_PDF = "pdf";
    /** Constants for transform type(xhtml).*/
    public static final String TRANS_TYPE_XHTML = "xhtml";
    /** Constants for transform type(eclipsehelp).*/
    public static final String TRANS_TYPE_ECLIPSEHELP = "eclipsehelp";
    /** Constants for transform type(javahelp).*/
    public static final String TRANS_TYPE_JAVAHELP = "javahelp";
    /** Constants for transform type(htmlhelp).*/
    public static final String TRANS_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for transform type(eclipsecontent).*/
    public static final String TRANS_TYPE_ECLIPSECONTENT = "eclipsecontent";

    /** Constant for generated property file name(catalog-dita.xml).*/
    public static final String FILE_NAME_CATALOG = "catalog-dita.xml";
    //store the scheme files refered by a scheme file in the form of Map<String Set<String>>
    /** Constant for generated property file name(subrelation.xml).*/
    public static final String FILE_NAME_SUBJECT_RELATION = "subrelation.xml";

    /** Property name for input file system path. Deprecated since 2.2 */
    @Deprecated
    public static final String INPUT_DITAMAP = "user.input.file";
    public static final String INPUT_DITAMAP_URI = "user.input.file.uri";
    /** Property name for input file list file list file, i.e. file which points to a file which points to the input file */
    public static final String INPUT_DITAMAP_LIST_FILE_LIST = "user.input.file.listfile";
    /** Property name for input directory system path. Deprecated since 2.2 */
    @Deprecated
    public static final String INPUT_DIR = "user.input.dir";
    public static final String INPUT_DIR_URI = "user.input.dir.uri";
    /** Property name for copy-to target2sourcemap list file */
    public static final String COPYTO_TARGET_TO_SOURCE_MAP_LIST = "copytotarget2sourcemaplist";
    /** Property name for relflag image list file */
    public static final String REL_FLAGIMAGE_LIST="relflagimagelist";

    /**Constants for common params used in ant invoker(tempDir).*/
    public static final String ANT_INVOKER_PARAM_TEMPDIR = "tempDir";
    /**Constants for common params used in ant invoker(basedir).*/
    public static final String ANT_INVOKER_PARAM_BASEDIR = "basedir";
    /**Constants for common params used in ant invoker(inputmap).*/
    public static final String ANT_INVOKER_PARAM_INPUTMAP = "inputmap";
    /**Constants for common params used in ant invoker(ditaval).*/
    public static final String ANT_INVOKER_PARAM_DITAVAL = "ditaval";
    /**Constants for common params used in ant invoker(maplinks).*/
    public static final String ANT_INVOKER_PARAM_MAPLINKS = "maplinks";
    /** Argument name for enabling profiling. */
    public static final String ANT_INVOKER_PARAM_PROFILING_ENABLED = "profiling.enable";

    /**Constants for extensive params used in ant invoker(targetext).*/
    public static final String ANT_INVOKER_EXT_PARAM_TARGETEXT = "targetext";
    /**Constants for extensive params used in ant invoker(indextype).*/
    public static final String ANT_INVOKER_EXT_PARAM_INDEXTYPE = "indextype";
    /**Constants for extensive params used in ant invoker(indexclass).*/
    public static final String ANT_INVOKER_EXT_PARAM_INDEXCLASS = "indexclass";
    /**Constants for extensive params used in ant invoker(encoding).*/
    public static final String ANT_INVOKER_EXT_PARAM_ENCODING = "encoding";
    /**Constants for extensive params used in ant invoker(output).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUT = "output";
    /**Constants for extensive params used in ant invoker(input).*/
    public static final String ANT_INVOKER_EXT_PARAM_INPUT = "input";
    /**Constants for extensive params used in ant invoker(ditadir).*/
    public static final String ANT_INVOKER_EXT_PARAM_DITADIR = "ditadir";
    /**Constants for extensive params used in ant invoker(inputdir).*/
    public static final String ANT_INVOKER_EXT_PARAM_INPUTDIR = "inputdir";
    /**Constants for extensive params used in ant invoker(style).*/
    public static final String ANT_INVOKER_EXT_PARAM_STYLE = "style";
    /**Constants for extensive params used in ant invoker(transtype).*/
    public static final String ANT_INVOKER_EXT_PARAM_TRANSTYPE = "transtype";
    /**Constants for extensive params used in ant invoker(outercontrol).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTTERCONTROL="outercontrol";
    /**Constants for extensive params used in ant invoker(generatecopyouter).*/
    public static final String ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER="generatecopyouter";
    /**Constants for extensive params used in ant invoker(onlytopicinmap).*/
    public static final String ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP="onlytopicinmap";
    /**Constants for extensive params used in ant invoker(validate).*/
    public static final String ANT_INVOKER_EXT_PARAM_VALIDATE="validate";
    /**Constants for extensive params used in ant invoker(outputdir).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUTDIR="outputdir";
    /**Constants for extensive params used in ant invoker(gramcache).*/
    public static final String ANT_INVOKER_EXT_PARAM_GRAMCACHE="gramcache";
    public static final String ANT_INVOKER_EXT_PARAN_SETSYSTEMID="setsystemid";
    public static final String ANT_INVOKER_EXT_PARAN_FORCE_UNIQUE = "force-unique";
    public static final String ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR = "generate-debug-attributes";
    public static final String ANT_INVOKER_EXT_PARAM_PROCESSING_MODE = "processing-mode";
    /**Constants for line separator.*/
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**OS relevant constants(OS NAME).*/
    public static final String OS_NAME = System.getProperty("os.name");
    /**OS relevant constants(windows).*/
    public static final String OS_NAME_WINDOWS = "windows";

    /**
     * Misc string constants used in this toolkit.
     */
    /**STRING_EMPTY.*/
    public static final String STRING_EMPTY = "";
    /**LEFT_BRACKET.*/
    public static final String LEFT_BRACKET = "(";
    /**RIGHT_BRACKET.*/
    public static final String RIGHT_BRACKET = ")";
    /**SLASH.*/
    public static final String SLASH = "/";
    /**BACK_SLASH.*/
    public static final String BACK_SLASH = "\\";
    /**SHARP.*/
    public static final String SHARP = "#";
    /**STICK.*/
    public static final String STICK = "|";
    /**EQUAL.*/
    public static final String EQUAL = "=";
    /**COMMA.*/
    public static final String COMMA = ",";
    /**LESS_THAN.*/
    public static final String LESS_THAN = "<";
    /**GREATER_THAN.*/
    public static final String GREATER_THAN = ">";
    /**QUESTION.*/
    public static final String QUESTION = "?";
    /**QUOTATION.*/
    public static final String QUOTATION = "\"";
    /**COLON.*/
    public static final String COLON = ":";
    /**DOT.*/
    public static final String DOT= ".";
    /**DOUBLE_BACK_SLASH.*/
    public static final String DOUBLE_BACK_SLASH = "\\\\";
    /**COLON_DOUBLE_SLASH.*/
    public static final String COLON_DOUBLE_SLASH = "://";
    /**XML_HEAD.*/
    public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    /**STRING_BLANK.*/
    public static final String STRING_BLANK = " ";
    /**COUNTRY_US.*/
    public static final String COUNTRY_US = "us";
    /**LANGUAGE_EN.*/
    public static final String LANGUAGE_EN = "en";
    /**UTF8.*/
    public static final String UTF8 = "UTF-8";
    /**SAX_DRIVER_PROPERTY.*/
    public static final String SAX_DRIVER_PROPERTY = "org.xml.sax.driver";
    /**SAX_DRIVER_DEFAULT_CLASS.*/
    public static final String SAX_DRIVER_DEFAULT_CLASS = "org.apache.xerces.parsers.SAXParser";
    /**SAX_DRIVER_SUN_HACK_CLASS.*/
    public static final String SAX_DRIVER_SUN_HACK_CLASS = "com.sun.org.apache.xerces.internal.parsers.SAXParser";
    /**SAX_DRIVER_CRIMSON_CLASS.*/
    public static final String SAX_DRIVER_CRIMSON_CLASS = "org.apache.crimson.parser.XMLReaderImpl";
    /**FEATURE_NAMESPACE_PREFIX.*/
    public static final String FEATURE_NAMESPACE_PREFIX = "http://xml.org/sax/features/namespace-prefixes";
    /**FEATURE_NAMESPACE.*/
    public static final String FEATURE_NAMESPACE = "http://xml.org/sax/features/namespaces";
    /**FEATURE_VALIDATION.*/
    public static final String FEATURE_VALIDATION = "http://xml.org/sax/features/validation";
    /**FEATURE_VALIDATION_SCHEMA.*/
    public static final String FEATURE_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
    /**TEMP_DIR_DEFAULT.*/
    public static final String TEMP_DIR_DEFAULT = "temp";
    /**FILTER_ACTION_EXCLUDE.*/
    public static final String FILTER_ACTION_EXCLUDE = "exclude";
    /**ATTR_SCOPE_VALUE_LOCAL.*/
    public static final String ATTR_SCOPE_VALUE_LOCAL = "local";
    /**ATTR_SCOPE_VALUE_EXTERNAL.*/
    public static final String ATTR_SCOPE_VALUE_EXTERNAL = "external";
    /**ATTR_SCOPE_VALUE_PEER.*/
    public static final String ATTR_SCOPE_VALUE_PEER = "peer";
    /**ATTR_FORMAT_VALUE_DITA.*/
    public static final String ATTR_FORMAT_VALUE_DITA = "dita";
    /**ATTR_FORMAT_VALUE_DITAMAP.*/
    public static final String ATTR_FORMAT_VALUE_DITAMAP = "ditamap";
    public static final String ATTR_FORMAT_VALUE_DITAVAL = "ditaval";
    public static final String ATTR_FORMAT_VALUE_IMAGE = "image";
    public static final String ATTR_FORMAT_VALUE_HTML = "html";
    /**ATTRIBUTE_NAME_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_NAME_DITAARCHVERSION = "DITAArchVersion";
    /**ATTRIBUTE_PREFIX_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_PREFIX_DITAARCHVERSION = "ditaarch";
    /**ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION = XMLNS_ATTRIBUTE + ":ditaarch";
    public static final String DITA_NAMESPACE = "http://dita.oasis-open.org/architecture/2005/";

    /**ATTR_CLASS_VALUE_SUBJECT_SCHEME_BASE.*/
    public static final String ATTR_CLASS_VALUE_SUBJECT_SCHEME_BASE = " subjectScheme/";
    /**ATTR_PROCESSING_ROLE_VALUE_NORMAL.*/
    public static final String ATTR_PROCESSING_ROLE_VALUE_NORMAL = "normal";
    /**ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.*/
    public static final String ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY = "resource-only";

    /**ATTR_PRINT_VALUE_YES.*/
    public static final String ATTR_PRINT_VALUE_YES = "yes";
    /**ATTR_PRINT_VALUE_NO.*/
    public static final String ATTR_PRINT_VALUE_NO = "no";
    /**ATTR_PRINT_VALUE_PRINT_ONLY.*/
    public static final String ATTR_PRINT_VALUE_PRINT_ONLY = "printonly";

    /** Conaction mark value */
    public static final String ATTR_CONACTION_VALUE_MARK = "mark";
    /** Conaction push after value */
    public static final String ATTR_CONACTION_VALUE_PUSHAFTER = "pushafter";
    /** Conaction push before value */
    public static final String ATTR_CONACTION_VALUE_PUSHBEFORE = "pushbefore";
    /** Conaction push replace value */
    public static final String ATTR_CONACTION_VALUE_PUSHREPLACE = "pushreplace";


    /** constants for filtering or flagging. */
    public static final String DEFAULT_ACTION = "default";
    /**chunk attribute.*/
    public static final String ATTRIBUTE_NAME_CHUNK = "chunk";

    /**constants for indexterm prefix(See).*/
    public static final String IndexTerm_Prefix_See = "See";
    /**constants for indexterm prefix(See also).*/
    public static final String IndexTerm_Prefix_See_Also = "See also";
    /**name attribute.*/
    public static final String ATTRIBUTE_NAME_NAME = "name";
    /**type attribute value subjectScheme.*/
    public static final String ATTR_TYPE_VALUE_SUBJECT_SCHEME = "subjectScheme";
    /**store how many scheme files a ditamap file used in form of Map<String, Set<String>>.*/
    public static final String FILE_NAME_SUBJECT_DICTIONARY = "subject_scheme.dictionary";
    /**export.xml to store exported elements.*/
    public static final String FILE_NAME_EXPORT_XML = "export.xml";
    /**pluginId.xml to store the plugin id.*/
    public static final String FILE_NAME_PLUGIN_XML = "pluginId.xml";

    /** Configuration filename. */
    public static final String CONF_PROPERTIES = "configuration.properties";
    /** Generated configuration filename. */
    public static final String GEN_CONF_PROPERTIES = "plugin.properties";
    /** Configuration value separator. */
    public static final String CONF_LIST_SEPARATOR = ";";
    /** Property name for supported image extensions. */
    public static final String CONF_SUPPORTED_IMAGE_EXTENSIONS = "supported_image_extensions";
    /** Property name for supported HTML extensions. */
    public static final String CONF_SUPPORTED_HTML_EXTENSIONS = "supported_html_extensions";
    /** Property name for supported resource file extensions. */
    public static final String CONF_SUPPORTED_RESOURCE_EXTENSIONS = "supported_resource_extensions";
    /** Property name for print transtypes. */
    public static final String CONF_PRINT_TRANSTYPES = "print_transtypes";
    /** Property name for template files. */
    public static final String CONF_TEMPLATES = "templates";

    /** Project reference name for job configuration object. */
    public static final String ANT_REFERENCE_JOB = "job";

    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_PATH2PROJ_TARGET_URI = "path2project-uri";
    public static final String PI_WORKDIR_TARGET = "workdir";
    public static final String PI_WORKDIR_TARGET_URI = "workdir-uri";

    /**
     * Instances should NOT be constructed in standard programming.
     */
    private Constants() {
    }
}
