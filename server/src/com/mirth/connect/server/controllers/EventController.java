package com.mirth.connect.server.controllers;

import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.server.event.EventListener;

public abstract class EventController extends Controller {
    public static EventController getInstance() {
        return ControllerFactory.getFactory().createEventController();
    }

    public abstract void addListener(EventListener listener);

    public abstract void removeListener(EventListener listener);

    public abstract void dispatchEvent(Event event);
}
