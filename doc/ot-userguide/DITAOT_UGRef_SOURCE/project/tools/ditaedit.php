<?php

/*
 * (c) Copyright VR Communications, Inc. 2006 All Rights Reserved.
 *
 * Author: Richard Johnson
 *
 */

function show_usage()
{
  die("Usage: php " . "ditaedit" . " ditamap-file Elementdelete|Replace|Count|List source-string replacement-string\n");
}

/*
  Delete an element and all its included attributes and text.
*/
function delete_element($ename, $s)
{
  $element_null = "<" . $ename . "/>";
  $element_start = "<" . $ename;

  $element_end = "</" . $ename . ">";
  $lend = strlen($element_end);

  $found = TRUE;
  $sr = $s;

  while ( $found )
  {
    $found = FALSE;

    /* first look for empty element */
    $efound = $element_null;
    $pos = strpos($sr, $element_null);
    if( $pos !== FALSE )
    {
      /* handle null element case */
      $found = TRUE;
      $delstr = $element_null;
      print(" delete empty element: " . $delstr . "\n");
      $sr = str_replace($delstr, "", $sr);
    }
    else
    {
      $pos = strpos($sr, $element_start);
      if( $pos !== FALSE )
      {
        $found = TRUE;
        $pos2 = strpos($sr, $element_end, $pos+1);
        if( $pos2 === FALSE )
        {
          $element_end = "/>";
          $lend = 2;
          $pos2 = strpos($sr, $element_end, $pos+1);
        }

        if( $pos2 === FALSE )
          die("file is not well-formed.\n");
        else
        {
           $delstr = substr($sr, $pos, $pos2 - $pos + $lend);
           print(" delete elementt: " . $delstr . "\n");
           $sr = str_replace($delstr, "", $sr);
        }
      } /* found start of element we want */
    } /* not empty element */
  }

  return $sr;
}

/*
  Display all lines in the file containing the string 
*/
function show_lines($s, $sstring)
{
  $noccur=0;
  $nl="\n";
  $sl = strlen($s);
  if($sl==0) return;

  $sstart=0;

  while( $sstart<($sl-1) )
  {
    $p = strpos($s, $sstring, $sstart);
    if($p === FALSE) break;

    /* get start of line containing the string */
    $ls=0;
    for($i=$p; $i>=0; $i--)
    {
      if( substr($s,$i,1)===$nl )
      {
        $ls=$i+1;
        break;
      }
    }

    /* get end of line containing the string */
    $le=strpos($s,$nl,$p);

    /* display the line containing the string */
    print( " " . trim(substr($s,$ls,$le-$ls)) . "\n");
    $noccur++;
    $sstart=$p+1;
  }

  if( $noccur>1 )
    print(" " . $noccur . " occurances in this file.\n");
  print("\n");
  return;
}

/**************************************************************

 PHP script to do editing operations for all files in a ditamap.

**************************************************************/

include 'ditautil.inc';

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = false; /* check id references */

$nc=0;

if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

$argc = count($argv);

/* pick up ditamap from arguments */
if( $argc>6 )
  show_usage();

$ditamap=null;
$sstring=null;
$dstring=null;
$opcode=null;

/* Collect all the arguments. Prompt for things not provided in args. */

$fp=null;

if( $argc>1 )
  $ditamap = $argv[1];
else
{
  /* read from console to prompt */
  if( !$fp )
    $fp = fopen("php://stdin","r"); 
  print "DITA map: ";
  // rtrim to cut off the \n from the shell
  $ditamap = rtrim(fgets($fp, 1024));
  if( strlen($ditamap)==0 )
    show_usage();
}

if( $argc>2 )
{
  $opc = $argv[2];
  $opcode=substr(strtoupper($opc),0,1);
}
else
{
  if( !$fp )
    $fp = fopen("php://stdin","r"); 
  print "operation(Elementdelete|Replace|Count|List): ";
  $opc = strtoupper(rtrim(fgets($fp, 1024)));
  if( strlen($opc)==0 )
    show_usage();
  else
  {
    $opcode=substr($opc,0,1);
  }
}

