/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.transformer;

import com.mirth.connect.client.ui.editors.IteratorPanel;
import com.mirth.connect.model.IteratorElement;
import com.mirth.connect.model.IteratorStep;
import com.mirth.connect.model.Step;

public class IteratorStepPanel extends IteratorPanel<Step> {

    @Override
    public Step getDefaults() {
        IteratorStep step = new IteratorStep();
        step.setName(getName("..."));
        return step;
    }

    @Override
    protected IteratorElement<Step> newIteratorElement() {
        return new IteratorStep();
    }

    @Override
    protected String getName(String target) {
        return "For each " + target;
    }

    @Override
    protected void initComponents() {}

    @Override
    protected void addMiddleComponents() {}
}