/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.CodeTemplate;
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
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }
}
