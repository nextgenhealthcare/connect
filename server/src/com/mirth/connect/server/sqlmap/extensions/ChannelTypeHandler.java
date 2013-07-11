package com.mirth.connect.server.sqlmap.extensions;

import com.mirth.connect.model.Channel;

public class ChannelTypeHandler extends SerializedObjectTypeHandler<Channel> {
    public ChannelTypeHandler() {
        super(Channel.class);
    }
}
