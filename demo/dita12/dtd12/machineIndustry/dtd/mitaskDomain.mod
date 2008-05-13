<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    Machine Industry Task Domain                      -->
<!--  VERSION:   1.2                                               -->
<!--  DATE:      September 2007                                    -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identfier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Machine Industry Task Domain//EN"
      Delivered as file "mitaskDomain.mod"                         -->

<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Define elements and specialization atttributed    -->
<!--             for the Machine Industry Task Domain              -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             September 2007                                    -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2007, 2008.              -->
<!--             All Rights Reserved.                              -->
<!--  UPDATES:                                                     -->
<!-- ============================================================= -->


<!-- ============================================================= -->
<!--                    ELEMENT NAME ENTITIES                      -->
<!-- ============================================================= -->


<!ENTITY % prelreqs    	 "prelreqs"                                  >
<!ENTITY % closereqs     "closereqs"                                 >
<!ENTITY % reqconds      "reqconds"                                  >
<!ENTITY % noconds       "noconds"                                   >
<!ENTITY % reqcond       "reqcond"                                   >
<!ENTITY % reqcontp      "reqcontp"                                  >
<!ENTITY % reqpers       "reqpers"                                   >
<!ENTITY % person        "person"                                    >
<!ENTITY % perscat       "perscat"                                   >
<!ENTITY % perskill      "perskill"                                  >
<!ENTITY % esttime       "esttime"                                   >
<!ENTITY % supequip      "supequip"                                  >
<!ENTITY % nosupeq       "nosupeq"                                   >
<!ENTITY % supeqli       "supeqli"                                   >
<!ENTITY % supequi       "supequi"                                   >
<!ENTITY % supplies      "supplies"                                  >
<!ENTITY % nosupply      "nosupply"                                  >
<!ENTITY % supplyli      "supplyli"                                  >
<!ENTITY % supply        "supply"                                    >
<!ENTITY % spares        "spares"                                    >
<!ENTITY % nospares      "nospares"                                  >
<!ENTITY % sparesli      "sparesli"                                  >
<!ENTITY % spare         "spare"                                     >
<!ENTITY % safety        "safety"                                    >
<!ENTITY % nosafety      "nosafety"                                  >
<!ENTITY % safecond      "safecond"                                  >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->
 
<!--                    LONG NAME: Preliminary Requirements        -->
<!ENTITY % prelreqs.content
                       "((%reqconds;)?, 
                         (%reqpers;)?, 
                         (%supequip;)?, 
                         (%supplies;)?,
                         (%spares;)?,
                         (%safety;)?)"
>
<!ENTITY % prelreqs.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT prelreqs    %prelreqs.content;>
<!ATTLIST prelreqs    %prelreqs.attributes;>


<!--                    LONG NAME: Closing Requirements        -->
<!ENTITY % closereqs.content
                       "(%reqconds;)"
>
<!ENTITY % closereqs.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT closereqs    %closereqs.content;>
<!ATTLIST closereqs    %closereqs.attributes;>

<!--                    LONG NAME: Required Conditions             -->
<!ENTITY % reqconds.content
                       "((%noconds;) |
						  ((%reqcond;) |
                           (%reqcontp;))+)"
>
<!ENTITY % reqconds.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT reqconds    %reqconds.content;>
<!ATTLIST reqconds    %reqconds.attributes;>

<!--                    LONG NAME: No Required Conditions            --><!-- specialized from li -->
<!ENTITY % noconds.content
                       "EMPTY"
>
<!ENTITY % noconds.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED    ">
<!ELEMENT noconds    %noconds.content;>
<!ATTLIST noconds    %noconds.attributes;>


<!--                    LONG NAME: Required Condition                 --><!-- specialized from li -->
<!ENTITY % reqcond.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % reqcond.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED    ">
<!ELEMENT reqcond    %reqcond.content;>
<!ATTLIST reqcond    %reqcond.attributes;>


<!--                    LONG NAME: Required Condition Technical Publication  --><!-- specialized from li -->
<!ENTITY % reqcontp.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % reqcontp.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT reqcontp    %reqcontp.content;>
<!ATTLIST reqcontp    %reqcontp.attributes;>
 
<!--                    LONG NAME: Required Persons                --><!-- specialized from ol -->
<!ENTITY % reqpers.content
                       "((%person;),
						 ((%perscat;)?,
                          (%perskill;)?,
                          (%esttime;)?)?)+
