<?php

/*
 * (c) Copyright VR Communications, Inc. 2007 All Rights Reserved.
 *
 * Author: Richard Johnson
 *
 * This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
 * See the accompanying license.txt file for applicable licenses.
 */

function show_usage()
{
  die("Usage: php " . "ditablog" . "  ditamap-file outdir pagedir imagedir\n");
}


/**************************************************************

 PHP script to post-process DITA xhtml output to create blog files.

**************************************************************/

include 'ditautil.inc';

/*
  Start of main program.
*/

$dbg_flag = false; /* control debug printing */
$ref_flag = false; /* check id references */

if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  $fsep = "\\";
else
  $fsep = "/";

$argc = count($argv);

/* pick up ditamap from arguments */
if( $argc < 5 )
  show_usage();

/* Collect all the arguments. */

$ditamap = $argv[1];
$outdir = $argv[2];
$pagedir = $argv[3];
$imagedir = $argv[4];

/* done processing arguments, display startup message */

print("\nditablog.php DITABlog utility:\n");
print("Starting from ditamap --- " . $ditamap . "\n");
printf("     output directory --- " . $outdir . "\n");
printf("       page directory --- " . $pagedir . "\n");
printf("      image directory --- " . $imagedir . "\n");

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
  print("\n" . count($fused) . " files and links are in this map. \n\n");
  /* first sort the file array */
  sort($fused);

  foreach( $fused as $f)
  {
    if( !isURL($f) )
    {
      $dname = dirname($f);
      $dirlist[$dname]=$dname;
    }
  }

  $rdir = rootdir($dirlist); /* find root of all directories found */

  if($dbgflag)
    print("rootdir: " . $rdir . "\n");

  foreach( $fused as $f)
  {
    if( isImage($f) )
    {
      printf("Referenced image: " . fshort($f,$rdir) . "\n");
    }

    $dt = getDOCTYPE($f);
    if( isDITA($f) && ($dt!="map") )
    {
      /* create filepath to xhtml output file and read it into a string */
      $fs = fshort($f,$rdir);
      $path_parts = pathinfo($f);
      $pdir = $path_parts['dirname'];
      $bname = $path_parts['basename'];
      $bfname = substr($bname,0, strpos($bname,'.'));
      $ppp = explode("\\",$pdir);
      $bdir = $ppp[count($ppp)-1];
      $fnametype = $bfname . ".html";
      $fdirnametype = $bdir . $fsep . $fnametype;
      $ofdirnametype = $pagedir . $fsep . $bfname . ".html";

      $fl = $outdir . $fsep . $fdirnametype;

      /* read entire file into a string */
      $s = file_get_contents($fl);

      if( $s !== FALSE )
      {
        /* setup image directory substitutions */
        $sstring = '../images';
        $dstring = '../ditablog_images';

        print("Post processing: " . $bdir . $fsep . $fnametype . "\n");

        if($dbgflag)
          print("Replacing " . '"' .$sstring . '"' . " -> " . 
                      '"' . $dstring . '"' . " in file " . $ofdirnametype . "\n");

        $sr = str_replace($sstring, $dstring, $s);

        /* add plug for DITA */
        $blurb='<p><span class="caption"><span class="emph"><span class="strong">poweredbyDITA&#8482;</span><br/> The following content was created using DITA/XML and processed using DITA Open Toolkit to facilitate reuse.</span></span></p>';

        $sr = str_replace('<div>', $blurb . "\n\n" . '<div>', $sr);
        $sr = str_replace("DITAOTUG_CSS","VRCommCSS", $sr);


        /* write the changed page file to new location */
        $rc = file_put_contents($ofdirnametype, $sr);
      }
      else
      {
        printf("Error reading file %s \n", $fl);
      }
    }
  }

} /* get_map_lists worked */
else
{
  print("Error: failure walking ditamap.\n");
}

?>
