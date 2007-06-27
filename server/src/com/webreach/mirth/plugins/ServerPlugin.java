package com.webreach.mirth.plugins;

import java.util.Properties;

public interface ServerPlugin
{
    public abstract void init(Properties properties);

    public abstract void start();

    public abstract void update(Properties properties);

    public abstract void stop();

    public abstract Properties getDefaultProperties();
}
