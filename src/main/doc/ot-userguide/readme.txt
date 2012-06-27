                                                        Aug 10, 2006

        README for DITA Open Toolkit User Guide and Reference Files

Contents
========

This folder contains these subfolders:

DITAOT_UGRef_SOURCE - DITA source for the DITA Open Toolkit User Guide and Reference

MY_DITA_SOURCE      - Sample files to accompany the User Guide

How to build the User Guide
===========================

The User Guide can be built using the standard DITA-OT 1.2.2. For
Windows users, there is a runbuild.bat file in the source directory
that invokes the build for various output types. All output types
can be built using this command. The output will be created in the
DITAOT_UGRef_OUTPUT directory.

A single Ant script (ant_scripts\DITAOT_UGRef_all.xml) is
used for building to all supported output targets. You may need to 
change some of the global properties at the beginning of this script 
to match your own DITA build environment. Also, the build debugging 
and reporting scripts require that the PHP intepreter be installed. 
If you do not have PHP installed, you may want to remove the targets 
for these scripts.



