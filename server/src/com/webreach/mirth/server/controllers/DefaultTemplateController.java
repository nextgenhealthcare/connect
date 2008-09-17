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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.util.SqlConfig;

public class DefaultTemplateController implements TemplateController {
	private Logger logger = Logger.getLogger(this.getClass());

	private static DefaultTemplateController instance = null;
	
	private DefaultTemplateController() {
		
	}
	
	public static TemplateController getInstance() {
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
			Map parameterMap = new HashMap();
			parameterMap.put("id", id);
			parameterMap.put("template", template);

			if (getTemplate(id) == null) {
			    SqlConfig.getSqlMapClient().insert("insertTemplate", parameterMap);
			} else {
			    SqlConfig.getSqlMapClient().update("updateTemplate", parameterMap);
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
			return (String) SqlConfig.getSqlMapClient().queryForObject("getTemplate", id);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
	
	public void clearTemplates() throws ControllerException {
		logger.debug("clearing templates table");
		
		try {
		    SqlConfig.getSqlMapClient().delete("deleteTemplate", null);
		} catch (SQLException e) {
			throw new ControllerException("error clearing templates", e);
		}
	}

    public void initialize() {
        // no initialization
    }

    public boolean isInitialized() {
        return true;
    }
}
