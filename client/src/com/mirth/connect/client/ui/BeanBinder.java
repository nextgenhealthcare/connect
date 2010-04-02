/**
 * L2FProd.com Common Components 6.9.1 License.
 *
 * Copyright 2005-2006 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webreach.mirth.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.UIManager;

import com.l2fprod.common.model.DefaultBeanInfoResolver;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Binds a bean object to a PropertySheet.
 * 
 * <b>Note: this class is not part of the library</b>
 */
public class BeanBinder {

    private final Object bean;
    private final PropertySheetPanel sheet;
    private PropertyChangeListener listener = null;
    private ActionListener updateListener;
    private boolean writeEnabled;

    public BeanBinder(Object bean, PropertySheetPanel sheet, ActionListener updateListener) {
        this(bean, sheet, new DefaultBeanInfoResolver().getBeanInfo(bean), updateListener);
    }

    public BeanBinder(Object bean, PropertySheetPanel sheet, BeanInfo beanInfo, ActionListener updateListener) {
        this.bean = bean;
        this.sheet = sheet;
        this.updateListener = updateListener;
        sheet.setProperties(beanInfo.getPropertyDescriptors());
        sheet.readFromObject(bean);

        // everytime a property change, update the button with it
        listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (writeEnabled) {
                    Property prop = (Property) evt.getSource();

                    try {
                        prop.writeToObject(BeanBinder.this.bean);
                        BeanBinder.this.updateListener.actionPerformed(new ActionEvent(evt, 0, ""));
                    } catch (RuntimeException e) {
                        // handle PropertyVetoException and restore previous
                        // value
                        if (e.getCause() instanceof PropertyVetoException) {
                            UIManager.getLookAndFeel().provideErrorFeedback(BeanBinder.this.sheet);
                            prop.setValue(evt.getOldValue());
                        }
                    }
                }
            }
        };

        sheet.addPropertySheetChangeListener(listener);
    }

    public void unbind() {
        sheet.removePropertyChangeListener(listener);
        sheet.setProperties(new Property[0]);

    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }
}
