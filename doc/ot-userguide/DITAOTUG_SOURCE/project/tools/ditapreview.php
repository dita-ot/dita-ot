<?php

/*
 * (c) Copyright VR Communications, Inc. 2006 All Rights Reserved.
 *
 * Author: Richard Johnson
 *
 * This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
 * See the accompanying license.txt file for applicable licenses.
 */

function show_usage()
{
  die("Usage: php " . "ditapreview" . " ditamap-file Last | Chapter n \n");
}

/*
  Initialize the temporary DITA map
*/
function init_map(&$map)
{
  $map='<?xml version="1.0" encoding="utf-8"?>' . "\n";
  $map=$map . '<!DOCTYPE map PUBLIC "-//OASIS//DTD DITA Map//EN" "../dtd/map.dtd">' . "\n";
  $map=$map . '<map title="DITA XHTML Plausible Preview">' . "\n";
}

/*
  Complete the DITA map
*/
function complete_map(&$map)
{
  $map = $map . '</map>' . "\n";
}

/*
  Add a file to the DITA map
*/
function add_to_map(&$map,$f,$fmt)
{
  if( isset($fmt) )
    $map=$map . '<topicref href="' . substr($f,1) . '" format="' . $fmt . '"/>' . "\n";
  else
    $map=$map . '<topicref href="' . substr($f,1) . '"/>' . "\n";
}

/**************************************************************

 PHP script to preview XHTML output for a subset of a DITA map.

**************************************************************/

include 'ditautil.inc';

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = false; /* check id references */

/* the valid opcodes */
$opL="L";
$opC="C";

$ndita=0;
$nchapter=0;

if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

$argc = count($argv);

/* pick up ditamap from arguments */
if( $argc < 2 )
  show_usage();

/* Collect all the arguments. */

$ditamap = $argv[1];

switch( $argc )
{
  case 2:
    /* set defaults */
    $opcode=$opL;
    $acount=1;
    break;
  case 3:
    $opcode  = $argv[2];
    $acount=1;
    break;
  case 4:
    $opcode  = $argv[2];
    $acount  = $argv[3];
    break;
  default:
    show_usage();
}

/* done processing arguments, display startup message */

print("\nStarting from ditamap --- " . $ditamap . "\n");
print(" operation -------------- " . $opcode . "\n");
print(" count ------------------ " . $acount . "\n");

/* validate the arguments */
if( !is_numeric($acount) )
  show_usage();

$opc = strtoupper(substr($opcode,0,1));
switch($opc)
{
  case $opC:
  case $opL:
    break;
  default:
    show_usage();
}

$map = basename($ditamap);
$dir = dirname($ditamap) . $fsep;

/*
  walk the map and get list of used files
*/

$rc = get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                    &$fused, &$notfound, &$lf, &$rf, &$tp, &$rcon);

