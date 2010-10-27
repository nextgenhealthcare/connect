/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.4 $
 */
public class JmsUtils {

    private static final transient Log logger = LogFactory.getLog(JmsUtils.class);

    public static void close(MessageProducer producer) throws JMSException {
        if (producer != null) {
            producer.close();
        }
    }

    public static void closeQuietly(MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                logger.error("Failed to close jms message producer", e);
            }
        }
    }

    public static void close(MessageConsumer consumer) throws JMSException {
        if (consumer != null) {
            consumer.close();
        }
    }

    public static void closeQuietly(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                logger.error("Failed to close jms message consumer", e);
            }
        }
    }

    public static void close(Session session) throws JMSException {
        if (session != null) {
            session.close();
        }
    }

    public static void closeQuietly(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                logger.error("Failed to close jms session consumer", e);
            }
        }
    }

}
