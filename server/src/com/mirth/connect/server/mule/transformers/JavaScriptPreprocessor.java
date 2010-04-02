/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

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

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.VMRouter;

public class JavaScriptPreprocessor extends AbstractEventAwareTransformer {
    private String channelId;
	private String preprocessingScriptId;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
	private CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
	private static String LOCAL_DEFAULT_SCRIPT = "return message;";
	
	public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

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
			String preprocessingScript = scriptController.getScript(channelId, preprocessingScriptId);

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
				scope.put("globalChannelMap", scope, GlobalChannelVariableStoreFactory.getInstance().get(channelId));
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
		script.append("importPackage(Packages.com.mirth.connect.server.util);\n");
		// The addAttachment function let's us dynamically put data into
		// attachment table
		script.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");

		script.append("function addAttachment(data, type) {");
		script.append("var attachment = Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().createAttachment(data, type);");
		script.append("muleContext.getProperties().get('attachments').add(attachment); \n");
		script.append("return attachment; }\n");

		try {
			List<CodeTemplate> templates = codeTemplateController.getCodeTemplate(null);
			for (CodeTemplate template : templates) {
				if (template.getType() == CodeSnippetType.FUNCTION) {
					if (template.getScope() == CodeTemplate.ContextType.GLOBAL_CONTEXT.getContext() || template.getScope() == CodeTemplate.ContextType.GLOBAL_CHANNEL_CONTEXT.getContext() || template.getScope() == CodeTemplate.ContextType.CHANNEL_CONTEXT.getContext()) {
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
