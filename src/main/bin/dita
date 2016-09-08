#! /bin/sh

# This file is part of the DITA Open Toolkit project.
# See the accompanying LICENSE file for applicable license.

# Derived from Apache Ant command line tool.

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Extract launch and ant arguments, (see details below).
ant_exec_args=
for arg in "$@" ; do
  if [ my"$arg" = my"--h"  -o my"$arg" = my"--help"  ] ; then
    ant_exec_args="$ant_exec_args -h"
  else
    ant_exec_args="$ant_exec_args \"$arg\""
  fi
done

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=`/usr/libexec/java_home`
           fi
           ;;
  MINGW*) mingw=true ;;
esac

if [ -z "$DITA_HOME" -o ! -d "$DITA_HOME" ] ; then
  ## resolve links - $0 may be a link to ant's home
  PRG="$0"
  progname=`basename "$0"`

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  DITA_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  DITA_HOME=`cd "$DITA_HOME" > /dev/null && pwd`
fi

# Set environment variables
. "$DITA_HOME/resources/env.sh"

# Add build script to arguments
ant_exec_args="$ant_exec_args \"-buildfile\" \"$DITA_HOME/build.xml\" \"-main\" \"org.dita.dost.invoker.Main\""

# For Cygwin and Mingw, ensure paths are in UNIX format before
# anything is touched
if $cygwin ; then
  [ -n "$DITA_HOME" ] &&
    DITA_HOME=`cygpath --unix "$DITA_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi
if $mingw ; then
  [ -n "$DITA_HOME" ] &&
    DITA_HOME="`(cd "$DITA_HOME"; pwd)`"
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
fi

# set ANT_LIB location
ANT_LIB="${DITA_HOME}/lib"

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    # IBM's JDK on AIX uses strange locations for the executables
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/jre/bin/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/bin/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi


# use launcher to determine classpaths
if [ -z "$LOCALCLASSPATH" ] ; then
  LOCALCLASSPATH=$ANT_LIB/ant-launcher.jar
else
  LOCALCLASSPATH=$ANT_LIB/ant-launcher.jar:$LOCALCLASSPATH
fi

# For Cygwin, switch paths to appropriate format before running java
# For PATHs convert to unix format first, then to windows format to ensure
# both formats are supported. Probably this will fail on directories with ;
# in the name in the path. Let's assume that paths containing ; are more
# rare than windows style paths on cygwin.
if $cygwin; then
  if [ "$OS" = "Windows_NT" ] && cygpath -m .>/dev/null 2>/dev/null ; then
    format=mixed
  else
    format=windows
  fi
  [ -n "$DITA_HOME" ] && DITA_HOME=`cygpath --$format "$DITA_HOME"`
  ANT_LIB=`cygpath --$format "$ANT_LIB"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --$format "$JAVA_HOME"`
  LCP_TEMP=`cygpath --path --unix "$LOCALCLASSPATH"`
  LOCALCLASSPATH=`cygpath --path --$format "$LCP_TEMP"`
  if [ -n "$CLASSPATH" ] ; then
    CP_TEMP=`cygpath --path --unix "$CLASSPATH"`
    CLASSPATH=`cygpath --path --$format "$CP_TEMP"`
  fi
  CYGHOME=`cygpath --$format "$HOME"`
fi

# add a second backslash to variables terminated by a backslash under cygwin
if $cygwin; then
  case "$DITA_HOME" in
    *\\ )
    DITA_HOME="$DITA_HOME\\"
    ;;
  esac
  case "$CYGHOME" in
    *\\ )
    CYGHOME="$CYGHOME\\"
    ;;
  esac
  case "$LOCALCLASSPATH" in
    *\\ )
    LOCALCLASSPATH="$LOCALCLASSPATH\\"
    ;;
  esac
  case "$CLASSPATH" in
    *\\ )
    CLASSPATH="$CLASSPATH\\"
    ;;
  esac
fi
# Execute ant using eval/exec to preserve spaces in paths,
# java options, and ant args
ant_sys_opts=
if [ -n "$CYGHOME" ]; then
  ant_sys_opts="-Dcygwin.user.home=\"$CYGHOME\""
fi
ant_exec_command="exec \"$JAVACMD\" $ANT_OPTS -Djava.awt.headless=true -classpath \"$LOCALCLASSPATH\" -Dant.home=\"$DITA_HOME\" -Ddita.dir=\"$DITA_HOME\" -Dant.library.dir=\"$ANT_LIB\" $ant_sys_opts org.apache.tools.ant.launch.Launcher $ANT_ARGS -cp \"$CLASSPATH\""
eval $ant_exec_command "$ant_exec_args"
