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

 PHP script to list authoring statistics for a ditamap.

**************************************************************/

include 'ditautil.inc';

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

if( $rc )
{
  print(count($fused) . " files used in " . $ditamap . "\n\n");

  $ncop=0;
  $nauth=0;
  $ncont=0;

  /* get copyright holder and author in the files we found */
  foreach($fused as $f)
  {
    if( !isURL($f) && !isIMAGE($f) )
    {
      $xml = @simplexml_load_file($f);
      if( $xml != FALSE )
      {
        /* copyright holder statistics */
        foreach( $xml->xpath('//copyrholder') as $ch )
        {
          $ncop++;
          $chr = (string)$ch; /* copyright holder */
          if( isset( $chold[$chr] ) )
            $chcnt[$chr]++;
          else
          {
            $chold[$chr]=$chr;
            $chcnt[$chr]=1;
          }
        }

        /* author statistics */
        foreach( $xml->xpath('//author[@type="creator"]') as $au )
        {
          $nauth++;
          $aut = (string)$au; /* author creator */
          if( isset( $authr[$aut] ) )
            $aucnt[$aut]++;
          else
          {
            $authr[$aut]=$aut;
            $aucnt[$aut]=1;
          }
        }

        /* contributor statistics */
        foreach( $xml->xpath('//author[@type="contributor"]') as $au )
        {
          $ncont++;
          $aut = (string)$au; /* author contributor */
          if( isset( $contr[$aut] ) )
            $contcnt[$aut]++;
          else
          {
            $contr[$aut]=$aut;
            $contcnt[$aut]=1;
          }
        }

      } /* file was parsed */
    }
  }

  if($ncop>0)
  {
    print(" count copyright holder\n");
    print(" ===== ========================\n");
    foreach($chold as $xc)
      printf("%6d %s \n", $chcnt[$xc], $xc);
    print("\n");
  }

  if($nauth>0)
  {
    print(" count author          \n");
    print(" ===== ========================\n");
    foreach($authr as $xc)
      printf("%6d %s \n", $aucnt[$xc], $xc);
  }

  if($ncont>0)
  {
    print("\n");
    print(" count contributor     \n");
    print(" ===== ========================\n");
    foreach($contr as $xc)
      printf("%6d %s \n", $contcnt[$xc], $xc);
  }


} /* get_map_lists worked */
else
{
  print("Error: failure walking ditamap.\n");
}

?>
