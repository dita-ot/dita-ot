<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Eclipse Map Collection                       -->
<!--  VERSION:   1.0                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//IBM//ELEMENTS DITA Eclipse Map Collection//EN" 
      Delivered as file "plugin.mod"                              -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributes    -->
<!--             for Eclipse Maps                                     -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             November 2009                                        -->
<!--                                                               -->
<!--            (C) Copyright IBM Corporation 2009.                -->
<!--             All Rights Reserved.                              -->

<!ENTITY % topicref-atts-on-tocref 'collection-type    (choice|unordered|sequence|family|-dita-use-conref-target) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  locktitle    (yes|no|-dita-use-conref-target) #IMPLIED
  linking (targetonly|sourceonly|normal|none|-dita-use-conref-target) #IMPLIED
  toc           (yes|no|-dita-use-conref-target) #IMPLIED
  search        (yes|no|-dita-use-conref-target) #IMPLIED
  print         (yes|no|-dita-use-conref-target) #IMPLIED
  format        CDATA    "ditamap"
  chunk         CDATA    #IMPLIED'
>
<!ENTITY % topicref-atts-simple-extension 'collection-type    (choice|unordered|sequence|family|-dita-use-conref-target) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  locktitle    (yes|no|-dita-use-conref-target) #IMPLIED
  linking (targetonly|sourceonly|normal|none|-dita-use-conref-target) #IMPLIED
  search        (yes|no|-dita-use-conref-target) #IMPLIED
  chunk         CDATA    #IMPLIED
  format        CDATA    #IMPLIED
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"'
>
<!ENTITY % topicref-atts-xml-extension 'collection-type    (choice|unordered|sequence|family|-dita-use-conref-target) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  locktitle    (yes|no|-dita-use-conref-target) #IMPLIED
  linking (targetonly|sourceonly|normal|none|-dita-use-conref-target) #IMPLIED
  search        (yes|no|-dita-use-conref-target) #IMPLIED
  chunk         CDATA    #IMPLIED
  format        CDATA     "xml"
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"'
>

<!--element redefinitions -->
<!ENTITY % plugin      "plugin">
<!ENTITY % pluginmeta  "pluginmeta">
<!ENTITY % primarytocref "primarytocref">
<!ENTITY % tocref      "tocref">
<!ENTITY % tocrefmeta  "tocrefmeta">
<!ENTITY % indexExtension "indexExtension">
<!ENTITY % contextExtension "contextExtension">
<!ENTITY % contextInfo "contextInfo">
<!ENTITY % contextPlugin "contextPlugin">
<!ENTITY % contentExtension "contentExtension">
<!ENTITY % contentProducer "contentProducer">
<!ENTITY % producerClass "producerClass">
<!ENTITY % parameter   "parameter">
<!ENTITY % extension   "extension">
<!ENTITY % extensionMeta "extensionMeta">
<!ENTITY % extensionPoint "extensionPoint">
<!ENTITY % extensionName "extensionName">
<!ENTITY % providerName "providerName">
<!ENTITY % plugininfo  "plugininfo">
<!ENTITY % pluginname  "pluginname">
<!ENTITY % extradir    "extradir">
<!ENTITY % manifestMeta    "manifestMeta">
<!ENTITY % requiresPlugin    "requiresPlugin">
<!ENTITY % versionRange     "versionRange ">
<!ENTITY % versionMin    "versionMin">
<!ENTITY % versionMax    "versionMax">
<!ENTITY % versionLessThanMax    "versionLessThanMax">
<!ENTITY % versionGreaterThanMin    "versionGreaterThanMin">
<!ENTITY % controllingPlugin "controllingPlugin">
<!ENTITY % osgiManifest "osgiManifest">
<!ENTITY % otherBundleHeaders "otherBundleHeaders">
<!ENTITY % qualifier "qualifier">
<!ENTITY % productName "productName">
<!ENTITY % productId "productId">
<!ENTITY % pluginProperties "pluginProperties">
<!ENTITY % dynamicDitaExtension "dynamicDitaExtension">
<!ENTITY % schemaExtension "schemaExtension">
<!ENTITY % map2eclipseOverride "map2eclipseOverride">
<!ENTITY % dita2xhtmlOverride "dita2xhtmlOverride">

<!ENTITY % fragmentInfo "fragmentInfo">
<!ENTITY % fragmentVersionInfo "fragmentVersionInfo">
<!ENTITY % fragmentName "fragmentName">




<!ENTITY included-domains "">

<!ELEMENT plugin ((%pluginmeta;),
                  (%primarytocref;|%tocref;|%indexExtension;|%contextExtension;|
                   %contentExtension;|%extension;)*,
                  (%dynamicDitaExtension;)?,
                  (%osgiManifest;)?  )  >
