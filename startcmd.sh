#!/bin/bash
cd $DITA_HOME

# Get the absolute path of DITAOT's home directory
DITA_DIR=$PWD

chmod +x $DITA_DIR/tools/ant/bin/ant

export ANT_HOME=$DITA_DIR/tools/ant
export PATH=$PATH:$DITA_DIR/tools/ant/bin
export CLASSPATH=$CLASSPATH:$DITA_DIR/lib/dost.jar:$DITA_DIR/lib/fop.jar:$DITA_DIR/lib/avalon-framework-cvs-20020806.jar:$DITA_DIR/lib/batik.jar:$DITA_DIR/lib/xalan.jar:$DITA_DIR/lib/xercersImpl.jar:$DITA_DIR/lib/serializer.jar:$DITA_DIR/lib/xml-apis.jar:$DITA_DIR/lib/icu4j.jar

bash
