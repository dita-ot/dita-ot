<?php

/*
 * (c) Copyright VR Communications, Inc. 2006 All Rights Reserved.
 *
 * Author: Richard Johnson, www.vrcommunications.com
 *
 * This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
 * See the accompanying license.txt file for applicable licenses.
 */

function cmp($aa, $bb) 
{
    $a=strtoupper($aa);
    $b=strtoupper($bb);

    if ($a == $b) {
        return 0;
    }
    return ($a < $b) ? -1 : 1;
}

/**************************************************************

 PHP script to list keyword statistics for a ditamap.

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

  /* find all the keywords in the file metadata */
  foreach($fused as $f)
  {
    if( !isURL($f) && !isIMAGE($f) )
    {
      $xml = @simplexml_load_file($f);
      if( $xml != FALSE )
      {
        /* keyword statistics */
        foreach( $xml->xpath('//keyword') as $ch )
        {
          $chr = (string)$ch;
          if( isset( $keywd[$chr] ) )
            $kycnt[$chr]++;
          else
          {
            $keywd[$chr]=$chr;
            $kycnt[$chr]=1;
          }
          $kc=$kycnt[$chr];
          $keywdf[$chr][$kc]=$f;
        } /* loop on keywords */
      } /* file was parsed */
    }
  }

  /* sort case insensitive */
  usort($keywd,"cmp");
  print(" count keyword string                      file\n");
  print(" ===== =================================== ==========\n");
  
  foreach($keywd as $xc)
  {
    $nf=0;
    foreach( $keywdf[$xc] as $ff )
    {
      if( $nf<1 )
        printf("%6d %s %s\n", $kycnt[$xc], substr(str_pad($xc,35),0,35) , $ff);
      else
        printf("                                           %s\n", $ff);
      $nf++;
    }
  }


} /* get_map_lists worked */
else
{
  print("Error: failure walking ditamap.\n");
}

?>
