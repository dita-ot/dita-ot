<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- ============ Hooks for domain extension ============ -->
<!ENTITY % subject "subject">
<!ENTITY % notedetail "notedetail">
<!ENTITY % noteheader "noteheader">
<!ENTITY % From "From">
<!ENTITY % To "To">
<!ENTITY % Cc "Cc">
<!ENTITY % Bcc "Bcc">
<!ENTITY % recipient "recipient">
<!ENTITY % Date "Date">
<!ENTITY % delivery "delivery">
<!ENTITY % Priority "Priority">
<!ENTITY % Importance "Importance">
<!ENTITY % ReturnReceipt "ReturnReceipt">
<!ENTITY % Encrypt "Encrypt">
<!ENTITY % attachments "attachments">
<!ENTITY % notebody "notebody">
<!ENTITY % references "references">
<!ENTITY % InReplyTo "InReplyTo">
<!ENTITY % Reference "Reference">

<!-- ============ Hooks for shell DTD ============ -->
<!ENTITY % enote-info-types "%info-types;">
<!ENTITY included-domains "">

<!-- ============ Element definitions ============ -->
<!ELEMENT enote         ((%subject;), (%prolog;)?, (%notedetail;), (%enote-info-types;)* )>
<!ATTLIST enote           id ID #REQUIRED
                          conref CDATA #IMPLIED
                          outputclass CDATA #IMPLIED
                          xml:lang NMTOKEN #IMPLIED
                          %arch-atts;
                          domains CDATA "&included-domains;"
>

<!ELEMENT subject        (#PCDATA)*>
<!ATTLIST subject         %id-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT notedetail    ((%noteheader;), (%notebody;)?)>
<!ATTLIST notedetail     %univ-atts;
                         outputclass CDATA #IMPLIED
>

<!ELEMENT noteheader     ((%From;), (%To;)?, (%Cc;)?, (%Bcc;)?, (%Date;)?, (%delivery;)?, (%references;)?, (%attachments;)?)>
<!ATTLIST noteheader      %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT From           (#PCDATA | %recipient;)*>
<!ATTLIST From            %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT To             (#PCDATA | %recipient;)*>
<!ATTLIST To              %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Cc             (#PCDATA | %recipient;)*>
<!ATTLIST Cc              %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Bcc             (#PCDATA | %recipient;)*>
<!ATTLIST Bcc             %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT recipient       (#PCDATA)*>
<!ATTLIST recipient       href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          format CDATA "mailto"
                          outputclass CDATA #IMPLIED
>

<!ELEMENT Date           (#PCDATA)*>
<!ATTLIST Date            %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT delivery        ((%Priority;)?, (%Importance;)?, (%ReturnReceipt;)?, (%Encrypt;)?)>
<!ATTLIST delivery        %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Priority        EMPTY>
<!ATTLIST Priority        name CDATA "priority"
                          value ( normal | non-urgent | urgent ) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Importance      EMPTY>
<!ATTLIST Importance      name CDATA "importance"
                          value ( normal | low | high ) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT ReturnReceipt   EMPTY>
<!ATTLIST ReturnReceipt   state (yes|no) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Encrypt         EMPTY>
<!ATTLIST Encrypt         state (yes|no) #REQUIRED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT references     ((%InReplyTo;)?, (%Reference;)*)>
<!ATTLIST references      %univ-atts;
                          outputclass CDATA #IMPLIED
>
<!ELEMENT InReplyTo       EMPTY>
<!ATTLIST InReplyTo       href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          format (messageID|enote) "messageID"
                          outputclass CDATA #IMPLIED
>
<!ELEMENT Reference       EMPTY>
<!ATTLIST Reference       href CDATA #IMPLIED
                          keyref NMTOKEN #IMPLIED
                          %univ-atts;
                          format (messageID|enote) "messageID"
                          outputclass CDATA #IMPLIED
>

<!ELEMENT attachments     (%image; | %object;)*>
<!ATTLIST attachments     %univ-atts;
                          outputclass CDATA #IMPLIED
>

<!ELEMENT notebody        (#PCDATA|%basic.ph;|%p;|%dl;|%ul;|%ol;|%pre;|%simpletable;)* >
<!ATTLIST notebody        spectitle CDATA #IMPLIED
                          %univ-atts;
                          outputclass CDATA #IMPLIED
>


<!-- ============ Element specialization declarations ============ -->
<!ATTLIST enote         class  CDATA "- topic/topic       enote/enote ">
<!ATTLIST subject       class  CDATA "- topic/title       enote/subject ">
<!ATTLIST notedetail    class  CDATA "- topic/body        enote/notedetail ">

<!ATTLIST noteheader    class  CDATA "- topic/ul          enote/noteheader ">

<!ATTLIST From          class  CDATA "- topic/li          enote/From ">
<!ATTLIST To            class  CDATA "- topic/li          enote/To ">
<!ATTLIST Cc            class  CDATA "- topic/li          enote/Cc ">
<!ATTLIST Bcc           class  CDATA "- topic/li          enote/Bcc ">
<!ATTLIST recipient     class  CDATA "- topic/xref        enote/recipient ">

<!ATTLIST Date          class  CDATA "- topic/li          enote/Date ">

<!ATTLIST delivery      class  CDATA "- topic/li          enote/delivery ">
<!ATTLIST Priority      class  CDATA "- topic/state       enote/Priority ">
<!ATTLIST Importance    class  CDATA "- topic/state       enote/Importance ">
<!ATTLIST ReturnReceipt class  CDATA "- topic/boolean     enote/ReturnReceipt ">
<!ATTLIST Encrypt       class  CDATA "- topic/boolean     enote/Encrypt ">

<!ATTLIST references    class  CDATA "- topic/li          enote/references ">
<!ATTLIST InReplyTo     class  CDATA "- topic/xref        enote/InReplyTo ">
<!ATTLIST References    class  CDATA "- topic/xref        enote/References ">

<!ATTLIST attachments   class  CDATA "- topic/li          enote/attachments ">

<!ATTLIST notebody      class  CDATA "- topic/section     enote/notebody ">
