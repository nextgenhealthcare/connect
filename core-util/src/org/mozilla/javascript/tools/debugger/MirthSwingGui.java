package org.mozilla.javascript.tools.debugger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class MirthSwingGui extends SwingGui {

	Logger logger = LogManager.getLogger(getClass());
	private boolean stopping = false;
	private MirthMain parent;

	public MirthSwingGui(MirthMain parent, Dim dim, String title) {
		super(dim, title);
		this.parent = parent;
	}

	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}

	@Override
	protected void exit() {
		stopping = true;
        MirthMain meDim = this.parent;
        meDim.finishScriptExecution();
        meDim.setVisible(false);
    }

	@Override
	void enterInterruptImpl(Dim.StackFrame lastFrame, String threadTitle, String alertMessage) {
		if (!stopping) {
			super.enterInterruptImpl(lastFrame, threadTitle, alertMessage);
		}
	}
}
