<?php

/*
 * (c) Copyright VR Communications, Inc. 2006 All Rights Reserved.
 *
 * Author: Richard Johnson, www.vrcommunications.com
 *
 * This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
  Included common routines used by DITA debugging
  and reporting tools.
*/
 
function my_simplexml_load_file($f)
{
  /* optional debugging output */
  if( $dbg_flag )
  {
    print("**my-simplexml_load " . $f . "\n");
  }

  $s=@file_get_contents($f);
  if($s === FALSE )
  {
    $xml=$s;
    print("Error, could not read file " . $f . "\n");
  }
  else
  {
    if(strlen($s)>0)
    {
      $s=str_replace("><",">\n<",$s);
      $xml=simplexml_load_string($s);
    }
    else
      $xml=FALSE;
  }

  return $xml;

}

/*
  Scan for unused files in a directory
*/
function myscandir ($dbg_flag, $fsep, $dir, &$fused, &$unused)
{

  /* optional debugging output */
  if( $dbg_flag )
  {
    print("**scandir " . $dir . "\n");
  }

  if (is_dir($dir)) 
  {
    /* if this is a directory, look for files in it that are not used by the ditamap */
    if ($dh = opendir($dir)) 
    {
      while (($file = readdir($dh)) !== false) 
      {
        $sdir = $dir . $fsep . $file;
        $sdir = realpath($sdir);
        $ft = filetype($sdir);
        if( $ft == "file" )
        {
          if ($file != "." && $file != "..") 
          { 
            if( !isset($fused[$sdir]) )
            {
              if( $dbg_flag )
                print("Unused file: " . $sdir . " != " . $fused[$sdir] . "\n");
              $unused[$sdir] = $sdir;
            }
          } /* not parent dir */
        }
      } /* loop through directory contents */
      closedir($dh);
    } /* we could open the directory */
  } /* it is a directory */
}

