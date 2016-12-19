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
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.mirth.connect.util.ColorUtil;

public class MirthTagWebController {
    private static int BACKSPACE = 8;

    private Logger logger = Logger.getLogger(getClass());
    private String tags;
    private AutoCompletionPopupWindow popupWindow;
    private Map<String, Color> tagColorMap;

    public MirthTagWebController() {}

    public MirthTagWebController(AutoCompletionPopupWindow popupWindow) {
        tags = "";
        this.popupWindow = popupWindow;
        tagColorMap = new HashMap<String, Color>();
    }

    public void clearTags() {
        tags = null;
        tagColorMap = new HashMap<String, Color>();
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public Map<String, Color> getTagColors() {
        return tagColorMap;
    }

    public void updatePopupWindow(final String completionText) {
        SwingUtilities.invokeLater(() -> {
            popupWindow.showPopup(completionText.trim());
        });
    }

    public void closePopupWindow() {
        popupWindow.hidePopup();
    }

    public void updateTagSearch(final String tagString) {
        tags = tagString;
        popupWindow.updateSearchPerformed(tagString);
    }

    public void deleteTagActionPerformed(final String tagString) {
        tags = tagString;
        popupWindow.deleteTagActionPerformed(tagString);
    }

    public void translateKey(String code, String completionText) {
        final Integer keyCode = Integer.parseInt(code);

        if (keyCode == BACKSPACE) {
            popupWindow.closePopup();
        } else {
            popupWindow.translateKey(keyCode, completionText);
        }
    }

    public String getColor(String tagName) {
        Color newColor = ColorUtil.getNewColor();
        tagColorMap.put(tagName, newColor);
        return "[\"" + ColorUtil.convertToHex(newColor) + "\",\"" + ColorUtil.convertToHex(ColorUtil.getForegroundColor(newColor)) + "\"]";
    }

    public void log(Object message) {
        logger.error(message);
    }
}