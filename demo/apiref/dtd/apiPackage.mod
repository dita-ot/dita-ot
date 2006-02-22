<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % apiPackage "apiPackage">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % apiPackage-types-default
    "apiPackage | apiClassifier | apiOperation | apiValue">
<!ENTITY % apiPackage-info-types  "%apiPackage-types-default;">

<!ENTITY included-domains "">


<!-- ============ Element definitions ============ -->

<!ELEMENT apiPackage       ( (%apiName;), (%shortdesc;), (%prolog;)?, (%apiDetail;), (%related-links;)?, (%apiPackage-info-types;)* )>
<!ATTLIST apiPackage      id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>


<!-- ============ Class ancestry ============ -->
<!ATTLIST apiPackage   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiPackage/apiPackage ">
