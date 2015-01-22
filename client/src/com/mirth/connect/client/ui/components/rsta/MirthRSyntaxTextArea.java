/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.EOLPreservingRSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.InsertBreakAction;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTextInterface;
import com.mirth.connect.client.ui.components.rsta.actions.ClearMarkedOccurrencesAction;
import com.mirth.connect.client.ui.components.rsta.actions.CopyAction;
import com.mirth.connect.client.ui.components.rsta.actions.CutAction;
import com.mirth.connect.client.ui.components.rsta.actions.FindNextAction;
import com.mirth.connect.client.ui.components.rsta.actions.FindReplaceAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowLineEndingsAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowTabLinesAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowWhitespaceAction;
import com.mirth.connect.client.ui.components.rsta.actions.ViewUserAPIAction;
import com.mirth.connect.client.ui.components.rsta.actions.WrapLinesAction;
import com.mirth.connect.model.CodeTemplate.ContextType;

public class MirthRSyntaxTextArea extends RSyntaxTextArea implements MirthTextInterface {

    private boolean saveEnabled = true;
    private FindReplaceProperties findReplaceProperties = new FindReplaceProperties();
    private JMenuItem findReplaceMenuItem;
    private JMenuItem findNextMenuItem;
    private JMenuItem clearMarkedOccurrencesMenuItem;
    private JMenu displayMenu;
    private JMenuItem showTabLinesMenuItem;
    private JMenuItem showWhitespaceMenuItem;
    private JMenuItem showLineEndingsMenuItem;
    private JMenuItem wrapLinesMenuItem;
    private JMenuItem viewUserAPIMenuItem;
    private String cachedStyleKey;

    public MirthRSyntaxTextArea() {
        this(SYNTAX_STYLE_JAVASCRIPT);
    }

    public MirthRSyntaxTextArea(String styleKey) {
        this(styleKey, ContextType.GLOBAL_CONTEXT.getContext());
    }

    public MirthRSyntaxTextArea(String styleKey, int context) {
        this(styleKey, context, true);
    }

    public MirthRSyntaxTextArea(String styleKey, int context, boolean autoCompleteEnabled) {
        super(new MirthRSyntaxDocument(styleKey));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setSyntaxEditingStyle(styleKey);
        setCodeFoldingEnabled(true);
        setAntiAliasingEnabled(true);
        setWrapStyleWord(true);
        setAutoIndentEnabled(true);

        // Add a document listener so that the save button is enabled whenever changes are made.
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            private void onChange() {
                if (saveEnabled) {
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (saveEnabled) {
                    boolean isAccelerated = (((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) || ((e.getModifiers() & InputEvent.CTRL_MASK) > 0));
                    if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                        PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                    }
                }
            }
        });

        int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // Use custom cut/copy actions to handle EOLs
        getActionMap().put(DefaultEditorKit.cutAction, new CutAction(this));
        getActionMap().put(DefaultEditorKit.copyAction, new CopyAction(this));

        Action clearMarkedOccurrencesAction = new ClearMarkedOccurrencesAction(this);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Clear Marked Occurrences");
        getActionMap().put("Clear Marked Occurrences", clearMarkedOccurrencesAction);

