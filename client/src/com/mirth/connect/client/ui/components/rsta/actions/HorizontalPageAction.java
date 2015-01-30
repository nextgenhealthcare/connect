/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.text.Document;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;

public class HorizontalPageAction extends MirthRecordableTextAction {

    private boolean left;

    public HorizontalPageAction(MirthRSyntaxTextArea textArea, boolean left) {
        super(textArea, left ? ActionInfo.PAGE_LEFT_SELECT : ActionInfo.PAGE_RIGHT_SELECT);
        this.left = left;
    }

    public void actionPerformedImpl(ActionEvent evt) {
        Rectangle visible = new Rectangle();
        textArea.computeVisibleRect(visible);

        if (left) {
            visible.x = Math.max(0, visible.x - visible.width);
        } else {
            visible.x += visible.width;
        }

        int selectedIndex = textArea.getCaretPosition();

        if (selectedIndex != -1) {
            if (left) {
                selectedIndex = textArea.viewToModel(new Point(visible.x, visible.y));
            } else {
                selectedIndex = textArea.viewToModel(new Point(visible.x + visible.width - 1, visible.y + visible.height - 1));
            }

            Document doc = textArea.getDocument();
            if (selectedIndex != 0 && selectedIndex > doc.getLength() - 1) {
                selectedIndex = doc.getLength() - 1;
            } else if (selectedIndex < 0) {
                selectedIndex = 0;
            }

            textArea.moveCaretPosition(selectedIndex);
        }
    }
}