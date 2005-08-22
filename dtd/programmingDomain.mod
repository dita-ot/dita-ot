<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA DITA Programming Domain                      -->
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
PUBLIC "-//OASIS//ELEMENTS DITA Programming Domain//EN"
      Delivered as file "programming-domain.mod"                   -->

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
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->


<!ENTITY % apiname      "apiname"                                    >
<!ENTITY % audience     "audience"                                   >
<!ENTITY % codeblock    "codeblock"                                  >
<!ENTITY % codeph       "codeph"                                     >
<!ENTITY % delim        "delim"                                      >
<!ENTITY % kwd          "kwd"                                        >
<!ENTITY % oper         "oper"                                       >
<!ENTITY % option       "option"                                     >
<!ENTITY % parmname     "parmname"                                   >
<!ENTITY % sep          "sep"                                        >
<!ENTITY % synph        "synph"                                      >
<!ENTITY % var          "var"                                        >

<!ENTITY % parml        "parml"                                      >
<!ENTITY % pd           "pd"                                         >
<!ENTITY % plentry      "plentry"                                    >
<!ENTITY % pt           "pt"                                         >

<!ENTITY % fragment     "fragment"                                   >
<!ENTITY % fragref      "fragref"                                    >
<!ENTITY % groupchoice  "groupchoice"                                >
<!ENTITY % groupcomp    "groupcomp"                                  >
<!ENTITY % groupseq     "groupseq"                                   >
<!ENTITY % repsep       "repsep"                                     >
<!ENTITY % synblk       "synblk"                                     >
<!ENTITY % synnote      "synnote"                                    >
<!ENTITY % synnoteref   "synnoteref"                                 >
<!ENTITY % syntaxdiagram 
                        "syntaxdiagram"                              >


<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->


<!--                    LONG NAME: Universal Attributes Local
                                   Importance                      -->
<!--                    Povide an alternative set of univ-atts that 
                        allows importance to be redefined locally  -->
<!ENTITY % univ-atts-no-importance
            '%id-atts;
             platform   CDATA                            #IMPLIED
             product    CDATA                            #IMPLIED
             audience   CDATA                            #IMPLIED
             otherprops 
                        CDATA                            #IMPLIED
             rev        CDATA                            #IMPLIED
             status     (new | changed | deleted |
                         unchanged)                      #IMPLIED
             translate  (yes|no)                         #IMPLIED
             xml:lang   NMTOKEN                          #IMPLIED'   > 


<!--                    LONG NAME: Code Emphasis                   -->
<!ELEMENT codeph        (#PCDATA | %basic.ph.notm;)*                 >
<!ATTLIST codeph      
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Code Block                      -->
<!ELEMENT codeblock     (#PCDATA | %basic.ph.notm; | %txt.incl;)*    >
<!ATTLIST codeblock       
             %display-atts;
             spectitle  CDATA                            #IMPLIED
             xml:space  (preserve)                  #FIXED 'preserve'
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Option                          -->
<!ELEMENT option         (#PCDATA)>
<!ATTLIST  option          keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED                 >


<!--                    LONG NAME: Variable                        -->
<!ELEMENT var           (%words.cnt;)*                               >
<!ATTLIST var         
             importance (optional | required | default)  #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter Name                  -->
<!ELEMENT parmname      (#PCDATA)                                    >
<!ATTLIST  parmname        
             keyref      CDATA #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Syntax Phrase                   -->
<!ELEMENT synph         (#PCDATA | %codeph; | %option; | %parmname; |
                         %var; | %kwd; | %oper; | %delim; | %sep; | 
                         %synph;)*                                   >
<!ATTLIST  synph   
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Operator                        -->
<!ELEMENT oper          (%words.cnt;)*                               >
<!ATTLIST oper            
             importance (optional | required | default)   #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                             #IMPLIED   >


<!--                    LONG NAME: Delimiter                       -->
<!ELEMENT delim         (%words.cnt;)*                               >
<!ATTLIST delim           
             importance   (optional | required)           #IMPLIED
             %univ-atts-no-importance;
             outputclass
                        CDATA                             #IMPLIED   >


<!--                    LONG NAME: Separator                       -->
<!ELEMENT sep           (%words.cnt;)*                               >
<!ATTLIST sep             
             importance (optional | required)            #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: API Name                        -->
<!ELEMENT apiname       (#PCDATA)                                    >
<!ATTLIST apiname         
             keyref     CDATA                            #IMPLIED
             %univ-atts;
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter List                  -->
<!ELEMENT parml         (%plentry;)+                                 >
<!ATTLIST parml           
             compact   (yes | no)                        #IMPLIED
             spectitle  CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter List Entry            -->
<!ELEMENT plentry       ((%pt;)+, (%pd;)+)                           >
<!ATTLIST plentry       
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter Term                  -->
<!ELEMENT pt            (%term.cnt;)*                                > 
<!ATTLIST pt           
             keyref     CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Parameter Description           -->
<!ELEMENT pd            (%defn.cnt;)*                                >
<!ATTLIST pd             
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Syntax Diagram                  -->
<!ELEMENT syntaxdiagram ((%title;)?,
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref; | %fragment; | %synblk; |
                          %synnote; | %synnoteref;)* )               >
<!ATTLIST syntaxdiagram   
             %display-atts;
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Syntax Block                    -->
<!ELEMENT synblk        ((%title;)?, 
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref; | %fragment;| %synnote; |
                          %synnoteref;)* )                           >
<!ATTLIST synblk
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Group Sequence                  -->
<!ELEMENT groupseq      ((%title;)?, (%repsep;)?,
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref; | %kwd; | %var; | %delim; | 
                          %oper; | %sep; | %synnote; | 
                          %synnoteref;)* )                           >
<!ATTLIST groupseq        
             importance (optional | required | default)    
                                                      #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Group Choice                    -->
<!ELEMENT groupchoice   ((%title;)?, (%repsep;)?,
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref;| %kwd; | %var; | %delim; |
                          %oper; | %sep; | %synnote; | 
                          %synnoteref;)* )                           > 
<!ATTLIST  groupchoice     
             importance (optional | required | default)    
                                                      #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Group Comparison                -->
<!ELEMENT groupcomp     ((%title;)?, (%repsep;)?,
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref; | %kwd; | %var; | %delim; |
                          %oper; | %sep; | %synnote; | 
                          %synnoteref;)* )                           > 
