#!/bin/sh
#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

#
# Make sure that JAXB_HOME and JAVA_HOME are set
#
if [ -z "$JAXB_HOME" ]
then
    # search the installation directory
    
    PRG=$0
    progname=`basename $0`
    saveddir=`pwd`
    
    cd `dirname $PRG`
    
    while [ -h "$PRG" ] ; do
        ls=`ls -ld "$PRG"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '.*/.*' > /dev/null; then
            PRG="$link"
        else
            PRG="`dirname $PRG`/$link"
        fi
    done
    
    JAXB_HOME=`dirname "$PRG"`/..
    
    # make it fully qualified
    cd "$saveddir"
    JAXB_HOME=`cd "$JAXB_HOME" && pwd`
    
    cd $saveddir
fi

JAXP_HOME=$JAXB_HOME/../jaxp
ENDORSED_DIRS=$JAXP_HOME/lib:$JAXP_HOME/lib/endorsed


[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    JAXB_HOME=`cygpath -w ${JAXB_HOME}`
    ENDORSED_DIRS="`cygpath -w -p "$ENDORSED_DIRS"`"
}

if [ -n "$JAVA_HOME" ]
then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA=java
fi

$JAVA $XJC_OPTS "-Djava.endorsed.dirs=$ENDORSED_DIRS" -jar "$JAXB_HOME/lib/jaxb-xjc.jar" "$@"
