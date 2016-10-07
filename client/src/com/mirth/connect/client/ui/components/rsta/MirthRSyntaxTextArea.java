/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.CompletionProviderBase;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.EOLPreservingRSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;
import org.fife.ui.rtextarea.RTextAreaUI;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTextInterface;
import com.mirth.connect.client.ui.components.rsta.ac.MirthLanguageSupport;
import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;
import com.mirth.connect.client.ui.components.rsta.actions.BeginMacroAction;
import com.mirth.connect.client.ui.components.rsta.actions.ClearMarkedOccurrencesAction;
import com.mirth.connect.client.ui.components.rsta.actions.CollapseAllCommentFoldsAction;
import com.mirth.connect.client.ui.components.rsta.actions.CollapseAllFoldsAction;
import com.mirth.connect.client.ui.components.rsta.actions.CollapseFoldAction;
import com.mirth.connect.client.ui.components.rsta.actions.CopyAction;
import com.mirth.connect.client.ui.components.rsta.actions.CutAction;
import com.mirth.connect.client.ui.components.rsta.actions.DeleteAction;
import com.mirth.connect.client.ui.components.rsta.actions.DeleteLineAction;
import com.mirth.connect.client.ui.components.rsta.actions.DeleteRestOfLineAction;
import com.mirth.connect.client.ui.components.rsta.actions.DocumentEndAction;
import com.mirth.connect.client.ui.components.rsta.actions.DocumentStartAction;
import com.mirth.connect.client.ui.components.rsta.actions.EndMacroAction;
import com.mirth.connect.client.ui.components.rsta.actions.ExpandAllFoldsAction;
import com.mirth.connect.client.ui.components.rsta.actions.ExpandFoldAction;
import com.mirth.connect.client.ui.components.rsta.actions.FindNextAction;
import com.mirth.connect.client.ui.components.rsta.actions.FindReplaceAction;
import com.mirth.connect.client.ui.components.rsta.actions.GoToMatchingBracketAction;
import com.mirth.connect.client.ui.components.rsta.actions.HorizontalPageAction;
import com.mirth.connect.client.ui.components.rsta.actions.InsertBreakAction;
import com.mirth.connect.client.ui.components.rsta.actions.JoinLineAction;
import com.mirth.connect.client.ui.components.rsta.actions.LineEndAction;
import com.mirth.connect.client.ui.components.rsta.actions.LineStartAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveDownAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveLeftAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveLeftWordAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveLineAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveRightAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveRightWordAction;
import com.mirth.connect.client.ui.components.rsta.actions.MoveUpAction;
import com.mirth.connect.client.ui.components.rsta.actions.PageDownAction;
import com.mirth.connect.client.ui.components.rsta.actions.PageUpAction;
import com.mirth.connect.client.ui.components.rsta.actions.PasteAction;
import com.mirth.connect.client.ui.components.rsta.actions.PlaybackMacroAction;
import com.mirth.connect.client.ui.components.rsta.actions.RedoAction;
import com.mirth.connect.client.ui.components.rsta.actions.ScrollAction;
import com.mirth.connect.client.ui.components.rsta.actions.SelectAllAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowLineEndingsAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowTabLinesAction;
import com.mirth.connect.client.ui.components.rsta.actions.ShowWhitespaceAction;
import com.mirth.connect.client.ui.components.rsta.actions.ToggleCommentAction;
import com.mirth.connect.client.ui.components.rsta.actions.UndoAction;
import com.mirth.connect.client.ui.components.rsta.actions.ViewUserAPIAction;
import com.mirth.connect.client.ui.components.rsta.actions.WrapLinesAction;
import com.mirth.connect.model.codetemplates.ContextType;

public class MirthRSyntaxTextArea extends RSyntaxTextArea implements MirthTextInterface {

    private static final String PREFERENCES_KEYSTROKES = "rstaPreferencesKeyStrokes";
    private static final String PREFERENCES_FIND_REPLACE = "rstaPreferencesFindReplace";
    private static final String PREFERENCES_TOGGLE_OPTIONS = "rstaPreferencesToggleOptions";
    private static final String PREFERENCES_AUTO_COMPLETE = "rstaPreferencesAutoComplete";

    private static RSTAPreferences rstaPreferences;
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(MirthRSyntaxTextArea.class.getName());

