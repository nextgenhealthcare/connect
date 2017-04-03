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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.IconButton;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.util.ColorUtil;

public class MirthTagField extends JPanel {
    private static String TAG_TYPE = "tag";
    private static char DELIM = ':';

    private Frame parent;
    private Logger logger = Logger.getLogger(this.getClass());

    private JFXPanel jfxPanel;
    private MirthTagWebBrowser mirthWebBrowser;
    private IconButton clearButton;
    private AutoCompletionPopupWindow acPopupWindow;

    private boolean restorePreferences;
    private List<Map<String, String>> cachedUserPreferenceTags = new ArrayList<Map<String, String>>();

    public MirthTagField(String preferencePrefix, final boolean channelContext, final Set<FilterCompletion> tags) {
        parent = PlatformUI.MIRTH_FRAME;
        restorePreferences = !channelContext;

        if (StringUtils.isNotBlank(preferencePrefix)) {
            try {
                Properties userPreferences = parent.mirthClient.getUserPreferences(parent.getCurrentUser(parent).getId(), Collections.singleton("initialTags" + preferencePrefix));
                cachedUserPreferenceTags = getUserPreferenceTags(userPreferences.getProperty("initialTags" + preferencePrefix));
            } catch (ClientException e) {
                logger.error("Error restoring tag preferences.", e);
            }
        }

        setBackground(channelContext ? UIConstants.BACKGROUND_COLOR : null);

        initComponents(channelContext);
        initLayout(channelContext);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    initFX(channelContext, tags);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initFX(boolean channelContext, Set<FilterCompletion> tags) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root);

        mirthWebBrowser = new MirthTagWebBrowser(acPopupWindow, cachedUserPreferenceTags, createAttributeMap(tags), channelContext);
        root.getChildren().add(mirthWebBrowser);

        jfxPanel.setScene(scene);

        InputMap inputMap = jfxPanel.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Arrow.up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Arrow.down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Arrow.left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Arrow.right");

        AbstractAction doNothing = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ActionMap actionMap = jfxPanel.getActionMap();
        actionMap.put("Arrow.up", doNothing);
        actionMap.put("Arrow.down", doNothing);
        actionMap.put("Arrow.left", doNothing);
        actionMap.put("Arrow.right", doNothing);

