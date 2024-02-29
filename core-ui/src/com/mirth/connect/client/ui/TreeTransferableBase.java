package com.mirth.connect.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public abstract class TreeTransferableBase implements Transferable {

    public static final DataFlavor MAPPER_DATA_FLAVOR = new DataFlavor(MapperDropData.class, "MapperDropData");
    public static final DataFlavor MESSAGE_BUILDER_DATA_FLAVOR = new DataFlavor(MessageBuilderDropData.class, "MessageBuilderDropData");
    public static final DataFlavor RULE_DATA_FLAVOR = new DataFlavor(RuleDropData.class, "RuleDropData");
}
