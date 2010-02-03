/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;

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
     * @param id
     * @param template
     * @throws ControllerException
     */
    public void putTemplate(String id, String template) throws ControllerException {
        logger.debug("adding template: id=" + id);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("id", id);
            parameterMap.put("template", template);

            if (getTemplate(id) == null) {
                SqlConfig.getSqlMapClient().insert("Template.insertTemplate", parameterMap);
            } else {
                SqlConfig.getSqlMapClient().update("Template.updateTemplate", parameterMap);
            }
        } catch (SQLException e) {
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
    public String getTemplate(String id) throws ControllerException {
        logger.debug("retrieving template: id=" + id);

        try {
            return (String) SqlConfig.getSqlMapClient().queryForObject("Template.getTemplate", id);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void clearTemplates() throws ControllerException {
        logger.debug("clearing templates table");

        try {
            SqlConfig.getSqlMapClient().delete("Template.deleteTemplate", null);
            
            if (DatabaseUtil.statementExists("Template.vacuumTemplateTable")) {
                SqlConfig.getSqlMapClient().update("Template.vacuumTemplateTable");
            }
            
        } catch (SQLException e) {
            throw new ControllerException("error clearing templates", e);
        }
    }
}