">
<!ENTITY % reqpers.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT reqpers    %reqpers.content;>
<!ATTLIST reqpers    %reqpers.attributes;>

 

<!--                    LONG NAME: Personnel                       --><!-- specialized from li -->
<!ENTITY % person.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % person.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT person    %person.content;>
<!ATTLIST person    %person.attributes;>


<!--                    LONG NAME: Personnel Category               --><!-- specialized from li -->
<!ENTITY % perscat.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % perscat.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT perscat    %perscat.content;>
<!ATTLIST perscat    %perscat.attributes;>


<!--                    LONG NAME: Personnel Skill Level             --><!-- specialized from li -->
<!ENTITY % perskill.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % perskill.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT perskill    %perskill.content;>
<!ATTLIST perskill    %perskill.attributes;>


<!--                    LONG NAME: Esttime                      --><!-- specialized from li -->
<!ENTITY % esttime.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % esttime.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT esttime    %esttime.content;>
<!ATTLIST esttime    %esttime.attributes;>



<!--                    LONG NAME: Support Equipment             --><!-- specialized from p -->
<!ENTITY % supequip.content
                       "((%nosupeq;)|
                         (%supeqli;))"
>
<!ENTITY % supequip.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT supequip    %supequip.content;>
<!ATTLIST supequip    %supequip.attributes;>

<!--                    LONG NAME: No Support Equipment            --><!-- specialized from data -->
<!ENTITY % nosupeq.content
                       "EMPTY"
>
<!ENTITY % nosupeq.attributes
             "%data-element-atts;"
>
<!ELEMENT nosupeq    %nosupeq.content;>
<!ATTLIST nosupeq    %nosupeq.attributes;>

<!--                    LONG NAME: Support Equipment List          --><!-- specialized from ul -->
<!ENTITY % supeqli.content
                       "(%supequi;)+"
>
<!ENTITY % supeqli.attributes
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
<!ELEMENT supeqli    %supeqli.content;>
<!ATTLIST supeqli    %supeqli.attributes;>


<!--                    LONG NAME: Support Equipment Item          --><!-- specialized from li -->
<!ENTITY % supequi.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % supequi.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT supequi    %supequi.content;>
<!ATTLIST supequi    %supequi.attributes;>



 <!--                    LONG NAME: Supplies             --><!-- specialized from p -->
<!ENTITY % supplies.content
                       "((%nosupply;)? |
                         (%supplyli;)?)"
>
<!ENTITY % supplies.attributes
             "%univ-atts; 
               outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT supplies    %supplies.content;>
<!ATTLIST supplies    %supplies.attributes;>


<!--                    LONG NAME: No Supplies            --><!-- specialized from data -->
<!ENTITY % nosupply.content
                       "EMPTY"
>
<!ENTITY % nosupply.attributes
             "%data-element-atts;"
>
<!ELEMENT nosupply    %nosupply.content;>
<!ATTLIST nosupply    %nosupply.attributes;>


<!--                    LONG NAME: Supply List          --><!-- specialized from ul -->
<!ENTITY % supplyli.content
                       "(%supply;)+"
>
<!ENTITY % supplyli.attributes
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
<!ELEMENT supplyli    %supplyli.content;>
<!ATTLIST supplyli    %supplyli.attributes;>


<!--                    LONG NAME: Supply Item          --><!-- specialized from li -->
<!ENTITY % supply.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % supply.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT supply    %supply.content;>
<!ATTLIST supply    %supply.attributes;>


 <!--                    LONG NAME: Spares             --><!-- specialized from p -->
<!ENTITY % spares.content
                       "((%nospares;)? |
                        (%sparesli;)?)"
>
<!ENTITY % spares.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT spares    %spares.content;>
<!ATTLIST spares    %spares.attributes;>


<!--                    LONG NAME: No Spares            --><!-- specialized from data -->
<!ENTITY % nospares.content
                       "EMPTY"
>
<!ENTITY % nospares.attributes
             "%data-element-atts;"
>
<!ELEMENT nospares    %nospares.content;>
<!ATTLIST nospares    %nospares.attributes;>


<!--                    LONG NAME: Spare List          --><!-- specialized from ul -->
<!ENTITY % sparesli.content
                       "(%spare;)+"
>
<!ENTITY % sparesli.attributes
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
<!ELEMENT sparesli    %sparesli.content;>
<!ATTLIST sparesli    %sparesli.attributes;>


