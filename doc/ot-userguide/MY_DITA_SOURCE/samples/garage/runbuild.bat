@echo off
@rem Batch script for the garage sample files
@rem The Ant script sets the default output target to xhtml
@rem The default Ant script is garage_hierarchy_all.xml
@rem Argument 1 (%1) in this script is the output target name
@rem Specifying "sequence" as argument 2 (%2) in this script runs the garage_sequence_all.xml Ant script
@rem Valid examples:
@rem runbuild [runs the hierarchy script to xhtml]
@rem runbuild dita2xhtml sequence [runs the sequence script to xhtml - "dita2xhtml" is required!] 
@rem runbuild dita2pdf2 [runs the hierarchy script to pdf2]
@rem runbuild dita2htmlhelp sequence [runs the sequence script to htmlhelp]
@rem runbuild dita2filtered sequence [runs the sequence script with filtering on - see the ditaval file] 
@rem runbuild all hierarchy [runs the hierarchy script to all targets]
@rem runbuild all sequence [runs the sequence script to all targets]

if "%2"=="" goto hierarchy

if "%2"=="hierarchy" goto hierarchy

if "%2"=="sequence" goto sequence

echo The second argument needs to be either "hierarchy" or "sequence".

goto done

:sequence

ant -Dbasedir=%DITA_DIR% -f ant_scripts\garage_sequence_all.xml -logger org.dita.dost.log.DITAOTBuildLogger %1

goto done

:hierarchy

ant -Dbasedir=%DITA_DIR% -f ant_scripts\garage_hierarchy_all.xml -logger org.dita.dost.log.DITAOTBuildLogger %1

:done


