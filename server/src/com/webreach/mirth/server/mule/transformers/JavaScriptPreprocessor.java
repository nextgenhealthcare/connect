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

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.util.CompiledScriptCache;

public class JavaScriptPreprocessor extends AbstractTransformer {
	private String preprocessingScriptId;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ScriptController.getInstance();

	public String getPreprocessingScriptId() {
		return this.preprocessingScriptId;
	}

	public void setPreprocessingScriptId(String preprocessingScriptId) {
		this.preprocessingScriptId = preprocessingScriptId;
	}

	@Override
	public void initialise() throws InitialisationException {
		try {
			Context context = Context.enter();

			String preprocessingScript = scriptController.getScript(preprocessingScriptId);
			
			if ((preprocessingScript != null) && (preprocessingScript.length() > 0) && !preprocessingScript.equals("// Modify the message variable below to pre process data\nreturn message;")) {
				String generatedPreprocessingScript = generatePreprocessingScript(preprocessingScript);
				logger.debug("compiling preprocessing script");
				Script compiledPreprocessingScript = context.compileString(generatedPreprocessingScript, preprocessingScriptId, 1, null);
				compiledScriptCache.putCompiledScript(preprocessingScriptId, compiledPreprocessingScript);
				logger.debug("adding preprocessor script");
			}else{
				logger.debug("removing preprocessor script");
				compiledScriptCache.removeCompiledScript(preprocessingScriptId);
			}
		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		String message = new String();

		if (src instanceof MessageObject) {
			// message = ((MessageObject)src).getEncodedData();
			return src;
		} else if (src instanceof String) {
			message = (String) src;
		}
		
		return doPreprocess(message);
	}

	public String doPreprocess(String message) throws TransformerException {
		try {
			Logger scriptLogger = Logger.getLogger("preprocessor");
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
			scope.put("message", scope, message);
			scope.put("logger", scope, scriptLogger);
			
            Script globalCompiledScript = compiledScriptCache.getCompiledScript("Preprocessor");
			Script compiledScript = compiledScriptCache.getCompiledScript(preprocessingScriptId);
			String returnValue = message;
			
            if(globalCompiledScript != null)
            {
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

			return returnValue;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	public String generatePreprocessingScript(String preprocessingScript) {
		logger.debug("generating preprocessing script");
		StringBuilder script = new StringBuilder();
		script.append("function doPreprocess() {" + preprocessingScript + " }\n");
		script.append("doPreprocess()\n");
		return script.toString();
	}
}
