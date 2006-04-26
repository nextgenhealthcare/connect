/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.applets.editor;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: dank
 * Date: Nov 15, 2005
 * Time: 11:07:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditorCaretListener implements CaretListener {

    private JTextComponent editor;
    private JTextField positionLabel;

    public EditorCaretListener(JTextComponent editor, JTextField positionLabel) {
        this.editor = editor;
        this.positionLabel = positionLabel;
    }

    public void caretUpdate(CaretEvent e) {
        caretUpdate();
    }

    public void caretUpdate() {
        int pos = editor.getCaretPosition();
        int currentLine = 1;
        int currentColumn = 1;

        try {
            Rectangle current = editor.modelToView(pos);

            if (current == null)
                throw new BadLocationException("null Rectangle", pos);

            Rectangle start = editor.modelToView(0);                                    
            int width = 7;

            currentLine = ((current.y - start.y) / current.height) + 1;
            currentColumn = ((current.x - start.x) / width) + 1;
        }
        catch (BadLocationException ble) {
        }

        finally {
            positionLabel.setText(currentLine + ":" + currentColumn);
        }
    }
}
