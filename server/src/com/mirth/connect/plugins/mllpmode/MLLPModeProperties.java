/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.util.TcpUtil;

public class MLLPModeProperties extends FrameModeProperties {

    public static final String PLUGIN_POINT = "MLLP";

    private boolean useMLLPv2;
    private String ackBytes;
    private String nackBytes;
    private String maxRetries;

    public MLLPModeProperties() {
        this(PLUGIN_POINT);
        useMLLPv2 = false;
        ackBytes = "06"; // <ACK>
        nackBytes = "15"; // <NAK>
        maxRetries = "2";
    }

    public MLLPModeProperties(String pluginPointName) {
        super(pluginPointName);
        setStartOfMessageBytes(TcpUtil.DEFAULT_LLP_START_BYTES);
        setEndOfMessageBytes(TcpUtil.DEFAULT_LLP_END_BYTES);
    }

    public boolean isUseMLLPv2() {
        return useMLLPv2;
    }

    public void setUseMLLPv2(boolean useMLLPv2) {
        this.useMLLPv2 = useMLLPv2;
    }

    public String getAckBytes() {
        return ackBytes;
    }

    public void setAckBytes(String ackBytes) {
        this.ackBytes = ackBytes;
    }

    public String getNackBytes() {
        return nackBytes;
    }

    public void setNackBytes(String nackBytes) {
        this.nackBytes = nackBytes;
    }

    public String getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(String maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;

        if (obj instanceof MLLPModeProperties) {
            MLLPModeProperties props = (MLLPModeProperties) obj;
            //@formatter:off
            equal = props.isUseMLLPv2() == useMLLPv2 &&
                props.getAckBytes().equals(ackBytes) &&
                props.getNackBytes().equals(nackBytes) &&
                props.getMaxRetries().equals(maxRetries);
            //@formatter:on
        }

        return equal && super.equals(obj);
    }
}
