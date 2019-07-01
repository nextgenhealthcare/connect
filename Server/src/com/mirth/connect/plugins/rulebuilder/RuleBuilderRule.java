/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.rulebuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.model.Rule;
import com.mirth.connect.util.ScriptBuilderException;

public class RuleBuilderRule extends Rule {

    public static final String PLUGIN_POINT = "Rule Builder";

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

    private String field;
    private Condition condition;
    private List<String> values;

    public RuleBuilderRule() {
        field = "";
        condition = Condition.EXISTS;
        values = new ArrayList<String>();
    }

    public RuleBuilderRule(RuleBuilderRule props) {
        super(props);
        field = props.getField();
        condition = props.getCondition();
        values = new ArrayList<String>(props.getValues());
    }

    @Override
    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();

        String acceptReturn, finalReturn, equals, equalsOperator;

        acceptReturn = "true";
        finalReturn = "false";

        script.append("if(");

        if (condition == Condition.EXISTS) {
            script.append(field + ".length > 0) ");
        } else if (condition == Condition.NOT_EXIST) {
            script.append(field + ".length == 0) ");
        } else if (condition == Condition.CONTAINS || condition == Condition.NOT_CONTAIN) {
            if (condition == Condition.CONTAINS) {
                equals = "!=";
                equalsOperator = "||";
            } else {
                equals = "==";
                equalsOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append("(" + field + ".indexOf(" + values.get(i) + ") " + equals + " -1)");
                    if (i + 1 == values.size()) {
                        script.append(") ");
                    } else {
                        script.append(" " + equalsOperator + " ");
                    }
                }
            } else {
                script.append(field + ".indexOf(\"\") " + equals + " -1) ");
            }
        } else {
            if (condition == Condition.EQUALS) {
                equals = "==";
                equalsOperator = "||";
            } else {
                equals = "!=";
                equalsOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append(field + " " + equals + " " + values.get(i));
                    if (i + 1 == values.size()) {
                        script.append(") ");
                    } else {
                        script.append(" " + equalsOperator + " ");
                    }
                }
            } else {
                script.append(field + " " + equals + " \"\") ");
            }
        }

        script.append("{\n");
        script.append("\treturn " + acceptReturn + ";");
        script.append("\n}\n");
        script.append("return " + finalReturn + ";");

        return script.toString();
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
    public Rule clone() {
        return new RuleBuilderRule(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("condition", condition);
        if (values != null) {
            purgedProperties.put("valuesCount", values.size());
        }
        return purgedProperties;
    }
}