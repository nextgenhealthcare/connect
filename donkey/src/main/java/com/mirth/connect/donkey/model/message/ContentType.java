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
    RAW('R'), PROCESSED_RAW('P'), TRANSFORMED('T'), ENCODED('E'), SENT('S'), RESPONSE('O'), PROCESSED_RESPONSE(
            'N');

    private char contentType;

    private ContentType(char contentType) {
        this.contentType = contentType;
    }

    public char getContentTypeCode() {
        return contentType;
    }

    public String toString() {
        return WordUtils.capitalize(super.toString().replace('_', ' ').toLowerCase());
    }

    public static ContentType fromChar(char contentType) {
        if (contentType == 'R')
            return RAW;
        if (contentType == 'P')
            return PROCESSED_RAW;
        if (contentType == 'T')
            return TRANSFORMED;
        if (contentType == 'E')
            return ENCODED;
        if (contentType == 'S')
            return SENT;
        if (contentType == 'O')
            return RESPONSE;
        if (contentType == 'N')
            return PROCESSED_RESPONSE;

        return null;
    }
}