/*
  Scan a file for references to other DITA files.
*/
function myscanfile ($dbg_flag,$ref_flag, $fin,$ids,&$ref,&$reftype,&$refcon,&$fused,&$notfound)
{
  /*
   fin = file to be scanned for references
   ids = optional additional ids in the reference
  */

  if( $dbg_flag )
  {
    print("**myscanfile " . $fin . " ids " . $ids . "\n");
  }
 
  $rc=0; /* default to nothing found */

  if( !isDITA($fin) )
  {
    /* Not a DITA source file */
    if( isURL($fin) )
    {
      /* save http:/https: "file" */
      $fused[$fin] = $fin;
      if( $dbg_flag )
        print("ignoring URL " . $fused[$fin] . "\n");
    }
    else
    {
      /* file type other than DITA source */
      $finr = realpath($fin);
      if( $finr === FALSE )
      {
        /* referenced file does not exist */
        print("Error, file " . $fin . " does not exist!\n");
        $rc = -1;
        return $rc;
      }
      else
      {
        /* file exists, but we ignore it */
        $fused[$finr] = $finr;
        if( $dbg_flag )
          print("ignoring " . $finr . " " . $fused[$finr] . "\n");
      }
    }
    return $rc;
  }

  $f = realpath($fin);
  if( $f === FALSE )
  {
    /* referenced file does not exist */
    print("Error, file " . $f . " does not exist!\n");
    $rc = -1;
    return $rc;
  }

  if( $dbg_flag )
  {
    print("**myscanfile realpath " . $f . "\n");
  }

  if( (PHP_OS=="WIN32") || (PHP_OS=="WINNT") )
  {
    /* If running in Windows, check for possible case mismatch problem on this input filepath.
       If not detected, this can cause various mysterious build behaviors.  */
    if( strpos($fin,"..")!= FALSE )
    {
      $fr = realpath($fin);
      $pp = pathinfo($fr);
      $pdir = $pp['dirname'];
      $pbase = $pp['basename'];
      /* get all the parts of the directory */
      $ppp = explode("\\",$pdir);
      $sdir = "";
      /* check that each directory contains the next */
      for($k=0; $k<count($ppp)-1; $k++)
      {
        $sdir = $sdir . $ppp[$k];
        $sdir2 = $ppp[$k+1];
        $dfiles = @scandir($sdir);
        $dfound = false;
        if( $dfiles )
        {
          for($m=0; $m<count($dfiles); $m++)
          {
            if( $dfiles[$m] == $sdir2 )
            {
              /* the subdirectory is found */
              $dfound = true;
              break;
            }
          }
        }
        if( !$dfound )
        {
          /* looks like a name folding error */
          print("Error, directory " . $sdir2 . " not found in directory " . $sdir . "\n");
          $rc = -1;
          return $rc;
        }
        $sdir = $sdir . "\\";
      } /* check all directory names */

      /* now check the name of the file */
      $dfiles = @scandir($pdir);
      $ffound = false;
      if( $dfiles )
      {
        for($m=0; $m<count($dfiles); $m++)
        {
          if( $pbase === $dfiles[$m] )
          {
            /* we found the file in the directory */
            $ffound=true;
            break;
          }
        } /* check is filename is OK */
      }
      if( !$ffound )
      {
        /* looks like a name folding error */
        print("Error, file " . $pbase . " not found in directory " . $pdir . "\n");
        $rc = -1;
        return $rc;
      }
    } /* check for name folding error */
  } /* we are running in Windows */

  if( !file_exists($f) )
  {
    /* this referenced file does not exist */
    $notfound[]=$fin;
    if($ref_flag)
      print("Error, file " . $fin . " does not exist!\n");
    $rc = -1;
    return $rc;
  }

  if( isset($fused[$f]) )
  {
    /* file already was scanned, check any conref ids supplied */
    if( $ref_flag && strlen($ids) > 0 )
    {
      /* validate id reference(s) */
      /* try to load and parse the file */
      $xml = @my_simplexml_load_file($f);
      if( $xml != FALSE ) /* could we parse it? */
      {
        /* search for matching ids */
        parse_ids($dbg_flag, $ids,$idt,$ide); /* parse id string */
        $rcid = check_ids($dbg_flag, $xml, $idt, $ide); /* check the reference */
        if( !$rcid )
        {
          print("Error: bad id reference " . $f . $ids . "\n");
          $rc = -1;
        }
      }
      else
      {
        if( $dbg_flag )
          print("Error, file " . $f . " could not be loaded or parsed!\n");
      }
    } /* id is present */
    return $rc;
  } /* file already scanned for references */

  $fused[$f] = $f;

  /* We check for "conref", "href", "object data" attributes */
  $refs = array('//object','//@href','//@conref'); /* what we look for */
  $refts = array("data","href","conref"); /* what we call this kind of reference */
  $nrefs = count($refs);

  /* try to load and parse the file */
  $xml = @my_simplexml_load_file($f);

  if( $xml != FALSE ) /* could we parse it? */
  {

    if( $ref_flag && strlen($ids)>0 )
    {
      /* search for matching ids */
      parse_ids($dbg_flag, $ids,$idt,$ide); /* parse id string */
      $rcid = check_ids($dbg_flag, $xml, $idt, $ide); /* check the reference */
      if( !$rcid )
      {
        print("Error: bad id reference " . $f . $ids . "\n");
      }
    }

    /* loop on types of references */
    for($i = 0; $i < $nrefs; $i++)
    {
      $el = $refs[$i];

      /* loop through elements we found and return what is being referenced */
      foreach ( $xml->xpath($el) as $elmt )
      {
        $idref="";
        $reftxt=$refts[$i];
        if( $i == 0 ) // special case for object
        {
          $xrf = trim($elmt['codebase']) . " " . trim($elmt['data']);
        }
        else // others have a single attribute
        {
          $xrf = trim($elmt);
        }

        /* debug - show the reference found */
        if( $dbg_flag )
          print(" found reference = " . $xrf . "\n");

        if( $xrf != "" )
        {
          /* there is a reference */
          if( substr($xrf,0,1) != "#" )
          {
            /* reference is to another file */
            $idref="";
            $pos = strpos($xrf, '#');
            if( $pos != FALSE )
            {
              /* the reference contains id(s) */
              $idref=substr($xrf,$pos);
              $xrf = substr($xrf,0,$pos);
              if( $dbg_flag )
                print("parsing " . $el . " idref " . $idref . " from " . $xrf . "\n");
            }
            if( $dbg_flag )
              print( $f . " (" . $el . ") -> " . $xrf . " idref " . $idref . "\n");
            $ref[$rc] = $xrf;
            $reftype[$rc] = $reftxt;
            $refcon[$rc] = $idref;
            $rc++;
          }
          else
          {
            /* a reference to this file, check it right now */
            $ref[$rc] = $f;
            $reftype[$rc] = $reftxt;
            $refcon[$rc] = $xrf;
            if( $dbg_flag )
              print( $f . " (" . $el . ") -> " . $f . " self idref " . $xrf . "\n");
            $rc++;

            if( $ref_flag && strlen($xrf)>0 )
            {
              parse_ids($dbg_flag, $xrf,$idt,$ide); /* parse id string */
              $rcid = check_ids($dbg_flag, $xml, $idt, $ide); /* check the reference */
              if( !$rcid )
              {
                print("Error: bad internal id reference " . $f . $xrf . "\n");
              }
            }
          }
        } /* non-null ref */
        else
        {
          if( $dbg_flag )
            print("Note: " . $f . " " . $el . " has NULL reference\n");
        }
      } /* element loop */
    }
  } /* load OK */
  else
  {
    if( $dbg_flag )
      print("Error, file " . $f . " could not be loaded or parsed!\n");
  }

  return $rc; /* return count of references found */
}

