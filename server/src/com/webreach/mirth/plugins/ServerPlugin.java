package com.webreach.mirth.plugins;

import java.util.Properties;

public interface ServerPlugin
{
    public abstract void init(Properties properties);

    public abstract void start();

    public abstract void update(Properties properties);
    
    public abstract void onDeploy();

    public abstract void stop();
    
    public abstract Object invoke (String method, Object object, String sessionId);

    public abstract Properties getDefaultProperties();
}
