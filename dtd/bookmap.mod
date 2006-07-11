<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- NOTES: * data element includes <ph>. Must add <ph> to the map model somehow.
              also means adding ph contents, such as xref. One element here is based on xref.
              How do we declare ph.cnt and xref.cnt? -->
<!--        * Design doc shows colophon in bookmeta, but it is a topicref, which cannot go in topicmeta.
              For now it is still directly in bookmap.   -->
<!--        * Derivation of bkrights changed from topic/p to topic/data -->
<!--        * Derivation of contact, organization changed from topic/p to topic/ph -->
<!--        * Derivation of bkprintloc changed from topic/p to topic/ph -->
<!--        * Derivation of affiliations changed from topic/note to topic/ph      -->
<!--        * Derivation of address changed from topic/lines to topic/ph. May need to
              add elements for each line of the address, otherwise it will run together.      -->
<!--        * firstname element changed to givenname      -->
<!--        * lastname element changed to familyname      -->
<!--        * resource content changed from xreftext.cnt to #PCDATA for now   -->
<!--        * summary content changed from ph.cnt to #PCDATA for now   -->
<!--        * changed URL in design doc to URLPhone   -->
<!--        * changed office in design doc to officePhone   -->
<!--        * replaced author with authorInformation in topicmeta-based elements   -->
<!--        * replaced publisher with publisherInformation in topicmeta-based elements   -->
<!--        * Derivation of bkpublishtype changed from topic/state to topic/data;
              flipped value and name attributes      -->
<!--        * Derivation of bkrestriction changed from topic/state to topic/data;
              flipped value and name attributes      -->
<!--        * element bkhistory changed to bkChangeHistory      -->
<!--        * Derivation of bkhistory (bkChangeHistory) changed from topic/ul to topic/data      -->
<!--        * Design shows contents of bkChangeHistory unordered, changed to ordered     -->
<!--        * Derivation of bkpublished, bkreviewed, bkedited, bktested, 
              bkapproved, bkevent, bkeventtype changed from topic/li to topic/data -->
<!--        * Design doc drops bkauthored - put back in bkChangeHistory? move bkpublished
              back to bkChangeHistory? currently replacing bkinfo/bkpublished and publisher in the map
              with publisherInformation     -->
<!--        * Derivation of bkid changed from topic/ul to topic/data -->
<!--        * bkvolume contained bkvolid, bklibrary. Changed to #PCDATA; bklibrary is part of title,
              coming in a domain. -->
              
<!--        * Removed specialnotices -->
<!--        * Add front and back matter; simplify part contents -->

<!-- Move to XNAL domain: -->
             
                               
<!ENTITY % bookmap         "bookmap">
<!ENTITY % bookmeta        "bookmeta">
<!ENTITY % bookid          "bookid">
<!ENTITY % publisherinformation "publisherinformation">
<!ENTITY % bookchangehistory "bookchangehistory">

<!ENTITY % bookrights        "bookrights">
<!ENTITY % copyrfirst    "copyrfirst">
<!ENTITY % copyrlast     "copyrlast">
<!ENTITY % bookowner         "bookowner">
<!ENTITY % bookrestriction   "bookrestriction">
<!ENTITY % printlocation      "printlocation">
<!ENTITY % published     "published">
<!ENTITY % publishtype   "publishtype">
<!ENTITY % revisionid    "revisionid">
<!ENTITY % reviewed      "reviewed">
<!ENTITY % edited        "edited">
<!ENTITY % tested        "tested">
<!ENTITY % approved      "approved">
<!ENTITY % bookevent         "bookevent">
<!ENTITY % bookeventtype     "bookeventtype">
<!ENTITY % bookpartno        "bookpartno">
<!ENTITY % edition       "edition">
<!ENTITY % isbn            "isbn">
<!ENTITY % booknumber           "booknumber">
<!ENTITY % volume        "volume">
<!--<!ENTITY % bkvolid         "bkvolid">-->
<!ENTITY % library       "library">
<!ENTITY % maintainer    "maintainer">

<!ENTITY % started         "started">
<!ENTITY % completed       "completed">
<!--<!ENTITY % date            "date">-->
<!ENTITY % day             "day">
<!ENTITY % month           "month">
<!ENTITY % year            "year">
<!--<!ENTITY % contact         "contact">-->
<!ENTITY % summary         "summary">
<!--<!ENTITY % affiliations    "affiliations">-->

