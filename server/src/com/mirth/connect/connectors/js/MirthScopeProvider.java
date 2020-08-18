package com.mirth.connect.connectors.js;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

public class MirthScopeProvider implements ScopeProvider {
	
	private Scriptable scope;
	
	public void setScope(Scriptable scope) {
		this.scope = scope;
	}

	@Override
	public Scriptable getScope() {
		return scope;
	}

}
