<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!-- Darwin Information Typing Architecture (DITA) Version 1.3     -->
<!-- Candidate OASIS Standard 01                                           -->
<!-- 03 September 2015                                           -->
<!-- Copyright (c) OASIS Open 2015. All rights reserved.           -->
<!-- Source: http://docs.oasis-open.org/dita/dita/v1.3/cos01/dita-v1.3-cos01-part0-overview.html                                -->
<!--                                                               -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Map                                          -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                        -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Map//EN"
      Delivered as file "map.mod"                                  -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Maps                      -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Corrected public ID in the comment above   -->
<!--    2005.11.15 RDA: Removed old declaration for topicreftypes  -->
<!--                      entity                                   -->
<!--    2006.06.06 RDA: Removed default locktitle="yes" from       -->
<!--                      %topicref-atts-no-toc;                   -->
<!--                    Remove keyword declaration                 -->
<!--                    Add reference to commonElements            -->
<!--                    Add title element to map                   -->
<!--                    Add data element to topicmeta              -->
<!--                    Remove shortdesc declaration               -->
<!--    2006.06.07 RDA: Make universal attributes universal        -->
<!--                      (DITA 1.1 proposal #12)                  -->
<!--    2006.06.14 RDA: Add dir attribute to localization-atts     -->
<!--    2006.06.14 RDA: Add outputclass attribute to most elemetns -->
<!--    2006.11.30 RDA: Add -dita-use-conref-target to enumerated  -->
<!--                      attributes                               -->
<!--    2006.11.30 RDA: Remove #FIXED from DITAArchVersion         -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.01.28 RDA: Added <metadata> to <topicmeta>            -->
<!--    2008.01.30 RDA: Replace @conref defn. with %conref-atts;   -->
<!--    2008.02.01 RDA: Added keys attributes, more keyref attrs   -->
<!--    2008.02.12 RDA: Expand relcolspec content model            -->
<!--    2008.02.12 RDA: Modify imbeds to use specific 1.2 version  -->
<!--    2008.02.12 RDA: Add navtitle to topicmeta                  -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!--    2010.09.20 RDA: Bring linktext content in sync with topic  -->
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
                                  '1.3'
  "
>



<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % map         "map"                                         >
<!ENTITY % navref      "navref"                                      >
<!ENTITY % topicref    "topicref"                                    >
<!ENTITY % anchor      "anchor"                                      >
<!ENTITY % reltable    "reltable"                                    >
<!ENTITY % relheader   "relheader"                                   >
<!ENTITY % relcolspec  "relcolspec"                                  >
<!ENTITY % relrow      "relrow"                                      >
<!ENTITY % relcell     "relcell"                                     >
<!ENTITY % topicmeta   "topicmeta"                                   >
<!ENTITY % shortdesc   "shortdesc"                                   >
<!ENTITY % linktext    "linktext"                                    >
<!ENTITY % searchtitle "searchtitle"                                 >
<!ENTITY % ux-window   "ux-window"                                   >

<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->  

<!ENTITY % topicref-atts
              "collection-type
                          (choice |
                           family |
                           sequence |
                           unordered |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               cascade
                          CDATA
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED
               linking
                          (none |
                           normal |
                           sourceonly |
                           targetonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               print
                          (no |
                           printonly |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               chunk
                          CDATA
                                    #IMPLIED
               keyscope
                          CDATA
                                    #IMPLIED"
>
<!ENTITY % topicref-atts-no-toc
              "collection-type
                          (choice |
                           family |
                           sequence |
                           unordered |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               cascade
                          CDATA
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    'resource-only'
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED
               linking
                          (none |
                           normal |
                           sourceonly |
                           targetonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    'no'
               print
                          (no |
                           printonly |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               chunk
                          CDATA
                                    #IMPLIED
               keyscope
                          CDATA
                                    #IMPLIED"
>
<!ENTITY % topicref-atts-no-toc-no-keyscope
              "collection-type
                          (choice |
                           family |
                           sequence |
                           unordered |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               cascade
                          CDATA
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               format
                          CDATA
                                    #IMPLIED
               linking
                          (none |
                           normal |
                           sourceonly |
                           targetonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    'no'
               print
                          (no |
                           printonly |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               chunk
                          CDATA
                                    #IMPLIED"
>
<!ENTITY % topicref-atts-without-format
              "collection-type
                          (choice |
                           family |
                           sequence |
                           unordered |
                           -dita-use-conref-target)
                                    #IMPLIED
               type
                          CDATA
                                    #IMPLIED
               cascade
                          CDATA
                                    #IMPLIED
               processing-role
                          (normal |
                           resource-only |
                           -dita-use-conref-target)
                                    #IMPLIED
               scope
                          (external |
                           local |
                           peer |
                           -dita-use-conref-target)
                                    #IMPLIED
               locktitle
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               linking
                          (none |
                           normal |
                           sourceonly |
                           targetonly |
                           -dita-use-conref-target)
                                    #IMPLIED
               toc
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               print
                          (no |
                           printonly |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               search
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               chunk
                          CDATA
                                    #IMPLIED
               keyscope
                          CDATA
                                    #IMPLIED"
>

    
<!-- ============================================================= -->
<!--                    MODULES CALLS                              -->
<!-- ============================================================= -->
  

<!--           Content elements common to map and topic            -->
<!ENTITY % commonElements-def
  PUBLIC "-//OASIS//ELEMENTS DITA 1.3 Common Elements//EN"
         "commonElements.mod"
>%commonElements-def;

<!--                       MetaData Elements                       -->
<!ENTITY % metaDecl-def
  PUBLIC "-//OASIS//ELEMENTS DITA 1.3 Metadata//EN"
         "metaDecl.mod"
>%metaDecl-def;
      
  
<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->

<!ENTITY included-domains 
  "" 
> 

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Map                             -->
<!ENTITY % map.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%anchor; |
                          %data.elements.incl; |
                          %navref; |
                          %reltable; |
                          %topicref;)*)"
>
<!ENTITY % map.attributes
              "title
                          CDATA
                                    #IMPLIED
               id
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
<!ELEMENT  map %map.content;>
<!ATTLIST  map %map.attributes;
                 %arch-atts;
                 domains 
                        CDATA
                                  "&included-domains;"
>


<!--                    LONG NAME: Navigation Reference            -->
<!ENTITY % navref.content
                       "EMPTY"
>
<!ENTITY % navref.attributes
              "%univ-atts;
               keyref
                          CDATA
                                    #IMPLIED
               mapref
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  navref %navref.content;>
<!ATTLIST  navref %navref.attributes;>


<!--                    LONG NAME: Topic Reference                 -->
<!ENTITY % topicref.content
                       "((%topicmeta;)?,
                         (%anchor; |
                          %data.elements.incl; |
                          %navref; |
                          %topicref;)*)"
>
<!ENTITY % topicref.attributes
              "navtitle
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
               query
                          CDATA
                                    #IMPLIED
               copy-to
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED
               %topicref-atts;
               %univ-atts;"
>
<!ELEMENT  topicref %topicref.content;>
<!ATTLIST  topicref %topicref.attributes;>


<!--                    LONG NAME: Anchor                          -->
<!ENTITY % anchor.content
                       "EMPTY"
>
<!ENTITY % anchor.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %localization-atts;
               id
                          ID
                                    #REQUIRED
               %conref-atts;
               %select-atts;"
>
<!ELEMENT  anchor %anchor.content;>
<!ATTLIST  anchor %anchor.attributes;>


<!--                    LONG NAME: Relationship Table              -->
<!ENTITY % reltable.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%relheader;)?,
                         (%relrow;)+)"
>
<!ENTITY % reltable.attributes
              "title
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED
               %topicref-atts-no-toc-no-keyscope;
               %univ-atts;"
>
<!ELEMENT  reltable %reltable.content;>
<!ATTLIST  reltable %reltable.attributes;>


<!--                    LONG NAME: Relationship Header             -->
<!ENTITY % relheader.content
                       "(%relcolspec;)+"
>
<!ENTITY % relheader.attributes
              "%univ-atts;"
>
<!ELEMENT  relheader %relheader.content;>
<!ATTLIST  relheader %relheader.attributes;>


<!--                    LONG NAME: Relationship Column Specification -->
<!ENTITY % relcolspec.content
                       "((%title;)?,
                         (%topicmeta;)?,
                         (%topicref;)*)"
>
<!ENTITY % relcolspec.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %topicref-atts-no-toc-no-keyscope;
               %univ-atts;"
>
<!ELEMENT  relcolspec %relcolspec.content;>
<!ATTLIST  relcolspec %relcolspec.attributes;>


<!--                    LONG NAME: Relationship Table Row          -->
<!ENTITY % relrow.content
                       "(%relcell;)*"
>
<!ENTITY % relrow.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %univ-atts;"
>
<!ELEMENT  relrow %relrow.content;>
<!ATTLIST  relrow %relrow.attributes;>


<!--                    LONG NAME: Relationship Table Cell         -->
<!ENTITY % relcell.content
                       "(%topicref; |
                         %data.elements.incl;)*"
>
<!ENTITY % relcell.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %topicref-atts-no-toc-no-keyscope;
               %univ-atts;"
>
<!ELEMENT  relcell %relcell.content;>
<!ATTLIST  relcell %relcell.attributes;>


<!--                    LONG NAME: Topic Metadata                  -->
<!ENTITY % topicmeta.content
                       "((%navtitle;)?,
                         (%linktext;)?,
                         (%searchtitle;)?,
                         (%shortdesc;)?,
                         (%author;)*,
                         (%source;)?,
                         (%publisher;)?,
                         (%copyright;)*,
                         (%critdates;)?,
                         (%permissions;)?,
                         (%metadata;)*,
                         (%audience;)*,
                         (%category;)*,
                         (%keywords;)*,
                         (%prodinfo;)*,
                         (%othermeta;)*,
                         (%resourceid;)*,
                         (%ux-window;)*,
                         (%data.elements.incl; |
                          %foreign.unknown.incl;)*)"
>
<!ENTITY % topicmeta.attributes
              "lockmeta
                          (no |
                           yes |
                           -dita-use-conref-target)
                                    #IMPLIED
               %univ-atts;"
>
<!ELEMENT  topicmeta %topicmeta.content;>
<!ATTLIST  topicmeta %topicmeta.attributes;>


<!--                    LONG NAME: Short Description               -->
<!ENTITY % shortdesc.content
                       "(%title.cnt; |
                         %xref;)*"
>
<!ENTITY % shortdesc.attributes
              "%univ-atts;
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!ELEMENT  shortdesc %shortdesc.content;>
<!ATTLIST  shortdesc %shortdesc.attributes;>


<!--                    LONG NAME: Link Text                       -->
<!ENTITY % linktext.content
                       "(%words.cnt; |
                         %ph;)*"
>
<!ENTITY % linktext.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %univ-atts;"
>
<!ELEMENT  linktext %linktext.content;>
<!ATTLIST  linktext %linktext.attributes;>


<!--                    LONG NAME: Search Title                    -->
<!ENTITY % searchtitle.content
                       "(%words.cnt;)*"
>
<!ENTITY % searchtitle.attributes
              "outputclass
                          CDATA
                                    #IMPLIED
               %univ-atts;"
>
<!ELEMENT  searchtitle %searchtitle.content;>
<!ATTLIST  searchtitle %searchtitle.attributes;>


<!--                    LONG NAME: User Experience Window          -->
<!ENTITY % ux-window.content
                       "EMPTY"
>
<!ENTITY % ux-window.attributes
              "name
                          CDATA
                                    #REQUIRED
               top
                          CDATA
                                    #IMPLIED
               left
                          CDATA
                                    #IMPLIED
               height
                          CDATA
                                    #IMPLIED
               width
                          CDATA
                                    #IMPLIED
               on-top
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    'no'
               features
                          CDATA
                                    #IMPLIED
               relative
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    'no'
               full-screen
                          (yes |
                           no |
                           -dita-use-conref-target)
                                    'no'
               %id-atts;
               %select-atts;"
>
<!ELEMENT  ux-window %ux-window.content;>
<!ATTLIST  ux-window %ux-window.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  map          %global-atts;  class CDATA "- map/map "          >
<!ATTLIST  navref       %global-atts;  class CDATA "- map/navref "       >
<!ATTLIST  topicref     %global-atts;  class CDATA "- map/topicref "     >
<!ATTLIST  anchor       %global-atts;  class CDATA "- map/anchor "       >
<!ATTLIST  reltable     %global-atts;  class CDATA "- map/reltable "     >
<!ATTLIST  relheader    %global-atts;  class CDATA "- map/relheader "    >
<!ATTLIST  relcolspec   %global-atts;  class CDATA "- map/relcolspec "   >
<!ATTLIST  relrow       %global-atts;  class CDATA "- map/relrow "       >
<!ATTLIST  relcell      %global-atts;  class CDATA "- map/relcell "      >
<!ATTLIST  topicmeta    %global-atts;  class CDATA "- map/topicmeta "    >
<!ATTLIST  linktext     %global-atts;  class CDATA "- map/linktext "     >
<!ATTLIST  searchtitle  %global-atts;  class CDATA "- map/searchtitle "  >
<!ATTLIST  shortdesc    %global-atts;  class CDATA "- map/shortdesc "    >
<!ATTLIST  ux-window    %global-atts;  class CDATA "- map/ux-window "    >

<!-- ================== End of DITA Map Module ==================== -->
 