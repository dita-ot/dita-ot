<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    XNAL Domain                                       -->
<!--  VERSION:   1.O                                               -->
<!--  DATE:      May 2006                                          -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA XNAL Domain//EN"
      Delivered as file "xnalDomain.mod"                           -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributed    -->
<!--             for Map Group Domain                              -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005.                    -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->


<!ENTITY % authorinformation "authorinformation">
<!ENTITY % namedetails       "namedetails">
<!-- Is this one really needed as a container for organizationname? -->
<!ENTITY % organizationnamedetails    "organizationnamedetails">
<!ENTITY % resource        "resource">
<!ENTITY % organizationname         "organizationname">
<!ENTITY % personname          "personname">
<!--<!ENTITY % preceedingtitle       "preceedingtitle">-->
<!ENTITY % honorific       "honorific">
<!ENTITY % firstname       "firstname">
<!ENTITY % middlename      "middlename">
<!ENTITY % lastname      "lastname">
<!ENTITY % generationidentifier         "generationidentifier">
<!ENTITY % otherinfo       "otherinfo">

<!ENTITY % addressdetails         "addressdetails">
<!--<!ENTITY % address         "address">-->
<!ENTITY % locality            "locality">
<!ENTITY % localityname            "localityname">
<!ENTITY % administrativearea       "administrativearea">
<!ENTITY % thoroughfare      "thoroughfare">
<!ENTITY % postalcode      "postalcode">
<!ENTITY % country         "country">

<!ENTITY % personinfo      "personinfo">
<!ENTITY % organizationinfo      "organizationinfo">
<!ENTITY % contactnumbers  "contactnumbers">
<!ENTITY % contactnumber   "contactnumber">
<!--<!ENTITY % officephone     "officephone">
<!ENTITY % fax             "fax">
<!ENTITY % cellular        "cellular">
<!ENTITY % urlphone        "urlphone">-->
<!ENTITY % emailaddresses  "emailaddresses">
<!ENTITY % emailaddress  "emailaddress">
<!ENTITY % urls  "urls">
<!ENTITY % url  "url">

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->
                      