<!ATTLIST plugin title       CDATA #IMPLIED
                          id        ID    #REQUIRED
                          %topicref-atts;
                          %select-atts;
                          %localization-atts;
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT pluginmeta ((%providerName;),(%copyright;)*,(%plugininfo;), (%fragmentInfo;)?, (%pluginProperties;)*, (%controllingPlugin;)? )>
<!ATTLIST pluginmeta lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>


<!ELEMENT providerName   (%words.cnt;)*>
<!ATTLIST providerName   href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          %select-atts;
>

<!ELEMENT pluginProperties     EMPTY                                        >
<!ATTLIST pluginProperties 
             name       CDATA                            #REQUIRED
             content    CDATA                            #REQUIRED
             translate-content
                        (yes | no | 
                         -dita-use-conref-target)        #IMPLIED
             %univ-atts;                                             >

<!ELEMENT osgiManifest (%manifestMeta;)>
<!ATTLIST osgiManifest
  id           ID        #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-simple-extension;
  %select-atts;
>

<!ELEMENT manifestMeta ((%requiresPlugin;)* , (%otherBundleHeaders;)*)>
<!ATTLIST manifestMeta lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>

<!ELEMENT otherBundleHeaders     EMPTY                                        >
<!ATTLIST otherBundleHeaders 
             name       CDATA                            #REQUIRED
             content    CDATA                            #REQUIRED
             translate-content (yes|no|-dita-use-conref-target) "no"
             %univ-atts;                                             >

<!ELEMENT requiresPlugin       ((%pluginname;),((%versionRange;)))>
<!ATTLIST requiresPlugin        %select-atts;
>

<!ELEMENT versionRange   (  (%versionMin;, (%versionMax; | %versionLessThanMax;)?) |
   (%versionGreaterThanMin;, (%versionMax; | %versionLessThanMax;)   ))
                        >
<!ATTLIST versionRange 
             %univ-atts;                                             >  

<!ELEMENT versionMin           EMPTY                                        >
<!ATTLIST versionMin
             %univ-atts;               
             version    CDATA                            #REQUIRED
             release    CDATA                            #IMPLIED
             modification 
                        CDATA                            #IMPLIED    >
                        

<!ELEMENT versionMax           EMPTY                                        >
<!ATTLIST versionMax
             %univ-atts;               
             version    CDATA                            #REQUIRED
             release    CDATA                            #IMPLIED
             modification 
                        CDATA                            #IMPLIED    >

<!ELEMENT versionLessThanMax           EMPTY                                        >
<!ATTLIST versionLessThanMax
             %univ-atts;               
             version    CDATA                            #REQUIRED
             release    CDATA                            #IMPLIED
             modification 
                        CDATA                            #IMPLIED    >

<!ELEMENT versionGreaterThanMin           EMPTY                                        >
<!ATTLIST versionGreaterThanMin
             %univ-atts;               
             version    CDATA                            #REQUIRED
             release    CDATA                            #IMPLIED
             modification 
                        CDATA                            #IMPLIED    >


<!ELEMENT fragmentInfo       ((%fragmentName;), (%fragmentVersionInfo;), (%qualifier;)?)>
<!ATTLIST fragmentInfo        %select-atts;
>

<!ELEMENT fragmentVersionInfo   ( (%vrm;), ( (%versionMin;, (%versionMax; | %versionLessThanMax;)?) |
   (%versionGreaterThanMin;, (%versionMax; | %versionLessThanMax;)) ))
                        >
<!ATTLIST fragmentVersionInfo 
             %univ-atts;                                             >   

<!ELEMENT fragmentName       (#PCDATA)                               >
<!ATTLIST fragmentName
             %univ-atts;                                             >            
              

<!ELEMENT plugininfo       ((%pluginname;),(%vrmlist;), (%qualifier;)?)>
<!ATTLIST plugininfo        %select-atts;
>
                      
<!ELEMENT pluginname       (%words.cnt;)*>
<!ATTLIST pluginname
             %univ-atts;                                             >  

<!ELEMENT qualifier       (%words.cnt;)*                               >
<!ATTLIST qualifier
             %univ-atts;                                             >  

<!ELEMENT primarytocref  (%tocrefmeta;)?>
<!ATTLIST primarytocref
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-on-tocref;
  %select-atts;
>

<!ELEMENT tocref (%tocrefmeta;)?>
<!ATTLIST tocref
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-on-tocref;
  %select-atts;
>

<!ELEMENT tocrefmeta ((%linktext;)?,(%searchtitle;)?,(%shortdesc;)?,(%author;)*,(%source;)?,(%publisher;)?,(%copyright;)*,(%critdates;)?,(%permissions;)?,(%audience;)*,(%category;)*,(%keywords;)*,(%prodinfo;)*,(%othermeta;)*,(%extradir;)?,(%resourceid;)*)>
<!ATTLIST tocrefmeta lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>

<!-- @content contains a directory name, so default to No Translation -->
<!ELEMENT extradir        EMPTY >
<!ATTLIST extradir        name CDATA "Extradir attribute"
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) "no"
                          %select-atts;
