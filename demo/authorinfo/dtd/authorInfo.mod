<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- ============ Specialization of declared elements ============ -->

<!ENTITY % authorInfo                "authorInfo">
<!ENTITY % authorName                "authorName">
<!ENTITY % prefix                    "prefix">
<!ENTITY % givenName                 "givenName">
<!ENTITY % middleName                "middleName">
<!ENTITY % familyName                "familyName">
<!ENTITY % suffix                    "suffix">
<!ENTITY % authorBody                "authorBody">
<!ENTITY % authorProperties          "authorProperties">
<!ENTITY % jobTitle                  "jobTitle">
<!ENTITY % companyName               "companyName">
<!ENTITY % bio                       "bio">
<!ENTITY % contactLinks              "contactLinks">
<!ENTITY % email                     "email">
<!ENTITY % emailcc                   "emailcc">
<!ENTITY % authorInfo-info-types     "%info-types;">

<!ELEMENT authorInfo            ((%authorName;), (%authorBody;), (%contactLinks;)?, (%authorInfo-info-types;) )>
<!ATTLIST authorInfo              id ID #REQUIRED
                                  conref CDATA #IMPLIED
                                  %select-atts;
                                  outputclass CDATA #IMPLIED
                                  %localization-atts;
                                  %arch-atts;
                                  domains CDATA "&included-domains;"
>

<!ELEMENT authorName            ((%prefix;)?, (%givenName;)?, (%middleName;)?, (%familyName;)?, (%suffix;)? )>
<!ATTLIST authorName              %id-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT prefix                (#PCDATA)*>
<!ATTLIST prefix                  keyref CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT givenName             (#PCDATA)*>
<!ATTLIST givenName               keyref CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT middleName            (#PCDATA)*>
<!ATTLIST middleName              keyref CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT familyName            (#PCDATA)*>
<!ATTLIST familyName              keyref CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT suffix                (#PCDATA)*>
<!ATTLIST suffix                  keyref CDATA #IMPLIED
                                  %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT authorBody            ((%authorProperties;), (%bio;) )>
<!ATTLIST authorBody              %id-atts;
                                  %localization-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT authorProperties      ((%jobTitle;), (%companyName;)?, (%image;)? )>
<!ATTLIST authorProperties        %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT jobTitle              (#PCDATA)*>
<!ATTLIST jobTitle                %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT companyName           (#PCDATA)*>
<!ATTLIST companyName             %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT bio                   (%section.cnt;)* >
<!ATTLIST bio                     %univ-atts;
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT contactLinks          ((%email;)+, (%emailcc;)*, (%link;)*) >
<!ATTLIST contactLinks            %rel-atts;
                                  %select-atts;
                                  format        CDATA   #IMPLIED
                                  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT email                 ((%linktext;)?, (%desc;)?)>
<!ATTLIST email                   href CDATA #IMPLIED
                                  keyref CDATA #IMPLIED
                                  %rel-atts;
                                  %select-atts;
                                  format        CDATA   "email"
                                  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
                                  outputclass CDATA #IMPLIED
>

<!ELEMENT emailcc               ((%linktext;)?, (%desc;)?)>
<!ATTLIST emailcc                 href CDATA #IMPLIED
                                  keyref CDATA #IMPLIED
                                  %rel-atts;
                                  %select-atts;
                                  format        CDATA   "email"
                                  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
                                  outputclass CDATA #IMPLIED
>

<!--specialization attributes-->

<!ATTLIST authorInfo            %global-atts; class  CDATA "- topic/topic authorInfo/authorInfo ">
<!ATTLIST authorName            %global-atts; class  CDATA "- topic/title authorInfo/authorName ">
<!ATTLIST prefix                %global-atts; class  CDATA "- topic/ph authorInfo/prefix ">
<!ATTLIST givenName             %global-atts; class  CDATA "- topic/ph authorInfo/givenName ">
<!ATTLIST middleName            %global-atts; class  CDATA "- topic/ph authorInfo/middleName ">
<!ATTLIST familyName            %global-atts; class  CDATA "- topic/ph authorInfo/familyName ">
<!ATTLIST suffix                %global-atts; class  CDATA "- topic/ph authorInfo/suffix ">
<!ATTLIST authorBody            %global-atts; class  CDATA "- topic/body authorInfo/authorBody ">
<!ATTLIST authorProperties      %global-atts; class  CDATA "- topic/section authorInfo/authorProperties ">
<!ATTLIST jobTitle              %global-atts; class  CDATA "- topic/p authorInfo/jobTitle ">
<!ATTLIST companyName           %global-atts; class  CDATA "- topic/p authorInfo/companyName ">
<!ATTLIST bio                   %global-atts; class  CDATA "- topic/section authorInfo/bio ">
<!ATTLIST contactLinks          %global-atts; class  CDATA "- topic/related-links authorInfo/contactLinks ">
<!ATTLIST email                 %global-atts; class  CDATA "- topic/link authorInfo/email ">
<!ATTLIST emailcc               %global-atts; class  CDATA "- topic/link authorInfo/emailcc ">