/* check opcode value */ 
switch( $opcode )
{
  case 'R':
  case 'C':
  case 'L':
  case 'E':
    break;
  default:
    show_usage();
}

if( $argc>3 )
  $sstring = $argv[3];
else
{
  if( !$fp )
    $fp = fopen("php://stdin","r"); 
  if( $opcode == "E" )
  {
    print "name of element to be deleted: ";
    $ename = rtrim(fgets($fp, 1024));
    if( strlen($ename)==0 )
      show_usage();
    $sstring= "<" . $ename;
    $dstring = "";
  }
  else
  {
    print "search string: ";
    $sstring = rtrim(fgets($fp, 1024));
    if( strlen($sstring)==0 )
      show_usage();
  }
}

if( $opcode === "R" )
{
  if( $argc>4 )
    $dstring = $argv[4];
  else
  {
    if( !$fp )
      $fp = fopen("php://stdin","r"); 
    print "replacement string: ";
    $dstring = rtrim(fgets($fp, 1024));
  }
}

if( $fp )
  fclose($fp);

/* done processing arguments, display startup message */

print("\nStarting from ditamap --- " . $ditamap . "\n");
print(" operation -------------- " . $opcode . "\n");
print(" search string ---------- " . $sstring . "\n");
if( $opcode === "R" )
  print(" replacement string ----- " . $dstring . "\n");

/* are we doing replacement? */
if( (($opcode !== "R") && ($opcode !== "E") ) || ($sstring === $dstring) )
  $repflag=false;
else
  $repflag=true;

$map = basename($ditamap);
$dir = dirname($ditamap) . $fsep;

/*
  walk the map and get list of used files
*/

$rc = get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                    &$fused, &$notfound, &$lf, &$rf, &$tp, &$rcon);

if( $rc )
{
  /* got the file list, now do string processing */
  print("\n" . count($fused) . " files used in " . $ditamap . "\n\n");

  foreach( $fused as $f )
  {
    if( isDITA($f) )
    {
      /* read entire file into a string */
      $s = file_get_contents($f);
      if( $s !== FALSE )
      {
        /* is the string present? */
        $p = strpos($s, $sstring);
        if( $p !== FALSE )
        {
          $nc++;
          if( $repflag )
          {
            /* replace the string */
            if( $opcode == "E" )
            {
              /* delete all instances of the element */
              print("Deleting element " . $ename . " in file " . $f . "\n");
              $sr = delete_element($ename, $s);
            }
            else
            {
              print("Replacing " . '"' .$sstring . '"' . " -> " . 
                    '"' . $dstring . '"' . " in file " . $f . "\n");
              $sr = str_replace($sstring, $dstring, $s);
            }

            /* write the changed file back */
            $rc = file_put_contents($f, $sr);
            if( $rc == 0 )
            {
              print("Error writing file " . $f . "\n");
              die("Stop.\n");
            }
          } /* replacing */
          else
          {
            if( $opcode !== "C" )
            {
              /* just list where the string occurs */
              print("String " . '"' . $sstring . '"' . " found in file " . $f . "\n");
              show_lines($s, $sstring);
            }
          } /* not replacing */
        } /* we found the string in the file */
      } /* the file was read OK */
      else
      {
        print("Error reading file " . $f . "\n");
        die("Stop.\n");
      }
    } /* file may be DITA source */
  } /* loop on files in map */
} /* get_map_lists worked */
else
{
  print("Error: failure scanning ditamap.\n");
  die("Stop.\n");
}

if( $nc>0 )
  print("\n" . '"' . $sstring . '"' . " found in " . $nc . " files.\n");
else
  print("\n" . '"' . $sstring . '"' . " not found.\n");

?>
