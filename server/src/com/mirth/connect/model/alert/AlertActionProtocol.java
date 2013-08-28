/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import org.apache.commons.lang.WordUtils;

public enum AlertActionProtocol {
    EMAIL, CHANNEL;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString());
    }
}