<!ENTITY % booktitle      "booktitle"                                    >
<!ENTITY % booklibrary    "booklibrary"                                  >
<!ENTITY % mainbooktitle  "mainbooktitle"                                >
<!ENTITY % booktitlealt   "booktitlealt"                                 >

<!--<!ENTITY % booktitle       "booktitle">
<!ENTITY % booksubtitle    "booksubtitle">
<!ENTITY % bookabstract    "bookabstract">-->
<!ENTITY % frontmatter        "frontmatter">
<!ENTITY % backmatter        "backmatter">
<!ENTITY % draftintro      "draftintro">
<!ENTITY % abstract        "abstract">
<!ENTITY % dedication      "dedication">
<!ENTITY % preface         "preface">
<!ENTITY % chapter         "chapter">
<!ENTITY % part            "part">
<!ENTITY % appendix        "appendix">
<!ENTITY % notices         "notices">
<!ENTITY % amendments      "amendments">
<!ENTITY % colophon        "colophon">

<!ENTITY % booklists       "booklists">
<!ENTITY % toc             "toc">
<!ENTITY % figurelist      "figurelist">
<!ENTITY % tablelist       "tablelist">
<!ENTITY % abbrevlist      "abbrevlist">
<!ENTITY % trademarklist   "trademarklist">
<!ENTITY % bibliolist      "bibliolist">
<!ENTITY % glossarylist    "glossarylist">
<!ENTITY % indexlist       "indexlist">
<!ENTITY % booklist        "booklist">



<!ENTITY included-domains "">

<!-- Currently: same as topicref, minus @query -->
<!ENTITY % chapter-atts 
            'navtitle   CDATA                             #IMPLIED
             href       CDATA                             #IMPLIED
             keyref     CDATA                             #IMPLIED
             copy-to    CDATA                             #IMPLIED
             outputclass 
                        CDATA                             #IMPLIED
             %topicref-atts;
             %univ-atts;                                          '>

<!-- leave off title attribute once there is a title element? -->
<!ELEMENT bookmap (((%title;) | (%booktitle;))?, (%bookmeta;)?, 
                   (%frontmatter;)?,
                   (%chapter;)*, (%part;)*, (%appendix;)*,
                   (%backmatter;)?,
                   (%reltable;)*)>
<!ATTLIST bookmap
             title      CDATA                             #IMPLIED
             id         ID                                #IMPLIED
             anchorref  CDATA                             #IMPLIED
             %localization-atts;
             %arch-atts;
             domains    CDATA                  "&included-domains;" 
             %topicref-atts;
             %select-atts;                                           >

<!--                    LONG NAME: Book Metadata                   -->
<!ELEMENT  bookmeta    ((%linktext;)?, (%searchtitle;)?, 
                         (%shortdesc;)?, (%author;)*, (%source;)?, 
                         (%critdates;)?, (%permissions;)?, 
                         (%audience;)*, (%category;)*, 
                         (%keywords;)*, (%prodinfo;)*, (%othermeta;)*, 
                         (%resourceid;)*, (%bookid;)?,
                         (%bookchangehistory;)*,
                         (%bookrights;)*,
                         (%publisherinformation;)*,
                         (%data;)*)                            > <!-- data element at end -->
<!ATTLIST  bookmeta
             lockmeta   (yes | no)                        #IMPLIED
             %univ-atts;                                             >

<!--                    LONG NAME: Front Matter                 -->
<!ELEMENT  frontmatter    (%booklists; | %notices; | %dedication; | %colophon; |
                           %abstract; | %draftintro; | %preface; | %topicref;)*  >
<!ATTLIST  frontmatter
             keyref     CDATA                             #IMPLIED
             query      CDATA                             #IMPLIED
             copy-to    CDATA                             #IMPLIED
             outputclass 
                        CDATA                             #IMPLIED
             %topicref-atts;
             %univ-atts;                                             >

<!--                    LONG NAME: Front Matter                 -->
<!ELEMENT  backmatter    (%booklists; | %notices; | %dedication; | %colophon; |
                           %amendments; | %topicref;)*  >
<!ATTLIST  backmatter
             keyref     CDATA                             #IMPLIED
             query      CDATA                             #IMPLIED
             copy-to    CDATA                             #IMPLIED
             outputclass 
                        CDATA                             #IMPLIED
             %topicref-atts;
             %univ-atts;                                             >

