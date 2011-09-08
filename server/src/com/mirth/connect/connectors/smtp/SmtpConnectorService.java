package com.mirth.connect.connectors.smtp;

import java.util.Properties;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.util.ConnectionTestResponse;

public class SmtpConnectorService implements ConnectorService {

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        if (method.equals("sendTestEmail")) {
            Properties props = (Properties) object;
            
            String host = props.getProperty(SmtpSenderProperties.SMTP_HOST);
            int port = Integer.parseInt(props.getProperty(SmtpSenderProperties.SMTP_PORT));
            String secure = props.getProperty(SmtpSenderProperties.SMTP_SECURE);
            
            boolean authentication = false;
            
            if ("1".equals(props.getProperty(SmtpSenderProperties.SMTP_AUTHENTICATION))) {
                authentication = true;
            }
            
            String username = props.getProperty(SmtpSenderProperties.SMTP_USERNAME);
            String password = props.getProperty(SmtpSenderProperties.SMTP_PASSWORD);
            String to = props.getProperty(SmtpSenderProperties.SMTP_TO);
            String from = props.getProperty(SmtpSenderProperties.SMTP_FROM);
            
            Email email = new SimpleEmail();
            email.setDebug(true);
            email.setHostName(host);
            email.setSmtpPort(port);
            
            if ("SSL".equalsIgnoreCase(secure)) {
                email.setSSL(true);
            } else if ("TLS".equalsIgnoreCase(secure)) {
                email.setTLS(true);
            }

            if (authentication) {
                email.setAuthentication(username, password);
            }

            email.setSubject("Mirth Connect Test Email");
            email.addTo(to);
            email.setFrom(from);
            email.setMsg("This is a test email from Mirth Connect.");
            
            try {
                email.send();
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully sent test email.");
            } catch (EmailException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, e.getMessage());
            }
        }

        return null;
    }

}
