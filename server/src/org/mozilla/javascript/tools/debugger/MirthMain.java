package org.mozilla.javascript.tools.debugger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

public class MirthMain extends Main {
    private static Map<String, MirthMain> mainInstanceMap = new HashedMap<>();

	public MirthMain(String title) {
	    //sets up dim/swinggui then overwrites them with a "mirth" version
		super(title);
		dim = new MirthDim();
        debugGui = new MirthSwingGui(dim, title);
	}
	
    private static MirthMain createInstance(String title, ContextFactory factory, Object scopeProvider, String scriptId){
        MirthMain workingMain = null;
        String key = (scriptId!=null)?scriptId:title;
        
        workingMain = mainInstanceMap.get(key); //get instance
        
        if (workingMain != null) {
            return workingMain;
        } else {
            workingMain = new MirthMain(title);
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

	public static MirthMain mirthMainEmbedded(ContextFactory factory, Object scopeProvider, String title, String scriptId) {
		if (title == null) {
			title = "Rhino JavaScript Debugger (embedded usage)";
		}
		MirthMain embeddedMain = createInstance(title, factory, scopeProvider, scriptId);
		
		embeddedMain.pack();
		embeddedMain.setSize(600, 460);
		embeddedMain.setVisible(true);
		return embeddedMain;
	}
	
	@Override
	public void setVisible(boolean flag) {
		if (flag) {
			MirthSwingGui mirthDebugGui = (MirthSwingGui)debugGui;
			mirthDebugGui.setStopping(!flag);
			((MirthDim)dim).setStopping(!flag);
		}
		super.setVisible(flag);
	}

	@Override
	public void dispose() {
		debugGui.dispose();
		dim = null;
	}
	
	public void finishScriptExecution() {
		((MirthSwingGui) debugGui).setStopping(true);
		((MirthDim) dim).setStopping(true);
		dim.clearAllBreakpoints();
		dim.go();
	}
	
	public void enableDebugging() {
		doBreak();
		((MirthSwingGui) debugGui).setStopping(false);
		((MirthDim) dim).setStopping(false);
	}

	public static void closeDebugger(String channelId) {
		Set<String> keysToRemove = new HashSet<>();
		
		for (String key : mainInstanceMap.keySet()) {
			if (key.contains(channelId)) {
				MirthMain closingMain = mainInstanceMap.get(key);
				closingMain.finishScriptExecution();
				closingMain.setVisible(false);
				closingMain.dispose();
				keysToRemove.add(key);
			}
		}
		
		for (String key : keysToRemove) {
			mainInstanceMap.remove(key);
		}

	}
}
