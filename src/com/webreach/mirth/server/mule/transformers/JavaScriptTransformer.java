package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(JavaScriptTransformer.class);
	private String script;

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public Object doTransform(Object source) throws TransformerException {
		try {
			Context context = Context.enter();
			Scriptable scope = context.initStandardObjects();
			HashMap map = new HashMap();

			scope.put("message", scope, source);
			scope.put("logger", scope, logger);
			scope.put("map", scope, map);

			String jsSource = "function debug(debug_message) { logger.debug(debug_message) } function doTransform() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " } doTransform()";
			logger.debug("executing transformation script:\n\t" + jsSource);
			context.evaluateString(scope, jsSource, "<cmd>", 1, null);

			return map;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}
}
