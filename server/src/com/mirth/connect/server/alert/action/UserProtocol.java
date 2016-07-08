/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.alert.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;

public class UserProtocol implements Protocol {
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public String getName() {
        return "User";
    }

    @Override
    public Map<String, String> getRecipientOptions() {
        Map<String, String> options = new HashMap<String, String>();

        try {
            for (User user : UserController.getInstance().getAllUsers()) {
                options.put(user.getId().toString(), user.getUsername());
            }
        } catch (ControllerException e) {
            logger.error("An error occurred while attempting to fetch users.", e);
        }

        return options;
    }

    @Override
    public List<String> getEmailAddressesForDispatch(List<String> recipients) {
        try {
            Set<String> emailAddresses = new LinkedHashSet<String>();
            List<User> users = userController.getAllUsers();
            Set<Integer> userIds = new HashSet<Integer>();
            Set<String> usernames = new HashSet<String>();

            Map<Integer, String> userMap = new HashMap<Integer, String>();
            for (User user : users) {
                userMap.put(user.getId(), user.getUsername());
            }

            for (String recipient : recipients) {
                if (NumberUtils.isNumber(recipient) && userMap.containsKey(NumberUtils.toInt(recipient))) {
                    userIds.add(NumberUtils.toInt(recipient));
                } else if (userMap.containsValue(recipient)) {
                    usernames.add(recipient);
                }
            }

            for (User user : users) {
                if ((userIds.contains(user.getId()) || usernames.contains(user.getUsername())) && StringUtils.isNotBlank(user.getEmail())) {
                    emailAddresses.add(user.getEmail());
                }
            }

            return new ArrayList<String>(emailAddresses);
        } catch (Exception e) {
            logger.error("An error occurred while attempting to look up email addresses for users.", e);
            return null;
        }
    }

    @Override
    public void doCustomDispatch(List<String> recipients, String subject, String content) {}
}
