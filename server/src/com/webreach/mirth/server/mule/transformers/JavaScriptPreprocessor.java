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

package com.webreach.mirth.server.mule.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.CodeTemplate.CodeSnippetType;
import com.webreach.mirth.server.controllers.CodeTemplateController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.GlobalVariableStore;
import com.webreach.mirth.server.util.VMRouter;

public class JavaScriptPreprocessor extends AbstractEventAwareTransformer {
	private String preprocessingScriptId;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
	private CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
	private static String LOCAL_DEFAULT_SCRIPT = "return message;";

	public String getPreprocessingScriptId() {
		return this.preprocessingScriptId;
	}

	public void setPreprocessingScriptId(String preprocessingScriptId) {
		this.preprocessingScriptId = preprocessingScriptId;
	}

	@Override
	public void initialise() throws InitialisationException {
		boolean createdContext = false;
		
		try {
			String preprocessingScript = scriptController.getScript(preprocessingScriptId);

			if ((preprocessingScript != null) && (preprocessingScript.length() > 0)) {
				Context context = Context.enter();
				createdContext = true;

				logger.debug("compiling preprocessing script");
				String generatedPreprocessingScript = generatePreprocessingScript(preprocessingScript);
				Script compiledPreprocessingScript = context.compileString(generatedPreprocessingScript, preprocessingScriptId, 1, null);
				String decompiledPreprocessingScript = context.decompileScript(compiledPreprocessingScript, 0);

				Script compiledDefaultScript = context.compileString(generatePreprocessingScript(LOCAL_DEFAULT_SCRIPT), preprocessingScriptId, 1, null);
				String decompiledDefaultScript = context.decompileScript(compiledDefaultScript, 0);

				if (!decompiledDefaultScript.equals(decompiledPreprocessingScript)) {
					logger.debug("adding preprocessor script");
					compiledScriptCache.putCompiledScript(preprocessingScriptId, compiledPreprocessingScript, generatedPreprocessingScript);
				}
			} else {
				logger.debug("clearing preprocessor script from previous deploy");
				compiledScriptCache.removeCompiledScript(preprocessingScriptId);
			}
		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			if (createdContext) {
				Context.exit();	
			}
		}
	}

	@Override
	public Object transform(Object src, UMOEventContext context) throws TransformerException {
		String message = new String();

		if (src instanceof MessageObject) {
			// message = ((MessageObject)src).getEncodedData();
			return src;
		} else if (src instanceof String) {
			message = (String) src;
		}

		return doPreprocess(message, context);
	}

	public String doPreprocess(String message, UMOEventContext muleContext) throws TransformerException {
		boolean createdContext = false;
		
		try {
			Script globalCompiledScript = compiledScriptCache.getCompiledScript(ConfigurationController.GLOBAL_PREPROCESSOR_KEY);
			Script compiledScript = compiledScriptCache.getCompiledScript(preprocessingScriptId);
			String returnValue = message;
			
			if ((compiledScript != null) || (globalCompiledScript != null)) {
				Logger scriptLogger = Logger.getLogger("preprocessor");
				Context context = Context.enter();
				createdContext = true;
				Scriptable scope = new ImporterTopLevel(context);
				List<Attachment> attachments = new ArrayList();
				muleContext.getProperties().put("attachments", attachments);
				scope.put("message", scope, message);
				scope.put("logger", scope, scriptLogger);
				scope.put("globalMap", scope, GlobalVariableStore.getInstance());
				scope.put("router", scope, new VMRouter());
				scope.put("muleContext", scope, muleContext);
				// Add the contextMap, it contains MuleContext properties
				Map contextMap = new HashMap();
				muleContext.getProperties().putAll(contextMap);
				scope.put("contextMap", scope, contextMap);

				if (globalCompiledScript != null) {
					Object result = globalCompiledScript.exec(context, scope);
					String processedMessage = (String) Context.jsToJava(result, java.lang.String.class);

					if (processedMessage != null) {
						returnValue = processedMessage;
					}
				}

				if (compiledScript != null) {
					Object result = compiledScript.exec(context, scope);
					String processedMessage = (String) Context.jsToJava(result, java.lang.String.class);

					if (processedMessage != null) {
						returnValue = processedMessage;
					}
				}
			}

			return returnValue;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			if (createdContext) {
				Context.exit();	
			}
		}
	}

	public String generatePreprocessingScript(String preprocessingScript) {
		logger.debug("generating preprocessing script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
		// The addAttachment function let's us dynamically put data into
		// attachment table
		script.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");

		script.append("function addAttachment(data, type) {");
		script.append("var attachment = Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().createAttachment(data, type);");
		script.append("muleContext.getProperties().get('attachments').add(attachment); \n");
		script.append("return attachment; }\n");

		try {
			List<CodeTemplate> templates = codeTemplateController.getCodeTemplate(null);
			for (CodeTemplate template : templates) {
				if (template.getType() == CodeSnippetType.FUNCTION) {
					if (template.getScope() == CodeTemplate.ContextType.GLOBAL_CONTEXT.getContext() || template.getScope() == CodeTemplate.ContextType.CHANNEL_CONTEXT.getContext()) {
						script.append(template.getCode());
					}
				}
			}
		} catch (ControllerException e) {
			logger.error("Could not get user functions.", e);
		}

		script.append("function doPreprocess() {" + preprocessingScript + " \n}\n");
		script.append("doPreprocess()\n");
		return script.toString();
	}
}