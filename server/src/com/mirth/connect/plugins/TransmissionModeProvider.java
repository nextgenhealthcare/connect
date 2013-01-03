/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;

public abstract class TransmissionModeProvider implements ServicePlugin {

    public abstract StreamHandler getStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties);

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void init(Properties properties) {}

    @Override
    public void update(Properties properties) {}

    @Override
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        return null;
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        return new ExtensionPermission[] {};
    }
}