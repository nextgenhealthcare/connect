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

package com.webreach.mirth.client.ui.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.jdesktop.swingx.JXList;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.VariableListHandler;

/**
 * An implementation of JXList that has mouse rollover selection implemented.
 */
public class MirthVariableList extends JXList
{

    public MirthVariableList()
    {
        this("${", "}");
    }
    
    public void setPrefixAndSuffix(String prefix, String suffix){
        this.setTransferHandler(new VariableListHandler(prefix, suffix));
    }
    /**
     * Creates a new instance of MirthVariableList
     */
    public MirthVariableList(String prefix, String suffix)
    {
        super();
        this.setDragEnabled(true);
        setPrefixAndSuffix(prefix, suffix);
        this.setFocusable(false);
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
        {
            public void mouseMoved(java.awt.event.MouseEvent evt)
            {
                mirthListMouseMoved(evt);
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseExited(java.awt.event.MouseEvent evt)
            {
                mirthListMouseExited(evt);
            }
        });
        this.addKeyListener(new KeyListener()
        {

            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                	PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }

            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void keyTyped(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

        });
    }

    /**
     * When leaving the variable list, the selection is cleared.
     */
    private void mirthListMouseExited(java.awt.event.MouseEvent evt)
    {
        this.clearSelection();
    }

    /**
     * When moving on the variable list, set the selection to whatever the mouse
     * is over.
     */
    private void mirthListMouseMoved(java.awt.event.MouseEvent evt)
    {
        int index = this.locationToIndex(evt.getPoint());

        if (index != -1)
            this.setSelectedIndex(index);
    }
}
