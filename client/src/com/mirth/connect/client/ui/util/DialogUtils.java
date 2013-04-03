package com.mirth.connect.client.ui.util;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

public class DialogUtils {
    public static void registerEscapeKey(final JDialog dialog, ActionListener actionListener) {
        dialog.getRootPane().registerKeyboardAction(actionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
