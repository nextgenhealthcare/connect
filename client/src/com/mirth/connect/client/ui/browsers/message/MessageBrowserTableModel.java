/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.util.Map;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;

public class MessageBrowserTableModel extends SortableTreeTableModel {
    private AbstractSortableTreeTableNode root;
    private int staticColumnCount;

    public MessageBrowserTableModel(int staticColumnCount) {
        this.staticColumnCount = staticColumnCount;
        root = new AbstractSortableTreeTableNode() {
            @Override
            public Object getValueAt(int column) {
                return null;
            }

            @Override
            public int getColumnCount() {
                return 0;
            }
        };

        setRoot(root);
    }

    @Override
    public int getHierarchicalColumn() {
        return 0;
    }

    public void addMessage(Message message) {
        Map<Integer, ConnectorMessage> connectorMessages = message.getConnectorMessages();

        MessageBrowserTableNode sourceNode;
        if (connectorMessages.containsKey(0)) {
            sourceNode = new MessageBrowserTableNode(staticColumnCount, message, 0, this);
        } else {
            sourceNode = new MessageBrowserTableNode(staticColumnCount, message);
        }

        insertNodeInto(sourceNode, root);

        for (Integer metaDataId : message.getConnectorMessages().keySet()) {
            if (metaDataId > 0) {
                insertNodeInto(new MessageBrowserTableNode(staticColumnCount, message, metaDataId, this), sourceNode);
            }
        }
    }

    public void clear() {
        int childCount = root.getChildCount();

        for (int i = 0; i < childCount; i++) {
            removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
        }
    }
}
