package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.server.Donkey;

public class DonkeyEventDispatcherProvider implements EventDispatcherProvider {

    @Override
    public EventDispatcher getEventDispatcher() {
        return Donkey.getInstance().getEventDispatcher();
    }

}
