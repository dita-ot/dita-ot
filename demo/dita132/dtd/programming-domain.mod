<!--
 |  (C) Copyright IBM Corporation 2001, 2005. All Rights Reserved.
 |
 | The Darwin Information Typing Architecture (DITA) was orginated by
 | IBM's XML Workgroup and ID Workbench tools team.
 |
 | Refer to this file by the following public identfier or an appropriate
 | system identifier:
 |
 |   PUBLIC "-//IBM//ELEMENTS DITA Programming Domain//EN"
 |
 | Release history (vrm):
 |   1.0.0 Initial release on developerWorks, March 2001 (dita00.zip)
 |   1.0.1 fix 1 on developerWorks, October 2001 (dita01.zip)
 |   1.0.2 consolidated redesign December 2001
 |   1.0.3 fix 1, dtd freeze for UCD-1 January 2002
 |   1.1.0 Release 1 March 2002
 |   1.1.1 Release 1.1 December 2002
 |   1.1.2 Release 1.2 June 2003
 |   1.1.3 Release 1.3 March 2004: bug fixes and map updates
 |   1.1.3a bug fixes: converted words.cnt back to PCDATA for option, parmname, apiname, kwd
 |                     (elements derived from keyword must have content models in kind)
 *-->

<!ENTITY % codeph "codeph">
<!ENTITY % codeblock "codeblock">
<!ENTITY % option "option">
<!ENTITY % kwd "kwd">
<!ENTITY % var "var">
<!ENTITY % parmname "parmname">
<!ENTITY % synph "synph">
<!ENTITY % oper "oper">
<!ENTITY % delim "delim">
<!ENTITY % sep "sep">
<!ENTITY % apiname "apiname">

<!ENTITY % parml "parml">
<!ENTITY % plentry "plentry">
<!ENTITY % pt "pt">
<!ENTITY % pd "pd">

<!ENTITY % syntaxdiagram "syntaxdiagram">
<!ENTITY % synblk "synblk">
<!ENTITY % groupseq "groupseq">
<!ENTITY % groupchoice "groupchoice">
<!ENTITY % groupcomp "groupcomp">
<!ENTITY % fragment "fragment">
<!ENTITY % fragref "fragref">
<!ENTITY % synnote "synnote">
<!ENTITY % synnoteref "synnoteref">
<!ENTITY % repsep "repsep">


<!-- provide an alternative set of univ-atts that allows importance to be redefined locally-->
<!ENTITY % univ-atts-no-importance
                         '%id-atts;
                          platform CDATA #IMPLIED
                          product CDATA #IMPLIED
                          audience CDATA #IMPLIED
                          otherprops CDATA #IMPLIED
                          rev CDATA #IMPLIED
                          status (new|changed|deleted|unchanged) #IMPLIED
                          translate (yes|no) #IMPLIED
                          xml:lang NMTOKEN #IMPLIED'
>



