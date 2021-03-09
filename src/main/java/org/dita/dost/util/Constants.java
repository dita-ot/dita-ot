/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
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

    public static final DitaClass ABBREV_D_ABBREVIATED_FORM = DitaClass.getInstance("+ topic/term abbrev-d/abbreviated-form ");
    public static final DitaClass BOOKMAP_ABBREVLIST = DitaClass.getInstance("- map/topicref bookmap/abbrevlist ");
    public static final DitaClass BOOKMAP_AMENDMENTS = DitaClass.getInstance("- map/topicref bookmap/amendments ");
    public static final DitaClass BOOKMAP_APPENDICES = DitaClass.getInstance("- map/topicref bookmap/appendices ");
    public static final DitaClass BOOKMAP_APPENDIX = DitaClass.getInstance("- map/topicref bookmap/appendix ");
    public static final DitaClass BOOKMAP_APPROVED = DitaClass.getInstance("- topic/data bookmap/approved ");
    public static final DitaClass BOOKMAP_BACKMATTER = DitaClass.getInstance("- map/topicref bookmap/backmatter ");
    public static final DitaClass BOOKMAP_BIBLIOLIST = DitaClass.getInstance("- map/topicref bookmap/bibliolist ");
    public static final DitaClass BOOKMAP_BOOKABSTRACT = DitaClass.getInstance("- map/topicref bookmap/bookabstract ");
    public static final DitaClass BOOKMAP_BOOKCHANGEHISTORY = DitaClass.getInstance("- topic/data bookmap/bookchangehistory ");
    public static final DitaClass BOOKMAP_BOOKEVENT = DitaClass.getInstance("- topic/data bookmap/bookevent ");
    public static final DitaClass BOOKMAP_BOOKEVENTTYPE = DitaClass.getInstance("- topic/data bookmap/bookeventtype ");
    public static final DitaClass BOOKMAP_BOOKID = DitaClass.getInstance("- topic/data bookmap/bookid ");
    public static final DitaClass BOOKMAP_BOOKLIBRARY = DitaClass.getInstance("- topic/ph bookmap/booklibrary ");
    public static final DitaClass BOOKMAP_BOOKLIST = DitaClass.getInstance("- map/topicref bookmap/booklist ");
    public static final DitaClass BOOKMAP_BOOKLISTS = DitaClass.getInstance("- map/topicref bookmap/booklists ");
    public static final DitaClass BOOKMAP_BOOKMAP = DitaClass.getInstance("- map/map bookmap/bookmap ");
    public static final DitaClass BOOKMAP_BOOKMETA = DitaClass.getInstance("- map/topicmeta bookmap/bookmeta ");
    public static final DitaClass BOOKMAP_BOOKNUMBER = DitaClass.getInstance("- topic/data bookmap/booknumber ");
    public static final DitaClass BOOKMAP_BOOKOWNER = DitaClass.getInstance("- topic/data bookmap/bookowner ");
    public static final DitaClass BOOKMAP_BOOKPARTNO = DitaClass.getInstance("- topic/data bookmap/bookpartno ");
    public static final DitaClass BOOKMAP_BOOKRESTRICTION = DitaClass.getInstance("- topic/data bookmap/bookrestriction ");
    public static final DitaClass BOOKMAP_BOOKRIGHTS = DitaClass.getInstance("- topic/data bookmap/bookrights ");
    public static final DitaClass BOOKMAP_BOOKTITLE = DitaClass.getInstance("- topic/title bookmap/booktitle ");
    public static final DitaClass BOOKMAP_BOOKTITLEALT = DitaClass.getInstance("- topic/ph bookmap/booktitlealt ");
    public static final DitaClass BOOKMAP_CHAPTER = DitaClass.getInstance("- map/topicref bookmap/chapter ");
    public static final DitaClass BOOKMAP_COLOPHON = DitaClass.getInstance("- map/topicref bookmap/colophon ");
    public static final DitaClass BOOKMAP_COMPLETED = DitaClass.getInstance("- topic/ph bookmap/completed ");
    public static final DitaClass BOOKMAP_COPYRFIRST = DitaClass.getInstance("- topic/data bookmap/copyrfirst ");
    public static final DitaClass BOOKMAP_COPYRLAST = DitaClass.getInstance("- topic/data bookmap/copyrlast ");
    public static final DitaClass BOOKMAP_DAY = DitaClass.getInstance("- topic/ph bookmap/day ");
    public static final DitaClass BOOKMAP_DEDICATION = DitaClass.getInstance("- map/topicref bookmap/dedication ");
    public static final DitaClass BOOKMAP_DRAFTINTRO = DitaClass.getInstance("- map/topicref bookmap/draftintro ");
    public static final DitaClass BOOKMAP_EDITED = DitaClass.getInstance("- topic/data bookmap/edited ");
    public static final DitaClass BOOKMAP_EDITION = DitaClass.getInstance("- topic/data bookmap/edition ");
    public static final DitaClass BOOKMAP_FIGURELIST = DitaClass.getInstance("- map/topicref bookmap/figurelist ");
    public static final DitaClass BOOKMAP_FRONTMATTER = DitaClass.getInstance("- map/topicref bookmap/frontmatter ");
    public static final DitaClass BOOKMAP_GLOSSARYLIST = DitaClass.getInstance("- map/topicref bookmap/glossarylist ");
    public static final DitaClass BOOKMAP_INDEXLIST = DitaClass.getInstance("- map/topicref bookmap/indexlist ");
    public static final DitaClass BOOKMAP_ISBN = DitaClass.getInstance("- topic/data bookmap/isbn ");
    public static final DitaClass BOOKMAP_MAINBOOKTITLE = DitaClass.getInstance("- topic/ph bookmap/mainbooktitle ");
    public static final DitaClass BOOKMAP_MAINTAINER = DitaClass.getInstance("- topic/data bookmap/maintainer ");
    public static final DitaClass BOOKMAP_MONTH = DitaClass.getInstance("- topic/ph bookmap/month ");
    public static final DitaClass BOOKMAP_NOTICES = DitaClass.getInstance("- map/topicref bookmap/notices ");
    public static final DitaClass BOOKMAP_ORGANIZATION = DitaClass.getInstance("- topic/data bookmap/organization ");
    public static final DitaClass BOOKMAP_PART = DitaClass.getInstance("- map/topicref bookmap/part ");
    public static final DitaClass BOOKMAP_PERSON = DitaClass.getInstance("- topic/data bookmap/person ");
    public static final DitaClass BOOKMAP_PREFACE = DitaClass.getInstance("- map/topicref bookmap/preface ");
    public static final DitaClass BOOKMAP_PRINTLOCATION = DitaClass.getInstance("- topic/data bookmap/printlocation ");
    public static final DitaClass BOOKMAP_PUBLISHED = DitaClass.getInstance("- topic/data bookmap/published ");
    public static final DitaClass BOOKMAP_PUBLISHERINFORMATION = DitaClass.getInstance("- topic/publisher bookmap/publisherinformation ");
    public static final DitaClass BOOKMAP_PUBLISHTYPE = DitaClass.getInstance("- topic/data bookmap/publishtype ");
    public static final DitaClass BOOKMAP_REVIEWED = DitaClass.getInstance("- topic/data bookmap/reviewed ");
    public static final DitaClass BOOKMAP_REVISIONID = DitaClass.getInstance("- topic/ph bookmap/revisionid ");
    public static final DitaClass BOOKMAP_STARTED = DitaClass.getInstance("- topic/ph bookmap/started ");
    public static final DitaClass BOOKMAP_SUMMARY = DitaClass.getInstance("- topic/ph bookmap/summary ");
    public static final DitaClass BOOKMAP_TABLELIST = DitaClass.getInstance("- map/topicref bookmap/tablelist ");
    public static final DitaClass BOOKMAP_TESTED = DitaClass.getInstance("- topic/data bookmap/tested ");
    public static final DitaClass BOOKMAP_TOC = DitaClass.getInstance("- map/topicref bookmap/toc ");
    public static final DitaClass BOOKMAP_TRADEMARKLIST = DitaClass.getInstance("- map/topicref bookmap/trademarklist ");
    public static final DitaClass BOOKMAP_VOLUME = DitaClass.getInstance("- topic/data bookmap/volume ");
    public static final DitaClass BOOKMAP_YEAR = DitaClass.getInstance("- topic/ph bookmap/year ");
    public static final DitaClass CLASSIFY_D_SUBJECTCELL = DitaClass.getInstance("+ map/relcell classify-d/subjectCell ");
    public static final DitaClass CLASSIFY_D_SUBJECTREF = DitaClass.getInstance("+ map/topicref classify-d/subjectref ");
    public static final DitaClass CLASSIFY_D_TOPICAPPLY = DitaClass.getInstance("+ map/topicref classify-d/topicapply ");
    public static final DitaClass CLASSIFY_D_TOPICCELL = DitaClass.getInstance("+ map/relcell classify-d/topicCell ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECT = DitaClass.getInstance("+ map/topicref classify-d/topicsubject ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTHEADER = DitaClass.getInstance("+ map/relrow classify-d/topicSubjectHeader ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTROW = DitaClass.getInstance("+ map/relrow classify-d/topicSubjectRow ");
    public static final DitaClass CLASSIFY_D_TOPICSUBJECTTABLE = DitaClass.getInstance("+ map/reltable classify-d/topicSubjectTable ");
    public static final DitaClass CONCEPT_CONBODY = DitaClass.getInstance("- topic/body concept/conbody ");
    public static final DitaClass CONCEPT_CONBODYDIV = DitaClass.getInstance("- topic/bodydiv concept/conbodydiv ");
    public static final DitaClass CONCEPT_CONCEPT = DitaClass.getInstance("- topic/topic concept/concept ");
    public static final DitaClass DELAY_D_ANCHORID = DitaClass.getInstance("+ topic/keyword delay-d/anchorid ");
    public static final DitaClass DELAY_D_ANCHORKEY = DitaClass.getInstance("+ topic/keyword delay-d/anchorkey ");
    public static final DitaClass DELAY_D_EXPORTANCHORS = DitaClass.getInstance("+ topic/keywords delay-d/exportanchors ");
    public static final DitaClass DITAVAREF_D_DITAVALMETA = DitaClass.getInstance("+ map/topicmeta ditavalref-d/ditavalmeta ");
    public static final DitaClass DITAVAREF_D_DITAVALREF = DitaClass.getInstance("+ map/topicref ditavalref-d/ditavalref ");
    public static final DitaClass DITAVAREF_D_DVR_KEYSCOPEPREFIX = DitaClass.getInstance("+ topic/data ditavalref-d/dvrKeyscopePrefix ");
    public static final DitaClass DITAVAREF_D_DVR_KEYSCOPESUFFIX = DitaClass.getInstance("+ topic/data ditavalref-d/dvrKeyscopeSuffix ");
    public static final DitaClass DITAVAREF_D_DVR_RESOURCEPREFIX = DitaClass.getInstance("+ topic/data ditavalref-d/dvrResourcePrefix ");
    public static final DitaClass DITAVAREF_D_DVR_RESOURCESUFFIX = DitaClass.getInstance("+ topic/data ditavalref-d/dvrResourceSuffix ");
    public static final DitaClass EQUATION_D_EQUATION_BLOCK = DitaClass.getInstance("+ topic/div equation-d/equation-block ");
    public static final DitaClass EQUATION_D_EQUATION_FIGURE = DitaClass.getInstance("+ topic/fig equation-d/equation-figure ");
    public static final DitaClass EQUATION_D_EQUATION_INLINE = DitaClass.getInstance("+ topic/ph equation-d/equation-inline ");
    public static final DitaClass EQUATION_D_EQUATION_NUMBER = DitaClass.getInstance("+ topic/ph equation-d/equation-number ");
    public static final DitaClass GLOSSENTRY_GLOSSABBREVIATION = DitaClass.getInstance("- topic/title concept/title glossentry/glossAbbreviation ");
    public static final DitaClass GLOSSENTRY_GLOSSACRONYM = DitaClass.getInstance("- topic/title concept/title glossentry/glossAcronym ");
    public static final DitaClass GLOSSENTRY_GLOSSALT = DitaClass.getInstance("- topic/section concept/section glossentry/glossAlt ");
    public static final DitaClass GLOSSENTRY_GLOSSALTERNATEFOR = DitaClass.getInstance("- topic/xref concept/xref glossentry/glossAlternateFor ");
    public static final DitaClass GLOSSENTRY_GLOSSBODY = DitaClass.getInstance("- topic/body concept/conbody glossentry/glossBody ");
    public static final DitaClass GLOSSENTRY_GLOSSDEF = DitaClass.getInstance("- topic/abstract concept/abstract glossentry/glossdef ");
    public static final DitaClass GLOSSENTRY_GLOSSENTRY = DitaClass.getInstance("- topic/topic concept/concept glossentry/glossentry ");
    public static final DitaClass GLOSSENTRY_GLOSSPARTOFSPEECH = DitaClass.getInstance("- topic/data concept/data glossentry/glossPartOfSpeech ");
    public static final DitaClass GLOSSENTRY_GLOSSPROPERTY = DitaClass.getInstance("- topic/data concept/data glossentry/glossProperty ");
    public static final DitaClass GLOSSENTRY_GLOSSSCOPENOTE = DitaClass.getInstance("- topic/note concept/note glossentry/glossScopeNote ");
    public static final DitaClass GLOSSENTRY_GLOSSSHORTFORM = DitaClass.getInstance("- topic/title concept/title glossentry/glossShortForm ");
    public static final DitaClass GLOSSENTRY_GLOSSSTATUS = DitaClass.getInstance("- topic/data concept/data glossentry/glossStatus ");
    public static final DitaClass GLOSSENTRY_GLOSSSURFACEFORM = DitaClass.getInstance("- topic/p concept/p glossentry/glossSurfaceForm ");
    public static final DitaClass GLOSSENTRY_GLOSSSYMBOL = DitaClass.getInstance("- topic/image concept/image glossentry/glossSymbol ");
    public static final DitaClass GLOSSENTRY_GLOSSSYNONYM = DitaClass.getInstance("- topic/title concept/title glossentry/glossSynonym ");
    public static final DitaClass GLOSSENTRY_GLOSSTERM = DitaClass.getInstance("- topic/title concept/title glossentry/glossterm ");
    public static final DitaClass GLOSSENTRY_GLOSSUSAGE = DitaClass.getInstance("- topic/note concept/note glossentry/glossUsage ");
    public static final DitaClass GLOSSGROUP_GLOSSGROUP = DitaClass.getInstance("- topic/topic concept/concept glossgroup/glossgroup ");
    public static final DitaClass GLOSSREF_D_GLOSSREF = DitaClass.getInstance("+ map/topicref glossref-d/glossref ");
    public static final DitaClass HAZARD_D_CONSEQUENCE = DitaClass.getInstance("+ topic/li hazard-d/consequence ");
    public static final DitaClass HAZARD_D_HAZARDSTATEMENT = DitaClass.getInstance("+ topic/note hazard-d/hazardstatement ");
    public static final DitaClass HAZARD_D_HAZARDSYMBOL = DitaClass.getInstance("+ topic/image hazard-d/hazardsymbol ");
    public static final DitaClass HAZARD_D_HOWTOAVOID = DitaClass.getInstance("+ topic/li hazard-d/howtoavoid ");
    public static final DitaClass HAZARD_D_MESSAGEPANEL = DitaClass.getInstance("+ topic/ul hazard-d/messagepanel ");
    public static final DitaClass HAZARD_D_TYPEOFHAZARD = DitaClass.getInstance("+ topic/li hazard-d/typeofhazard ");
    public static final DitaClass HI_D_B = DitaClass.getInstance("+ topic/ph hi-d/b ");
    public static final DitaClass HI_D_I = DitaClass.getInstance("+ topic/ph hi-d/i ");
    public static final DitaClass HI_D_LINE_THROUGH = DitaClass.getInstance("+ topic/ph hi-d/line-through ");
    public static final DitaClass HI_D_OVERLINE = DitaClass.getInstance("+ topic/ph hi-d/overline ");
    public static final DitaClass HI_D_SUB = DitaClass.getInstance("+ topic/ph hi-d/sub ");
    public static final DitaClass HI_D_SUP = DitaClass.getInstance("+ topic/ph hi-d/sup ");
    public static final DitaClass HI_D_TT = DitaClass.getInstance("+ topic/ph hi-d/tt ");
    public static final DitaClass HI_D_U = DitaClass.getInstance("+ topic/ph hi-d/u ");
    public static final DitaClass INDEXING_D_INDEX_SEE = DitaClass.getInstance("+ topic/index-base indexing-d/index-see ");
    public static final DitaClass INDEXING_D_INDEX_SEE_ALSO = DitaClass.getInstance("+ topic/index-base indexing-d/index-see-also ");
    public static final DitaClass INDEXING_D_INDEX_SORT_AS = DitaClass.getInstance("+ topic/index-base indexing-d/index-sort-as ");
    public static final DitaClass LEARNING_D_LCANSWERCONTENT = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcAnswerContent ");
    public static final DitaClass LEARNING_D_LCANSWEROPTION = DitaClass.getInstance("+ topic/li learningInteractionBase-d/li learning-d/lcAnswerOption ");
    public static final DitaClass LEARNING_D_LCANSWEROPTIONGROUP = DitaClass.getInstance("+ topic/ul learningInteractionBase-d/ul learning-d/lcAnswerOptionGroup ");
    public static final DitaClass LEARNING_D_LCAREA = DitaClass.getInstance("+ topic/figgroup learningInteractionBase-d/figgroup learning-d/lcArea ");
    public static final DitaClass LEARNING_D_LCAREACOORDS = DitaClass.getInstance("+ topic/ph learningInteractionBase-d/ph learning-d/lcAreaCoords ");
    public static final DitaClass LEARNING_D_LCAREASHAPE = DitaClass.getInstance("+ topic/keyword learningInteractionBase-d/keyword learning-d/lcAreaShape ");
    public static final DitaClass LEARNING_D_LCASSET = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcAsset ");
    public static final DitaClass LEARNING_D_LCCORRECTRESPONSE = DitaClass.getInstance("+ topic/data learningInteractionBase-d/data learning-d/lcCorrectResponse ");
    public static final DitaClass LEARNING_D_LCFEEDBACK = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcFeedback ");
    public static final DitaClass LEARNING_D_LCFEEDBACKCORRECT = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackCorrect ");
    public static final DitaClass LEARNING_D_LCFEEDBACKINCORRECT = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackIncorrect ");
    public static final DitaClass LEARNING_D_LCHOTSPOT = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcHotspot ");
    public static final DitaClass LEARNING_D_LCHOTSPOTMAP = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/figgroup learning-d/lcHotspotMap ");
    public static final DitaClass LEARNING_D_LCINSTRUCTORNOTE = DitaClass.getInstance("+ topic/note learningInteractionBase-d/note learning-d/lcInstructornote ");
    public static final DitaClass LEARNING_D_LCITEM = DitaClass.getInstance("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcItem ");
    public static final DitaClass LEARNING_D_LCMATCHING = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMatching ");
    public static final DitaClass LEARNING_D_LCMATCHINGHEADER = DitaClass.getInstance("+ topic/sthead learningInteractionBase-d/sthead learning-d/lcMatchingHeader ");
    public static final DitaClass LEARNING_D_LCMATCHINGITEM = DitaClass.getInstance("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItem ");
    public static final DitaClass LEARNING_D_LCMATCHINGITEMFEEDBACK = DitaClass.getInstance("+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItemFeedback ");
    public static final DitaClass LEARNING_D_LCMATCHINGPAIR = DitaClass.getInstance("+ topic/strow learningInteractionBase-d/strow learning-d/lcMatchingPair ");
    public static final DitaClass LEARNING_D_LCMATCHTABLE = DitaClass.getInstance("+ topic/simpletable learningInteractionBase-d/simpletable learning-d/lcMatchTable ");
    public static final DitaClass LEARNING_D_LCMULTIPLESELECT = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMultipleSelect ");
    public static final DitaClass LEARNING_D_LCOPENANSWER = DitaClass.getInstance("+ topic/p learningInteractionBase-d/p learning-d/lcOpenAnswer ");
    public static final DitaClass LEARNING_D_LCOPENQUESTION = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcOpenQuestion ");
    public static final DitaClass LEARNING_D_LCQUESTION = DitaClass.getInstance("+ topic/p learningInteractionBase-d/lcQuestionBase learning-d/lcQuestion ");
    public static final DitaClass LEARNING_D_LCSEQUENCE = DitaClass.getInstance("+ topic/data learningInteractionBase-d/data learning-d/lcSequence ");
    public static final DitaClass LEARNING_D_LCSEQUENCEOPTION = DitaClass.getInstance("+ topic/li learningInteractionBase-d/li learning-d/lcSequenceOption ");
    public static final DitaClass LEARNING_D_LCSEQUENCEOPTIONGROUP = DitaClass.getInstance("+ topic/ol learningInteractionBase-d/ol learning-d/lcSequenceOptionGroup ");
    public static final DitaClass LEARNING_D_LCSEQUENCING = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSequencing ");
    public static final DitaClass LEARNING_D_LCSINGLESELECT = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSingleSelect ");
    public static final DitaClass LEARNING_D_LCTRUEFALSE = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcTrueFalse ");
    public static final DitaClass LEARNING2_D_LCANSWERCONTENT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcAnswerContent2 ");
    public static final DitaClass LEARNING2_D_LCANSWEROPTION2 = DitaClass.getInstance("+ topic/li learningInteractionBase2-d/li learning2-d/lcAnswerOption2 ");
    public static final DitaClass LEARNING2_D_LCANSWEROPTIONGROUP2 = DitaClass.getInstance("+ topic/ul learningInteractionBase2-d/ul learning2-d/lcAnswerOptionGroup2 ");
    public static final DitaClass LEARNING2_D_LCAREA2 = DitaClass.getInstance("+ topic/figgroup learningInteractionBase2-d/figgroup learning2-d/lcArea2 ");
    public static final DitaClass LEARNING2_D_LCAREACOORDS2 = DitaClass.getInstance("+ topic/ph learningInteractionBase2-d/ph learning2-d/lcAreaCoords2 ");
    public static final DitaClass LEARNING2_D_LCAREASHAPE2 = DitaClass.getInstance("+ topic/keyword learningInteractionBase2-d/keyword learning2-d/lcAreaShape2 ");
    public static final DitaClass LEARNING2_D_LCASSET2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcAsset2 ");
    public static final DitaClass LEARNING2_D_LCCORRECTRESPONSE2 = DitaClass.getInstance("+ topic/data learningInteractionBase2-d/data learning2-d/lcCorrectResponse2 ");
    public static final DitaClass LEARNING2_D_LCFEEDBACK2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedback2 ");
    public static final DitaClass LEARNING2_D_LCFEEDBACKCORRECT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackCorrect2 ");
    public static final DitaClass LEARNING2_D_LCFEEDBACKINCORRECT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackIncorrect2 ");
    public static final DitaClass LEARNING2_D_LCHOTSPOT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcHotspot2 ");
    public static final DitaClass LEARNING2_D_LCHOTSPOTMAP2 = DitaClass.getInstance("+ topic/fig learningInteractionBase2-d/figgroup learning2-d/lcHotspotMap2 ");
    public static final DitaClass LEARNING2_D_LCINSTRUCTORNOTE2 = DitaClass.getInstance("+ topic/note learningInteractionBase2-d/note learning2-d/lcInstructornote2 ");
    public static final DitaClass LEARNING2_D_LCITEM2 = DitaClass.getInstance("+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcItem2 ");
    public static final DitaClass LEARNING2_D_LCMATCHING2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMatching2 ");
    public static final DitaClass LEARNING2_D_LCMATCHINGHEADER2 = DitaClass.getInstance("+ topic/sthead learningInteractionBase2-d/sthead learning2-d/lcMatchingHeader2 ");
    public static final DitaClass LEARNING2_D_LCMATCHINGITEM2 = DitaClass.getInstance("+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItem2 ");
    public static final DitaClass LEARNING2_D_LCMATCHINGITEMFEEDBACK2 = DitaClass.getInstance("+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItemFeedback2 ");
    public static final DitaClass LEARNING2_D_LCMATCHINGPAIR2 = DitaClass.getInstance("+ topic/strow learningInteractionBase2-d/strow learning2-d/lcMatchingPair2 ");
    public static final DitaClass LEARNING2_D_LCMATCHTABLE2 = DitaClass.getInstance("+ topic/simpletable learningInteractionBase2-d/simpletable learning2-d/lcMatchTable2 ");
    public static final DitaClass LEARNING2_D_LCMULTIPLESELECT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMultipleSelect2 ");
    public static final DitaClass LEARNING2_D_LCOPENANSWER2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/div learning2-d/lcOpenAnswer2 ");
    public static final DitaClass LEARNING2_D_LCOPENQUESTION2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcOpenQuestion2 ");
    public static final DitaClass LEARNING2_D_LCQUESTION2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcQuestionBase2 learning2-d/lcQuestion2 ");
    public static final DitaClass LEARNING2_D_LCSEQUENCE2 = DitaClass.getInstance("+ topic/data learningInteractionBase2-d/data learning2-d/lcSequence2 ");
    public static final DitaClass LEARNING2_D_LCSEQUENCEOPTION2 = DitaClass.getInstance("+ topic/li learningInteractionBase2-d/li learning2-d/lcSequenceOption2 ");
    public static final DitaClass LEARNING2_D_LCSEQUENCEOPTIONGROUP2 = DitaClass.getInstance("+ topic/ol learningInteractionBase2-d/ol learning2-d/lcSequenceOptionGroup2 ");
    public static final DitaClass LEARNING2_D_LCSEQUENCING2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSequencing2 ");
    public static final DitaClass LEARNING2_D_LCSINGLESELECT2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSingleSelect2 ");
    public static final DitaClass LEARNING2_D_LCTRUEFALSE2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcTrueFalse2 ");
    public static final DitaClass LEARNINGASSESSMENT_LEARNINGASSESSMENT = DitaClass.getInstance("- topic/topic learningBase/learningBase learningAssessment/learningAssessment ");
    public static final DitaClass LEARNINGASSESSMENT_LEARNINGASSESSMENTBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody learningAssessment/learningAssessmentbody ");
    public static final DitaClass LEARNINGBASE_LCAUDIENCE = DitaClass.getInstance("- topic/section learningBase/lcAudience ");
    public static final DitaClass LEARNINGBASE_LCCHALLENGE = DitaClass.getInstance("- topic/section learningBase/lcChallenge ");
    public static final DitaClass LEARNINGBASE_LCDURATION = DitaClass.getInstance("- topic/section learningBase/lcDuration ");
    public static final DitaClass LEARNINGBASE_LCINSTRUCTION = DitaClass.getInstance("- topic/section learningBase/lcInstruction ");
    public static final DitaClass LEARNINGBASE_LCINTERACTION = DitaClass.getInstance("- topic/section learningBase/lcInteraction ");
    public static final DitaClass LEARNINGBASE_LCINTRO = DitaClass.getInstance("- topic/section learningBase/lcIntro ");
    public static final DitaClass LEARNINGBASE_LCNEXTSTEPS = DitaClass.getInstance("- topic/section learningBase/lcNextSteps ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVE = DitaClass.getInstance("- topic/li learningBase/lcObjective ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVES = DitaClass.getInstance("- topic/section learningBase/lcObjectives ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVESGROUP = DitaClass.getInstance("- topic/ul learningBase/lcObjectivesGroup ");
    public static final DitaClass LEARNINGBASE_LCOBJECTIVESSTEM = DitaClass.getInstance("- topic/ph learningBase/lcObjectivesStem ");
    public static final DitaClass LEARNINGBASE_LCPREREQS = DitaClass.getInstance("- topic/section learningBase/lcPrereqs ");
    public static final DitaClass LEARNINGBASE_LCRESOURCES = DitaClass.getInstance("- topic/section learningBase/lcResources ");
    public static final DitaClass LEARNINGBASE_LCREVIEW = DitaClass.getInstance("- topic/section learningBase/lcReview ");
    public static final DitaClass LEARNINGBASE_LCSUMMARY = DitaClass.getInstance("- topic/section learningBase/lcSummary ");
    public static final DitaClass LEARNINGBASE_LCTIME = DitaClass.getInstance("- topic/data learningBase/lcTime ");
    public static final DitaClass LEARNINGBASE_LEARNINGBASE = DitaClass.getInstance("- topic/topic learningBase/learningBase ");
    public static final DitaClass LEARNINGBASE_LEARNINGBASEBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody ");
    public static final DitaClass LEARNINGCONTENT_LEARNINGCONTENT = DitaClass.getInstance("- topic/topic learningBase/learningBase learningContent/learningContent ");
    public static final DitaClass LEARNINGCONTENT_LEARNINGCONTENTBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody learningContent/learningContentbody ");
    public static final DitaClass LEARNINGGROUPMAP_LEARNINGGROUPMAP = DitaClass.getInstance("- map/map learningGroupMap/learningGroupMap ");
    public static final DitaClass LEARNINGINTERACTIONBASE_D_LCINTERACTIONBASE = DitaClass.getInstance("+ topic/fig learningInteractionBase-d/lcInteractionBase ");
    public static final DitaClass LEARNINGINTERACTIONBASE_D_LCQUESTIONBASE = DitaClass.getInstance("+ topic/p learningInteractionBase-d/lcQuestionBase ");
    public static final DitaClass LEARNINGINTERACTIONBASE2_D_LCINTERACTIONBASE2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcInteractionBase2 ");
    public static final DitaClass LEARNINGINTERACTIONBASE2_D_LCINTERACTIONLABEL2 = DitaClass.getInstance("+ topic/p learningInteractionBase2-d/lcInteractionLabel2 ");
    public static final DitaClass LEARNINGINTERACTIONBASE2_D_LCQUESTIONBASE2 = DitaClass.getInstance("+ topic/div learningInteractionBase2-d/lcQuestionBase2 ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGCONTENTCOMPONENTREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningContentComponentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGCONTENTREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningContentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGGROUP = DitaClass.getInstance("+ map/topicref learningmap-d/learningGroup ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGGROUPMAPREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningGroupMapRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGOBJECT = DitaClass.getInstance("+ map/topicref learningmap-d/learningObject ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGOBJECTMAPREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningObjectMapRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGOVERVIEWREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningOverviewRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPLANREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningPlanRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPOSTASSESSMENTREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningPostAssessmentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGPREASSESSMENTREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningPreAssessmentRef ");
    public static final DitaClass LEARNINGMAP_D_LEARNINGSUMMARYREF = DitaClass.getInstance("+ map/topicref learningmap-d/learningSummaryRef ");
    public static final DitaClass LEARNINGMETA_D_LCLOM = DitaClass.getInstance("+ topic/metadata learningmeta-d/lcLom ");
    public static final DitaClass LEARNINGMETA_D_LOMAGGREGATIONLEVEL = DitaClass.getInstance("+ topic/data learningmeta-d/lomAggregationLevel ");
    public static final DitaClass LEARNINGMETA_D_LOMCONTEXT = DitaClass.getInstance("+ topic/data learningmeta-d/lomContext ");
    public static final DitaClass LEARNINGMETA_D_LOMCOVERAGE = DitaClass.getInstance("+ topic/data learningmeta-d/lomCoverage ");
    public static final DitaClass LEARNINGMETA_D_LOMDIFFICULTY = DitaClass.getInstance("+ topic/data learningmeta-d/lomDifficulty ");
    public static final DitaClass LEARNINGMETA_D_LOMINSTALLATIONREMARKS = DitaClass.getInstance("+ topic/data learningmeta-d/lomInstallationRemarks ");
    public static final DitaClass LEARNINGMETA_D_LOMINTENDEDUSERROLE = DitaClass.getInstance("+ topic/data learningmeta-d/lomIntendedUserRole ");
    public static final DitaClass LEARNINGMETA_D_LOMINTERACTIVITYLEVEL = DitaClass.getInstance("+ topic/data learningmeta-d/lomInteractivityLevel ");
    public static final DitaClass LEARNINGMETA_D_LOMINTERACTIVITYTYPE = DitaClass.getInstance("+ topic/data learningmeta-d/lomInteractivityType ");
    public static final DitaClass LEARNINGMETA_D_LOMLEARNINGRESOURCETYPE = DitaClass.getInstance("+ topic/data learningmeta-d/lomLearningResourceType ");
    public static final DitaClass LEARNINGMETA_D_LOMOTHERPLATFORMREQUIREMENTS = DitaClass.getInstance("+ topic/data learningmeta-d/lomOtherPlatformRequirements ");
    public static final DitaClass LEARNINGMETA_D_LOMSEMANTICDENSITY = DitaClass.getInstance("+ topic/data learningmeta-d/lomSemanticDensity ");
    public static final DitaClass LEARNINGMETA_D_LOMSTRUCTURE = DitaClass.getInstance("+ topic/data learningmeta-d/lomStructure ");
    public static final DitaClass LEARNINGMETA_D_LOMTECHREQUIREMENT = DitaClass.getInstance("+ topic/data learningmeta-d/lomTechRequirement ");
    public static final DitaClass LEARNINGMETA_D_LOMTYPICALAGERANGE = DitaClass.getInstance("+ topic/data learningmeta-d/lomTypicalAgeRange ");
    public static final DitaClass LEARNINGMETA_D_LOMTYPICALLEARNINGTIME = DitaClass.getInstance("+ topic/data learningmeta-d/lomTypicalLearningTime ");
    public static final DitaClass LEARNINGOBJECTMAP_LEARNINGOBJECTMAP = DitaClass.getInstance("- map/map learningObjectMap/learningObjectMap ");
    public static final DitaClass LEARNINGOVERVIEW_LEARNINGOVERVIEW = DitaClass.getInstance("- topic/topic learningBase/learningBase learningOverview/learningOverview ");
    public static final DitaClass LEARNINGOVERVIEW_LEARNINGOVERVIEWBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody learningOverview/learningOverviewbody ");
    public static final DitaClass LEARNINGPLAN_LCAGE = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcAge ");
    public static final DitaClass LEARNINGPLAN_LCASSESSMENT = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcAssessment ");
    public static final DitaClass LEARNINGPLAN_LCATTITUDE = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcAttitude ");
    public static final DitaClass LEARNINGPLAN_LCBACKGROUND = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcBackground ");
    public static final DitaClass LEARNINGPLAN_LCCIN = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcCIN ");
    public static final DitaClass LEARNINGPLAN_LCCLASSROOM = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcClassroom ");
    public static final DitaClass LEARNINGPLAN_LCCLIENT = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcClient ");
    public static final DitaClass LEARNINGPLAN_LCCONSTRAINTS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcConstraints ");
    public static final DitaClass LEARNINGPLAN_LCDELIVDATE = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcDelivDate ");
    public static final DitaClass LEARNINGPLAN_LCDELIVERY = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcDelivery ");
    public static final DitaClass LEARNINGPLAN_LCDOWNLOADTIME = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcDownloadTime ");
    public static final DitaClass LEARNINGPLAN_LCEDLEVEL = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcEdLevel ");
    public static final DitaClass LEARNINGPLAN_LCFILESIZELIMITATIONS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcFileSizeLimitations ");
    public static final DitaClass LEARNINGPLAN_LCGAPANALYSIS = DitaClass.getInstance("- topic/section learningBase/section learningPlan/lcGapAnalysis ");
    public static final DitaClass LEARNINGPLAN_LCGAPITEM = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcGapItem ");
    public static final DitaClass LEARNINGPLAN_LCGAPITEMDELTA = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcGapItemDelta ");
    public static final DitaClass LEARNINGPLAN_LCGENERALDESCRIPTION = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcGeneralDescription ");
    public static final DitaClass LEARNINGPLAN_LCGOALS = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcGoals ");
    public static final DitaClass LEARNINGPLAN_LCGRAPHICS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcGraphics ");
    public static final DitaClass LEARNINGPLAN_LCHANDOUTS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcHandouts ");
    public static final DitaClass LEARNINGPLAN_LCINTERVENTION = DitaClass.getInstance("- topic/section learningBase/section learningPlan/lcIntervention ");
    public static final DitaClass LEARNINGPLAN_LCINTERVENTIONITEM = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcInterventionItem ");
    public static final DitaClass LEARNINGPLAN_LCJTAITEM = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcJtaItem ");
    public static final DitaClass LEARNINGPLAN_LCKNOWLEDGE = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcKnowledge ");
    public static final DitaClass LEARNINGPLAN_LCLEARNSTRAT = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcLearnStrat ");
    public static final DitaClass LEARNINGPLAN_LCLMS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcLMS ");
    public static final DitaClass LEARNINGPLAN_LCMODDATE = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcModDate ");
    public static final DitaClass LEARNINGPLAN_LCMOTIVATION = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcMotivation ");
    public static final DitaClass LEARNINGPLAN_LCNEEDS = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcNeeds ");
    public static final DitaClass LEARNINGPLAN_LCNEEDSANALYSIS = DitaClass.getInstance("- topic/section learningBase/section learningPlan/lcNeedsAnalysis ");
    public static final DitaClass LEARNINGPLAN_LCNOLMS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcNoLMS ");
    public static final DitaClass LEARNINGPLAN_LCOJT = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcOJT ");
    public static final DitaClass LEARNINGPLAN_LCORGANIZATIONAL = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcOrganizational ");
    public static final DitaClass LEARNINGPLAN_LCORGCONSTRAINTS = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcOrgConstraints ");
    public static final DitaClass LEARNINGPLAN_LCPLANAUDIENCE = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlanAudience ");
    public static final DitaClass LEARNINGPLAN_LCPLANDESCRIP = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlanDescrip ");
    public static final DitaClass LEARNINGPLAN_LCPLANOBJECTIVE = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcPlanObjective ");
    public static final DitaClass LEARNINGPLAN_LCPLANPREREQS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlanPrereqs ");
    public static final DitaClass LEARNINGPLAN_LCPLANRESOURCES = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcPlanResources ");
    public static final DitaClass LEARNINGPLAN_LCPLANSUBJECT = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlanSubject ");
    public static final DitaClass LEARNINGPLAN_LCPLANTITLE = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlanTitle ");
    public static final DitaClass LEARNINGPLAN_LCPLAYERS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcPlayers ");
    public static final DitaClass LEARNINGPLAN_LCPROCESSES = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcProcesses ");
    public static final DitaClass LEARNINGPLAN_LCPROJECT = DitaClass.getInstance("- topic/section learningBase/section learningPlan/lcProject ");
    public static final DitaClass LEARNINGPLAN_LCRESOLUTION = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcResolution ");
    public static final DitaClass LEARNINGPLAN_LCSECURITY = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcSecurity ");
    public static final DitaClass LEARNINGPLAN_LCSKILLS = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcSkills ");
    public static final DitaClass LEARNINGPLAN_LCSPECCHARS = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcSpecChars ");
    public static final DitaClass LEARNINGPLAN_LCTASK = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcTask ");
    public static final DitaClass LEARNINGPLAN_LCTASKITEM = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcTaskItem ");
    public static final DitaClass LEARNINGPLAN_LCTECHNICAL = DitaClass.getInstance("- topic/section learningBase/section learningPlan/lcTechnical ");
    public static final DitaClass LEARNINGPLAN_LCVALUES = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcValues ");
    public static final DitaClass LEARNINGPLAN_LCVIEWERS = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcViewers ");
    public static final DitaClass LEARNINGPLAN_LCW3C = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcW3C ");
    public static final DitaClass LEARNINGPLAN_LCWORKENV = DitaClass.getInstance("- topic/fig learningBase/fig learningPlan/lcWorkEnv ");
    public static final DitaClass LEARNINGPLAN_LCWORKENVDESCRIPTION = DitaClass.getInstance("- topic/p learningBase/p learningPlan/lcWorkEnvDescription ");
    public static final DitaClass LEARNINGPLAN_LEARNINGPLAN = DitaClass.getInstance("- topic/topic learningBase/learningBase learningPlan/learningPlan ");
    public static final DitaClass LEARNINGPLAN_LEARNINGPLANBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody learningPlan/learningPlanbody ");
    public static final DitaClass LEARNINGSUMMARY_LEARNINGSUMMARY = DitaClass.getInstance("- topic/topic learningBase/learningBase learningSummary/learningSummary ");
    public static final DitaClass LEARNINGSUMMARY_LEARNINGSUMMARYBODY = DitaClass.getInstance("- topic/body learningBase/learningBasebody learningSummary/learningSummarybody ");
    public static final DitaClass MAP_ANCHOR = DitaClass.getInstance("- map/anchor ");
    public static final DitaClass MAP_LINKTEXT = DitaClass.getInstance("- map/linktext ");
    public static final DitaClass MAP_MAP = DitaClass.getInstance("- map/map ");
    public static final DitaClass MAP_NAVREF = DitaClass.getInstance("- map/navref ");
    public static final DitaClass MAP_RELCELL = DitaClass.getInstance("- map/relcell ");
    public static final DitaClass MAP_RELCOLSPEC = DitaClass.getInstance("- map/relcolspec ");
    public static final DitaClass MAP_RELHEADER = DitaClass.getInstance("- map/relheader ");
    public static final DitaClass MAP_RELROW = DitaClass.getInstance("- map/relrow ");
    public static final DitaClass MAP_RELTABLE = DitaClass.getInstance("- map/reltable ");
    public static final DitaClass MAP_SEARCHTITLE = DitaClass.getInstance("- map/searchtitle ");
    public static final DitaClass MAP_SHORTDESC = DitaClass.getInstance("- map/shortdesc ");
    public static final DitaClass MAP_TOPICMETA = DitaClass.getInstance("- map/topicmeta ");
    public static final DitaClass MAP_TOPICREF = DitaClass.getInstance("- map/topicref ");
    public static final DitaClass MAP_UX_WINDOW = DitaClass.getInstance("- map/ux-window ");
    public static final DitaClass MAPGROUP_D_ANCHORREF = DitaClass.getInstance("+ map/topicref mapgroup-d/anchorref ");
    public static final DitaClass MAPGROUP_D_KEYDEF = DitaClass.getInstance("+ map/topicref mapgroup-d/keydef ");
    public static final DitaClass MAPGROUP_D_MAPREF = DitaClass.getInstance("+ map/topicref mapgroup-d/mapref ");
    public static final DitaClass MAPGROUP_D_TOPICGROUP = DitaClass.getInstance("+ map/topicref mapgroup-d/topicgroup ");
    public static final DitaClass MAPGROUP_D_TOPICHEAD = DitaClass.getInstance("+ map/topicref mapgroup-d/topichead ");
    public static final DitaClass MAPGROUP_D_TOPICSET = DitaClass.getInstance("+ map/topicref mapgroup-d/topicset ");
    public static final DitaClass MAPGROUP_D_TOPICSETREF = DitaClass.getInstance("+ map/topicref mapgroup-d/topicsetref ");
    public static final DitaClass MARKUP_D_MARKUPNAME = DitaClass.getInstance("+ topic/keyword markup-d/markupname ");
    public static final DitaClass MATHML_D_MATHML = DitaClass.getInstance("+ topic/foreign mathml-d/mathml ");
    public static final DitaClass MATHML_D_MATHMLREF = DitaClass.getInstance("+ topic/xref mathml-d/mathmlref ");
    public static final DitaClass PR_D_APINAME = DitaClass.getInstance("+ topic/keyword pr-d/apiname ");
    public static final DitaClass PR_D_CODEBLOCK = DitaClass.getInstance("+ topic/pre pr-d/codeblock ");
    public static final DitaClass PR_D_CODEPH = DitaClass.getInstance("+ topic/ph pr-d/codeph ");
    public static final DitaClass PR_D_CODEREF = DitaClass.getInstance("+ topic/xref pr-d/coderef ");
    public static final DitaClass PR_D_DELIM = DitaClass.getInstance("+ topic/ph pr-d/delim ");
    public static final DitaClass PR_D_FRAGMENT = DitaClass.getInstance("+ topic/figgroup pr-d/fragment ");
    public static final DitaClass PR_D_FRAGREF = DitaClass.getInstance("+ topic/xref pr-d/fragref ");
    public static final DitaClass PR_D_GROUPCHOICE = DitaClass.getInstance("+ topic/figgroup pr-d/groupchoice ");
    public static final DitaClass PR_D_GROUPCOMP = DitaClass.getInstance("+ topic/figgroup pr-d/groupcomp ");
    public static final DitaClass PR_D_GROUPSEQ = DitaClass.getInstance("+ topic/figgroup pr-d/groupseq ");
    public static final DitaClass PR_D_KWD = DitaClass.getInstance("+ topic/keyword pr-d/kwd ");
    public static final DitaClass PR_D_OPER = DitaClass.getInstance("+ topic/ph pr-d/oper ");
    public static final DitaClass PR_D_OPTION = DitaClass.getInstance("+ topic/keyword pr-d/option ");
    public static final DitaClass PR_D_PARML = DitaClass.getInstance("+ topic/dl pr-d/parml ");
    public static final DitaClass PR_D_PARMNAME = DitaClass.getInstance("+ topic/keyword pr-d/parmname ");
    public static final DitaClass PR_D_PD = DitaClass.getInstance("+ topic/dd pr-d/pd ");
    public static final DitaClass PR_D_PLENTRY = DitaClass.getInstance("+ topic/dlentry pr-d/plentry ");
    public static final DitaClass PR_D_PT = DitaClass.getInstance("+ topic/dt pr-d/pt ");
    public static final DitaClass PR_D_REPSEP = DitaClass.getInstance("+ topic/ph pr-d/repsep ");
    public static final DitaClass PR_D_SEP = DitaClass.getInstance("+ topic/ph pr-d/sep ");
    public static final DitaClass PR_D_SYNBLK = DitaClass.getInstance("+ topic/figgroup pr-d/synblk ");
    public static final DitaClass PR_D_SYNNOTE = DitaClass.getInstance("+ topic/fn pr-d/synnote ");
    public static final DitaClass PR_D_SYNNOTEREF = DitaClass.getInstance("+ topic/xref pr-d/synnoteref ");
    public static final DitaClass PR_D_SYNPH = DitaClass.getInstance("+ topic/ph pr-d/synph ");
    public static final DitaClass PR_D_SYNTAXDIAGRAM = DitaClass.getInstance("+ topic/fig pr-d/syntaxdiagram ");
    public static final DitaClass PR_D_VAR = DitaClass.getInstance("+ topic/ph pr-d/var ");
    public static final DitaClass REFERENCE_PROPDESC = DitaClass.getInstance("- topic/stentry reference/propdesc ");
    public static final DitaClass REFERENCE_PROPDESCHD = DitaClass.getInstance("- topic/stentry reference/propdeschd ");
    public static final DitaClass REFERENCE_PROPERTIES = DitaClass.getInstance("- topic/simpletable reference/properties ");
    public static final DitaClass REFERENCE_PROPERTY = DitaClass.getInstance("- topic/strow reference/property ");
    public static final DitaClass REFERENCE_PROPHEAD = DitaClass.getInstance("- topic/sthead reference/prophead ");
    public static final DitaClass REFERENCE_PROPTYPE = DitaClass.getInstance("- topic/stentry reference/proptype ");
    public static final DitaClass REFERENCE_PROPTYPEHD = DitaClass.getInstance("- topic/stentry reference/proptypehd ");
    public static final DitaClass REFERENCE_PROPVALUE = DitaClass.getInstance("- topic/stentry reference/propvalue ");
    public static final DitaClass REFERENCE_PROPVALUEHD = DitaClass.getInstance("- topic/stentry reference/propvaluehd ");
    public static final DitaClass REFERENCE_REFBODY = DitaClass.getInstance("- topic/body reference/refbody ");
    public static final DitaClass REFERENCE_REFBODYDIV = DitaClass.getInstance("- topic/bodydiv reference/refbodydiv ");
    public static final DitaClass REFERENCE_REFERENCE = DitaClass.getInstance("- topic/topic reference/reference ");
    public static final DitaClass REFERENCE_REFSYN = DitaClass.getInstance("- topic/section reference/refsyn ");
    public static final DitaClass RELMGMT_D_CHANGE_COMPLETED = DitaClass.getInstance("+ topic/data relmgmt-d/change-completed ");
    public static final DitaClass RELMGMT_D_CHANGE_HISTORYLIST = DitaClass.getInstance("+ topic/metadata relmgmt-d/change-historylist ");
    public static final DitaClass RELMGMT_D_CHANGE_ITEM = DitaClass.getInstance("+ topic/data relmgmt-d/change-item ");
    public static final DitaClass RELMGMT_D_CHANGE_ORGANIZATION = DitaClass.getInstance("+ topic/data relmgmt-d/change-organization ");
    public static final DitaClass RELMGMT_D_CHANGE_PERSON = DitaClass.getInstance("+ topic/data relmgmt-d/change-person ");
    public static final DitaClass RELMGMT_D_CHANGE_REQUEST_ID = DitaClass.getInstance("+ topic/data relmgmt-d/change-request-id ");
    public static final DitaClass RELMGMT_D_CHANGE_REQUEST_REFERENCE = DitaClass.getInstance("+ topic/data relmgmt-d/change-request-reference ");
    public static final DitaClass RELMGMT_D_CHANGE_REQUEST_SYSTEM = DitaClass.getInstance("+ topic/data relmgmt-d/change-request-system ");
    public static final DitaClass RELMGMT_D_CHANGE_REVISIONID = DitaClass.getInstance("+ topic/data relmgmt-d/change-revisionid ");
    public static final DitaClass RELMGMT_D_CHANGE_STARTED = DitaClass.getInstance("+ topic/data relmgmt-d/change-started ");
    public static final DitaClass RELMGMT_D_CHANGE_SUMMARY = DitaClass.getInstance("+ topic/data relmgmt-d/change-summary ");
    public static final DitaClass SUBJECTSCHEME_ATTRIBUTEDEF = DitaClass.getInstance("- topic/data subjectScheme/attributedef ");
    public static final DitaClass SUBJECTSCHEME_DEFAULTSUBJECT = DitaClass.getInstance("- map/topicref subjectScheme/defaultSubject ");
    public static final DitaClass SUBJECTSCHEME_ELEMENTDEF = DitaClass.getInstance("- topic/data subjectScheme/elementdef ");
    public static final DitaClass SUBJECTSCHEME_ENUMERATIONDEF = DitaClass.getInstance("- map/topicref subjectScheme/enumerationdef ");
    public static final DitaClass SUBJECTSCHEME_HASINSTANCE = DitaClass.getInstance("- map/topicref subjectScheme/hasInstance ");
    public static final DitaClass SUBJECTSCHEME_HASKIND = DitaClass.getInstance("- map/topicref subjectScheme/hasKind ");
    public static final DitaClass SUBJECTSCHEME_HASNARROWER = DitaClass.getInstance("- map/topicref subjectScheme/hasNarrower ");
    public static final DitaClass SUBJECTSCHEME_HASPART = DitaClass.getInstance("- map/topicref subjectScheme/hasPart ");
    public static final DitaClass SUBJECTSCHEME_HASRELATED = DitaClass.getInstance("- map/topicref subjectScheme/hasRelated ");
    public static final DitaClass SUBJECTSCHEME_RELATEDSUBJECTS = DitaClass.getInstance("- map/topicref subjectScheme/relatedSubjects ");
    public static final DitaClass SUBJECTSCHEME_SCHEMEREF = DitaClass.getInstance("- map/topicref subjectScheme/schemeref ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTDEF = DitaClass.getInstance("- map/topicref subjectScheme/subjectdef ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTHEAD = DitaClass.getInstance("- map/topicref subjectScheme/subjectHead ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTHEADMETA = DitaClass.getInstance("- map/topicmeta subjectScheme/subjectHeadMeta ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTREL = DitaClass.getInstance("- map/relrow subjectScheme/subjectRel ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTRELHEADER = DitaClass.getInstance("- map/relrow subjectScheme/subjectRelHeader ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTRELTABLE = DitaClass.getInstance("- map/reltable subjectScheme/subjectRelTable ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTROLE = DitaClass.getInstance("- map/relcell subjectScheme/subjectRole ");
    public static final DitaClass SUBJECTSCHEME_SUBJECTSCHEME = DitaClass.getInstance("- map/map subjectScheme/subjectScheme ");
    public static final DitaClass SVG_D_SVG_CONTAINER = DitaClass.getInstance("+ topic/foreign svg-d/svg-container ");
    public static final DitaClass SVG_D_SVGREF = DitaClass.getInstance("+ topic/xref svg-d/svgref ");
    public static final DitaClass SW_D_CMDNAME = DitaClass.getInstance("+ topic/keyword sw-d/cmdname ");
    public static final DitaClass SW_D_FILEPATH = DitaClass.getInstance("+ topic/ph sw-d/filepath ");
    public static final DitaClass SW_D_MSGBLOCK = DitaClass.getInstance("+ topic/pre sw-d/msgblock ");
    public static final DitaClass SW_D_MSGNUM = DitaClass.getInstance("+ topic/keyword sw-d/msgnum ");
    public static final DitaClass SW_D_MSGPH = DitaClass.getInstance("+ topic/ph sw-d/msgph ");
    public static final DitaClass SW_D_SYSTEMOUTPUT = DitaClass.getInstance("+ topic/ph sw-d/systemoutput ");
    public static final DitaClass SW_D_USERINPUT = DitaClass.getInstance("+ topic/ph sw-d/userinput ");
    public static final DitaClass SW_D_VARNAME = DitaClass.getInstance("+ topic/keyword sw-d/varname ");
    public static final DitaClass TASK_CHDESC = DitaClass.getInstance("- topic/stentry task/chdesc ");
    public static final DitaClass TASK_CHDESCHD = DitaClass.getInstance("- topic/stentry task/chdeschd ");
    public static final DitaClass TASK_CHHEAD = DitaClass.getInstance("- topic/sthead task/chhead ");
    public static final DitaClass TASK_CHOICE = DitaClass.getInstance("- topic/li task/choice ");
    public static final DitaClass TASK_CHOICES = DitaClass.getInstance("- topic/ul task/choices ");
    public static final DitaClass TASK_CHOICETABLE = DitaClass.getInstance("- topic/simpletable task/choicetable ");
    public static final DitaClass TASK_CHOPTION = DitaClass.getInstance("- topic/stentry task/choption ");
    public static final DitaClass TASK_CHOPTIONHD = DitaClass.getInstance("- topic/stentry task/choptionhd ");
    public static final DitaClass TASK_CHROW = DitaClass.getInstance("- topic/strow task/chrow ");
    public static final DitaClass TASK_CMD = DitaClass.getInstance("- topic/ph task/cmd ");
    public static final DitaClass TASK_CONTEXT = DitaClass.getInstance("- topic/section task/context ");
    public static final DitaClass TASK_INFO = DitaClass.getInstance("- topic/itemgroup task/info ");
    public static final DitaClass TASK_POSTREQ = DitaClass.getInstance("- topic/section task/postreq ");
    public static final DitaClass TASK_PREREQ = DitaClass.getInstance("- topic/section task/prereq ");
    public static final DitaClass TASK_RESULT = DitaClass.getInstance("- topic/section task/result ");
    public static final DitaClass TASK_STEP = DitaClass.getInstance("- topic/li task/step ");
    public static final DitaClass TASK_STEPRESULT = DitaClass.getInstance("- topic/itemgroup task/stepresult ");
    public static final DitaClass TASK_STEPS = DitaClass.getInstance("- topic/ol task/steps ");
    public static final DitaClass TASK_STEPS_INFORMAL = DitaClass.getInstance("- topic/section task/steps-informal ");
    public static final DitaClass TASK_STEPS_UNORDERED = DitaClass.getInstance("- topic/ul task/steps-unordered ");
    public static final DitaClass TASK_STEPSECTION = DitaClass.getInstance("- topic/li task/stepsection ");
    public static final DitaClass TASK_STEPTROUBLESHOOTING = DitaClass.getInstance("- topic/itemgroup task/steptroubleshooting ");
    public static final DitaClass TASK_STEPXMP = DitaClass.getInstance("- topic/itemgroup task/stepxmp ");
    public static final DitaClass TASK_SUBSTEP = DitaClass.getInstance("- topic/li task/substep ");
    public static final DitaClass TASK_SUBSTEPS = DitaClass.getInstance("- topic/ol task/substeps ");
    public static final DitaClass TASK_TASK = DitaClass.getInstance("- topic/topic task/task ");
    public static final DitaClass TASK_TASKBODY = DitaClass.getInstance("- topic/body task/taskbody ");
    public static final DitaClass TASK_TASKTROUBLESHOOTING = DitaClass.getInstance("- topic/section task/tasktroubleshooting ");
    public static final DitaClass TASK_TUTORIALINFO = DitaClass.getInstance("- topic/itemgroup task/tutorialinfo ");
    public static final DitaClass TASKREQ_D_CLOSEREQS = DitaClass.getInstance("+ topic/section task/postreq taskreq-d/closereqs ");
    public static final DitaClass TASKREQ_D_ESTTIME = DitaClass.getInstance("+ topic/li task/li taskreq-d/esttime ");
    public static final DitaClass TASKREQ_D_NOCONDS = DitaClass.getInstance("+ topic/li task/li taskreq-d/noconds ");
    public static final DitaClass TASKREQ_D_NOSAFETY = DitaClass.getInstance("+ topic/li task/li taskreq-d/nosafety ");
    public static final DitaClass TASKREQ_D_NOSPARES = DitaClass.getInstance("+ topic/data task/data taskreq-d/nospares ");
    public static final DitaClass TASKREQ_D_NOSUPEQ = DitaClass.getInstance("+ topic/data task/data taskreq-d/nosupeq ");
    public static final DitaClass TASKREQ_D_NOSUPPLY = DitaClass.getInstance("+ topic/data task/data taskreq-d/nosupply ");
    public static final DitaClass TASKREQ_D_PERSCAT = DitaClass.getInstance("+ topic/li task/li taskreq-d/perscat ");
    public static final DitaClass TASKREQ_D_PERSKILL = DitaClass.getInstance("+ topic/li task/li taskreq-d/perskill ");
    public static final DitaClass TASKREQ_D_PERSONNEL = DitaClass.getInstance("+ topic/li task/li taskreq-d/personnel ");
    public static final DitaClass TASKREQ_D_PRELREQS = DitaClass.getInstance("+ topic/section task/prereq taskreq-d/prelreqs ");
    public static final DitaClass TASKREQ_D_REQCOND = DitaClass.getInstance("+ topic/li task/li taskreq-d/reqcond ");
    public static final DitaClass TASKREQ_D_REQCONDS = DitaClass.getInstance("+ topic/ul task/ul taskreq-d/reqconds ");
    public static final DitaClass TASKREQ_D_REQCONTP = DitaClass.getInstance("+ topic/li task/li taskreq-d/reqcontp ");
    public static final DitaClass TASKREQ_D_REQPERS = DitaClass.getInstance("+ topic/ul task/ul taskreq-d/reqpers ");
    public static final DitaClass TASKREQ_D_SAFECOND = DitaClass.getInstance("+ topic/li task/li taskreq-d/safecond ");
    public static final DitaClass TASKREQ_D_SAFETY = DitaClass.getInstance("+ topic/ol task/ol taskreq-d/safety ");
    public static final DitaClass TASKREQ_D_SPARE = DitaClass.getInstance("+ topic/li task/li taskreq-d/spare ");
    public static final DitaClass TASKREQ_D_SPARES = DitaClass.getInstance("+ topic/p task/p taskreq-d/spares ");
    public static final DitaClass TASKREQ_D_SPARESLI = DitaClass.getInstance("+ topic/ul task/ul taskreq-d/sparesli ");
    public static final DitaClass TASKREQ_D_SUPEQLI = DitaClass.getInstance("+ topic/ul task/ul taskreq-d/supeqli ");
    public static final DitaClass TASKREQ_D_SUPEQUI = DitaClass.getInstance("+ topic/li task/li taskreq-d/supequi ");
    public static final DitaClass TASKREQ_D_SUPEQUIP = DitaClass.getInstance("+ topic/p task/p taskreq-d/supequip ");
    public static final DitaClass TASKREQ_D_SUPPLIES = DitaClass.getInstance("+ topic/p task/p taskreq-d/supplies ");
    public static final DitaClass TASKREQ_D_SUPPLY = DitaClass.getInstance("+ topic/li task/li taskreq-d/supply ");
    public static final DitaClass TASKREQ_D_SUPPLYLI = DitaClass.getInstance("+ topic/ul task/ul taskreq-d/supplyli ");
    public static final DitaClass TOPIC_ABSTRACT = DitaClass.getInstance("- topic/abstract ");
    public static final DitaClass TOPIC_ALT = DitaClass.getInstance("- topic/alt ");
    public static final DitaClass TOPIC_AUDIENCE = DitaClass.getInstance("- topic/audience ");
    public static final DitaClass TOPIC_AUTHOR = DitaClass.getInstance("- topic/author ");
    public static final DitaClass TOPIC_BODY = DitaClass.getInstance("- topic/body ");
    public static final DitaClass TOPIC_BODYDIV = DitaClass.getInstance("- topic/bodydiv ");
    public static final DitaClass TOPIC_BOOLEAN = DitaClass.getInstance("- topic/boolean ");
    public static final DitaClass TOPIC_BRAND = DitaClass.getInstance("- topic/brand ");
    public static final DitaClass TOPIC_CATEGORY = DitaClass.getInstance("- topic/category ");
    public static final DitaClass TOPIC_CITE = DitaClass.getInstance("- topic/cite ");
    public static final DitaClass TOPIC_COLSPEC = DitaClass.getInstance("- topic/colspec ");
    public static final DitaClass TOPIC_COMPONENT = DitaClass.getInstance("- topic/component ");
    public static final DitaClass TOPIC_COPYRHOLDER = DitaClass.getInstance("- topic/copyrholder ");
    public static final DitaClass TOPIC_COPYRIGHT = DitaClass.getInstance("- topic/copyright ");
    public static final DitaClass TOPIC_COPYRYEAR = DitaClass.getInstance("- topic/copyryear ");
    public static final DitaClass TOPIC_CREATED = DitaClass.getInstance("- topic/created ");
    public static final DitaClass TOPIC_CRITDATES = DitaClass.getInstance("- topic/critdates ");
    public static final DitaClass TOPIC_DATA = DitaClass.getInstance("- topic/data ");
    public static final DitaClass TOPIC_DATA_ABOUT = DitaClass.getInstance("- topic/data-about ");
    public static final DitaClass TOPIC_DD = DitaClass.getInstance("- topic/dd ");
    public static final DitaClass TOPIC_DDHD = DitaClass.getInstance("- topic/ddhd ");
    public static final DitaClass TOPIC_DESC = DitaClass.getInstance("- topic/desc ");
    public static final DitaClass TOPIC_DIV = DitaClass.getInstance("- topic/div ");
    public static final DitaClass TOPIC_DL = DitaClass.getInstance("- topic/dl ");
    public static final DitaClass TOPIC_DLENTRY = DitaClass.getInstance("- topic/dlentry ");
    public static final DitaClass TOPIC_DLHEAD = DitaClass.getInstance("- topic/dlhead ");
    public static final DitaClass TOPIC_DRAFT_COMMENT = DitaClass.getInstance("- topic/draft-comment ");
    public static final DitaClass TOPIC_DT = DitaClass.getInstance("- topic/dt ");
    public static final DitaClass TOPIC_DTHD = DitaClass.getInstance("- topic/dthd ");
    public static final DitaClass TOPIC_ENTRY = DitaClass.getInstance("- topic/entry ");
    public static final DitaClass TOPIC_EXAMPLE = DitaClass.getInstance("- topic/example ");
    public static final DitaClass TOPIC_FALLBACK = DitaClass.getInstance("- topic/fallback ");
    public static final DitaClass TOPIC_FEATNUM = DitaClass.getInstance("- topic/featnum ");
    public static final DitaClass TOPIC_FIG = DitaClass.getInstance("- topic/fig ");
    public static final DitaClass TOPIC_FIGGROUP = DitaClass.getInstance("- topic/figgroup ");
    public static final DitaClass TOPIC_FN = DitaClass.getInstance("- topic/fn ");
    public static final DitaClass TOPIC_FOREIGN = DitaClass.getInstance("- topic/foreign ");
    public static final DitaClass TOPIC_IMAGE = DitaClass.getInstance("- topic/image ");
    public static final DitaClass TOPIC_INCLUDE = DitaClass.getInstance("- topic/include ");
    public static final DitaClass TOPIC_INDEX_BASE = DitaClass.getInstance("- topic/index-base ");
    public static final DitaClass TOPIC_INDEXTERM = DitaClass.getInstance("- topic/indexterm ");
    public static final DitaClass TOPIC_INDEXTERMREF = DitaClass.getInstance("- topic/indextermref ");
    public static final DitaClass TOPIC_ITEMGROUP = DitaClass.getInstance("- topic/itemgroup ");
    public static final DitaClass TOPIC_KEYWORD = DitaClass.getInstance("- topic/keyword ");
    public static final DitaClass TOPIC_KEYWORDS = DitaClass.getInstance("- topic/keywords ");
    public static final DitaClass TOPIC_LI = DitaClass.getInstance("- topic/li ");
    public static final DitaClass TOPIC_LINES = DitaClass.getInstance("- topic/lines ");
    public static final DitaClass TOPIC_LINK = DitaClass.getInstance("- topic/link ");
    public static final DitaClass TOPIC_LINKINFO = DitaClass.getInstance("- topic/linkinfo ");
    public static final DitaClass TOPIC_LINKLIST = DitaClass.getInstance("- topic/linklist ");
    public static final DitaClass TOPIC_LINKPOOL = DitaClass.getInstance("- topic/linkpool ");
    public static final DitaClass TOPIC_LINKTEXT = DitaClass.getInstance("- topic/linktext ");
    public static final DitaClass TOPIC_LONGDESCREF = DitaClass.getInstance("- topic/longdescref ");
    public static final DitaClass TOPIC_LONGQUOTEREF = DitaClass.getInstance("- topic/longquoteref ");
    public static final DitaClass TOPIC_LQ = DitaClass.getInstance("- topic/lq ");
    public static final DitaClass TOPIC_METADATA = DitaClass.getInstance("- topic/metadata ");
    public static final DitaClass TOPIC_NAVTITLE = DitaClass.getInstance("- topic/navtitle ");
    public static final DitaClass TOPIC_NO_TOPIC_NESTING = DitaClass.getInstance("- topic/no-topic-nesting ");
    public static final DitaClass TOPIC_NOTE = DitaClass.getInstance("- topic/note ");
    public static final DitaClass TOPIC_OBJECT = DitaClass.getInstance("- topic/object ");
    public static final DitaClass TOPIC_OL = DitaClass.getInstance("- topic/ol ");
    public static final DitaClass TOPIC_OTHERMETA = DitaClass.getInstance("- topic/othermeta ");
    public static final DitaClass TOPIC_P = DitaClass.getInstance("- topic/p ");
    public static final DitaClass TOPIC_PARAM = DitaClass.getInstance("- topic/param ");
    public static final DitaClass TOPIC_PERMISSIONS = DitaClass.getInstance("- topic/permissions ");
    public static final DitaClass TOPIC_PH = DitaClass.getInstance("- topic/ph ");
    public static final DitaClass TOPIC_PLATFORM = DitaClass.getInstance("- topic/platform ");
    public static final DitaClass TOPIC_PRE = DitaClass.getInstance("- topic/pre ");
    public static final DitaClass TOPIC_PRODINFO = DitaClass.getInstance("- topic/prodinfo ");
    public static final DitaClass TOPIC_PRODNAME = DitaClass.getInstance("- topic/prodname ");
    public static final DitaClass TOPIC_PROGNUM = DitaClass.getInstance("- topic/prognum ");
    public static final DitaClass TOPIC_PROLOG = DitaClass.getInstance("- topic/prolog ");
    public static final DitaClass TOPIC_PUBLISHER = DitaClass.getInstance("- topic/publisher ");
    public static final DitaClass TOPIC_Q = DitaClass.getInstance("- topic/q ");
    public static final DitaClass TOPIC_RELATED_LINKS = DitaClass.getInstance("- topic/related-links ");
    public static final DitaClass TOPIC_REQUIRED_CLEANUP = DitaClass.getInstance("- topic/required-cleanup ");
    public static final DitaClass TOPIC_RESOURCEID = DitaClass.getInstance("- topic/resourceid ");
    public static final DitaClass TOPIC_REVISED = DitaClass.getInstance("- topic/revised ");
    public static final DitaClass TOPIC_ROW = DitaClass.getInstance("- topic/row ");
    public static final DitaClass TOPIC_SEARCHTITLE = DitaClass.getInstance("- topic/searchtitle ");
    public static final DitaClass TOPIC_SECTION = DitaClass.getInstance("- topic/section ");
    public static final DitaClass TOPIC_SECTIONDIV = DitaClass.getInstance("- topic/sectiondiv ");
    public static final DitaClass TOPIC_SERIES = DitaClass.getInstance("- topic/series ");
    public static final DitaClass TOPIC_SHORTDESC = DitaClass.getInstance("- topic/shortdesc ");
    public static final DitaClass TOPIC_SIMPLETABLE = DitaClass.getInstance("- topic/simpletable ");
    public static final DitaClass TOPIC_SL = DitaClass.getInstance("- topic/sl ");
    public static final DitaClass TOPIC_SLI = DitaClass.getInstance("- topic/sli ");
    public static final DitaClass TOPIC_SOURCE = DitaClass.getInstance("- topic/source ");
    public static final DitaClass TOPIC_STATE = DitaClass.getInstance("- topic/state ");
    public static final DitaClass TOPIC_STENTRY = DitaClass.getInstance("- topic/stentry ");
    public static final DitaClass TOPIC_STHEAD = DitaClass.getInstance("- topic/sthead ");
    public static final DitaClass TOPIC_STROW = DitaClass.getInstance("- topic/strow ");
    public static final DitaClass TOPIC_TABLE = DitaClass.getInstance("- topic/table ");
    public static final DitaClass TOPIC_TBODY = DitaClass.getInstance("- topic/tbody ");
    public static final DitaClass TOPIC_TERM = DitaClass.getInstance("- topic/term ");
    public static final DitaClass TOPIC_TEXT = DitaClass.getInstance("- topic/text ");
    public static final DitaClass TOPIC_TGROUP = DitaClass.getInstance("- topic/tgroup ");
    public static final DitaClass TOPIC_THEAD = DitaClass.getInstance("- topic/thead ");
    public static final DitaClass TOPIC_TITLE = DitaClass.getInstance("- topic/title ");
    public static final DitaClass TOPIC_TITLEALTS = DitaClass.getInstance("- topic/titlealts ");
    public static final DitaClass TOPIC_TM = DitaClass.getInstance("- topic/tm ");
    public static final DitaClass TOPIC_TOPIC = DitaClass.getInstance("- topic/topic ");
    public static final DitaClass TOPIC_UL = DitaClass.getInstance("- topic/ul ");
    public static final DitaClass TOPIC_UNKNOWN = DitaClass.getInstance("- topic/unknown ");
    public static final DitaClass TOPIC_VRM = DitaClass.getInstance("- topic/vrm ");
    public static final DitaClass TOPIC_VRMLIST = DitaClass.getInstance("- topic/vrmlist ");
    public static final DitaClass TOPIC_XREF = DitaClass.getInstance("- topic/xref ");
    public static final DitaClass TROUBLESHOOTING_CAUSE = DitaClass.getInstance("- topic/section troubleshooting/cause ");
    public static final DitaClass TROUBLESHOOTING_CONDITION = DitaClass.getInstance("- topic/section troubleshooting/condition ");
    public static final DitaClass TROUBLESHOOTING_REMEDY = DitaClass.getInstance("- topic/section troubleshooting/remedy ");
    public static final DitaClass TROUBLESHOOTING_RESPONSIBLEPARTY = DitaClass.getInstance("- topic/p troubleshooting/responsibleParty ");
    public static final DitaClass TROUBLESHOOTING_TROUBLEBODY = DitaClass.getInstance("- topic/body troubleshooting/troublebody ");
    public static final DitaClass TROUBLESHOOTING_TROUBLESHOOTING = DitaClass.getInstance("- topic/topic troubleshooting/troubleshooting ");
    public static final DitaClass TROUBLESHOOTING_TROUBLESOLUTION = DitaClass.getInstance("- topic/bodydiv troubleshooting/troubleSolution ");
    public static final DitaClass UI_D_MENUCASCADE = DitaClass.getInstance("+ topic/ph ui-d/menucascade ");
    public static final DitaClass UI_D_SCREEN = DitaClass.getInstance("+ topic/pre ui-d/screen ");
    public static final DitaClass UI_D_SHORTCUT = DitaClass.getInstance("+ topic/keyword ui-d/shortcut ");
    public static final DitaClass UI_D_UICONTROL = DitaClass.getInstance("+ topic/ph ui-d/uicontrol ");
    public static final DitaClass UI_D_WINTITLE = DitaClass.getInstance("+ topic/keyword ui-d/wintitle ");
    public static final DitaClass UT_D_AREA = DitaClass.getInstance("+ topic/figgroup ut-d/area ");
    public static final DitaClass UT_D_COORDS = DitaClass.getInstance("+ topic/ph ut-d/coords ");
    public static final DitaClass UT_D_IMAGEMAP = DitaClass.getInstance("+ topic/fig ut-d/imagemap ");
    public static final DitaClass UT_D_SHAPE = DitaClass.getInstance("+ topic/keyword ut-d/shape ");
    public static final DitaClass UT_D_SORT_AS = DitaClass.getInstance("+ topic/data ut-d/sort-as ");
    public static final DitaClass XML_D_NUMCHARREF = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/numcharref ");
    public static final DitaClass XML_D_PARAMETERENTITY = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/parameterentity ");
    public static final DitaClass XML_D_TEXTENTITY = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/textentity ");
    public static final DitaClass XML_D_XMLATT = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/xmlatt ");
    public static final DitaClass XML_D_XMLELEMENT = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/xmlelement ");
    public static final DitaClass XML_D_XMLNSNAME = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/xmlnsname ");
    public static final DitaClass XML_D_XMLPI = DitaClass.getInstance("+ topic/keyword markup-d/markupname xml-d/xmlpi ");
    public static final DitaClass XNAL_D_ADDRESSDETAILS = DitaClass.getInstance("+ topic/ph xnal-d/addressdetails ");
    public static final DitaClass XNAL_D_ADMINISTRATIVEAREA = DitaClass.getInstance("+ topic/ph xnal-d/administrativearea ");
    public static final DitaClass XNAL_D_AUTHORINFORMATION = DitaClass.getInstance("+ topic/author xnal-d/authorinformation ");
    public static final DitaClass XNAL_D_CONTACTNUMBER = DitaClass.getInstance("+ topic/data xnal-d/contactnumber ");
    public static final DitaClass XNAL_D_CONTACTNUMBERS = DitaClass.getInstance("+ topic/data xnal-d/contactnumbers ");
    public static final DitaClass XNAL_D_COUNTRY = DitaClass.getInstance("+ topic/ph xnal-d/country ");
    public static final DitaClass XNAL_D_EMAILADDRESS = DitaClass.getInstance("+ topic/data xnal-d/emailaddress ");
    public static final DitaClass XNAL_D_EMAILADDRESSES = DitaClass.getInstance("+ topic/data xnal-d/emailaddresses ");
    public static final DitaClass XNAL_D_FIRSTNAME = DitaClass.getInstance("+ topic/data xnal-d/firstname ");
    public static final DitaClass XNAL_D_GENERATIONIDENTIFIER = DitaClass.getInstance("+ topic/data xnal-d/generationidentifier ");
    public static final DitaClass XNAL_D_HONORIFIC = DitaClass.getInstance("+ topic/data xnal-d/honorific ");
    public static final DitaClass XNAL_D_LASTNAME = DitaClass.getInstance("+ topic/data xnal-d/lastname ");
    public static final DitaClass XNAL_D_LOCALITY = DitaClass.getInstance("+ topic/ph xnal-d/locality ");
    public static final DitaClass XNAL_D_LOCALITYNAME = DitaClass.getInstance("+ topic/ph xnal-d/localityname ");
    public static final DitaClass XNAL_D_MIDDLENAME = DitaClass.getInstance("+ topic/data xnal-d/middlename ");
    public static final DitaClass XNAL_D_NAMEDETAILS = DitaClass.getInstance("+ topic/data xnal-d/namedetails ");
    public static final DitaClass XNAL_D_ORGANIZATIONINFO = DitaClass.getInstance("+ topic/data xnal-d/organizationinfo ");
    public static final DitaClass XNAL_D_ORGANIZATIONNAME = DitaClass.getInstance("+ topic/ph xnal-d/organizationname ");
    public static final DitaClass XNAL_D_ORGANIZATIONNAMEDETAILS = DitaClass.getInstance("+ topic/ph xnal-d/organizationnamedetails ");
    public static final DitaClass XNAL_D_OTHERINFO = DitaClass.getInstance("+ topic/data xnal-d/otherinfo ");
    public static final DitaClass XNAL_D_PERSONINFO = DitaClass.getInstance("+ topic/data xnal-d/personinfo ");
    public static final DitaClass XNAL_D_PERSONNAME = DitaClass.getInstance("+ topic/data xnal-d/personname ");
    public static final DitaClass XNAL_D_POSTALCODE = DitaClass.getInstance("+ topic/ph xnal-d/postalcode ");
    public static final DitaClass XNAL_D_THOROUGHFARE = DitaClass.getInstance("+ topic/ph xnal-d/thoroughfare ");
    public static final DitaClass XNAL_D_URL = DitaClass.getInstance("+ topic/data xnal-d/url ");
    public static final DitaClass XNAL_D_URLS = DitaClass.getInstance("+ topic/data xnal-d/urls ");

    public static final DitaClass SUBMAP = DitaClass.getInstance("+ map/topicref mapgroup-d/topicgroup ditaot-d/submap ");
    public static final DitaClass DITA_OT_D_KEYDEF = DitaClass.getInstance("+ map/topicref mapgroup-d/keydef ditaot-d/keydef ");
    public static final DitaClass DITA_OT_D_DITAVAL_STARTPROP = DitaClass.getInstance("+ topic/foreign ditaot-d/ditaval-startprop ");
    public static final DitaClass DITA_OT_D_DITAVAL_ENDPROP = DitaClass.getInstance("+ topic/foreign ditaot-d/ditaval-endprop ");

    /**maplinks element.*/
    public static final String ELEMENT_NAME_MAPLINKS = "maplinks";
    /**prop element.*/
    public static final String ELEMENT_NAME_PROP = "prop";
    public static final String ELEMENT_NAME_REVPROP = "revprop";
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
    /**locktitle attribute.*/
    public static final String ATTRIBUTE_NAME_LOCKTITLE = "locktitle";
    /**locktitle="yes" value.*/
    public static final String ATTRIBUTE_NAME_LOCKTITLE_VALUE_YES = "yes";
    /**format attribute.*/
    public static final String ATTRIBUTE_NAME_FORMAT = "format";
    public static final String ATTRIBUTE_NAME_ENCODING = "encoding";
    public static final String ATTRIBUTE_NAME_PARSE = "parse";
    /**charset attribute.*/
    public static final String ATTRIBUTE_NAME_CHARSET = "charset";
    /**lang attribute.*/
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
    public static final String ATTRIBUTE_NAME_SPECIALIZATIONS = "specializations";
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
    public static final String ATTRIBUTE_NAME_OUTPUTCLASS = "outputclass";
    /**scope attribute.*/
    public static final String ATTRIBUTE_NAME_REV = "rev";
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
    /**generated imagerefuri attribute.*/
    public static final String ATTRIBUTE_NAME_IMAGEREF_URI = "imagerefuri";
    /**start attribute.*/
    public static final String ATTRIBUTE_NAME_START = "start";
    /**conref attribute.*/
    public static final String ATTRIBUTE_NAME_END = "end";
    /**conaction attribute.*/
    public static final String ATTRIBUTE_NAME_CONACTION = "conaction";
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
    public static final String ATTRIBUTE_NAME_COLOR = "color";
    public static final String ATTRIBUTE_NAME_BACKCOLOR = "backcolor";
    public static final String ATTRIBUTE_NAME_STYLE = "style";
    public static final String ATTRIBUTE_NAME_CHANGEBAR = "changebar";

    public static final String ATTRIBUTE_VALUETYPE_VALUE_REF = "ref";

    public static final String ATTRIBUTE_CASCADE_VALUE_MERGE = "merge";
    public static final String ATTRIBUTE_CASCADE_VALUE_NOMERGE = "nomerge";

    /** URI path separator. */
    public static final String URI_SEPARATOR = "/";
    /** UNIX path separator. */
    public static final String UNIX_SEPARATOR = "/";
    /** Windows path separator. */
    public static final String WINDOWS_SEPARATOR = "\\";

    /** Constants for index type(htmlhelp). Deprecated since 3.0 */
    @Deprecated
    public static final String INDEX_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for index type(eclipsehelp).*/
    public static final String INDEX_TYPE_ECLIPSEHELP = "eclipsehelp";

    /** Constants for transform type(pdf). Deprecated since 3.0 */
    @Deprecated
    public static final String TRANS_TYPE_PDF = "pdf";
    /** Constants for transform type(xhtml). Deprecated since 3.0 */
    @Deprecated
    public static final String TRANS_TYPE_XHTML = "xhtml";
    /** Constants for transform type(eclipsehelp). Deprecated since 3.0 */
    @Deprecated
    public static final String TRANS_TYPE_ECLIPSEHELP = "eclipsehelp";
    /** Constants for transform type(htmlhelp). Deprecated since 3.0 */
    @Deprecated
    public static final String TRANS_TYPE_HTMLHELP = "htmlhelp";
    /** Constants for transform type(eclipsecontent). Deprecated since 3.0 */
    @Deprecated
    public static final String TRANS_TYPE_ECLIPSECONTENT = "eclipsecontent";

    /** Constant for generated property file name(catalog-dita.xml).*/
    public static final String FILE_NAME_CATALOG = "catalog-dita.xml";
    //store the scheme files refered by a scheme file in the form of Map<String Set<String>>
    /** Constant for generated property file name(subrelation.xml).*/
    public static final String FILE_NAME_SUBJECT_RELATION = "subrelation.xml";
    /** Constant for generated DITAVAL file name(ditaot.generated.ditaval).*/
    public static final String FILE_NAME_MERGED_DITAVAL = "ditaot.generated.ditaval";

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
    /** Property name for copy-to target2sourcemap list file. Deprecated since 2.3 */
    @Deprecated
    public static final String COPYTO_TARGET_TO_SOURCE_MAP_LIST = "copytotarget2sourcemaplist";
    /** Property name for relflag image list file */
    public static final String REL_FLAGIMAGE_LIST = "relflagimagelist";

    /**Constants for common params used in ant invoker(tempDir).*/
    public static final String ANT_INVOKER_PARAM_TEMPDIR = "tempDir";
    /**Constants for common params used in ant invoker(basedir).*/
    public static final String ANT_INVOKER_PARAM_BASEDIR = "basedir";
    /**Constants for common params used in ant invoker(inputmap).*/
    public static final String ANT_INVOKER_PARAM_INPUTMAP = "inputmap";
    public static final String ANT_INVOKER_PARAM_RESOURCES = "resources";
    /**Constants for common params used in ant invoker(ditaval).*/
    public static final String ANT_INVOKER_PARAM_DITAVAL = "ditaval";
    /**Constants for common params used in ant invoker(mergedditaval)*/
    public static final String ANT_INVOKER_PARAM_MERGEDDITAVAL = "mergedditaval";
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
    public static final String ANT_INVOKER_EXT_PARAM_OUTTERCONTROL = "outercontrol";
    /**Constants for extensive params used in ant invoker(generatecopyouter).*/
    public static final String ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER = "generatecopyouter";
    /**Constants for extensive params used in ant invoker(onlytopicinmap).*/
    public static final String ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP = "onlytopicinmap";
    /**Constants for extensive params used in ant invoker(crawl).*/
    public static final String ANT_INVOKER_EXT_PARAM_CRAWL = "crawl";
    public static final String ANT_INVOKER_EXT_PARAM_CRAWL_VALUE_MAP = "map";
    public static final String ANT_INVOKER_EXT_PARAM_CRAWL_VALUE_TOPIC = "topic";
    /**Constants for extensive params used in ant invoker(validate).*/
    public static final String ANT_INVOKER_EXT_PARAM_VALIDATE = "validate";
    /**Constants for extensive params used in ant invoker(outputdir).*/
    public static final String ANT_INVOKER_EXT_PARAM_OUTPUTDIR = "outputdir";
    /**Constants for extensive params used in ant invoker(gramcache).*/
    public static final String ANT_INVOKER_EXT_PARAM_GRAMCACHE = "gramcache";
    public static final String ANT_INVOKER_EXT_PARAN_SETSYSTEMID = "setsystemid";
    public static final String ANT_INVOKER_EXT_PARAN_FORCE_UNIQUE = "force-unique";
    public static final String ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR = "generate-debug-attributes";
    public static final String ANT_INVOKER_EXT_PARAM_PROCESSING_MODE = "processing-mode";
    /**Constants for line separator.*/
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**OS relevant constants(OS NAME).*/
    public static final String OS_NAME = System.getProperty("os.name");
    /**OS relevant constants(windows).*/
    public static final String OS_NAME_WINDOWS = "windows";

    //Misc string constants used in this toolkit.

    /**STRING_EMPTY. Deprecated since 3.0 */
    @Deprecated
    public static final String STRING_EMPTY = "";
    /**LEFT_BRACKET.*/
    public static final String LEFT_BRACKET = "(";
    /**RIGHT_BRACKET.*/
    public static final String RIGHT_BRACKET = ")";
    /**SLASH.*/
    public static final String SLASH = "/";
    /**BACK_SLASH. Deprecated since 3.0 */
    @Deprecated
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
    /**DOUBLE_BACK_SLASH. Deprecated since 3.0 */
    @Deprecated
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
    /**TEMP_DIR_DEFAULT. Deprecated since 3.0 */
    @Deprecated
    public static final String TEMP_DIR_DEFAULT = "temp";
    /**FILTER_ACTION_EXCLUDE. Deprecated since 3.0 */
    @Deprecated
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
    /** ATTR_FORMAT_VALUE_NONDITA = format unknown, but not DITA **/
    public static final String ATTR_FORMAT_VALUE_NONDITA = "nondita";
    /**ATTRIBUTE_NAME_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_NAME_DITAARCHVERSION = "DITAArchVersion";
    /**ATTRIBUTE_PREFIX_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_PREFIX_DITAARCHVERSION = "ditaarch";
    /**ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION.*/
    public static final String ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION = XMLNS_ATTRIBUTE + ":" + ATTRIBUTE_PREFIX_DITAARCHVERSION;
    public static final String DITA_NAMESPACE = "http://dita.oasis-open.org/architecture/2005/";
    public static final String DITA_OT_NS_PREFIX = "dita-ot";
    public static final String DITA_OT_NAMESPACE = "http://dita-ot.sourceforge.net";
    public static final String DITA_OT_NS = "http://dita-ot.sourceforge.net/ns/201007/dita-ot";
    /**ATTRIBUTE_NAMESPACE_PREFIX_XSI.*/
    public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSI_NS_PREFIX = "xsi";
    public static final String ATTRIBUTE_NAMESPACE_PREFIX_XSI = XMLNS_ATTRIBUTE + ":" + XSI_NS_PREFIX;
    public static final String ATTRIBUTE_NAME_NONAMESPACESCHEMALOCATION = "xsi:noNamespaceSchemaLocation";
    /**dita-ot:orig-href.*/
    public static final String ATTRIBUTE_NAME_DITA_OT_ORIG_HREF = DITA_OT_NS_PREFIX + ":" + "orig-href";


    /**ATTR_CLASS_VALUE_SUBJECT_SCHEME_BASE. Deprecated since 3.0 */
    @Deprecated
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
    
    /** Standard token for attribute to be ignored due to conref */
    public static final String ATTR_VALUE_DITA_USE_CONREF_TARGET = "-dita-use-conref-target";

    /** constants for filtering or flagging. */
    public static final String DEFAULT_ACTION = "default";
    /**chunk attribute.*/
    public static final String ATTRIBUTE_NAME_CHUNK = "chunk";
    
    /** constants for args.draft argument value. **/
    public static final String ARGS_DRAFT_YES = "yes";
    public static final String ARGS_DRAFT_NO = "no";

    /**constants for indexterm prefix(See).
     *
     * @deprecated since 3.2
     **/
    @Deprecated
    public static final String IndexTerm_Prefix_See = "See";
    /**constants for indexterm prefix(See also).
     *
     * @deprecated since 3.2
     **/
    @Deprecated
    public static final String IndexTerm_Prefix_See_Also = "See also";
    /**name attribute.*/
    public static final String ATTRIBUTE_NAME_NAME = "name";
    /**type attribute value subjectScheme.*/
    public static final String ATTR_TYPE_VALUE_SUBJECT_SCHEME = "subjectScheme";
    /**store how many scheme files a ditamap file used in form of {@code Map&lt;String, Set&lt;String>>}.*/
    public static final String FILE_NAME_SUBJECT_DICTIONARY = "subject_scheme.dictionary";
    /**export.xml to store exported elements.*/
    public static final String FILE_NAME_EXPORT_XML = "export.xml";
    /**pluginId.xml to store the plugin id.*/
    public static final String FILE_NAME_PLUGIN_XML = "pluginId.xml";

    /** Application configuration filename. */
    public static final String APP_CONF_PROPERTIES = "application.properties";
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
    public static final String CONF_TRANSTYPES = "transtypes";
    /** Property name for template files. */
    public static final String CONF_TEMPLATES = "templates";
    /** Plugin configuration file name. */
    public static final String PLUGIN_CONF = "plugins.xml";

    /** Project reference name for job configuration object. */
    public static final String ANT_REFERENCE_JOB = "job";
    /** Project reference name for XML utils object. */
    public static final String ANT_REFERENCE_XML_UTILS = "xmlutils";
    public static final String ANT_REFERENCE_STORE = "store";
    /** Temporary directory Ant property name. */
    public static final String ANT_TEMP_DIR = "dita.temp.dir";

    /** OASIS catalog file namespace. */
    public static final String OASIS_CATALOG_NAMESPACE = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
    
    /** Deprecated since 2.3 */
    @Deprecated
    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_PATH2PROJ_TARGET_URI = "path2project-uri";
    public static final String PI_PATH2ROOTMAP_TARGET_URI = "path2rootmap-uri";
    /** Deprecated since 2.3 */
    @Deprecated
    public static final String PI_WORKDIR_TARGET = "workdir";
    public static final String PI_WORKDIR_TARGET_URI = "workdir-uri";

    /**
     * Instances should NOT be constructed in standard programming.
     */
    private Constants() {
    }
}
