/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaDefaultInputMap;

import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;
import com.mirth.connect.client.ui.reference.ReferenceListFactory;

public class MirthInputMap extends RSyntaxTextAreaDefaultInputMap {

    private static MirthInputMap instance = null;

    private Map<ActionInfo, KeyStroke> keyStrokeMap = new HashMap<ActionInfo, KeyStroke>();

    private MirthInputMap() {}

    public static MirthInputMap getInstance() {
        synchronized (ReferenceListFactory.class) {
            if (instance == null) {
                instance = new MirthInputMap();
            }
            return instance;
        }
    }

    void update(Map<String, KeyStroke> map) {
        for (Iterator<Entry<ActionInfo, KeyStroke>> it = keyStrokeMap.entrySet().iterator(); it.hasNext();) {
            remove(it.next().getValue());
            it.remove();
        }

        for (Entry<String, KeyStroke> entry : map.entrySet()) {
            ActionInfo actionInfo = ActionInfo.fromActionMapKey(entry.getKey());
            if (actionInfo != null && entry.getValue() != null) {
                put(entry.getValue(), actionInfo);
            }
        }
    }

    public KeyStroke getKeyStroke(ActionInfo actionInfo) {
        return keyStrokeMap.get(actionInfo);
    }

    private void put(KeyStroke keyStroke, ActionInfo actionInfo) {
        put(keyStroke, actionInfo.getActionMapKey());
        keyStrokeMap.put(actionInfo, keyStroke);
    }
}