<!ATTLIST  groupcomp       
             importance (optional | required | default)    
                                                      #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Fragment                        -->
<!ELEMENT fragment      ((%title;)?, 
                         (%groupseq; | %groupchoice; | %groupcomp; |
                          %fragref; | %synnote; | %synnoteref;)* )   >
<!ATTLIST fragment        
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >
  


<!--                    LONG NAME: Fragment Reference              -->
<!ELEMENT fragref       (%xrefph.cnt;)*><!--xref-->
<!ATTLIST fragref         
             href       CDATA                            #IMPLIED
             importance (optional | required)            #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Syntax Diagram Note             -->
<!ELEMENT synnote       (#PCDATA | %basic.ph;)*                      >     
<!ATTLIST synnote        
             callout    CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Syntax Note Reference           -->
<!ELEMENT synnoteref     EMPTY >
<!ATTLIST synnoteref      
             href       CDATA                            #IMPLIED
             %univ-atts;                                  
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Repeat Separator                -->
<!ELEMENT repsep         (%words.cnt;)*                              >
<!ATTLIST repsep          
            importance  (optional | required)  #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!--                    LONG NAME: Keyword                         -->
<!ELEMENT kwd           (#PCDATA)                                    >
<!ATTLIST kwd             
             keyref     CDATA                            #IMPLIED
             importance (optional | required | default)  #IMPLIED
             %univ-atts-no-importance;                             
             outputclass 
                        CDATA                            #IMPLIED    >


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
             

<!ATTLIST  apiname    %global-atts; class CDATA "+ topic/keyword pr-d/apiname "  >
<!ATTLIST  codeblock  %global-atts; class CDATA "+ topic/pre pr-d/codeblock "    >
<!ATTLIST  codeph     %global-atts; class CDATA "+ topic/ph pr-d/codeph "        >
<!ATTLIST  delim      %global-atts; class CDATA "+ topic/ph pr-d/delim "         >
<!ATTLIST  fragment   %global-atts; class CDATA "+ topic/figgroup pr-d/fragment ">
<!ATTLIST  fragref    %global-atts; class CDATA "+ topic/xref pr-d/fragref "     >
<!ATTLIST  groupchoice 
                      %global-atts; class CDATA "+ topic/figgroup pr-d/groupchoice ">
<!ATTLIST  groupcomp  %global-atts; class CDATA "+ topic/figgroup pr-d/groupcomp ">
<!ATTLIST  groupseq   %global-atts; class CDATA "+ topic/figgroup pr-d/groupseq ">
<!ATTLIST  kwd        %global-atts; class CDATA "+ topic/keyword pr-d/kwd "      >
<!ATTLIST  oper       %global-atts; class CDATA "+ topic/ph pr-d/oper "          >
<!ATTLIST  option     %global-atts; class CDATA "+ topic/keyword pr-d/option "   >
<!ATTLIST  parml      %global-atts; class CDATA "+ topic/dl pr-d/parml "         >
<!ATTLIST  parmname   %global-atts; class CDATA "+ topic/keyword pr-d/parmname " >
<!ATTLIST  pd         %global-atts; class CDATA "+ topic/dd pr-d/pd "            >
<!ATTLIST  plentry    %global-atts; class CDATA "+ topic/dlentry pr-d/plentry "  >
<!ATTLIST  pt         %global-atts; class CDATA "+ topic/dt pr-d/pt "            >
<!ATTLIST  repsep     %global-atts; class CDATA "+ topic/ph pr-d/repsep "        >
<!ATTLIST  sep        %global-atts; class CDATA "+ topic/ph pr-d/sep "           >
<!ATTLIST  synblk     %global-atts; class CDATA "+ topic/figgroup pr-d/synblk "  >
<!ATTLIST  synnote    %global-atts; class CDATA "+ topic/fn pr-d/synnote "       >
<!ATTLIST  synnoteref %global-atts; class CDATA "+ topic/xref pr-d/synnoteref "  >
<!ATTLIST  synph      %global-atts; class CDATA "+ topic/ph pr-d/synph "         >
<!ATTLIST  syntaxdiagram 
                      %global-atts; class CDATA "+ topic/fig pr-d/syntaxdiagram ">
<!ATTLIST  var        %global-atts; class CDATA "+ topic/ph pr-d/var "           >


<!-- ================== End Programming Domain  ====================== -->