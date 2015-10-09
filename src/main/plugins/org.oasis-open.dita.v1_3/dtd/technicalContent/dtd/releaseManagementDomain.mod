<?xml version="1.0" encoding="UTF-8"?>
<!-- DITA Release Management Metadata Domain                       -->
<!--                                                               -->
<!-- Defines element types for capturing change details within     -->
<!-- topics or maps.                                               -->
<!-- DITA 1.3                                                      -->
<!-- Copyright (c) 2013 OASIS Open                                 -->
<!-- ============================================================= -->
<!--                     PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                     TYPICAL INVOCATION                         -->
<!--                                                                -->
<!-- Refer to this file by the following public identifier or an   -->
<!-- appropriate system identifier                                 -->
<!-- PUBLIC "-//OASIS//ENTITIES DITA Release Management Domain//EN" -->
<!-- ============================================================= -->
<!--                                                               -->

<!-- ============================================================= -->
<!--                   ELEMENT NAME ENTITIES                       -->
<!-- ============================================================= -->

<!ENTITY % change-historylist
                       "change-historylist"                          >
<!ENTITY % change-item "change-item"                                 >
<!ENTITY % change-person
                       "change-person"                               >
<!ENTITY % change-organization
                       "change-organization"                         >
<!ENTITY % change-revisionid
                       "change-revisionid"                           >
<!ENTITY % change-request-reference
                       "change-request-reference"                    >
<!ENTITY % change-request-system
                       "change-request-system"                       >
<!ENTITY % change-request-id
                       "change-request-id"                           >
<!ENTITY % change-started
                       "change-started"                              >
<!ENTITY % change-completed
                       "change-completed"                            >
<!ENTITY % change-summary
                       "change-summary"                              >

<!-- ============================================================= -->
<!--                    ELEMENT DECLARATIONS                       -->
<!-- ============================================================= -->

<!ENTITY % changehistory.data.atts
              "%univ-atts;
               datatype
                          CDATA
                                    #IMPLIED
               outputclass
                          CDATA
                                    #IMPLIED"
>
<!--                    LONG NAME: Change History List             -->
<!ENTITY % change-historylist.content
                       "(%change-item;)*"
>
<!ENTITY % change-historylist.attributes
              "%changehistory.data.atts;"
>
<!ELEMENT  change-historylist %change-historylist.content;>
<!ATTLIST  change-historylist %change-historylist.attributes;>


<!--                    LONG NAME: Change History List Item        -->
<!ENTITY % change-item.content
                       "((%change-person; |
                          %change-organization;)*,
                         (%change-revisionid;)?,
                         (%change-request-reference;)?,
                         (%change-started;)?,
                         (%change-completed;),
                         (%change-summary;)*,
                         (%data;)*)"
>
<!ENTITY % change-item.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-item'"
>
<!ELEMENT  change-item %change-item.content;>
<!ATTLIST  change-item %change-item.attributes;>


<!--                    LONG NAME: Change Person                   -->
<!ENTITY % change-person.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % change-person.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-person'"
>
<!ELEMENT  change-person %change-person.content;>
<!ATTLIST  change-person %change-person.attributes;>


<!--                    LONG NAME: Change Organization             -->
<!ENTITY % change-organization.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % change-organization.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-organization'"
>
<!ELEMENT  change-organization %change-organization.content;>
<!ATTLIST  change-organization %change-organization.attributes;>


<!--                    LONG NAME: Change Revision ID              -->
<!ENTITY % change-revisionid.content
                       "(%data.cnt;)*"
>
<!ENTITY % change-revisionid.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-revisionid'"
>
<!ELEMENT  change-revisionid %change-revisionid.content;>
<!ATTLIST  change-revisionid %change-revisionid.attributes;>


<!--                    LONG NAME: Change Request Reference        -->
<!ENTITY % change-request-reference.content
                       "((%change-request-system;)?,
                         (%change-request-id;)?)"
>
<!ENTITY % change-request-reference.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-request-reference'"
>
<!ELEMENT  change-request-reference %change-request-reference.content;>
<!ATTLIST  change-request-reference %change-request-reference.attributes;>


<!--                    LONG NAME: Change Request System           -->
<!ENTITY % change-request-system.content
                       "(%data.cnt;)*"
>
<!ENTITY % change-request-system.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-request-system'"
>
<!ELEMENT  change-request-system %change-request-system.content;>
<!ATTLIST  change-request-system %change-request-system.attributes;>


<!--                    LONG NAME: Change Request ID               -->
<!ENTITY % change-request-id.content
                       "(%data.cnt;)*"
>
<!ENTITY % change-request-id.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-request-id'"
>
<!ELEMENT  change-request-id %change-request-id.content;>
<!ATTLIST  change-request-id %change-request-id.attributes;>


<!--                    LONG NAME: Change started date             -->
<!ENTITY % change-started.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % change-started.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-started'"
>
<!ELEMENT  change-started %change-started.content;>
<!ATTLIST  change-started %change-started.attributes;>


<!--                    LONG NAME: Change completed date           -->
<!ENTITY % change-completed.content
                       "(#PCDATA |
                         %text;)*"
>
<!ENTITY % change-completed.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-completed'"
>
<!ELEMENT  change-completed %change-completed.content;>
<!ATTLIST  change-completed %change-completed.attributes;>


<!--                    LONG NAME: Change Summary                  -->
<!ENTITY % change-summary.content
                       "(%data.cnt;)*"
>
<!ENTITY % change-summary.attributes
              "%changehistory.data.atts;
               name
                          CDATA
                                    'change-summary'"
>
<!ELEMENT  change-summary %change-summary.content;>
<!ATTLIST  change-summary %change-summary.attributes;>



<!-- ============================================================= -->
<!--             SPECIALIZATION ATTRIBUTE DECLARATIONS             -->
<!-- ============================================================= -->
  
<!ATTLIST  change-historylist %global-atts;  class CDATA "+ topic/metadata relmgmt-d/change-historylist ">
<!ATTLIST  change-item  %global-atts;  class CDATA "+ topic/data relmgmt-d/change-item ">
<!ATTLIST  change-person %global-atts;  class CDATA "+ topic/data relmgmt-d/change-person ">
<!ATTLIST  change-organization %global-atts;  class CDATA "+ topic/data relmgmt-d/change-organization ">
<!ATTLIST  change-revisionid %global-atts;  class CDATA "+ topic/data relmgmt-d/change-revisionid ">
<!ATTLIST  change-request-reference %global-atts;  class CDATA "+ topic/data relmgmt-d/change-request-reference ">
<!ATTLIST  change-request-system %global-atts;  class CDATA "+ topic/data relmgmt-d/change-request-system ">
<!ATTLIST  change-request-id %global-atts;  class CDATA "+ topic/data relmgmt-d/change-request-id ">
<!ATTLIST  change-started %global-atts;  class CDATA "+ topic/data relmgmt-d/change-started ">
<!ATTLIST  change-completed %global-atts;  class CDATA "+ topic/data relmgmt-d/change-completed ">
<!ATTLIST  change-summary %global-atts;  class CDATA "+ topic/data relmgmt-d/change-summary ">

<!-- ================== End of DITA Release Management Domain ==================== -->
 