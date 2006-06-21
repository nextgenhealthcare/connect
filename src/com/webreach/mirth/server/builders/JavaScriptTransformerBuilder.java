package com.webreach.mirth.server.builders;

import java.util.Iterator;

import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;

public class JavaScriptTransformerBuilder {
	public String getScript(Transformer transformer) throws BuilderException {
		StringBuilder builder = new StringBuilder();

		for (Iterator iter = transformer.getSteps().iterator(); iter.hasNext();) {
			Step step = (Step) iter.next();
			builder.append(step.getScript() + ";\n");
		}

		return builder.toString();
	}
}
