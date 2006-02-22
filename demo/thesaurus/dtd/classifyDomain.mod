<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<!ENTITY % topicsubject        "topicsubject">
<!ENTITY % subjectref          "subjectref">
<!ENTITY % topicSubjectTable   "topicSubjectTable">
<!ENTITY % topicSubjectHeader  "topicSubjectHeader">
<!ENTITY % topicSubjectRow     "topicSubjectRow">
<!ENTITY % topicCell           "topicCell">
<!ENTITY % subjectCell         "subjectCell">

<!-- SKOS equivalent:  primary if href or keyref are specified -->
<!ELEMENT topicsubject ((%topicmeta;)?, (%subjectref;)*)>
<!ATTLIST topicsubject
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  type         CDATA     #IMPLIED
  scope       (local | external) #IMPLIED
  format       CDATA     #IMPLIED
  %select-atts;
>

<!ELEMENT subjectref ((%topicmeta;)?)>
<!ATTLIST subjectref
  navtitle     CDATA     #IMPLIED
  id           ID        #IMPLIED
  href         CDATA     #IMPLIED
  keyref       CDATA     #IMPLIED
  query        CDATA     #IMPLIED
  conref       CDATA     #IMPLIED
  collection-type    (choice|unordered|sequence|family) #IMPLIED
  type         CDATA     #IMPLIED
  scope       (local | external) #IMPLIED
  format       CDATA     #IMPLIED
  linking     (targetonly|sourceonly|normal|none) #IMPLIED
  %select-atts;
>

<!ELEMENT topicSubjectTable    ((%topicmeta;)?, (%topicSubjectHeader;)?, (%topicSubjectRow;)+) >
<!ATTLIST topicSubjectTable  title CDATA #IMPLIED
                          %id-atts;
                          %topicref-atts-no-toc;
                          %select-atts;
>
<!-- The header defines the set of subjects for each column.
     By default, the subject in the header cell must be a broader ancestor
         within a scheme available during processing for the subjects
         in the same column of other rows
     In the header, the topicCell serves primarily as a placeholder
         for the topic column but could also provide some constraints
         or metadata for the topics -->
<!ELEMENT topicSubjectHeader  ((%topicCell;), (%subjectCell;)+)>
<!ATTLIST topicSubjectHeader  %id-atts;
                          %select-atts;
>
<!ELEMENT topicSubjectRow  ((%topicCell;), (%subjectCell;)+)>
<!ATTLIST topicSubjectRow  %id-atts;
                          %select-atts;
>
<!ELEMENT topicCell      ((%topicref;)+)>
<!ATTLIST topicCell       %id-atts;
                          %topicref-atts;
>
<!ELEMENT subjectCell    ((%topicsubject;)?, (%subjectref;)*)>
<!ATTLIST subjectCell     %id-atts;
                          %topicref-atts;
>


<!ATTLIST topicsubject %global-atts;
    class CDATA "+ map/topicref classify-d/topicsubject ">
<!ATTLIST subjectref %global-atts;
    class CDATA "+ map/topicref classify-d/subjectref ">
<!ATTLIST topicSubjectTable %global-atts;
    class CDATA "+ map/reltable classify-d/topicSubjectTable ">
<!ATTLIST topicSubjectHeader %global-atts;
    class CDATA "+ map/relrow classify-d/topicSubjectHeader ">
<!ATTLIST topicSubjectRow %global-atts;
    class CDATA "+ map/relrow classify-d/topicSubjectRow ">
<!ATTLIST topicCell %global-atts;
    class CDATA "+ map/relcell classify-d/topicCell ">
<!ATTLIST subjectCell %global-atts;
    class CDATA "+ map/relcell classify-d/subjectCell ">
