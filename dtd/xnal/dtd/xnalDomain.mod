<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    XNAL Domain                                       -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
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
<!--             for the XNAL Domain                               -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             June 2006                                         -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2006, 2009.              -->
<!--             All Rights Reserved.                              -->
<!--  UPDATES:                                                     -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.01.28 RDA: Removed enumerations for attributes:       -->
<!--                    authorinformation/@type                    -->
<!--    2008.02.12 RDA: Add keyword to data specializations        -->
<!--    2008.02.12 RDA: Add @format, @scope to authorinformation   -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->


<!ENTITY % authorinformation "authorinformation"                     >
<!ENTITY % addressdetails  "addressdetails"                          >
<!ENTITY % administrativearea "administrativearea"                   >
<!ENTITY % contactnumber   "contactnumber"                           >
<!ENTITY % contactnumbers  "contactnumbers"                          >
<!ENTITY % country         "country"                                 >
<!ENTITY % emailaddress    "emailaddress"                            >
<!ENTITY % emailaddresses  "emailaddresses"                          >
<!ENTITY % firstname       "firstname"                               >
<!ENTITY % generationidentifier "generationidentifier"               >
<!ENTITY % honorific       "honorific"                               >
<!ENTITY % lastname        "lastname"                                >
<!ENTITY % locality        "locality"                                >
<!ENTITY % localityname    "localityname"                            >
<!ENTITY % middlename      "middlename"                              >
<!ENTITY % namedetails     "namedetails"                             >
<!ENTITY % organizationinfo "organizationinfo"                       >
<!ENTITY % organizationname "organizationname"                       >
<!ENTITY % organizationnamedetails "organizationnamedetails"         >
<!ENTITY % otherinfo       "otherinfo"                               >
<!ENTITY % personinfo      "personinfo"                              >
<!ENTITY % personname      "personname"                              >
<!ENTITY % postalcode      "postalcode"                              >
<!ENTITY % thoroughfare    "thoroughfare"                            >
<!ENTITY % url             "url"                                     >
<!ENTITY % urls            "urls"                                    >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->
 
<!--                    LONG NAME: Author Information              -->
<!ENTITY % authorinformation.content
                       "(%organizationinfo; | 
                         %personinfo;)*
">
<!-- 20080128: Removed enumeration for @type for DITA 1.2. Previous values:
               creator, contributor, -dita-use-conref-target           -->
<!ENTITY % authorinformation.attributes
             "%univ-atts;
              href 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              scope 
                       (external | 
                        local | 
                        peer | 
                        -dita-use-conref-target) 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT authorinformation    %authorinformation.content;>
<!ATTLIST authorinformation    %authorinformation.attributes;>

<!--                    LONG NAME: Name Details                    -->
<!ENTITY % namedetails.content
                       "(%organizationnamedetails; | 
                         %personname; )*
">
<!ENTITY % namedetails.attributes
             "%data-element-atts;"
>
<!ELEMENT namedetails    %namedetails.content;>
<!ATTLIST namedetails    %namedetails.attributes;>


<!--                    LONG NAME: Organization Details            -->
<!ENTITY % organizationnamedetails.content
                       "((%organizationname;)?, 
                         (%otherinfo;)*)"
>
<!ENTITY % organizationnamedetails.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT organizationnamedetails    %organizationnamedetails.content;>
<!ATTLIST organizationnamedetails    %organizationnamedetails.attributes;>


<!--                    LONG NAME: Organization Name               -->
<!ENTITY % organizationname.content
                       "(%ph.cnt;)*"
>
<!ENTITY % organizationname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT organizationname    %organizationname.content;>
<!ATTLIST organizationname    %organizationname.attributes;>


<!--                    LONG NAME: Person Name                     -->
<!ENTITY % personname.content
                       "((%honorific;)?, 
                         (%firstname;)*,
                         (%middlename;)*,
                         (%lastname;)*,
                         (%generationidentifier;)?, 
                         (%otherinfo;)*)"
>
<!ENTITY % personname.attributes
             "%data-element-atts;"
>
<!ELEMENT personname    %personname.content;>
<!ATTLIST personname    %personname.attributes;>


<!--                    LONG NAME: Honorific                       -->
<!ENTITY % honorific.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % honorific.attributes
             "%data-element-atts;"
>
<!ELEMENT honorific    %honorific.content;>
<!ATTLIST honorific    %honorific.attributes;>


<!--                    LONG NAME: First Name                      -->
<!ENTITY % firstname.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % firstname.attributes
             "%data-element-atts;"
