<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA DITA Topic                                   -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Topic//EN"
      Delivered as file "topic.mod"                                -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and sepcialization         -->
<!--             attributes for the Programming Domain             -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005.                    -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
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
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->


<!--                    Definitions of declared elements           -->
<!ENTITY % topicDefns   PUBLIC 
                       "-//OASIS//ENTITIES DITA Topic Definitions//EN" 
                       "topicDefn.ent"                              >
%topicDefns;


<!-- ============================================================= -->
<!--                    ENTITY DECLARATIONS FOR ATTRIBUTE VALUES   -->
<!-- ============================================================= -->




<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->


<!--                   Phrase/inline elements of various classes   -->
<!ENTITY % basic.ph    "%ph; | %term; | %xref; | %cite; | %q; |
                        %boolean; | %state; | %keyword; | %tm;"      >

<!--                   Elements common to most body-like contexts  -->
<!ENTITY % basic.block "%p; | %lq; | %note; | %dl; | %ul; | %ol;|  
                        %sl; | %pre; | %lines; | %fig; | %image; | 
                        %object; |  %table; | %simpletable;">

<!-- class groupings to preserve in a schema -->

<!ENTITY % basic.phandblock     "%basic.ph; | %basic.block;"         >


<!-- Exclusions: models modified by removing excluded content      -->
<!ENTITY % basic.ph.noxref
                      "%ph;|%term;|              %q;|%boolean;|%state;|%keyword;|%tm;">
<!ENTITY % basic.ph.notm
                      "%ph;|%term;|%xref;|%cite;|%q;|%boolean;|%state;|%keyword;">


<!ENTITY % basic.block.notbl
                      "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;">
<!ENTITY % basic.block.nonote
                      "%p;|%lq;|       %dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.nopara
                      "    %lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.nolq
                      "%p;|     %note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|%fig;|%image;|%object;|%table;|%simpletable;">
<!ENTITY % basic.block.notbnofg
                      "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|      %image;|%object;">
<!ENTITY % basic.block.notbfgobj
                      "%p;|%lq;|%note;|%dl;|%ul;|%ol;|%sl;|%pre;|%lines;|      %image;">


<!-- Inclusions: defined sets that can be added into appropriate models -->
<!ENTITY % txt.incl             '%draft-comment;|%required-cleanup;|%fn;|%indextermref;|%indexterm;'>

<!-- Predefined content model groups, based on the previous, element-only categories: -->
<!-- txt.incl is appropriate for any mixed content definitions (those that have PCDATA) -->
<!-- the context for blocks is implicitly an InfoMaster "containing_division" -->
<!ENTITY % body.cnt             "%basic.block; | %required-cleanup;">
<!ENTITY % section.cnt          "#PCDATA | %basic.ph; | %basic.block; | %title; |  %txt.incl;">
<!ENTITY % section.notitle.cnt  "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;">
<!ENTITY % listitem.cnt         "#PCDATA | %basic.ph; | %basic.block; |%itemgroup;| %txt.incl;">
<!ENTITY % itemgroup.cnt        "#PCDATA | %basic.ph; | %basic.block; |             %txt.incl;">
<!ENTITY % title.cnt            "#PCDATA | %basic.ph.noxref; | %image;">
<!ENTITY % xreftext.cnt         "#PCDATA | %basic.ph.noxref; | %image;">
<!ENTITY % xrefph.cnt           "#PCDATA | %basic.ph.noxref;">
<!ENTITY % shortquote.cnt       "#PCDATA | %basic.ph;">
<!ENTITY % para.cnt             "#PCDATA | %basic.ph; | %basic.block.nopara; | %txt.incl;">
<!ENTITY % note.cnt             "#PCDATA | %basic.ph; | %basic.block.nonote; | %txt.incl;">
<!ENTITY % longquote.cnt        "#PCDATA | %basic.ph; | %basic.block.nolq;   | %txt.incl;">
<!ENTITY % tblcell.cnt          "#PCDATA | %basic.ph; | %basic.block.notbl;  | %txt.incl;">
<!ENTITY % desc.cnt             "#PCDATA | %basic.ph; | %basic.block.notbfgobj;">
<!ENTITY % ph.cnt               "#PCDATA | %basic.ph; | %image;              | %txt.incl;">
<!ENTITY % fn.cnt               "#PCDATA | %basic.ph; | %basic.block.notbl;">
<!ENTITY % term.cnt             "#PCDATA | %basic.ph; | %image;">
<!ENTITY % defn.cnt             "%listitem.cnt;">
<!ENTITY % pre.cnt              "#PCDATA | %basic.ph; | %txt.incl;">
<!ENTITY % fig.cnt              "%basic.block.notbnofg; | %simpletable;">
<!ENTITY % words.cnt            "#PCDATA | %keyword; | %term;">


