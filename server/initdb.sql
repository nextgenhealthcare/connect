INSERT INTO PERSON (USERNAME, PASSWORD) VALUES('admin', 'admin');
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('FTP Reader', 'org.mule.providers.ftp.FtpConnector', 'ftp', 'ByteArrayToString', 'LISTENER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('SFTP Reader', 'org.mule.providers.sftp.SftpConnector', 'sftp', 'ByteArrayToString', 'LISTENER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('JMS Reader', 'org.mule.providers.jms.JmsConnector', 'jms', 'JMSMessageToObject ObjectToString', 'LISTENER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('SOAP Listener', 'org.mule.providers.soap.axis.AxisConnector', 'axis', 'SOAPRequestToString', 'LISTENER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('File Reader', 'org.mule.providers.file.FileConnector', 'file', 'ByteArrayToString', 'LISTENER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('Database Reader', 'org.mule.providers.jdbc.JdbcConnector', 'jdbc', 'ResultMapToXML', 'LISTENER', 0, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('LLP Listener', 'org.mule.providers.tcp.TcpConnector', 'tcp', 'ByteArrayToString', 'LISTENER', 1, 0);

INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('FTP Writer', 'org.mule.providers.ftp.FtpConnector', 'ftp', '', 'SENDER', 1, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('JMS Writer', 'org.mule.providers.jms.JmsConnector', 'jms', 'MessageObjectToJMSMessage', 'SENDER', 1, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('SOAP Sender', 'org.mule.providers.soap.axis.AxisConnector', 'axis', '', 'SENDER', 1, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('PDF Writer', 'org.mule.providers.pdf.PdfConnector', 'pdf', '', 'SENDER', 1, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('File Writer', 'org.mule.providers.file.FileConnector', 'file', '', 'SENDER', 1, 1);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('Database Writer', 'org.mule.providers.jdbc.JdbcConnector', 'jdbc', '', 'SENDER', 1, 0);
INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('LLP Sender', 'org.mule.providers.tcp.TcpConnector', 'tcp', '', 'SENDER', 1, 1);

-- INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('HTTP Listener', 'org.mule.providers.http.HttpsConnector', 'http', 'HttpRequestToString', 'LISTENER');
-- INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('HTTPS Listener', 'org.mule.providers.http.HttpConnector', 'https', 'HttpRequestToString', 'LISTENER');
-- INSERT INTO TRANSPORT (NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS, TYPE, IS_INBOUND, IS_OUTBOUND) VALUES ('Email Sender', 'org.mule.providers.smtp.SmtpConnector', 'smtp', '', 'SENDER');