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

    /**
     * Sets the status of <b>all</b> users to logged out.
     * 
     */
    public abstract void resetUserStatus();

    /**
     * Returns a list of users that match the fields on the specified user, or all users if
     * user parameter is <code>null</code>.
     * 
     * @param user
     * @return
     * @throws ControllerException
     */
    public abstract List<User> getUser(User user) throws ControllerException;

    /**
     * Updates the specified user.
     * 
     * @param user
     * @param plainTextPassword
     * @throws ControllerException
     */
    public abstract void updateUser(User user, String plainTextPassword) throws ControllerException;

    /**
     * Deletes the specified user.
     * 
     * @param user the user to update
     * @param currentUserId the ID of the user requesting this action
     * @throws ControllerException if the specified user is <code>null</code> or is the current user
     */
    public abstract void removeUser(User user, Integer currentUserId) throws ControllerException;

    /**
     * Authorizes the specified user.
     * 
     * @param user the user to authorize
     * @param plainTextPassword the user's password.
     * @return <code>true</code> if the specified password matches the user's password, <code>false</code> otherwise
     * @throws ControllerException if the user's password could not be retrieved or verified
     */
    public abstract boolean authorizeUser(User user, String plainTextPassword) throws ControllerException;

    /**
     * Sets the user's status to logged in.
     * 
     * @param user the user to login
     * @throws ControllerException if the user's status could not be updated
     */
    public abstract void loginUser(User user) throws ControllerException;

    /**
     * Sets the user's status to logged out.
     * 
     * @param user the user to logout
     * @throws ControllerException if the user's status could not be updated
     */
    public abstract void logoutUser(User user) throws ControllerException;

    /**
     * Returns <code>true</code> if the user's status is set to logged in
     * 
     * @param user the user to check
     * @return <code>true</code> if the user's status is set to logged in, <code>false</code> otherwise
     * @throws ControllerException if the user's status could not be retrieved
     */
    public abstract boolean isUserLoggedIn(User user) throws ControllerException;

    /**
     * Returns a user preference (name/value pair).
     * 
     * @param user the user for whom to retrieve the preferences
     * @return a <code>Properties</code> that contains the name/value pairs of preferences
     * @throws ControllerException if the preferences could not be retrieved
     */
    public abstract Properties getUserPreferences(User user) throws ControllerException;

    /**
     * Sets a user preference. For example, the dashboard refresh interval.
     * 
     * @param user the user for whom to set the preference
     * @param name the name of the preference
     * @param value the value of the preference
     * @throws ControllerException if the preference could not be set
     */
    public abstract void setUserPreference(User user, String name, String value) throws ControllerException;
}
