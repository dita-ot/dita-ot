<?php

/*
 * (c) Copyright VR Communications, Inc. 2006 All Rights Reserved.
 *
 * Author: Richard Johnson, www.vrcommunications.com
 *
 * This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
 * See the accompanying license.txt file for applicable licenses.
 */

/**************************************************************

 PHP script to scan all ID strings used by a ditamap,
 and show which are duplicates.

**************************************************************/

include 'ditautil.inc';

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = false; /* check id references */
$problems=0;

/* pick up ditamap from arguments */
if( count($argv)!=2 )
  die("Usage: php " . $argv[0] . " ditamap-file \n");

if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

/* the top directory we are going to process */
$ditamap = $argv[1];

print("Starting from ditamap " . $ditamap . "\n");

$map = basename($ditamap);
$dir = dirname($ditamap) . $fsep;

/*
  walk the map and get list of used files, and references
*/

$rc = get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                    $fused, $notfound, $lf, $rf, $tp, $rcon);

if( count($notfound)>0 )
{
  print(count($notfound) . " files not found.\n");
  foreach( $notfound as $nf )
  {
    print($nf . "\n");
  }
}

if( $rc )
{
  print(count($fused) . " files used in " . $ditamap . "\n\n");

  /* look for IDs in all the files found in the map */
  foreach($fused as $f)
  {
    if( !isURL($f) && !isIMAGE($f) && ($f !== $ditamap) )
    {
      $irc = get_ids($dbg_flag, $f, $idfile, $idid);
    }
  }

  print("There are " . count($idfile) . " IDs defined.\n\n");
  array_multisort($idid, SORT_ASC, $idfile, SORT_ASC);
  $idl = max_str_len($idid, $padding);
  $lastid="";
  for($i=0; $i<count($idfile); $i++)
  {
    if( $lastid == $idid[$i] )
    {
      $xx = "* ";
      $problems++;
    }
    else
      $xx = "  ";
    print(substr($xx . $idid[$i] . $padding,0,$idl) . " " . $idfile[$i] . "\n");
    $lastid = $idid[$i];

  }
} /* get_map_lists worked */
else
{
  print("Error: failure walking ditamap.\n");
}

print("\n");
if($problems>0)
{
  print($problems . " duplicate IDs found.\n");
}
else
  print("no duplicate IDs found.\n");
?>
