/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.ArrayUtils;

import com.mirth.connect.client.ui.PlatformUI;

public class KeyStrokeTextField extends JTextField implements KeyListener {

    private static final int[] modifierKeyCodes = new int[] { KeyEvent.VK_SHIFT,
            KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_ALT_GRAPH, KeyEvent.VK_META };
    private static final int[] modifierMasks = new int[] { InputEvent.SHIFT_MASK,
            InputEvent.CTRL_MASK, InputEvent.ALT_MASK, InputEvent.ALT_GRAPH_MASK,
            InputEvent.META_MASK };
    private static final int[] ignoreKeyCodes = new int[] { KeyEvent.VK_BACK_SPACE, KeyEvent.VK_S,
            KeyEvent.VK_WINDOWS };

    private Integer keyCode;
    private int modifiers;

    public KeyStrokeTextField() {
        addKeyListener(this);
        setDragEnabled(false);
    }

    public KeyStroke getKeyStroke() {
        return keyCode != null ? KeyStroke.getKeyStroke(keyCode, modifiers) : null;
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        if (keyStroke != null) {
            keyCode = keyStroke.getKeyCode();
            modifiers = keyStroke.getModifiers();
            updateKeyStroke();
        } else {
            reset();
        }
    }

    @Override
    public void keyTyped(KeyEvent evt) {
        if (keyCode != null && (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            return;
        }

        // Clear the text field if backspace is pressed
        if (evt.getKeyCode() == KeyEvent.VK_BACK_SPACE || evt.getKeyChar() == '\b') {
            reset();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        evt.consume();
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        if (keyCode != null && (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            return;
        }

        if (keyCode == null && evt.getKeyCode() > 0 && !ArrayUtils.contains(ignoreKeyCodes, evt.getKeyCode())) {
            int index = ArrayUtils.indexOf(modifierKeyCodes, evt.getKeyCode());
            if (index >= 0) {
                modifiers |= modifierMasks[index];
            } else {
                keyCode = evt.getKeyCode();
            }

            updateKeyStroke();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        evt.consume();
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (keyCode != null && (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            return;
        }

        if (keyCode == null && evt.getKeyCode() > 0 && modifiers > 0 && !ArrayUtils.contains(ignoreKeyCodes, evt.getKeyCode())) {
            int index = ArrayUtils.indexOf(modifierKeyCodes, evt.getKeyCode());
            if (index >= 0 && keyCode == null) {
                modifiers = (modifiers ^= modifierMasks[index]) & modifiers;
            } else {
                keyCode = evt.getKeyCode();
            }

            updateKeyStroke();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        evt.consume();
    }

    private void reset() {
        keyCode = null;
        modifiers = 0;
        updateKeyStroke();
    }

    private void updateKeyStroke() {
        StringBuilder builder = new StringBuilder();

        for (int mask : modifierMasks) {
            if ((modifiers & mask) > 0) {
                if (builder.length() > 0) {
                    builder.append('+');
                }
                builder.append(KeyEvent.getKeyModifiersText(mask));
            }
        }

        if (keyCode != null) {
            if (builder.length() > 0) {
                builder.append('-');
            }
            builder.append(KeyEvent.getKeyText(keyCode));
        }

        setText(builder.toString());
    }
}