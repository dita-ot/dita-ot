<!--
 | (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Topic//EN"
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
 |   1.1.3a bug fix: cut keyword def from meta_xml; paste into topic.mod unchanged, put into map.mod with no tm
 *-->
<!ENTITY DTDVersion 'V1.1.3' >



<!-- 
 | Processing Notes:
 | - The class attribute has an impliable value to allow generalization (being able to maintain the
 |   "history" of what an element was derived from when generalized); the memory allows either
 |   respecialization back to original form, or possibly a more useful migration into a different
 |   domain or infotyped topic.
 |
 | - Links and xrefs have these documented target types (for @type):
 |    link types: topic | concept | task | reference | external | local
 |    xref types: same as link, plus: fig | figgroup | table | li | fn | section
 |
 | - The relcolwidth attribute takes full, relative specifications with no units.
 |   That is, "85* 15*" is valid; "85 15" is not.
 |
 +-->

<!-- =========================================================================== -->
<!-- COMMON ENTITY DECLARATIONS ================================================ -->
<!-- =========================================================================== -->
<!-- ============ definitions of declared elements ============ -->
<!ENTITY % topicDefns PUBLIC "-//IBM//ENTITIES DITA Topic Definitions//EN" "topic_defn.ent">
  %topicDefns;


<!-- Phrase or inline elements of various classes -->
<!ENTITY % basic.ph             "%ph;|%term;|%xref;|%cite;|%q;|%boolean;|%state;|%keyword;|%tm;">

<!-- Elements common to most body-like contexts -->
<!ENTITY % basic.block          "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
   <!-- class groupings to preserve in a schema -->

<!ENTITY % basic.phandblock     "%basic.ph; | %basic.block;">


<!-- Exclusions: models modified by removing excluded content -->
<!ENTITY % basic.ph.noxref      "%ph;|%term;|              %q;|%boolean;|%state;|%keyword;|%tm;">
<!ENTITY % basic.ph.notm        "%ph;|%term;|%xref;|%cite;|%q;|%boolean;|%state;|%keyword;">


<!ENTITY % basic.block.notbl     "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;">
<!ENTITY % basic.block.nonote    "%p;|%lq;|       %dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.nopara    "    %lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.nolq      "%p;|     %note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.notbnofg  "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|      %image;|%object;">
<!ENTITY % basic.block.notbfgobj "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|      %image;">


<!-- Inclusions: defined sets that can be added into appropriate models -->
<!ENTITY % txt.incl             '%draft-comment;|%required-cleanup;|%fn;|%indextermref;|%indexterm;'>

<!-- Predefined content model groups, based on the previous, element-only categories: -->
<!-- txt.incl is appropriate for any mixed content definitions (those that have PCDATA) -->
<!-- the context for blocks is implicitly an InfoMaster "containing_division" -->
<!ENTITY % body.cnt             "%basic.block; | %required-cleanup;">
<!ENTITY % section.cnt          "#PCDATA | %basic.ph; | %basic.block; | %title; |  %txt.incl;">
<!ENTITY % section.notitle.cnt  "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;">
<!ENTITY % listitem.cnt         "#PCDATA | %basic.ph; | %basic.block; |%itemgroup;| %txt.incl;">
<!ENTITY % itemgroup.cnt        "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;">
<!ENTITY % title.cnt            "#PCDATA | %basic.ph.noxref; | %image;">
<!ENTITY % xreftext.cnt         "#PCDATA | %basic.ph.noxref; | %image;">
<!ENTITY % xrefph.cnt           "#PCDATA | %basic.ph.noxref;">
<!ENTITY % shortquote.cnt       "#PCDATA | %basic.ph;">
<!ENTITY % para.cnt             "#PCDATA | %basic.ph; | %basic.block.nopara; | %txt.incl;">
<!ENTITY % note.cnt             "#PCDATA | %basic.ph; | %basic.block.nonote; | %txt.incl;">
<!ENTITY % longquote.cnt        "#PCDATA | %basic.ph; | %basic.block.nolq;   | %txt.incl;">
<!ENTITY % tblcell.cnt          "#PCDATA | %basic.ph; | %basic.block.notbl;  | %txt.incl;">
<!ENTITY % desc.cnt             "#PCDATA | %basic.ph; | %basic.block.notbfgobj;">
<!ENTITY % ph.cnt               "#PCDATA | %basic.ph; | %image;              | %txt.incl;">
<!ENTITY % fn.cnt               "#PCDATA | %basic.ph; | %basic.block.notbl;">
<!ENTITY % term.cnt             "#PCDATA | %basic.ph; | %image;">
<!ENTITY % defn.cnt             "%listitem.cnt;">
<!ENTITY % pre.cnt              "#PCDATA | %basic.ph; | %txt.incl;">
<!ENTITY % fig.cnt              "%basic.block.notbnofg; | %simpletable;">
<!ENTITY % words.cnt            "#PCDATA | %keyword; | %term;">

<!-- COMMON ENTITY DECLARATIONS =================================== -->
<!-- for use within the DTD and supported topics; these will NOT work
     outside of this DTD or dtds that specialize from it! -->
<!ENTITY nbsp "&#160;">


<!-- NOTATION DECLARATIONS =================================== -->
<!-- DITA uses the direct reference model; notations may be added later as required -->


<!-- STRUCTURAL MEMBERS ======================================================== -->
  <!-- things that can be nested under topic after body - redefined when specializing -->
<!ENTITY % topicreftypes 'topic | concept | task | reference | external | local'>
<!ENTITY % info-types     'topic'> <!-- include zone -->


<!-- COMMON ATTLIST SETS ========================================== -->

<!-- imply datatypes for particular attribute values -->

<!ENTITY % date-format   'CDATA'>


<!-- these are common for some classes of resources and exhibits -->

<!ENTITY % rel-atts      'type CDATA #IMPLIED
                          role (parent|child|sibling|friend|next|previous|cousin|ancestor|descendant|sample|external|other) #IMPLIED
                          otherrole CDATA #IMPLIED'
>
<!ENTITY % display-atts  'scale (50|60|70|80|90|100|110|120|140|160|180|200) #IMPLIED
                          frame (top | bottom |topbot | all | sides | none) #IMPLIED
                          expanse (page|column|textline) #IMPLIED'
>
<!ENTITY % select-atts   'platform CDATA #IMPLIED
                          product CDATA #IMPLIED
                          audience CDATA #IMPLIED
                          otherprops CDATA #IMPLIED
                          importance ( obsolete | deprecated | optional | default | low | normal | high | recommended | required | urgent ) #IMPLIED
                          rev CDATA #IMPLIED
                          status (new|changed|deleted|unchanged) #IMPLIED'
>

<!ENTITY % id-atts       'id NMTOKEN #IMPLIED
                          conref CDATA #IMPLIED'
>
<!ENTITY % univ-atts     '%id-atts;
                          %select-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED'
>

<!ENTITY % global-atts    'xtrc CDATA #IMPLIED
                           xtrf CDATA #IMPLIED'
>

<!-- http://www.w3.org/TR/REC-xml#sec-lang-tag -->
<!--  proposed content: Internet RFC 1766 'Tags for the identification of Language' -->
<!--  and ISO 639-1 (two-character codes) -->


<!-- TYPED TOPICS (semantic and structural specialization) ========================= -->

<!ENTITY % topic-info-types "%info-types;">
<!ENTITY included-domains "">

<!--  infotype 'topic'
 | Topic is the archetype from which other typed topics may be derived.
 | Its body has completely optional content, which allows topic to be used as a titled container
 | role: migration target for XHTML, other hierarchically structured source
 *-->
<!ELEMENT topic          (%title;, (%titlealts;)?, (%shortdesc;)?, (%prolog;)?, %body;, (%related-links;)?, (%topic-info-types;)* )>
<!ATTLIST topic           id ID #REQUIRED
                          conref CDATA #IMPLIED
                          %select-atts;
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          DTDVersion CDATA #FIXED "&DTDVersion;"
                          domains CDATA "&included-domains;"
>

<!ELEMENT title          (%title.cnt;)* > <!-- this is referenced inside CALS table -->
<!ATTLIST title           %id-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT titlealts      ((%navtitle;)?,(%searchtitle;)?)>
<!ATTLIST titlealts       %id-atts;
>
<!ELEMENT navtitle       (%words.cnt;)*>
<!ATTLIST navtitle        %id-atts;
>
<!ELEMENT searchtitle    (%words.cnt;)*>
<!ATTLIST searchtitle     %id-atts;
>
<!ELEMENT shortdesc      (%title.cnt;)* >
<!ATTLIST shortdesc       %id-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT body           (%body.cnt; | %section; | %example;)* >
<!ATTLIST body            %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED>

<!ELEMENT no-topic-nesting EMPTY
>
<!ELEMENT section         (%section.cnt;)* >
<!ATTLIST section         spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT example         (%section.cnt;)* >
<!ATTLIST example         spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- desc is used in context with figure and table titles and also for
     content models within linkgroup and object (for accessibility) -->
<!ELEMENT desc           (%desc.cnt;)* >
<!ATTLIST desc            %id-atts;
                          outputclass CDATA #IMPLIED
>


<!-- PROLOG (metadata for topics) =================================== -->


<!-- TYPED DATA ELEMENTS: ======================================================= -->
<!-- typed content definitions  -->
<!-- typed, localizable content -->



<!ELEMENT prolog         ((%author;)*,(%source;)?,(%publisher;)?,(%copyright;)*,(%critdates;)?,(%permissions;)?,(%metadata;)*, (%resourceid;)*)>
<!ELEMENT metadata       ((%audience;)*,(%category;)*,(%keywords;)*,(%prodinfo;)*,(%othermeta;)*) >
<!ATTLIST metadata        mapkeyref CDATA #IMPLIED
>



<!-- =========================================================================== -->
<!-- BASIC DOCUMENT ELEMENT DECLARATIONS (rich text) =========================== -->
<!-- =========================================================================== -->

<!-- Base form: Paragraph -->
<!ELEMENT p              (%para.cnt;)*>
<!ATTLIST p               %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Note -->
<!ELEMENT note           (%note.cnt;)*>
<!ATTLIST note            type (note|tip|fastpath|restriction|important|remember|attention|caution|danger|other) #IMPLIED
                          spectitle CDATA #IMPLIED
                          othertype CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>



<!-- Base form: Excerpt -->
<!ELEMENT lq             (%longquote.cnt;)*>
<!ATTLIST lq              href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          type (external|internal|bibliographic) #IMPLIED
                          reftitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Quoted text -->
<!ELEMENT q              (%shortquote.cnt;)* > <!-- q=quote, lq=long quote -->
<!ATTLIST q               %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Simple List -->
<!ELEMENT sl             (%sli;)+>
<!ATTLIST sl              compact (yes|no) #IMPLIED
                          spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: List Item -->
<!ELEMENT sli             (%ph.cnt;)*>
<!ATTLIST sli              %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- Base form: Unordered List -->
<!ELEMENT ul             (%li;)+>
<!ATTLIST ul              compact (yes|no) #IMPLIED
                          spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Ordered List -->
<!ELEMENT ol             (%li;)+>
<!ATTLIST ol              compact (yes|no) #IMPLIED
                          spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: List Item -->
<!ELEMENT li             (%listitem.cnt;)*>
<!ATTLIST li              %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Item Group -->
<!ELEMENT itemgroup      (%itemgroup.cnt;)*>
<!ATTLIST itemgroup       %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- Base form: Definition List -->
<!ELEMENT dl             ((%dlhead;)?, (%dlentry;)+) >
<!ATTLIST dl              compact (yes|no) #IMPLIED
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT dlhead         ((%dthd;)?,(%ddhd;)?)>
<!ATTLIST dlhead          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT dthd           (%title.cnt;)*>     <!-- heading for dt and dd -->
<!ATTLIST dthd            %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT ddhd           (%title.cnt;)*>
<!ATTLIST ddhd            %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT dlentry        ((%dt;)+,(%dd;)+) >
<!ATTLIST dlentry         %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT dt             (%term.cnt;)*> <!-- defining term -->
<!ATTLIST dt              keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT dd             (%defn.cnt;)* >   <!-- description -->
<!ATTLIST dd              %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Figure -->
<!ELEMENT fig            ((%title;)?, (%desc;)?, (%figgroup; | %fig.cnt;)*)>
<!ATTLIST fig             %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT figgroup       ((%title;)?, (%figgroup; | %xref; | %fn; | %ph; | %keyword;)*)>
<!ATTLIST figgroup       %univ-atts;
                         outputclass CDATA #IMPLIED
>

<!-- Base form: Preformatted Text -->
<!ELEMENT pre            (%pre.cnt;)*>
<!ATTLIST pre             %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>

<!-- Base form: Line Respecting Text -->
<!ELEMENT lines          (%pre.cnt;)*>
<!ATTLIST lines           %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>

<!-- Base form: Base Phrase Types -->

<!ELEMENT keyword        (#PCDATA | tm)*>
<!ATTLIST keyword         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT term           (#PCDATA | %tm;)*>
<!ATTLIST term            keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT ph             (%ph.cnt;)*>
<!ATTLIST ph              keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT tm             (#PCDATA | %tm;)*>
<!ATTLIST tm
                          trademark CDATA           #IMPLIED
                          tmowner   CDATA           #IMPLIED
                          tmtype   (tm|reg|service) #REQUIRED
                          tmclass   CDATA           #IMPLIED
>
<!ELEMENT boolean        EMPTY>
<!ATTLIST boolean         state (yes|no) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- a state can have a name and a string value, even if empty or indeterminate -->
<!-- note that "state" is distinguished from element "meta," intended for more general metadescription -->
<!ELEMENT state          EMPTY>
<!ATTLIST state           name CDATA #REQUIRED
                          value CDATA #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Image Data -->
<!ELEMENT image          (%alt;)?>
<!ATTLIST image           href       CDATA           #REQUIRED
                          keyref     NMTOKEN         #IMPLIED
                          alt        CDATA           #IMPLIED
                          longdescref CDATA          #IMPLIED
                          height     NMTOKEN         #IMPLIED
                          width      NMTOKEN         #IMPLIED
                          align      CDATA           #IMPLIED
                          placement  (inline|break)  "inline"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT alt            (%words.cnt;)*>
<!ATTLIST alt             %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Streaming/Executable Data -->
<!ELEMENT object         ((%desc;)?, (%param;)*)>
<!ATTLIST object
                          declare     (declare)      #IMPLIED
                          classid     CDATA          #IMPLIED
                          codebase    CDATA          #IMPLIED
                          data        CDATA          #IMPLIED
                          type        CDATA          #IMPLIED
                          codetype    CDATA          #IMPLIED
                          archive     CDATA          #IMPLIED
                          standby     CDATA          #IMPLIED
                          height      NMTOKEN        #IMPLIED
                          width       NMTOKEN        #IMPLIED
                          usemap      CDATA          #IMPLIED
                          name        CDATA          #IMPLIED
                          tabindex    NMTOKEN        #IMPLIED
                          longdescref CDATA          #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT param          EMPTY>
<!ATTLIST param
                          name        CDATA          #REQUIRED
                          id          ID             #IMPLIED
                          value       CDATA          #IMPLIED
                          valuetype   (data|ref|object) #IMPLIED

                          type        CDATA          #IMPLIED
>

<!-- Base form: Simple Table -->
<!ELEMENT simpletable    ((%sthead;)?, (%strow;)+) >
<!ATTLIST simpletable     relcolwidth CDATA #IMPLIED
                          keycol NMTOKEN #IMPLIED
                          refcols NMTOKENS #IMPLIED
                          %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT sthead         (%stentry;)+>
<!ATTLIST sthead          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT strow          (%stentry;)*>
<!ATTLIST strow           %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT stentry        (%tblcell.cnt;)*>
<!ATTLIST stentry         %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Review Comments Block -->
<!ELEMENT draft-comment  (#PCDATA | %basic.phandblock;)*>
<!ATTLIST draft-comment   author CDATA #IMPLIED
                          time CDATA #IMPLIED
                          disposition (issue|open|accepted|rejected|deferred|duplicate|reopened|unassigned|completed) #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Required Cleanup Block -->
<!ELEMENT required-cleanup ANY >
<!ATTLIST required-cleanup remap CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Footnote -->
<!ELEMENT fn             (%fn.cnt;)*> <!--footnote -->
<!ATTLIST fn              callout CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Index Entry -->

<!-- <!ELEMENT indexterm      (#PCDATA|indexterm)*> -->   <!-- Index entry -->
<!-- <!ATTLIST indexterm       keyref NMTOKEN #IMPLIED
                          %univ-atts;
> -->
<!ELEMENT indextermref   EMPTY>               <!-- Index term reference -->
<!ATTLIST indextermref    keyref NMTOKEN #REQUIRED
                          %univ-atts;
>

<!-- Base form: Citation (from a bibliographic source) -->
<!ELEMENT cite           (%xrefph.cnt;)* >
<!ATTLIST cite            keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- Base form: Cross Reference/Link -->
<!ELEMENT xref           (%xreftext.cnt;)*>
<!ATTLIST xref            href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          type   CDATA  #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!-- ================ links grouping ================ -->

<!ELEMENT related-links  (%link; | %linklist; | %linkpool;)+>
<!ATTLIST related-links   %rel-atts;
                          %select-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT link           ((%linktext;)?, (%desc;)?)>
<!ATTLIST link            href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          %rel-atts;
                          %select-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT linktext       (%words.cnt;)*>
<!ELEMENT linklist       ((%title;)?, (%desc;)?,(%linklist; | %link;)*,(%linkinfo;)?)>
<!ATTLIST linklist        collection-type (unordered|sequence|choice|tree|family) #IMPLIED
                          duplicates (yes|no) #IMPLIED
                          mapkeyref CDATA #IMPLIED
                          %rel-atts;
                          %select-atts;
                          spectitle CDATA #IMPLIED
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT linkinfo       (%desc.cnt;)*>
<!ELEMENT linkpool       (%linkpool; | %link;)*> <!-- for now -->
<!ATTLIST linkpool        collection-type (unordered|sequence|choice|tree|family) #IMPLIED
                          duplicates (yes|no) #IMPLIED
                          mapkeyref CDATA #IMPLIED
                          %rel-atts;
                          %select-atts;
                          format        CDATA   #IMPLIED
                          scope (local | peer | external) #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!-- ==================== include sections ======================= -->
<!-- ======== Table elements ======== -->
<!ENTITY % tableXML PUBLIC "-//IBM//ELEMENTS DITA CALS Tables//EN" "tbl_xml.mod">
  %tableXML;

<!-- ======= MetaData elements, plus keyword and indexterm ======= -->
<!ENTITY % metaXML PUBLIC "-//IBM//ELEMENTS DITA Metadata//EN" "meta_xml.mod">
  %metaXML;

<!-- ============ Specialization of declared elements ============ -->
<!ENTITY % topicClasses  PUBLIC "-//IBM//ENTITIES DITA Topic Class//EN" "topic_class.ent">
  %topicClasses;
