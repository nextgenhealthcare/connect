/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.destinationsetfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.model.Step;
import com.mirth.connect.util.ScriptBuilderException;

public class DestinationSetFilterStep extends Step {

    public static final String PLUGIN_POINT = "Destination Set Filter";

    public enum Behavior {
        REMOVE("the following"), REMOVE_ALL_EXCEPT("all except the following"), REMOVE_ALL("all");

        private String value;

        private Behavior(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Condition {
        EXISTS(false, "exists"), NOT_EXIST(false, "does not exist"), EQUALS(true,
                "equals"), NOT_EQUAL(true, "does not equal"), CONTAINS(true,
                        "contains"), NOT_CONTAIN(true, "does not contain");

        private boolean valuesEnabled;
        private String presentTense;

        private Condition(boolean valuesEnabled, String presentTense) {
            this.valuesEnabled = valuesEnabled;
            this.presentTense = presentTense;
        }

        public boolean isValuesEnabled() {
            return valuesEnabled;
        }

        public String getPresentTense() {
            return presentTense;
        }

        @Override
        public String toString() {
            return WordUtils.capitalizeFully(super.toString().replace('_', ' '));
        }
    }

    private Behavior behavior;
    private List<Integer> metaDataIds;
    private String field;
    private Condition condition;
    private List<String> values;

    public DestinationSetFilterStep() {
        behavior = Behavior.REMOVE;
        metaDataIds = new ArrayList<Integer>();
        field = "";
        condition = Condition.EXISTS;
        values = new ArrayList<String>();
    }

    public DestinationSetFilterStep(DestinationSetFilterStep props) {
        super(props);
        behavior = props.getBehavior();
        metaDataIds = new ArrayList<Integer>(props.getMetaDataIds());
        field = props.getField();
        condition = props.getCondition();
        values = new ArrayList<String>(props.getValues());
    }

    @Override
    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();

        String equalsOperator;
        String conditionOperator;

        script.append("if (");

        if (condition == Condition.EXISTS) {
            script.append("getArrayOrXmlLength(").append(field).append(") > 0) ");
        } else if (condition == Condition.NOT_EXIST) {
            script.append("getArrayOrXmlLength(").append(field).append(") == 0) ");
        } else if (condition == Condition.CONTAINS || condition == Condition.NOT_CONTAIN) {
            if (condition == Condition.CONTAINS) {
                equalsOperator = "!=";
                conditionOperator = "||";
            } else {
                equalsOperator = "==";
                conditionOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append("(").append(field).append(".indexOf(").append(values.get(i)).append(") ").append(equalsOperator).append(" -1)");
                    if (i + 1 == values.size()) {
                        script.append(") ");
                    } else {
                        script.append(' ').append(conditionOperator).append(' ');
                    }
                }
            } else {
                script.append(field + ".indexOf(\"\") " + equalsOperator + " -1) ");
            }
        } else {
            if (condition == Condition.EQUALS) {
                equalsOperator = "==";
                conditionOperator = "||";
            } else {
                equalsOperator = "!=";
                conditionOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append(field).append(" ").append(equalsOperator).append(" ").append(values.get(i));
                    if (i + 1 == values.size()) {
                        script.append(") ");
                    } else {
                        script.append(' ').append(conditionOperator).append(' ');
                    }
                }
            } else {
                script.append(field).append(' ').append(equalsOperator).append(" \"\") ");
            }
        }

        script.append("{\n\tdestinationSet.");
        if (behavior == Behavior.REMOVE) {
            script.append("remove");
        } else if (behavior == Behavior.REMOVE_ALL_EXCEPT) {
            script.append("removeAllExcept");
        } else {
            script.append("removeAll");
        }

        script.append('(');
        if (behavior != Behavior.REMOVE_ALL) {
            script.append('[').append(StringUtils.join(metaDataIds, ", ")).append(']');
        }
        script.append(");\n}\n");

        return script.toString();
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }

    public List<Integer> getMetaDataIds() {
        return metaDataIds;
    }

    public void setMetaDataIds(List<Integer> metaDataIds) {
        this.metaDataIds = metaDataIds;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Step clone() {
        return new DestinationSetFilterStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("behavior", behavior);
        purgedProperties.put("metaDataIdsCount", metaDataIds.size());
        purgedProperties.put("condition", condition);
        purgedProperties.put("valuesCount", values.size());
        return purgedProperties;
    }
}