    private ContextType contextType;
    private boolean saveEnabled = true;
    private String cachedStyleKey;
    private Action[] actions;

    private CustomMenuItem undoMenuItem;
    private CustomMenuItem redoMenuItem;
    private CustomMenuItem cutMenuItem;
    private CustomMenuItem copyMenuItem;
    private CustomMenuItem pasteMenuItem;
    private CustomMenuItem deleteMenuItem;
    private CustomMenuItem selectAllMenuItem;
    private CustomMenuItem findReplaceMenuItem;
    private CustomMenuItem findNextMenuItem;
    private CustomMenuItem clearMarkedOccurrencesMenuItem;
    private JMenu foldingMenu;
    private CustomMenuItem collapseFoldMenuItem;
    private CustomMenuItem expandFoldMenuItem;
    private CustomMenuItem collapseAllFoldsMenuItem;
    private CustomMenuItem collapseAllCommentFoldsMenuItem;
    private CustomMenuItem expandAllFoldsMenuItem;
    private JMenu displayMenu;
    private CustomJCheckBoxMenuItem showTabLinesMenuItem;
    private CustomJCheckBoxMenuItem showWhitespaceMenuItem;
    private CustomJCheckBoxMenuItem showLineEndingsMenuItem;
    private CustomJCheckBoxMenuItem wrapLinesMenuItem;
    private JMenu macroMenu;
    private CustomMenuItem beginMacroMenuItem;
    private CustomMenuItem endMacroMenuItem;
    private CustomMenuItem playbackMacroMenuItem;
    private CustomMenuItem viewUserAPIMenuItem;

    static {
        Preferences userPreferences = Preferences.userNodeForPackage(Mirth.class);
        String keyStrokesJSON = userPreferences.get(PREFERENCES_KEYSTROKES, null);
        String findReplaceJSON = userPreferences.get(PREFERENCES_FIND_REPLACE, null);
        String toggleOptionsJSON = userPreferences.get(PREFERENCES_TOGGLE_OPTIONS, null);
        String autoCompleteJSON = userPreferences.get(PREFERENCES_AUTO_COMPLETE, null);

        rstaPreferences = RSTAPreferences.fromJSON(keyStrokesJSON, findReplaceJSON, toggleOptionsJSON, autoCompleteJSON);

        updateKeyStrokePreferences(userPreferences);
        updateFindReplacePreferences(userPreferences);
        updateToggleOptionPreferences(userPreferences);
        updateAutoCompletePreferences(userPreferences);
    }

    public MirthRSyntaxTextArea() {
        this(null);
    }

    public MirthRSyntaxTextArea(ContextType contextType) {
        this(contextType, SYNTAX_STYLE_JAVASCRIPT);
    }

    public MirthRSyntaxTextArea(ContextType contextType, String styleKey) {
        this(contextType, styleKey, true);
    }

