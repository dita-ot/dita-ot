<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Metadata                                     -->
<!--  VERSION:   1.O                                               -->
<!--  DATE:      February 2005                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ENTITIES DITA Metadata//EN"
      Delivered as file "meta_xml.dtd"                             -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA XML Metadata              -->
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


<!ENTITY % audience     "audience"                                   >
<!ENTITY % author       "author"                                     >
<!ENTITY % brand        "brand"                                      >
<!ENTITY % category     "category"                                   >
<!ENTITY % component    "component"                                  >
<!ENTITY % copyrholder  "copyrholder"                                >
<!ENTITY % copyright    "copyright"                                  >
<!ENTITY % copyryear    "copyryear"                                  >
<!ENTITY % created      "created"                                    >
<!ENTITY % critdates    "critdates"                                  >
<!ENTITY % featnum      "featnum"                                    >
<!ENTITY % indexterm    "indexterm"                                  >
<!ENTITY % keywords     "keywords"                                   >
<!ENTITY % othermeta    "othermeta"                                  >
<!ENTITY % permissions  "permissions"                                >
<!ENTITY % platform     "platform"                                   >
<!ENTITY % prodinfo     "prodinfo"                                   >
<!ENTITY % prodname     "prodname"                                   >
<!ENTITY % prognum      "prognum"                                    >
<!ENTITY % publisher    "publisher"                                  >
<!ENTITY % resourceid   "resourceid"                                 >
<!ENTITY % revised      "revised"                                    >
<!ENTITY % series       "series"                                     >
<!ENTITY % source       "source"                                     >
<!ENTITY % vrm          "vrm"                                        >
<!ENTITY % vrmlist      "vrmlist"                                    >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Author                          -->
<!ELEMENT author        (%words.cnt;)*                               >
<!ATTLIST author 
             href       CDATA                             #IMPLIED
             keyref     CDATA                             #IMPLIED
             type       (creator | contributor)           #IMPLIED   >


<!--                     LONG NAME: Source                         -->
<!ELEMENT source       (%words.cnt;)*                                >
<!ATTLIST source 
             href       CDATA                             #IMPLIED
             keyref     CDATA                             #IMPLIED   >


<!--                    LONG NAME: Publisher                       -->
<!ELEMENT publisher     (%words.cnt;)*                               >
<!ATTLIST publisher
             href       CDATA                             #IMPLIED
             keyref     CDATA                             #IMPLIED
             %select-atts;                                           >


<!--                    LONG NAME: Copyright                       -->
<!ELEMENT copyright     ((%copyryear;)+, %copyrholder;)              >
<!ATTLIST copyright 
             type      (primary | secondary)              #IMPLIED   >


<!--                    LONG NAME: Copyright Year                  -->
<!ELEMENT copyryear     EMPTY                                        >
<!ATTLIST copyryear
             year       %date-format;                    #REQUIRED
             %select-atts;                                           >


<!--                    LONG NAME: Copyright Holder                -->
<!ELEMENT copyrholder   (%words.cnt;)*                               >


<!--                    LONG NAME: Critical Dates                  -->
<!ELEMENT critdates     (%created;, (%revised;)*)                    >


<!--                    LONG NAME: Created Date                    -->
<!ELEMENT created       EMPTY                                        >
<!ATTLIST created 
             date       %date-format;                    #REQUIRED
             golive     %date-format;                     #IMPLIED
             expiry     %date-format;                     #IMPLIED   >


<!--                    LONG NAME: Revised Date                    -->
<!ELEMENT revised       EMPTY                                        >
<!ATTLIST revised  
             modified   %date-format;                    #REQUIRED
             golive     %date-format;                     #IMPLIED
             expiry     %date-format;                     #IMPLIED
             %select-atts;                                           >


<!--                     LONG NAME: Permissions                     -->
<!ELEMENT permissions  EMPTY                                        >
<!ATTLIST permissions
             view       (internal | classified | all | 
                         entitled)                       #REQUIRED   >


<!--                    LONG NAME: Category                        -->
<!ELEMENT category      (%words.cnt;)*                               >
<!ATTLIST category     
             %select-atts;                                           >


<!--                    LONG NAME: Audience                        -->
<!ELEMENT audience      EMPTY                                        >
<!ATTLIST audience
             type       (user | purchaser | administrator |
                        programmer | executive | services |
                        other)                            #IMPLIED
             othertype  CDATA                             #IMPLIED
             job        (installing | customizing | 
                         administering | programming |
                         using| maintaining | troubleshooting |
                         evaluating | planning | migrating |
                         other)                           #IMPLIED
             otherjob    CDATA                            #IMPLIED
             experiencelevel
                         (novice | general | expert)      #IMPLIED
             name        NMTOKEN                          #IMPLIED
             %select-atts;                                           >


<!--                    LONG NAME: Keywords                        -->
<!ELEMENT keywords      (%indexterm; | %keyword;)*                   >
<!ATTLIST keywords
             %id-atts;
             %select-atts;                                           >


