<!ENTITY % bookmap         "bookmap">
<!ENTITY % bkbasicinfo     "bkbasicinfo">
<!ENTITY % bkinfo          "bkinfo">
<!ENTITY % booktitle       "booktitle">
<!ENTITY % booksubtitle    "booksubtitle">
<!ENTITY % bookabstract    "bookabstract">
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
<!ENTITY % abbrevlist      "abbrevlist">
<!ENTITY % trademarklist   "trademarklist">
<!ENTITY % bibliolist      "bibliolist">
<!ENTITY % glossarylist    "glossarylist">
<!ENTITY % indexlist       "indexlist">
<!ENTITY % divinfo         "divinfo">
<!ENTITY % divtitle        "divtitle">

<!ENTITY included-domains "">

<!-- DEFERRED FOR NOW
<!ELEMENT bookmap (... , (%booklists;)?)>
-->
<!ELEMENT bookmap (((%bkbasicinfo;) | (%bkinfo;))?, (%draftintro;)?, (%abstract;)?, (%dedication;)?, (%preface;)?, (%chapter;)*, ( ( (%appendix;)*, (%notices;)? , (%amendments;)? ) | (%part;)* ), (%colophon;)?)>
<!ATTLIST bookmap  title      CDATA   #IMPLIED
                   id         ID      #IMPLIED
                   xml:lang   NMTOKEN #IMPLIED
                   DTDVersion CDATA   #FIXED "&DTDVersion;"
                   domains    CDATA   "&included-domains;">

