/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Properties;

import com.mirth.connect.model.User;

public abstract class UserController extends Controller {
    public static UserController getInstance() {
        return ControllerFactory.getFactory().createUserController();
    }
    
    public abstract void resetUserStatus();
    
    public abstract List<User> getUser(User user) throws ControllerException;

    public abstract void updateUser(User user, String plainTextPassword) throws ControllerException;

    public abstract void removeUser(User user, Integer currentUserId) throws ControllerException;

    public abstract boolean authorizeUser(User user, String plainTextPassword) throws ControllerException;

    public abstract void loginUser(User user) throws ControllerException;

    public abstract void logoutUser(User user) throws ControllerException;

    public abstract boolean isUserLoggedIn(User user) throws ControllerException;

    public abstract Properties getUserPreferences(User user) throws ControllerException;

    public abstract void setUserPreference(User user, String name, String value) throws ControllerException;
}
