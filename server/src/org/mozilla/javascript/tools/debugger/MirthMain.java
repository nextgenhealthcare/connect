package org.mozilla.javascript.tools.debugger;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

public class MirthMain extends Main {

	public MirthMain(String title) {
		super(title);
		debugGui = new MirthSwingGui(dim, title);
	}

	public static Main mirthMainEmbedded(ContextFactory factory, Object scopeProvider, String title) {
		if (title == null) {
			title = "Rhino JavaScript Debugger (embedded usage)";
		}
		Main main = new MirthMain(title);
		main.doBreak();

		main.attachTo(factory);
		if (scopeProvider instanceof ScopeProvider) {
			main.setScopeProvider((ScopeProvider) scopeProvider);
		} else {
			Scriptable scope = (Scriptable) scopeProvider;
			if (scope instanceof Global) {
				Global global = (Global) scope;
				global.setIn(main.getIn());
				global.setOut(main.getOut());
				global.setErr(main.getErr());
			}
			main.setScope(scope);
		}

		main.pack();
		main.setSize(600, 460);
		main.setVisible(true);
		return main;
	}

}
