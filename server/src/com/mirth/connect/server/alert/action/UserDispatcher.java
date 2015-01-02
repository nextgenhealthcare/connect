package com.mirth.connect.server.alert.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.UserController;

public class UserDispatcher extends EmailDispatcher {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void dispatch(List<String> recipients, String subject, String content) throws DispatchException {
        logger.debug("Dispatching alert action to users: " + StringUtils.join(recipients, ','));
        User user = null;

        if (recipients.size() == 1) {
            user = new User();
            user.setId(Integer.parseInt(recipients.get(0)));
        }

        try {
            List<String> emailAddresses = new ArrayList<String>();
            List<User> users = UserController.getInstance().getUser(user);
            Set<String> userIds = new HashSet<String>(recipients);

            for (User thisUser : users) {
                if (userIds.contains(thisUser.getId().toString()) && !StringUtils.isBlank(thisUser.getEmail())) {
                    emailAddresses.add(thisUser.getEmail());
                }
            }

            super.dispatch(emailAddresses, subject, content);
        } catch (ControllerException e) {
            throw new DispatchException("An error occurred while attempting to fetch user information.", e);
        }
    }
}
