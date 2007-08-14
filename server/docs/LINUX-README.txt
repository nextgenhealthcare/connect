MIRTH LINUX INSTALLATION INSTRUCTIONS
==================================================

To run the Mirth installer in GUI mode, run the following command:

java -jar mirth-[version]-setup.jar

To run the Mirth installer in automatic mode using a pre-configured installation script, run the following command:

java -jar mirth-[version]-setup.jar script.xml

To change the installation directory for the autoamted installer, edit the following element in the script.xml file:

<installpath>~/Mirth</installpath>