        // Map the home/end buttons to start/end of line actions
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), DefaultEditorKit.beginLineAction);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), DefaultEditorKit.endLineAction);

        // Map a regular enter keypress to an LF character insertion
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Insert LF Break");
        getActionMap().put("Insert LF Break", new InsertBreakAction("\n"));

        // Map a shift + enter keypress to a CR character insertion
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK), "Insert CR Break");
        getActionMap().put("Insert CR Break", new InsertBreakAction("\r"));

        findReplaceMenuItem = new CustomMenuItem(this, new FindReplaceAction(this), KeyEvent.VK_F, defaultModifier);
        findNextMenuItem = new CustomMenuItem(this, new FindNextAction(this), KeyEvent.VK_G, defaultModifier);
        clearMarkedOccurrencesMenuItem = new CustomMenuItem(this, clearMarkedOccurrencesAction);
        displayMenu = new JMenu("Display");
        showTabLinesMenuItem = new JCheckBoxMenuItem(new ShowTabLinesAction(this));
        showWhitespaceMenuItem = new JCheckBoxMenuItem(new ShowWhitespaceAction(this));
        showLineEndingsMenuItem = new JCheckBoxMenuItem(new ShowLineEndingsAction(this));
        wrapLinesMenuItem = new JCheckBoxMenuItem(new WrapLinesAction(this));
        viewUserAPIMenuItem = new CustomMenuItem(this, new ViewUserAPIAction(this));

        initPopupMenuLayout();
        if (autoCompleteEnabled) {
            LanguageSupportFactory.get().register(this);
        }
    }

    public boolean isSaveEnabled() {
        return saveEnabled;
    }

    public void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
    }

    public FindReplaceProperties getFindReplaceProperties() {
        return findReplaceProperties;
    }

    public String getEOLFixedText() {
        if (getDocument() instanceof EOLPreservingRSyntaxDocument) {
            try {
                return ((EOLPreservingRSyntaxDocument) getDocument()).getEOLFixedText(0, getDocument().getLength());
            } catch (BadLocationException e) {
            }
        }
        return getText();
    }

    public String getEOLFixedSelectedText() {
        String txt = null;
        int p0 = Math.min(getCaret().getDot(), getCaret().getMark());
        int p1 = Math.max(getCaret().getDot(), getCaret().getMark());
        if (p0 != p1) {
            try {
                MirthRSyntaxDocument doc = (MirthRSyntaxDocument) getDocument();
                txt = doc.getEOLFixedText(p0, p1 - p0);
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return txt;
    }

    @Override
    public void setText(String text) {
        boolean visible = PlatformUI.MIRTH_FRAME.changesHaveBeenMade();
        super.setText(text);
        discardAllEdits();

        if (visible) {
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        } else {
            PlatformUI.MIRTH_FRAME.setSaveEnabled(false);
        }

        if (!isEnabled()) {
            setBracketMatchingEnabled(false);
            super.setSyntaxEditingStyle(SYNTAX_STYLE_NONE);
        }
    }

    public void setSelectedText(String text) {
        replaceSelection(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setForeground(UIManager.getColor(enabled ? "Label.foreground" : "Label.disabledForeground"));
        setHighlightCurrentLine(enabled);
        setBracketMatchingEnabled(enabled);

        if (enabled) {
            if (cachedStyleKey != null) {
                super.setSyntaxEditingStyle(cachedStyleKey);
            }
        } else {
            super.setSyntaxEditingStyle(SYNTAX_STYLE_NONE);
        }
    }

    @Override
    protected void configurePopupMenu(JPopupMenu popupMenu) {
        super.configurePopupMenu(popupMenu);
        boolean canType = isEditable() && isEnabled();
        clearMarkedOccurrencesMenuItem.setEnabled(canType && ((RSyntaxTextAreaHighlighter) getHighlighter()).getMarkAllHighlightCount() > 0);
    }

    @Override
    public void setSyntaxEditingStyle(String styleKey) {
        super.setSyntaxEditingStyle(styleKey);
        cachedStyleKey = styleKey;
    }

    private void initPopupMenuLayout() {
        // Find the Select All menu item so we know where to insert after
        Component[] menuComponents = getPopupMenu().getComponents();
        int insertIndex = -1;
        for (int i = 0; i < menuComponents.length; i++) {
            if (menuComponents[i] instanceof JMenuItem && ((JMenuItem) menuComponents[i]).getText().equals("Select All")) {
                insertIndex = i;
            }
        }

        getPopupMenu().insert(findReplaceMenuItem, ++insertIndex);
        getPopupMenu().insert(findNextMenuItem, ++insertIndex);
        getPopupMenu().insert(clearMarkedOccurrencesMenuItem, ++insertIndex);
        getPopupMenu().addSeparator();
        displayMenu.add(showTabLinesMenuItem);
        displayMenu.add(showWhitespaceMenuItem);
        displayMenu.add(showLineEndingsMenuItem);
        displayMenu.add(wrapLinesMenuItem);
        getPopupMenu().add(displayMenu);
        getPopupMenu().addSeparator();
        getPopupMenu().add(viewUserAPIMenuItem);
    }
}