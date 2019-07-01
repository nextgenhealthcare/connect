/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.UnsupportedEncodingException;

/**
 * Used to store and retrieve details about message attachments such as the ID, MIME type, and
 * content.
 */
public class Attachment {
    private String id;
    private byte[] content;
    private String type;

    /**
     * Instantiates a new Attachment with no ID, content, or MIME type.
     */
    public Attachment() {}

    /**
     * Instantiates a new Attachment.
     * 
     * @param id
     *            The unique ID of the attachment.
     * @param content
     *            The content (byte array) to store for the attachment.
     * @param type
     *            The MIME type of the attachment.
     */
    public Attachment(String id, byte[] content, String type) {
        this.id = id;
        this.content = content;
        this.type = type;
    }

    /**
     * Instantiates a new Attachment with String data using UTF-8 charset encoding.
     * 
     * @param id
     *            The unique ID of the attachment.
     * @param content
     *            The string representation of the attachment content.
     * @param type
     *            The MIME type of the attachment.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public Attachment(String id, String content, String type) throws UnsupportedEncodingException {
        this.id = id;
        setContentString(content);
        this.type = type;
    }

    /**
     * Instantiates a new Attachment with String data and a given charset encoding.
     * 
     * @param id
     *            The unique ID of the attachment.
     * @param content
     *            The string representation of the attachment content.
     * @param charset
     *            The charset encoding to convert the string to bytes with.
     * @param type
     *            The MIME type of the attachment.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public Attachment(String id, String content, String charset, String type) throws UnsupportedEncodingException {
        this.id = id;
        setContentString(content, charset);
        this.type = type;
    }

    /**
     * Returns the unique replacement token for the attachment. This token should replace the
     * attachment content in the message string, and will be used to re-attach the attachment
     * content in the outbound message before it is sent to a downstream system.
     * 
     * @return The unique replacement token for the attachment.
     */
    public String getAttachmentId() {
        return "${ATTACH:" + id + "}";
    }

    /**
     * Returns the unique ID for the attachment.
     * 
     * @return The unique ID for the attachment.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID for the attachment.
     * 
     * @param id
     *            The unique ID to use for the attachment.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the content of the attachment as a byte array.
     * 
     * @return The content of the attachment as a byte array.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns the content of the attachment as a string, using UTF-8 encoding.
     * 
     * @return The content of the attachment as a string, using UTF-8 encoding.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public String getContentString() throws UnsupportedEncodingException {
        return getContentString("UTF-8");
    }

    /**
     * Returns the content of the attachment as a string, using the specified charset encoding.
     * 
     * @param charset
     *            The charset encoding to convert the content bytes to a string with.
     * @return The content of the attachment as a string, using the specified charset encoding.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public String getContentString(String charset) throws UnsupportedEncodingException {
        return new String(content, charset);
    }

    /**
     * Sets the content of the attachment.
     * 
     * @param content
     *            The content (byte array) to use for the attachment.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Sets the content of the attachment, using UTF-8 encoding.
     * 
     * @param content
     *            The string representation of the attachment content.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public void setContentString(String content) throws UnsupportedEncodingException {
        setContentString(content, "UTF-8");
    }

    /**
     * Sets the content of the attachment, using the specified charset encoding.
     * 
     * @param content
     *            The string representation of the attachment content.
     * @param charset
     *            The charset encoding to convert the string to bytes with.
     * @throws UnsupportedEncodingException
     *             If the named charset is not supported.
     */
    public void setContentString(String content, String charset) throws UnsupportedEncodingException {
        this.content = content.getBytes(charset);
    }

    /**
     * Returns the MIME type of the attachment.
     * 
     * @return The MIME type of the attachment.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the MIME type for the attachment.
     * 
     * @param type
     *            The MIME type to set for the attachment.
     */
    public void setType(String type) {
        this.type = type;
    }
}