MIRTH LINUX INSTALLATION INSTRUCTIONS
==================================================

To run the Mirth installer in GUI mode, run the following command:

java -jar mirth-[version]-setup.jar

To run the Mirth installer in automatic mode using a pre-configured installation script, run the following command:

java -jar mirth-[version]-setup.jar script.xml

To change the installation directory for the autoamted installer, edit the following element in the script.xml file:

<installpath>/opt/Mirth</installpath>

Note that you must have permission to write to the /opt directory. Mirth must be run by a user with the same permission as the one who installed it, otherwise you will have to run the following command:

chown [owner]:[group] /opt/Mirth -R