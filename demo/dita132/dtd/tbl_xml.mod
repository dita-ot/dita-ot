<!--  
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ENTITIES DITA CALS Tables//EN"
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
 *-->

<![IGNORE[
  <!ELEMENT ph             (#PCDATA | ph)*>
 <!ENTITY % tblcell.cnt    "#PCDATA | ph">
 <!ENTITY % title.cnt      "#PCDATA | ph">
 <!ELEMENT title           (%title.cnt;)* > <!-- referenced within the table declaration -->
]]>


<!ENTITY % table "table">
<!ENTITY % tgroup "tgroup">
<!ENTITY % colspec "colspec">
<!ENTITY % spanspec "spanspec">
<!ENTITY % thead "thead">
<!ENTITY % tfoot "tfoot">
<!ENTITY % tbody "tbody">
<!ENTITY % row "row">
<!ENTITY % entry "entry">



  <!ENTITY % yesorno   "NMTOKEN" >

<!-- Note: frame and scale are members of PE "display-atts" in topic dtd -->
<!-- NOte: "label" and "title" are interchangeable for now; migrate to label ASAP -->

  <!ELEMENT table        (((%title;)?, (%desc;)?)?, (%tgroup;)+) >
  <!ATTLIST table         %display-atts;
                          colsep         %yesorno;             #IMPLIED
                          rowsep         %yesorno;             #IMPLIED 
                          rowheader   (firstcol | norowheader) #IMPLIED
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!--ELEMENT title      (#PCDATA) --> <!-- this is defined in the parent DTD -->

  <!ELEMENT tgroup       ((%colspec;)*, (%spanspec;)*, (%thead;)?, (%tfoot;)?, %tbody;) >
  <!ATTLIST tgroup        cols       NMTOKEN                #REQUIRED
                          colsep     %yesorno;              #IMPLIED
                          rowsep     %yesorno;              #IMPLIED
                          align      (left | right |
                                      center | justify )     #IMPLIED
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!ELEMENT colspec      EMPTY >
  <!ATTLIST colspec       colnum      NMTOKEN                #IMPLIED
                          colname     NMTOKEN                #IMPLIED
                          align       (left | right |
                                       center | justify )     #IMPLIED
                          colwidth    CDATA                  #IMPLIED
                          colsep      %yesorno;              #IMPLIED
                          rowsep      %yesorno;              #IMPLIED 
>

  <!ELEMENT spanspec     EMPTY >
  <!ATTLIST spanspec      namest      NMTOKEN                #REQUIRED
                          nameend     NMTOKEN                #REQUIRED
                          spanname    NMTOKEN                #REQUIRED
                          align       (left | right |
                                       center | justify )    #IMPLIED
                          colsep      %yesorno;              #IMPLIED
                          rowsep      %yesorno;              #IMPLIED
>

  <!ELEMENT thead        ((%colspec;)*, (%row;)+) >
  <!ATTLIST thead         valign      (top | middle | bottom) #IMPLIED
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!ELEMENT tfoot        ((%colspec;)*, (%row;)+) >
  <!ATTLIST tfoot         valign      (top | middle | bottom) #IMPLIED
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!ELEMENT tbody        (%row;)+ >
  <!ATTLIST tbody         valign      (top | middle | bottom) #IMPLIED
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!ELEMENT row          (%entry;)+ >
  <!ATTLIST row           rowsep      %yesorno;              #IMPLIED
                          valign      (top | bottom | middle) #IMPLIED 
                          outputclass CDATA #IMPLIED
                          %univ-atts;
>

  <!ELEMENT entry        (%tblcell.cnt;)* >
  <!ATTLIST entry         colname     NMTOKEN                #IMPLIED
                          namest      NMTOKEN                #IMPLIED
                          nameend     NMTOKEN                #IMPLIED
                          spanname    NMTOKEN                #IMPLIED
                          morerows    NMTOKEN                #IMPLIED
                          colsep      %yesorno;              #IMPLIED
                          rowsep      %yesorno;              #IMPLIED
                          valign      (top | bottom | middle) #IMPLIED
                          align       (left | right |
                                       center | justify )     #IMPLIED 
                          rev CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST table    %global-atts; class CDATA "- topic/table ">
<!ATTLIST tgroup   %global-atts; class CDATA "- topic/tgroup ">
<!ATTLIST colspec  %global-atts; class CDATA "- topic/colspec ">
<!ATTLIST spanspec %global-atts; class CDATA "- topic/spanspec ">
<!ATTLIST thead    %global-atts; class CDATA "- topic/thead ">
<!ATTLIST tfoot    %global-atts; class CDATA "- topic/tfoot ">
<!ATTLIST tbody    %global-atts; class CDATA "- topic/tbody ">
<!ATTLIST row      %global-atts; class CDATA "- topic/row ">
<!ATTLIST entry    %global-atts; class CDATA "- topic/entry ">
