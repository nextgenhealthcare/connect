/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public abstract class FilterTransformer<C extends FilterTransformerElement> implements Serializable, Migratable, Purgable {

    private List<C> elements;

    public FilterTransformer() {
        elements = new ArrayList<C>();
    }

    @SuppressWarnings("unchecked")
    public FilterTransformer(FilterTransformer<C> props) {
        elements = new ArrayList<C>();
        if (CollectionUtils.isNotEmpty(props.getElements())) {
            for (C element : props.getElements()) {
                elements.add((C) element.clone());
            }
        }
    }

    public List<C> getElements() {
        return elements;
    }

    public void setElements(List<C> elements) {
        this.elements = elements;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("elements", PurgeUtil.purgeList(elements));
        return purgedProperties;
    }

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, CalendarToStringStyle.instance());
    }

    @Override
    public abstract FilterTransformer<C> clone();
}