package com.webreach.mirth.plugins;

import java.util.Properties;

public interface ServerPlugin {
    public void init(Properties properties);

    public void start();

    public void update(Properties properties);

    public void onDeploy();

    public void stop();

    public Object invoke(String method, Object object, String sessionId);

    public Properties getDefaultProperties();
}