>
<!ELEMENT firstname    %firstname.content;>
<!ATTLIST firstname    %firstname.attributes;>

<!--                    LONG NAME: Middle Name                     -->
<!ENTITY % middlename.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % middlename.attributes
             "%data-element-atts;"
>
<!ELEMENT middlename    %middlename.content;>
<!ATTLIST middlename    %middlename.attributes;>

<!--                    LONG NAME: Last Name                       -->
<!ENTITY % lastname.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % lastname.attributes
             "%data-element-atts;"
>
<!ELEMENT lastname    %lastname.content;>
<!ATTLIST lastname    %lastname.attributes;>


<!--                    LONG NAME: Generation Identifier           -->
<!ENTITY % generationidentifier.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % generationidentifier.attributes
             "%data-element-atts;"
>
<!ELEMENT generationidentifier    %generationidentifier.content;>
<!ATTLIST generationidentifier    %generationidentifier.attributes;>


<!--                    LONG NAME: Other Information               -->
<!ENTITY % otherinfo.content
                       "(%words.cnt;)*
">
<!ENTITY % otherinfo.attributes
             "%data-element-atts;"
>
<!ELEMENT otherinfo    %otherinfo.content;>
<!ATTLIST otherinfo    %otherinfo.attributes;>


<!--                    LONG NAME: Address Details                 -->
<!ENTITY % addressdetails.content
                       "(%words.cnt; |
                         %administrativearea; |
                         %country; |
                         %locality; | 
                         %thoroughfare;)*"
>
<!ENTITY % addressdetails.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT addressdetails    %addressdetails.content;>
<!ATTLIST addressdetails    %addressdetails.attributes;>


<!--                    LONG NAME: Locality                        -->
<!ENTITY % locality.content
                       "(%words.cnt; |
                         %localityname; |
                         %postalcode;)*"
>
<!ENTITY % locality.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT locality    %locality.content;>
<!ATTLIST locality    %locality.attributes;>


<!--                    LONG NAME: Locality Name                   -->
<!ENTITY % localityname.content
                       "(%words.cnt;)*"
>
<!ENTITY % localityname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT localityname    %localityname.content;>
<!ATTLIST localityname    %localityname.attributes;>


<!--                    LONG NAME: Administrative Area             -->
<!ENTITY % administrativearea.content
                       "(%words.cnt;)*"
>
<!ENTITY % administrativearea.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT administrativearea    %administrativearea.content;>
<!ATTLIST administrativearea    %administrativearea.attributes;>


<!--                    LONG NAME: Thoroughfare                    -->
<!ENTITY % thoroughfare.content
                       "(%words.cnt;)*"
>
<!ENTITY % thoroughfare.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT thoroughfare    %thoroughfare.content;>
<!ATTLIST thoroughfare    %thoroughfare.attributes;>


<!--                    LONG NAME: Postal Code                     -->
<!ENTITY % postalcode.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % postalcode.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT postalcode    %postalcode.content;>
<!ATTLIST postalcode    %postalcode.attributes;>


<!--                    LONG NAME: Country                         -->
<!ENTITY % country.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % country.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT country    %country.content;>
<!ATTLIST country    %country.attributes;>


<!--                    LONG NAME: Person Information              -->
<!ENTITY % personinfo.content
                       "((%namedetails;)?, 
                         (%addressdetails;)?,
                         (%contactnumbers;)?, 
                         (%emailaddresses;)?)"
>
<!ENTITY % personinfo.attributes
             "%data-element-atts;"
>
<!ELEMENT personinfo    %personinfo.content;>
<!ATTLIST personinfo    %personinfo.attributes;>


<!--                    LONG NAME: Organization Information        -->
<!ENTITY % organizationinfo.content
                       "((%namedetails;)?, 
                         (%addressdetails;)?, 
                         (%contactnumbers;)?, 
                         (%emailaddresses;)?,
                         (%urls;)?)"
>
<!ENTITY % organizationinfo.attributes
             "%data-element-atts;"
>
<!ELEMENT organizationinfo    %organizationinfo.content;>
<!ATTLIST organizationinfo    %organizationinfo.attributes;>


<!--                    LONG NAME: Contact Numbers                 -->
<!ENTITY % contactnumbers.content
                       "(%contactnumber;)*"
>
<!ENTITY % contactnumbers.attributes
             "%data-element-atts;"
>
<!ELEMENT contactnumbers    %contactnumbers.content;>
<!ATTLIST contactnumbers    %contactnumbers.attributes;>

 
<!--                    LONG NAME: Contact Number                  -->
<!--                    Note: set the type of number using @type   -->
<!ENTITY % contactnumber.content
                       "(#PCDATA |
                         %keyword;)*"
