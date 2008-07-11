package com.webreach.mirth.client.ui.actions;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.syntax.jedit.JEditTextArea;

import com.webreach.mirth.client.ui.FindRplDialog;

public class FindAndReplaceAction extends AbstractAction
{
    JEditTextArea comp;
    FindRplDialog find;
    Window owner;

    public FindAndReplaceAction(Window owner, JEditTextArea comp)
    {
        super("Find/Replace");
        this.comp = comp;
        this.owner = owner;
    }

    public void actionPerformed(ActionEvent e)
    {
    	if (owner instanceof Frame) {
    		find = new FindRplDialog((Frame)owner,true,comp);
    	} else { // window instanceof Dialog
    		find = new FindRplDialog((Dialog)owner,true,comp);
    	}
    	
    	find.paintAll(find.getGraphics());
        find.setVisible(true);
    }
    public boolean isEnabled()
    {
        return comp.isEnabled();
    }
}