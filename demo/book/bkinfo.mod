<!ENTITY % bkinfo          "bkinfo">
<!ENTITY % bktitlealts     "bktitlealts">
<!ENTITY % bksubtitle      "bksubtitle">
<!ENTITY % bktitleabbrev   "bktitleabbrev">
<!ENTITY % bkinfobody      "bkinfobody">
<!ENTITY % bkid            "bkid">
<!ENTITY % bkpartno        "bkpartno">
<!ENTITY % bkedition       "bkedition">
<!ENTITY % isbn            "isbn">
<!ENTITY % bknum           "bknum">
<!ENTITY % bkvolume        "bkvolume">
<!ENTITY % bkvolid         "bkvolid">
<!ENTITY % bklibrary       "bklibrary">
<!ENTITY % bkmaintainer    "bkmaintainer">
<!ENTITY % bkpublisher     "bkpublisher">
<!ENTITY % bkprintloc      "bkprintloc">
<!ENTITY % bkrights        "bkrights">
<!ENTITY % bkcopyrfirst    "bkcopyrfirst">
<!ENTITY % bkcopyrlast     "bkcopyrlast">
<!ENTITY % bkowner         "bkowner">
<!ENTITY % bkrestriction   "bkrestriction">
<!ENTITY % bkhistory       "bkhistory">
<!ENTITY % bkauthored      "bkauthored">
<!ENTITY % bkreviewed      "bkreviewed">
<!ENTITY % bkedited        "bkedited">
<!ENTITY % bktested        "bktested">
<!ENTITY % bkapproved      "bkapproved">
<!ENTITY % bkpublished     "bkpublished">
<!ENTITY % bkpublishtype   "bkpublishtype">
<!ENTITY % bkevent         "bkevent">
<!ENTITY % bkeventtype     "bkeventtype">
<!ENTITY % bkrevisionid    "bkrevisionid">
<!ENTITY % person          "person">
<!ENTITY % honorific       "honorific">
<!ENTITY % firstname       "firstname">
<!ENTITY % middlename      "middlename">
<!ENTITY % lastname        "lastname">
<!ENTITY % lineage         "lineage">
<!ENTITY % resource        "resource">
<!ENTITY % affiliations    "affiliations">
<!ENTITY % otherinfo       "otherinfo">
<!ENTITY % organization    "organization">
<!ENTITY % orgname         "orgname">
<!ENTITY % address         "address">
<!ENTITY % city            "city">
<!ENTITY % stateprov       "stateprov">
<!ENTITY % postalcode      "postalcode">
<!ENTITY % country         "country">
<!ENTITY % phone           "phone">
<!ENTITY % started         "started">
<!ENTITY % completed       "completed">
<!ENTITY % date            "date">
<!ENTITY % day             "day">
<!ENTITY % month           "month">
<!ENTITY % year            "year">
<!ENTITY % summary         "summary">
<!ENTITY % bkcover         "bkcover">
<!ENTITY % bkcoverfront    "bkcoverfront">
<!ENTITY % bkcoverback     "bkcoverback">
<!ENTITY % bkabstract      "bkabstract">

<!-- DocBook elements that are not supported
          Article-oriented markup:
               bookinfo / artpagenums
               bookinfo / confgroup
               bookinfo / contractnum
               bookinfo / contractsponsor
               bookinfo / issn
               bookinfo / issuenum
     -->

<!ENTITY DTDVersion 'V1.1.1' >

<!ENTITY included-domains "">

<!-- DocBook:   bookinfo
     IBMIDDoc:  prolog and ibmiddoc attributes
     -->
<!ELEMENT bkinfo        (%title;, (%bktitlealts;)?, (%bkabstract;)?, (%prolog;)?, %bkinfobody;)>
<!ATTLIST bkinfo          id ID #REQUIRED
                          conref CDATA #IMPLIED
                          %select-atts;
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          DTDVersion CDATA #FIXED "&DTDVersion;"
                          domains CDATA "&included-domains;"
>