        mirthWebBrowser.setUserTags(cachedUserPreferenceTags, true);
    }

    private void initComponents(boolean channelContext) {
        if (channelContext) {
            setToolTipText("Add or remove tags here. General tag management may be done in the Settings -> Tags tab.");
        } else {
            setToolTipText("Enter tags or free text here. Free text will match on channel names, case insensitive.");
        }

        jfxPanel = new JFXPanel();
        jfxPanel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(110, 110, 110), 1, false));

        clearButton = new IconButton();
        clearButton.setIcon(UIConstants.ICON_X);
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mirthWebBrowser.clear();
                clearButton.setEnabled(false);
                acPopupWindow.deleteTagActionPerformed("");
            }
        });

        acPopupWindow = new AutoCompletionPopupWindow();
        acPopupWindow.addUpdateSearchListener(new SearchFilterListener() {
            @Override
            public void doSearch(final String filterString) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateUserTags(filterString);
                        clearButton.setEnabled(StringUtils.isNotBlank(filterString));
                    }
                });
            }

            @Override
            public void doDelete(String filterString) {
                updateUserTags(filterString);
                clearButton.setEnabled(StringUtils.isNotBlank(filterString));
            }
        });
    }

    private void initLayout(boolean channelContext) {
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, fill"));

        add(acPopupWindow, "split 2");
        add(jfxPanel, "h 24!, gaptop 1, growx, push");

        if (!channelContext) {
            add(clearButton, "h 22!, w 22!, aligny top, gaptop 2");
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        jfxPanel.setEnabled(enabled);
        mirthWebBrowser.setEnabled(enabled);
    }

    public void addUpdateSearchListener(SearchFilterListener searchListener) {
        acPopupWindow.addUpdateSearchListener(searchListener);
    }

    private void updateUserTags(String filterString) {
        try {
            cachedUserPreferenceTags = getUserPreferenceTags(filterString);
        } catch (Exception e) {
            logger.error("Error saving tag preferences.");
        }
    }

    public void createTagOnFocusLost() {
        acPopupWindow.createTagOnFocusLost();
    }

    public void setFocus(boolean focus) {
        jfxPanel.requestFocus();
        mirthWebBrowser.setFocus(focus);
    }

    public void closePopupWindow() {
        acPopupWindow.closePopupWindow();
    }

    public void setChannelTags(List<ChannelTag> tags) {
        List<Map<String, String>> tagAttributes = new ArrayList<Map<String, String>>();

        for (ChannelTag tag : tags) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("label", tag.getName());
            attributes.put("value", TAG_TYPE + DELIM + tag.getName());
            tagAttributes.add(attributes);
        }

        mirthWebBrowser.setUserTags(tagAttributes, true);
    }

    public void clear() {
        acPopupWindow.clear();
        mirthWebBrowser.clear();
    }

    public String getTags() {
        return mirthWebBrowser != null ? mirthWebBrowser.getTags() : "";
    }

    public Map<String, Color> getTagColors() {
        return mirthWebBrowser != null ? mirthWebBrowser.getTagColors() : new HashMap<String, Color>();
    }

    public boolean isFilterEnabled() {
        return mirthWebBrowser != null && StringUtils.isNotEmpty(mirthWebBrowser.getTags());
    }

    public void update(Set<FilterCompletion> tags, boolean channelContext, boolean updateUserTags, boolean updateController) {
        Map<String, Map<String, String>> attributeMap = createAttributeMap(tags);

        if (mirthWebBrowser != null && MapUtils.isNotEmpty(attributeMap)) {
            mirthWebBrowser.updateTags(attributeMap, channelContext);

            if (updateUserTags) {
                clearButton.setEnabled(CollectionUtils.isNotEmpty(cachedUserPreferenceTags));
                mirthWebBrowser.setUserTags(cachedUserPreferenceTags, updateController);
            }
        }

        if (acPopupWindow != null) {
            acPopupWindow.setTags(tags);
        }
    }

    private Map<String, Map<String, String>> createAttributeMap(Set<FilterCompletion> tags) {
        Map<String, Map<String, String>> tagObjectMap = new HashMap<String, Map<String, String>>();

        for (FilterCompletion tag : tags) {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("background", ColorUtil.convertToHex(tag.getBackgroundColor()));
            attributes.put("color", ColorUtil.convertToHex(tag.getForegroundColor()));
            attributes.put("type", tag.getType());

            tagObjectMap.put(tag.getName(), attributes);
        }

        return tagObjectMap;
    }

    public void setUserPreferenceTags() {
        if (mirthWebBrowser != null) {
            mirthWebBrowser.setUserTags(cachedUserPreferenceTags, true);
        }
    }

    private List<Map<String, String>> getUserPreferenceTags(String userTags) {
        List<Map<String, String>> userPreferenceTags = new ArrayList<Map<String, String>>();
        try {
            if (restorePreferences && StringUtils.isNotBlank(userTags)) {
                String[] tags = userTags.split(",");
                for (String tag : tags) {
                    String[] tagPair = tag.split(":");

                    if (ArrayUtils.isNotEmpty(tagPair) && tagPair.length == 2) {
                        Map<String, String> attributes = new HashMap<String, String>();
                        String tagName = String.valueOf(tagPair[1]).trim();
                        String tagType = String.valueOf(tagPair[0]).trim();

                        attributes.put("label", tagName);
                        attributes.put("value", String.valueOf(tagType) + DELIM + tagName);

                        userPreferenceTags.add(attributes);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error saving tag preferences.");
        }

        return userPreferenceTags;
    }
}
