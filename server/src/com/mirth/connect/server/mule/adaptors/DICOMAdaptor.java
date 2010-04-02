/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.adaptors;

import java.util.Iterator;
import java.util.Map;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.UUIDGenerator;

public class DICOMAdaptor extends Adaptor {
    protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
        messageObject.setRawDataProtocol(MessageObject.Protocol.DICOM);
        messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
        messageObject.setEncodedDataProtocol(MessageObject.Protocol.DICOM);

        try {
            // Set transformed data
            DICOMSerializer dSerializer = (DICOMSerializer) serializer;
            String message = dSerializer.toXML(source);
            messageObject.setTransformedData(message);

            // Set rawdata on messageobject without attachment data
            if(dSerializer.rawData != null)
                messageObject.setRawData(dSerializer.rawData);
            else
                messageObject.setRawData("");
            
            // Set source data to the new raw data which does not include the attachments
            // If the source is used after this point it will no longer have any attachment data.
            source = messageObject.getRawData();
            
            // Create attachment
            if(dSerializer.getPixelData() != null && !dSerializer.getPixelData().isEmpty()) {
                Iterator<String> i = dSerializer.getPixelData().iterator();
                Attachment attachment = new Attachment();
                attachment.setType("DICOM");
                MessageObjectController moc = ControllerFactory.getFactory().createMessageObjectController();
                moc.setAttachmentMessageId(messageObject, attachment);
                while(i.hasNext()){
                    String image = i.next();
                    attachment.setAttachmentId(UUIDGenerator.getUUID());
                    attachment.setData(image.getBytes());
                    attachment.setSize(image.length());
                    moc.insertAttachment(attachment);
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
