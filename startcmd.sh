#!/bin/sh
#  This file is part of the DITA Open Toolkit project hosted on 
#  Sourceforge.net. See the accompanying license.txt file for 
#  applicable licenses.
#  (c) Copyright IBM Corp. 2006 All Rights Reserved.

if  [ "${DITA_HOME:+1}" != "1" ]; then 
   echo "DITA_HOME environment variable is empty or not set";
   exit 127;
fi

cd "$DITA_HOME"

# Get the absolute path of DITAOT's home directory
DITA_DIR="`pwd`"

if [ -f "$DITA_DIR"/tools/ant/bin/ant ] && [ ! -x "$DITA_DIR"/tools/ant/bin/ant ]; then
chmod +x "$DITA_DIR"/tools/ant/bin/ant
fi

export ANT_OPTS="-Xmx512m $ANT_OPTS"
export ANT_OPTS="$ANT_OPTS -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl"
export ANT_HOME="$DITA_DIR"/tools/ant
export PATH="$DITA_DIR"/tools/ant/bin:"$PATH"

NEW_CLASSPATH="$DITA_DIR/lib:$DITA_DIR/lib/dost.jar:$DITA_DIR/lib/commons-codec-1.4.jar:$DITA_DIR/lib/resolver.jar:$DITA_DIR/lib/icu4j.jar"
NEW_CLASSPATH="$DITA_DIR/lib/saxon/saxon9.jar:$DITA_DIR/lib/saxon/saxon9-dom.jar:$NEW_CLASSPATH"
if test -n "$CLASSPATH"
then
export CLASSPATH="$NEW_CLASSPATH":"$CLASSPATH"
else
export CLASSPATH="$NEW_CLASSPATH"
fi

"$SHELL"
