package com.mirth.connect.server.alert.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.UserController;

public class UserProtocol implements Protocol {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public String getName() {
        return "User";
    }

    @Override
    public Map<String, String> getRecipientOptions() {
        Map<String, String> options = new HashMap<String, String>();

        try {
            for (User user : UserController.getInstance().getUser(null)) {
                options.put(user.getId().toString(), user.getUsername());
            }
        } catch (ControllerException e) {
            logger.error("An error occurred while attempting to fetch users.", e);
        }

        return options;
    }

    @Override
    public Dispatcher getDispatcher() {
        return new UserDispatcher();
    }
}
