<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Bookmap                                      -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA BookMap//EN" 
      Delivered as file "bookmap.mod"                              -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributes    -->
<!--             for Book Maps                                     -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2004                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2004, 2005.         -->
<!--             All Rights Reserved.                              -->
<!--  UPDATES:                                                     -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.01.28 RDA: Removed enumerations for attributes:       -->
<!--                    publishtype/@value, bookrestriction/@value -->
<!--    2008.01.28 RDA: Added <metadata> to <bookmeta>             -->
<!--    2008.01.30 RDA: Replace @conref defn. with %conref-atts;   -->
<!--    2008.02.01 RDA: Added keys attributes, more keyref attrs   -->
<!--    2008.02.12 RDA: Add keyword to many data specializations   -->
<!--    2008.02.12 RDA: Add @format, @scope, and @type to          -->
<!--                    publisherinformation                       -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!--    2008.03.17 RDA: Add appendices element                     -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   ARCHITECTURE ENTITIES                       -->
<!-- ============================================================= -->

<!-- default namespace prefix for DITAArchVersion attribute can be
     overridden through predefinition in the document type shell   -->
<!ENTITY % DITAArchNSPrefix
  "ditaarch" 
>

<!-- must be instanced on each topic type                          -->
<!ENTITY % arch-atts 
             "xmlns:%DITAArchNSPrefix; 
                         CDATA 
                                  #FIXED 'http://dita.oasis-open.org/architecture/2005/'
              %DITAArchNSPrefix;:DITAArchVersion
                         CDATA
                                  '1.2'
  "
>


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->
 
<!ENTITY % bookmap         "bookmap"                                 >

<!ENTITY % abbrevlist      "abbrevlist"                              >
<!ENTITY % bookabstract    "bookabstract"                            >
<!ENTITY % amendments      "amendments"                              >
<!ENTITY % appendices      "appendices"                              >
<!ENTITY % appendix        "appendix"                                >
<!ENTITY % approved        "approved"                                >
<!ENTITY % backmatter      "backmatter"                              >
<!ENTITY % bibliolist      "bibliolist"                              >
<!ENTITY % bookchangehistory "bookchangehistory"                     >
<!ENTITY % bookevent       "bookevent"                               >
<!ENTITY % bookeventtype   "bookeventtype"                           >
<!ENTITY % bookid          "bookid"                                  >
<!ENTITY % booklibrary     "booklibrary"                             >
<!ENTITY % booklist        "booklist"                                >
<!ENTITY % booklists       "booklists"                               >
<!ENTITY % bookmeta        "bookmeta"                                >
<!ENTITY % booknumber      "booknumber"                              >
<!ENTITY % bookowner       "bookowner"                               >
<!ENTITY % bookpartno      "bookpartno"                              >
<!ENTITY % bookrestriction "bookrestriction"                         >
<!ENTITY % bookrights      "bookrights"                              >
<!ENTITY % booktitle       "booktitle"                               >
<!ENTITY % booktitlealt    "booktitlealt"                            >
<!ENTITY % chapter         "chapter"                                 >
<!ENTITY % colophon        "colophon"                                >
<!ENTITY % completed       "completed"                               >
<!ENTITY % copyrfirst      "copyrfirst"                              >
<!ENTITY % copyrlast       "copyrlast"                               >
<!ENTITY % day             "day"                                     >
<!ENTITY % dedication      "dedication"                              >
<!ENTITY % draftintro      "draftintro"                              >
<!ENTITY % edited          "edited"                                  >
<!ENTITY % edition         "edition"                                 >
<!ENTITY % figurelist      "figurelist"                              >
<!ENTITY % frontmatter     "frontmatter"                             >
<!ENTITY % glossarylist    "glossarylist"                            >
<!ENTITY % indexlist       "indexlist"                               >
<!ENTITY % isbn            "isbn"                                    >
<!ENTITY % mainbooktitle   "mainbooktitle"                           >
<!ENTITY % maintainer      "maintainer"                              >
<!ENTITY % month           "month"                                   >
<!ENTITY % notices         "notices"                                 >
<!ENTITY % organization    "organization"                            >
<!ENTITY % part            "part"                                    >
<!ENTITY % person          "person"                                  >
<!ENTITY % preface         "preface"                                 >
<!ENTITY % printlocation   "printlocation"                           >
<!ENTITY % published       "published"                               >
<!ENTITY % publisherinformation "publisherinformation"               >
<!ENTITY % publishtype     "publishtype"                             >
<!ENTITY % reviewed        "reviewed"                                >
<!ENTITY % revisionid      "revisionid"                              >
<!ENTITY % started         "started"                                 >
<!ENTITY % summary         "summary"                                 >
<!ENTITY % tablelist       "tablelist"                               >
<!ENTITY % tested          "tested"                                  >
<!ENTITY % trademarklist   "trademarklist"                           >
<!ENTITY % toc             "toc"                                     >
<!ENTITY % volume          "volume"                                  >
<!ENTITY % year            "year"                                    >


