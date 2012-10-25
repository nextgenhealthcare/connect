package com.mirth.connect.plugins;

public interface ServerPlugin {
    public String getPluginPointName();

    public void start();

    public void stop();
}
