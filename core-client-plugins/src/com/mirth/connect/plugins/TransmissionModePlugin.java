/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

public abstract class TransmissionModePlugin extends ClientPlugin {

    public TransmissionModePlugin(String name) {
        super(name);
    }

    public abstract TransmissionModeClientProvider createProvider();

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}
