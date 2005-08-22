<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Task                                         -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Task//EN"
      Delivered as file "task.mod"                                 -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the DITA Tasks                     -->
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
<!--                   SPECIALIZATION OF DECLARED ELEMENTS         -->
<!-- ============================================================= -->

<!ENTITY % taskClasses SYSTEM "task_class.ent"                       >


<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % task        "task"                                        >
<!ENTITY % taskbody    "taskbody"                                    >
<!ENTITY % steps       "steps"                                       >
<!ENTITY % steps-unordered 
                       "steps-unordered"                             >
<!ENTITY % step        "step"                                        >
<!ENTITY % cmd         "cmd"                                         >
<!ENTITY % substeps    "substeps"                                    >
<!ENTITY % substep     "substep"                                     >
<!ENTITY % tutorialinfo 
                       "tutorialinfo"                                >
<!ENTITY % info        "info"                                        >
<!ENTITY % stepxmp     "stepxmp"                                     >
<!ENTITY % stepresult  "stepresult"                                  >
<!ENTITY % choices     "choices"                                     >
<!ENTITY % choice      "choice"                                      >
<!ENTITY % result      "result"                                      >
<!ENTITY % prereq      "prereq"                                      >
<!ENTITY % postreq     "postreq"                                     >
<!ENTITY % context     "context"                                     >
<!ENTITY % choicetable "choicetable"                                 >
<!ENTITY % chhead      "chhead"                                      >
<!ENTITY % chrow       "chrow"                                       >
<!ENTITY % choptionhd  "choptionhd"                                  >
<!ENTITY % chdeschd    "chdeschd"                                    >
<!ENTITY % choption    "choption"                                    >
<!ENTITY % chdesc      "chdesc"                                      >


<!-- ============================================================= -->
<!--                    SHARED ATTRIBUTE LISTS                     -->
<!-- ============================================================= -->


<!--                    Provide an alternative set of univ-atts 
                        that allows importance to be redefined 
                        locally                                    -->
<!ENTITY % univ-atts-no-importance-task
            '%id-atts;
             platform   CDATA                            #IMPLIED
             product    CDATA                            #IMPLIED
             audience   CDATA                            #IMPLIED
             otherprops CDATA                            #IMPLIED
             rev        CDATA                            #IMPLIED
             status     (new | changed | deleted |   
                         unchanged)                      #IMPLIED
             translate  (yes | no)                       #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED'                              >

<!ENTITY % task-info-types 
                        "%info-types;"                               >


<!-- ============================================================= -->
<!--                    DOMAINS ATTRIBUTE OVERRIDE                 -->
<!-- ============================================================= -->


<!ENTITY included-domains ""                                         >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Task                            -->
<!ELEMENT task          (%title;, (%titlealts;)?, (%shortdesc;)?, 
                         (%prolog;)?, (%taskbody;)?, 
                         (%related-links;)?, (%task-info-types;)* )  >
<!ATTLIST task            
             id         ID                               #REQUIRED
             conref     CDATA                            #IMPLIED
             %select-atts;
             xml:lang   NMTOKEN                          #IMPLIED
             %arch-atts;
             outputclass 
                        CDATA                            #IMPLIED
             domains    CDATA                "&included-domains;"    >


<!--                    LONG NAME: Task Body                       -->
<!ELEMENT taskbody      ((%prereq;)?, (%context;)?, 
                         (%steps; | %steps-unordered;)?, 
                         (%result;)?, (%example;)?, (%postreq;)?)    >
<!ATTLIST taskbody        
             %id-atts;
             translate  (yes|no)                         #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Prerequisites                   -->
<!ELEMENT prereq        (%section.notitle.cnt;)*                     >
<!ATTLIST prereq        
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Context                         -->
<!ELEMENT context       (%section.notitle.cnt;)*                     >
<!ATTLIST context        
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Steps                           -->
<!ELEMENT steps         ((%step;)+)                                  >
<!ATTLIST steps         
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Steps: Unordered                -->
<!ELEMENT steps-unordered 
                        ((%step;)+)                                  >
<!ATTLIST steps-unordered 
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Step                            -->
<!ELEMENT step          (%cmd;, 
                         (%info;  |%substeps; | %tutorialinfo; |
                          %stepxmp; | %choicetable; | %choices;)*, 
                         (%stepresult;)? )                           >
<!ATTLIST step            
             importance (optional | required)            #IMPLIED
             %univ-atts-no-importance-task;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--ATTLIST step          
               importance (optional | required)          #IMPLIED  -->


<!--                    LONG NAME: Command                         -->
<!ELEMENT cmd            (%ph.cnt;)* >
<!ATTLIST cmd             
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Information                     -->
<!ELEMENT info          (%itemgroup.cnt;)*                           >
<!ATTLIST info           
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Sub-steps                       -->
<!ELEMENT substeps      (%substep;)+                                 >
<!ATTLIST substeps       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Sub-step                        -->
<!ELEMENT substep       (%cmd;, 
                         (%info; | %tutorialinfo; | %stepxmp;)*, 
                         (%stepresult;)? )                           >
