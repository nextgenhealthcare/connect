package com.mirth.connect.connectors.smtp;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
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

            int port = -1;
            try {
                port = Integer.parseInt(props.getProperty(SmtpSenderProperties.SMTP_PORT));
            } catch (NumberFormatException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid port: \"" + props.getProperty(SmtpSenderProperties.SMTP_PORT) + "\"");
            }

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

            try {
                for (String toAddress : StringUtils.split(to, ",")) {
                    email.addTo(toAddress);
                }
                
                email.setFrom(from);
                email.setMsg("Receipt of this email confirms that mail originating from this Mirth Connect Server is capable of reaching its intended destination.\n\nSMTP Configuration:\n- Host: " + host + "\n- Port: " + port);

                email.send();
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully sent test email to: " + to);
            } catch (EmailException e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, e.getMessage());
            }
        }

        return null;
    }

}