    public MirthRSyntaxTextArea(ContextType contextType, String styleKey, boolean autoCompleteEnabled) {
        super(new MirthRSyntaxDocument(styleKey));
        this.contextType = contextType;

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

        undoMenuItem = new CustomMenuItem(this, new UndoAction(), ActionInfo.UNDO);
        redoMenuItem = new CustomMenuItem(this, new RedoAction(), ActionInfo.REDO);
        cutMenuItem = new CustomMenuItem(this, new CutAction(this), ActionInfo.CUT);
        copyMenuItem = new CustomMenuItem(this, new CopyAction(this), ActionInfo.COPY);
        pasteMenuItem = new CustomMenuItem(this, new PasteAction(), ActionInfo.PASTE);
        deleteMenuItem = new CustomMenuItem(this, new DeleteAction(), ActionInfo.DELETE);
        selectAllMenuItem = new CustomMenuItem(this, new SelectAllAction(), ActionInfo.SELECT_ALL);
        findReplaceMenuItem = new CustomMenuItem(this, new FindReplaceAction(this), ActionInfo.FIND_REPLACE);
        findNextMenuItem = new CustomMenuItem(this, new FindNextAction(this), ActionInfo.FIND_NEXT);
        clearMarkedOccurrencesMenuItem = new CustomMenuItem(this, new ClearMarkedOccurrencesAction(this), ActionInfo.CLEAR_MARKED_OCCURRENCES);
        foldingMenu = new JMenu("Folding");
        collapseFoldMenuItem = new CustomMenuItem(this, new CollapseFoldAction(), ActionInfo.FOLD_COLLAPSE);
        expandFoldMenuItem = new CustomMenuItem(this, new ExpandFoldAction(), ActionInfo.FOLD_EXPAND);
        collapseAllFoldsMenuItem = new CustomMenuItem(this, new CollapseAllFoldsAction(), ActionInfo.FOLD_COLLAPSE_ALL);
        collapseAllCommentFoldsMenuItem = new CustomMenuItem(this, new CollapseAllCommentFoldsAction(), ActionInfo.FOLD_COLLAPSE_ALL_COMMENTS);
        expandAllFoldsMenuItem = new CustomMenuItem(this, new ExpandAllFoldsAction(), ActionInfo.FOLD_EXPAND_ALL);
        displayMenu = new JMenu("Display");
        showTabLinesMenuItem = new CustomJCheckBoxMenuItem(this, new ShowTabLinesAction(this), ActionInfo.DISPLAY_SHOW_TAB_LINES);
        showWhitespaceMenuItem = new CustomJCheckBoxMenuItem(this, new ShowWhitespaceAction(this), ActionInfo.DISPLAY_SHOW_WHITESPACE);
        showLineEndingsMenuItem = new CustomJCheckBoxMenuItem(this, new ShowLineEndingsAction(this), ActionInfo.DISPLAY_SHOW_LINE_ENDINGS);
        wrapLinesMenuItem = new CustomJCheckBoxMenuItem(this, new WrapLinesAction(this), ActionInfo.DISPLAY_WRAP_LINES);
        macroMenu = new JMenu("Macro");
        beginMacroMenuItem = new CustomMenuItem(this, new BeginMacroAction(), ActionInfo.MACRO_BEGIN);
        endMacroMenuItem = new CustomMenuItem(this, new EndMacroAction(), ActionInfo.MACRO_END);
        playbackMacroMenuItem = new CustomMenuItem(this, new PlaybackMacroAction(), ActionInfo.MACRO_PLAYBACK);
        viewUserAPIMenuItem = new CustomMenuItem(this, new ViewUserAPIAction(this), ActionInfo.VIEW_USER_API);

        // Add actions that wont be in the popup menu
        getActionMap().put(ActionInfo.DELETE_REST_OF_LINE.getActionMapKey(), new DeleteRestOfLineAction());
        getActionMap().put(ActionInfo.DELETE_LINE.getActionMapKey(), new DeleteLineAction());
        getActionMap().put(ActionInfo.JOIN_LINE.getActionMapKey(), new JoinLineAction());
        getActionMap().put(ActionInfo.GO_TO_MATCHING_BRACKET.getActionMapKey(), new GoToMatchingBracketAction());
        getActionMap().put(ActionInfo.TOGGLE_COMMENT.getActionMapKey(), new ToggleCommentAction());
        getActionMap().put(ActionInfo.DOCUMENT_START.getActionMapKey(), new DocumentStartAction(false));
        getActionMap().put(ActionInfo.DOCUMENT_SELECT_START.getActionMapKey(), new DocumentStartAction(true));
        getActionMap().put(ActionInfo.DOCUMENT_END.getActionMapKey(), new DocumentEndAction(false));
        getActionMap().put(ActionInfo.DOCUMENT_SELECT_END.getActionMapKey(), new DocumentEndAction(true));
        getActionMap().put(ActionInfo.LINE_START.getActionMapKey(), new LineStartAction(false));
        getActionMap().put(ActionInfo.LINE_SELECT_START.getActionMapKey(), new LineStartAction(true));
        getActionMap().put(ActionInfo.LINE_END.getActionMapKey(), new LineEndAction(false));
        getActionMap().put(ActionInfo.LINE_SELECT_END.getActionMapKey(), new LineEndAction(true));
        getActionMap().put(ActionInfo.MOVE_LEFT.getActionMapKey(), new MoveLeftAction(false));
        getActionMap().put(ActionInfo.MOVE_LEFT_SELECT.getActionMapKey(), new MoveLeftAction(true));
        getActionMap().put(ActionInfo.MOVE_LEFT_WORD.getActionMapKey(), new MoveLeftWordAction(false));
        getActionMap().put(ActionInfo.MOVE_LEFT_WORD_SELECT.getActionMapKey(), new MoveLeftWordAction(true));
        getActionMap().put(ActionInfo.MOVE_RIGHT.getActionMapKey(), new MoveRightAction(false));
        getActionMap().put(ActionInfo.MOVE_RIGHT_SELECT.getActionMapKey(), new MoveRightAction(true));
        getActionMap().put(ActionInfo.MOVE_RIGHT_WORD.getActionMapKey(), new MoveRightWordAction(false));
        getActionMap().put(ActionInfo.MOVE_RIGHT_WORD_SELECT.getActionMapKey(), new MoveRightWordAction(true));
        getActionMap().put(ActionInfo.MOVE_UP.getActionMapKey(), new MoveUpAction(false));
        getActionMap().put(ActionInfo.MOVE_UP_SELECT.getActionMapKey(), new MoveUpAction(true));
        getActionMap().put(ActionInfo.MOVE_UP_SCROLL.getActionMapKey(), new ScrollAction(true));
        getActionMap().put(ActionInfo.MOVE_UP_LINE.getActionMapKey(), new MoveLineAction(true));
        getActionMap().put(ActionInfo.MOVE_DOWN.getActionMapKey(), new MoveDownAction(false));
        getActionMap().put(ActionInfo.MOVE_DOWN_SELECT.getActionMapKey(), new MoveDownAction(true));
        getActionMap().put(ActionInfo.MOVE_DOWN_SCROLL.getActionMapKey(), new ScrollAction(false));
        getActionMap().put(ActionInfo.MOVE_DOWN_LINE.getActionMapKey(), new MoveLineAction(false));
        getActionMap().put(ActionInfo.PAGE_UP.getActionMapKey(), new PageUpAction(false));
        getActionMap().put(ActionInfo.PAGE_UP_SELECT.getActionMapKey(), new PageUpAction(true));
        getActionMap().put(ActionInfo.PAGE_LEFT_SELECT.getActionMapKey(), new HorizontalPageAction(this, true));
        getActionMap().put(ActionInfo.PAGE_DOWN.getActionMapKey(), new PageDownAction(false));
        getActionMap().put(ActionInfo.PAGE_DOWN_SELECT.getActionMapKey(), new PageDownAction(true));
        getActionMap().put(ActionInfo.PAGE_RIGHT_SELECT.getActionMapKey(), new HorizontalPageAction(this, false));
        getActionMap().put(ActionInfo.INSERT_LF_BREAK.getActionMapKey(), new InsertBreakAction("\n"));
        getActionMap().put(ActionInfo.INSERT_CR_BREAK.getActionMapKey(), new InsertBreakAction("\r"));

        List<Action> actionList = new ArrayList<Action>();
        for (Object key : getActionMap().allKeys()) {
            actionList.add(getActionMap().get(key));
        }
        actions = actionList.toArray(new Action[actionList.size()]);

        if (autoCompleteEnabled) {
            LanguageSupportFactory.get().register(this);
            // Remove the default auto-completion trigger since we handle that ourselves
            getInputMap().remove(AutoCompletion.getDefaultTriggerKey());
        }
    }