<!--                    LONG NAME: Product Information             -->
<!ELEMENT prodinfo      ((%prodname;), (%vrmlist;),
                         (%brand; | %series; | %platform; | 
                          %prognum; | %featnum; | %component;)* )    >
<!ATTLIST prodinfo
             %select-atts;                                           >                                     


<!--                    LONG NAME: Product Name                    -->
<!ELEMENT prodname      (%words.cnt;)*                               > 


<!--                    LONG NAME: Version Release and Modification
                                   List                            -->
<!ELEMENT vrmlist       (%vrm;)+                                     >


<!--                    LONG NAME: Version Release and Modification-->
<!ELEMENT vrm           EMPTY                                        >
<!ATTLIST vrm               
             version    CDATA                              #REQUIRED
             release    CDATA                              #IMPLIED
             modification 
                        CDATA                              #IMPLIED  >
             
<!--                    LONG NAME: Brand                           -->
<!ELEMENT brand         (%words.cnt;)*                               >


<!--                    LONG NAME: Series                          -->
<!ELEMENT series        (%words.cnt;)*                               >


<!--                    LONG NAME: Platform                        -->
<!ELEMENT platform      (%words.cnt;)*                               >


<!--                    LONG NAME: Program Number                  -->
<!ELEMENT prognum       (%words.cnt;)*                               >


<!--                    LONG NAME: Feature Number                  -->
<!ELEMENT featnum       (%words.cnt;)*                               >


<!--                    LONG NAME: Component                       -->
<!ELEMENT component     (%words.cnt;)*                               >


<!--                    LONG NAME: Other Metadata                  -->
<!--                    NOTE: needs to be HTML-equiv, at least     -->
<!ELEMENT othermeta     EMPTY                                        >
<!ATTLIST othermeta 
             name       CDATA                            #REQUIRED
             content    CDATA                            #REQUIRED
             translate-content
                        (yes | no)                        #IMPLIED
             %select-atts;                                           >


<!--                    LONG NAME: Resource Identifier             -->
<!ELEMENT resourceid    EMPTY                                        >
<!ATTLIST resourceid
             id         CDATA                            #REQUIRED
             appname    CDATA                             #IMPLIED   >             


<!--                    LONG NAME: Index Term                      -->
<!ELEMENT indexterm     (%words.cnt;|%indexterm;)*                   >
<!ATTLIST indexterm
             keyref     CDATA                             #IMPLIED
             %univ-atts;                                             >             
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
             

<!ATTLIST author      %global-atts;  class CDATA "- topic/author "      >
<!ATTLIST source      %global-atts;  class CDATA "- topic/source "      >
<!ATTLIST publisher   %global-atts;  class CDATA "- topic/publisher "   >
<!ATTLIST copyright   %global-atts;  class CDATA "- topic/copyright "   >
<!ATTLIST copyryear   %global-atts;  class CDATA "- topic/copyryear "   >
<!ATTLIST copyrholder %global-atts;  class CDATA "- topic/copyrholder " >
<!ATTLIST critdates   %global-atts;  class CDATA "- topic/critdates "   >
<!ATTLIST created     %global-atts;  class CDATA "- topic/created "     >
<!ATTLIST revised     %global-atts;  class CDATA "- topic/revised "     >
<!ATTLIST permissions %global-atts;  class CDATA "- topic/permissions " >
<!ATTLIST category    %global-atts;  class CDATA "- topic/category "    >
<!ATTLIST audience    %global-atts;  class CDATA "- topic/audience "    >
<!ATTLIST keywords    %global-atts;  class CDATA "- topic/keywords "    >
<!ATTLIST prodinfo    %global-atts;  class CDATA "- topic/prodinfo "    >
<!ATTLIST prodname    %global-atts;  class CDATA "- topic/prodname "    >
<!ATTLIST vrmlist     %global-atts;  class CDATA "- topic/vrmlist "     >
<!ATTLIST vrm         %global-atts;  class CDATA "- topic/vrm "         >
<!ATTLIST brand       %global-atts;  class CDATA "- topic/brand "       >
<!ATTLIST series      %global-atts;  class CDATA "- topic/series "      >
<!ATTLIST platform    %global-atts;  class CDATA "- topic/platform "    >
<!ATTLIST prognum     %global-atts;  class CDATA "- topic/prognum "     >
<!ATTLIST featnum     %global-atts;  class CDATA "- topic/featnum "     >
<!ATTLIST component   %global-atts;  class CDATA "- topic/component "   >
<!ATTLIST othermeta   %global-atts;  class CDATA "- topic/othermeta "   >
<!ATTLIST resourceid  %global-atts;  class CDATA "- topic/resourceid "  >
<!ATTLIST indexterm   %global-atts;  class CDATA "- topic/indexterm "   >

<!-- ================== End Metadata  ================================ -->