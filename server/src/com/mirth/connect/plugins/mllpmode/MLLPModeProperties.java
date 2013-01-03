package com.mirth.connect.plugins.mllpmode;

import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.util.TcpUtil;

public class MLLPModeProperties extends FrameModeProperties {

    public static final String PLUGIN_POINT = "MLLP";

    public MLLPModeProperties() {
        this(PLUGIN_POINT);
    }

    public MLLPModeProperties(String pluginPointName) {
        super(pluginPointName);
        setStartOfMessageBytes(TcpUtil.DEFAULT_LLP_START_BYTES);
        setEndOfMessageBytes(TcpUtil.DEFAULT_LLP_END_BYTES);
    }
}
