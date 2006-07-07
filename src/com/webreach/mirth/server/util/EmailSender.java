package com.webreach.mirth.server.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class EmailSender {
	private Logger logger = Logger.getLogger(this.getClass());
	private String host;
	private int port;
	private String username;
	private String password;

	public EmailSender(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public void sendEmail(String to, String cc, String from, String subject, String body) {
		try {
			Properties props = System.getProperties();

			// attaching to default Session, or we could start a new one --
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);

			Session session = Session.getDefaultInstance(props, null);

			// create a new message
			Message message = new MimeMessage(session);

			// set the FROM and TO fields
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

			// include CC recipients
			if (cc != null) {
				message.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc, false));	
			}

			// set the subject and body text
			message.setSubject(subject);
			message.setText(body);

			// set some other header information
			message.setSentDate(new Date());

			// send the message
			Transport transport = session.getTransport("smtp");
			transport.connect(host, username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			logger.warn("Could not send email message.", e);
		}
	}
}
