<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA DITA Programming Domain                      -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      November 2009                                     -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Programming Domain//EN"
      Delivered as file "programmingDomain.mod"                    -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the elements and specialization         -->
<!--             attributes for the Programming Domain             -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             March 2001                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2005, 2009.              -->
<!--             (C) Copyright IBM Corporation 2001, 2004.         -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!--    2005.11.15 RDA: Updated these comments to match template   -->
<!--    2005.11.15 RDA: Corrected Long Names for syntax groups,    -->
<!--                    codeph, and kwd                            -->
<!--    2005.11.15 RDA: Corrected the "Delivered as" system ID     -->
<!--    2006.06.07 RDA: Make universal attributes universal        -->
<!--                      (DITA 1.1 proposal #12)                  -->
<!--    2006.11.30 RDA: Add -dita-use-conref-target to enumerated  -->
<!--                      attributes                               -->
<!--    2007.12.01 EK:  Reformatted DTD modules for DITA 1.2       -->
<!--    2008.02.12 RDA: Add text to synph, items with only #PCDATA -->
<!--    2008.02.12 RDA: Add coderef element                        -->
<!--    2008.02.13 RDA: Create .content and .attributes entities   -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->


<!ENTITY % apiname      "apiname"                                    >
<!ENTITY % codeblock    "codeblock"                                  >
<!ENTITY % codeph       "codeph"                                     >
<!ENTITY % coderef      "coderef"                                     >
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
<!--                    Provide an alternative set of univ-atts that 
                        allows importance to be redefined locally  -->
<!ENTITY % univ-atts-no-importance
             "base 
                        CDATA 
                                  #IMPLIED
              %base-attribute-extensions;
              %id-atts;
              %filter-atts;
              %localization-atts; 
              rev 
                        CDATA 
                                  #IMPLIED
               status 
                        (new | 
                         changed | 
                         deleted |
                         unchanged | 
                         -dita-use-conref-target) 
                                  #IMPLIED
  " 
> 


<!--                    LONG NAME: Code Phrase                     -->
<!ENTITY % codeph.content
                       "(#PCDATA | 
                         %basic.ph.notm; | 
                         %data.elements.incl; | 
                         %foreign.unknown.incl;)*"
>
<!ENTITY % codeph.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT codeph    %codeph.content;>
<!ATTLIST codeph    %codeph.attributes;>



<!--                    LONG NAME: Code Block                      -->
<!ENTITY % codeblock.content
                       "(#PCDATA | 
                         %basic.ph.notm;  |
                         %coderef; |
                         %data.elements.incl; | 
                         %foreign.unknown.incl;| 
                         %txt.incl;)* 
 ">
<!ENTITY % codeblock.attributes
             "%display-atts;
              spectitle 
                        CDATA 
                                  #IMPLIED
              xml:space 
                        (preserve) 
                                  #FIXED 'preserve'
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT codeblock    %codeblock.content;>
<!ATTLIST codeblock    %codeblock.attributes;>


<!--                    LONG NAME: Literal code reference          -->
<!ENTITY % coderef.content
                       "EMPTY"
>
<!ENTITY % coderef.attributes
             "href 
                        CDATA 
                                  #IMPLIED
              keyref 
                        CDATA 
                                  #IMPLIED
              type 
                        CDATA 
                                  #IMPLIED
              format 
                        CDATA 
                                  #IMPLIED
              scope 
                        (external | 
                         local | 
                         peer | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT coderef    %coderef.content;>
<!ATTLIST coderef    %coderef.attributes;>



<!--                    LONG NAME: Option                          -->
<!ENTITY % option.content
                       "(#PCDATA |
                         %text;)*
">
<!ENTITY % option.attributes
             "keyref
                        CDATA
                                  #IMPLIED
              %univ-atts;
              outputclass
                        CDATA
                                  #IMPLIED
">
<!ELEMENT option    %option.content;>
<!ATTLIST option    %option.attributes;>



<!--                    LONG NAME: Variable                        -->
<!ENTITY % var.content
                       "(%words.cnt;)*"
>
<!ENTITY % var.attributes
             "importance 
                        (default | 
                         optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
              CDATA
                                  #IMPLIED"
>
<!ELEMENT var    %var.content;>
<!ATTLIST var    %var.attributes;>



<!--                    LONG NAME: Parameter Name                  -->
<!ENTITY % parmname.content
                       "(#PCDATA |
                         %text;)*
