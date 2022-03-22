package org.mozilla.javascript.tools.debugger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

public class MirthMain extends Main {
    private static Map<String, MirthMain> mainInstanceMap = new ConcurrentHashMap<>();

	public MirthMain(String title) {
	    //sets up dim/swingGUI then overwrites them with a "mirth" version
		super(title);
		dim = new MirthDim();
        debugGui = new MirthSwingGui(this, dim, title);
	}

    private static MirthMain createInstance(String title, ContextFactory factory, Object scopeProvider, String scriptId){
        MirthMain workingMain = null;
        String key = (scriptId!=null)?scriptId:title;

        workingMain = mainInstanceMap.get(key); //get instance

        if (workingMain != null) {
            if (workingMain.dim ==null) workingMain.dim = new MirthDim();
            return workingMain;
        } else {
            workingMain = new MirthMain(key);
            workingMain.doBreak();

            workingMain.attachTo(factory);
            if (scopeProvider instanceof ScopeProvider) {
                workingMain.setScopeProvider((ScopeProvider) scopeProvider);
            } else {
                Scriptable scope = (Scriptable) scopeProvider;
                if (scope instanceof Global) {
                    Global global = (Global) scope;
                    global.setIn(workingMain.getIn());
                    global.setOut(workingMain.getOut());
                    global.setErr(workingMain.getErr());
                }
                workingMain.setScope(scope);
            }
            mainInstanceMap.put(key, workingMain);
        }
        return workingMain;
    }

	public static MirthMain mirthMainEmbedded(ContextFactory factory, Object scopeProvider, String title, String scriptId, boolean showWindow) {
		if (title == null) {
			title = "Rhino JavaScript Debugger (embedded usage)";
		}
		MirthMain embeddedMain = createInstance(title, factory, scopeProvider, scriptId);

		embeddedMain.pack();
		embeddedMain.setSize(600, 460);
		embeddedMain.setVisible(showWindow);
		return embeddedMain;
	}
	
	   public static MirthMain mirthMainEmbedded(ContextFactory factory, Object scopeProvider, String title, String scriptId) {
	        return mirthMainEmbedded(factory, scopeProvider, title, scriptId, true);
	    }

	@Override
	public void setVisible(boolean flag) {
		if (flag) {
			if (debugGui != null) {
				MirthSwingGui mirthDebugGui = (MirthSwingGui)debugGui;
				mirthDebugGui.setStopping(!flag);
			}
			if (dim != null) {
				((MirthDim)dim).setStopping(!flag);
			}
		}
		super.setVisible(flag);
	}

	@Override
	public void dispose() {
	    this.finishScriptExecution();
	    if (dim != null) {
	    	this.detach();
	    }
	    this.removeFromMap();
	    if (debugGui != null) {
	    	debugGui.dispose();
	    }
		dim = null;
	}

	public void finishScriptExecution() {
		if (debugGui != null) {
			((MirthSwingGui) debugGui).setStopping(true);
		}
		if (dim != null) {
			((MirthDim) dim).setStopping(true);
			dim.clearAllBreakpoints();
			dim.go();
		}
	}

	public void enableDebugging() {
		doBreak();
		if (debugGui != null) {
			((MirthSwingGui) debugGui).setStopping(false);
		}
		if (dim != null) {
			((MirthDim) dim).setStopping(false);
		}
	}

    private void removeFromMap() {
    	if (debugGui != null) {
    		String key = debugGui.getTitle();
        	mainInstanceMap.remove(key);
    	}
    }

	public static void closeDebugger(String channelId) {
		for (String key : mainInstanceMap.keySet()) {
			if (key.contains(channelId)) {
				MirthMain closingMain = mainInstanceMap.get(key);
				closingMain.dispose();
			}
		}
	}
}
