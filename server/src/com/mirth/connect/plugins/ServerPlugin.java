package com.mirth.connect.plugins;

import java.util.Properties;

public interface ServerPlugin {
    public void init(Properties properties);

    public void start();

    public void stop();
}
