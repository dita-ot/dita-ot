<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<!ENTITY % subjectScheme    "subjectScheme">
<!ENTITY % schemeref        "schemeref">
<!ENTITY % hasNarrower      "hasNarrower">
<!ENTITY % hasKind          "hasKind">
<!ENTITY % hasPart          "hasPart">
<!ENTITY % hasInstance      "hasInstance">
<!ENTITY % subjectHead      "subjectHead">
<!ENTITY % subjectdef       "subjectdef">
<!ENTITY % subjectmeta      "subjectmeta">
<!ENTITY % subjPrefLabel    "subjPrefLabel">
<!ENTITY % subjAltLabel     "subjAltLabel">
<!ENTITY % subjHiddenLabel  "subjHiddenLabel">
<!ENTITY % subjPrefSymbol   "subjPrefSymbol">
<!ENTITY % subjAltSymbol    "subjAltSymbol">
<!ENTITY % relatedSubjects  "relatedSubjects">
<!ENTITY % subjectRelTable  "subjectRelTable">
<!ENTITY % subjectRelHeader "subjectRelHeader">
<!ENTITY % subjectRel       "subjectRel">
<!ENTITY % subjectRole      "subjectRole">

<!ELEMENT subjectScheme ((%topicmeta;)?, ((%hasNarrower;|%hasKind;|%hasPart;|%hasInstance;|%subjectdef;|%subjectHead;|%relatedSubjects;|%subjectRelTable;|%schemeref;|%navref;|%anchor;|%topicref;|%reltable;)*))>
<!ATTLIST subjectScheme   title     CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
                          %arch-atts;
                          domains    CDATA "&included-domains;"
>

<!ELEMENT schemeref ((%topicmeta;)?)>
<!ATTLIST schemeref
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  type         CDATA     'scheme'
  format       CDATA     'ditamap'
  %select-atts;
>

<!ELEMENT hasNarrower ((%subjectdef;|%subjectHead;)*)>
<!ATTLIST hasNarrower
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  %select-atts;
>
<!ELEMENT hasKind ((%subjectdef;|%subjectHead;)*)>
<!ATTLIST hasKind
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  %select-atts;
>
<!ELEMENT hasPart ((%subjectdef;|%subjectHead;)*)>
<!ATTLIST hasPart
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  %select-atts;
>
<!ELEMENT hasInstance ((%subjectdef;|%subjectHead;)*)>
<!ATTLIST hasInstance
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  %select-atts;
>

<!-- SKOS equivalent: concept collection -->
<!ELEMENT subjectHead ((%subjectdef;|%subjectHead;)*)>
<!ATTLIST subjectHead
  navtitle     CDATA     #REQUIRED
  id           ID        #IMPLIED
  conref       CDATA     #IMPLIED
  collection-type    (unordered|sequence) #IMPLIED
  linking     (normal)   'normal'
  %select-atts;
>

<!ELEMENT subjectdef ((%subjectmeta;)?, ((%hasNarrower;|%hasKind;|%hasPart;|%hasInstance;)|(%subjectdef;|%subjectHead;)*))>
<!ATTLIST subjectdef
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     #IMPLIED
  scope       (local | external) #IMPLIED
  locktitle   (yes|no)   #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  %select-atts;
>
<!ELEMENT subjectmeta ((%shortdesc;)?, (%author;)*, (%source;)?, (%publisher;)?, (%copyright;)*, (%critdates;)?, (%permissions;)?, (%category;)*, (%subjPrefLabel;)?, (%subjAltLabel;)*, (%subjHiddenLabel;)*, (%subjPrefSymbol;)?, (%subjAltSymbol;)*, (%othermeta;)*, (%resourceid;)*)>
<!ATTLIST subjectmeta lockmeta (yes|no) 'yes'>
<!ELEMENT subjPrefLabel   EMPTY>
<!ATTLIST subjPrefLabel   name    CDATA 'prefLabel'
                          content CDATA #REQUIRED
                          translate-content (yes|no) 'yes'
                          %select-atts;
>
<!ELEMENT subjAltLabel    EMPTY>
<!ATTLIST subjAltLabel    name    CDATA 'altLabel'
                          content CDATA #REQUIRED
                          translate-content (yes|no) 'yes'
                          %select-atts;
>
<!ELEMENT subjHiddenLabel  EMPTY>
<!ATTLIST subjHiddenLabel name    CDATA 'hiddenLabel'
                          content CDATA #REQUIRED
                          translate-content (yes|no) 'yes'
                          %select-atts;