<!--                    LONG NAME: Spare Item          --><!-- specialized from li -->
<!ENTITY % spare.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % spare.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT spare    %spare.content;>
<!ATTLIST spare    %spare.attributes;>


<!--                    LONG NAME: Safety Conditions             --><!-- specialized from ol -->
<!ENTITY % safety.content
                       "((%nosafety;)? |
                         (%safecond;)?)">
<!ENTITY % safety.attributes
             "%univ-atts; 
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT safety    %safety.content;>
<!ATTLIST safety    %safety.attributes;>


<!--                    LONG NAME: No Safety Conditions            --><!-- specialized from li -->
<!ENTITY % nosafety.content
                       "EMPTY"
>
<!ENTITY % nosafety.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT nosafety    %nosafety.content;>
<!ATTLIST nosafety    %nosafety.attributes;>

<!--                    LONG NAME: Safety Condition                 --><!-- specialized from li -->
<!ENTITY % safecond.content
                       "(%listitem.cnt;)*"
>
<!ENTITY % safecond.attributes
             "%univ-atts;
              outputclass 
                        CDATA
                                  #IMPLIED"
>
<!ELEMENT safecond    %safecond.content;>
<!ATTLIST safecond    %safecond.attributes;>


<!-- ============================================================= -->
<!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
<!-- ============================================================= -->


<!ATTLIST prelreqs    %global-atts;  class  CDATA "+ topic/section mitask-d/prelreqs "  >
<!ATTLIST closereqs   %global-atts;  class  CDATA "+ topic/example mitask-d/closereqs " >
<!ATTLIST reqconds    %global-atts;  class  CDATA "+ topic/ol mitask-d/reqconds "       >
<!ATTLIST noconds     %global-atts;  class  CDATA "+ topic/li mitask-d/noconds "        >
<!ATTLIST reqcond     %global-atts;  class  CDATA "+ topic/li mitask-d/reqcond "        >
<!ATTLIST reqcontp    %global-atts;  class  CDATA "+ topic/li mitask-d/reqcontp "       >
<!ATTLIST reqpers     %global-atts;  class  CDATA "+ topic/ol mitask-d/reqpers "        >
<!ATTLIST person      %global-atts;  class  CDATA "+ topic/li mitask-d/person "         >
<!ATTLIST perscat     %global-atts;  class  CDATA "+ topic/li mitask-d/perscat "        >
<!ATTLIST perskill    %global-atts;  class  CDATA "+ topic/li mitask-d/perskill "       >
<!ATTLIST esttime     %global-atts;  class  CDATA "+ topic/li mitask-d/esttime "        >
<!ATTLIST supequip    %global-atts;  class  CDATA "+ topic/p mitask-d/supequip "        >
<!ATTLIST nosupeq     %global-atts;  class  CDATA "+ topic/data mitask-d/nosupeq "      >
<!ATTLIST supeqli     %global-atts;  class  CDATA "+ topic/ul mitask-d/supeqli "        >
<!ATTLIST supequi     %global-atts;  class  CDATA "+ topic/li mitask-d/supequi "        >
<!ATTLIST supplies    %global-atts;  class  CDATA "+ topic/p mitask-d/supplies "        >
<!ATTLIST nosupply    %global-atts;  class  CDATA "+ topic/data mitask-d/nosupply "     >
<!ATTLIST supplyli    %global-atts;  class  CDATA "+ topic/ul mitask-d/supplyli "       >
<!ATTLIST supply      %global-atts;  class  CDATA "+ topic/li mitask-d/supply "         >
<!ATTLIST spares      %global-atts;  class  CDATA "+ topic/p mitask-d/spares "          >
<!ATTLIST nospares    %global-atts;  class  CDATA "+ topic/data mitask-d/nospares "     >
<!ATTLIST sparesli    %global-atts;  class  CDATA "+ topic/ul mitask-d/sparesli "       >
<!ATTLIST spare       %global-atts;  class  CDATA "+ topic/li mitask-d/spare "          >
<!ATTLIST safety      %global-atts;  class  CDATA "+ topic/ol mitask-d/safety "         >
<!ATTLIST nosafety    %global-atts;  class  CDATA "+ topic/li mitask-d/nosafety "       >
<!ATTLIST safecond    %global-atts;  class  CDATA "+ topic/li mitask-d/safecond "       >

<!-- ================== End DITA Machine Industry Task Domain  =================== -->