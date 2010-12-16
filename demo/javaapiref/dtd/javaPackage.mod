<!--
 | (C) Copyright IBM Corporation 2005, 2009. All Rights Reserved.
 *-->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % javaPackage           "javaPackage">
<!ENTITY % javaPackageDetail     "javaPackageDetail">


<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % javaPackage-types-default  "no-topic-nesting">
<!ENTITY % javaPackage-info-types     "%javaPackage-types-default;">

<!ENTITY included-domains "">


<!-- ============ Topic specializations ============ -->
<!ELEMENT javaPackage     ((%apiName;), (%shortdesc; | %abstract;), (%prolog;)?, (%javaPackageDetail;), (%related-links;)?, (%javaPackage-info-types;)*)>
<!ATTLIST javaPackage     id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          %localization-atts;
                          %select-atts;
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT javaPackageDetail      ((%apiDesc;)?, (%example;|%section;|%apiImpl;)*)>
<!ATTLIST javaPackageDetail  %id-atts;
                          %localization-atts;
                          outputclass CDATA #IMPLIED>


<!-- ============ Class attributes for type ancestry ============ -->
<!ATTLIST javaPackage   %global-atts;
    class  CDATA "- topic/topic reference/reference apiRef/apiRef apiPackage/apiPackage javaPackage/javaPackage ">
<!ATTLIST javaPackageDetail   %global-atts;
    class  CDATA "- topic/body reference/refbody apiRef/apiDetail apiPackage/apiDetail javaPackage/javaPackageDetail ">