>

<!ELEMENT controllingPlugin ((%productName;), (%productId;)? )>


<!ELEMENT productName       (#PCDATA )                             >
<!ATTLIST productName
             %univ-atts;                                             >
             
<!ELEMENT productId       (#PCDATA)                               >
<!ATTLIST productId
             %univ-atts;                                             >             


<!ELEMENT indexExtension ((%tocrefmeta;)?)>
<!ATTLIST indexExtension
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #REQUIRED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-xml-extension;
  %select-atts;
>

<!-- Add elements for context extension elements (point="org.eclipse.help.contexts") -->

<!ELEMENT contextExtension ((%contextInfo;)?)>
<!ATTLIST contextExtension
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #REQUIRED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-xml-extension;
  %select-atts;
>

<!ELEMENT contextInfo ((%extensionName;)?,(%contextPlugin;)?)>
<!ATTLIST contextInfo lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>

<!ELEMENT extensionName   EMPTY >
<!ATTLIST extensionName   name CDATA "Extension name"
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) #IMPLIED
                          %select-atts;
>

<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT contextPlugin   EMPTY >
<!ATTLIST contextPlugin   name CDATA "Plugin"
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) "no"
                          %select-atts;
>

<!-- Add elements for Content Producer extension elements (point="org.eclipse.help.contentProducer") -->

<!ELEMENT contentExtension (%contentProducer;)?>
<!ATTLIST contentExtension
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-simple-extension;
  %select-atts;
>

<!ELEMENT contentProducer ((%extensionName;)?,(%producerClass;),(%parameter;)*)>
<!ATTLIST contentProducer lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>
                               
<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT producerClass   EMPTY >
<!ATTLIST producerClass   name CDATA "Producer class"
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) "no"
                          %select-atts;
>

<!-- Parameters to the class specified in producerClass: need name/value pair, comes straight from othermeta -->
<!ELEMENT parameter       EMPTY >
<!ATTLIST parameter       name CDATA    #REQUIRED
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) #IMPLIED
                          %select-atts;
>

<!ELEMENT dynamicDitaExtension ((%schemaExtension;)?,
                                (%dita2xhtmlOverride;)?,
                                (%map2eclipseOverride;)?)>
<!ATTLIST dynamicDitaExtension
  id           ID        #IMPLIED
  conref       CDATA     #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  format        CDATA     "xml"
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"  
  %select-atts;
>

<!ELEMENT schemaExtension EMPTY>
<!ATTLIST schemaExtension
  id           ID        #IMPLIED
  href         CDATA     #REQUIRED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  type         CDATA     #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  format        CDATA     "xml"
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"
  %select-atts;
>

<!ELEMENT dita2xhtmlOverride EMPTY>
<!ATTLIST dita2xhtmlOverride
  id           ID        #IMPLIED
  href         CDATA     #REQUIRED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  type         CDATA     #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  format        CDATA     "xsl"
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"
  %select-atts;
>

<!ELEMENT map2eclipseOverride EMPTY>
<!ATTLIST map2eclipseOverride
  id           ID        #IMPLIED
  href         CDATA     #REQUIRED
  keyref       CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  type         CDATA     #IMPLIED
  scope (local | peer | external | -dita-use-conref-target) #IMPLIED
  format        CDATA     "xsl"
  print         (yes|no|-dita-use-conref-target)  "no"
  toc           (yes|no|-dita-use-conref-target)  "no"
  %select-atts;
>


<!-- Elements to allow for unexpected new extensions -->
<!ELEMENT extension (%extensionMeta;)>
<!ATTLIST extension
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-simple-extension;
  %select-atts;
>

<!ELEMENT extensionMeta ((%extensionPoint;),(%extensionName;)?,(%othermeta;)*)>
<!ATTLIST extensionMeta lockmeta (yes|no|-dita-use-conref-target) #IMPLIED>

<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT extensionPoint  EMPTY >
<!ATTLIST extensionPoint  name CDATA "Extension point"
                          content CDATA #REQUIRED
                          translate-content (yes|no|-dita-use-conref-target) "no"
                          %select-atts;
>

<!--specialization attributes-->


<!ATTLIST plugin %global-atts; class CDATA "- map/map eclipsemap/plugin ">

