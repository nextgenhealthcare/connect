/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;

import com.mirth.connect.donkey.util.migration.Migratable;

public abstract class ConnectorProperties implements Serializable, Migratable {
    public abstract String getProtocol();

    public abstract String getName();

    public abstract String toFormattedString();
}
