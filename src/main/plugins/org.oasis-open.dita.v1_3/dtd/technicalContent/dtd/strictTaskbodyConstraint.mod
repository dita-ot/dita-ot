<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--                    HEADER                                     -->
<!-- ============================================================= -->
<!--  MODULE:    DITA Strict Taskbody Constraint                   -->
<!--  VERSION:   1.3                                               -->
<!--  DATE:      March 2014                                        -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- ============================================================= -->
<!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
<!--                    TYPICAL INVOCATION                         -->
<!--                                                               -->
<!--  Refer to this file by the following public identifier or an  -->
<!--       appropriate system identifier                           -->
<!-- PUBLIC "-//OASIS//ELEMENTS DITA Strict Taskbody Constraint//EN" -->
<!--       Delivered as file "strictTaskbodyConstraint.mod"             -->
<!-- ============================================================= -->
<!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
<!--                                                               -->
<!-- PURPOSE:    Declaring the domain entity for the strict task   -->
<!--             constraint module                                 -->
<!--                                                               -->
<!-- ORIGINAL CREATION DATE:                                       -->
<!--             April 2008                                        -->
<!--                                                               -->
<!--             (C) Copyright OASIS Open 2008, 2014.              -->
<!--             All Rights Reserved.                              -->
<!--                                                               -->
<!--  UPDATES:                                                     -->
<!-- ============================================================= -->
<!--                                                               -->

<!ENTITY taskbody-constraints
  "(topic task strictTaskbody-c)"
>
<!ENTITY % prereq                                          "prereq">
<!ENTITY % context                                         "context">
<!ENTITY % steps                                           "steps">
<!ENTITY % steps-unordered                                 "steps-unordered">
<!ENTITY % result                                          "result">
<!ENTITY % tasktroubleshooting                             "tasktroubleshooting">
<!ENTITY % example                                         "example">
<!ENTITY % postreq                                         "postreq">

<!ENTITY % taskbody.content
              "((%prereq;)?,
                (%context;)?,
                (%steps; |
                 %steps-unordered;)?,
                (%result;)?,
                (%tasktroubleshooting;)?,
                (%example;)?,
                (%postreq;)?)"
>

<!-- ================== DITA Strict Taskbody Constraint ==================== -->
 