<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->


<!ENTITY included-domains 
  ""
>

<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->

<!-- Currently: same as topicref, minus @query -->
<!ENTITY % chapter-atts 
             'navtitle 
                         CDATA 
                                   #IMPLIED
              href 
                         CDATA 
                                   #IMPLIED
              keyref 
                         CDATA 
                                   #IMPLIED
              keys 
                         CDATA 
                                   #IMPLIED
              copy-to 
                         CDATA 
                                   #IMPLIED
              outputclass 
                         CDATA 
                                   #IMPLIED
              %topicref-atts;
              %univ-atts;' 
>


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Book Map                        -->
<!ENTITY % bookmap.content
                       "(((%title;) | 
                          (%booktitle;))?,
                         (%bookmeta;)?, 
                         (%frontmatter;)?,
                         (%chapter;)*, 
                         (%part;)*, 
                         ((%appendices;)? | (%appendix;)*),
                         (%backmatter;)?,
                         (%reltable;)*)"
>
<!ENTITY % bookmap.attributes
             "id 
                        ID 
                                  #IMPLIED
              %conref-atts;
              anchorref 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              %localization-atts;
              %topicref-atts;
              %select-atts;"
>
<!ELEMENT bookmap    %bookmap.content;>
<!ATTLIST bookmap    
              %bookmap.attributes;
              %arch-atts;
              domains 
                        CDATA 
                                  '&included-domains;'
>

<!--                    LONG NAME: Book Metadata                   -->
<!ENTITY % bookmeta.content
                       "((%linktext;)?, 
                         (%searchtitle;)?, 
                         (%shortdesc;)?, 
                         (%author;)*, 
                         (%source;)?, 
                         (%publisherinformation;)*,
                         (%critdates;)?, 
                         (%permissions;)?, 
                         (%metadata;)*, 
                         (%audience;)*, 
                         (%category;)*, 
                         (%keywords;)*, 
                         (%prodinfo;)*, 
                         (%othermeta;)*, 
                         (%resourceid;)*, 
                         (%bookid;)?,
                         (%bookchangehistory;)*,
                         (%bookrights;)*,
                         (%data;)*)"
>
<!ENTITY % bookmeta.attributes
             "lockmeta 
                        (no | 
                         yes | 
                         -dita-use-conref-target)
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT bookmeta    %bookmeta.content;>
<!ATTLIST bookmeta    %bookmeta.attributes;>


<!--                    LONG NAME: Front Matter                    -->
<!ENTITY % frontmatter.content
                       "(%bookabstract; | 
                         %booklists; | 
                         %colophon; | 
                         %dedication; | 
                         %draftintro; | 
                         %notices; | 
                         %preface; | 
                         %topicref;)*"
>
<!ENTITY % frontmatter.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              %topicref-atts;
              %univ-atts;"
>
<!ELEMENT frontmatter    %frontmatter.content;>
<!ATTLIST frontmatter    %frontmatter.attributes;>


<!--                    LONG NAME: Back Matter                     -->
<!ENTITY % backmatter.content
                       "(%amendments; | 
                         %booklists; | 
                         %colophon; | 
                         %dedication; | 
                         %notices; | 
                         %topicref;)*"
>
<!ENTITY % backmatter.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              query 
                        CDATA 
                                  #IMPLIED
              outputclass 
                        CDATA 
                                  #IMPLIED
              %topicref-atts;
              %univ-atts;"
>
<!ELEMENT backmatter    %backmatter.content;>
<!ATTLIST backmatter    %backmatter.attributes;>


<!--                    LONG NAME: Publisher Information           -->
<!ENTITY % publisherinformation.content
                       "(((%person;) | 
                          (%organization;))*, 
                         (%printlocation;)*, 
                         (%published;)*, 
                         (%data;)*)"
>
<!ENTITY % publisherinformation.attributes
             "href 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                       (external | 
                        local | 
                        peer | 
                        -dita-use-conref-target) 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;"
>
<!ELEMENT publisherinformation    %publisherinformation.content;>
<!ATTLIST publisherinformation    %publisherinformation.attributes;>


