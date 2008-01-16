package com.webreach.mirth.client.ui.actions;

import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

import org.syntax.jedit.JEditTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Oct 31, 2007
 * Time: 3:35:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class FindAndReplaceAction extends AbstractAction
{
    JEditTextArea comp;
    FindRplDialog find;
    Frame frame;

    public FindAndReplaceAction(Frame frame, JEditTextArea comp)
    {
        super("Find/Replace");
        this.comp = comp;
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent e)
    {
        find = new FindRplDialog(frame,true,comp);
        find.setVisible(true);
    }
    public boolean isEnabled()
    {
        return comp.isEnabled();
    }
}