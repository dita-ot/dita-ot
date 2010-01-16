<?xml version="1.0" encoding="UTF-8"?>

<!-- Element name entities. -->

<!--
    <public publicId="-//Moldflow//ELEMENTS DITA 1.1 Tree Domain 1//EN" uri="dtd/treeDomain1.mod"/>
    <sysem systemId="http://www.moldflow.com/schema/dtd/dita/1.1/tree/1/treeDomain1.mod" uri="dtd/treeDomain1.mod"/>
-->

<!ENTITY % tree "tree">
<!ENTITY % node "node">
<!ENTITY % treenote "treenote">
<!ENTITY % treenoteref "treenoteref">

<!-- Content models. -->

<!ELEMENT tree  ((%title;)?, (%node; | %treenote; | %treenoteref;)* ) >
<!ATTLIST tree
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass CDATA                            #IMPLIED    >
             
<!ELEMENT node ((%title;)?, (%node; | %treenote; | %treenoteref; | %figgroup; | %xref; | %fn; | %ph; | %keyword;)*) >
<!ATTLIST node             %univ-atts;
             outputclass CDATA                            #IMPLIED    >

<!ELEMENT treenote       (#PCDATA | %basic.ph;)*                      >     
<!ATTLIST treenote        
             callout    CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass                         CDATA                            #IMPLIED    >
                        
<!ELEMENT treenoteref     EMPTY >
<!ATTLIST treenoteref      
             href       CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass                      CDATA                            #IMPLIED    >
                        
                        
<!-- Class attributes. -->

<!ATTLIST tree %global-atts; class CDATA "+ topic/fig tree-d/tree " >
<!ATTLIST node %global-atts; class CDATA "+ topic/figgroup tree-d/node " >
<!ATTLIST treenote %global-atts; class CDATA "+ topic/fn tree-d/treenote " >
<!ATTLIST treenoteref %global-atts; class CDATA "+ topic/xref tree-d/treenoteref " >
