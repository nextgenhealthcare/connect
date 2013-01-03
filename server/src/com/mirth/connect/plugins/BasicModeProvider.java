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

import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.framemode.FrameStreamHandler;

public class BasicModeProvider extends TransmissionModeProvider {

    @Override
    public String getPluginPointName() {
        return "Basic";
    }

    @Override
    public StreamHandler getStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties) {
        return new FrameStreamHandler(inputStream, outputStream, batchStreamReader, transmissionModeProperties);
    }
}
