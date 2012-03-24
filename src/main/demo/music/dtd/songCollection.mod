<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2006 All Rights Reserved. -->
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Song Reference                               -->
<!--  VERSION:   2.O                                               -->
<!--  DATE:      October 2009                                      -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//RDA//ELEMENTS DITA Song Reference//EN"
      Delivered as file "songs.mod"                                -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Music Collections                  -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             January 2006                                      -->
<!-- UPDATED:                                                      -->
<!--    20091013: Version 2.0 adds DITA 1.1 features;
                  adds play count and last played date             -->
<!--                                                               -->
<!--             (C) Copyright IBM Corporation 2006.               -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ARCHITECTURE ENTITIES                       -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->



<!ENTITY % song-info-types 
                      "songCollection"                              >


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->


<!ENTITY % songCollection "songCollection">
<!ENTITY % songBody   "songBody">
<!ENTITY % songList      "songList">
<!--
<!ENTITY % songListHeader  "songListHeader">
<!ENTITY % songHeader  "songHeader">
<!ENTITY % albumsHeader "albumsHeader">
<!ENTITY % artistHeader "artistHeader">
<!ENTITY % genreHeader "genreHeader">
<!ENTITY % ratingHeader "ratingHeader">
<!ENTITY % countHeader "countHeader">
<!ENTITY % playdateHeader "playdateHeader">
-->
<!ENTITY % songRow       "songRow">
<!ENTITY % song        "song">
<!ENTITY % album      "album">
<!ENTITY % artist    "artist">
<!ENTITY % genre    "genre">
<!ENTITY % rating    "rating">
<!ENTITY % count    "count">
<!ENTITY % playdate    "playdate">

<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->


<!ENTITY included-domains ""                                         >

<!ENTITY % univ-atts-translate-off     
            '%id-atts;
             %select-atts;
             translate 
                       (yes | no |-dita-use-conref-target) "no"
             xml:lang   NMTOKEN                            #IMPLIED
             dir       (lro | ltr | 
                        rlo | rtl | -dita-use-conref-target) 
                                                           #IMPLIED' >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Music Collection                -->
<!ELEMENT songCollection     (%title;, (%titlealts;)?, (%shortdesc; | %abstract;)?, 
                         (%prolog;)?, (%songBody;)?, (%related-links;)?, 
                         (%song-info-types;)* )                     >
<!ATTLIST songCollection
             id         ID                               #REQUIRED
             conref     CDATA                            #IMPLIED
             %select-atts;
             %localization-atts;
             %arch-atts;
             domains    CDATA                  "&included-domains;"
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Music Body                      -->
<!ELEMENT songBody       ((%section; | %simpletable; | %songList;)* )          >
<!ATTLIST songBody         
             %id-atts;
             translate  (yes | no)                       #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME:                       -->
<!ELEMENT songList    ((%songRow;)+)               >
<!ATTLIST songList      
             relcolwidth 
                        CDATA                           #IMPLIED
             keycol     NMTOKEN                         #IMPLIED
             refcols    NMTOKENS                        #IMPLIED
             spectitle  CDATA                           #IMPLIED
             %display-atts;
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--
<!ELEMENT songListHeader      ((%songHeader;)?, (%albumsHeader;)?, 
                         (%artistHeader;)?,(%genreHeader;)?,(%ratingHeader;)?,
                         (%countHeader;)?,(%playdateHeader;)?) >
<!ATTLIST songListHeader       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT songHeader    (%tblcell.cnt;)*                             >
<!ATTLIST songHeader     
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT albumsHeader   (%tblcell.cnt;)*                             >
<!ATTLIST albumsHeader     
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT artistHeader    (%tblcell.cnt;)*                             >
<!ATTLIST artistHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT genreHeader    (%tblcell.cnt;)*                             >
<!ATTLIST genreHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT ratingHeader    (%tblcell.cnt;)*                             >
<!ATTLIST ratingHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT countHeader    (%tblcell.cnt;)*                             >
<!ATTLIST countHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ELEMENT playdateHeader    (%tblcell.cnt;)*                             >
<!ATTLIST playdateHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
-->

