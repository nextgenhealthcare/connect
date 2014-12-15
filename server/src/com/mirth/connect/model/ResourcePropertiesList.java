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
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("resourcePropertiesList")
public class ResourcePropertiesList implements Serializable {

    private List<ResourceProperties> list;

    public ResourcePropertiesList() {
        this(new ArrayList<ResourceProperties>());
    }

    public ResourcePropertiesList(List<ResourceProperties> list) {
        this.list = list;
    }

    public List<ResourceProperties> getList() {
        return list;
    }

    public void setList(List<ResourceProperties> list) {
        this.list = list;
    }
}