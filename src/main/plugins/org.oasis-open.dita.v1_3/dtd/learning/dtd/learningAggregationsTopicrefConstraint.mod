<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================  -->
<!-- MODULE:    DITA Learning Simple Topicref Constraint - RNG               -->
<!-- VERSION:   1.3                                                -->
<!-- DATE:      June 2013                                          -->
<!-- ============================================================= -->
<!-- =============================================================  -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)      -->
<!-- PURPOSE:    Limit topicrefs to non-navigation specializations  -->
<!--             of topicref                                       -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!-- June 2013                                                     -->
<!-- (C) Copyright OASIS Open 2013, 2014                                 -->
<!-- All Rights Reserved.                                           -->
<!-- ============================================================= -->
<!--                                                               -->

<!ENTITY learningAggregationsTopicref-constraints
  "(map learningAggregationsTopicref-c)"
>
<!ENTITY % mapgroup-d-topicref                             "mapgroup-d-topicref">
<!ENTITY % keydef                                          "keydef">
<!ENTITY % mapref                                          "mapref">
<!ENTITY % topicgroup                                      "topicgroup">

<!ENTITY % topicref
              "%mapgroup-d-topicref;"
>
<!ENTITY % mapgroup-d-topicref
              "(%keydef; |
                %mapref; |
                %topicgroup;)*"
>

<!-- ================== DITA Learning Simple Topicref Constraint ==================== -->
 