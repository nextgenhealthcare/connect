/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Denotes various types of content created by a channel. Available types are:
 * 
 * RAW, PROCESSED_RAW, TRANSFORMED, ENCODED, SENT, RESPONSE,
 * RESPONSE_TRANSFORMED, PROCESSED_RESPONSE, CONNECTOR_MAP, CHANNEL_MAP,
 * RESPONSE_MAP, PROCESSING_ERROR, POSTPROCESSOR_ERROR, RESPONSE_ERROR
 */
public enum ContentType {
    RAW, PROCESSED_RAW, TRANSFORMED, ENCODED, SENT, RESPONSE, RESPONSE_TRANSFORMED, PROCESSED_RESPONSE, CONNECTOR_MAP, CHANNEL_MAP, RESPONSE_MAP, PROCESSING_ERROR, POSTPROCESSOR_ERROR, RESPONSE_ERROR;

    private ContentType() {}

    @Override
    public String toString() {
        return WordUtils.capitalize(super.toString().replace('_', ' ').toLowerCase());
    }

    static ContentType fromDonkeyContentType(com.mirth.connect.donkey.model.message.ContentType contentType) {
        switch (contentType) {
            case RAW:
                return RAW;
            case PROCESSED_RAW:
                return PROCESSED_RAW;
            case TRANSFORMED:
                return TRANSFORMED;
            case ENCODED:
                return ENCODED;
            case SENT:
                return SENT;
            case RESPONSE:
                return RESPONSE;
            case RESPONSE_TRANSFORMED:
                return RESPONSE_TRANSFORMED;
            case PROCESSED_RESPONSE:
                return PROCESSED_RESPONSE;
            case CONNECTOR_MAP:
                return CONNECTOR_MAP;
            case CHANNEL_MAP:
                return CHANNEL_MAP;
            case RESPONSE_MAP:
                return RESPONSE_MAP;
            case PROCESSING_ERROR:
                return PROCESSING_ERROR;
            case POSTPROCESSOR_ERROR:
                return POSTPROCESSOR_ERROR;
            case RESPONSE_ERROR:
                return RESPONSE_ERROR;
            default:
                return null;
        }
    }

    com.mirth.connect.donkey.model.message.ContentType toDonkeyContentType() {
        switch (this) {
            case RAW:
                return com.mirth.connect.donkey.model.message.ContentType.RAW;
            case PROCESSED_RAW:
                return com.mirth.connect.donkey.model.message.ContentType.PROCESSED_RAW;
            case TRANSFORMED:
                return com.mirth.connect.donkey.model.message.ContentType.TRANSFORMED;
            case ENCODED:
                return com.mirth.connect.donkey.model.message.ContentType.ENCODED;
            case SENT:
                return com.mirth.connect.donkey.model.message.ContentType.SENT;
            case RESPONSE:
                return com.mirth.connect.donkey.model.message.ContentType.RESPONSE;
            case RESPONSE_TRANSFORMED:
                return com.mirth.connect.donkey.model.message.ContentType.RESPONSE_TRANSFORMED;
            case PROCESSED_RESPONSE:
                return com.mirth.connect.donkey.model.message.ContentType.PROCESSED_RESPONSE;
            case CONNECTOR_MAP:
                return com.mirth.connect.donkey.model.message.ContentType.CONNECTOR_MAP;
            case CHANNEL_MAP:
                return com.mirth.connect.donkey.model.message.ContentType.CHANNEL_MAP;
            case RESPONSE_MAP:
                return com.mirth.connect.donkey.model.message.ContentType.RESPONSE_MAP;
            case PROCESSING_ERROR:
                return com.mirth.connect.donkey.model.message.ContentType.PROCESSING_ERROR;
            case POSTPROCESSOR_ERROR:
                return com.mirth.connect.donkey.model.message.ContentType.POSTPROCESSOR_ERROR;
            case RESPONSE_ERROR:
                return com.mirth.connect.donkey.model.message.ContentType.RESPONSE_ERROR;
            default:
                return null;
        }
    }
}