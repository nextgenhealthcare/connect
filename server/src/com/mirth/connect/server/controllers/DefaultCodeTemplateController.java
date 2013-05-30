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

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultCodeTemplateController extends CodeTemplateController {
    private Logger logger = Logger.getLogger(this.getClass());
    private static CodeTemplateController instance = null;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    private DefaultCodeTemplateController() {

    }

    public static CodeTemplateController create() {
        synchronized (DefaultCodeTemplateController.class) {
            if (instance == null) {
                instance = new DefaultCodeTemplateController();
            }

            return instance;
        }
    }

    public List<CodeTemplate> getCodeTemplate(CodeTemplate codeTemplate) throws ControllerException {
        logger.debug("getting codeTemplate: " + codeTemplate);

        try {
            return SqlConfig.getSqlSessionManager().selectList("CodeTemplate.getCodeTemplate", codeTemplate);
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    public void updateCodeTemplates(List<CodeTemplate> codeTemplates) throws ControllerException {
        // remove all codeTemplates
        removeCodeTemplate(null);

        for (CodeTemplate codeTemplate : codeTemplates) {
            insertCodeTemplate(codeTemplate);
        }
    }

    private void insertCodeTemplate(CodeTemplate codeTemplate) throws ControllerException {
        try {
            CodeTemplate codeTemplateFilter = new CodeTemplate();
            codeTemplateFilter.setId(codeTemplate.getId());

            try {
                SqlConfig.getSqlSessionManager().startManagedSession();

                // insert the codeTemplate and its properties
                logger.debug("adding codeTemplate: " + codeTemplate);
                SqlConfig.getSqlSessionManager().insert("CodeTemplate.insertCodeTemplate", codeTemplate);

                SqlConfig.getSqlSessionManager().commit();
            } finally {
                SqlConfig.getSqlSessionManager().close();
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    public void removeCodeTemplate(CodeTemplate codeTemplate) throws ControllerException {
        logger.debug("removing codeTemplate: " + codeTemplate);

        try {
            SqlConfig.getSqlSessionManager().delete("CodeTemplate.deleteCodeTemplate", codeTemplate);

            if (DatabaseUtil.statementExists("CodeTemplate.vacuumCodeTemplateTable")) {
                SqlConfig.getSqlSessionManager().update("CodeTemplate.vacuumCodeTemplateTable");
            }

        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }
}
