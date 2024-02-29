package com.mirth.connect.client.ui.editors;

import java.awt.dnd.DropTargetListener;

import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.MutablePair;

import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.Rule.Operator;

public abstract class BaseEditorPaneBase<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends JPanel implements DropTargetListener {
    
    public static final String MAPPER = "Mapper";
    public static final String MESSAGE_BUILDER = "Message Builder";
    
    protected abstract int getNumColumn();
    
    protected abstract int getNameColumn();
    
    protected abstract int getTypeColumn();
    
    protected abstract int getEnabledColumn();
    
    public abstract void addNewElement();
    
    public abstract void addNewElement(String name, String variable, String mapping, String type);
    
    public abstract void addNewElement(String name, String variable, String mapping, String type, boolean showIteratorWizard);

    protected static class OperatorNamePair extends MutablePair<Operator, String> {

        public OperatorNamePair(String name) {
            this(null, name);
        }

        public OperatorNamePair(Operator operator, String name) {
            super(operator, name);
        }

        public Operator getOperator() {
            return getLeft();
        }

        public String getName() {
            return getRight();
        }
    }
}
