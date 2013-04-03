/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import org.apache.commons.lang3.text.WordUtils;

public enum ContentType {
    RAW(1), PROCESSED_RAW(2), TRANSFORMED(3), ENCODED(4), SENT(5), RESPONSE(6), RESPONSE_TRANSFORMED(
            7), PROCESSED_RESPONSE(8), CONNECTOR_MAP(9), CHANNEL_MAP(10), RESPONSE_MAP(11), PROCESSING_ERROR(
            12), POSTPROCESSOR_ERROR(13), RESPONSE_ERROR(14);

    private static int PROCESSING_ERROR_CODE = 1 << 0;
    private static int POSTPROCESSOR_ERROR_CODE = 1 << 1;
    private static int RESPONSE_ERROR_CODE = 1 << 2;
    private int contentType;

    private ContentType(int contentType) {
        this.contentType = contentType;
    }

    public int getContentTypeCode() {
        return contentType;
    }

    public String toString() {
        return WordUtils.capitalize(super.toString().replace('_', ' ').toLowerCase());
    }

    public static ContentType fromCode(int contentType) {
        if (contentType == ContentType.RAW.getContentTypeCode())
            return RAW;
        if (contentType == ContentType.PROCESSED_RAW.getContentTypeCode())
            return PROCESSED_RAW;
        if (contentType == ContentType.TRANSFORMED.getContentTypeCode())
            return TRANSFORMED;
        if (contentType == ContentType.ENCODED.getContentTypeCode())
            return ENCODED;
        if (contentType == ContentType.SENT.getContentTypeCode())
            return SENT;
        if (contentType == ContentType.RESPONSE.getContentTypeCode())
            return RESPONSE;
        if (contentType == ContentType.RESPONSE_TRANSFORMED.getContentTypeCode())
            return RESPONSE_TRANSFORMED;
        if (contentType == ContentType.PROCESSED_RESPONSE.getContentTypeCode())
            return PROCESSED_RESPONSE;
        if (contentType == ContentType.CONNECTOR_MAP.getContentTypeCode())
            return CONNECTOR_MAP;
        if (contentType == ContentType.CHANNEL_MAP.getContentTypeCode())
            return CHANNEL_MAP;
        if (contentType == ContentType.RESPONSE_MAP.getContentTypeCode())
            return RESPONSE_MAP;
        if (contentType == ContentType.PROCESSING_ERROR.getContentTypeCode())
            return PROCESSING_ERROR;
        if (contentType == ContentType.POSTPROCESSOR_ERROR.getContentTypeCode())
            return POSTPROCESSOR_ERROR;
        if (contentType == ContentType.RESPONSE_ERROR.getContentTypeCode())
            return RESPONSE_ERROR;

        return null;
    }

    public static ContentType[] getMessageTypes() {
        return new ContentType[] { RAW, PROCESSED_RAW, TRANSFORMED, ENCODED, SENT, RESPONSE,
                RESPONSE_TRANSFORMED, PROCESSED_RESPONSE };
    }

    public static ContentType[] getMapTypes() {
        return new ContentType[] { CONNECTOR_MAP, CHANNEL_MAP, RESPONSE_MAP };
    }

    public static ContentType[] getErrorTypes() {
        return new ContentType[] { PROCESSING_ERROR, POSTPROCESSOR_ERROR, RESPONSE_ERROR };
    }

    /**
     * Return the errorCode for the content type. Error codes are powers of 2.
     */
    public int getErrorCode() {
        if (contentType == ContentType.PROCESSING_ERROR.getContentTypeCode()) {
            return PROCESSING_ERROR_CODE;
        }
        if (contentType == ContentType.POSTPROCESSOR_ERROR.getContentTypeCode()) {
            return POSTPROCESSOR_ERROR_CODE;
        }
        if (contentType == ContentType.RESPONSE_ERROR.getContentTypeCode()) {
            return RESPONSE_ERROR_CODE;
        }

        return 0;
    }
}
