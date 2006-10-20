package com.webreach.mirth.server.mule.transformers;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.mule.util.CompiledScriptCache;

public class JavaScriptPreprocessor extends AbstractTransformer {
	private String preprocessingScriptId;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = new ScriptController();

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
			if ((preprocessingScript != null) && !preprocessingScript.equals("")) {
				String generatedPreprocessingScript = generatePreprocessingScript(preprocessingScript);
				logger.debug("compiling preprocessing script");
				Script compiledPreprocessingScript = context.compileString(generatedPreprocessingScript, preprocessingScriptId, 1, null);
				compiledScriptCache.putCompiledScript(preprocessingScriptId, compiledPreprocessingScript);
			}
		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		String message = (String) src;
		return doPreprocess(message);
	}

	public String doPreprocess(String message) throws TransformerException {
		try {
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
			scope.put("message", scope, message);
			Script compiledScript = compiledScriptCache.getCompiledScript(preprocessingScriptId);
			String returnValue = message;

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
