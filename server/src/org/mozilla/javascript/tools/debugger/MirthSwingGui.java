package org.mozilla.javascript.tools.debugger;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class MirthSwingGui extends SwingGui {

	Logger logger = Logger.getLogger(getClass());
	private boolean exiting = false;

	public MirthSwingGui(Dim dim, String title) {
		super(dim, title);
	}
	
	public void setExiting(boolean exiting) {
		this.exiting = exiting;
	}
	
	@Override
	protected void exit() {
		exiting = true;
        if (exitAction != null) {
            SwingUtilities.invokeLater(exitAction);
        }
        dim.setReturnValue(Dim.EXIT);
    }

	@Override
	void enterInterruptImpl(Dim.StackFrame lastFrame, String threadTitle, String alertMessage) {
		statusBar.setText("Thread: " + threadTitle);

		showStopLine(lastFrame);

		if (alertMessage != null) {
			MessageDialogWrapper.showMessageDialog(this, alertMessage, "Exception in Script",
					JOptionPane.ERROR_MESSAGE);
		}

		updateEnabled(true);

		Dim.ContextData contextData = lastFrame.contextData();

		JComboBox<String> ctx = context.context;
		List<String> toolTips = context.toolTips;
		context.disableUpdate();
		int frameCount = contextData.frameCount();
		ctx.removeAllItems();
		// workaround for JDK 1.4 bug that caches selected value even after
		// removeAllItems() is called
		ctx.setSelectedItem(null);
		toolTips.clear();
		for (int i = 0; i < frameCount; i++) {
			Dim.StackFrame frame = contextData.getFrame(i);
			String url = frame.getUrl();
			int lineNumber = frame.getLineNumber();
			String shortName = url;
			if (url.length() > 20) {
				shortName = "..." + url.substring(url.length() - 17);
			}
			String location = "\"" + shortName + "\", line " + lineNumber;
			ctx.insertItemAt(location, i);
			location = "\"" + url + "\", line " + lineNumber;
			toolTips.add(location);
		}
		context.enableUpdate();

		if (!exiting) {
			ctx.setSelectedIndex(0);
		}
		
		ctx.setMinimumSize(new Dimension(50, ctx.getMinimumSize().height));
	}
}