<!--                    LONG NAME: Person                          -->
<!ENTITY % person.content
                       "(%words.cnt;)*"
>
<!ENTITY % person.attributes
             "%data-element-atts;"
>
<!ELEMENT person    %person.content;>
<!ATTLIST person    %person.attributes;>


<!--                    LONG NAME: Organization                    -->
<!ENTITY % organization.content
                       "(%words.cnt;)*"
>
<!ENTITY % organization.attributes
             "%data-element-atts;"
>
<!ELEMENT organization    %organization.content;>
<!ATTLIST organization    %organization.attributes;>


<!--                    LONG NAME: Book Change History             -->
<!ENTITY % bookchangehistory.content
                       "((%reviewed;)*, 
                         (%edited;)*, 
                         (%tested;)*, 
                         (%approved;)*, 
                         (%bookevent;)*)"
>
<!ENTITY % bookchangehistory.attributes
             "%data-element-atts;"
>
<!ELEMENT bookchangehistory    %bookchangehistory.content;>
<!ATTLIST bookchangehistory    %bookchangehistory.attributes;>


<!--                    LONG NAME: Book ID                         -->
<!ENTITY % bookid.content
                       "((%bookpartno;)*, 
                         (%edition;)?, 
                         (%isbn;)?, 
                         (%booknumber;)?, 
                         (%volume;)*, 
                         (%maintainer;)?)"
>
<!ENTITY % bookid.attributes
             "%data-element-atts;"
>
<!ELEMENT bookid    %bookid.content;>
<!ATTLIST bookid    %bookid.attributes;>


<!--                    LONG NAME: Summary                         -->
<!ENTITY % summary.content
                       "(%words.cnt;)*"
>
<!ENTITY % summary.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT summary    %summary.content;>
<!ATTLIST summary    %summary.attributes;>


<!--                    LONG NAME: Print Location                  -->
<!ENTITY % printlocation.content
                       "(%words.cnt;)*"
>
<!ENTITY % printlocation.attributes
             "%data-element-atts;"
>
<!ELEMENT printlocation    %printlocation.content;>
<!ATTLIST printlocation    %printlocation.attributes;>


<!--                    LONG NAME: Published                       -->
<!ENTITY % published.content
                       "(((%person;) | 
                          (%organization;))*,
                         (%publishtype;)?, 
                         (%revisionid;)?,
                         (%started;)?, 
                         (%completed;)?, 
                         (%summary;)?, 
                         (%data;)*)"
>
<!ENTITY % published.attributes
             "%data-element-atts;"
>
<!ELEMENT published    %published.content;>
<!ATTLIST published    %published.attributes;>

<!--                    LONG NAME: Publish Type                    -->
<!ENTITY % publishtype.content
                       "EMPTY"
>
<!-- 20080128: Removed enumeration for @value for DITA 1.2. Previous values:
               beta, general, limited, -dita-use-conref-target
               Matches data-element-atts, but value is required           -->
<!ENTITY % publishtype.attributes
             "%univ-atts;
              name 
                        CDATA 
                                  #IMPLIED
              datatype 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local | 
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              outputclass
                        CDATA 
                                  #IMPLIED
              value 
                        CDATA 
                                  #REQUIRED"
>
<!ELEMENT publishtype    %publishtype.content;>
<!ATTLIST publishtype    %publishtype.attributes;>
 
<!--                    LONG NAME: Revision ID                     -->
<!ENTITY % revisionid.content
                       "(#PCDATA |
                         %keyword;)*
">
<!ENTITY % revisionid.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT revisionid    %revisionid.content;>
<!ATTLIST revisionid    %revisionid.attributes;>

 
<!--                    LONG NAME: Start Date                      -->
<!ENTITY % started.content
                       "(((%year;), 
                          ((%month;), 
                           (%day;)?)?) | 
                         ((%month;), 
                          (%day;)?, 
                          (%year;)) | 
                         ((%day;), 
                          (%month;), 
                          (%year;)))"
>
<!ENTITY % started.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT started    %started.content;>
<!ATTLIST started    %started.attributes;>

 
<!--                    LONG NAME: Completion Date                 -->
<!ENTITY % completed.content
                       "(((%year;), 
                          ((%month;), 
                           (%day;)?)?) | 
                         ((%month;), 
                          (%day;)?, 
                          (%year;)) | 
                         ((%day;), 
                          (%month;), 
                          (%year;)))"