    public static RSTAPreferences getRSTAPreferences() {
        return rstaPreferences;
    }

    public static void updateKeyStrokePreferences() {
        updateKeyStrokePreferences(Preferences.userNodeForPackage(Mirth.class));
    }

    public static void updateKeyStrokePreferences(Preferences userPreferences) {
        MirthInputMap.getInstance().update(rstaPreferences.getKeyStrokeMap());
        userPreferences.put(PREFERENCES_KEYSTROKES, rstaPreferences.getKeyStrokesJSON());
    }

    public static void updateFindReplacePreferences() {
        updateFindReplacePreferences(Preferences.userNodeForPackage(Mirth.class));
    }

    public static void updateFindReplacePreferences(Preferences userPreferences) {
        userPreferences.put(PREFERENCES_FIND_REPLACE, rstaPreferences.getFindReplaceJSON());
    }

    public static void updateToggleOptionPreferences() {
        updateToggleOptionPreferences(Preferences.userNodeForPackage(Mirth.class));
    }

    public static void updateToggleOptionPreferences(Preferences userPreferences) {
        userPreferences.put(PREFERENCES_TOGGLE_OPTIONS, rstaPreferences.getToggleOptionsJSON());
    }

    public static void updateAutoCompletePreferences() {
        updateAutoCompletePreferences(Preferences.userNodeForPackage(Mirth.class));
    }

