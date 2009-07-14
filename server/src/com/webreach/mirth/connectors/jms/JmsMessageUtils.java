/* 
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsMessageUtils.java,v 1.5 2005/06/23 08:01:30 gnt Exp $
 * $Revision: 1.5 $
 * $Date: 2005/06/23 08:01:30 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package com.webreach.mirth.connectors.jms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.util.compression.CompressionHelper;

/**
 * <code>JmsMessageUtils</code> TODO -document class
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class JmsMessageUtils
{
    /**
     * The logger for this class
     */
    private static final transient Log logger = LogFactory.getLog(JmsMessageUtils.class);

    public static Message getMessageForObject(Object object, Session session) throws JMSException
    {
        if (object instanceof Message) {
            return (Message) object;
        } else if (object instanceof String) {
            TextMessage text = session.createTextMessage((String) object);
            return text;
        } else if (object instanceof Map) {
            MapMessage map = session.createMapMessage();
            Map.Entry entry = null;
            Map temp = (Map) object;

            for (Iterator i = temp.entrySet().iterator(); i.hasNext();) {
                entry = (Map.Entry) i.next();
                map.setObject(entry.getKey().toString(), entry.getValue());
            }

            return map;
        } else if (object instanceof InputStream) {
            StreamMessage stream = session.createStreamMessage();
            InputStream temp = (InputStream) object;

            byte[] buffer = new byte[1024 * 4];
            int len = 0;
            try {
                while ((len = temp.read(buffer)) != -1) {
                    stream.writeBytes(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new JMSException("Failed to read input stream to create a stream message: " + e);
            }

            return stream;
        } else if (object instanceof byte[]) {
            BytesMessage bytes = session.createBytesMessage();
            byte[] buf = (byte[]) object;
            for (int i = 0; i < buf.length; i++) {
                bytes.writeByte(buf[i]);
            }

            return bytes;
        } else if (object instanceof Serializable) {
            ObjectMessage oMsg = session.createObjectMessage();
            oMsg.setObject((Serializable) object);
            return oMsg;
        } else {
            throw new JMSException("Source was not a supported type, data must be Serializable, String, byte[], Map or InputStream");
        }
    }

    public static Object getObjectForMessage(Message source) throws JMSException
    {
        Object result = null;
        try {
            if (source instanceof ObjectMessage) {
                result = ((ObjectMessage) source).getObject();
            } else if (source instanceof MapMessage) {
                Hashtable map = new Hashtable();
                MapMessage m = (MapMessage) source;

                for (Enumeration e = m.getMapNames(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    Object obj = m.getObject(name);
                    map.put(name, obj);
                }

                result = map;
            } else if (source instanceof javax.jms.BytesMessage) {

                javax.jms.BytesMessage bm = (javax.jms.BytesMessage) source;
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

                byte[] buffer = new byte[1024 * 4];
                int len = 0;
                bm.reset();
                while ((len = bm.readBytes(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                result = baos.toByteArray();
                baos.close();
                if (result != null) {
                    if (logger.isDebugEnabled())
                        logger.debug("JMSToObject: extracted " + ((byte[]) result).length
                                + " bytes from JMS BytesMessage");
                }
            } else if (source instanceof TextMessage) {
                result = ((TextMessage) source).getText();

            } else if (source instanceof BytesMessage) {
                byte[] bytes = getBytesFromMessage(source);
                return CompressionHelper.uncompressByteArray(bytes);
            } else if (source instanceof StreamMessage) {

                StreamMessage sm = (javax.jms.StreamMessage) source;

                result = new java.util.Vector();
                try {
                    Object obj = null;
                    while ((obj = sm.readObject()) != null) {
                        ((java.util.Vector) result).addElement(obj);
                    }
                } catch (MessageEOFException eof) {
                } catch (Exception e) {
                    throw new JMSException("Failed to extract information from JMS Stream Message: " + e);
                }
            } else {
                result = source;
            }
        } catch (Exception e) {
            throw new JMSException("Failed to transform message: " + e.getMessage());
        }
        return result;
    }

    /**
     * @param message the message to receive the bytes from. Note this only
     *            works for TextMessge, ObjectMessage, StreamMessage and
     *            BytesMessage.
     * @return a byte array corresponding with the message payload
     * @throws JMSException if the message can't be read or if the message
     *             passed is a MapMessage
     * @throws java.io.IOException if a failiare occurs while stream and
     *             converting the message data
     */
    public static byte[] getBytesFromMessage(Message message) throws JMSException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 2];
        int len;

        if (message instanceof BytesMessage) {
            BytesMessage bMsg = (BytesMessage) message;
            // put message in read-only mode
            bMsg.reset();
            while ((len = bMsg.readBytes(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } else if (message instanceof StreamMessage) {
            StreamMessage sMsg = (StreamMessage) message;
            sMsg.reset();
            while ((len = sMsg.readBytes(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } else if (message instanceof ObjectMessage) {
            ObjectMessage oMsg = (ObjectMessage) message;
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(oMsg.getObject());
            os.flush();
            baos.write(bs.toByteArray());
            os.close();
            bs.close();
        } else if (message instanceof TextMessage) {
            TextMessage tMsg = (TextMessage) message;
            baos.write(tMsg.getText().getBytes());
        } else {
            throw new JMSException("Cannot get bytes from Map Message");
        }

        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }

    public static String getNameForDestination(Destination dest) throws JMSException
    {
        if (dest instanceof Queue) {
            return ((Queue) dest).getQueueName();
        } else if (dest instanceof Topic) {
            return ((Topic) dest).getTopicName();
        } else {
            return null;
        }
    }
}