>
<!ENTITY % completed.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT completed    %completed.content;>
<!ATTLIST completed    %completed.attributes;>

 
<!--                    LONG NAME: Year                            -->
<!ENTITY % year.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % year.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT year    %year.content;>
<!ATTLIST year    %year.attributes;>

 
<!--                    LONG NAME: Month                           -->
<!ENTITY % month.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % month.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT month    %month.content;>
<!ATTLIST month    %month.attributes;>

 
<!--                    LONG NAME: Day                             -->
<!ENTITY % day.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % day.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT day    %day.content;>
<!ATTLIST day    %day.attributes;>

 
<!--                    LONG NAME: Reviewed                        -->
<!ENTITY % reviewed.content
                       "(((%organization;) | 
                          (%person;))*, 
                         (%revisionid;)?, 
                         (%started;)?, 
                         (%completed;)?, 
                         (%summary;)?, 
                         (%data;)*)"
>
<!ENTITY % reviewed.attributes
             "%data-element-atts;"
>
<!ELEMENT reviewed    %reviewed.content;>
<!ATTLIST reviewed    %reviewed.attributes;>


<!--                    LONG NAME: Editeded                        -->
<!ENTITY % edited.content
                       "(((%organization;) | 
                          (%person;))*, 
                          (%revisionid;)?, 
                          (%started;)?, 
                          (%completed;)?, 
                          (%summary;)?, 
                          (%data;)*)"
>
<!ENTITY % edited.attributes
             "%data-element-atts;"
>
<!ELEMENT edited    %edited.content;>
<!ATTLIST edited    %edited.attributes;>


<!--                    LONG NAME: Tested                          -->
<!ENTITY % tested.content
                       "(((%organization;) | 
                          (%person;))*, 
                          (%revisionid;)?, 
                          (%started;)?, 
                          (%completed;)?, 
                          (%summary;)?, 
                          (%data;)*)"
>
<!ENTITY % tested.attributes
             "%data-element-atts;"
>
<!ELEMENT tested    %tested.content;>
<!ATTLIST tested    %tested.attributes;>


<!--                    LONG NAME: Approved                        -->
<!ENTITY % approved.content
                       "(((%organization;) | 
                          (%person;))*, 
                          (%revisionid;)?, 
                          (%started;)?, 
                          (%completed;)?, 
                          (%summary;)?, 
                          (%data;)*)"
>
<!ENTITY % approved.attributes
             "%data-element-atts;"
>
<!ELEMENT approved    %approved.content;>
<!ATTLIST approved    %approved.attributes;>


<!--                    LONG NAME: Book Event                      -->
<!ENTITY % bookevent.content
                       "((%bookeventtype;)?, 
                         (((%organization;) | 
                           (%person;))*, 
                          (%revisionid;)?, 
                          (%started;)?, 
                          (%completed;)?, 
                          (%summary;)?, 
                          (%data;)*))"
>
<!ENTITY % bookevent.attributes
             "%data-element-atts;"
>
<!ELEMENT bookevent    %bookevent.content;>
<!ATTLIST bookevent    %bookevent.attributes;>

<!--                    LONG NAME: Book Event Type                 -->
<!ENTITY % bookeventtype.content
                       "EMPTY"
>
<!-- Attributes are the same as data-element-atts except that 
     @name is required                                             -->
<!ENTITY % bookeventtype.attributes
             "name 
                        CDATA 
                                  #REQUIRED 
              datatype 
                        CDATA 
                                  #IMPLIED
              value 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local | 
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT bookeventtype    %bookeventtype.content;>
<!ATTLIST bookeventtype    %bookeventtype.attributes;>

<!--                    LONG NAME: Book Part Number                -->
<!ENTITY % bookpartno.content
                       "(%words.cnt;)*"
>
<!ENTITY % bookpartno.attributes
             "%data-element-atts;"
>
<!ELEMENT bookpartno    %bookpartno.content;>
<!ATTLIST bookpartno    %bookpartno.attributes;>


<!--                    LONG NAME: Edition                         -->
<!ENTITY % edition.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % edition.attributes
             "%data-element-atts;"
>
<!ELEMENT edition    %edition.content;>
<!ATTLIST edition    %edition.attributes;>


<!--                    LONG NAME: ISBN Number                     -->
<!ENTITY % isbn.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % isbn.attributes
             "%data-element-atts;"
>
<!ELEMENT isbn    %isbn.content;>
<!ATTLIST isbn    %isbn.attributes;>


<!--                    LONG NAME: Book Number                     -->
<!ENTITY % booknumber.content
                       "(%words.cnt;)*"
