@echo off
@rem Batch script for the grocery shopping sample files
@rem The Ant script sets the default output target to xhtml
@rem The default Ant script is grocery_all.xml
@rem Valid examples:
@rem runbuild [runs the script to xhtml]
@rem runbuild dita2xhtml [runs the script to xhtml]
@rem runbuild dita2pdf2 [runs the script to pdf2]
@rem runbuild dita2htmlhelp [runs the script to htmlhelp]
@rem runbuild all [runs the script to all targets]

ant -Dbasedir=%DITA_DIR% -f ant_scripts\grocery_all.xml -logger org.dita.dost.log.DITAOTBuildLogger %1

