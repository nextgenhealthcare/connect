#!/bin/sh
CLASSPATH=mirth-launcher.jar
CLASSPATH=$CLASSPATH:lib/log4j-1.2.13.jar
CLASSPATH=$CLASSPATH:conf/
java -Xmx256m -classpath $CLASSPATH com.webreach.mirth.server.launcher.MirthLauncher mirth-launcher.xml