">
<!ENTITY % parmname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT parmname    %parmname.content;>
<!ATTLIST parmname    %parmname.attributes;>



<!--                    LONG NAME: Syntax Phrase                   -->
<!ENTITY % synph.content
                       "(#PCDATA | 
                         %codeph; | 
                         %delim; |
                         %kwd; | 
                         %oper; | 
                         %option; | 
                         %parmname; |
                         %sep; | 
                         %synph; |
                         %text; | 
                         %var; 
                         )*
">
<!ENTITY % synph.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT synph    %synph.content;> 
<!ATTLIST  synph   %synph.attributes;>

<!--                    LONG NAME: Operator                        -->
<!ENTITY % oper.content
                       "(%words.cnt;)*"
>
<!ENTITY % oper.attributes
             "importance 
                        (default | 
                         optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT oper    %oper.content;>
<!ATTLIST oper    %oper.attributes;>



<!--                    LONG NAME: Delimiter                       -->
<!ENTITY % delim.content
                       "(%words.cnt;)*"
>
<!ENTITY % delim.attributes
             "importance 
                        (optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance;
              outputclass
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT delim    %delim.content;>
<!ATTLIST delim    %delim.attributes;>



<!--                    LONG NAME: Separator                       -->
<!ENTITY % sep.content
                       "(%words.cnt;)*"
>
<!ENTITY % sep.attributes
             "importance 
                        (optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT sep    %sep.content;>
<!ATTLIST sep    %sep.attributes;>



<!--                    LONG NAME: API Name                        -->
<!ENTITY % apiname.content
                       "(#PCDATA |
                         %text;)*
">
<!ENTITY % apiname.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT apiname    %apiname.content;>
<!ATTLIST apiname    %apiname.attributes;>



<!--                    LONG NAME: Parameter List                  -->
<!ENTITY % parml.content
                       "(%plentry;)+"
>
<!ENTITY % parml.attributes
             "compact 
                        (yes | 
                         no |
                         -dita-use-conref-target) 
                                  #IMPLIED
              spectitle 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT parml    %parml.content;>
<!ATTLIST parml    %parml.attributes;>



<!--                    LONG NAME: Parameter List Entry            -->
<!ENTITY % plentry.content
                       "((%pt;)+, 
                         (%pd;)+)"
>
<!ENTITY % plentry.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT plentry    %plentry.content;>
<!ATTLIST plentry    %plentry.attributes;>



<!--                    LONG NAME: Parameter Term                  -->
<!ENTITY % pt.content
                       "(%term.cnt;)*"
>
<!ENTITY % pt.attributes
             "keyref 
                        CDATA 
                                  #IMPLIED
              %univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT pt    %pt.content;>
<!ATTLIST pt    %pt.attributes;>



<!--                    LONG NAME: Parameter Description           -->
<!ENTITY % pd.content
                       "(%defn.cnt;)*"
>
<!ENTITY % pd.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT pd    %pd.content;>
<!ATTLIST pd    %pd.attributes;>



<!--                    LONG NAME: Syntax Diagram                  -->
<!ENTITY % syntaxdiagram.content
                       "((%title;)?,
                         (%fragment; | 
                          %fragref; | 
                          %groupchoice; | 
                          %groupcomp; |
                          %groupseq; | 
                          %synblk; |
                          %synnote; | 
                          %synnoteref;)* )"
>
<!ENTITY % syntaxdiagram.attributes
             "%display-atts;
              %univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT syntaxdiagram    %syntaxdiagram.content;>
<!ATTLIST syntaxdiagram    %syntaxdiagram.attributes;>


<!--                    LONG NAME: Syntax Block                    -->
<!ENTITY % synblk.content
                       "((%title;)?, 
                        (%fragment; | 
                         %fragref; | 
                         %groupchoice; | 
                         %groupcomp; |
                         %groupseq; | 
                         %synnote; |
                         %synnoteref;)* )"
>
<!ENTITY % synblk.attributes
             "%univ-atts; 
              outputclass 
                        CDATA 
                                  #IMPLIED"
>
<!ELEMENT synblk    %synblk.content;>
<!ATTLIST synblk    %synblk.attributes;>



<!--                    LONG NAME: Sequence Group                  -->
<!ENTITY % groupseq.content
                       "((%title;)?, 
                         (%repsep;)?,
                         (%delim; | 
                          %fragref; | 
                          %groupchoice; | 
                          %groupcomp; |
                          %groupseq; | 
                          %kwd; | 
                          %oper; | 
                          %sep; | 
                          %synnote; | 
                          %synnoteref; | 
                          %var;)* )"
