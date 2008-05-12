<!--  
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ENTITIES DITA Metadata//EN"
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
 |   1.1.3a architecture: introduced tm into keyword to support conrefing of product names
 |          bug fix: cut keyword def from meta_xml; paste into topic.mod unchanged, put into map.mod with no tm
 *-->

<!ENTITY % author "author">
<!ENTITY % source "source">
<!ENTITY % publisher "publisher">
<!ENTITY % copyright "copyright">
<!ENTITY % copyryear "copyryear">
<!ENTITY % copyrholder "copyrholder">
<!ENTITY % critdates "critdates">
<!ENTITY % created "created">
<!ENTITY % revised "revised">
<!ENTITY % permissions "permissions">
<!ENTITY % category "category">
<!ENTITY % audience "audience">
<!ENTITY % keywords "keywords">
<!ENTITY % prodinfo "prodinfo">
<!ENTITY % prodname "prodname">
<!ENTITY % vrmlist "vrmlist">
<!ENTITY % vrm "vrm">
<!ENTITY % brand "brand">
<!ENTITY % series "series">
<!ENTITY % platform "platform">
<!ENTITY % prognum "prognum">
<!ENTITY % featnum "featnum">
<!ENTITY % component "component">
<!ENTITY % othermeta "othermeta">
<!ENTITY % resourceid "resourceid">
<!ENTITY % indexterm "indexterm">


<!ELEMENT author         (%words.cnt;)*>
<!ATTLIST author          href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          type (creator|contributor) #IMPLIED
>
<!ELEMENT source         (%words.cnt;)*>
<!ATTLIST source          href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
>
<!ELEMENT publisher      (%words.cnt;)*>
<!ATTLIST publisher       href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          %select-atts;
>
<!ELEMENT copyright      ((%copyryear;)+,%copyrholder;)>
<!ATTLIST copyright       type (primary|secondary) #IMPLIED>
<!ELEMENT copyryear      EMPTY>
<!ATTLIST copyryear       year %date-format; #REQUIRED
                          %select-atts;
>
<!ELEMENT copyrholder    (%words.cnt;)*>

<!-- all attributes within critdates need to take ISO date format -->
<!ELEMENT critdates      (%created;,(%revised;)*)>
<!ELEMENT created        EMPTY>
<!ATTLIST created         date     %date-format; #REQUIRED
                          golive   %date-format; #IMPLIED
                          expiry   %date-format; #IMPLIED
>
<!ELEMENT revised        EMPTY>
<!ATTLIST revised         modified %date-format; #REQUIRED
                          golive   %date-format; #IMPLIED
                          expiry   %date-format; #IMPLIED
                          %select-atts;
>
<!ELEMENT permissions    EMPTY>

<!ATTLIST permissions     view (internal|classified|all|entitled) #REQUIRED
>
<!-- metadata grouping -->
<!ELEMENT category       (%words.cnt;)*>
<!ATTLIST category        %select-atts;
>
<!ELEMENT audience       EMPTY>
<!ATTLIST audience        type (user|purchaser|administrator|programmer|executive|services|other) #IMPLIED
                          othertype CDATA #IMPLIED
                          job (installing|customizing|administering|programming|using|maintaining|troubleshooting|evaluating|planning|migrating|other) #IMPLIED
                          otherjob CDATA #IMPLIED
                          experiencelevel (novice|general|expert) #IMPLIED
                          name NMTOKEN #IMPLIED
                          %select-atts;
>
<!ELEMENT keywords       (%indexterm;|%keyword;)*>
<!ATTLIST keywords        %id-atts;
                          %select-atts;
>
<!ELEMENT prodinfo       (%prodname;,%vrmlist;,(%brand;|%series;|%platform;|%prognum;|%featnum;|%component;)*)>
<!ATTLIST prodinfo        %select-atts;
>
<!ELEMENT prodname       (%words.cnt;)*>  <!-- applications may validate the content of these dataphrase elements -->
<!ELEMENT vrmlist        (%vrm;)+>
<!ELEMENT vrm            EMPTY>
<!ATTLIST vrm             version CDATA #REQUIRED
                          release CDATA #IMPLIED
                          modification CDATA #IMPLIED
>
<!ELEMENT brand          (%words.cnt;)*>
<!ELEMENT series         (%words.cnt;)*>
<!ELEMENT platform       (%words.cnt;)*>
<!ELEMENT prognum        (%words.cnt;)*>
<!ELEMENT featnum        (%words.cnt;)*>
<!ELEMENT component      (%words.cnt;)*>


<!ELEMENT othermeta      EMPTY > <!-- needs to be HTML-equiv, at least -->
<!ATTLIST othermeta       name CDATA #REQUIRED
                          content CDATA #REQUIRED
                          translate-content (yes|no) #IMPLIED
                          %select-atts;
>

<!ELEMENT resourceid     EMPTY>
<!ATTLIST resourceid     id CDATA #REQUIRED
                         appname CDATA #IMPLIED
>

<!ELEMENT indexterm      (%words.cnt;|%indexterm;)*>   <!-- Index entry -->
<!ATTLIST indexterm       keyref NMTOKEN #IMPLIED
                          %univ-atts;
>

<!--specialization attributes-->

<!ATTLIST author %global-atts; class CDATA "- topic/author ">
<!ATTLIST source %global-atts; class CDATA "- topic/source ">
<!ATTLIST publisher %global-atts; class CDATA "- topic/publisher ">
<!ATTLIST copyright %global-atts; class CDATA "- topic/copyright ">
<!ATTLIST copyryear %global-atts; class CDATA "- topic/copyryear ">
<!ATTLIST copyrholder %global-atts; class CDATA "- topic/copyrholder ">
<!ATTLIST critdates %global-atts; class CDATA "- topic/critdates ">
<!ATTLIST created %global-atts; class CDATA "- topic/created ">
<!ATTLIST revised %global-atts; class CDATA "- topic/revised ">
<!ATTLIST permissions %global-atts; class CDATA "- topic/permissions ">
<!ATTLIST category %global-atts; class CDATA "- topic/category ">
<!ATTLIST audience %global-atts; class CDATA "- topic/audience ">
<!ATTLIST keywords %global-atts; class CDATA "- topic/keywords ">
<!ATTLIST prodinfo %global-atts; class CDATA "- topic/prodinfo ">
<!ATTLIST prodname %global-atts; class CDATA "- topic/prodname ">
<!ATTLIST vrmlist %global-atts; class CDATA "- topic/vrmlist ">
<!ATTLIST vrm %global-atts; class CDATA "- topic/vrm ">
<!ATTLIST brand %global-atts; class CDATA "- topic/brand ">
<!ATTLIST series %global-atts; class CDATA "- topic/series ">
<!ATTLIST platform %global-atts; class CDATA "- topic/platform ">
<!ATTLIST prognum %global-atts; class CDATA "- topic/prognum ">
<!ATTLIST featnum %global-atts; class CDATA "- topic/featnum ">
<!ATTLIST component %global-atts; class CDATA "- topic/component ">
<!ATTLIST othermeta %global-atts; class CDATA "- topic/othermeta ">
<!ATTLIST resourceid %global-atts; class CDATA "- topic/resourceid ">
<!ATTLIST indexterm %global-atts; class CDATA "- topic/indexterm ">
