package com.webreach.mirth.client.ui.components;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.SyntaxDocument;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.actions.CopyAction;
import com.webreach.mirth.client.ui.actions.CutAction;
import com.webreach.mirth.client.ui.actions.DeleteAction;
import com.webreach.mirth.client.ui.actions.FindAndReplaceAction;
import com.webreach.mirth.client.ui.actions.PasteAction;
import com.webreach.mirth.client.ui.actions.RedoAction;
import com.webreach.mirth.client.ui.actions.SelectAllAction;
import com.webreach.mirth.client.ui.actions.ShowLineEndingsAction;
import com.webreach.mirth.client.ui.actions.SnippetAction;
import com.webreach.mirth.client.ui.actions.UndoAction;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory.ListType;
import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.CodeTemplate.ContextType;

/**
 * Mirth's implementation of the JTextArea. Adds enabling of the save button in
 * parent. Also adds a trigger button (right click) editor menu with Cut, Copy,
 * Paste, Delete, and Select All.
 */
public class MirthSyntaxTextArea extends JEditTextArea implements MirthTextInterface {

    private Frame parent;
    // private JPopupMenu menu;
    private CutAction cutAction;
    private CopyAction copyAction;
    private PasteAction pasteAction;
    private DeleteAction deleteAction;
    private SelectAllAction selectAllAction;
    private UndoAction undoAction;
    private RedoAction redoAction;
    private FindAndReplaceAction findReplaceAction;
    private ShowLineEndingsAction showLineEndingsAction;
    private JMenu varlist;
    private JMenu hl7list;
    private JMenu funclist;
    protected boolean showSnippets;

    public MirthSyntaxTextArea() {
        initialize(false, false, ContextType.GLOBAL_CONTEXT.getContext());
    }