>
<!ENTITY % booknumber.attributes
             "%data-element-atts;"
>
<!ELEMENT booknumber    %booknumber.content;>
<!ATTLIST booknumber    %booknumber.attributes;>


<!--                    LONG NAME: Volume                          -->
<!ENTITY % volume.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % volume.attributes
             "%data-element-atts;"
>
<!ELEMENT volume    %volume.content;>
<!ATTLIST volume    %volume.attributes;>


<!--                    LONG NAME: Maintainer                      -->
<!ENTITY % maintainer.content
                       "(((%person;) | 
                          (%organization;))*, 
                         (%data;)*)
">
<!ENTITY % maintainer.attributes
             "%data-element-atts;"
>
<!ELEMENT maintainer    %maintainer.content;>
<!ATTLIST maintainer    %maintainer.attributes;>


<!--                    LONG NAME: Book Rights                     -->
<!ENTITY % bookrights.content
                       "((%copyrfirst;)?, 
                         (%copyrlast;)?,
                         (%bookowner;), 
                         (%bookrestriction;)?, 
                         (%summary;)?)"
>
<!ENTITY % bookrights.attributes
             "%data-element-atts;"
>
<!ELEMENT bookrights    %bookrights.content;>
<!ATTLIST bookrights    %bookrights.attributes;>


<!--                    LONG NAME: First Copyright                 -->
<!ENTITY % copyrfirst.content
                       "(%year;)"
>
<!ENTITY % copyrfirst.attributes
             "%data-element-atts;"
>
<!ELEMENT copyrfirst    %copyrfirst.content;>
<!ATTLIST copyrfirst    %copyrfirst.attributes;>

 
<!--                    LONG NAME: Last Copyright                  -->
<!ENTITY % copyrlast.content
                       "(%year;)"
>
<!ENTITY % copyrlast.attributes
             "%data-element-atts;"
>
<!ELEMENT copyrlast    %copyrlast.content;>
<!ATTLIST copyrlast    %copyrlast.attributes;>


<!--                    LONG NAME: Book Owner                      -->
<!ENTITY % bookowner.content
                       "((%organization;) | 
                         (%person;))* 
 ">
<!ENTITY % bookowner.attributes
             "%data-element-atts;"
>
<!ELEMENT bookowner    %bookowner.content;>
<!ATTLIST bookowner    %bookowner.attributes;>

<!--                    LONG NAME: Book Restriction                -->
<!ENTITY % bookrestriction.content
                       "EMPTY"
>
<!-- Same attributes as data-element-atts, except for @value -->
<!-- 20080128: Removed enumeration for @value for DITA 1.2. Previous values:
               confidential, licensed, restricted, 
               unclassified, -dita-use-conref-target               -->
<!ENTITY % bookrestriction.attributes
             "%univ-atts;
              name 
                        CDATA 
                                  #IMPLIED
              datatype 
                        CDATA 
                                  #IMPLIED
              href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external |
                         local | 
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              outputclass
                        CDATA 
                                  #IMPLIED
              value 
                        CDATA 
                                  #REQUIRED"
>
<!ELEMENT bookrestriction    %bookrestriction.content;>
<!ATTLIST bookrestriction    %bookrestriction.attributes;>

<!--                    LONG NAME: Book Title                      -->
<!ENTITY % booktitle.content
                       "((%booklibrary;)?,
                         (%mainbooktitle;),
                         (%booktitlealt;)*)"
>
<!ENTITY % booktitle.attributes
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
<!ELEMENT booktitle    %booktitle.content;>
<!ATTLIST booktitle    %booktitle.attributes;>


<!-- The following three elements are specialized from <ph>. They are
     titles, which have a more limited content model than phrases. The
     content model here matches title.cnt; that entity is not reused
     in case it is expanded in the future to include something not
     allowed in a phrase.                                          -->
<!--                    LONG NAME: Library Title                   -->
<!ENTITY % booklibrary.content
                       "(#PCDATA | 
                         %basic.ph.noxref; | 
                         %image;)*"
>
<!ENTITY % booklibrary.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT booklibrary    %booklibrary.content;>
<!ATTLIST booklibrary    %booklibrary.attributes;>

 
<!--                    LONG NAME: Main Book Title                 -->
<!ENTITY % mainbooktitle.content
                       "(#PCDATA | 
                         %basic.ph.noxref; | 
                         %image;)*"