<!ELEMENT codeph         (#PCDATA | %basic.ph.notm;)* >
<!ATTLIST codeph          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT codeblock      (#PCDATA | %basic.ph.notm; | %txt.incl;)* >
<!ATTLIST codeblock       %display-atts;
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:space (preserve) #FIXED 'preserve'
>
<!ELEMENT option         (#PCDATA)>
<!ATTLIST option          keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT var            (%words.cnt;)*>
<!ATTLIST var             importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT parmname       (#PCDATA)>
<!ATTLIST parmname        keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT synph          (#PCDATA | %codeph; | %option; | %parmname; |
                          %var; | %kwd; | %oper; | %delim; | %sep; | %synph;)* >
<!-- userinput | cmdname |-->
<!ATTLIST synph           %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT oper           (%words.cnt;)*>
<!ATTLIST oper            importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT delim          (%words.cnt;)*>
<!ATTLIST delim           importance           (optional | required)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT sep            (%words.cnt;)*>
<!ATTLIST sep             importance           (optional | required)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT apiname        (#PCDATA)>
<!ATTLIST apiname         keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- parameter list -->
<!ELEMENT parml          (%plentry;)+ >
<!ATTLIST parml           compact (yes|no) #IMPLIED
                          %univ-atts;
                          spectitle CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>
<!ELEMENT plentry        ((%pt;)+,(%pd;)+) >
<!ATTLIST plentry         %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT pt             (%term.cnt;)*> <!--#PCDATA | %basic.ph; | %image;-->
<!ATTLIST pt              keyref NMTOKEN #IMPLIED

                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT pd             (%defn.cnt;)* >        <!-- description -->
<!ATTLIST pd              %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- Base form: Syntax Diagram -->
<!ELEMENT syntaxdiagram  ((%title;)?,(%groupseq;|%groupchoice;|%groupcomp;|%fragref;|%fragment;|%synblk;|%synnote;|%synnoteref;)*)><!--fig-->
<!ATTLIST syntaxdiagram   %display-atts;
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT synblk         ((%title;)?,(%groupseq;|%groupchoice;|%groupcomp;|%fragref;|%fragment;|
                          %synnote;|%synnoteref;)*)><!--figgroup-->
<!ATTLIST synblk          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT groupseq       ((%title;)?,(%repsep;)?,(%groupseq;|%groupchoice;|%groupcomp;|%fragref;|
                          %kwd;|%var;|%delim;|%oper;|%sep;|%synnote;|%synnoteref;)*)><!--figgroup-->
<!ATTLIST groupseq        importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT groupchoice    ((%title;)?,(%repsep;)?,(%groupseq;|%groupchoice;|%groupcomp;|%fragref;|
                          %kwd;|%var;|%delim;|%oper;|%sep;|%synnote;|%synnoteref;)*)><!--figgroup-->
<!ATTLIST groupchoice     importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT groupcomp      ((%title;)?,(%repsep;)?,(%groupseq;|%groupchoice;|%groupcomp;|%fragref;|
                          %kwd;|%var;|%delim;|%oper;|%sep;|%synnote;|%synnoteref;)*)><!--figgroup-->
<!ATTLIST groupcomp       importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT fragment       ((%title;)?, (%groupseq;|%groupchoice;|%groupcomp;|%fragref;|%synnote;|%synnoteref;)*) ><!--figgroup-->
<!ATTLIST fragment        %univ-atts;
                          outputclass CDATA #IMPLIED

>

<!ELEMENT fragref        (%xrefph.cnt;)*><!--xref-->
<!ATTLIST fragref         href CDATA #IMPLIED
                          importance           (optional | required)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT synnote        (#PCDATA|%basic.ph;)* ><!--fn-->
<!ATTLIST synnote         callout CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED

>

<!ELEMENT synnoteref     EMPTY ><!--xref-->
<!ATTLIST synnoteref      href CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED

>

<!ELEMENT repsep         (%words.cnt;)*><!--ph-->
<!ATTLIST repsep          importance           (optional | required)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT kwd            (#PCDATA)>
<!ATTLIST kwd             keyref NMTOKEN #IMPLIED
                          importance           (optional | required | default)  #IMPLIED
                          %univ-atts-no-importance;
                          outputclass CDATA #IMPLIED
>



<!ATTLIST codeph         %global-atts; class CDATA "+ topic/ph pr-d/codeph ">
<!ATTLIST codeblock      %global-atts; class CDATA "+ topic/pre pr-d/codeblock ">
<!ATTLIST option         %global-atts; class CDATA "+ topic/keyword pr-d/option ">
<!ATTLIST var            %global-atts; class CDATA "+ topic/ph pr-d/var ">
<!ATTLIST parmname       %global-atts; class CDATA "+ topic/keyword pr-d/parmname ">
<!ATTLIST synph          %global-atts; class CDATA "+ topic/ph pr-d/synph ">
<!ATTLIST oper           %global-atts; class CDATA "+ topic/ph pr-d/oper ">
<!ATTLIST delim          %global-atts; class CDATA "+ topic/ph pr-d/delim ">
<!ATTLIST sep            %global-atts; class CDATA "+ topic/ph pr-d/sep ">
<!ATTLIST apiname        %global-atts; class CDATA "+ topic/keyword pr-d/apiname ">

<!ATTLIST parml          %global-atts; class CDATA "+ topic/dl pr-d/parml ">
<!ATTLIST plentry        %global-atts; class CDATA "+ topic/dlentry pr-d/plentry ">
<!ATTLIST pt             %global-atts; class CDATA "+ topic/dt pr-d/pt ">
<!ATTLIST pd             %global-atts; class CDATA "+ topic/dd pr-d/pd ">

<!ATTLIST syntaxdiagram  %global-atts; class CDATA "+ topic/fig pr-d/syntaxdiagram ">
<!ATTLIST synblk         %global-atts; class CDATA "+ topic/figgroup pr-d/synblk ">
<!ATTLIST groupseq       %global-atts; class CDATA "+ topic/figgroup pr-d/groupseq ">
<!ATTLIST groupchoice    %global-atts; class CDATA "+ topic/figgroup pr-d/groupchoice ">
<!ATTLIST groupcomp      %global-atts; class CDATA "+ topic/figgroup pr-d/groupcomp ">
<!ATTLIST fragment       %global-atts; class CDATA "+ topic/figgroup pr-d/fragment ">
<!ATTLIST fragref        %global-atts; class CDATA "+ topic/xref pr-d/fragref ">
<!ATTLIST synnote        %global-atts; class CDATA "+ topic/fn pr-d/synnote ">
<!ATTLIST synnoteref     %global-atts; class CDATA "+ topic/xref pr-d/synnoteref ">
<!ATTLIST repsep         %global-atts; class CDATA "+ topic/ph pr-d/repsep ">
<!ATTLIST kwd            %global-atts; class CDATA "+ topic/keyword pr-d/kwd ">
