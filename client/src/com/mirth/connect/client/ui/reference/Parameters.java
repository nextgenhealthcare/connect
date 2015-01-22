/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.reference.Parameters.Parameter;

public class Parameters extends ArrayList<Parameter> {

    public Parameters() {}

    public Parameters(String name, String type, String description) {
        add(new Parameter(name, type, description));
    }

    public Parameters add(String name, String type, String description) {
        add(new Parameter(name, type, description));
        return this;
    }

    public class Parameter {

        private String name;
        private String type;
        private String description;

        public Parameter(String name, String type, String description) {
            this.name = name;
            this.type = StringUtils.defaultString(type, "Object");
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}