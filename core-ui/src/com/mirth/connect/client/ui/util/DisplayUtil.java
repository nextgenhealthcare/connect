/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Formatter;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.math.NumberUtils;

public class DisplayUtil {

    /**
     * This returns a formatted string that shows the number based on the locale turns 1000 into
     * 1,000
     * 
     * @param number
     * @return
     */
    public static String formatNumber(int number) {
        StringBuilder str = new StringBuilder();
        Formatter f = new Formatter(str, Locale.getDefault());
        f.format("%,d", number);
        return str.toString();
    }

    /**
     * This returns a formatted string that shows the number based on the locale turns 1000 into
     * 1,000
     * 
     * @param number
     * @return
     */
    public static String formatNumber(long number) {
        StringBuilder str = new StringBuilder();
        Formatter f = new Formatter(str, Locale.getDefault());
        f.format("%,d", number);
        return str.toString();
    }

    /**
     * Formats a number according to the locale turns 1000.0 to 1,000
     * 
     * @param number
     * @return
     */
    public static String formatNumber(float number) {
        StringBuilder str = new StringBuilder();
        Formatter f = new Formatter(str, Locale.getDefault());
        f.format("%,.0f", number);
        return str.toString();
    }

    /**
     * Sets a dialog's resizable property. When JDK 11 or greater is used, the property is forced to
     * be true due to https://bugs.openjdk.java.net/browse/JDK-8208743
     */
    public static void setResizable(Dialog dialog, boolean resizable) {
        if (isJDK11OrGreater()) {
            resizable = true;
        }
        dialog.setResizable(resizable);
    }

    /**
     * Sets a frame's resizable property. When JDK 11 or greater is used, the property is forced to
     * be true due to https://bugs.openjdk.java.net/browse/JDK-8208743
     */
    public static void setResizable(Frame frame, boolean resizable) {
        if (isJDK11OrGreater()) {
            resizable = true;
        }
        frame.setResizable(resizable);
    }

    public static boolean isJDK11OrGreater() {
        String version = System.getProperty("java.version");

        int index = version.indexOf('-');
        if (index > 0) {
            version = version.substring(0, index);
        }

        index = version.indexOf('.');
        if (index > 0) {
            version = version.substring(0, index);
        }

        return NumberUtils.toDouble(version) >= 11;
    }

    /**
     * Shows a confirmation dialog with a text input. This is needed due to
     * https://bugs.openjdk.java.net/browse/JDK-8208743.
     */
    public static String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) throws HeadlessException {
        return showInputDialog(parentComponent, message, "Input", JOptionPane.QUESTION_MESSAGE, null, null, initialSelectionValue);
    }

    /**
     * Shows a confirmation dialog with a text input. This is needed due to
     * https://bugs.openjdk.java.net/browse/JDK-8208743.
     */
    public static String showInputDialog(Component parentComponent, Object message, String title, int messageType) throws HeadlessException {
        return showInputDialog(parentComponent, message, title, messageType, null, null, null);
    }

    /**
     * Shows a confirmation dialog with a text input. This is needed due to
     * https://bugs.openjdk.java.net/browse/JDK-8208743.
     */
    public static String showInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) throws HeadlessException {
        JOptionPane optionPane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION);
        optionPane.setWantsInput(true);
        optionPane.setSelectionValues(selectionValues);
        optionPane.setInitialSelectionValue(initialSelectionValue);
        optionPane.setComponentOrientation((parentComponent == null ? JOptionPane.getRootFrame() : parentComponent).getComponentOrientation());

        JDialog dialog = optionPane.createDialog(parentComponent, title);
        // https://bugs.openjdk.java.net/browse/JDK-8208743
        DisplayUtil.setResizable(dialog, false);

        optionPane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();

        Object value = optionPane.getInputValue();

        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return (String) value;
    }
}
