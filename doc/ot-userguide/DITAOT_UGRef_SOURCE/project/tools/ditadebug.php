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

 PHP script to scan all the files used by a ditamap and
 produce various debugging and status reports.

**************************************************************/

/* include common routines */
include 'ditautil.inc';

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = true; /* check id references */

/* pick up ditamap from arguments */
if( count($argv)!=2 )
  die("Usage: php " . $argv[0] . " ditamap-file \n");

/* set the file separator for the operating system */
if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

/* the top directory we are going to cross-reference */
$ditamap = $argv[1];

print("Starting from ditamap " . $ditamap . "\n");

$map = basename($ditamap);
$dir = dirname($ditamap) . $fsep;
if( $dbg_flag )
  print("dir: " . $dir . " file: " . $map . "\n\n");

/*
  walk the map and get list of used files, and references
*/

$rc = get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                    $fused, $notfound, $lf, $rf, $tp, $rcon);

/* maybe input was not a map, nothing found */
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
  /* calculate list of unique directories used in the map */
  foreach($fused as $f)
  {
    if( !isURL($f) )
    {
      $dname = dirname($f);
      $dirlist[$dname]=$dname;
    }
  }

  /* find unused files in each directory used */
  foreach($dirlist as $d)
  {
    myscandir ($dbg_flag, $fsep, $d, $fused, $unused);
  }

  if( count($unused)>0 )
  {
    sort($unused);
    print("\n" . count($unused) . " unused files in directories used by this map: \n\n");
    foreach($unused as $uf)
    {
      print($uf . " , " . getDOCTYPE($uf) . " , " .getAuthor($uf) . "\n");
    }
  }

  $rdir = rootdir($dirlist); /* find root of all directories found */

  if( $dbg_flag )
    print("rootdir: " . $rdir . "\n");

  /* output list of directories used */
  sort($dirlist);
  print("\n" . count($dirlist) . " directories in this map: \n\n");
  foreach($dirlist as $d)
  {
    print($d . "\n");
  }


  print("\n" . count($fused) . " files and links in this map: \n\n");
  /* first sort the file array */
  sort($fused);
  foreach( $fused as $f)
  {
    if( isURL($f) )
      $fmt="00000000";
    else
      $fmt = date("Ymd", filemtime($f));

    $dt = getDOCTYPE($f);
    /* total up by doc type of file */
    if( isset($dt_tot[$dt]) )
      $dt_tot[$dt]++;
    else   
      $dt_tot[$dt]=1;

    print(fshort($f,$rdir) . " , " . $dt . " , " . getAuthor($f) . " , " .
          getSize($f) . " , " . $fmt . " , " . getdesc($f) . "\n");
  }

  /* print DOCTYPE totals */
  print("\nTotals by file type\n");
  print(" count file type \n");
  print("====== ========= \n");
  foreach( $dt_tot as $key=>$value )
  {
    printf("%6d %s \n", $value, $key);
  }

  /* print out all the references */
  print("\n" . count($lf) . " references in this map: \n\n");
  for($i=0 ; $i < count($lf); $i++)
  {
    $refp = $rf[$i];
    if( !isURL($refp) )
      if( $rf[$i] !== $lf[$i] )
      {
        $rp = realpath(dirname($lf[$i]) . $fsep . $refp);
        if( $rp !== FALSE )
          $refp = fshort($rp,$rdir);
      }
      else /* self reference */
        $refp = fshort($lf[$i],$rdir);
    print(fshort($lf[$i],$rdir) . " , " . $tp[$i] . " , " . $refp . $rcon[$i] . "\n");
  }
} /* get_map_lists worked */
else
{
  print("Error: failure walking ditamap.\n");
}

?>