<!ATTLIST primarytocref %global-atts; class CDATA "- map/topicref eclipsemap/primarytocref ">
<!ATTLIST tocref %global-atts; class CDATA "- map/topicref eclipsemap/tocref ">
<!ATTLIST controllingPlugin %global-atts; class CDATA "- map/data eclipsemap/controllingPlugin ">
<!ATTLIST indexExtension %global-atts; class CDATA "- map/topicref eclipsemap/indexExtension ">
<!ATTLIST contextExtension %global-atts; class CDATA "- map/topicref eclipsemap/contextExtension ">
<!ATTLIST contentExtension %global-atts; class CDATA "- map/topicref eclipsemap/contentExtension ">
<!ATTLIST extension %global-atts; class CDATA "- map/topicref eclipsemap/extension ">
<!ATTLIST osgiManifest %global-atts; class CDATA "- map/topicref eclipsemap/osgiManifest ">
<!ATTLIST dynamicDitaExtension %global-atts; class CDATA "- map/topicref eclipsemap/dynamicDitaExtension ">
<!ATTLIST schemaExtension %global-atts; class CDATA "- map/topicref eclipsemap/schemaExtension ">
<!ATTLIST map2eclipseOverride %global-atts; class CDATA "- map/topicref eclipsemap/map2eclipseOverride ">
<!ATTLIST dita2xhtmlOverride %global-atts; class CDATA "- map/topicref eclipsemap/dita2xhtmlOverride ">

<!ATTLIST pluginmeta %global-atts; class CDATA "- map/topicmeta eclipsemap/pluginmeta ">
<!ATTLIST manifestMeta %global-atts; class CDATA "- map/topicmeta eclipsemap/manifestMeta ">
<!ATTLIST tocrefmeta %global-atts; class CDATA "- map/topicmeta eclipsemap/tocrefmeta ">
<!ATTLIST contextInfo %global-atts; class CDATA "- map/topicmeta eclipsemap/contextInfo ">
<!ATTLIST contentProducer %global-atts; class CDATA "- map/topicmeta eclipsemap/contentProducer ">
<!ATTLIST extensionMeta %global-atts; class CDATA "- map/topicmeta eclipsemap/extensionMeta ">

<!ATTLIST providerName %global-atts; class CDATA "- topic/publisher eclipsemap/providerName ">
<!ATTLIST plugininfo %global-atts; class CDATA "- topic/prodinfo eclipsemap/plugininfo ">
<!ATTLIST requiresPlugin %global-atts; class CDATA "- topic/prodinfo eclipsemap/requiresPlugin ">
<!ATTLIST pluginname %global-atts; class CDATA "- topic/prodname eclipsemap/pluginname ">
<!ATTLIST productName %global-atts; class CDATA "- topic/keyword eclipsemap/productName ">
<!ATTLIST productId %global-atts; class CDATA "- topic/keyword eclipsemap/productId ">

<!ATTLIST fragmentName %global-atts; class CDATA "- topic/prodname eclipsemap/fragmentName ">
<!ATTLIST fragmentInfo %global-atts; class CDATA "- topic/prodinfo eclipsemap/fragmentInfo "> 
<!ATTLIST fragmentVersionInfo %global-atts; class CDATA "- topic/vrmlist eclipsemap/fragmentVersionInfo ">

<!ATTLIST qualifier %global-atts; class CDATA "- topic/prognum eclipsemap/qualifier ">
<!ATTLIST versionRange %global-atts; class CDATA "- topic/vrmlist eclipsemap/versionRange ">


<!ATTLIST extradir %global-atts; class CDATA "- topic/othermeta eclipsemap/extradir ">
<!ATTLIST extensionName %global-atts; class CDATA "- topic/othermeta eclipsemap/extensionName ">
<!ATTLIST contextPlugin %global-atts; class CDATA "- topic/othermeta eclipsemap/contextPlugin ">
<!ATTLIST producerClass %global-atts; class CDATA "- topic/othermeta eclipsemap/producerClass ">
<!ATTLIST parameter %global-atts; class CDATA "- topic/othermeta eclipsemap/parameter ">
<!ATTLIST extensionPoint %global-atts; class CDATA "- topic/othermeta eclipsemap/extensionPoint ">

<!ATTLIST versionMin %global-atts; class CDATA "- topic/vrm eclipsemap/versionMin ">
<!ATTLIST versionMax %global-atts; class CDATA "- topic/vrm eclipsemap/versionMax ">
<!ATTLIST versionLessThanMax %global-atts; class CDATA "- topic/vrm eclipsemap/versionLessThanMax ">
<!ATTLIST versionGreaterThanMin %global-atts; class CDATA "- topic/vrm eclipsemap/versionGreaterThanMin ">
<!ATTLIST pluginProperties %global-atts; class CDATA "- topic/othermeta eclipsemap/pluginProperties ">
<!ATTLIST otherBundleHeaders %global-atts; class CDATA "- topic/othermeta eclipsemap/otherBundleHeaders ">

