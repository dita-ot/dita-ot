<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!ENTITY % apipackage       "apipackage">
<!ENTITY % apiclassifier    "apiclassifier">
<!ENTITY % apioperation     "apioperation">
<!ENTITY % apivalue         "apivalue">

<!ELEMENT apipackage     (#PCDATA)>
<!ATTLIST apipackage      href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT apiclassifier  (#PCDATA)>
<!ATTLIST apiclassifier   href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT apioperation   (#PCDATA)>
<!ATTLIST apioperation    href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>

<!ELEMENT apivalue       (#PCDATA)>
<!ATTLIST apivalue        href CDATA #IMPLIED
                      keyref CDATA #IMPLIED
                      type   CDATA  #IMPLIED
                      %univ-atts;
                      format        CDATA   #IMPLIED
                      scope (local | peer | external) #IMPLIED
                      outputclass CDATA #IMPLIED
>


<!ATTLIST apipackage   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apipackage ">
<!ATTLIST apiclassifier   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apiclassifier ">
<!ATTLIST apioperation   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apioperation ">
<!ATTLIST apivalue   %global-atts;
    class  CDATA "+ topic/xref pr-d/xref api-d/apivalue ">
