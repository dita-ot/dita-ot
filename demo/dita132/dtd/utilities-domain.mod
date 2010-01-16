<!--  
 | (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Utilities Domain//EN"
 |
 | Release history (vrm):
 |   1.0.0 Initial release on developerWorks, March 2001 (dita00.zip)
 |   1.0.1 fix 1 on developerWorks, October 2001 (dita01.zip)
 |   1.0.2 consolidated redesign December 2001
 |   1.0.3 fix 1, dtd freeze for UCD-1 January 2002
 |   1.1.0 Release 1 March 2002 (dita10.zip) (dita10.zip)
 |   1.1.1 Release 1.1 December 2002
 |   1.1.2 Release 1.2 June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fix: created new att set to define translate="no" default for shape and coords
 *-->



<!-- ====  the imagemap model derives from fig ==== -->

<!-- convert element name into extensible parameter entity -->
<!ENTITY % imagemap "imagemap">
<!ENTITY % area "area">
<!ENTITY % shape "shape">
<!ENTITY % coords "coords">


<!-- provide an alternative univ-atts that sets translate to default 'no'-->
<!ENTITY % univ-atts-translate-no
                         '%id-atts;
                          platform CDATA #IMPLIED
                          product CDATA #IMPLIED
                          audience CDATA #IMPLIED
                          otherprops CDATA #IMPLIED
                          importance ( obsolete | deprecated | optional | default | low | normal | high | recommended | required | urgent ) #IMPLIED
                          rev CDATA #IMPLIED
                          status (new|changed|deleted|unchanged) #IMPLIED
                          translate (yes|no) "no"
                          xml:lang NMTOKEN #IMPLIED'
>

<!-- declare the structure and content models -->
<!ELEMENT imagemap       ((%image;), (%area;)+)>
<!ATTLIST imagemap        %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT area           ((%shape;), (%coords;), (%xref;))>
<!ATTLIST area            %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT shape          (#PCDATA)>
<!ATTLIST shape           keyref NMTOKEN #IMPLIED
                          %univ-atts-translate-no;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT coords         (%words.cnt;)*>
<!ATTLIST coords          keyref NMTOKEN #IMPLIED
                          %univ-atts-translate-no;
                          outputclass CDATA #IMPLIED
>
<!-- rounding out the model, xref and image are used as is -->


<!-- declare the class derivations -->
<!ATTLIST imagemap %global-atts; class CDATA "+ topic/fig ut-d/imagemap ">
<!ATTLIST area     %global-atts; class CDATA "+ topic/figgroup ut-d/area ">
<!ATTLIST shape    %global-atts; class CDATA "+ topic/keyword ut-d/shape ">
<!ATTLIST coords   %global-atts; class CDATA "+ topic/ph ut-d/coords ">
