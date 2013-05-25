package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.server.event.EventListener;

public abstract class EventController extends Controller {
    public static EventController getInstance() {
        return ControllerFactory.getFactory().createEventController();
    }

    public abstract void addListener(EventListener listener);

    public abstract void removeListener(EventListener listener);

    public abstract void dispatchEvent(Event event);
    
    public abstract void insertEvent(ServerEvent serverEvent);
    
    public abstract Integer getMaxEventId() throws ControllerException;
    
    public abstract List<ServerEvent> getEvents(EventFilter filter, Integer offset, Integer limit) throws ControllerException;
    
    public abstract Long getEventCount(EventFilter filter) throws ControllerException;
    
    public abstract void removeAllEvents() throws ControllerException;
    
    public abstract String exportAllEvents() throws ControllerException;
    
    public abstract String exportAndRemoveAllEvents() throws ControllerException;
}
