set echo off

SET CLASSPATH=classes

rem jaxb lib
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxb\lib\jaxb-api.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxb\lib\jaxb-impl.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxb\lib\jaxb-libs.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxb\lib\jaxb-xjc.jar"

rem shared lib
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\activation.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\commons-beanutils.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\commons-collections.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\commons-digester.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\commons-logging.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\jaas.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\jax-qname.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\jta-spec1_0_1.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\mail.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\namespace.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\relaxngDatatype.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\xmlsec.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jwsdp-shared\lib\xsdlib.jar"

rem jaxp lib
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxp\lib\jaxp-api.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxp\lib\endorsed\dom.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxp\lib\endorsed\sax.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxp\lib\endorsed\xalan.jar"
SET CLASSPATH=%CLASSPATH%;"C:\Sun\jwsdp-1.6\jaxp\lib\endorsed\xercesImpl.jar"

java -Djava.endorsed.dirs="C:\Sun\jwsdp-1.6\jaxp\lib;C:\Sun\jwsdp-1.6\jaxp\lib\endorsed" -classpath %CLASSPATH% Main