>
<!ENTITY % mainbooktitle.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT mainbooktitle    %mainbooktitle.content;>
<!ATTLIST mainbooktitle    %mainbooktitle.attributes;>

 
<!--                    LONG NAME: Alternate Book Title            -->
<!ENTITY % booktitlealt.content
                       "(#PCDATA | 
                         %basic.ph.noxref; | 
                         %image;)*"
>
<!ENTITY % booktitlealt.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT booktitlealt    %booktitlealt.content;>
<!ATTLIST booktitlealt    %booktitlealt.attributes;>


<!--                    LONG NAME: Draft Introduction              -->
<!ENTITY % draftintro.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % draftintro.attributes
             "%chapter-atts;"
>
<!ELEMENT draftintro    %draftintro.content;>
<!ATTLIST draftintro    %draftintro.attributes;>


<!--                    LONG NAME: Book Abstract                   -->
<!ENTITY % bookabstract.content
                       "EMPTY"
>
<!ENTITY % bookabstract.attributes
             "%chapter-atts;"
>
<!ELEMENT bookabstract    %bookabstract.content;>
<!ATTLIST bookabstract    %bookabstract.attributes;>


<!--                    LONG NAME: Dedication                      -->
<!ENTITY % dedication.content
                       "EMPTY"
>
<!ENTITY % dedication.attributes
             "%chapter-atts;"
>
<!ELEMENT dedication    %dedication.content;>
<!ATTLIST dedication    %dedication.attributes;>


<!--                    LONG NAME: Preface                         -->
<!ENTITY % preface.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % preface.attributes
             "%chapter-atts;"
>
<!ELEMENT preface    %preface.content;>
<!ATTLIST preface    %preface.attributes;>


<!--                    LONG NAME: Chapter                         -->
<!ENTITY % chapter.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % chapter.attributes
             "%chapter-atts;"
>
<!ELEMENT chapter    %chapter.content;>
<!ATTLIST chapter    %chapter.attributes;>


<!--                    LONG NAME: Part                            -->
<!ENTITY % part.content
                       "((%topicmeta;)?,
                         ((%chapter;) | 
                          (%topicref;))* )"
>
<!ENTITY % part.attributes
             "%chapter-atts;"
>
<!ELEMENT part    %part.content;>
<!ATTLIST part    %part.attributes;>


<!--                    LONG NAME: Appendix                        -->
<!ENTITY % appendix.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % appendix.attributes
             "%chapter-atts;"
>
<!ELEMENT appendix    %appendix.content;>
<!ATTLIST appendix    %appendix.attributes;>


<!--                    LONG NAME: Appendices                      -->
<!ENTITY % appendices.content
                       "((%topicmeta;)?, 
                         (%appendix;)*)"
>
<!ENTITY % appendices.attributes
             "%chapter-atts;"
>
<!ELEMENT appendices    %appendices.content;>
<!ATTLIST appendices    %appendices.attributes;>

<!--                    LONG NAME: Notices                         -->
<!ENTITY % notices.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % notices.attributes
             "%chapter-atts;"
>
<!ELEMENT notices    %notices.content;>
<!ATTLIST notices    %notices.attributes;>


<!--                    LONG NAME: Amendments                      -->
<!ENTITY % amendments.content
                       "EMPTY"
>
<!ENTITY % amendments.attributes
             "%chapter-atts;"
>
<!ELEMENT amendments    %amendments.content;>
<!ATTLIST amendments    %amendments.attributes;>


<!--                    LONG NAME: Colophon                        -->
<!ENTITY % colophon.content
                       "EMPTY"
>
<!ENTITY % colophon.attributes
             "%chapter-atts;"
>
<!ELEMENT colophon    %colophon.content;>
<!ATTLIST colophon    %colophon.attributes;>


<!--                    LONG NAME: Book Lists                      -->
<!ENTITY % booklists.content
                       "((%abbrevlist;) |
                         (%bibliolist;) |
                         (%booklist;) |
                         (%figurelist;) |
                         (%glossarylist;) |
                         (%indexlist;) |
                         (%tablelist;) |
                         (%trademarklist;) |
                         (%toc;))*"
>
<!ENTITY % booklists.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %topicref-atts;
              %id-atts;
              %select-atts;
              %localization-atts;"
>
<!ELEMENT booklists    %booklists.content;>
<!ATTLIST booklists    %booklists.attributes;>


<!--                    LONG NAME: Table of Contents               -->
<!ENTITY % toc.content
                       "EMPTY"
>
<!ENTITY % toc.attributes
             "%chapter-atts;"
>
<!ELEMENT toc    %toc.content;>
<!ATTLIST toc    %toc.attributes;>


