/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;


public abstract class TemplateController extends Controller {
    public static TemplateController getInstance() {
        return ControllerFactory.getFactory().createTemplateController();
    }
    
    /**
     * Adds a template with the specified id to the database. If a template with
     * the id already exists it will be overwritten.
     * 
     * @param groupId
     * @param id
     * @param template
     * @throws ControllerException
     */
    public abstract void putTemplate(String groupId, String id, String template) throws ControllerException;

    /**
     * Returns the template with the specified id, null otherwise.
     * 
     * @param groupId
     * @param id
     * @return
     * @throws ControllerException
     */
    public abstract String getTemplate(String groupId, String id) throws ControllerException;

    public abstract void removeTemplates(String groupId) throws ControllerException;
    
    public abstract void removeAllTemplates() throws ControllerException;
}
