/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultTemplateController extends TemplateController {
    private Logger logger = Logger.getLogger(this.getClass());

    private static DefaultTemplateController instance = null;

    private DefaultTemplateController() {

    }

    public static TemplateController create() {
        synchronized (DefaultTemplateController.class) {
            if (instance == null) {
                instance = new DefaultTemplateController();
            }

            return instance;
        }
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
    public void putTemplate(String groupId, String id, String template) throws ControllerException {
        logger.debug("adding template: groupId=" + groupId + ", id=" + id);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);
            parameterMap.put("template", template);

            if (getTemplate(groupId, id) == null) {
                SqlConfig.getSqlSessionManager().insert("Template.insertTemplate", parameterMap);
            } else {
                SqlConfig.getSqlSessionManager().update("Template.updateTemplate", parameterMap);
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Returns the template with the specified id, null otherwise.
     * 
     * @param id
     * @return
     * @throws ControllerException
     */
    public String getTemplate(String groupId, String id) throws ControllerException {
        logger.debug("retrieving template: groupId=" + groupId + ", id=" + id);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);

            return (String) SqlConfig.getSqlSessionManager().selectOne("Template.getTemplate", parameterMap);
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    public void removeTemplates(String groupId) throws ControllerException {
        logger.debug("removing templates: groupId=" + groupId);

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("groupId", groupId);
        
        try {
            SqlConfig.getSqlSessionManager().delete("Template.deleteTemplate", parameterMap);
        } catch (PersistenceException e) {
            throw new ControllerException("error clearing templates", e);
        }
    }

    public void removeAllTemplates() throws ControllerException {
        logger.debug("clearing templates table");

        try {
            SqlConfig.getSqlSessionManager().delete("Template.deleteTemplate");
            
            if (DatabaseUtil.statementExists("Template.vacuumTemplateTable")) {
                SqlConfig.getSqlSessionManager().update("Template.vacuumTemplateTable");
            }
        } catch (PersistenceException e) {
            throw new ControllerException("error clearing templates", e);
        }
    }
}