<!-- DocBook:   bookinfo / title
                book / title
     IBMIDDoc:  prolog / ibmbibentry / doctitle / titleblk / title
     DITA:      title
     -->

<!ELEMENT bktitlealts     ((%bktitleabbrev;)?, (%bksubtitle;)?)>
<!ATTLIST bktitlealts     %id-atts;
>
<!-- DocBook:   bookinfo / subtitle
                book / subtitle
     IBMIDDoc:  prolog / ibmbibentry / doctitle / titleblk / subtitle
     -->
<!ELEMENT bksubtitle      (#PCDATA)*>
<!ATTLIST bksubtitle      %id-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / titleabbrev
                book / titleabbrev
     IBMIDDoc:  prolog / ibmbibentry / doctitle / titleblk / stitle
     -->
<!ELEMENT bktitleabbrev   (#PCDATA)*>
<!ATTLIST bktitleabbrev   %id-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / abstract
     IBMIDDoc:  prolog / ibmbibentry / desc
     -->
<!ELEMENT bkabstract      (#PCDATA)*>
<!ATTLIST bkabstract      %id-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / indexterm
                bookinfo / itermset / indexterm
     IBMIDDoc:  ???
     DITA:      prolog / metadata / keywords / indexterm
     -->
<!-- DocBook:   bookinfo / keywordset / keyword
     IBMIDDoc:  prolog / ibmbibentry / retkey
     DITA:      prolog / metadata / keywords / keyword
     -->
<!-- DocBook:   bookinfo / subjectset / subject / subjectterm
     IBMIDDoc:  ???
     DITA:      prolog / metadata / category
     -->
<!-- DocBook:   bookinfo / orgname
     IBMIDDoc:  ibmiddoc @ brand
     DITA:      prolog / metadata / prodinfo / brand
     -->
<!-- DocBook:   bookinfo / productname / productnumber |
                bookinfo / productnumber
     IBMIDDoc:  prolog / ibmbibentry / doctitle / productlevel
     DITA:      prolog / metadata / prodinfo / vrmlist / vrm @ version
     -->
<!-- DocBook:   book @ arch  || book @ os
     IBMIDDoc:  prolog / ibmbibentry / doctitle / platform
     DITA:      prolog / metadata / prodinfo / platform
     -->
<!-- DocBook:   bookinfo / productname
     IBMIDDoc:  prolog / prodinfo / prodname
     DITA:      prolog / metadata / prodinfo / prodname
     -->
<!-- DocBook:   bookinfo / productnumber
     IBMIDDoc:  prolog / prodinfo / version
     DITA:      prolog / metadata / prodinfo / vrmlist / vrm @ version
     -->
<!-- DocBook:   bookinfo / productname
     IBMIDDoc:  prolog / ibmprodinfo / prodname
     DITA:      prolog / metadata / prodinfo / prodname
     -->
<!-- DocBook:   bookinfo / productname / revhistory / revision / revnumber
     IBMIDDoc:  prolog / ibmprodinfo / ibmfeatnum
     DITA:      prolog / metadata / prodinfo / featnum
     -->
<!-- DocBook:   bookinfo / invpartnumber 
     IBMIDDoc:  prolog / ibmprodinfo / ibmpgmnum
     DITA:      prolog / metadata / prodinfo / prognum
     -->
<!-- DocBook:   bookinfo / productnumber
     IBMIDDoc:  prolog / ibmprodinfo / version
     DITA:      prolog / metadata / prodinfo / vrmlist / vrm @ version
     -->
<!-- DocBook:   bookinfo / releaseinfo
     IBMIDDoc:  prolog / ibmprodinfo / release
     DITA:      prolog / metadata / prodinfo / vrmlist / vrm @ release
     -->
<!-- DocBook:   bookinfo / releaseinfo
     IBMIDDoc:  prolog / ibmprodinfo / modlvl
     DITA:      prolog / metadata / prodinfo / vrmlist / vrm @ modification
     -->
<!-- DocBook:   bookinfo / bibliomisc / revhistory / revision / revnumber |
	            bookinfo / bibliomisc / productnumber
     IBMIDDoc:  prolog / ibmbibentry / origibmdocnum
     DITA:      prolog / source
     -->
<!-- DocBook:   bookinfo / bibliomisc / sgmltag
     IBMIDDoc:  prolog / ibmbibentry / publicid
     DITA:      prolog / resourceid
     -->

<!ELEMENT bkinfobody      ((%bkid;)?, (%bkpublisher;)?, (%bkrights;)*, (%bkhistory;)?, (%bkcover;)?) >
<!ATTLIST bkinfobody      %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT bkid           ((%bkpartno;)?, (%bkedition;)?, (%isbn;)?, (%bknum;)?, (%bkvolume;)*, (%bkmaintainer;)?)>
<!ATTLIST bkid            %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / invpartnumber
     IBMIDDoc:  prolog / ibmbibentry / ibmpartnum
     -->
<!ELEMENT bkpartno        (#PCDATA)*>
<!ATTLIST bkpartno        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / edition
     IBMIDDoc:  ???
     -->
<!ELEMENT bkedition       (#PCDATA)*>
<!ATTLIST bkedition       %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / isbn
     IBMIDDoc:  prolog / ibmbibentry / isbn
     -->
<!ELEMENT isbn            (#PCDATA)*>
<!ATTLIST isbn            %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / pubsnumber
     IBMIDDoc:  prolog / ibmbibentry / ibmdocnum
     -->
<!ELEMENT bknum           (#PCDATA)*>
<!ATTLIST bknum           %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkvolume        ((%bkvolid;), (%bklibrary;))>
<!ATTLIST bkvolume        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / volumenum
     IBMIDDoc:  prolog / ibmbibentry / volid
     -->
<!ELEMENT bkvolid         (#PCDATA)*>
<!ATTLIST bkvolid         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / ???
     IBMIDDoc:  prolog / ibmbibentry / doctitle / library / titleblk / title
     -->
<!ELEMENT bklibrary       (%xreftext.cnt;)*>
<!ATTLIST bklibrary       href          CDATA   #IMPLIED
                          keyref        NMTOKEN #IMPLIED
                          type          CDATA   #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope         CDATA   "external"
                          outputclass   CDATA   #IMPLIED
>
<!-- DocBook:   bookinfo / ???
     IBMIDDoc:  prolog / maintainer
     -->
<!ELEMENT bkmaintainer    ((%person;) | (%organization;))*>
<!ATTLIST bkmaintainer    %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / publisher / publishername
     IBMIDDoc:  prolog / ibmbibentry / publisher / corpname
     -->
<!ELEMENT bkpublisher     (((%person;) | (%organization;))*, (%bkprintloc;)*)>
<!ATTLIST bkpublisher     %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   DocBook:   bookinfo / publisher / address
     IBMIDDoc:  prolog / ibmbibentry / prtloc
     -->
<!ELEMENT bkprintloc      (#PCDATA)*>
<!ATTLIST bkprintloc      %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / copyright
     IBMIDDoc:  prolog / copyrdefs / copyr
     -->
<!ELEMENT bkrights        ((%bkcopyrfirst;)?, (%bkcopyrlast;)?, (%bkowner;), (%bkrestriction;)?, (%summary;)?)>
<!ATTLIST bkrights        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkcopyrfirst    (%year;)>
<!ATTLIST bkcopyrfirst    keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkcopyrlast     (%year;)>
<!ATTLIST bkcopyrlast     keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / copyright / holder
     IBMIDDoc:  prolog / owners
     -->
<!ELEMENT bkowner         ((%person;) | (%organization;))*>
<!ATTLIST bkowner         type CDATA "other"
                          spectitle CDATA #IMPLIED
                          othertype CDATA "owner"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   ???
     IBMIDDoc:  ibmiddoc @ classif
                ibmiddoc @ sec
     -->
<!ELEMENT bkrestriction   EMPTY>
<!ATTLIST bkrestriction   name (confidential|restricted|licensed|unclassified) #REQUIRED
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT bkhistory       ((%bkauthored;) | (%bkreviewed;) | (%bkedited;) | (%bktested;) | (%bkapproved;) | (%bkpublished;) | (%bkevent;))*>
<!ATTLIST bkhistory       %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / authorgroup
                bookinfo / author
                bookinfo / revhistory
     IBMIDDoc:  prolog / ibmbibentry / authors
                prolog / revdefs
     -->
<!ELEMENT bkauthored      (((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkauthored      %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkreviewed      (((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkreviewed      %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / editor
     IBMIDDoc:  prolog / critdates / critdate
     -->
<!ELEMENT bkedited        (((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkedited        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bktested        (((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bktested        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   ???
     IBMIDDoc:  prolog / approvers
     -->
<!ELEMENT bkapproved      (((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkapproved      %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   bookinfo / printhistory
                bookinfo / pubdate
     IBMIDDoc:  prolog / critdates / critdate
     -->
<!ELEMENT bkpublished     ((%bkpublishtype;)?, ((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkpublished     %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkpublishtype   EMPTY>
<!ATTLIST bkpublishtype   name (beta|limited|general) #REQUIRED
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / othercredit
     IBMIDDoc:  ???
     -->
<!ELEMENT bkevent         ((%bkeventtype;)?, ((%person;) | (%organization;))*, (%bkrevisionid;)?, (%started;)?, (%completed;)?, (%summary;)?)>
<!ATTLIST bkevent         %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkeventtype     EMPTY>
<!ATTLIST bkeventtype     name CDATA #REQUIRED
                          value CDATA ""
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkrevisionid    (#PCDATA)*>
<!ATTLIST bkrevisionid    keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   personname
     IBMIDDoc:  person
     -->
<!ELEMENT person          ((%honorific;)*, (%firstname;)?, (%middlename;)*, (%lastname;)*, (%lineage;)?, (%address;)*, (%phone;)*, (%resource;)*, (%summary;)?, (%affiliations;)?, (%otherinfo;)*)>
<!ATTLIST person          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT honorific       (#PCDATA)*>
<!ATTLIST honorific       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT firstname       (#PCDATA)*>
<!ATTLIST firstname       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT middlename      (#PCDATA)*>
<!ATTLIST middlename      keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT lastname        (#PCDATA)*>
<!ATTLIST lastname        keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT lineage         (#PCDATA)*>
<!ATTLIST lineage         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT resource        (%xreftext.cnt;)*>
<!ATTLIST resource        href          CDATA   #IMPLIED
                          keyref        NMTOKEN #IMPLIED
                          type          CDATA   #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope         CDATA   "external"
                          outputclass   CDATA   #IMPLIED
>
<!-- DocBook:   bookinfo / affiliation
     IBMIDDoc:  ???
     -->
<!ELEMENT affiliations    ((%organization;)*)>
<!ATTLIST affiliations    type CDATA "other"
                          spectitle CDATA #IMPLIED
                          othertype CDATA "affiliations"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT otherinfo       (#PCDATA)*>
<!ATTLIST otherinfo       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / corpauthor
                bookinfo / corpname 
                bookinfo / orgname 
     IBMIDDoc:  corp
     -->
<!ELEMENT organization    ((%orgname;)?, (%address;)*, (%phone;)*, (%resource;)*, (%summary;)?, (%otherinfo;)*)>
<!ATTLIST organization    %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT orgname         (#PCDATA)*>
<!ATTLIST orgname         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   bookinfo / publisher / address
     IBMIDDoc:  prolog / ibmbibentry / publisher / address
     -->
<!ELEMENT address         (#PCDATA|%city;|%stateprov;|%postalcode;|%country;)*>
<!ATTLIST address         %display-atts;
                          %univ-atts;
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>

<!-- DocBook:   city
     IBMIDDoc:  ???
     -->
<!ELEMENT city            (#PCDATA)*>
<!ATTLIST city            keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   state
     IBMIDDoc:  ???
     -->
<!ELEMENT stateprov       (#PCDATA)*>
<!ATTLIST stateprov       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   postcode
     IBMIDDoc:  postalcode
     -->
<!ELEMENT postalcode      (#PCDATA)*>
<!ATTLIST postalcode      keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- DocBook:   country
     IBMIDDoc:  ???
     -->
<!ELEMENT country         (#PCDATA)*>
<!ATTLIST country         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- DocBook:   ???
     IBMIDDoc:  phone
     -->
<!ELEMENT phone           (#PCDATA)*>
<!ATTLIST phone           keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT started         (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST started         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT completed       (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST completed       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT date            (((%year;), ((%month;), (%day;)?)?) | ((%month;), (%day;)?, (%year;)) | ((%day;), (%month;), (%year;)))>
<!ATTLIST date            keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT year            (#PCDATA)*>
<!ATTLIST year            keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT month           (#PCDATA)*>
<!ATTLIST month           keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT day             (#PCDATA)*>
<!ATTLIST day             keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT summary         (%ph.cnt;)*>
<!ATTLIST summary         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT bkcover         ((%bkcoverfront;)?, (%bkcoverback;)?)>
<!ATTLIST bkcover         %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkcoverfront    (%basic.block;)+>
<!ATTLIST bkcoverfront    %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT bkcoverback     (%basic.block;)+>
<!ATTLIST bkcoverback     %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ATTLIST bkinfo        %global-atts;
        class CDATA "- topic/topic bkinfo/bkinfo ">
<!ATTLIST bktitlealts %global-atts;
        class CDATA "- topic/titlealts bkinfo/bktitlealts ">
<!ATTLIST bktitleabbrev %global-atts;
        class CDATA "- topic/navtitle bkinfo/bktitleabbrev ">
<!ATTLIST bksubtitle %global-atts;
        class CDATA "- topic/searchtitle bkinfo/bksubtitle ">
<!ATTLIST bkabstract %global-atts;
        class CDATA "- topic/shortdesc bkinfo/bkabstract ">
<!ATTLIST bkinfobody %global-atts;
        class CDATA "- topic/body bkinfo/bkinfobody ">
<!ATTLIST bkid %global-atts;
        class CDATA "- topic/ul bkinfo/bkid ">
<!ATTLIST bkpartno %global-atts;
        class CDATA "- topic/li bkinfo/bkpartno ">
<!ATTLIST bkedition %global-atts;
        class CDATA "- topic/li bkinfo/bkedition ">
<!ATTLIST isbn %global-atts;
        class CDATA "- topic/li bkinfo/isbn ">
<!ATTLIST bknum %global-atts;
        class CDATA "- topic/li bkinfo/bknum ">
<!ATTLIST bkvolume %global-atts;
        class CDATA "- topic/li bkinfo/bkvolume ">
<!ATTLIST bkvolid %global-atts;
        class CDATA "- topic/ph bkinfo/bkvolid ">
<!ATTLIST bklibrary %global-atts;
        class CDATA "- topic/xref bkinfo/bklibrary ">
<!ATTLIST bkmaintainer %global-atts;
        class CDATA "- topic/li bkinfo/bkmaintainer ">
<!ATTLIST bkpublisher %global-atts;
        class CDATA "- topic/section bkinfo/bkpublisher ">
<!ATTLIST bkprintloc %global-atts;
        class CDATA "- topic/p bkinfo/bkprintloc ">
<!ATTLIST bkrights %global-atts;
        class CDATA "- topic/p bkinfo/bkrights ">
<!ATTLIST bkcopyrfirst %global-atts;
        class CDATA "- topic/ph bkinfo/bkcopyrfirst ">
<!ATTLIST bkcopyrlast %global-atts;
        class CDATA "- topic/ph bkinfo/bkcopyrlast ">
<!ATTLIST bkowner %global-atts;
        class CDATA "- topic/note bkinfo/bkowner ">
<!ATTLIST bkrestriction %global-atts;
        class CDATA "- topic/state bkinfo/bkrestriction ">
<!ATTLIST bkhistory %global-atts;
        class CDATA "- topic/ul bkinfo/bkhistory ">
<!ATTLIST bkauthored %global-atts;
        class CDATA "- topic/li bkinfo/bkauthored ">
<!ATTLIST bkreviewed %global-atts;
        class CDATA "- topic/li bkinfo/bkreviewed ">
<!ATTLIST bkedited %global-atts;
        class CDATA "- topic/li bkinfo/bkedited ">
<!ATTLIST bktested %global-atts;
        class CDATA "- topic/li bkinfo/bktested ">
<!ATTLIST bkapproved %global-atts;
        class CDATA "- topic/li bkinfo/bkapproved ">
<!ATTLIST bkpublished %global-atts;
        class CDATA "- topic/li bkinfo/bkpublished ">
<!ATTLIST bkpublishtype %global-atts;
        class CDATA "- topic/state bkinfo/bkpublishtype ">
<!ATTLIST bkevent %global-atts;
        class CDATA "- topic/li bkinfo/bkevent ">
<!ATTLIST bkeventtype %global-atts;
        class CDATA "- topic/state bkinfo/bkeventtype ">
<!ATTLIST bkrevisionid %global-atts;
        class CDATA "- topic/ph bkinfo/bkrevisionid ">
<!ATTLIST person %global-atts;
        class CDATA "- topic/p bkinfo/person ">
<!ATTLIST honorific %global-atts;
        class CDATA "- topic/ph bkinfo/honorific ">
<!ATTLIST firstname %global-atts;
        class CDATA "- topic/ph bkinfo/firstname ">
<!ATTLIST middlename %global-atts;
        class CDATA "- topic/ph bkinfo/middlename ">
<!ATTLIST lastname %global-atts;
        class CDATA "- topic/ph bkinfo/lastname ">
<!ATTLIST lineage %global-atts;
        class CDATA "- topic/ph bkinfo/lineage ">
<!ATTLIST resource %global-atts;
        class CDATA "- topic/xref bkinfo/resource ">
<!ATTLIST affiliations %global-atts;
        class CDATA "- topic/note bkinfo/affiliations ">
<!ATTLIST otherinfo %global-atts;
        class CDATA "- topic/ph bkinfo/otherinfo ">
<!ATTLIST organization %global-atts;
        class CDATA "- topic/p bkinfo/organization ">
<!ATTLIST orgname %global-atts;
        class CDATA "- topic/ph bkinfo/orgname ">
<!ATTLIST address %global-atts;
        class CDATA "- topic/lines bkinfo/address ">
<!ATTLIST city %global-atts;
        class CDATA "- topic/ph bkinfo/city ">
<!ATTLIST stateprov %global-atts;
        class CDATA "- topic/ph bkinfo/stateprov ">
<!ATTLIST postalcode %global-atts;
        class CDATA "- topic/ph bkinfo/postalcode ">
<!ATTLIST country %global-atts;
        class CDATA "- topic/ph bkinfo/country ">
<!ATTLIST phone %global-atts;
        class CDATA "- topic/ph bkinfo/phone ">
<!ATTLIST started %global-atts;
        class CDATA "- topic/ph bkinfo/started ">
<!ATTLIST completed %global-atts;
        class CDATA "- topic/ph bkinfo/completed ">
<!ATTLIST date %global-atts;
        class CDATA "- topic/ph bkinfo/date ">
<!ATTLIST day %global-atts;
        class CDATA "- topic/ph bkinfo/day ">
<!ATTLIST month %global-atts;
        class CDATA "- topic/ph bkinfo/month ">
<!ATTLIST year %global-atts;
        class CDATA "- topic/ph bkinfo/year ">
<!ATTLIST summary %global-atts;
        class CDATA "- topic/ph bkinfo/summary ">
<!ATTLIST bkcover %global-atts;
        class CDATA "- topic/ul bkinfo/bkcover ">
<!ATTLIST bkcoverfront %global-atts;
        class CDATA "- topic/li bkinfo/bkcoverfront ">
<!ATTLIST bkcoverback %global-atts;
        class CDATA "- topic/li bkinfo/bkcoverback ">