<!--                    LONG NAME: Figure List                     -->
<!ENTITY % figurelist.content
                       "EMPTY"
>
<!ENTITY % figurelist.attributes
             "%chapter-atts;"
>
<!ELEMENT figurelist    %figurelist.content;>
<!ATTLIST figurelist    %figurelist.attributes;>


<!--                    LONG NAME: Table List                      -->
<!ENTITY % tablelist.content
                       "EMPTY"
>
<!ENTITY % tablelist.attributes
             "%chapter-atts;"
>
<!ELEMENT tablelist    %tablelist.content;>
<!ATTLIST tablelist    %tablelist.attributes;>


<!--                    LONG NAME: Abbreviation List               -->
<!ENTITY % abbrevlist.content
                       "EMPTY"
>
<!ENTITY % abbrevlist.attributes
             "%chapter-atts;"
>
<!ELEMENT abbrevlist    %abbrevlist.content;>
<!ATTLIST abbrevlist    %abbrevlist.attributes;>


<!--                    LONG NAME: Trademark List                  -->
<!ENTITY % trademarklist.content
                       "EMPTY"
>
<!ENTITY % trademarklist.attributes
             "%chapter-atts;"
>
<!ELEMENT trademarklist    %trademarklist.content;>
<!ATTLIST trademarklist    %trademarklist.attributes;>


<!--                    LONG NAME: Bibliography List               -->
<!ENTITY % bibliolist.content
                       "EMPTY"
>
<!ENTITY % bibliolist.attributes
             "%chapter-atts;"
>
<!ELEMENT bibliolist    %bibliolist.content;>
<!ATTLIST bibliolist    %bibliolist.attributes;>


<!--                    LONG NAME: Glossary List                   -->
<!ENTITY % glossarylist.content
                       "((%topicmeta;)?, 
                         (%topicref;)*)"
>
<!ENTITY % glossarylist.attributes
             "%chapter-atts;"
>
<!ELEMENT glossarylist    %glossarylist.content;>
<!ATTLIST glossarylist    %glossarylist.attributes;>


<!--                    LONG NAME: Index List                      -->
<!ENTITY % indexlist.content
                       "EMPTY"
>
<!ENTITY % indexlist.attributes
             "%chapter-atts;"
>
<!ELEMENT indexlist    %indexlist.content;>
<!ATTLIST indexlist    %indexlist.attributes;>


<!--                    LONG NAME: Book List                       -->
<!ENTITY % booklist.content
                       "EMPTY"
>
<!ENTITY % booklist.attributes
             "%chapter-atts;"
