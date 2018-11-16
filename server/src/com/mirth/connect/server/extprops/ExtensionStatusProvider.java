/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.extprops;

import java.util.Properties;

public abstract class ExtensionStatusProvider implements ExtensionStatusInterface {

    protected Properties mirthProperties;

    public ExtensionStatusProvider(Properties mirthProperties) {
        this.mirthProperties = mirthProperties;
    }
}