<!ATTLIST substep         
             importance (optional | required)            #IMPLIED
             %univ-atts-no-importance-task;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--ATTLIST substep       
               importance 
                        (optional | required)            #IMPLIED  -->


<!--                    LONG NAME: Tutorial Information            -->
<!ELEMENT tutorialinfo  (%itemgroup.cnt;)*                           >
<!ATTLIST tutorialinfo   
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Step Example                    -->
<!ELEMENT stepxmp       (%itemgroup.cnt;)*                           >
<!ATTLIST stepxmp       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choices                         -->
<!ELEMENT choices       ((%choice;)+)                                >
<!ATTLIST choices       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice                          -->
<!ELEMENT choice        (#PCDATA | %basic.ph; | %basic.block; |
                         %itemgroup; | %txt.incl;)*                  >
<!ATTLIST choice          
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Table                    -->
<!ELEMENT choicetable   ((%chhead;)?, (%chrow;)+ )                   >
<!ATTLIST choicetable     
             relcolwidth 
                        CDATA                            #IMPLIED
             keycol     NMTOKEN                          "1"
             refcols    NMTOKENS                         #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %display-atts;
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Head                     -->
<!ELEMENT chhead        ((%choptionhd;), (%chdeschd;) )              >
<!ATTLIST chhead         
             %univ-atts-no-importance-task;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Option Head              -->
<!ELEMENT choptionhd    (%tblcell.cnt;)*                             >
<!ATTLIST choptionhd     
             specentry  CDATA                           #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Schedule                 -->
<!ELEMENT chdeschd      (%tblcell.cnt;)*                             >
<!ATTLIST chdeschd      
             specentry  CDATA                           #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Row                      -->
<!ELEMENT chrow         ((%choption;), (%chdesc;) )                  >
<!ATTLIST chrow        
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Option                   -->
<!ELEMENT choption      (%tblcell.cnt;)*>
<!ATTLIST choption        
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Choice Description              -->
<!ELEMENT chdesc        (%tblcell.cnt;)*>
<!ATTLIST chdesc                
             specentry  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Step Result                     -->
<!ELEMENT stepresult    (%itemgroup.cnt;)*                           >
<!ATTLIST stepresult    
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Result                          -->
<!ELEMENT result        (%section.notitle.cnt;)*                     >
<!ATTLIST result         
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Post Requirements               -->
<!ELEMENT postreq       (%section.notitle.cnt;)*                     >
<!ATTLIST postreq        
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
             

<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST task        %global-atts;  class  CDATA "- topic/topic task/task "        >
<!ATTLIST taskbody    %global-atts;  class  CDATA "- topic/body task/taskbody "     >
<!ATTLIST steps       %global-atts;  class  CDATA "- topic/ol task/steps "          >
<!ATTLIST steps-unordered 
                      %global-atts;  class  CDATA "- topic/ul task/steps-unordered ">
<!ATTLIST step        %global-atts;  class  CDATA "- topic/li task/step "           >
<!ATTLIST cmd         %global-atts;  class  CDATA "- topic/ph task/cmd "            >
<!ATTLIST substeps    %global-atts;  class  CDATA "- topic/ol task/substeps "       >
<!ATTLIST substep     %global-atts;  class  CDATA "- topic/li task/substep "        >
<!ATTLIST tutorialinfo 
                      %global-atts;  class  CDATA "- topic/itemgroup task/tutorialinfo ">
<!ATTLIST info        %global-atts;  class  CDATA "- topic/itemgroup task/info "    >
<!ATTLIST stepxmp     %global-atts;  class  CDATA "- topic/itemgroup task/stepxmp " >
<!ATTLIST stepresult  %global-atts;  class  CDATA "- topic/itemgroup task/stepresult ">

<!ATTLIST choices     %global-atts;  class  CDATA "- topic/ul task/choices "        >
<!ATTLIST choice      %global-atts;  class  CDATA "- topic/li task/choice "         >
<!ATTLIST result       %global-atts;  class  CDATA "- topic/section task/result "   >
<!ATTLIST prereq      %global-atts;  class  CDATA "- topic/section task/prereq "    >
<!ATTLIST postreq     %global-atts;  class  CDATA "- topic/section task/postreq "   >
<!ATTLIST context     %global-atts;  class  CDATA "- topic/section task/context "   >

<!ATTLIST choicetable %global-atts;  class  CDATA "- topic/simpletable task/choicetable ">
<!ATTLIST chhead      %global-atts;  class  CDATA "- topic/sthead task/chhead "     >
<!ATTLIST chrow       %global-atts;  class  CDATA "- topic/strow task/chrow "       >
<!ATTLIST choptionhd  %global-atts;  class  CDATA "- topic/stentry task/choptionhd ">
<!ATTLIST chdeschd    %global-atts;  class  CDATA "- topic/stentry task/chdeschd "  >
<!ATTLIST choption    %global-atts;  class  CDATA "- topic/stentry task/choption "  >
<!ATTLIST chdesc      %global-atts;  class  CDATA "- topic/stentry task/chdesc "    >

 
<!-- ================== End DITA Task  =========================== -->