    public static void updateAutoCompletePreferences(Preferences userPreferences) {
        userPreferences.put(PREFERENCES_AUTO_COMPLETE, rstaPreferences.getAutoCompleteJSON());
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    public boolean isSaveEnabled() {
        return saveEnabled;
    }

    public void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
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
        setText(text, true);
    }

    public void setText(String text, boolean discardEdits) {
        boolean visible = PlatformUI.MIRTH_FRAME.changesHaveBeenMade();
        super.setText(text);
        setCaretPosition(0);
        if (discardEdits) {
            discardAllEdits();
        }

        if (visible) {
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        } else {
            PlatformUI.MIRTH_FRAME.setSaveEnabled(false);
        }

        if (!isEnabled()) {
            setBracketMatchingEnabled(false);
            super.setSyntaxEditingStyle(SYNTAX_STYLE_NONE);
        }

        updateDisplayOptions();
    }

    public void updateDisplayOptions() {
        // Update display options from preferences
        showTabLinesMenuItem.setSelected(BooleanUtils.toBoolean(rstaPreferences.getToggleOptions().get(ActionInfo.DISPLAY_SHOW_TAB_LINES.getActionMapKey())));
        showWhitespaceMenuItem.setSelected(BooleanUtils.toBoolean(rstaPreferences.getToggleOptions().get(ActionInfo.DISPLAY_SHOW_WHITESPACE.getActionMapKey())));
        showLineEndingsMenuItem.setSelected(BooleanUtils.toBoolean(rstaPreferences.getToggleOptions().get(ActionInfo.DISPLAY_SHOW_LINE_ENDINGS.getActionMapKey())));
        wrapLinesMenuItem.setSelected(BooleanUtils.toBoolean(rstaPreferences.getToggleOptions().get(ActionInfo.DISPLAY_WRAP_LINES.getActionMapKey())));

        LanguageSupport languageSupport = (LanguageSupport) LanguageSupportFactory.get().getSupportFor(getSyntaxEditingStyle());
        if (languageSupport != null && languageSupport instanceof MirthLanguageSupport) {
            AutoCompletion autoCompletion = ((MirthLanguageSupport) languageSupport).getAutoCompletionFor(this);
            if (autoCompletion != null) {
                LanguageAwareCompletionProvider provider = (LanguageAwareCompletionProvider) autoCompletion.getCompletionProvider();
                AutoCompleteProperties autoCompleteProperties = rstaPreferences.getAutoCompleteProperties();
                autoCompletion.setAutoActivationDelay(autoCompleteProperties.getActivationDelay());
                setAutoActivationRules(provider, autoCompleteProperties.isActivateAfterLetters(), autoCompleteProperties.getActivateAfterOthers());
            }
        }
    }