>
<!-- The content attribute supplies the URI for the symbol image -->
<!ELEMENT subjPrefSymbol  EMPTY>
<!ATTLIST subjPrefSymbol  name    CDATA 'prefSymbol'
                          content CDATA #REQUIRED
                          translate-content (yes|no) 'no'
                          %select-atts;
>
<!ELEMENT subjAltSymbol   EMPTY>
<!ATTLIST subjAltSymbol   name    CDATA 'altSymbol'
                          content CDATA #REQUIRED
                          translate-content (yes|no) 'no'
                          %select-atts;
>

<!-- To define roles within a relationship, you can specialize
     the relatedSubjects container and its contained subjectdef elements,
     possibly setting the linking attribute to targetonly or sourceonly.
     For instance, a dependency relationship could contain depended-on
     and dependent subjects.
     -->
<!ELEMENT relatedSubjects ((%subjectdef;)*)>
<!ATTLIST relatedSubjects
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) 'family'
  %select-atts;
>

<!-- Where there are many instances of a subject relationship in which
     different subjects have defined roles within the relationship,
     you can use or specialize the subjectRelTable.
     Note that each row matrixes relationships across columns such that
     a subject receives relationships to every subject in other columns
     within the same row. -->
<!ELEMENT subjectRelTable  ((%subjectRelHeader;)?, (%subjectRel;)+) >
<!ATTLIST subjectRelTable  title CDATA #IMPLIED
                          %id-atts;
                          %topicref-atts-no-toc;
                          %select-atts;
>
<!-- The header defines the role of subjects in each column
     The role definition can be an informal navtitle or 
         a formal reference -->
<!ELEMENT subjectRelHeader  ((%subjectRole;)+)>
<!ATTLIST subjectRelHeader  %id-atts;
                          %select-atts;
>
<!ELEMENT subjectRel     ((%subjectRole;)+)>
<!ATTLIST subjectRel      %id-atts;
                          %select-atts;
>
<!ELEMENT subjectRole    ((%subjectdef;)+)>
<!ATTLIST subjectRole     %id-atts;
                          %topicref-atts;
>

<!ATTLIST subjectScheme %global-atts;
    class CDATA "- map/map scheme/subjectScheme ">
<!ATTLIST schemeref %global-atts;
    class CDATA "- map/topicref scheme/schemeref ">
<!ATTLIST hasNarrower %global-atts;
    class CDATA "- map/topicref scheme/hasNarrower ">
<!ATTLIST hasKind %global-atts;
    class CDATA "- map/topicref scheme/hasKind ">
<!ATTLIST hasPart %global-atts;
    class CDATA "- map/topicref scheme/hasPart ">
<!ATTLIST hasInstance %global-atts;
    class CDATA "- map/topicref scheme/hasInstance ">
<!ATTLIST subjectHead %global-atts;
    class CDATA "- map/topicref scheme/subjectHead ">
<!ATTLIST subjectdef %global-atts;
    class CDATA "- map/topicref scheme/subjectdef ">
<!ATTLIST subjectmeta %global-atts;
    class CDATA "- map/topicmeta scheme/subjectmeta ">
<!ATTLIST subjPrefLabel %global-atts;
    class CDATA "- topic/othermeta scheme/subjPrefLabel ">
<!ATTLIST subjAltLabel %global-atts;
    class CDATA "- topic/othermeta scheme/subjAltLabel ">
<!ATTLIST subjHiddenLabel %global-atts;
    class CDATA "- topic/othermeta scheme/subjHiddenLabel ">
<!ATTLIST subjPrefSymbol %global-atts;
    class CDATA "- topic/othermeta scheme/subjPrefSymbol ">
<!ATTLIST subjAltSymbol %global-atts;
    class CDATA "- topic/othermeta scheme/subjAltSymbol ">
<!ATTLIST relatedSubjects %global-atts;
    class CDATA "- map/topicref scheme/relatedSubjects ">
<!ATTLIST subjectRelTable %global-atts;
    class CDATA "- map/reltable scheme/subjectRelTable ">
<!ATTLIST subjectRelHeader %global-atts;
    class CDATA "- map/relrow scheme/subjectRelHeader ">
<!ATTLIST subjectRel %global-atts;
    class CDATA "- map/relrow scheme/subjectRel ">
<!ATTLIST subjectRole %global-atts;
    class CDATA "- map/relcell scheme/subjectRole ">
