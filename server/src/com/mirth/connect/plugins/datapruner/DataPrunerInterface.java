package com.mirth.connect.plugins.datapruner;

public interface DataPrunerInterface {

    void beforeDataPruner();
    
    void afterDataPruner();
    
    void beforeDataPrunerOffline(String serverId);
    
    void afterDataPrunerOffline(String serverId);
    
}