/*
  Check conref id(s) for validity.
*/
function check_ids($dbg_flag, $xml, $idtopic, $idelem)
{
  $rc=true;

  if($dbg_flag)
    print("**check_ids: " . $idtopic . " " . $idelem . "\n");
  /* search for matching ids */
  if( strlen($idtopic)>0 )
  {
    $idtopicflag=false;
    $idelemflag=false;
    foreach( $xml->xpath('//@id') as $xid)
    {
      if($dbg_flag)
        print("**check_ids compare " . $xid . " to " . $idtopic . " and " . $idelem . "\n");
      if( $xid == $idtopic )
        $idtopicflag=true;

      if( strlen($idelem)>0 )
        if( $xid == $idelem )
          $idelemflag=true;

      if( $idtopicflag && $idelemflag )
        break;
    }
    if($dbg_flag)
      print("**check_ids flags " . $idtopicflag . " and " . $idelemflag . "\n");

    if( !$idtopicflag || ( (strlen($idelem)>0) && !$idelemflag ) )
    {
      if($idtopicflag==false)
      {
        print("topic id " . $idtopic . " not found.\n");
      }
      else
      {
        if($idelemflag==false)
          print("element id " . $idelem . " not found.\n");
      }
      /* one or more ids not found! */
      $rc = false;
    }
    else
      $rc = true;
  }

  if($dbg_flag)
    print("**check_ids returns " . $rc . "\n");
  return $rc;
}

/* 
  parse id reference into topic and element parts 
*/
function parse_ids($dbg_flag, $ids, &$idtopic, &$idelem)
{
  $rc=true;


  if($dbg_flag)
    print("**parse_ids: " . $ids . "\n");

  $idtopic="";
  $idelem="";
  if( strlen($ids)>0 )
  {
    if( substr($ids,0,1) == '#' )
    {
      $wstr = substr($ids,1);
      $spos = strpos($wstr,'/');
      if( $spos != false )
      {
        $idtopic = substr($wstr,0, $spos);
        $idelem  = substr($wstr, $spos+1);
      }
      else
      {
        $idtopic = $wstr;
      }
    }
    else
    {
      print("Invalid id string " . $fin . " " . $ids . "\n");
      $rc=false;
    }
  }
  return $rc;
}