<!ELEMENT bkbasicinfo ((%booktitle;)?, (%booksubtitle;)?, (%bookabstract;)?, (%author;)*, (%publisher;)?, (%copyright;)*, (%critdates;)?, (%permissions;)?, (%audience;)*, (%category;)*, (%keywords;)*, (%prodinfo;)*, (%othermeta;)*)>
<!ELEMENT booktitle       (#PCDATA)*>
<!ELEMENT booksubtitle    (#PCDATA)*>
<!ELEMENT bookabstract    (#PCDATA)*>

<!ELEMENT bkinfo EMPTY>
<!ATTLIST bkinfo  id      ID     #IMPLIED
                  href    CDATA  #REQUIRED
                  conref  CDATA  #IMPLIED >

<!-- roles with contained topicrefs have an optional href attribute
     and divinfo -->
<!ELEMENT draftintro ((%divinfo;)?, (%topicref;)+)>
<!ATTLIST draftintro   id       ID       #IMPLIED
                       href     CDATA    #IMPLIED
                       navtitle CDATA    #IMPLIED
                       conref   CDATA    #IMPLIED
                       toc      (yes|no) #IMPLIED
                       print    (yes|no) #IMPLIED>

<!ELEMENT divinfo ((%divtitle;), (%shortdesc;), (%author;)*, (%publisher;)?, (%copyright;)*, (%critdates;)?, (%permissions;)?, (%audience;)*, (%category;)*, (%keywords;)*, (%prodinfo;)*, (%othermeta;)*)>
<!ELEMENT divtitle       (#PCDATA)*>

<!-- roles without contained topicrefs have a required href attribute
     and no divinfo -->
<!ELEMENT abstract EMPTY>
<!ATTLIST abstract    id       ID       #IMPLIED
                      href     CDATA    #REQUIRED
                      navtitle CDATA    #IMPLIED
                      conref   CDATA    #IMPLIED
                      toc      (yes|no) #IMPLIED
                      print    (yes|no) #IMPLIED >
<!ELEMENT dedication EMPTY>
<!ATTLIST dedication  id       ID       #IMPLIED
                      href     CDATA    #REQUIRED
                      navtitle CDATA    #IMPLIED
                      conref   CDATA    #IMPLIED
                      toc      (yes|no) #IMPLIED
                      print    (yes|no) #IMPLIED >
<!ELEMENT preface ((%divinfo;)?, (%topicref;)+)>
<!ATTLIST preface  id        ID       #IMPLIED
                   href      CDATA    #IMPLIED
                   navtitle  CDATA    #IMPLIED
                   conref    CDATA    #IMPLIED
                   toc       (yes|no) #IMPLIED
                   print     (yes|no) #IMPLIED >

<!ELEMENT chapter ((%divinfo;)?, (%topicref;)+)>
<!ATTLIST chapter  id        ID       #IMPLIED
                   href      CDATA    #IMPLIED
                   navtitle  CDATA    #IMPLIED
                   conref    CDATA    #IMPLIED
                   toc       (yes|no) #IMPLIED
                   print     (yes|no) #IMPLIED >
<!ELEMENT part ((%divinfo;)?, ((%chapter;)+ | ((%appendix;)*, (%notices;)? , (%amendments;)?)))>
<!ATTLIST part  id        ID       #IMPLIED
                href      CDATA    #IMPLIED
                navtitle  CDATA    #IMPLIED
                conref    CDATA    #IMPLIED
                toc       (yes|no) #IMPLIED
                print     (yes|no) #IMPLIED >
<!ELEMENT appendix ((%divinfo;)?, (%topicref;)+)>
<!ATTLIST appendix  id        ID       #IMPLIED
                    href      CDATA    #IMPLIED
                    navtitle  CDATA    #IMPLIED
                    conref    CDATA    #IMPLIED
                    toc       (yes|no) #IMPLIED
                    print     (yes|no) #IMPLIED >
<!ELEMENT notices ((%divinfo;)?, (%topicref;)+)>
<!ATTLIST notices  id        ID       #IMPLIED
                   href      CDATA    #IMPLIED
                   navtitle  CDATA    #IMPLIED
                   conref    CDATA    #IMPLIED
                   toc       (yes|no) #IMPLIED
                   print     (yes|no) #IMPLIED >
<!ELEMENT amendments EMPTY>
<!ATTLIST amendments  id        ID       #IMPLIED
                      href      CDATA    #REQUIRED
                      navtitle  CDATA    #IMPLIED
                      conref    CDATA    #IMPLIED
                      toc       (yes|no) #IMPLIED
                      print     (yes|no) #IMPLIED >
<!ELEMENT colophon EMPTY>
<!ATTLIST colophon  id        ID       #IMPLIED
                    href      CDATA    #REQUIRED
                    navtitle  CDATA    #IMPLIED
                    conref    CDATA    #IMPLIED
                    toc       (yes|no) #IMPLIED
                    print     (yes|no) #IMPLIED >

<!-- DEFERRED FOR NOW
<!ELEMENT booklists (
    (%abbrevlist;)?,
    (%trademarklist;)?,
    (%bibliolist;)?,
    (%glossarylist;)?,
    (%indexlist;)?
    )>
<!ATTLIST booklists  id       ID       #IMPLIED
                     conref   CDATA    #IMPLIED >

<!ELEMENT abbrevlist EMPTY>
<!ATTLIST abbrevlist  id       ID       #IMPLIED
                      href     CDATA    #REQUIRED
                      navtitle CDATA    #IMPLIED
                      conref   CDATA    #IMPLIED
                      toc      (yes|no) #IMPLIED
                      print    (yes|no) #IMPLIED >
<!ELEMENT trademarklist EMPTY>
<!ATTLIST trademarklist  id       ID       #IMPLIED
                         href     CDATA    #REQUIRED
                         navtitle CDATA    #IMPLIED
                         conref   CDATA    #IMPLIED
                         toc      (yes|no) #IMPLIED
                         print    (yes|no) #IMPLIED >
<!ELEMENT bibliolist EMPTY>
<!ATTLIST bibliolist  id       ID       #IMPLIED
                      href     CDATA    #REQUIRED
                      navtitle CDATA    #IMPLIED
                      conref   CDATA    #IMPLIED
                      toc      (yes|no) #IMPLIED
                      print    (yes|no) #IMPLIED >
<!ELEMENT glossarylist EMPTY>
<!ATTLIST glossarylist  id       ID       #IMPLIED
                        href     CDATA    #REQUIRED
                        navtitle CDATA    #IMPLIED
                        conref   CDATA    #IMPLIED
                        toc      (yes|no) #IMPLIED
                        print    (yes|no) #IMPLIED >
<!ELEMENT indexlist EMPTY>
<!ATTLIST indexlist  id       ID       #IMPLIED
                     href     CDATA    #REQUIRED
                     navtitle CDATA    #IMPLIED
                     conref   CDATA    #IMPLIED
                     toc      (yes|no) #IMPLIED
                     print    (yes|no) #IMPLIED >
-->

<!ATTLIST bookmap %global-atts;
    class CDATA "- map/map bookmap/bookmap ">

<!ATTLIST bkinfo %global-atts;
    class CDATA "- map/topicref bookmap/bkinfo ">

<!ATTLIST bkbasicinfo %global-atts;
    class CDATA "- map/topicmeta  bookmap/bkbasicinfo ">
<!ATTLIST booktitle %global-atts;
    class CDATA "- map/linktext bookmap/booktitle ">
<!ATTLIST booksubtitle %global-atts;
    class CDATA "- map/searchtitle bookmap/booksubtitle ">
<!ATTLIST bookabstract %global-atts;
    class CDATA "- map/shortdesc bookmap/bookabstract ">

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

<!ATTLIST divinfo %global-atts;
    class CDATA "- map/topicmeta  bookmap/divinfo ">
<!ATTLIST divtitle %global-atts;
    class CDATA "- map/linktext bookmap/divtitle ">

<!-- DEFERRED FOR NOW
<!ATTLIST booklists %global-atts;
    class CDATA "- map/topicref bookmap/booklists ">

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
-->
