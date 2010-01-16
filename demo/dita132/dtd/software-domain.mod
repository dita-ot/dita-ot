<!--  
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Software Domain//EN"
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
 |   1.1.3a bug fix: replaced single quotes in msgblock/@xml:space def with double quotes (syntax)
 |                   converted words.cnt back to PCDATA for msgnum, cmdname, varname
 |                   (elements derived from keyword must have content models in kind)
 *-->


<!ENTITY % msgph "msgph">
<!ENTITY % msgblock "msgblock">
<!ENTITY % msgnum "msgnum">
<!ENTITY % cmdname "cmdname">
<!ENTITY % varname "varname">
<!ENTITY % filepath "filepath">
<!ENTITY % userinput "userinput">
<!ENTITY % systemoutput "systemoutput">


<!-- software domain vocabulary -->
<!ELEMENT msgph          (%words.cnt;)* >
<!ATTLIST msgph           %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT msgblock       (%words.cnt;)* >
<!ATTLIST msgblock        %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED "preserve"
>
<!ELEMENT msgnum         (#PCDATA)>
<!ATTLIST msgnum          keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT cmdname        (#PCDATA)>
<!ATTLIST cmdname         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT varname        (#PCDATA)>
<!ATTLIST varname         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT filepath       (%words.cnt;)* >
<!ATTLIST filepath        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT userinput      (%words.cnt;)* >
<!ATTLIST userinput       %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT systemoutput   (%words.cnt;)* >
<!ATTLIST systemoutput    %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!ATTLIST msgph          %global-atts; class CDATA "+ topic/ph sw-d/msgph ">
<!ATTLIST msgblock       %global-atts; class CDATA "+ topic/pre sw-d/msgblock ">
<!ATTLIST msgnum         %global-atts; class CDATA "+ topic/keyword sw-d/msgnum ">
<!ATTLIST cmdname        %global-atts; class CDATA "+ topic/keyword sw-d/cmdname ">
<!ATTLIST varname        %global-atts; class CDATA "+ topic/keyword sw-d/varname ">
<!ATTLIST filepath       %global-atts; class CDATA "+ topic/ph sw-d/filepath ">
<!ATTLIST userinput      %global-atts; class CDATA "+ topic/ph sw-d/userinput ">
<!ATTLIST systemoutput   %global-atts; class CDATA "+ topic/ph sw-d/systemoutput ">