    private void initialize(boolean lineNumbers, final boolean showSnippets, final int context) {
        this.parent = PlatformUI.MIRTH_FRAME;
        this.setFocusable(true);
        this.showSnippets = showSnippets;
        this.setCaretVisible(false);
        this.setShowLineEndings(false);
        // Setup menu actions
        cutAction = new CutAction(this);
        copyAction = new CopyAction(this);
        pasteAction = new PasteAction(this);
        deleteAction = new DeleteAction(this);
        selectAllAction = new SelectAllAction(this);
        undoAction = new UndoAction(this);
        redoAction = new RedoAction(this);
        findReplaceAction = new FindAndReplaceAction(this);
        showLineEndingsAction = new ShowLineEndingsAction(this);
        popup = new JPopupMenu();

        popup.add(undoAction);
        popup.add(redoAction);
        popup.addSeparator();
        popup.add(cutAction);
        popup.add(copyAction);
        popup.add(pasteAction);
        popup.addSeparator();
        popup.add(deleteAction);
        popup.addSeparator();
        popup.add(selectAllAction);
        popup.add(findReplaceAction);
        popup.add(new JCheckBoxMenuItem(showLineEndingsAction));

        if (showSnippets) {
            varlist = new JMenu("Built-in Variables");
            hl7list = new JMenu("HL7 Helpers");
            funclist = new JMenu("Built-in Functions");
            ReferenceListFactory functionBuilder = ReferenceListFactory.getInstance();
            ArrayList<CodeTemplate> jshelpers = functionBuilder.getVariableListItems(ListType.ALL.getValue(), context);
            Iterator<CodeTemplate> it = jshelpers.iterator();

            while (it.hasNext()) {
                CodeTemplate item = it.next();
                switch (item.getType()) {
                    case FUNCTION:
                        funclist.add(new SnippetAction(this, item.getName(), item.getCode()));
                        break;
                    case VARIABLE:
                        varlist.add(new SnippetAction(this, item.getName(), item.getCode()));
                        break;
                    case CODE:
                        funclist.add(new SnippetAction(this, item.getName(), item.getCode()));
                        break;
                }
            }
            popup.addSeparator();
            popup.add(varlist);
            popup.add(funclist);
            // popup.add(hl7list);
        }

        this.popupHandler = new PopUpHandler() {

            public void showPopupMenu(JPopupMenu menu, MouseEvent evt) {
                menu.getComponent(0).setEnabled(undoAction.isEnabled());
                menu.getComponent(1).setEnabled(redoAction.isEnabled());
                menu.getComponent(3).setEnabled(cutAction.isEnabled());
                menu.getComponent(4).setEnabled(copyAction.isEnabled());
                menu.getComponent(5).setEnabled(pasteAction.isEnabled());
                menu.getComponent(7).setEnabled(deleteAction.isEnabled());
                menu.getComponent(9).setEnabled(selectAllAction.isEnabled());
                menu.getComponent(10).setEnabled(findReplaceAction.isEnabled());
                menu.getComponent(11).setEnabled(showLineEndingsAction.isEnabled());
                if (isShowLineEndings()) {
                    ((JCheckBoxMenuItem) menu.getComponent(11)).setState(true);
                } else {
                    ((JCheckBoxMenuItem) menu.getComponent(11)).setState(false);
                }
                if (showSnippets) {
                    menu.getComponent(12).setEnabled(varlist.isEnabled());
                    menu.getComponent(13).setEnabled(funclist.isEnabled());
                    // menu.getComponent(14).setEnabled(hl7list.isEnabled());
                }
                menu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        };

    }

    public MirthSyntaxTextArea(boolean lineNumbers, final boolean showSnippets) {
        super(lineNumbers);
        initialize(lineNumbers, showSnippets, ContextType.GLOBAL_CONTEXT.getContext());
    }

    public MirthSyntaxTextArea(boolean lineNumbers, final boolean showSnippets, final int context) {
        super(lineNumbers);
        initialize(lineNumbers, showSnippets, context);
    }

    /*
     * Support for undo and redo
     * 
     */
    public void undo() {
        if (this.undo.canUndo()) {
            this.undo.undo();
        }
    }

    public void redo() {
        if (this.undo.canRedo()) {
            this.undo.redo();
        }
    }

    public boolean canRedo() {
        return this.undo.canRedo();
    }

    public boolean canUndo() {
        return this.undo.canUndo();
    }

    /**
     * Overrides setDocument(Document doc) so that a document listener is added
     * to the current document to listen for changes.
     */
    public void setDocument(SyntaxDocument doc) {

        super.setDocument(doc);
        this.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                parent.enableSave();
            }

            public void removeUpdate(DocumentEvent e) {
                parent.enableSave();
            }

            public void insertUpdate(DocumentEvent e) {
                parent.enableSave();
            }
        });

    }

    /**
     * Overrides setText(String t) so that the save button is disabled when
     * Mirth sets the text of a field.
     */
    public void setText(String t) {
        boolean visible = parent.changesHaveBeenMade();

        // if (t != null)
        // t = t.replaceAll("\r", "\n");
        super.setText(t);

        if (visible) {
            parent.enableSave();
        } else {
            parent.disableSave();
        }
    }

    public String getText() {
        return super.getText();
        /*
         * StringBuffer sb = new StringBuffer(); // Get paragraph element
         * Element paragraph = getDocument().getDefaultRootElement(); // Get
         * number of content elements int contentCount =
         * paragraph.getElementCount(); // Get index ranges for each content
         * element. // Each content element represents one line. // Each line
         * includes the terminating newline. for (int i = 0; i < contentCount;
         * i++) { Element e = paragraph.getElement(i); int rangeStart =
         * e.getStartOffset(); int rangeEnd = e.getEndOffset(); try { String
         * text = getText(rangeStart, rangeEnd - rangeStart);
         * sb.append(text.replaceAll("\\n", "").replaceAll("\\r", ""));
         * sb.append(System.getProperty("line.separator")); } catch (Exception
         * ex) { } } String retval = sb.toString(); if (retval.length() > 0) {
         * retval = retval.substring(0, retval.length() - 1); } return retval;
         */
    }

    public void replaceSelection(String text) {
        setSelectedText(text);

    }
}