/*
  Find DTD used by an XML document
*/
function getDOCTYPE($f)
{
  static $dtsave;

  /* if value is cached, just return it */
  if( isset($dtsave[$f]) )
    return $dtsave[$f];

  global $dbg_flag;
  if( $dbg_flag )
    print("**getDOCTYPE " . $f . "\n");
  $rc="*None*";

  /* these are not files */
  if( isURL($f) )
  {
    if( substr_count($f,"http:")>0 )
      $rc = "http:";
    if( substr_count($f,"https:")>0 )
      $rc = "https:";
    else 
      $rc = "*URL*";
  }
  else
  {
    if( file_exists($f) )
    {
      $parts=pathinfo($f);
      $rc=$parts['extension'];
      $handle = fopen($f,"r");
      /* look for DOCTYPE line in the file */
      for($i=0; ($i<100) && (!feof($handle));$i++)
      {
        $buffer = fgets($handle, 4096);
        $tok = strtok($buffer," ");
        if( $tok == "<!DOCTYPE" )
        {
          /* we found a DOCTYPE, return the value */
          $tok = strtok(" ");
          $rc = trim($tok);
          break;
        }
      }
      fclose($handle);
    }
    else
    {
      if( $dbg_flag )
        print("Error in getDOCTYPE, file " . $f . " does not exist.\n");
      $rc="*no-file*";
    }
  }

  $dtsave[$f]=$rc; /* cache value */
  return $rc;
}

/*
  Main workhorse routine that finds files, references, and files not
  found in a ditamap.
*/
function get_map_lists($dbg_flag, $ref_flag, $ditamap, $fsep,
                       &$fused, &$notfound, &$lf, &$rf, &$tp, &$rcon)
{
  $getrc=true;

  /*
     Begin by finding what is referenced in the input ditamap.
  */
  $rc   = myscanfile($dbg_flag,$ref_flag, $ditamap,"",$ref,$reftype,$refcon,$fused,$notfound);
  if(   $rc == 0 )
  {
    print("No references found in " . $ditamap . "\n");
    $getrc=false;
    return $getrc;
  }

  $fused[$ditamap]   = $ditamap;

  if( $dbg_flag )
    print($rc . " references found in the ditamap.\n");

  /* store the first set of references */
  for($i=0;   $i<count($ref); $i++)
  {
    $lf[$i] = $ditamap;
    $rf[$i] = $ref[$i];
    $tp[$i] = $reftype[$i];
    $rcon[$i] = $refcon[$i];
  }

  if( $dbg_flag )
     print("Base reference count is " . count($lf) . "\n");
  $gcnt   = count($lf);

  /*
     Now look at further references until no more can be found.
  */

  $found = true;
  $rstart=0;
  $rlast=count($lf);
  while($found)
  {
    $found=false;
    if( $dbg_flag )
      print("** " . $rstart . " to " . $rlast . "\n");

    for($k=$rstart; $k<$rlast; $k++)
    {
      if( $rf[$k] !== $lf[$k] )
      {
        $sf = $rf[$k];
        $dname = dirname($lf[$k]) . $fsep;
        if( isURL($sf) )
          $dname="";
        $ffull = $dname . $sf;
         /* look for more references in this file */
         $rc = myscanfile($dbg_flag,$ref_flag, $dname . $sf,$rcon[$k],$ref,$reftype,$refcon,$fused,$notfound);
        if( $rc > 0 )
        {
          /* the file contained some more references */
          $found = true;
          for($i=0; $i<$rc; $i++)
          {
            $found = true;
            $rfull = $dname . $sf;
            $rrfull = realpath($rfull);
            if( $rrfull === FALSE )
              $lf[$gcnt] = $rfull;
            else
              $lf[$gcnt] = $rrfull;
            $rf[$gcnt] = $ref[$i];
            $tp[$gcnt] = $reftype[$i];
            $rcon[$gcnt] = $refcon[$i];
            $gcnt++;
          } /* loop on references */
        } /* some references were found */
        else if( $rc == -1)
        {
          /* Handle reference not found error. Show where it was referenced. */
          if( $ref_flag)
            print("  Bad reference: " . $lf[$k] . " => " . $rf[$k] . "\n");
        } /* not self reference */
      } /* self reference was already checked */
    }
    if( $found )
    {
      /* we have more scanning to do */
      $rstart = $rlast;
      $rlast=count($lf);
    }
  }  /* while loop */

  return $getrc;
}

/*
  Find longest string in an array
*/
function max_str_len($ar, &$padding)
{
  $rc=0;
  foreach($ar as $a)
    if( strlen($a)>$rc ) $rc=strlen($a);

  $padding="";
  for($i=0; $i<$rc; $i++)
    $padding = $padding . " ";

  return $rc;
}

