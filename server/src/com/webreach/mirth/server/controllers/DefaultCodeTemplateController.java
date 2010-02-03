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
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;

public class DefaultCodeTemplateController extends CodeTemplateController {
    private Logger logger = Logger.getLogger(this.getClass());
    private static CodeTemplateController instance = null;

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
            return SqlConfig.getSqlMapClient().queryForList("CodeTemplate.getCodeTemplate", codeTemplate);
        } catch (SQLException e) {
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
                SqlConfig.getSqlMapClient().startTransaction();

                // insert the codeTemplate and its properties
                logger.debug("adding codeTemplate: " + codeTemplate);
                SqlConfig.getSqlMapClient().insert("CodeTemplate.insertCodeTemplate", codeTemplate);

                SqlConfig.getSqlMapClient().commitTransaction();
            } finally {
                SqlConfig.getSqlMapClient().endTransaction();
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeCodeTemplate(CodeTemplate codeTemplate) throws ControllerException {
        logger.debug("removing codeTemplate: " + codeTemplate);

        try {
            SqlConfig.getSqlMapClient().delete("CodeTemplate.deleteCodeTemplate", codeTemplate);
            
            if (DatabaseUtil.statementExists("CodeTemplate.vacuumCodeTemplateTable")) {
                SqlConfig.getSqlMapClient().update("CodeTemplate.vacuumCodeTemplateTable");
            }
            
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }
}
