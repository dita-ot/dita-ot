package org.dita.dost.platform;

import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StylesheetOptimizerFilter extends AbstractXMLFilter {

    public static final String OPTIMIZED_XSL_EXTENSION = ".optimized.xsl";
    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";

    final static List<Entry<Pattern, String>> ps;
    static {
        ps = Arrays.asList(
                compile("abstract", "glossentry/glossdef"),
                compile("author", "xnal-d/authorinformation"),
                compile("body", "concept/conbody", "glossentry/glossBody", "learningAssessment/learningAssessmentbody", "learningBase/learningBasebody", "learningContent/learningContentbody", "learningOverview/learningOverviewbody", "learningPlan/learningPlanbody", "learningSummary/learningSummarybody", "reference/refbody", "task/taskbody", "troubleshooting/troublebody"),
                compile("bodydiv", "concept/conbodydiv", "reference/refbodydiv", "troubleshooting/troubleSolution"),
                compile("data", "bookmap/approved", "bookmap/bookchangehistory", "bookmap/bookevent", "bookmap/bookeventtype", "bookmap/bookid", "bookmap/booknumber", "bookmap/bookowner", "bookmap/bookpartno", "bookmap/bookrestriction", "bookmap/bookrights", "bookmap/copyrfirst", "bookmap/copyrlast", "bookmap/edited", "bookmap/edition", "bookmap/isbn", "bookmap/maintainer", "bookmap/organization", "bookmap/person", "bookmap/printlocation", "bookmap/published", "bookmap/publishtype", "bookmap/reviewed", "bookmap/tested", "bookmap/volume", "ditavalref-d/dvrKeyscopePrefix", "ditavalref-d/dvrKeyscopeSuffix", "ditavalref-d/dvrResourcePrefix", "ditavalref-d/dvrResourceSuffix", "glossentry/glossPartOfSpeech", "glossentry/glossProperty", "glossentry/glossStatus", "learning-d/lcCorrectResponse", "learning-d/lcSequence", "learning2-d/lcCorrectResponse2", "learning2-d/lcSequence2", "learningBase/lcTime", "learningmeta-d/lomAggregationLevel", "learningmeta-d/lomContext", "learningmeta-d/lomCoverage", "learningmeta-d/lomDifficulty", "learningmeta-d/lomInstallationRemarks", "learningmeta-d/lomIntendedUserRole", "learningmeta-d/lomInteractivityLevel", "learningmeta-d/lomInteractivityType", "learningmeta-d/lomLearningResourceType", "learningmeta-d/lomOtherPlatformRequirements", "learningmeta-d/lomSemanticDensity", "learningmeta-d/lomStructure", "learningmeta-d/lomTechRequirement", "learningmeta-d/lomTypicalAgeRange", "learningmeta-d/lomTypicalLearningTime", "relmgmt-d/change-completed", "relmgmt-d/change-item", "relmgmt-d/change-organization", "relmgmt-d/change-person", "relmgmt-d/change-request-id", "relmgmt-d/change-request-reference", "relmgmt-d/change-request-system", "relmgmt-d/change-revisionid", "relmgmt-d/change-started", "relmgmt-d/change-summary", "subjectScheme/attributedef", "subjectScheme/elementdef", "taskreq-d/nospares", "taskreq-d/nosupeq", "taskreq-d/nosupply", "ut-d/sort-as", "xnal-d/contactnumber", "xnal-d/contactnumbers", "xnal-d/emailaddress", "xnal-d/emailaddresses", "xnal-d/firstname", "xnal-d/generationidentifier", "xnal-d/honorific", "xnal-d/lastname", "xnal-d/middlename", "xnal-d/namedetails", "xnal-d/organizationinfo", "xnal-d/otherinfo", "xnal-d/personinfo", "xnal-d/personname", "xnal-d/url", "xnal-d/urls"),
                compile("dd", "pr-d/pd"),
                compile("div", "equation-d/equation-block", "learning2-d/lcAnswerContent2", "learning2-d/lcAsset2", "learning2-d/lcFeedback2", "learning2-d/lcFeedbackCorrect2", "learning2-d/lcFeedbackIncorrect2", "learning2-d/lcHotspot2", "learning2-d/lcMatching2", "learning2-d/lcMultipleSelect2", "learning2-d/lcOpenAnswer2", "learning2-d/lcOpenQuestion2", "learning2-d/lcQuestion2", "learning2-d/lcSequencing2", "learning2-d/lcSingleSelect2", "learning2-d/lcTrueFalse2", "learningInteractionBase2-d/lcInteractionBase2", "learningInteractionBase2-d/lcQuestionBase2"),
                compile("dl", "pr-d/parml"),
                compile("dlentry", "pr-d/plentry"),
                compile("dt", "pr-d/pt"),
                compile("fig", "equation-d/equation-figure", "learning-d/lcHotspot", "learning-d/lcHotspotMap", "learning-d/lcMatching", "learning-d/lcMultipleSelect", "learning-d/lcOpenQuestion", "learning-d/lcSequencing", "learning-d/lcSingleSelect", "learning-d/lcTrueFalse", "learning2-d/lcHotspotMap2", "learningInteractionBase-d/lcInteractionBase", "learningPlan/lcCIN", "learningPlan/lcClassroom", "learningPlan/lcClient", "learningPlan/lcConstraints", "learningPlan/lcDelivDate", "learningPlan/lcDownloadTime", "learningPlan/lcFileSizeLimitations", "learningPlan/lcGapItem", "learningPlan/lcGraphics", "learningPlan/lcHandouts", "learningPlan/lcInterventionItem", "learningPlan/lcLMS", "learningPlan/lcModDate", "learningPlan/lcNoLMS", "learningPlan/lcOJT", "learningPlan/lcOrganizational", "learningPlan/lcPlanAudience", "learningPlan/lcPlanDescrip", "learningPlan/lcPlanPrereqs", "learningPlan/lcPlanSubject", "learningPlan/lcPlanTitle", "learningPlan/lcPlayers", "learningPlan/lcResolution", "learningPlan/lcSecurity", "learningPlan/lcTask", "learningPlan/lcViewers", "learningPlan/lcW3C", "learningPlan/lcWorkEnv", "pr-d/syntaxdiagram", "ut-d/imagemap"),
                compile("figgroup", "learning-d/lcArea", "learning2-d/lcArea2", "pr-d/fragment", "pr-d/groupchoice", "pr-d/groupcomp", "pr-d/groupseq", "pr-d/synblk", "ut-d/area"),
                compile("fn", "pr-d/synnote"),
                compile("foreign", "mathml-d/mathml", "svg-d/svg-container"),
                compile("image", "glossentry/glossSymbol", "hazard-d/hazardsymbol"),
                compile("index-base", "indexing-d/index-see", "indexing-d/index-see-also", "indexing-d/index-sort-as"),
                compile("itemgroup", "task/info", "task/stepresult", "task/steptroubleshooting", "task/stepxmp", "task/tutorialinfo"),
                compile("keyword", "delay-d/anchorid", "delay-d/anchorkey", "learning-d/lcAreaShape", "learning2-d/lcAreaShape2", "markup-d/markupname", "pr-d/apiname", "pr-d/kwd", "pr-d/option", "pr-d/parmname", "sw-d/cmdname", "sw-d/msgnum", "sw-d/varname", "ui-d/shortcut", "ui-d/wintitle", "ut-d/shape", "xml-d/numcharref", "xml-d/parameterentity", "xml-d/textentity", "xml-d/xmlatt", "xml-d/xmlelement", "xml-d/xmlnsname", "xml-d/xmlpi"),
                compile("keywords", "delay-d/exportanchors"),
                compile("li", "hazard-d/consequence", "hazard-d/howtoavoid", "hazard-d/typeofhazard", "learning-d/lcAnswerOption", "learning-d/lcSequenceOption", "learning2-d/lcAnswerOption2", "learning2-d/lcSequenceOption2", "learningBase/lcObjective", "task/choice", "task/step", "task/stepsection", "task/substep", "taskreq-d/esttime", "taskreq-d/noconds", "taskreq-d/nosafety", "taskreq-d/perscat", "taskreq-d/perskill", "taskreq-d/personnel", "taskreq-d/reqcond", "taskreq-d/reqcontp", "taskreq-d/safecond", "taskreq-d/spare", "taskreq-d/supequi", "taskreq-d/supply"),
                compile("map", "bookmap/bookmap", "learningGroupMap/learningGroupMap", "learningObjectMap/learningObjectMap", "subjectScheme/subjectScheme"),
                compile("metadata", "learningmeta-d/lcLom", "relmgmt-d/change-historylist"),
                compile("note", "glossentry/glossScopeNote", "glossentry/glossUsage", "hazard-d/hazardstatement", "learning-d/lcInstructornote", "learning2-d/lcInstructornote2"),
                compile("ol", "learning-d/lcSequenceOptionGroup", "learning2-d/lcSequenceOptionGroup2", "task/steps", "task/substeps", "taskreq-d/safety"),
                compile("p", "glossentry/glossSurfaceForm", "learning-d/lcAnswerContent", "learning-d/lcAsset", "learning-d/lcFeedback", "learning-d/lcFeedbackCorrect", "learning-d/lcFeedbackIncorrect", "learning-d/lcOpenAnswer", "learning-d/lcQuestion", "learningInteractionBase-d/lcQuestionBase", "learningInteractionBase2-d/lcInteractionLabel2", "learningPlan/lcAge", "learningPlan/lcAssessment", "learningPlan/lcAttitude", "learningPlan/lcBackground", "learningPlan/lcDelivery", "learningPlan/lcEdLevel", "learningPlan/lcGapItemDelta", "learningPlan/lcGeneralDescription", "learningPlan/lcGoals", "learningPlan/lcJtaItem", "learningPlan/lcKnowledge", "learningPlan/lcLearnStrat", "learningPlan/lcMotivation", "learningPlan/lcNeeds", "learningPlan/lcOrgConstraints", "learningPlan/lcPlanObjective", "learningPlan/lcPlanResources", "learningPlan/lcProcesses", "learningPlan/lcSkills", "learningPlan/lcSpecChars", "learningPlan/lcTaskItem", "learningPlan/lcValues", "learningPlan/lcWorkEnvDescription", "taskreq-d/spares", "taskreq-d/supequip", "taskreq-d/supplies", "troubleshooting/responsibleParty"),
                compile("ph", "bookmap/booklibrary", "bookmap/booktitlealt", "bookmap/completed", "bookmap/day", "bookmap/mainbooktitle", "bookmap/month", "bookmap/revisionid", "bookmap/started", "bookmap/summary", "bookmap/year", "equation-d/equation-inline", "equation-d/equation-number", "hi-d/b", "hi-d/i", "hi-d/line-through", "hi-d/overline", "hi-d/sub", "hi-d/sup", "hi-d/tt", "hi-d/u", "learning-d/lcAreaCoords", "learning2-d/lcAreaCoords2", "learningBase/lcObjectivesStem", "pr-d/codeph", "pr-d/delim", "pr-d/oper", "pr-d/repsep", "pr-d/sep", "pr-d/synph", "pr-d/var", "sw-d/filepath", "sw-d/msgph", "sw-d/systemoutput", "sw-d/userinput", "task/cmd", "ui-d/menucascade", "ui-d/uicontrol", "ut-d/coords", "xnal-d/addressdetails", "xnal-d/administrativearea", "xnal-d/country", "xnal-d/locality", "xnal-d/localityname", "xnal-d/organizationname", "xnal-d/organizationnamedetails", "xnal-d/postalcode", "xnal-d/thoroughfare"),
                compile("pre", "pr-d/codeblock", "sw-d/msgblock", "ui-d/screen"),
                compile("publisher", "bookmap/publisherinformation"),
                compile("relcell", "classify-d/subjectCell", "classify-d/topicCell", "subjectScheme/subjectRole"),
                compile("relrow", "classify-d/topicSubjectHeader", "classify-d/topicSubjectRow", "subjectScheme/subjectRel", "subjectScheme/subjectRelHeader"),
                compile("reltable", "classify-d/topicSubjectTable", "subjectScheme/subjectRelTable"),
                compile("section", "glossentry/glossAlt", "learningBase/lcAudience", "learningBase/lcChallenge", "learningBase/lcDuration", "learningBase/lcInstruction", "learningBase/lcInteraction", "learningBase/lcIntro", "learningBase/lcNextSteps", "learningBase/lcObjectives", "learningBase/lcPrereqs", "learningBase/lcResources", "learningBase/lcReview", "learningBase/lcSummary", "learningPlan/lcGapAnalysis", "learningPlan/lcIntervention", "learningPlan/lcNeedsAnalysis", "learningPlan/lcProject", "learningPlan/lcTechnical", "reference/refsyn", "task/context", "task/postreq", "task/prereq", "task/result", "task/steps-informal", "task/tasktroubleshooting", "taskreq-d/closereqs", "taskreq-d/prelreqs", "troubleshooting/cause", "troubleshooting/condition", "troubleshooting/remedy"),
                compile("simpletable", "learning-d/lcMatchTable", "learning2-d/lcMatchTable2", "reference/properties", "task/choicetable"),
                compile("stentry", "learning-d/lcItem", "learning-d/lcMatchingItem", "learning-d/lcMatchingItemFeedback", "learning2-d/lcItem2", "learning2-d/lcMatchingItem2", "learning2-d/lcMatchingItemFeedback2", "reference/propdesc", "reference/propdeschd", "reference/proptype", "reference/proptypehd", "reference/propvalue", "reference/propvaluehd", "task/chdesc", "task/chdeschd", "task/choption", "task/choptionhd"),
                compile("sthead", "learning-d/lcMatchingHeader", "learning2-d/lcMatchingHeader2", "reference/prophead", "task/chhead"),
                compile("strow", "learning-d/lcMatchingPair", "learning2-d/lcMatchingPair2", "reference/property", "task/chrow"),
                compile("term", "abbrev-d/abbreviated-form"),
                compile("title", "bookmap/booktitle", "glossentry/glossAbbreviation", "glossentry/glossAcronym", "glossentry/glossShortForm", "glossentry/glossSynonym", "glossentry/glossterm"),
                compile("topic", "concept/concept", "glossentry/glossentry", "glossgroup/glossgroup", "learningAssessment/learningAssessment", "learningBase/learningBase", "learningContent/learningContent", "learningOverview/learningOverview", "learningPlan/learningPlan", "learningSummary/learningSummary", "reference/reference", "task/task", "troubleshooting/troubleshooting"),
                compile("topicmeta", "bookmap/bookmeta", "ditavalref-d/ditavalmeta", "subjectScheme/subjectHeadMeta"),
                compile("topicref", "bookmap/abbrevlist", "bookmap/amendments", "bookmap/appendices", "bookmap/appendix", "bookmap/backmatter", "bookmap/bibliolist", "bookmap/bookabstract", "bookmap/booklist", "bookmap/booklists", "bookmap/chapter", "bookmap/colophon", "bookmap/dedication", "bookmap/draftintro", "bookmap/figurelist", "bookmap/frontmatter", "bookmap/glossarylist", "bookmap/indexlist", "bookmap/notices", "bookmap/part", "bookmap/preface", "bookmap/tablelist", "bookmap/toc", "bookmap/trademarklist", "classify-d/subjectref", "classify-d/topicapply", "classify-d/topicsubject", "ditavalref-d/ditavalref", "glossref-d/glossref", "learningmap-d/learningContentComponentRef", "learningmap-d/learningContentRef", "learningmap-d/learningGroup", "learningmap-d/learningGroupMapRef", "learningmap-d/learningObject", "learningmap-d/learningObjectMapRef", "learningmap-d/learningOverviewRef", "learningmap-d/learningPlanRef", "learningmap-d/learningPostAssessmentRef", "learningmap-d/learningPreAssessmentRef", "learningmap-d/learningSummaryRef", "mapgroup-d/anchorref", "mapgroup-d/keydef", "mapgroup-d/mapref", "mapgroup-d/topicgroup", "mapgroup-d/topichead", "mapgroup-d/topicset", "mapgroup-d/topicsetref", "subjectScheme/defaultSubject", "subjectScheme/enumerationdef", "subjectScheme/hasInstance", "subjectScheme/hasKind", "subjectScheme/hasNarrower", "subjectScheme/hasPart", "subjectScheme/hasRelated", "subjectScheme/relatedSubjects", "subjectScheme/schemeref", "subjectScheme/subjectHead", "subjectScheme/subjectdef"),
                compile("ul", "hazard-d/messagepanel", "learning-d/lcAnswerOptionGroup", "learning2-d/lcAnswerOptionGroup2", "learningBase/lcObjectivesGroup", "task/choices", "task/steps-unordered", "taskreq-d/reqconds", "taskreq-d/reqpers", "taskreq-d/sparesli", "taskreq-d/supeqli", "taskreq-d/supplyli"),
                compile("xref", "glossentry/glossAlternateFor", "mathml-d/mathmlref", "pr-d/coderef", "pr-d/fragref", "pr-d/synnoteref", "svg-d/svgref"),
                new SimpleEntry<>(Pattern.compile("\\*\\[contains\\(\\@class, *' (?:map|topic)/(\\w+) '\\)\\]"), "$1")
        );
    }

    private static Entry<Pattern, String> compile(final String element, final String... classes) {
        final String cls = StringUtils.join(Arrays.asList(classes), "|");
        Pattern pattern = Pattern.compile("\\*(\\[contains\\(\\@class, *' (?:" + cls + ") '\\)\\])");
        return new SimpleEntry(pattern, element + "$1");
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
                             final Attributes atts) throws SAXException {
        Attributes resAtts = atts;
        if (uri.equals(XSL_NS)) {
            final AttributesImpl res = new AttributesImpl(atts);
            if (localName.equals("include") || localName.equals("import")) {
                XMLUtils.addOrSetAttribute(res, "href", rewriteImport(res.getValue("href")));
            } else {
                for (int i = 0; i < res.getLength(); i++) {
                    res.setValue(i, optimizeAttributeValue(res.getValue(i)));
                }
            }
            resAtts = res;
        }
        getContentHandler().startElement(uri, localName, name, resAtts);
    }

    private String rewriteImport(final String href) {
        if (href.endsWith(".xsl")) {
            return href.substring(0, href.length() - 4) + OPTIMIZED_XSL_EXTENSION;
        }
        return href;
    }

    private String optimizeAttributeValue(final String value) {
        String res = value;
        for (Entry<Pattern, String> e: ps) {
            final Matcher m = e.getKey().matcher(res);
            res = m.replaceAll(e.getValue());
        }
        return res;
    }

}