/*
  get all ids from a file
*/
function get_ids($dbg_flag, $f, &$idfile, &$idid)
{
  $rc=0;

  if($dbg_flag)
    print("**get_ids: " . $f . "\n");

  /* try to load and parse the file */
  $xml = @my_simplexml_load_file($f);
  if( $xml != FALSE ) /* could we parse it? */
  {
    foreach( $xml->xpath('//@id') as $ids )
    {
      $rc++;
      $idfile[] = $f;
      $idid[]   = $ids;
    }
  }
  else
  {
    print("Error: could not parse " . $f . "\n");
    $rc=-1;
  }

  return $rc;
}

/*
  return true if this file is a URL
*/
function isURL($refp)
{
  /* check for "file" that is really a URL */
  if( (substr_count($refp,"http:")==0) && 
      (substr_count($refp,"mailto:")==0) && 
      (substr_count($refp,"https:")==0) )
    $rc=false;
  else
    $rc=true;

  return $rc;
}

/*
  return true if this file is an image
*/
function isIMAGE($f)
{
  $parts = pathinfo($f);
  $ext = $parts['extension'];
  if( ($ext=="gif") ||
      ($ext=="png") ||
      ($ext=="jpg") )
    $rc=true;
  else
    $rc=false;
  return $rc;
}

/*
  return true if file is some kind of map
*/
function ismap($f)
{
  $dt=getDOCTYPE($f);

  if( ($dt=="map") || ($dt=="chapter") )
    $rc=true;
  else
    $rc=false;

  return $rc;
}

/*
  return true if this file is a DITA source file
*/
function isDITA($f)
{
  $parts = pathinfo($f);
  $ext = @$parts['extension'];

  $rc = false;

  if( ($ext=="xml") ||
      ($ext=="ditamap") ||
      ($ext=="dita") )
    $rc=true;

  return $rc;
}

/* 
  find root of a set of directories 
*/
function rootdir($d)
{
  $root="";
  if( count($d)== 0 )
  {
    print("Error finding root of empty directory set.\n");
    return $root;
  }
  
  foreach($d as $dir)
  {
    if( $root == "" )
      $root = $dir;
    else
    {
      if( strpos($root, $dir)==0 )
        $root = $dir;
    }

    return $root;
  }
}

/* 
  return abbreviated file path based on root directory 
*/
function fshort($f, $rdir)
{
  global $dbg_flag;
  if($dbg_flag)
    print("**fshort " . $f . " dir " . $rdir . "\n");
  $rc=$f;

  if( !isURL($f) )
  {
    $sp = strpos($f, $rdir);
    if( $sp===0 )
      $rc=substr($f,strlen($rdir));
  }

  return $rc;
}

/* 
  get title description for a topic 
*/
function getdesc($f)
{
  global $dbg_flag;
  if($dbg_flag)
    print("**getdesc " . $f . "\n");
  $sd="*no-title*";

  if( !isURL($f) && !isIMAGE($f) )
  {
    /* try to load and parse the file */
    $xml = @my_simplexml_load_file($f);
    if( $xml != FALSE ) /* could we parse it? */
    {
      foreach( $xml->xpath('//title') as $xsd)
      {
        $sd=trim(strtr($xsd,","," "));
        break;
      }
    }
  }

  return $sd;
}

/* 
  get first author for a topic 
*/
function getAuthor($f)
{
  global $dbg_flag;
  if($dbg_flag)
    print("**getAuthor " . $f . "\n");

  $auth="*no-author*";

  if( !isURL($f) && !isIMAGE($f) )
  {
    /* try to load and parse the file */
    $xml = @my_simplexml_load_file($f);
    if( $xml != FALSE ) /* could we parse it? */
    {
      foreach( $xml->xpath('//author[@type="creator"]') as $xauth)
      {
        $auth=strtr($xauth,","," ");
        break;
      } /* author loop */
    } /* file was parsed */
  } /* might be XML */

  return $auth;

}

/* 
  get file size in bytes 
*/
function getSize($f)
{
  global $dbg_flag;
  if($dbg_flag)
    print("**getSize " . $f . "\n");
  $sz=0;
  

  if( !isURL($f) )
  {
    $handle = fopen($f,"r");
    if( $handle !== FALSE )
    {
      $fstat = fstat($handle);
      $sz = $fstat['size'];
      fclose($handle);
    }
  }

  return $sz;
}

?>
