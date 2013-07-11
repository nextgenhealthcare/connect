package com.mirth.connect.server.sqlmap.extensions;

import java.util.Properties;

public class PropertiesTypeHandler extends SerializedObjectTypeHandler<Properties> {
    public PropertiesTypeHandler() {
        super(Properties.class);
    }
}
