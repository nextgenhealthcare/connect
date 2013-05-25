package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.Event;

public interface EventDispatcher {
    
    public void dispatchEvent(Event event);
}