<!--                    LONG NAME:         -->
<!ELEMENT songRow      ((%song;)?, (%album;)?, (%artist;)?,(%genre;)?,
                        (%rating;)?,(%count;)?,(%playdate;)?)>
<!ATTLIST songRow       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME:                    -->
<!ELEMENT song      (%ph.cnt;)*                                  >
<!ATTLIST song       
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME:                   -->
<!ELEMENT album     (%ph.cnt;)*                               >
<!ATTLIST album      
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME:              -->
<!ELEMENT artist      (%ph.cnt;)*                                  >
<!ATTLIST artist        
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME:              -->
<!ELEMENT genre      (%ph.cnt;)*                                  >
<!ATTLIST genre        
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME:              -->
<!ELEMENT rating      (%ph.cnt;)*                                  >
<!ATTLIST rating        
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME:              -->
<!ELEMENT count      (%ph.cnt;)*                                  >
<!ATTLIST count        
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
             
<!--                    LONG NAME:              -->
<!ELEMENT playdate      (%ph.cnt;)*                                  >
<!ATTLIST playdate        
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!ATTLIST songCollection %global-atts; class CDATA "- topic/topic       reference/reference  songCollection/songCollection " >
<!ATTLIST songBody     %global-atts;  class  CDATA "- topic/body        reference/refbody    songCollection/songBody "   >
<!ATTLIST songList  %global-atts;  class  CDATA "- topic/simpletable       reference/simpletable songCollection/songList ">
<!--<!ATTLIST songListHeader    %global-atts;  class  CDATA "- topic/sthead        reference/sthead   songCollection/songListHeader "  >
<!ATTLIST songHeader    %global-atts;  class  CDATA "- topic/stentry     reference/stentry   songCollection/songHeader "  >
<!ATTLIST albumsHeader   %global-atts;  class  CDATA "- topic/stentry    reference/stentry  songCollection/albumsHeader " >
<!ATTLIST artistHeader    %global-atts;  class  CDATA "- topic/stentry reference/stentry   songCollection/artistHeader "  >
<!ATTLIST genreHeader    %global-atts;  class  CDATA "- topic/stentry reference/stentry   songCollection/genreHeader "  >
<!ATTLIST ratingHeader    %global-atts;  class  CDATA "- topic/stentry reference/stentry   songCollection/ratingHeader "  >
<!ATTLIST countHeader    %global-atts;  class  CDATA "- topic/stentry reference/stentry   songCollection/countHeader "  >
<!ATTLIST playdateHeader    %global-atts;  class  CDATA "- topic/stentry reference/stentry   songCollection/playdateHeader "  >-->

<!ATTLIST songRow    %global-atts;  class  CDATA "- topic/strow reference/strow    songCollection/songRow "  >
<!ATTLIST song     %global-atts;  class  CDATA "- topic/stentry reference/stentry  songCollection/song ">
<!ATTLIST album   %global-atts;  class  CDATA "- topic/stentry  reference/stentry  songCollection/album ">
<!ATTLIST artist %global-atts;  class  CDATA "- topic/stentry   reference/stentry  songCollection/artist ">
<!ATTLIST genre %global-atts;  class  CDATA "- topic/stentry    reference/stentry  songCollection/genre ">
<!ATTLIST rating %global-atts;  class  CDATA "- topic/stentry   reference/stentry  songCollection/rating ">
<!ATTLIST count %global-atts;  class  CDATA "- topic/stentry    reference/stentry  songCollection/count ">
<!ATTLIST playdate %global-atts;  class  CDATA "- topic/stentry reference/stentry  songCollection/playdate ">

 
<!-- ================== End DITA Musical Reference  =================== -->

