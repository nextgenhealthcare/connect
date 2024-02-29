/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;

public class ChannelTableTransferable implements Transferable {

    public static final DataFlavor CHANNEL_DATA_FLAVOR = new DataFlavor(List.class, "Mirth Connect Channel");

    private static final DataFlavor[] flavors = { DataFlavor.stringFlavor,
            DataFlavor.plainTextFlavor, CHANNEL_DATA_FLAVOR };

    private List<?> list;
    private boolean includesGroups;

    public ChannelTableTransferable(Channel channel) {
        this(Collections.singletonList(channel));
    }

    public ChannelTableTransferable(ChannelGroup group) {
        this(Collections.singletonList(group));
    }

    public ChannelTableTransferable(List<?> list) {
        this.list = list;

        for (Object obj : list) {
            if (obj instanceof ChannelGroup) {
                includesGroups = true;
                break;
            }
        }
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (includesGroups) {
            return flavor.equals(DataFlavor.stringFlavor) || flavor.equals(DataFlavor.plainTextFlavor);
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        if (includesGroups) {
            return new DataFlavor[] { DataFlavor.stringFlavor, DataFlavor.plainTextFlavor };
        } else {
            return (DataFlavor[]) flavors.clone();
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return getStringData();
        } else if (flavor.equals(DataFlavor.plainTextFlavor)) {
            return new StringReader(getStringData());
        } else if (flavor.equals(CHANNEL_DATA_FLAVOR) && !includesGroups) {
            return list;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private String getStringData() {
        StringBuilder builder = new StringBuilder();

        for (Iterator<?> it = list.iterator(); it.hasNext();) {
            Object obj = (Object) it.next();

            if (obj instanceof Channel) {
                Channel channel = (Channel) obj;
                builder.append(channel.getName()).append(" (").append(channel.getId()).append(')');
            } else if (obj instanceof ChannelGroup) {
                ChannelGroup group = (ChannelGroup) obj;
                builder.append(group.getName()).append(" (").append(group.getId()).append(')');
            }

            if (it.hasNext()) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}