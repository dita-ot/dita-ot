<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!ENTITY % javapackage    "javapackage">
<!ENTITY % javaclass      "javaclass">
<!ENTITY % javainterface  "javainterface">
<!ENTITY % javafield      "javafield">
<!ENTITY % javamethod     "javamethod">

<!ELEMENT javapackage    (#PCDATA)>
<!ATTLIST javapackage     href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT javaclass      (#PCDATA)>
<!ATTLIST javaclass       href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT javainterface  (#PCDATA)>
<!ATTLIST javainterface   href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT javafield      (#PCDATA)>
<!ATTLIST javafield       href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT javamethod     (#PCDATA)>
<!ATTLIST javamethod      href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>


<!ATTLIST javapackage   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apipackage javaapi-d/javapackage ">
<!ATTLIST javaclass   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apiclassifier javaapi-d/javaclass ">
<!ATTLIST javainterface   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apiclassifier javaapi-d/javainterface ">
<!ATTLIST javafield   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apivalue javaapi-d/javafield ">
<!ATTLIST javamethod   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apioperation javaapi-d/javamethod ">