<!-- many items based on data element: copied here for reference -->
<!-- <!ELEMENT data      (#PCDATA|%keyword;|%term;|%image;|%object;|%ph;|%data;)*> -->

<!-- Based on Chris's xCIL/xNAL V3 spreadsheet: authorinformation will contain
     only personinfo and organizationinfo. The names and addresses will be grouped
     within those containers, to keep an a name/address with the person or organization. -->
<!ELEMENT authorinformation      ((%personinfo; | %organizationinfo;)*)>
<!ATTLIST authorinformation     
             %univ-atts;
             href       CDATA                             #IMPLIED
             keyref     CDATA                             #IMPLIED
             type       (creator | contributor)           #IMPLIED   >

<!ELEMENT namedetails      ((%personname; | %organizationnamedetails;)*)>
<!ATTLIST namedetails    %data-element-atts;>

<!ELEMENT organizationnamedetails      
                 ((%organizationname;)?,
                             (%resource;)*,
                             (%otherinfo;)*)      >  
<!ATTLIST organizationnamedetails              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!-- BKINFO USED xreftext.cnt -->
<!-- Not needed if organizationnamedetails is removed; not sure if it is needed there -->
<!ELEMENT resource        (%words.cnt;)*>
<!ATTLIST resource        href          CDATA   #IMPLIED
                          keyref        CDATA #IMPLIED
                          type          CDATA   #IMPLIED
                          %univ-atts;
                          format        CDATA   #IMPLIED
                          scope         (local|peer|external)   "external"
                          outputclass   CDATA   #IMPLIED
>

<!ELEMENT organizationname         (%ph.cnt;)*>
<!ATTLIST organizationname         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT personname            ((%honorific;)?, (%firstname;)*, (%middlename;)*,
                             (%lastname;)*, (%generationidentifier;)?, (%resource;)?, 
                             (%otherinfo;)*)      >  
<!ATTLIST personname %data-element-atts;>

<!--<!ELEMENT preceedingtitle       (#PCDATA)*>
<!ATTLIST preceedingtitle %data-element-atts;>-->

<!ELEMENT honorific       (#PCDATA)*>
<!ATTLIST honorific %data-element-atts;>

<!ELEMENT firstname       (#PCDATA)*>
<!ATTLIST firstname %data-element-atts;>

<!ELEMENT middlename      (#PCDATA)*>
<!ATTLIST middlename %data-element-atts;>

<!ELEMENT lastname        (#PCDATA)*>
<!ATTLIST lastname %data-element-atts;>

<!ELEMENT generationidentifier     (#PCDATA)*>
<!ATTLIST generationidentifier %data-element-atts;>

<!ELEMENT otherinfo       (%words.cnt;)*>
<!ATTLIST otherinfo %data-element-atts;>

<!ELEMENT addressdetails         (%words.cnt;|%locality;|%administrativearea;|%thoroughfare;|%country;)*>
<!ATTLIST addressdetails              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!--<!ELEMENT address         (%words.cnt;)*>
<!ATTLIST address              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >-->

<!ELEMENT locality            (%words.cnt;|%localityname;|%postalcode;)*>
<!ATTLIST locality            keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT localityname    (%words.cnt;)*>
<!ATTLIST localityname    keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT administrativearea       (%words.cnt;)*>
<!ATTLIST administrativearea       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT thoroughfare       (%words.cnt;)*>
<!ATTLIST thoroughfare       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT postalcode      (#PCDATA)*>
<!ATTLIST postalcode      keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT country         (#PCDATA)*>
<!ATTLIST country         keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT personinfo      ((%namedetails;)?, (%addressdetails;)?, (%contactnumbers;)?, (%emailaddresses;)?)      >  
<!ATTLIST personinfo              %data-element-atts;    >

<!ELEMENT organizationinfo  ((%namedetails;)?, (%addressdetails;)?, (%contactnumbers;)?, (%emailaddresses;)?, (%urls;)?)      >  
<!ATTLIST organizationinfo              %data-element-atts;    >

<!-- on advice from Chris Kravogel - remove specific phone types, and have users set
     the value using @type on contactnumber -->
<!--<!ELEMENT contactnumbers      ((%officephone;)*, (%fax;)*, (%cellular;)*, (%urlphone;)*, (%contactnumber;)*)      >  -->
<!ELEMENT contactnumbers      ((%contactnumber;)*)>
<!ATTLIST contactnumbers  %data-element-atts;    >
                        
<!--<!ELEMENT officephone      (#PCDATA)  >  
<!ATTLIST officephone              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >
<!ELEMENT fax      (#PCDATA)  >  
<!ATTLIST fax              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >
<!ELEMENT cellular      (#PCDATA)  >  
<!ATTLIST cellular              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >
<!ELEMENT urlphone      (#PCDATA)  >  
<!ATTLIST urlphone              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >-->

<!ELEMENT contactnumber      (#PCDATA)      >  
<!ATTLIST contactnumber              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >
                        
<!ELEMENT emailaddresses      (%emailaddress;)*      >  
<!ATTLIST emailaddresses  %data-element-atts;    >

<!ELEMENT emailaddress      (%words.cnt;)*      >  
<!ATTLIST emailaddress    %data-element-atts;    >

<!ELEMENT urls      (%url;)*      >  
<!ATTLIST urls  %data-element-atts;    >

<!ELEMENT url      (%words.cnt;)*      >  
<!ATTLIST url    %data-element-atts;    >

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST authorinformation %global-atts;
        class CDATA "- topic/author xnal-d/authorinformation ">
<!ATTLIST namedetails %global-atts;
        class CDATA "- topic/data xnal-d/namedetails ">
<!ATTLIST organizationnamedetails %global-atts;
        class CDATA "- topic/ph xnal-d/organizationnamedetails ">
<!ATTLIST resource %global-atts;
        class CDATA "- topic/xref xnal-d/resource ">
<!ATTLIST organizationname %global-atts;
        class CDATA "- topic/ph xnal-d/organizationname ">
<!ATTLIST otherinfo %global-atts;
        class CDATA "- topic/data xnal-d/otherinfo ">
<!ATTLIST personname %global-atts;
        class CDATA "- topic/data xnal-d/personname ">
<!--<!ATTLIST preceedingtitle %global-atts;
        class CDATA "- topic/ph xnal-d/preceedingtitle ">-->
<!ATTLIST honorific %global-atts;
        class CDATA "- topic/data xnal-d/honorific ">
<!ATTLIST firstname %global-atts;
        class CDATA "- topic/data xnal-d/firstname ">
<!ATTLIST middlename %global-atts;
        class CDATA "- topic/data xnal-d/middlename ">
<!ATTLIST lastname %global-atts;
        class CDATA "- topic/data xnal-d/lastname ">
<!ATTLIST generationidentifier %global-atts;
        class CDATA "- topic/data xnal-d/generationidentifier ">

<!ATTLIST addressdetails %global-atts;
        class CDATA "- topic/ph xnal-d/addressdetails ">
<!--<!ATTLIST address %global-atts;
        class CDATA "- topic/ph xnal-d/address ">-->
<!ATTLIST locality %global-atts;
        class CDATA "- topic/ph xnal-d/locality ">
<!ATTLIST localityname %global-atts;
        class CDATA "- topic/ph xnal-d/localityname ">
<!ATTLIST administrativearea %global-atts;
        class CDATA "- topic/ph xnal-d/administrativearea ">
<!ATTLIST thoroughfare %global-atts;
        class CDATA "- topic/ph xnal-d/thoroughfare ">
<!ATTLIST postalcode %global-atts;
        class CDATA "- topic/ph xnal-d/postalcode ">
<!ATTLIST country %global-atts;
        class CDATA "- topic/ph xnal-d/country ">

<!ATTLIST personinfo %global-atts;
        class CDATA "- topic/data xnal-d/personinfo ">
<!ATTLIST organizationinfo %global-atts;
        class CDATA "- topic/data xnal-d/organizationinfo ">
<!ATTLIST contactnumbers %global-atts;
        class CDATA "- topic/data xnal-d/contactnumbers ">
<!ATTLIST contactnumber %global-atts;
        class CDATA "- topic/ph xnal-d/contactnumber ">
<!--<!ATTLIST officephone %global-atts;
        class CDATA "- topic/ph xnal-d/officephone ">
<!ATTLIST fax %global-atts;
        class CDATA "- topic/ph xnal-d/fax ">
<!ATTLIST cellular %global-atts;
        class CDATA "- topic/ph xnal-d/cellular ">
<!ATTLIST urlphone %global-atts;
        class CDATA "- topic/ph xnal-d/urlphone ">-->
<!ATTLIST emailaddresses %global-atts;
        class CDATA "- topic/data xnal-d/emailaddresses ">
<!ATTLIST emailaddress %global-atts;
        class CDATA "- topic/data xnal-d/emailaddress ">
<!ATTLIST urls %global-atts;
        class CDATA "- topic/data xnal-d/urls ">
<!ATTLIST url %global-atts;
        class CDATA "- topic/data xnal-d/url ">

<!-- ================== DITA Map Group Domain  =================== -->