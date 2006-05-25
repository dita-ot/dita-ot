<!--
 |  (C) Copyright IBM Corporation 2001, 2004. All Rights Reserved.
 |
 | Release history (vrm):
 |   1.0.0 Initial release for Eclipse Map Collection requirement (R007946)
 *-->


<!ENTITY % topicref-atts-on-tocref 'collection-type    (choice|unordered|sequence|family) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external) #IMPLIED
  locktitle    (yes|no) #IMPLIED
  linking (targetonly|sourceonly|normal|none) #IMPLIED
  toc           (yes|no) #IMPLIED
  search        (yes|no) #IMPLIED
  print         (yes|no) #IMPLIED
  format        CDATA    "ditamap"
  chunk         CDATA    #IMPLIED'
>
<!ENTITY % topicref-atts-simple-extension 'collection-type    (choice|unordered|sequence|family) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external) #IMPLIED
  locktitle    (yes|no) #IMPLIED
  linking (targetonly|sourceonly|normal|none) #IMPLIED
  search        (yes|no) #IMPLIED
  chunk         CDATA    #IMPLIED
  format        CDATA    #IMPLIED
  print         (yes|no)  "no"
  toc           (yes|no)  "no"'
>
<!ENTITY % topicref-atts-xml-extension 'collection-type    (choice|unordered|sequence|family) #IMPLIED
  type CDATA #IMPLIED
  scope (local | peer | external) #IMPLIED
  locktitle    (yes|no) #IMPLIED
  linking (targetonly|sourceonly|normal|none) #IMPLIED
  search        (yes|no) #IMPLIED
  chunk         CDATA    #IMPLIED
  format        CDATA     "xml"
  print         (yes|no)  "no"
  toc           (yes|no)  "no"'
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


<!ENTITY included-domains "">

<!ELEMENT plugin ((%pluginmeta;),(%primarytocref;|%tocref;|%indexExtension;|%contextExtension;|%contentExtension;|%extension;)*)>
<!ATTLIST plugin title       CDATA #IMPLIED
                          id        ID    #REQUIRED
                          %topicref-atts;
                          %select-atts;
                          %arch-atts;
                          domains CDATA "&included-domains;"
>


<!ELEMENT pluginmeta ((%providerName;),(%plugininfo;))>
<!ATTLIST pluginmeta lockmeta (yes|no) #IMPLIED>

<!ELEMENT providerName   (%words.cnt;)*>
<!ATTLIST providerName   href CDATA #IMPLIED
                          keyref CDATA #IMPLIED
                          %select-atts;
>

<!ELEMENT plugininfo       (%pluginname;,%vrmlist;)>
<!ATTLIST plugininfo        %select-atts;
>
                      
<!ELEMENT pluginname       (%words.cnt;)*>

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
<!ATTLIST tocrefmeta lockmeta (yes|no) #IMPLIED>

<!-- @content contains a directory name, so default to No Translation -->
<!ELEMENT extradir        EMPTY >
<!ATTLIST extradir        name CDATA "Extradir attribute"
                          content CDATA #REQUIRED
                          translate-content (yes|no) "no"
                          %select-atts;
>

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
<!ATTLIST contextInfo lockmeta (yes|no) #IMPLIED>

<!ELEMENT extensionName   EMPTY >
<!ATTLIST extensionName   name CDATA "Extension name"
                          content CDATA #REQUIRED
                          translate-content (yes|no) #IMPLIED
                          %select-atts;
>

<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT contextPlugin   EMPTY >
<!ATTLIST contextPlugin   name CDATA "Plugin"
                          content CDATA #REQUIRED
                          translate-content (yes|no) "no"
                          %select-atts;
>

<!-- Add elements for Content Producer extension elements (point="org.eclipse.help.contentProducer") -->

<!ELEMENT contentExtension (%contentProducer;)>
<!ATTLIST contentExtension
  id           ID        #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  copy-to      CDATA     #IMPLIED
  %topicref-atts-simple-extension;
  %select-atts;
>

<!ELEMENT contentProducer ((%extensionName;)?,(%producerClass;),(%parameter;)*)>
<!ATTLIST contentProducer lockmeta (yes|no) #IMPLIED>
                               
<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT producerClass   EMPTY >
<!ATTLIST producerClass   name CDATA "Producer class"
                          content CDATA #REQUIRED
                          translate-content (yes|no) "no"
                          %select-atts;
>

<!-- Parameters to the class specified in producerClass: need name/value pair, comes straight from othermeta -->
<!ELEMENT parameter       EMPTY >
<!ATTLIST parameter       name CDATA    #REQUIRED
                          content CDATA #REQUIRED
                          translate-content (yes|no) #IMPLIED
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
<!ATTLIST extensionMeta lockmeta (yes|no) #IMPLIED>

<!-- @content will contain a java class name, so set default translation to "no" -->
<!ELEMENT extensionPoint  EMPTY >
<!ATTLIST extensionPoint  name CDATA "Extension point"
                          content CDATA #REQUIRED
                          translate-content (yes|no) "no"
                          %select-atts;
>

<!--specialization attributes-->


<!ATTLIST plugin %global-atts; class CDATA "- map/map eclipsemap/plugin ">

<!ATTLIST primarytocref %global-atts; class CDATA "- map/topicref eclipsemap/primarytocref ">
<!ATTLIST tocref %global-atts; class CDATA "- map/topicref eclipsemap/tocref ">
<!ATTLIST indexExtension %global-atts; class CDATA "- map/topicref eclipsemap/indexExtension ">
<!ATTLIST contextExtension %global-atts; class CDATA "- map/topicref eclipsemap/contextExtension ">
<!ATTLIST contentExtension %global-atts; class CDATA "- map/topicref eclipsemap/contentExtension ">
<!ATTLIST extension %global-atts; class CDATA "- map/topicref eclipsemap/extension ">

<!ATTLIST pluginmeta %global-atts; class CDATA "- map/topicmeta eclipsemap/pluginmeta ">
<!ATTLIST tocrefmeta %global-atts; class CDATA "- map/topicmeta eclipsemap/tocrefmeta ">
<!ATTLIST contextInfo %global-atts; class CDATA "- map/topicmeta eclipsemap/contextInfo ">
<!ATTLIST contentProducer %global-atts; class CDATA "- map/topicmeta eclipsemap/contentProducer ">
<!ATTLIST extensionMeta %global-atts; class CDATA "- map/topicmeta eclipsemap/extensionMeta ">

<!ATTLIST providerName %global-atts; class CDATA "- topic/publisher eclipsemap/providerName ">
<!ATTLIST plugininfo %global-atts; class CDATA "- topic/prodinfo eclipsemap/plugininfo ">
<!ATTLIST pluginname %global-atts; class CDATA "- topic/prodname eclipsemap/pluginname ">


<!ATTLIST extradir %global-atts; class CDATA "- topic/othermeta eclipsemap/extradir ">
<!ATTLIST extensionName %global-atts; class CDATA "- topic/othermeta eclipsemap/extensionName ">
<!ATTLIST contextPlugin %global-atts; class CDATA "- topic/othermeta eclipsemap/contextPlugin ">
<!ATTLIST producerClass %global-atts; class CDATA "- topic/othermeta eclipsemap/producerClass ">
<!ATTLIST parameter %global-atts; class CDATA "- topic/othermeta eclipsemap/parameter ">
<!ATTLIST extensionPoint %global-atts; class CDATA "- topic/othermeta eclipsemap/extensionPoint ">


