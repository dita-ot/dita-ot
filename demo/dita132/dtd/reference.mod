<!--
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Reference//EN"
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
 |   1.1.3a bug fix: revised "DTDVersion" to match release version (consistency);
 |                   revised refbody attlist to match other infotype's body attlists (consistency)
 *-->

<!ENTITY DTDVersion 'V1.1.3' >



<!-- ============ Specialization of declared elements ============ -->
<!ENTITY % referenceClasses SYSTEM "reference_class.ent">
<!--%referenceClasses;-->

<!ENTITY % refbody "refbody">
<!ENTITY % refsyn "refsyn">
<!ENTITY % properties "properties">
<!ENTITY % property "property">
<!ENTITY % proptype "proptype">
<!ENTITY % propvalue "propvalue">
<!ENTITY % propdesc "propdesc">
<!ENTITY % prophead "prophead">
<!ENTITY % proptypehd "proptypehd">
<!ENTITY % propvaluehd "propvaluehd">
<!ENTITY % propdeschd "propdeschd">

<!ENTITY % reference-info-types "%info-types;">
<!ENTITY included-domains "">

<!ELEMENT reference      (%title;, (%titlealts;)?, (%shortdesc;)?, (%prolog;)?, %refbody;, (%related-links;)?, (%reference-info-types;)* )>
<!ATTLIST reference       id ID #REQUIRED
                          conref CDATA #IMPLIED
                          %select-atts;
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          DTDVersion CDATA #FIXED "&DTDVersion;"
                          domains CDATA "&included-domains;"
>

<!ELEMENT refbody        ((%section; | %refsyn; | %example; | %table; | %simpletable; | %properties;)*)>
<!ATTLIST refbody         %id-atts;
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          outputclass CDATA #IMPLIED
>


    <!-- unique sections:  -->
<!ELEMENT refsyn         (%section.cnt;)* > <!-- syntax content -->
<!ATTLIST refsyn          spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT properties     ((%prophead;)?, (%property;)+)>
<!ATTLIST properties      relcolwidth CDATA #IMPLIED
                          keycol NMTOKEN #IMPLIED
                          refcols NMTOKENS #IMPLIED
                          %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!ELEMENT prophead       ((%proptypehd;)?, (%propvaluehd;)?, (%propdeschd;)?)>
<!ATTLIST prophead        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT proptypehd     (%tblcell.cnt;)*>
<!ATTLIST proptypehd      %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT propvaluehd    (%tblcell.cnt;)*>
<!ATTLIST propvaluehd     %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT propdeschd     (%tblcell.cnt;)*>
<!ATTLIST propdeschd      %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>


<!ELEMENT property       ((%proptype;)?, (%propvalue;)?, (%propdesc;)?)>
<!ATTLIST property        %univ-atts;
                          outputclass CDATA #IMPLIED

>
<!ELEMENT proptype       (%ph.cnt;)*>
<!ATTLIST proptype        %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT propvalue      (%ph.cnt;)*>
<!ATTLIST propvalue       %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT propdesc       (%desc.cnt;)*>
<!ATTLIST propdesc        %univ-atts;
                          specentry CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>




<!--specialization attributes-->

<!ATTLIST reference   %global-atts; class  CDATA "- topic/topic       reference/reference ">
<!ATTLIST refbody     %global-atts; class  CDATA "- topic/body        reference/refbody ">
<!ATTLIST refsyn      %global-atts; class  CDATA "- topic/section     reference/refsyn ">
<!ATTLIST properties  %global-atts; class  CDATA "- topic/simpletable reference/properties ">
<!ATTLIST property    %global-atts; class  CDATA "- topic/strow       reference/property ">
<!ATTLIST proptype    %global-atts; class  CDATA "- topic/stentry     reference/proptype ">
<!ATTLIST propvalue   %global-atts; class  CDATA "- topic/stentry     reference/propvalue ">
<!ATTLIST propdesc    %global-atts; class  CDATA "- topic/stentry     reference/propdesc ">

<!ATTLIST prophead    %global-atts; class  CDATA "- topic/sthead      reference/prophead ">
<!ATTLIST proptypehd  %global-atts; class  CDATA "- topic/stentry     reference/proptypehd ">
<!ATTLIST propvaluehd %global-atts; class  CDATA "- topic/stentry     reference/propvaluehd ">
<!ATTLIST propdeschd  %global-atts; class  CDATA "- topic/stentry     reference/propdeschd ">