>
<!ENTITY % contactnumber.attributes
             "%data-element-atts;"
>
<!ELEMENT contactnumber    %contactnumber.content;>
<!ATTLIST contactnumber    %contactnumber.attributes;>

 
<!--                    LONG NAME: Email Addresses                 -->
<!ENTITY % emailaddresses.content
                       "(%emailaddress;)*"
>
<!ENTITY % emailaddresses.attributes
             "%data-element-atts;"
>
<!ELEMENT emailaddresses    %emailaddresses.content;>
<!ATTLIST emailaddresses    %emailaddresses.attributes;>


<!--                    LONG NAME: Email Address                   -->
<!ENTITY % emailaddress.content
                       "(%words.cnt;)*"
>
<!ENTITY % emailaddress.attributes
             "%data-element-atts;"
>
<!ELEMENT emailaddress    %emailaddress.content;>
<!ATTLIST emailaddress    %emailaddress.attributes;>


<!--                    LONG NAME: URLs                            -->
<!ENTITY % urls.content
                       "(%url;)*"
>
<!ENTITY % urls.attributes
             "%data-element-atts;"
>
<!ELEMENT urls    %urls.content;>
<!ATTLIST urls    %urls.attributes;>


<!--                    LONG NAME: URL                             -->
<!ENTITY % url.content
                       "(%words.cnt;)*"
>
<!ENTITY % url.attributes
             "%data-element-atts;"
>
<!ELEMENT url    %url.content;>
<!ATTLIST url    %url.attributes;>


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->

<!ATTLIST addressdetails %global-atts; class CDATA "+ topic/ph xnal-d/addressdetails ">
<!ATTLIST administrativearea %global-atts; class CDATA "+ topic/ph xnal-d/administrativearea ">
<!ATTLIST authorinformation %global-atts; class CDATA "+ topic/author xnal-d/authorinformation ">
<!ATTLIST contactnumber %global-atts; class CDATA "+ topic/data xnal-d/contactnumber ">
<!ATTLIST contactnumbers %global-atts; class CDATA "+ topic/data xnal-d/contactnumbers ">
<!ATTLIST country     %global-atts; class CDATA "+ topic/ph xnal-d/country ">
<!ATTLIST emailaddress %global-atts; class CDATA "+ topic/data xnal-d/emailaddress ">
<!ATTLIST emailaddresses %global-atts; class CDATA "+ topic/data xnal-d/emailaddresses ">
<!ATTLIST firstname   %global-atts; class CDATA "+ topic/data xnal-d/firstname ">
<!ATTLIST generationidentifier %global-atts; class CDATA "+ topic/data xnal-d/generationidentifier ">
<!ATTLIST honorific   %global-atts; class CDATA "+ topic/data xnal-d/honorific ">
<!ATTLIST lastname    %global-atts; class CDATA "+ topic/data xnal-d/lastname ">
<!ATTLIST locality    %global-atts; class CDATA "+ topic/ph xnal-d/locality ">
<!ATTLIST localityname %global-atts; class CDATA "+ topic/ph xnal-d/localityname ">
<!ATTLIST middlename  %global-atts; class CDATA "+ topic/data xnal-d/middlename ">
<!ATTLIST namedetails %global-atts; class CDATA "+ topic/data xnal-d/namedetails ">
<!ATTLIST organizationinfo %global-atts; class CDATA "+ topic/data xnal-d/organizationinfo ">
<!ATTLIST organizationname %global-atts;  class CDATA "+ topic/ph xnal-d/organizationname ">
<!ATTLIST organizationnamedetails %global-atts; class CDATA "+ topic/ph xnal-d/organizationnamedetails ">
<!ATTLIST otherinfo   %global-atts; class CDATA "+ topic/data xnal-d/otherinfo ">
<!ATTLIST personinfo  %global-atts; class CDATA "+ topic/data xnal-d/personinfo ">
<!ATTLIST personname  %global-atts; class CDATA "+ topic/data xnal-d/personname ">
<!ATTLIST postalcode  %global-atts; class CDATA "+ topic/ph xnal-d/postalcode ">
<!ATTLIST thoroughfare %global-atts; class CDATA "+ topic/ph xnal-d/thoroughfare ">
<!ATTLIST url         %global-atts; class CDATA "+ topic/data xnal-d/url ">
<!ATTLIST urls        %global-atts; class CDATA "+ topic/data xnal-d/urls ">

<!-- ================== End DITA XNAL Domain  =================== -->