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

/**
 * Created by IntelliJ IDEA.
 * User: dank
 * Date: Nov 9, 2005
 * Time: 4:06:45 PM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

/*
**  This class will merge individual edits into a single larger edit.
**  That is, characters entered sequentially will be grouped together and
**  undone as a group. Any attribute changes will be considered as part
**  of the group and will therefore be undone when the group is undone.
*/

public class CompoundUndoManager extends UndoManager
        implements UndoableEditListener, DocumentListener {
    public CompoundEdit compoundEdit;
    private JTextComponent editor;

    //  These fields are used to help determine whether the edit is an
    //  incremental edit. For each character added the offset and length
    //  should increase by 1 or decrease by 1 for each character removed.

    private int lastOffset;
    private int lastLength;

    public CompoundUndoManager(JTextComponent editor) {
        this.editor = editor;
        editor.getDocument().addUndoableEditListener(this);
    }

    /*
     **  Add a DocumentLister before the undo is done so we can position
     **  the Caret correctly as each edit is undone.
     */
    public void undo() {
        CaretListener [] cl = editor.getCaretListeners();
        for (int i = 0; i < cl.length; ++i) {
            editor.removeCaretListener(cl[i]);
        }

        try {
            editor.getDocument().addDocumentListener(this);

            super.undo();

            editor.getDocument().removeDocumentListener(this);
        } finally {

            for (int i = 0; i < cl.length; ++i) {
                editor.addCaretListener(cl[i]);
                if (cl[i] instanceof EditorCaretListener) {
                    //trigger the caret update after we re-add the listener
                    ((EditorCaretListener) cl[i]).caretUpdate();
                }
            }
        }

    }

    /*
     **  Add a DocumentLister before the redo is done so we can position
     **  the Caret correctly as each edit is redone.
     */
    public void redo() {
        editor.getDocument().addDocumentListener(this);
        super.redo();
        editor.getDocument().removeDocumentListener(this);
    }

    /*
     **  Whenever an UndoableEdit happens the edit will either be absorbed
     **  by the current compound edit or a new compound edit will be started
     */
    public void undoableEditHappened(UndoableEditEvent e) {
        //  Start a new compound edit

        if (compoundEdit == null) {
            compoundEdit = startCompoundEdit(e.getEdit());
            lastLength = editor.getDocument().getLength();
            return;
        }

        //  Check for an attribute change

        AbstractDocument.DefaultDocumentEvent event =
                (AbstractDocument.DefaultDocumentEvent) e.getEdit();

        if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
            compoundEdit.addEdit(e.getEdit());
            return;
        }

        //  Check for an incremental edit or backspace.
        //  The change in Caret position and Document length should be either
        //  1 or -1 .

        int offsetChange = editor.getCaretPosition() - lastOffset;
        int lengthChange = editor.getDocument().getLength() - lastLength;

        if (Math.abs(offsetChange) == 1
                && Math.abs(lengthChange) == 1) {
            compoundEdit.addEdit(e.getEdit());
            lastOffset = editor.getCaretPosition();
            lastLength = editor.getDocument().getLength();
            return;
        }

        //  Not incremental edit, end previous edit and start a new one

        compoundEdit.end();
        compoundEdit = startCompoundEdit(e.getEdit());
    }

    /*
     **  Each CompoundEdit will store a group of related incremental edits
     **  (ie. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
        //  Track Caret and Document information of this compound edit

        lastOffset = editor.getCaretPosition();
        lastLength = editor.getDocument().getLength();

        //  The compound edit is used to store incremental edits

        compoundEdit = new MyCompoundEdit();
        compoundEdit.addEdit(anEdit);

        //  The compound edit is added to the UndoManager. All incremental
        //  edits stored in the compound edit will be undone/redone at once

        addEdit(compoundEdit);
        return compoundEdit;
    }

    //  Implement DocumentListener
    //
    // 	Updates to the Document as a result of Undo/Redo will cause the
    //  Caret to be repositioned

    public void insertUpdate(final DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int offset = e.getOffset() + e.getLength();
                offset = Math.min(offset, editor.getDocument().getLength());
                editor.setCaretPosition(offset);
            }
        });
    }

    public void removeUpdate(DocumentEvent e) {
        editor.setCaretPosition(e.getOffset());
    }

    public void changedUpdate(DocumentEvent e) {
    }


    class MyCompoundEdit extends CompoundEdit {
        public boolean isInProgress() {
            //  in order for the canUndo() and canRedo() methods to work
            //  assume that the compound edit is never in progress

            return false;
        }

        public void undo() throws CannotUndoException {
            //  End the edit so future edits don't get absorbed by this edit

            if (compoundEdit != null)
                compoundEdit.end();

            super.undo();

            //  Always start a new compound edit after an undo

            compoundEdit = null;
        }

    }

    public static void main(String[] args) {
        final JTextPane textPane = new JTextPane();
        textPane.setPreferredSize(new Dimension(200, 200));

        //  Comment out this code when not using SyntaxDocument class
        //  This class provides basic java syntax highlighting
        //  http://www.discoverteenergy.com/files/SyntaxDocument.java

        EditorKit editorKit = new StyledEditorKit() {
            public Document createDefaultDocument() {
                return new SyntaxDocument();
            }
        };
        textPane.setEditorKit(editorKit);

        //  End of code to comment out

        final CompoundUndoManager undoManager = new CompoundUndoManager(textPane);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        panel.add(buttons, BorderLayout.SOUTH);

        JButton undo = new JButton("Undo");
        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    undoManager.undo();
                    textPane.requestFocus();
                }
                catch (CannotUndoException ex) {
                    System.out.println("Unable to undo: " + ex);
                }
            }
        });
        buttons.add(undo);

        JButton redo = new JButton("Redo");
        redo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    undoManager.redo();
                    textPane.requestFocus();
                }
                catch (CannotRedoException ex) {
                    System.out.println("Unable to redo: " + ex);
                }
            }
        });
        buttons.add(redo);

        JFrame frame = new JFrame("Compound Edit");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}