    private void setAutoActivationRules(CompletionProvider provider, boolean letters, String others) {
        if (provider != null) {
            if (provider instanceof CompletionProviderBase) {
                ((CompletionProviderBase) provider).setAutoActivationRules(letters, others);
            }
            if (provider instanceof LanguageAwareCompletionProvider) {
                LanguageAwareCompletionProvider langProvider = (LanguageAwareCompletionProvider) provider;
                setAutoActivationRules(langProvider.getDefaultCompletionProvider(), letters, others);
                setAutoActivationRules(langProvider.getStringCompletionProvider(), letters, others);
                setAutoActivationRules(langProvider.getCommentCompletionProvider(), letters, others);
                setAutoActivationRules(langProvider.getDocCommentCompletionProvider(), letters, others);
            }
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
    protected RTextAreaUI createRTextAreaUI() {
        return new MirthRSyntaxTextAreaUI(this);
    }

    @Override
    protected JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();

        menu.add(undoMenuItem);
        menu.add(redoMenuItem);
        menu.addSeparator();

        menu.add(cutMenuItem);
        menu.add(copyMenuItem);
        menu.add(pasteMenuItem);
        menu.add(deleteMenuItem);
        menu.addSeparator();

        menu.add(selectAllMenuItem);
        menu.add(findReplaceMenuItem);
        menu.add(findNextMenuItem);
        menu.add(clearMarkedOccurrencesMenuItem);
        menu.addSeparator();

        foldingMenu.add(collapseFoldMenuItem);
        foldingMenu.add(expandFoldMenuItem);
        foldingMenu.add(collapseAllFoldsMenuItem);
        foldingMenu.add(collapseAllCommentFoldsMenuItem);
        foldingMenu.add(expandAllFoldsMenuItem);
        menu.add(foldingMenu);
        menu.addSeparator();

        displayMenu.add(showTabLinesMenuItem);
        displayMenu.add(showWhitespaceMenuItem);
        displayMenu.add(showLineEndingsMenuItem);
        displayMenu.add(wrapLinesMenuItem);
        menu.add(displayMenu);
        menu.addSeparator();

        macroMenu.add(beginMacroMenuItem);
        macroMenu.add(endMacroMenuItem);
        macroMenu.add(playbackMacroMenuItem);
        menu.add(macroMenu);
        menu.addSeparator();

        menu.add(viewUserAPIMenuItem);

        return menu;
    }

    @Override
    protected void configurePopupMenu(JPopupMenu popupMenu) {
        if (popupMenu != null) {
            boolean canType = isEditable() && isEnabled();

            undoMenuItem.setEnabled(undoMenuItem.getAction().isEnabled() && canType && canUndo());
            redoMenuItem.setEnabled(redoMenuItem.getAction().isEnabled() && canType && canRedo());
            cutMenuItem.setEnabled(cutMenuItem.getAction().isEnabled() && canType);
            copyMenuItem.setEnabled(copyMenuItem.getAction().isEnabled() && canType);
            pasteMenuItem.setEnabled(pasteMenuItem.getAction().isEnabled() && canType);
            deleteMenuItem.setEnabled(deleteMenuItem.getAction().isEnabled() && canType);
            findNextMenuItem.setEnabled(findNextMenuItem.getAction().isEnabled() && CollectionUtils.isNotEmpty(rstaPreferences.getFindReplaceProperties().getFindHistory()));
            clearMarkedOccurrencesMenuItem.setEnabled(clearMarkedOccurrencesMenuItem.getAction().isEnabled() && canType && ((RSyntaxTextAreaHighlighter) getHighlighter()).getMarkAllHighlightCount() > 0);
            foldingMenu.setEnabled(getFoldManager().isCodeFoldingSupportedAndEnabled());
            beginMacroMenuItem.setEnabled(!isRecordingMacro());
            endMacroMenuItem.setEnabled(isRecordingMacro());
            playbackMacroMenuItem.setEnabled(!isRecordingMacro() && getCurrentMacro() != null);

            undoMenuItem.updateAccelerator();
            redoMenuItem.updateAccelerator();
            cutMenuItem.updateAccelerator();
            copyMenuItem.updateAccelerator();
            pasteMenuItem.updateAccelerator();
            deleteMenuItem.updateAccelerator();
            selectAllMenuItem.updateAccelerator();
            findReplaceMenuItem.updateAccelerator();
            findNextMenuItem.updateAccelerator();
            clearMarkedOccurrencesMenuItem.updateAccelerator();
            collapseFoldMenuItem.updateAccelerator();
            expandFoldMenuItem.updateAccelerator();
            collapseAllFoldsMenuItem.updateAccelerator();
            collapseAllCommentFoldsMenuItem.updateAccelerator();
            expandAllFoldsMenuItem.updateAccelerator();
            beginMacroMenuItem.updateAccelerator();
            endMacroMenuItem.updateAccelerator();
            playbackMacroMenuItem.updateAccelerator();
            viewUserAPIMenuItem.updateAccelerator();
        }
    }

    @Override
    public void setSyntaxEditingStyle(String styleKey) {
        super.setSyntaxEditingStyle(styleKey);
        cachedStyleKey = styleKey;
    }

    @Override
    public Action[] getActions() {
        return actions != null ? actions : getUI().getEditorKit(this).getActions();
    }
}