<!ELEMENT publisherinformation      ((%printlocation;)*, (%published;)*,(%data;)*)>
<!ATTLIST publisherinformation    %data-element-atts;>

<!ELEMENT bookchangehistory      ((%reviewed;)*, (%edited;)*, (%tested;)*, (%approved;)*, (%bookevent;)*)>
<!ATTLIST bookchangehistory    %data-element-atts;>

<!ELEMENT bookid           ((%bookpartno;)?, (%edition;)?, (%isbn;)?, (%booknumber;)?, (%volume;)*, (%maintainer;)?)>
<!ATTLIST bookid           %data-element-atts;>



<!--<!ELEMENT contact      (%data;)*  >  
<!ATTLIST contact              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >-->
                        
<!ELEMENT summary         (%words.cnt;)*>
<!ATTLIST summary         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!--
<!ELEMENT affiliations    ((%organizationnamedetails;)*)>
<!ATTLIST affiliations    keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>-->

<!ELEMENT printlocation      (#PCDATA)*>
<!ATTLIST printlocation         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT published     ((%data;)*, (%publishtype;)?, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST published    %data-element-atts;>

<!ELEMENT publishtype   EMPTY>
<!-- Same attributes as <data>, except that @value is enumerated and required -->
<!ATTLIST publishtype   %univ-atts;
             name        CDATA #IMPLIED
             label       CDATA #IMPLIED
             datatype    CDATA #IMPLIED
             href        CDATA #IMPLIED
             format      CDATA #IMPLIED
             type        CDATA #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass CDATA #IMPLIED
             value (beta|limited|general) #REQUIRED
>
<!ELEMENT revisionid    (#PCDATA)*>
<!ATTLIST revisionid    keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT started         (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST started         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT completed       (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST completed       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!--<!ELEMENT date            (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST date            keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>-->
<!ELEMENT year            (#PCDATA)*>
<!ATTLIST year            keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT month           (#PCDATA)*>
<!ATTLIST month           keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT day             (#PCDATA)*>
<!ATTLIST day             keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT reviewed      ((%data;)*, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST reviewed      %data-element-atts;>

<!-- DocBook:   bookinfo / editor
     IBMIDDoc:  prolog / critdates / critdate
     -->
<!ELEMENT edited        ((%data;)*, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST edited        %data-element-atts;>

<!ELEMENT tested        ((%data;)*, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST tested        %data-element-atts;>

<!-- DocBook:   ???
     IBMIDDoc:  prolog / approvers
     -->
<!ELEMENT approved      ((%data;)*, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST approved      %data-element-atts;>

<!-- DocBook:   bookinfo / othercredit
     IBMIDDoc:  ???
     -->
<!ELEMENT bookevent         ((%bookeventtype;)?, (%data;)*, (%revisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bookevent         %data-element-atts;>

<!ELEMENT bookeventtype     EMPTY>
<!-- Attributes are the same as on <data> except that @name is required -->
<!ATTLIST bookeventtype     %univ-atts;
             label       CDATA #IMPLIED
             datatype    CDATA #IMPLIED
             value       CDATA #IMPLIED
             href        CDATA #IMPLIED
             format      CDATA #IMPLIED
             type        CDATA #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass CDATA #IMPLIED
             name CDATA #REQUIRED
>

<!-- DocBook:   bookinfo / invpartnumber
     IBMIDDoc:  prolog / ibmbibentry / ibmpartnum
     -->
<!ELEMENT bookpartno        (#PCDATA)*>
<!ATTLIST bookpartno        %data-element-atts;>

<!-- DocBook:   bookinfo / edition
     IBMIDDoc:  ???
     -->
<!ELEMENT edition       (#PCDATA)*>
<!ATTLIST edition       %data-element-atts;>

<!-- DocBook:   bookinfo / isbn
     IBMIDDoc:  prolog / ibmbibentry / isbn
     -->
<!ELEMENT isbn            (#PCDATA)*>
<!ATTLIST isbn            %data-element-atts;>
<!-- DocBook:   bookinfo / pubsnumber
     IBMIDDoc:  prolog / ibmbibentry / ibmdocnum
     -->
<!ELEMENT booknumber           (#PCDATA)*>
<!ATTLIST booknumber           %data-element-atts;>

<!ELEMENT volume        (#PCDATA)>
<!ATTLIST volume        %data-element-atts;>

<!-- DocBook:   bookinfo / volumenum
     IBMIDDoc:  prolog / ibmbibentry / volid
     -->
<!--<!ELEMENT bkvolid         (#PCDATA)*>
<!ATTLIST bkvolid         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>-->
<!-- DocBook:   bookinfo / ???
     IBMIDDoc:  prolog / ibmbibentry / doctitle / library / titleblk / title
     -->
<!-- should be xreftext.cnt -->
<!--<!ELEMENT library       (#PCDATA)*>  
<!ATTLIST library       href          CDATA   #IMPLIED
                          keyref        CDATA #IMPLIED
                          type          CDATA   #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope         CDATA   "external"
                          outputclass   CDATA   #IMPLIED
>-->
<!-- DocBook:   bookinfo / ???
     IBMIDDoc:  prolog / maintainer
     -->
<!ELEMENT maintainer    (%data;)*>
<!ATTLIST maintainer    %data-element-atts;>

<!ELEMENT bookrights        ((%copyrfirst;)?, (%copyrlast;)?, (%bookowner;), (%bookrestriction;)?, (%summary;)?)>
<!ATTLIST bookrights        %data-element-atts;>

<!ELEMENT copyrfirst    (%year;)>
<!ATTLIST copyrfirst    keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT copyrlast     (%year;)>
<!ATTLIST copyrlast     keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / copyright / holder
     IBMIDDoc:  prolog / owners
     -->
<!ELEMENT bookowner         (%data;)*>
<!ATTLIST bookowner         %data-element-atts;>

<!-- DocBook:   ???
     IBMIDDoc:  ibmiddoc @ classif
                ibmiddoc @ sec
     -->
<!ELEMENT bookrestriction   EMPTY>
<!-- Same attributes as data, except for @value -->
<!ATTLIST bookrestriction   %univ-atts;
             name        CDATA #IMPLIED
             label       CDATA #IMPLIED
             datatype    CDATA #IMPLIED
             href        CDATA #IMPLIED
             format      CDATA #IMPLIED
             type        CDATA #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass CDATA #IMPLIED
             value (confidential|restricted|licensed|unclassified) #REQUIRED
>

<!ELEMENT booktitle         ((%booklibrary;)?,(%mainbooktitle;),(%booktitlealt;)*)                               > 
<!ATTLIST booktitle         %id-atts;
                    %localization-atts;
                    outputclass CDATA                            #IMPLIED    >

<!-- The following three elements are specialized from <ph>. They reflect titles,
     which have a more limited content model than phrases. The content model here
     matches title.cnt; that entity is not reused in case it is expanded in the
     future to include something not allowed in a phrase.                   -->
<!ELEMENT booklibrary            (#PCDATA | %basic.ph.noxref; | %image;)*                                  >  
<!ATTLIST booklibrary              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass CDATA                            #IMPLIED    >
<!ELEMENT mainbooktitle            (#PCDATA | %basic.ph.noxref; | %image;)*                                  >  
<!ATTLIST mainbooktitle              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass CDATA                            #IMPLIED    >
<!ELEMENT booktitlealt            (#PCDATA | %basic.ph.noxref; | %image;)*                                  >  
<!ATTLIST booktitlealt              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass CDATA                            #IMPLIED    >

<!-- roles with contained topicrefs have an optional href attribute
     and topicmeta -->
<!ELEMENT draftintro ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST draftintro  %chapter-atts;   >

<!-- roles without contained topicrefs have a required href attribute
     and no topicmeta -->
<!ELEMENT abstract EMPTY>
<!ATTLIST abstract    %chapter-atts;    >

<!ELEMENT dedication EMPTY>
<!ATTLIST dedication %chapter-atts;    >

<!ELEMENT preface ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST preface %chapter-atts;    >

<!ELEMENT chapter ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST chapter %chapter-atts;    >

<!ELEMENT part ((%topicmeta;)?, ((%topicref;) | (%chapter;))* )>
<!ATTLIST part   %chapter-atts;    >

<!ELEMENT appendix ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST appendix %chapter-atts;    >

<!ELEMENT notices ((%topicmeta;)?, (%topicref;)*)>
<!ATTLIST notices %chapter-atts;    >

<!ELEMENT amendments EMPTY>
<!ATTLIST amendments %chapter-atts;    >

<!ELEMENT colophon EMPTY>
<!ATTLIST colophon %chapter-atts;    >

<!ELEMENT booklists (
                     (%toc;) |
                     (%figurelist;) |
                     (%tablelist;) |
                     (%abbrevlist;) |
                     (%trademarklist;) |
                     (%bibliolist;) |
                     (%glossarylist;) |
                     (%indexlist;) |
                     (%booklist;)
                    )*>
<!-- Similar to topicgroup: same as chapter-atts, minus href and navtitle -->
<!ATTLIST booklists
             keyref     CDATA                             #IMPLIED
             copy-to    CDATA                             #IMPLIED
             %topicref-atts;
             %id-atts;
             %select-atts;
             %localization-atts;                                  >

<!ELEMENT toc EMPTY>
<!ATTLIST toc %chapter-atts;    >

<!ELEMENT figurelist EMPTY>
<!ATTLIST figurelist %chapter-atts;    >

<!ELEMENT tablelist EMPTY>
<!ATTLIST tablelist %chapter-atts;    >

<!ELEMENT abbrevlist EMPTY>
<!ATTLIST abbrevlist %chapter-atts;    >

<!ELEMENT trademarklist EMPTY>
<!ATTLIST trademarklist %chapter-atts;    >

<!ELEMENT bibliolist EMPTY>
<!ATTLIST bibliolist %chapter-atts;    >

<!ELEMENT glossarylist ((%topicmeta;), (%topicref;))*>
<!ATTLIST glossarylist %chapter-atts;    >

<!ELEMENT indexlist EMPTY>
<!ATTLIST indexlist %chapter-atts;    >

<!-- Single booklist, available for specialization -->                     
<!ELEMENT booklist EMPTY>
<!ATTLIST booklist %chapter-atts;    >

                     
<!ATTLIST bookmap %global-atts;
    class CDATA "- map/map bookmap/bookmap ">
<!ATTLIST bookmeta %global-atts;
    class CDATA "- map/topicmeta bookmap/bookmeta ">
<!ATTLIST booktitle     %global-atts;  class CDATA "- topic/title bookmap/booktitle ">
<!ATTLIST booklibrary     %global-atts;  class CDATA "- topic/ph bookmap/booklibrary ">
<!ATTLIST mainbooktitle     %global-atts;  class CDATA "- topic/ph bookmap/mainbooktitle ">
<!ATTLIST booktitlealt     %global-atts;  class CDATA "- topic/ph bookmap/booktitlealt ">

<!ATTLIST frontmatter %global-atts;
    class CDATA "- map/topicref bookmap/frontmatter ">
<!ATTLIST backmatter %global-atts;
    class CDATA "- map/topicref bookmap/backmatter ">
<!ATTLIST draftintro %global-atts;
    class CDATA "- map/topicref bookmap/draftintro ">
<!ATTLIST abstract %global-atts;
    class CDATA "- map/topicref bookmap/abstract ">
<!ATTLIST dedication %global-atts;
    class CDATA "- map/topicref bookmap/dedication ">
<!ATTLIST preface %global-atts;
    class CDATA "- map/topicref bookmap/preface ">
<!ATTLIST chapter %global-atts;
    class CDATA "- map/topicref bookmap/chapter ">
<!ATTLIST part %global-atts;
    class CDATA "- map/topicref bookmap/part ">
<!ATTLIST appendix %global-atts;
    class CDATA "- map/topicref bookmap/appendix ">
<!ATTLIST notices %global-atts;
    class CDATA "- map/topicref bookmap/notices ">
<!ATTLIST amendments %global-atts;
    class CDATA "- map/topicref bookmap/amendments ">
<!ATTLIST colophon %global-atts;
    class CDATA "- map/topicref bookmap/colophon ">

<!ATTLIST booklists %global-atts;
    class CDATA "- map/topicref bookmap/booklists ">

<!ATTLIST toc %global-atts;
    class CDATA "- map/topicref bookmap/toc ">
<!ATTLIST figurelist %global-atts;
    class CDATA "- map/topicref bookmap/figurelist ">
<!ATTLIST tablelist %global-atts;
    class CDATA "- map/topicref bookmap/tablelist ">
<!ATTLIST abbrevlist %global-atts;
    class CDATA "- map/topicref bookmap/abbrevlist ">
<!ATTLIST trademarklist %global-atts;
    class CDATA "- map/topicref bookmap/trademarklist ">
<!ATTLIST bibliolist %global-atts;
    class CDATA "- map/topicref bookmap/bibliolist ">
<!ATTLIST glossarylist %global-atts;
    class CDATA "- map/topicref bookmap/glossarylist ">
<!ATTLIST indexlist %global-atts;
    class CDATA "- map/topicref bookmap/indexlist ">
<!ATTLIST booklist %global-atts;
    class CDATA "- map/topicref bookmap/booklist ">


<!--<!ATTLIST affiliations %global-atts;
        class CDATA "- topic/ph bookmap/affiliations ">-->
<!ATTLIST summary %global-atts;
        class CDATA "- topic/ph bookmap/summary ">
<!ATTLIST publisherinformation %global-atts;
        class CDATA "- topic/data bookmap/publisherinformation ">
<!--<!ATTLIST contact %global-atts;
        class CDATA "- topic/ph bookmap/contact ">-->
<!ATTLIST printlocation %global-atts;
        class CDATA "- topic/ph bookmap/printlocation ">
<!ATTLIST published %global-atts;
        class CDATA "- topic/data bookmap/published ">
<!ATTLIST publishtype %global-atts;
        class CDATA "- topic/data bookmap/publishtype ">
<!ATTLIST revisionid %global-atts;
        class CDATA "- topic/ph bookmap/revisionid ">
<!ATTLIST started %global-atts;
        class CDATA "- topic/ph bookmap/started ">
<!ATTLIST completed %global-atts;
        class CDATA "- topic/ph bookmap/completed ">
<!--<!ATTLIST date %global-atts;
        class CDATA "- topic/ph bookmap/date ">-->
<!ATTLIST day %global-atts;
        class CDATA "- topic/ph bookmap/day ">
<!ATTLIST month %global-atts;
        class CDATA "- topic/ph bookmap/month ">
<!ATTLIST year %global-atts;
        class CDATA "- topic/ph bookmap/year ">
<!ATTLIST bookchangehistory %global-atts;
        class CDATA "- topic/data bookmap/bookchangehistory ">
<!ATTLIST reviewed %global-atts;
        class CDATA "- topic/data bookmap/reviewed ">
<!ATTLIST edited %global-atts;
        class CDATA "- topic/data bookmap/edited ">
<!ATTLIST tested %global-atts;
        class CDATA "- topic/data bookmap/tested ">
<!ATTLIST approved %global-atts;
        class CDATA "- topic/data bookmap/approved ">
<!ATTLIST bookevent %global-atts;
        class CDATA "- topic/data bookmap/bookevent ">
<!ATTLIST bookeventtype %global-atts;
        class CDATA "- topic/data bookmap/bookeventtype ">
<!ATTLIST bookid %global-atts;
        class CDATA "- topic/data bookmap/bookid ">
<!ATTLIST bookpartno %global-atts;
        class CDATA "- topic/data bookmap/bookpartno ">
<!ATTLIST edition %global-atts;
        class CDATA "- topic/data bookmap/edition ">
<!ATTLIST isbn %global-atts;
        class CDATA "- topic/data bookmap/isbn ">
<!ATTLIST booknumber %global-atts;
        class CDATA "- topic/data bookmap/booknumber ">
<!ATTLIST volume %global-atts;
        class CDATA "- topic/data bookmap/volume ">
<!--<!ATTLIST bkvolid %global-atts;
        class CDATA "- topic/ph bookmap/bkvolid ">
<!ATTLIST library %global-atts;
        class CDATA "- topic/xref bookmap/library ">-->
<!ATTLIST maintainer %global-atts;
        class CDATA "- topic/data bookmap/maintainer ">
<!ATTLIST bookrights %global-atts;
        class CDATA "- topic/data bookmap/bookrights ">
<!ATTLIST copyrfirst %global-atts;
        class CDATA "- topic/ph bookmap/copyrfirst ">
<!ATTLIST copyrlast %global-atts;
        class CDATA "- topic/ph bookmap/copyrlast ">
<!ATTLIST bookowner %global-atts;
        class CDATA "- topic/data bookmap/bookowner "> <!-- changed from note -->
<!ATTLIST bookrestriction %global-atts;
        class CDATA "- topic/data bookmap/bookrestriction ">
