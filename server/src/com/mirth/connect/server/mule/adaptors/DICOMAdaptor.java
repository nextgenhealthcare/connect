/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.DICOMSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.SerializerFactory;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.util.UUIDGenerator;

public class DICOMAdaptor extends Adaptor {
    @Override
    protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
        messageObject.setRawDataProtocol(MessageObject.Protocol.DICOM);
        messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
        messageObject.setEncodedDataProtocol(MessageObject.Protocol.DICOM);

        try {
            // Set transformed data
            DICOMSerializer dicomSerializer = (DICOMSerializer) serializer;
            String message = dicomSerializer.toXML(source);
            messageObject.setTransformedData(message);

            // Set rawdata on messageobject without attachment data
            if (dicomSerializer.getRawData() != null) {
                messageObject.setRawData(dicomSerializer.getRawData());
            } else {
                messageObject.setRawData(StringUtils.EMPTY);
            }

            // Set source data to the new raw data which does not include the attachments
            // If the source is used after this point it will no longer have any attachment data.
            source = messageObject.getRawData();

            // Create attachment
            if (dicomSerializer.getPixelData() != null && !dicomSerializer.getPixelData().isEmpty()) {
                Attachment attachment = new Attachment();
                attachment.setType("DICOM");

                MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
                messageObjectController.setAttachmentMessageId(messageObject, attachment);

                for (String image : dicomSerializer.getPixelData()) {
                    attachment.setAttachmentId(UUIDGenerator.getUUID());
                    attachment.setData(image.getBytes());
                    attachment.setSize(image.length());
                    messageObjectController.insertAttachment(attachment);
                }

                messageObject.setAttachment(true);
            }

            populateMetadataFromXML(message);
        } catch (Exception e) {
            handleException(e);
        }

        if (emptyFilterAndTransformer) {
            messageObject.setEncodedData(source);
        }
    }

    @Override
    public IXMLSerializer<String> getSerializer(Map properties) {
        return SerializerFactory.getSerializer(MessageObject.Protocol.DICOM, properties);
    }
}
