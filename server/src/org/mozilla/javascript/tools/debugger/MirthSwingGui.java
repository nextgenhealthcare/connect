package org.mozilla.javascript.tools.debugger;

import org.apache.log4j.Logger;

public class MirthSwingGui extends SwingGui {

	Logger logger = Logger.getLogger(getClass());
	private boolean stopping = false;

	public MirthSwingGui(Dim dim, String title) {
		super(dim, title);
	}
	
	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}
	
	@Override
	protected void exit() {
		stopping = true;
		((MirthDim)dim).setStopping(true);
        dim.clearAllBreakpoints();
        dim.go();
    }

	@Override
	void enterInterruptImpl(Dim.StackFrame lastFrame, String threadTitle, String alertMessage) {
		if (!stopping) {
			super.enterInterruptImpl(lastFrame, threadTitle, alertMessage);
		}
	}
}
