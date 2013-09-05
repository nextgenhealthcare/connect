/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connectionStatusEventType")
public enum ConnectionStatusEventType {
    IDLE(true), READING(true), WRITING(true), POLLING(true), RECEIVING(true), SENDING(true), WAITING_FOR_RESPONSE(true), CONNECTED(true), CONNECTING(true), DISCONNECTED(false), INFO(false), FAILURE(false);
    
    private boolean state;
    
    private ConnectionStatusEventType(boolean state) {
        this.state = state;
    }
    
    public List<ConnectionStatusEventType> getConnectorStates() {
        List<ConnectionStatusEventType> states = new ArrayList<ConnectionStatusEventType>();
        
        for (ConnectionStatusEventType type : ConnectionStatusEventType.values()) {
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
