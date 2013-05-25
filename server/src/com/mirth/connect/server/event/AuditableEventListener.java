package com.mirth.connect.server.event;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class AuditableEventListener extends EventListener {
    private EventController eventController;
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected void onShutdown() {

    }

    @Override
    public Set<EventType> getEventTypes() {
        Set<EventType> eventTypes = new HashSet<EventType>();

        eventTypes.add(EventType.SERVER);

        return eventTypes;
    }

    @Override
    protected void processEvent(Event event) {
        if (event instanceof ServerEvent) {
            ServerEvent serverEvent = (ServerEvent) event;

            if (eventController == null) {
                eventController = ControllerFactory.getFactory().createEventController();
            }
            
            eventController.insertEvent(serverEvent);
        }
    }

}
