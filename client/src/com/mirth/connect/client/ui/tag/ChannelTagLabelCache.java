/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.tag;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.tag.TagLabel;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.util.ColorUtil;

public class ChannelTagLabelCache {

    private static final BufferedImage tagImage = ColorUtil.toBufferedImage(UIConstants.ICON_TAG_GRAY.getImage());

    private static volatile ChannelTagLabelCache instance = null;

    private Map<ChannelTag, TagLabel> textModeMap = new HashMap<ChannelTag, TagLabel>();
    private Map<ChannelTag, TagLabel> iconModeMap = new HashMap<ChannelTag, TagLabel>();

    private ChannelTagLabelCache() {}

    public static ChannelTagLabelCache getInstance() {
        ChannelTagLabelCache cache = instance;

        if (cache == null) {
            synchronized (ChannelTagLabelCache.class) {
                cache = instance;
                if (cache == null) {
                    instance = cache = new ChannelTagLabelCache();
                }
            }
        }

        return cache;
    }

    public TagLabel getLabel(ChannelTag tag, boolean textMode) {
        Map<ChannelTag, TagLabel> map = textMode ? textModeMap : iconModeMap;
        TagLabel label = map.get(tag);
        if (label == null) {
            label = createLabel(tag, textMode);
            map.put(tag, label);
        }
        return label;
    }

    public void removeExpiredTags(Set<ChannelTag> allTags) {
        removeExpiredTags(allTags, textModeMap);
        removeExpiredTags(allTags, iconModeMap);
    }

    private void removeExpiredTags(Set<ChannelTag> allTags, Map<ChannelTag, TagLabel> map) {
        for (Iterator<ChannelTag> it = map.keySet().iterator(); it.hasNext();) {
            if (!allTags.contains(it.next())) {
                it.remove();
            }
        }
    }

    private TagLabel createLabel(ChannelTag tag, boolean textMode) {
        TagLabel tagLabel = new TagLabel();
        tagLabel.setToolTipText(tag.getName());

        if (textMode) {
            tagLabel.decorate(true);
            tagLabel.setBackground(tag.getBackgroundColor());
            tagLabel.setForeground(ColorUtil.getForegroundColor(tag.getBackgroundColor()));
            tagLabel.setText(" " + tag.getName() + " ");
        } else {
            tagLabel.setIcon(new ImageIcon(ColorUtil.tint(tagImage, tag.getBackgroundColor(), ColorUtil.getForegroundColor(tag.getBackgroundColor()))));
        }

        return tagLabel;
    }
}
