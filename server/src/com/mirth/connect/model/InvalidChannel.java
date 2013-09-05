/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.LinkedHashSet;

import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.thoughtworks.xstream.io.xml.DocumentReader;

public class InvalidChannel extends Channel {

    private Throwable cause;
    private String channelXml;

    public InvalidChannel(String id, String name, Integer revision, Throwable cause, String channelXml) {
        init(id, name, revision, cause, channelXml);
    }

    public InvalidChannel(DonkeyElement channel, Throwable cause) {
        this(channel, cause, null);
    }

    public InvalidChannel(DonkeyElement channel, Throwable cause, DocumentReader reader) {
        // Reset the stream reader to the channel element
        while (reader != null && reader.getCurrent() instanceof Element && !reader.getCurrent().equals(channel.getElement())) {
            reader.moveUp();
        }

        if (channel == null) {
            throw new SerializerException("Could not create invalid channel. The channel element is null.");
        }

        DonkeyElement id = channel.getChildElement("id");
        DonkeyElement name = channel.getChildElement("name");
        DonkeyElement revision = channel.getChildElement("revision");

        if (id == null || name == null || revision == null) {
            throw new SerializerException("Could not create invalid channel. The channel ID, name, or revision is missing.");
        }

        try {
            init(id.getTextContent(), name.getTextContent(), Integer.valueOf(revision.getTextContent()), cause, channel.toXml());
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    private void init(String id, String name, Integer revision, Throwable cause, String channelXml) {
        setId(id);
        setName(name);
        setRevision(revision);
        this.cause = cause;
        this.channelXml = channelXml;

        super.setEnabled(false);
        setDescription("This channel is invalid. Verify all required extensions are loaded correctly.");
        getProperties().setTags(new LinkedHashSet<String>());

        Connector sourceConnector = new Connector();
        sourceConnector.setFilter(new Filter());
        sourceConnector.setTransformer(new Transformer());
        setSourceConnector(sourceConnector);
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getChannelXml() {
        return channelXml;
    }

    public void setChannelXml(String channelXml) {
        this.channelXml = channelXml;
    }

    @Override
    public void setId(String id) {
        setProperty("id", id);
        super.setId(id);
    }

    @Override
    public void setName(String name) {
        setProperty("name", name);
        super.setName(name);
    }

    @Override
    public void setRevision(int revision) {
        setProperty("revision", String.valueOf(revision));
        super.setRevision(revision);
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    private void setProperty(String key, String value) {
        if (channelXml != null) {
            try {
                DonkeyElement root = new DonkeyElement(channelXml);
                DonkeyElement child = root.getChildElement(key);
                if (child != null) {
                    child.setTextContent(value);
                }
                channelXml = root.toXml();
            } catch (Exception e) {
            }
        }
    }
}