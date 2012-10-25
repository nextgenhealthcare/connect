/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

public class MetaDataColumnException extends Exception {
    private MetaDataColumn column;

    public MetaDataColumnException(String message) {
        super(message);
    }

    public MetaDataColumnException(Throwable cause) {
        super(cause);
    }

    public MetaDataColumnException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaDataColumnException(String message, MetaDataColumn column) {
        super(message);
        this.column = column;
    }

    public MetaDataColumnException(Throwable cause, MetaDataColumn column) {
        super(cause);
        this.column = column;
    }

    public MetaDataColumn getMetaDataColumn() {
        return column;
    }
}
