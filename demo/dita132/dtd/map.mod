<!--
 | (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//Elements DITA Map//EN"
 |
 | Release history (vrm):
 |   1.0.0 Initial release on developerWorks, March 2001 (dita00.zip)
 |   1.0.1 fix 1 on developerWorks, October 2001 (dita01.zip)
 |   1.0.2 consolidated redesign December 2001
 |   1.0.3 fix 1, dtd freeze for UCD-1 January 2002
 |   1.1.0 Release 1 March 2002 (dita10.zip)
 |   1.1.1 Release 1.1 December 2002
 |   1.1.2 Release 1.2 June 2003
 |   1.1.2 Release 1.3 March 2004: bug fixes
 |   1.1.3a bug fix: completed removal of erroneous term/tm inclusion from words.cnt
 |   1.1.3a bug fix: cut keyword def from meta_xml; paste into topic.mod unchanged, put into map.mod with no tm
 |   1.1.3a bug fix: moved keyword declaration to follow meta_xml inclusion (PE reference order issue)
 *-->

<!ENTITY DTDVersion 'V1.1.3' >


<!--element redefinitions -->
<!ENTITY % map         "map">
<!ENTITY % navref      "navref">
<!ENTITY % topicref    "topicref">
<!ENTITY % anchor      "anchor">
<!ENTITY % reltable    "reltable">
<!ENTITY % relheader   "relheader">
<!ENTITY % relcolspec  "relcolspec">
<!ENTITY % relrow      "relrow">
<!ENTITY % relcell     "relcell">
<!ENTITY % topicmeta   "topicmeta">
<!ENTITY % linktext    "linktext">

<!ENTITY % searchtitle "searchtitle">
<!ENTITY % shortdesc   "shortdesc">


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

<!ENTITY % topicreftypes 'topic | concept | task | reference | external | local'>

<!ENTITY % topicref-atts 'collection-type    (choice|unordered|sequence|family) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external) #IMPLIED
  locktitle    (yes|no) #IMPLIED
  format        CDATA   #IMPLIED
  linking (targetonly|sourceonly|normal|none) #IMPLIED
  toc           (yes|no) #IMPLIED
  print         (yes|no) #IMPLIED
  search        (yes|no) #IMPLIED
  chunk         CDATA    #IMPLIED'
>

<!ENTITY % date-format   'CDATA'>

<!ENTITY % topicref-atts-no-toc 'collection-type  (choice|unordered|sequence|family) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external) #IMPLIED
  locktitle    (yes|no) "yes"
  format        CDATA   #IMPLIED
  linking (targetonly|sourceonly|normal|none) #IMPLIED
  toc           (yes|no) "no"
  print         (yes|no) #IMPLIED
  search        (yes|no) #IMPLIED
  chunk         CDATA    #IMPLIED'
>

<!--[20040606.01 DRD: move all keyword definitions from meta_xml.mod into map.mod, with new definitions]-->
<!ENTITY % keyword "keyword">

<!ELEMENT keyword        (#PCDATA)>
<!ATTLIST keyword         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ATTLIST keyword %global-atts; class CDATA "- topic/keyword ">

<!ENTITY % words.cnt            "#PCDATA | %keyword;">


<!ENTITY % metaXML PUBLIC "-//IBM//ELEMENTS DITA Metadata//EN" "meta_xml.mod">
  %metaXML;



<!ELEMENT map ((%topicmeta;)?,(%navref;|%anchor;|%topicref;|%reltable;)*)>
<!ATTLIST map title       CDATA #IMPLIED
                          id        ID    #IMPLIED
                          anchorref CDATA #IMPLIED
                          %topicref-atts;
                          %select-atts;
                          DTDVersion CDATA #FIXED "&DTDVersion;"
                          domains CDATA "&included-domains;"
>

<!ELEMENT navref          EMPTY>
<!ATTLIST navref          mapref CDATA #IMPLIED>


<!ELEMENT topicref ((%topicmeta;)?, (%topicref;|%navref;|%anchor;)*)>
<!ATTLIST topicref
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts;
  %select-atts;
>

<!ELEMENT anchor EMPTY>
<!ATTLIST anchor id   ID #REQUIRED>


<!--relationship table-->
<!ELEMENT reltable    ((%topicmeta;)?, (%relheader;)?, (%relrow;)+) >
<!ATTLIST reltable     title CDATA #IMPLIED
                       %id-atts;
                          %topicref-atts-no-toc;
                          %select-atts;
>


<!ELEMENT relheader (%relcolspec;)+>

<!ELEMENT relcolspec (%topicmeta;)?>
<!ATTLIST relcolspec
                          %topicref-atts;
                          %select-atts;
>


<!ELEMENT relrow (%relcell;)*>
<!ATTLIST relrow          %id-atts;
                          %select-atts;
>

<!ELEMENT relcell ((%topicref;)*)>
<!ATTLIST relcell         %id-atts;
                          %topicref-atts;
>


<!ELEMENT topicmeta ((%linktext;)?,(%searchtitle;)?,(%shortdesc;)?,(%author;)*,(%source;)?,(%publisher;)?,(%copyright;)*,(%critdates;)?,(%permissions;)?,(%audience;)*,(%category;)*,(%keywords;)*,(%prodinfo;)*,(%othermeta;)*,(%resourceid;)*)>
<!ATTLIST topicmeta lockmeta (yes|no) #IMPLIED>


<!ELEMENT linktext (%words.cnt;)*>
<!ELEMENT searchtitle (%words.cnt;)*>
<!ELEMENT shortdesc (%words.cnt;)*>



<!--identity-->
<!--@navtitle - the title of the target topic for use in navigation - may be derived (from topic's navtitle or title), or may be locally defined-->
<!--@id - an id for the reference itself - to allow the topicref and its children to be reused by other maps, or elsehere in same map-->

<!--referencing-->
<!--@href - the URL/URI for the target topic eg ..\ref\bigfile.xml#mytopic-->
<!--@keyref - a key that the processor can use to look up the URL/URI for the target-->
<!--@query - lists query criteria, or uses topicref's metadata as criteria; pulls in matching topics under the current one if title is present, or replaces the current location if title is not-->
<!--@conref - pulls in another topicref and its children by URL plus id or just id if local-->


<!--sorting-->
<!--@collection-type - used to identify the kind of grouping for child topicrefs-->
<!--FUTURE: @sortkey - lists criteria for sorting children - won't apply when colltype=sequence; should be able to list multiple criteria -eg @type @title gives order by type but alpha within type-->

<!--metadata-->
<!--@type - the type of the target topic - should match the target's root topic elem or one of its ancestors in its class attribute-->

<!--processing info-->
<!--@locktitle - set to yes if you want the local def of the title to override the target-->
<!--@linking - targetonly means that the current ref will be a target for links generated from the rel, but will not be a source for any of those links (ie, no links based on the context rel will be added to the referenced topic-->

<!--specialization attributes-->
<!ATTLIST map %global-atts; class CDATA "- map/map ">
<!ATTLIST navref %global-atts; class CDATA "- map/navref ">
<!ATTLIST topicref %global-atts; class CDATA "- map/topicref ">
<!ATTLIST anchor %global-atts; class CDATA "- map/anchor ">
<!ATTLIST reltable %global-atts; class CDATA "- map/reltable ">
<!ATTLIST relheader %global-atts; class CDATA "- map/relheader ">
<!ATTLIST relcolspec %global-atts; class CDATA "- map/relcolspec ">
<!ATTLIST relrow %global-atts; class CDATA "- map/relrow ">
<!ATTLIST relcell %global-atts; class CDATA "- map/relcell ">
<!ATTLIST topicmeta %global-atts; class CDATA "- map/topicmeta ">
<!ATTLIST linktext %global-atts; class CDATA "- map/linktext ">
<!ATTLIST searchtitle %global-atts; class CDATA "- map/searchtitle ">
<!ATTLIST shortdesc %global-atts; class CDATA "- map/shortdesc ">
