<!--  
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA User Interface Domain//EN"
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
 |   1.1.3a bug fix: converted words.cnt back to PCDATA for wintitle
 |                   (elements derived from keyword must have content models in kind)
 *-->

<!ENTITY % uicontrol "uicontrol">
<!ENTITY % wintitle "wintitle">
<!ENTITY % menucascade "menucascade">
<!ENTITY % shortcut "shortcut">
<!ENTITY % screen "screen">


<!--ui keyword types-->
<!ELEMENT uicontrol (%words.cnt;|%image;|%shortcut;)*>
<!ATTLIST uicontrol       keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT wintitle (#PCDATA)*>
<!ATTLIST wintitle        keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT menucascade (%uicontrol;)+>
<!ATTLIST menucascade     keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!ELEMENT shortcut (#PCDATA)>
<!ATTLIST shortcut        keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT screen         (#PCDATA | %basic.ph.notm; | %txt.incl;)*>
<!ATTLIST screen          %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>


<!-- specialization class declarations -->
<!ATTLIST uicontrol       %global-atts; class CDATA "+ topic/ph ui-d/uicontrol ">
<!ATTLIST wintitle        %global-atts; class CDATA "+ topic/keyword ui-d/wintitle ">
<!ATTLIST menucascade     %global-atts; class CDATA "+ topic/ph ui-d/menucascade ">
<!ATTLIST shortcut        %global-atts; class CDATA "+ topic/keyword ui-d/shortcut ">
<!ATTLIST screen          %global-atts; class CDATA "+ topic/pre ui-d/screen ">