if( $rc )
{
  /* got the file list, now do preview processing */
  print("\n" . count($fused) . " files used in " . $ditamap . "\n\n");

  $lastdate=0;
  foreach( $fused as $f)
  {
    /* loop through all DITA files */
    if( isDITA($f) )
    {
      $ndita++;
      $fmt = date("Ymd", filemtime($f));
      $docdate[$f]=$fmt;
      /* get last file date */
      if($fmt>$lastdate)
        $lastdate = $fmt;

      $dt = getDOCTYPE($f);
      $doct[$f]=$dt;
      $dirs[] = dirname($f);

      if( $dbg_flag )
        print(fshort($f,$rdir) . " , " . $dt . " , " . getAuthor($f) . " , " .
              getSize($f) . " , " . $fmt . " , " . getdesc($f) . "\n");

      if( ($opc==$opC) && ($dt=="map") )
      {
        /* check for desired chapter */
        $nchapter++;
        if( $nchapter == $acount )
        {
          print("Chapter " . $acount . " map file is " . $f . "\n");
          $tempmap = $f;
          break;
        }
      }
    } /* file is DITA source */
  }
  print($ndita . " files found, last date is " . $lastdate . "\n");
  $rootd = rootdir($dirs);
  print("project root directory is: " . $rootd . "\n");

  /* create a temporary DITA map if "Last" was specified */
  if( ($opc == $opL) && ($ndita>0) )
  {
    $mfile="";
    init_map($mfile); /* initialize a temporary map */

    /* add all the map files */
    foreach( $fused as $f)
    {
      if( isDITA($f) )
      {
        if( ( strpos($doct[$f],"map") !== FALSE) && ($docdate[$f] == $lastdate) )
          add_to_map($mfile,fshort($f,$rootd),"ditamap");
      }
    }
    /* add all the non-map files */
    foreach( $fused as $f)
    {
      if( isDITA($f) )
      {
        if( ( strpos($doct[$f],"map") === FALSE) && ($docdate[$f] == $lastdate) )
          add_to_map($mfile,fshort($f,$rootd),null);
      }
    }
    complete_map($mfile);

    print("\n");
    print("Created map:\n");
    print($mfile);
    print("\n");
  } /* make map for files last touched */

  /* make sure we have a temp directory for the output */
  if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
    $tdir = "C:\\temp";
  else
    $tdir = "/tmp";

  if( $dbg_flag )
    print("TEMP dir is " . $tdir . "\n");

  if( !is_dir($tdir) )
  {
    $drc = mkdir($tdir);
    if( $drc )
      print($tdir . " directory has been created.\n");
    else
      die($tdir . " directory could not be created.\n");
  }

  $outdir = $tdir . $fsep . "DITAout";
  if( $dbg_flag )
    print("DITA output dir is " . $outdir . "\n");
  if( !is_dir($outdir) )
  {
    $drc = mkdir($outdir);
    if( $drc )
      print($outdir . " directory has been created.\n");
    else
      die($outdir . " directory could not be created.\n");
  }

  if( $opc == $opL )
  {
    /* save the temporary DITA map to disk */
    $tname = tempnam($rootd,"DT");
    rename($tname, $tname . ".ditamap");
    $tname = $tname . ".ditamap";
    $tempmap = $tname;
    file_put_contents($tempmap, $mfile);
  }

  $tmpdir = $tdir . $fsep . "DITAtemp";
  if( $dbg_flag )
    print("DITA TEMP dir is " . $tmpdir . "\n");
  if( !is_dir($tmpdir) )
  {
    $drc = mkdir($tmpdir);
    if( $drc )
      print($tmpdir . " directory has been created.\n");
    else
      die($tmpdir . " directory could not be created.\n");
  }

  if( !isset($tempmap) )
    die("Nothing found to process.\n");

  /* create the Java command to do the processing */
  $rtempmap = realpath($tempmap);
  if( $opc == $opC )
    $basedir = dirname($rtempmap);
  else
    $basedir = $rootd;

  $cmd = "java org.dita.dost.invoker.CommandLineInvoker" .
          " /ditadir:" . $_ENV['DITA_DIR'] .
          " /basedir:" . $basedir .
          " /i:" . $rtempmap . 
          " /outdir:" . $outdir . 
          " /tempdir:" . $tmpdir . 
          " /transtype:xhtml /cleantemp:yes";

  /* set file we will point the browser at to display our results */
  if( $opc == $opL )
    $indexdir = $outdir;
  else
  {
    $ppp = explode($fsep,$rtempmap);
    $subdir = $ppp[count($ppp)-2];
    $indexdir = $outdir . $fsep . $subdir;
  }
  $indexfile = $indexdir . $fsep . "index.html";
  print("indexfile: " . $indexfile . "\n");

  print("Processing command: " . $cmd . "\n");

  system($cmd,$crc);
  print("processing return code was " . $crc . "\n");

  /* cleanup temporary file */
  if( $opc == $opL )
  {
    unlink($tempmap);
  }

  /* launch Windows Web browser */
  $browser1 = "C:\WINDOWS\ServicepackFiles\i386\iexplore.exe"; /* IE6 */
  $browser2 = "C:\WINDOWS\ie7\iexplore.exe"; /* IE7 */
  if( file_exists($browser1) )
    system($browser1 . " " . $indexfile, $crc);
  else if ( file_exists($browser2) )
    system($browser2 . " " . $indexfile, $crc);
  else
    print("Error, could not find Windows Internet Explorer.\n");

} /* get_map_lists worked */
else
{
  print("Error: failure scanning ditamap.\n");
  die("Stop.\n");
}

?>
