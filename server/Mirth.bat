@ECHO OFF
SET CLASSPATH=mirth-launcher.jar
SET CLASSPATH=%CLASSPATH%;lib/log4j-1.2.13.jar
SET CLASSPATH=%CLASSPATH%;conf/
java -classpath %CLASSPATH% com.webreach.mirth.server.launcher.Launcher launcher.xml