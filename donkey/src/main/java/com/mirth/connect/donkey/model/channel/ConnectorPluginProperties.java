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

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public abstract class ConnectorPluginProperties implements Serializable, Migratable, Purgable {

    public abstract String getName();

    /*
     * Force all connector plugin properties to implement equals. When a channel is updated, there
     * is a check to make sure it is not equal to the current version of the channel. If it is the
     * same (minus the last modified date and the revision number) then the channel is not updated
     * and the revision number does that change. If an extension of this class does not override and
     * implement equals(), then a channel that stores those connector properties will always be
     * updated and the revision number incremented, even if no actual changes were made.
     */
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}
}
