/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.util.ScriptBuilderException;

public class IteratorRuleProperties extends IteratorProperties<Rule> {

    private boolean intersectIterations;
    private boolean breakEarly;

    public IteratorRuleProperties() {
        intersectIterations = false;
        breakEarly = true;
    }

    public IteratorRuleProperties(IteratorRuleProperties props) {
        super(props);
        intersectIterations = props.isIntersectIterations();
        breakEarly = props.isBreakEarly();
    }

    public boolean isIntersectIterations() {
        return intersectIterations;
    }

    public void setIntersectIterations(boolean intersectIterations) {
        this.intersectIterations = intersectIterations;
    }

    public boolean isBreakEarly() {
        return breakEarly;
    }

    public void setBreakEarly(boolean breakEarly) {
        this.breakEarly = breakEarly;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<Rule>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        int depth = ancestors.size();
        ancestors.push(this);

        script.append("var _iterator_flag_").append(depth).append(" = ").append(intersectIterations).append(";\n");
        script.append("for (var ").append(getIndexVariable()).append(" = 0; ").append(getIndexVariable()).append(" < getArrayOrXmlLength(").append(getTarget()).append("); ").append(getIndexVariable()).append("++) {\n");

        if (CollectionUtils.isNotEmpty(getChildren())) {
            script.append("if (");
            if (intersectIterations) {
                script.append("!(");
            }

            boolean first = true;
            for (Rule child : getChildren()) {
                if (first) {
                    first = false;
                } else {
                    script.append(Objects.equals(child.getOperator(), Operator.AND) ? "&&" : "||");
                }

                script.append("\n(function() {\n");
                if (child instanceof FilterTransformerIterable) {
                    script.append(StringUtils.defaultString(((FilterTransformerIterable<Rule>) child).getIterationScript(loadFiles, ancestors)));
                } else {
                    script.append(StringUtils.defaultString(child.getScript(loadFiles)));
                }
                script.append("\n}() == true)\n");
            }

            if (intersectIterations) {
                script.append(')');
            }
            script.append(") { _iterator_flag_").append(depth).append(" = ").append(!intersectIterations).append("; ");
            if (breakEarly) {
                script.append("break; ");
            }
            script.append("}\n");
        }

        script.append("\n}\nreturn _iterator_flag_").append(depth).append(";\n");

        ancestors.pop();
        return script.toString();
    }

    @Override
    public IteratorRuleProperties clone() {
        return new IteratorRuleProperties(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("intersectIterations", intersectIterations);
        purgedProperties.put("breakEarly", breakEarly);
        return purgedProperties;
    }
}