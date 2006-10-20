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

 PHP script to verify all external links used by a ditamap.

**************************************************************/

include 'ditautil.inc';

/*
  check that a URL appears to be valid
*/
function verify_link($dbg_flag, $f)
{
  $rc=true;
  if( $dbg_flag )
    print("**verify_link: " . $f . "\n");

  $url = parse_url($f);

  $host = $url['host'];
  if($dbg_flag)
  {
    print("hostname: " . $host . " path: " . $url['path'] . "\n");
  }

  $ip = gethostbyname($host);
  if( $ip == $host )
  {
    /* this host does not exist! a definite error */
    print("Host " . $host . " invalid for " . $f . " .\n");
    $rc=false;
  }
  else
  {
    $file = @fopen($f,"r");
    if( !$file )
    {
      print("URL " . $f . " open failed. URL may not exist.\n");
      $rc=false;
    }
    else
      fclose($file);
  }

  return $rc;
}

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = false; /* check id references */

/* pick up ditamap from arguments */
if( count($argv)!=2 )
  die("Usage: php " . $argv[0] . " ditamap-file \n");

if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

/* the top directory we are going to check */
$ditamap = $argv[1];

print("Verify URLs from ditamap " . $ditamap . "\n");

$map = basename($ditamap);
$dir = dirname($ditamap) . $fsep;

/*
  walk the map and get list of used files, and references
*/

$rc = get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                    $fused, $notfound, $lf, $rf, $tp, $rcon);

if( $rc )
{
  print(count($fused) . " files used in " . $ditamap . "\n\n");

  $lc=0;
  $lfail=0;

  /* verify all URLs found */
  foreach($fused as $f)
  {
    if( isURL($f) )
    {
      $lc++;
      $lrc = verify_link($dbg_flag, $f);
      if( !$lrc )
      {
        $lfail++;
      }
    }
  }

  print("\n" . $lc . " links tested \n");
  print($lfail . " links failed verification \n");
} /* get_map_lists worked */
else
{
  print("Error: failure processing ditamap.\n");
}

?>