<!-- ============================================================= -->
<!--                COMMON ENTITY DECLARATIONS                     -->
<!-- ============================================================= -->

<!-- for use within the DTD and supported topics; these will NOT work
     outside of this DTD or dtds that specialize from it!          -->
<!ENTITY nbsp                   "&#160;"                             >


<!-- ============================================================= -->
<!--                    NOTATION DECLARATIONS                      -->
<!-- ============================================================= -->
<!--                    DITA uses the direct reference model; 
                        notations may be added later as required   -->


<!-- ============================================================= -->
<!--                    STRUCTURAL MEMBERS                         -->
<!-- ============================================================= -->

<!ENTITY % topicreftypes 'topic | concept | task | reference | 
                          external | local'                          >

<!ENTITY % info-types    'topic'                                     > 


<!-- ============================================================= -->
<!--                    COMMON ATTLIST SETS                        -->
<!-- ============================================================= -->


<!ENTITY % date-format 'CDATA'                                       >

<!ENTITY % rel-atts      
            'type       CDATA                              #IMPLIED
             role      (parent | child | sibling | 
                        friend | next | previous | cousin | 
                        ancestor | descendant | sample | 
                        external | other)                  #IMPLIED
             otherrole  CDATA                              #IMPLIED' >

<!ENTITY % display-atts  
            'scale     (50|60|70|80|90|100|110|120|140|160|
                        180|200)                           #IMPLIED
             frame     (top | bottom |topbot | all | 
                        sides | none)                      #IMPLIED
             expanse   (page | column | textline)           #IMPLIED' >


<!ENTITY % select-atts   
            'platform   CDATA                              #IMPLIED
             product    CDATA                              #IMPLIED
             audience   CDATA                              #IMPLIED
             otherprops 
                        CDATA #IMPLIED
             importance 
                       (obsolete | deprecated | optional | 
                        default | low | normal | high | 
                        recommended | required | urgent )  #IMPLIED
             rev        CDATA                              #IMPLIED
             status    (new | changed | deleted | 
                        unchanged)                         #IMPLIED' >

<!ENTITY % id-atts  
            'id         NMTOKEN                            #IMPLIED
             conref     CDATA                              #IMPLIED' >

<!ENTITY % univ-atts     
            '%id-atts;
             %select-atts;
             translate 
                       (yes | no)                          #IMPLIED
             xml:lang   NMTOKEN                            #IMPLIED' >

<!ENTITY % global-atts    
            'xtrc       CDATA                              #IMPLIED
             xtrf       CDATA                              #IMPLIED
             xmlns      CDATA                              #FIXED ""'>


<!-- ============================================================= -->
<!--                    SPECIALIZATION OF DECLARED ELEMENTS        -->
<!-- ============================================================= -->

<!ENTITY % topic-info-types "%info-types;">


<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->

<!ENTITY included-domains ""                                         >
  

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!--                    LONG NAME: Topic                           -->
<!ELEMENT topic         (%title;, (%titlealts;)?, (%shortdesc;)?, 
                         (%prolog;)?, (%body;)?, (%related-links;)?,
                         (%topic-info-types;)* )                     >
<!ATTLIST topic           
             id         ID                                 #REQUIRED
             conref     CDATA                              #IMPLIED
             %select-atts;
             outputclass 
                        CDATA                              #IMPLIED
             xml:lang   NMTOKEN                            #IMPLIED
             %arch-atts;
             domains    CDATA                    "&included-domains;">


<!--                    LONG NAME: Title                           -->
<!--                    This is referenced inside CALS table       -->
<!ELEMENT title         (%title.cnt;)*                               > 
<!ATTLIST title         
             %id-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Title Alternatives              -->
<!ELEMENT titlealts     ((%navtitle;)?, (%searchtitle;)?)            >
<!ATTLIST titlealts      
             %id-atts;                                               >


<!--                    LONG NAME: Navigation Title                -->
<!ELEMENT navtitle      (%words.cnt;)*                               >
<!ATTLIST navtitle     
             %id-atts;                                               >

<!--                    LONG NAME: Search Title                    -->
<!ELEMENT searchtitle   (%words.cnt;)*                               >
<!ATTLIST searchtitle     
             %id-atts;                                               >