>
<!ELEMENT booklist    %booklist.content;>
<!ATTLIST booklist    %booklist.attributes;>


 
<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST bookmap     %global-atts; class CDATA "- map/map bookmap/bookmap ">
<!ATTLIST abbrevlist  %global-atts; class CDATA "- map/topicref bookmap/abbrevlist ">
<!ATTLIST amendments  %global-atts; class CDATA "- map/topicref bookmap/amendments ">
<!ATTLIST appendices  %global-atts; class CDATA "- map/topicref bookmap/appendices ">
<!ATTLIST appendix    %global-atts; class CDATA "- map/topicref bookmap/appendix ">
<!ATTLIST approved    %global-atts; class CDATA "- topic/data bookmap/approved ">
<!ATTLIST backmatter  %global-atts; class CDATA "- map/topicref bookmap/backmatter ">
<!ATTLIST bibliolist  %global-atts; class CDATA "- map/topicref bookmap/bibliolist ">
<!ATTLIST bookabstract %global-atts; class CDATA "- map/topicref bookmap/bookabstract ">
<!ATTLIST bookchangehistory %global-atts; class CDATA "- topic/data bookmap/bookchangehistory ">
<!ATTLIST bookevent   %global-atts; class CDATA "- topic/data bookmap/bookevent ">
<!ATTLIST bookeventtype %global-atts; class CDATA "- topic/data bookmap/bookeventtype ">
<!ATTLIST bookid      %global-atts; class CDATA "- topic/data bookmap/bookid ">
<!ATTLIST booklibrary %global-atts;  class CDATA "- topic/ph bookmap/booklibrary ">
<!ATTLIST booklist    %global-atts; class CDATA "- map/topicref bookmap/booklist ">
<!ATTLIST booklists   %global-atts; class CDATA "- map/topicref bookmap/booklists ">
<!ATTLIST bookmeta    %global-atts; class CDATA "- map/topicmeta bookmap/bookmeta ">
<!ATTLIST booknumber  %global-atts; class CDATA "- topic/data bookmap/booknumber ">
<!ATTLIST bookowner   %global-atts; class CDATA "- topic/data bookmap/bookowner ">
<!ATTLIST bookpartno  %global-atts; class CDATA "- topic/data bookmap/bookpartno ">
<!ATTLIST bookrestriction %global-atts; class CDATA "- topic/data bookmap/bookrestriction ">
<!ATTLIST bookrights  %global-atts; class CDATA "- topic/data bookmap/bookrights ">
<!ATTLIST booktitle   %global-atts;  class CDATA "- topic/title bookmap/booktitle ">
<!ATTLIST booktitlealt %global-atts;  class CDATA "- topic/ph bookmap/booktitlealt ">
<!ATTLIST chapter     %global-atts; class CDATA "- map/topicref bookmap/chapter ">
<!ATTLIST colophon    %global-atts; class CDATA "- map/topicref bookmap/colophon ">
<!ATTLIST completed   %global-atts; class CDATA "- topic/ph bookmap/completed ">
<!ATTLIST copyrfirst  %global-atts; class CDATA "- topic/data bookmap/copyrfirst ">
<!ATTLIST copyrlast   %global-atts; class CDATA "- topic/data bookmap/copyrlast ">
<!ATTLIST day         %global-atts; class CDATA "- topic/ph bookmap/day ">
<!ATTLIST dedication  %global-atts; class CDATA "- map/topicref bookmap/dedication ">
<!ATTLIST draftintro  %global-atts; class CDATA "- map/topicref bookmap/draftintro ">
<!ATTLIST edited      %global-atts; class CDATA "- topic/data bookmap/edited ">
<!ATTLIST edition     %global-atts; class CDATA "- topic/data bookmap/edition ">
<!ATTLIST figurelist  %global-atts; class CDATA "- map/topicref bookmap/figurelist ">
<!ATTLIST frontmatter %global-atts; class CDATA "- map/topicref bookmap/frontmatter ">
<!ATTLIST glossarylist %global-atts; class CDATA "- map/topicref bookmap/glossarylist ">
<!ATTLIST indexlist   %global-atts; class CDATA "- map/topicref bookmap/indexlist ">
<!ATTLIST isbn        %global-atts; class CDATA "- topic/data bookmap/isbn ">
<!ATTLIST mainbooktitle %global-atts;  class CDATA "- topic/ph bookmap/mainbooktitle ">
<!ATTLIST maintainer  %global-atts; class CDATA "- topic/data bookmap/maintainer ">
<!ATTLIST month       %global-atts; class CDATA "- topic/ph bookmap/month ">
<!ATTLIST notices     %global-atts; class CDATA "- map/topicref bookmap/notices ">
<!ATTLIST organization %global-atts; class CDATA "- topic/data bookmap/organization ">
<!ATTLIST part        %global-atts; class CDATA "- map/topicref bookmap/part ">
<!ATTLIST person      %global-atts; class CDATA "- topic/data bookmap/person ">
<!ATTLIST preface     %global-atts; class CDATA "- map/topicref bookmap/preface ">
<!ATTLIST printlocation %global-atts; class CDATA "- topic/data bookmap/printlocation ">
<!ATTLIST published   %global-atts; class CDATA "- topic/data bookmap/published ">
<!ATTLIST publisherinformation %global-atts; class CDATA "- topic/publisher bookmap/publisherinformation ">
<!ATTLIST publishtype %global-atts; class CDATA "- topic/data bookmap/publishtype ">
<!ATTLIST reviewed    %global-atts; class CDATA "- topic/data bookmap/reviewed ">
<!ATTLIST revisionid  %global-atts; class CDATA "- topic/ph bookmap/revisionid ">
<!ATTLIST started     %global-atts; class CDATA "- topic/ph bookmap/started ">
<!ATTLIST summary     %global-atts; class CDATA "- topic/ph bookmap/summary ">
<!ATTLIST tablelist   %global-atts; class CDATA "- map/topicref bookmap/tablelist ">
<!ATTLIST tested      %global-atts; class CDATA "- topic/data bookmap/tested ">
<!ATTLIST toc         %global-atts; class CDATA "- map/topicref bookmap/toc ">
<!ATTLIST trademarklist %global-atts; class CDATA "- map/topicref bookmap/trademarklist ">
<!ATTLIST volume      %global-atts; class CDATA "- topic/data bookmap/volume ">
<!ATTLIST year        %global-atts; class CDATA "- topic/ph bookmap/year ">

<!-- ================== End book map ============================= -->