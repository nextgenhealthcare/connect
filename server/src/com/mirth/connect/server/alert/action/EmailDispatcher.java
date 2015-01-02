package com.mirth.connect.server.alert.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.util.ServerSMTPConnectionFactory;

public class EmailDispatcher implements Dispatcher {
    private static final String DEFAULT_SUBJECT = "Mirth Connect Alert";

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void dispatch(List<String> recipients, String subject, String content) throws DispatchException {
        logger.debug("Dispatching alert action to email addresses: " + StringUtils.join(recipients, ','));

        if (!recipients.isEmpty()) {
            if (StringUtils.isEmpty(subject)) {
                subject = DEFAULT_SUBJECT;
            }

            try {
                ServerSMTPConnectionFactory.createSMTPConnection().send(StringUtils.join(recipients, ","), null, subject, content);
            } catch (ControllerException e) {
                throw new DispatchException("Could not load default SMTP settings.", e);
            } catch (EmailException e) {
                throw new DispatchException("Error sending alert email.", e);
            }
        }
    }
}