>
<!ENTITY % groupseq.attributes
             "importance 
                        (default |
                         required | 
                         optional | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT groupseq    %groupseq.content;>
<!ATTLIST groupseq    %groupseq.attributes;>

<!--                    LONG NAME: Choice Group                    -->
<!ENTITY % groupchoice.content
                       "((%title;)?, 
                         (%repsep;)?,
                         (%delim; |
                          %fragref; | 
                          %groupchoice; | 
                          %groupcomp; |
                          %groupseq; | 
                          %kwd; | 
                          %oper; | 
                          %sep; | 
                          %synnote; | 
                          %synnoteref; | 
                          %var;)* )"
>
<!ENTITY % groupchoice.attributes
             "importance 
                        (default |
                         required | 
                         optional | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT groupchoice    %groupchoice.content;> 
<!ATTLIST groupchoice    %groupchoice.attributes;>

<!--                    LONG NAME: Composite group                 -->
<!ENTITY % groupcomp.content
                       "((%title;)?, 
                         (%repsep;)?,
                         (%delim; |
                          %fragref; | 
                          %groupchoice; | 
                          %groupcomp; |
                          %groupseq; | 
                          %kwd; | 
                          %oper; | 
                          %sep; | 
                          %synnote; | 
                          %synnoteref; | 
                          %var;)* )"
>
<!ENTITY % groupcomp.attributes
             "importance 
                        (default |
                         required | 
                         optional | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT groupcomp    %groupcomp.content;> 
<!ATTLIST groupcomp    %groupcomp.attributes;>

<!--                    LONG NAME: Fragment                        -->
<!ENTITY % fragment.content
                       "((%title;)?, 
                         (%fragref; | 
                          %groupchoice; | 
                          %groupcomp; |
                          %groupseq; | 
                          %synnote; | 
                          %synnoteref;)* )"
>
<!ENTITY % fragment.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT fragment    %fragment.content;>
<!ATTLIST fragment    %fragment.attributes;>

 


<!--                    LONG NAME: Fragment Reference              -->
<!ENTITY % fragref.content
                       "(%xrefph.cnt;)*
">
<!ENTITY % fragref.attributes
             "href 
                        CDATA 
                                  #IMPLIED
              importance 
                        (optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT fragref    %fragref.content;>
<!ATTLIST fragref    %fragref.attributes;>


<!--                    LONG NAME: Syntax Diagram Note             -->
<!ENTITY % synnote.content
                       "(#PCDATA | 
                         %basic.ph;)*"
>
<!ENTITY % synnote.attributes
             "callout 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT synnote    %synnote.content;>
<!ATTLIST synnote    %synnote.attributes;>



<!--                    LONG NAME: Syntax Note Reference           -->
<!ENTITY % synnoteref.content
                       "EMPTY"
>
<!ENTITY % synnoteref.attributes
             "href 
                        CDATA 
                                  #IMPLIED
              %univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT synnoteref    %synnoteref.content;>
<!ATTLIST synnoteref    %synnoteref.attributes;>



<!--                    LONG NAME: Repeat Separator                -->
<!ENTITY % repsep.content
                       "(%words.cnt;)*"
>
<!ENTITY % repsep.attributes
             "importance 
                        (optional | 
                         required | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT repsep    %repsep.content;>
<!ATTLIST repsep    %repsep.attributes;>



<!--                    LONG NAME: Syntax Keyword                  -->
<!ENTITY % kwd.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % kwd.attributes
             "keyref 
                         CDATA 
                                   #IMPLIED
               importance 
                        (default |
                         required | 
                         optional | 
                         -dita-use-conref-target) 
                                  #IMPLIED
              %univ-atts-no-importance; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT kwd    %kwd.content;>
<!ATTLIST kwd    %kwd.attributes;>



<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->
 

<!ATTLIST  apiname    %global-atts; class CDATA "+ topic/keyword pr-d/apiname "  >
<!ATTLIST  codeblock  %global-atts; class CDATA "+ topic/pre pr-d/codeblock "    >
<!ATTLIST  codeph     %global-atts; class CDATA "+ topic/ph pr-d/codeph "        >
<!ATTLIST  coderef    %global-atts; class CDATA "+ topic/xref pr-d/coderef "     >
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