/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.client.ui.PlatformUI;

import netscape.javascript.JSObject;

public class AutoCompletionPopupWindow extends JTextField {
    private static int UP_ARROW_KEY = 38;
    private static int DOWN_ARROW_KEY = 40;
    private static int ESCAPE_KEY = 27;
    private static int ENTER_KEY = 13;

    private AutoCompletionDelegate completionDelegate;
    private AutoCompletionProvider provider;
    private WebEngine engineCtrl;

    private String currentlyEnteredText = "";
    private List<SearchFilterListener> updateSearchListeners = new ArrayList<SearchFilterListener>();

    public AutoCompletionPopupWindow() {
        setVisible(false);
        setBackground(Color.white);
        setFocusable(true);

        initComponents();
    }

    @Override
    public Rectangle modelToView(int pos) throws BadLocationException {
        return new Rectangle(getLocation(), new Dimension(50, 24));
    }

    private void initComponents() {
        provider = new AutoCompletionProvider();
        provider.setAutoActivationRules(true, null);
        provider.setListCellRenderer(new TagCompletionRenderer());

        completionDelegate = new AutoCompletionDelegate(provider, this);
        completionDelegate.setAutoCompleteSingleChoices(false);
        completionDelegate.install(this);
    }

    public void createTagOnFocusLost() {
        translateKey(ENTER_KEY, "");
    }

    public void setTag(final String tagName, final String tagType) {
        doCall("insertTag", tagName, tagType);
        currentlyEnteredText = "";
        hidePopup();
        PlatformUI.MIRTH_FRAME.setSaveEnabled(PlatformUI.MIRTH_FRAME.currentContentPage == PlatformUI.MIRTH_FRAME.channelEditPanel);
    }

    public void setWebEngine(WebEngine engineCtrl) {
        this.engineCtrl = engineCtrl;
    }

    public void clear() {
        currentlyEnteredText = "";
    }

    public void closePopupWindow() {
        hidePopup();
        setFocus(false);
    }

    public void setTags(Set<FilterCompletion> tags) {
        provider.clear();
        for (FilterCompletion tag : tags) {
            provider.addCompletion(new TagCompletion(provider, tag.getName(), tag.getType(), tag.getBackgroundColor(), tag.getIcon()));
        }
    }

    public void showPopup(String completionText) {
        showPopup(completionText, false);
    }

    public void showPopup(String completionText, boolean force) {
        if (force || StringUtils.isNotBlank(completionText)) {
            currentlyEnteredText = completionText;
            setText(completionText);
            completionDelegate.doCompletion();
        } else {
            hidePopup();
        }
    }

    public void hidePopup() {
        completionDelegate.hideChildWindows();

        if (completionDelegate.isPopupVisible()) {
            completionDelegate.hidePopupWindow();
        }
    }

    public void closePopup() {
        if (StringUtils.isEmpty(currentlyEnteredText)) {
            hidePopup();
        }
    }

    public void translateKey(Integer keyCode, String completionText) {
        if (!isFocusOwner()) {
            requestFocusInWindow();
        }

        if (keyCode == UP_ARROW_KEY) {
            completionDelegate.moveUp();
        } else if (keyCode == DOWN_ARROW_KEY) {
            if (completionDelegate.isPopupVisible()) {
                completionDelegate.moveDown();
            } else {
                showPopup(completionText, true);
            }
        } else if (keyCode == ENTER_KEY) { // TODO test channel and tag with the same name
            String tagText = currentlyEnteredText;
            if (completionDelegate.isPopupVisible()) {
                tagText = completionDelegate.getSelectedValue();
            } else if (StringUtils.isNotEmpty(tagText)) {
                setTag(tagText, "name"); // Assume the user is searching a name
            }
        } else if (keyCode == ESCAPE_KEY && completionDelegate.isPopupVisible()) {
            hidePopup();
        }
    }

    public void addUpdateSearchListener(SearchFilterListener updateSearchListener) {
        if (!updateSearchListeners.contains(updateSearchListener)) {
            updateSearchListeners.add(updateSearchListener);
        }
    }

    public void updateSearchPerformed(String filterString) {
        for (SearchFilterListener listener : updateSearchListeners) {
            if (listener != null) {
                listener.doSearch(filterString);
            }
        }

        hidePopup();
    }

    public void deleteTagActionPerformed(String filterString) {
        for (SearchFilterListener listener : updateSearchListeners) {
            if (listener != null) {
                listener.doDelete(filterString);
            }
        }
    }

    public void setFocus(boolean focus) {
        doCall("setFocus", convertToJSON(focus));
    }

    private String convertToJSON(Object object) {
        String jsonData = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonData = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e.getCause(), "Error converting to JSON");
        }

        return jsonData;
    }

    private void doCall(final String method, final Object... args) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    JSObject tokenField = (JSObject) engineCtrl.executeScript("window");
                    tokenField.call(method, args);
                }
            });
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e.getCause(), "Error in tagfield");
        }
    }
}
