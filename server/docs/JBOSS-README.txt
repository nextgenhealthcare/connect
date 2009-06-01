MIRTH CONNECT JBOSS INSTALLATION INSTRUCTIONS
==================================================

Mirth Connect has been successfully tested with JBoss version 4.2.0. To deploy Mirth Connect in JBoss:

1. Extract the mirth.sar directory into your deploy directory in JBoss. If it is renamed or copied anywhere else, mirth.sar/META-INF/jboss-service.xml will need to be modified.
2. It is mandatory that mirth.sar/conf/mirth.properties is modified. The http.port and jmx.port properties must be changed, because 8080 and 1099 are used by JBoss defaults.  

Recommended settings are:

http.port=8081
jmx.port=1097

3. When JBoss is started and shutdown, Mirth Connect should run alongside it.  You may visit the Mirth Connect Administrator page at the http.port specified above. A JBoss service MBean should now be deployed that will allow you to start and stop the Mirth Connect service from the JBoss Management Console.