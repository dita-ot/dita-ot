<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % subjectDetail     "subjectDetail">
<!ENTITY % subjectDefinition "subjectDefinition">
<!ENTITY % subjectLabels     "subjectLabels">
<!ENTITY % prefLabel         "prefLabel">
<!ENTITY % altLabel          "altLabel">
<!ENTITY % hiddenLabel       "hiddenLabel">
<!ENTITY % prefSymbol        "prefSymbol">
<!ENTITY % altSymbol         "altSymbol">
<!ENTITY % scopeNote         "scopeNote">
<!ENTITY % historyNote       "historyNote">
<!ENTITY % editorialNote     "editorialNote">
<!ENTITY % changeNote        "changeNote">
<!ENTITY % subjectNote       "subjectNote">
<!ENTITY % fullnoteref       "fullnoteref">

<!-- ============ Element definitions ============ -->
<!ELEMENT subjectDetail     ((%subjectLabels;)?, (%subjectDefinition;)?, (%scopeNote;)?, (%historyNote;)?, (%editorialNote;)?, (%changeNote;)?, (%subjectNote;)*)>
<!ATTLIST subjectDetail      spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT subjectLabels     ((%prefLabel;)?, (%altLabel;)*, (%hiddenLabel;)*, (%prefSymbol;)?, (%altSymbol;)*)>
<!ATTLIST subjectLabels      %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!-- If not supplied, the topic title is used -->
<!ELEMENT prefLabel      (#PCDATA | %tm;)*>
<!ATTLIST prefLabel       keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT altLabel       (#PCDATA | %tm;)*>
<!ATTLIST altLabel        keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT hiddenLabel    (#PCDATA | %tm;)*>
<!ATTLIST hiddenLabel     keyref CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT prefSymbol     (%alt;)?>
<!ATTLIST prefSymbol      href       CDATA           #REQUIRED
                          keyref     CDATA           #IMPLIED
                          alt        CDATA           #IMPLIED
                          longdescref CDATA          #IMPLIED
                          height     NMTOKEN         #IMPLIED
                          width      NMTOKEN         #IMPLIED
                          align      CDATA           #IMPLIED
                          placement  (inline|break)  "inline"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT altSymbol      (%alt;)?>
<!ATTLIST altSymbol       href       CDATA           #REQUIRED
                          keyref     CDATA           #IMPLIED
                          alt        CDATA           #IMPLIED
                          longdescref CDATA          #IMPLIED
                          height     NMTOKEN         #IMPLIED
                          width      NMTOKEN         #IMPLIED
                          align      CDATA           #IMPLIED
                          placement  (inline|break)  "inline"
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!-- If not supplied, the topic short description is used -->
<!ELEMENT subjectDefinition  (%title.cnt;)*>
<!ATTLIST subjectDefinition  %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT scopeNote      (#PCDATA|%basic.ph.noxref;|%fullnoteref;)* >
<!ATTLIST scopeNote       type (other) 'other'
                          spectitle CDATA #IMPLIED
                          othertype CDATA  'scope'
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT historyNote    (#PCDATA|%basic.ph.noxref;|%fullnoteref;)* >
<!ATTLIST historyNote     type (other) 'other'
                          spectitle CDATA #IMPLIED
                          othertype CDATA  'history'
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT editorialNote  (#PCDATA|%basic.ph.noxref;|%fullnoteref;)* >
<!ATTLIST editorialNote   type (other) 'other'
                          spectitle CDATA #IMPLIED
                          othertype CDATA  'editorial'
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT changeNote     (#PCDATA|%basic.ph.noxref;|%fullnoteref;)* >
<!ATTLIST changeNote      type (other) 'other'
                          spectitle CDATA #IMPLIED
                          othertype CDATA  'change'
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT subjectNote     (#PCDATA|%basic.ph.noxref;|%fullnoteref;)* >
<!ATTLIST subjectNote      type (other) 'other'
                          spectitle CDATA #IMPLIED
                          othertype CDATA  'public'
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT fullnoteref    (#PCDATA)*>
<!ATTLIST fullnoteref     href        CDATA #IMPLIED
                          keyref      CDATA #IMPLIED
                          %univ-atts;
                          format      CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
>

<!-- ============ Element specialization declarations ============ -->
<!ATTLIST subjectDetail %global-atts;
    class CDATA "+ topic/section subject-d/subjectDetail ">
<!ATTLIST subjectLabels %global-atts;
    class CDATA "+ topic/p subject-d/subjectLabels ">
<!ATTLIST prefLabel %global-atts;
    class CDATA "+ topic/term subject-d/prefLabel ">
<!ATTLIST altLabel %global-atts;
    class CDATA "+ topic/term subject-d/altLabel ">
<!ATTLIST hiddenLabel %global-atts;
    class CDATA "+ topic/term subject-d/hiddenLabel ">
<!ATTLIST prefSymbol %global-atts;
    class CDATA "+ topic/image subject-d/prefSymbol ">
<!ATTLIST altSymbol %global-atts;
    class CDATA "+ topic/image subject-d/altSymbol ">
<!ATTLIST subjectDefinition %global-atts;
    class CDATA "+ topic/p subject-d/subjectDefinition ">
<!ATTLIST scopeNote %global-atts;
    class CDATA "+ topic/note subject-d/scopeNote ">
<!ATTLIST historyNote %global-atts;
    class CDATA "+ topic/note subject-d/historyNote ">
<!ATTLIST editorialNote %global-atts;
    class CDATA "+ topic/note subject-d/editorialNote ">
<!ATTLIST changeNote %global-atts;
    class CDATA "+ topic/note subject-d/changeNote ">
<!ATTLIST subjectNote %global-atts;
    class CDATA "+ topic/note subject-d/subjectNote ">
<!ATTLIST fullnoteref %global-atts;
    class CDATA "+ topic/xref subject-d/fullnoteref ">
