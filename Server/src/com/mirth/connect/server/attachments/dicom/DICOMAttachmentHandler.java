/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.dicom;

import org.apache.commons.codec.binary.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class DICOMAttachmentHandler implements AttachmentHandler {

    private DicomObject dicomObject;
    private DicomElement dicomElement;
    private int index;
    private String attachmentId;

    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {
        index = 0;
        try {
            byte[] messageBytes = null;
            boolean decode = false;

            if (message.isBinary()) {
                messageBytes = message.getRawBytes();
            } else {
                // Taking a string is much more inefficient than taking in a byte array. 
                // If the user manually sends a message, it will arrive as a base64 encoded string, so we must support Strings for DICOM still.
                // However, DICOM messages that use this initializer should be relatively small in size.
                messageBytes = StringUtils.getBytesUsAscii(message.getRawData());
                decode = true;
            }

            dicomObject = DICOMConverter.byteArrayToDicomObject(messageBytes, decode);
            dicomElement = dicomObject.remove(Tag.PixelData);
            attachmentId = ServerUUIDGenerator.getUUID();
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        try {
            if (dicomElement != null) {
                if (dicomElement.hasItems()) {
                    int total = dicomElement.countItems();
                    if (index < total) {
                        // Add prefix with sequence ID so that fragments will get re-attached in the right order
                        String fragment = "F" + org.apache.commons.lang3.StringUtils.leftPad(String.valueOf(index), String.valueOf(total).length(), '0') + "-";
                        return new Attachment(fragment + attachmentId, dicomElement.getFragment(index++), "DICOM");
                    }
                } else {
                    Attachment attachment = new Attachment(attachmentId, dicomElement.getBytes(), "DICOM");
                    dicomElement = null;
                    return attachment;
                }
            }

            return null;
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public String shutdown() throws AttachmentException {
        try {
            byte[] encodedMessage = Base64Util.encodeBase64(DICOMConverter.dicomObjectToByteArray(dicomObject));
            dicomElement = null;
            dicomObject = null;
            return StringUtils.newStringUsAscii(encodedMessage);
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }

    }
}
