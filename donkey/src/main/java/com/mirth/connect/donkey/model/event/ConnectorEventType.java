package com.mirth.connect.donkey.model.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;

public enum ConnectorEventType {
    IDLE(true), READING(true), WRITING(true), POLLING(true), RECEIVING(true), SENDING(true), WAITING_FOR_RESPONSE(true), CONNECTED(true), CONNECTING(true), DISCONNECTED(false), INFO(false), FAILURE(false);
    
    private boolean state;
    
    private ConnectorEventType(boolean state) {
        this.state = state;
    }
    
    public List<ConnectorEventType> getConnectorStates() {
        List<ConnectorEventType> states = new ArrayList<ConnectorEventType>();
        
        for (ConnectorEventType type : ConnectorEventType.values()) {
            if (type.isState()) {
                states.add(type);
            }
        }
        
        return states;
    }
    
    public boolean isState() {
        return state;
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