<!--                    LONG NAME: Short Description               -->
<!ELEMENT shortdesc     (%title.cnt;)*                               >
<!ATTLIST shortdesc    
             %id-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Body                            -->
<!ELEMENT body          (%body.cnt; | %section; | %example;)*        >
<!ATTLIST body            
             %id-atts;
             translate  (yes | no)                       #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: No Topic nesting                -->
<!ELEMENT no-topic-nesting EMPTY                                     >


<!--                    LONG NAME: Section                         -->
<!ELEMENT section       (%section.cnt;)*                             >
<!ATTLIST section         
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: Example                         -->
<!ELEMENT example       (%section.cnt;)*                             >
<!ATTLIST example         
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Description                     -->
<!--                    Desc is used in context with figure and 
                        table titles and also for content models 
                        within linkgroup and object (for 
                        accessibility)                             -->
<!ELEMENT desc          (%desc.cnt;)*                                >
<!ATTLIST desc           
             %id-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!-- ============================================================= -->
<!--                    PROLOG (METADATA FOR TOPICS)               -->
<!--                    TYPED DATA ELEMENTS                        -->
<!-- ============================================================= -->
<!--                    typed content definitions                  -->
<!--                    typed, localizable content                 -->

<!--                    LONG NAME: Prolog                          -->
<!ELEMENT prolog        ((%author;)*, (%source;)?, (%publisher;)?,
                         (%copyright;)*, (%critdates;)?,
                         (%permissions;)?, (%metadata;)*, 
                         (%resourceid;)*)                            >


<!--                    LONG NAME: Metadata                        -->
<!ELEMENT metadata       ((%audience;)*, (%category;)*, (%keywords;)*,
                          (%prodinfo;)*, (%othermeta;)*)             >
<!ATTLIST metadata        
              mapkeyref CDATA                             #IMPLIED   >



<!-- ============================================================= -->
<!--                    BASIC DOCUMENT ELEMENT DECLARATIONS        -->
<!--                    (rich text)                                -->
<!-- ============================================================= -->

<!--                    LONG NAME: Paragraph                       -->
<!ELEMENT p             (%para.cnt;)*                                >
<!ATTLIST p              
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Note                            -->
<!ELEMENT note          (%note.cnt;)*                                >
<!ATTLIST note            
             type       (note | tip | fastpath | restriction |
                         important | remember| attention|
                         caution | danger| other)        #IMPLIED             
             spectitle  CDATA                            #IMPLIED
             othertype  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Long Quote (Excerpt)            -->
<!ELEMENT lq            (%longquote.cnt;)*                           >
<!ATTLIST lq              
             href       CDATA                           #IMPLIED
             keyref     CDATA                           #IMPLIED
             type       (external | internal | 
                         bibliographic)                 #IMPLIED
             reftitle   CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Quoted text                     -->
<!ELEMENT q             (%shortquote.cnt;)*                          >
<!ATTLIST q              
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Simple List                     -->
<!ELEMENT sl            (%sli;)+                                     >
<!ATTLIST sl            
             compact    (yes | no)                       #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Simple List Item                -->
<!ELEMENT sli           (%ph.cnt;)*                                  >
<!ATTLIST sli             
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Unordered List                  -->
<!ELEMENT ul            (%li;)+                                      >
<!ATTLIST ul            
             compact    (yes | no)                       #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Ordered List                    -->
<!ELEMENT ol            (%li;)+                                      >
<!ATTLIST ol              
             compact    (yes | no)                       #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: List Item                      -->
<!ELEMENT li            (%listitem.cnt;)*                           >
<!ATTLIST li             
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Item Group                      -->
<!ELEMENT itemgroup     (%itemgroup.cnt;)*                           >
<!ATTLIST itemgroup       
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Definition List                 -->
<!ELEMENT dl            ((%dlhead;)?, (%dlentry;)+)                  >
<!ATTLIST dl              
             compact    (yes | no)                       #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Definition List Head            -->
<!ELEMENT dlhead        ((%dthd;)?, (%ddhd;)? )                      >
<!ATTLIST dlhead        
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Term Header                     -->
<!ELEMENT dthd          (%title.cnt;)*                               >
<!ATTLIST dthd           
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Definition Header               -->
<!ELEMENT ddhd          (%title.cnt;)*                               >
<!ATTLIST ddhd           
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Definition List Entry           -->
<!ELEMENT dlentry       ((%dt;)+, (%dd;)+ )                          >
<!ATTLIST dlentry       
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >



<!--                    LONG NAME: Definition Term                 -->  
<!ELEMENT dt            (%term.cnt;)*                                >
<!ATTLIST dt            
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Definition Description          -->
<!ELEMENT dd            (%defn.cnt;)*                                >
<!ATTLIST dd           
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: Figure                          -->
<!ELEMENT fig           ((%title;)?, (%desc;)?, 
                         (%figgroup; | %fig.cnt;)* )                 >
<!ATTLIST fig          
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Figure Group                    -->
<!ELEMENT figgroup      ((%title;)?, 
                         (%figgroup; | %xref; | %fn; | %ph; | 
                          %keyword;)* )                              >
<!ATTLIST figgroup     
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >



<!--                    LONG NAME: Preformatted Text               -->
<!ELEMENT pre           (%pre.cnt;)*                                 >                                
<!ATTLIST pre          
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             xml:space  (preserve)               #FIXED 'preserve'
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Line Respecting Text            -->
<!ELEMENT lines         (%pre.cnt;)*                                 >
<!ATTLIST lines           
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             xml:space  (preserve)               #FIXED 'preserve'
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!-- ============================================================= -->
<!--                   BASE FORM PHRASE TYPES                      -->
<!-- ============================================================= -->


<!--                    LONG NAME: Keyword                         -->
<!ELEMENT keyword       (#PCDATA | %tm;)*                            >
<!ATTLIST keyword       
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                           #IMPLIED     >


<!--                    LONG NAME: Term                            -->
<!ELEMENT term          (#PCDATA | %tm;)*                            >
<!ATTLIST term          
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Phrase                          -->
<!ELEMENT ph            (%ph.cnt;)*                                  >  
<!ATTLIST ph              
             keyref     CDATA                           #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Trade Mark                      -->
<!ELEMENT tm            (#PCDATA | %tm;)*                            >
<!ATTLIST tm
             trademark  CDATA                           #IMPLIED
             tmowner    CDATA                           #IMPLIED
             tmtype     (tm | reg | service)            #REQUIRED
             tmclass    CDATA                           #IMPLIED     >


<!--                    LONG NAME: Boolean  (deprecated)           -->
<!ELEMENT boolean       EMPTY                                        >
<!ATTLIST boolean           
             state      (yes | no)                      #REQUIRED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: State                           -->
<!--                    A state can have a name and a string value, 
                        even if empty or indeterminate             -->
<!ELEMENT state         EMPTY                                        >
<!ATTLIST state          
             name       CDATA                            #REQUIRED
             value      CDATA                            #REQUIRED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: Image Data                      -->
<!ELEMENT image         (%alt;)?                                     >
<!ATTLIST image           
             href       CDATA                            #REQUIRED
             keyref     NMTOKEN                          #IMPLIED
             alt        CDATA                            #IMPLIED
             longdescref 
                        CDATA                            #IMPLIED
             height     NMTOKEN                          #IMPLIED
             width      NMTOKEN                          #IMPLIED
             align      CDATA                            #IMPLIED
             placement  (inline|break)                   "inline"
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Required Cleanup Block          -->
<!ELEMENT alt            (%words.cnt;)*>
<!ATTLIST alt             %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!--                    LONG NAME: Object (Streaming/Executable 
                                   Data)                           -->
<!ELEMENT object        ((%desc;)?, (%param;)*)                      >
<!ATTLIST object
             declare    (declare)                        #IMPLIED
             classid    CDATA                            #IMPLIED
             codebase   CDATA                            #IMPLIED
             data       CDATA                            #IMPLIED
             type       CDATA                            #IMPLIED
             codetype   CDATA                            #IMPLIED
             archive    CDATA                            #IMPLIED
             standby    CDATA                            #IMPLIED
             height     NMTOKEN                          #IMPLIED
             width      NMTOKEN                          #IMPLIED
             usemap     CDATA                            #IMPLIED
             name       CDATA                            #IMPLIED
             tabindex   NMTOKEN                          #IMPLIED
             longdescre CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter                       -->
<!ELEMENT param         EMPTY>
<!ATTLIST param
             name       CDATA                            #REQUIRED
             id         ID                               #IMPLIED
             value      CDATA                            #IMPLIED
             valuetype  (data|ref|object)                #IMPLIED
             type       CDATA                            #IMPLIED    >  


<!--                    LONG NAME: Simple Table                    -->
<!ELEMENT simpletable   ((%sthead;)?, (%strow;)+)                    >
<!ATTLIST simpletable     
             relcolwidth 
                        CDATA                            #IMPLIED
             keycol     NMTOKEN                          #IMPLIED
             refcols    NMTOKENS                         #IMPLIED
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Simple Table Head               -->
<!ELEMENT sthead        (%stentry;)+                                 >
<!ATTLIST sthead     
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Simple Table Row                -->
<!ELEMENT strow         (%stentry;)*                                 >
<!ATTLIST strow        
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Simple Table Cell (entry)       -->
<!ELEMENT stentry       (%tblcell.cnt;)*                             >
<!ATTLIST stentry 
             specentry  CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >

<!--                    LONG NAME: Review Comments Bloc            -->
<!ELEMENT draft-comment (#PCDATA | %basic.phandblock;)*              >
<!ATTLIST draft-comment   
             author     CDATA                            #IMPLIED
             time       CDATA                            #IMPLIED
             disposition  
                        (issue | open | accepted | rejected |
                         deferred| duplicate | reopened|
                         unassigned | completed)         #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Required Cleanup Block          -->
<!ELEMENT required-cleanup 
                        ANY                                          >
<!ATTLIST required-cleanup 
             remap      CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Footnote                        -->
<!ELEMENT fn            (%fn.cnt;)*                                  >
<!ATTLIST fn              
             callout    CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Link                            -->
<!ELEMENT indextermref   EMPTY>               <!-- Index term reference -->
<!ATTLIST indextermref    keyref CDATA #REQUIRED
                          %univ-atts;
>

<!--                    LONG NAME: Citation (bibliographic source) -->
<!ELEMENT cite          (%xrefph.cnt;)*                              >
<!ATTLIST cite            
             keyref     CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Cross Reference/Link            -->
<!ELEMENT xref          (%xreftext.cnt; | %desc;)*                   >
<!ATTLIST xref            
             href       CDATA                            #IMPLIED
             keyref     CDATA                            #IMPLIED
             type       CDATA                            #IMPLIED
             %univ-atts;
             format     CDATA                            #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!-- ============================================================= -->
<!--                      LINKING GROUPING                         -->
<!-- ============================================================= -->


<!--                    LONG NAME: Related Links                   -->
<!ELEMENT related-links (%link; | %linklist; | %linkpool;)+          >
<!ATTLIST related-links  
             %rel-atts;
             %select-atts;
             format     CDATA                            #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Link                            -->
<!ELEMENT link          ((%linktext;)?, (%desc;)?)                   >
<!ATTLIST link            
             href       CDATA                            #IMPLIED
             keyref     CDATA                            #IMPLIED
             query      CDATA                            #IMPLIED
             %rel-atts;
             %select-atts;
             format     CDATA                            #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Link Text                       -->
<!ELEMENT linktext      (%words.cnt;)*                               >


<!--                    LONG NAME: Link List                       -->
<!ELEMENT linklist      ((%title;)?, (%desc;)?,
                         (%linklist; | %link;)*, (%linkinfo;)?)      >
<!ATTLIST linklist        
            collection-type 
                        (unordered | sequence | choice |
                         tree | family)                   #IMPLIED
             duplicates (yes | no)                        #IMPLIED
                          mapkeyref CDATA #IMPLIED
             %rel-atts;
             %select-atts;
             spectitle  CDATA                            #IMPLIED
             format     CDATA                            #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Link Information                -->
<!ELEMENT linkinfo      (%desc.cnt;)*                                >


<!--                    LONG NAME: Link Pool                       -->
<!ELEMENT linkpool      (%linkpool; | %link;)*                       >
<!ATTLIST linkpool        
             collection-type 
                        (unordered | sequence | choice |
                         tree | family)                   #IMPLIED
             duplicates (yes | no)                        #IMPLIED
             mapkeyref  CDATA                             #IMPLIED
             %rel-atts;
             %select-atts;
             format     CDATA   #IMPLIED
             scope      (local | peer | external)        #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >



<!-- ============================================================= -->
<!--                    MODULES CALLS                              -->
<!-- ============================================================= -->


<!--                      Table Elements                           -->
<!ENTITY % tableXML       PUBLIC  
"-//OASIS//ELEMENTS DITA CALS Tables//EN" 
"tblDecl.mod"                                                        >
%tableXML;

<!--                       MetaData Elements, plus keyword and 
                           indexterm                               -->
<!ENTITY % metaXML         PUBLIC 
"-//OASIS//ELEMENTS DITA Metadata//EN" 
"metaDecl.mod"                                                       >
%metaXML;

<!--                       Specialization of Declared Elements     -->
<!ENTITY % topicClasses    PUBLIC 
"-//OASIS//ENTITIES DITA Topic Class//EN" 
"topicAttr.mod"                                                    >
  %topicClasses;


<!-- ================== End DITA Topic  ========================== -->