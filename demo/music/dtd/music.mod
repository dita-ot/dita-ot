<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2006 All Rights Reserved. -->
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Reference                                    -->
<!--  VERSION:   1.O                                               -->
<!--  DATE:      January 2006                                      -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//RDA//ELEMENTS DITA Musical Reference//EN"
      Delivered as file "music.mod"                                -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for Music Collections                  -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             January 2006                                      -->
<!--                                                               -->
<!--             (C) Copyright IBM Corporation 2006.               -->
<!--             All Rights Reserved.                              -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                   ARCHITECTURE ENTITIES                       -->
<!-- ============================================================= -->

<!-- default namespace prefix for DITAArchVersion attribute can be
     overridden through predefinition in the document type shell   -->
<!ENTITY % DITAArchNSPrefix
                       "ditaarch"                                    >

<!-- must be instanced on each topic type                          -->
<!ENTITY % arch-atts "
             xmlns:%DITAArchNSPrefix; 
                        CDATA                              #FIXED
                       'http://dita.oasis-open.org/architecture/2005/'
             %DITAArchNSPrefix;:DITAArchVersion
                        CDATA                              #FIXED
                       '1.0'"                                        >


<!-- ============================================================= -->
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->



<!ENTITY % music-info-types 
                      "musicCollection"                              >


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->


<!ENTITY % musicCollection "musicCollection"                         >
<!ENTITY % musicBody   "musicBody"                                   >
<!ENTITY % cdList      "cdList"                                      >
<!ENTITY % cdHeader    "cdHeader"                                    >
<!ENTITY % bandHeader  "bandHeader"                                  >
<!ENTITY % albumsHeader "albumsHeader"                               >
<!ENTITY % commentsHeader "commentsHeader"                           >
<!ENTITY % cdRow       "cdRow"                                       >
<!ENTITY % band        "band"                                        >
<!ENTITY % albums      "albums"                                      >
<!ENTITY % comments    "comments"                                    >

<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->


<!ENTITY included-domains ""                                         >

<!ENTITY % univ-atts-translate-off     
            '%id-atts;
             %select-atts;
             translate 
                       (yes | no)                          "no"
             xml:lang   NMTOKEN                            #IMPLIED' >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Music Collection                -->
<!ELEMENT musicCollection     (%title;, (%titlealts;)?, (%shortdesc;)?, 
                         (%prolog;)?, (%musicBody;)?, (%related-links;)?, 
                         (%music-info-types;)* )                     >
<!ATTLIST musicCollection
             id         ID                               #REQUIRED
             conref     CDATA                            #IMPLIED
             %select-atts;
             xml:lang   NMTOKEN                          #IMPLIED
             %arch-atts;
             domains    CDATA                  "&included-domains;"
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Music Body                      -->
<!ELEMENT musicBody       ((%section; | %simpletable; | %cdList;)* )          >
<!ATTLIST musicBody         
             %id-atts;
             translate  (yes | no)                       #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Properties                      -->
<!ELEMENT cdList    ((%cdHeader;)?, (%cdRow;)+)               >
<!ATTLIST cdList      
             relcolwidth 
                        CDATA                           "1* 2* 3*"
             keycol     NMTOKEN                         #IMPLIED
             refcols    NMTOKENS                        #IMPLIED
             spectitle  CDATA                           #IMPLIED
             %display-atts;
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME:  Property Head                  -->
<!ELEMENT cdHeader      ((%bandHeader;)?, (%albumsHeader;)?, 
                         (%commentsHeader;)?)                            >
<!ATTLIST cdHeader       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: Property Type Head              -->
<!ELEMENT bandHeader    (%tblcell.cnt;)*                             >
<!ATTLIST bandHeader     
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property Value Head             -->
<!ELEMENT albumsHeader   (%tblcell.cnt;)*                             >
<!ATTLIST albumsHeader     
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property Schedule               -->
<!ELEMENT commentsHeader    (%tblcell.cnt;)*                             >
<!ATTLIST commentsHeader    
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property                        -->
<!ELEMENT cdRow      ((%band;)?, (%albums;)?, 
                         (%comments;)?)                              >
<!ATTLIST cdRow       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property Type                   -->
<!ELEMENT band      (%ph.cnt;)*                                  >
<!ATTLIST band       
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property Value                  -->
<!ELEMENT albums     (#PCDATA | %ol;)*                               >
<!ATTLIST albums      
             specentry  CDATA                            #IMPLIED
             %univ-atts-translate-off;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Property Descrption             -->
<!ELEMENT comments      (#PCDATA | %basic.ph; | %image; | %p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;)*                                  >
<!ATTLIST comments        
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >

             
<!ATTLIST musicCollection %global-atts; class CDATA "- topic/topic       reference/reference  musicCollection/musicCollection " >
<!ATTLIST musicBody     %global-atts;  class  CDATA "- topic/body        reference/refbody    musicCollection/musicBody "   >
<!ATTLIST cdList  %global-atts;  class  CDATA "- topic/simpletable       reference/properties musicCollection/cdList ">
<!ATTLIST cdHeader    %global-atts;  class  CDATA "- topic/sthead        reference/prophead   musicCollection/cdHeader "  >
<!ATTLIST bandHeader    %global-atts;  class  CDATA "- topic/stentry     reference/proptypehd   musicCollection/bandHeader "  >
<!ATTLIST albumsHeader   %global-atts;  class  CDATA "- topic/stentry    reference/propvaluehd  musicCollection/albumsHeader " >
<!ATTLIST commentsHeader    %global-atts;  class  CDATA "- topic/stentry reference/propdeschd   musicCollection/commentsHeader "  >

<!ATTLIST cdRow    %global-atts;  class  CDATA "- topic/strow       reference/property  musicCollection/cdRow "  >
<!ATTLIST band     %global-atts;  class  CDATA "- topic/stentry     reference/proptype  musicCollection/band ">
<!ATTLIST albums   %global-atts;  class  CDATA "- topic/stentry     reference/propvalue musicCollection/albums ">
<!ATTLIST comments %global-atts;  class  CDATA "- topic/stentry     reference/propdesc  musicCollection/comments ">

 
<!-- ================== End DITA Reference  =========================== -->

