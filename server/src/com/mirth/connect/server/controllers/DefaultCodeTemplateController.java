/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultCodeTemplateController extends CodeTemplateController {
    private Logger logger = Logger.getLogger(this.getClass());
    private static CodeTemplateController instance = null;

    private DefaultCodeTemplateController() {}

    public static CodeTemplateController create() {
        synchronized (DefaultCodeTemplateController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(CodeTemplateController.class);

                if (instance == null) {
                    instance = new DefaultCodeTemplateController();
                }
            }

            return instance;
        }
    }

    public List<CodeTemplate> getCodeTemplate(CodeTemplate codeTemplate) throws ControllerException {
        logger.debug("getting codeTemplate: " + codeTemplate);

        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            String id = (codeTemplate == null) ? null : codeTemplate.getId();
            List<Map<String, Object>> rows = SqlConfig.getSqlSessionManager().selectList("CodeTemplate.getCodeTemplate", id);
            List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
            
            for (Map<String, Object> row : rows) {
                try {
                    codeTemplates.add(serializer.deserialize((String) row.get("codeTemplate"), CodeTemplate.class));
                } catch (Exception e) {
                    logger.error("Failed to load code template " + row.get("id"), e);
                }
            }
            
            return codeTemplates;
        } catch (Exception e) {
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
            try {
                SqlConfig.getSqlSessionManager().startManagedSession();

                // insert the codeTemplate and its properties
                logger.debug("adding codeTemplate: " + codeTemplate);

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", codeTemplate.getId());
                params.put("codeTemplate", ObjectXMLSerializer.getInstance().serialize(codeTemplate));

                SqlConfig.getSqlSessionManager().insert("CodeTemplate.insertCodeTemplate", params);

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
            String id = codeTemplate == null ? null : codeTemplate.getId();
            SqlConfig.getSqlSessionManager().delete("CodeTemplate.deleteCodeTemplate", id);

            if (DatabaseUtil.statementExists("CodeTemplate.vacuumCodeTemplateTable")) {
                SqlConfig.getSqlSessionManager().update("CodeTemplate.vacuumCodeTemplateTable");
            }

        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }
}
