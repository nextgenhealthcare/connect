/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.Serializable;

import com.mirth.connect.donkey.util.purge.Purgable;

public abstract class SchemeProperties implements Serializable, Purgable {
    public SchemeProperties() {}

    public abstract SchemeProperties getFileSchemeProperties();

    public abstract String getSummaryText();

    public abstract String toFormattedString();

    @Override
    public abstract SchemeProperties clone();
}