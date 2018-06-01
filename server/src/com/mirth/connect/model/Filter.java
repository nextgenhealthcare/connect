/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Filter represents a list of rules which are executed on each message and either accepts or
 * rejects the message.
 */
@XStreamAlias("filter")
public class Filter extends FilterTransformer<Rule> {

    public Filter() {}

    public Filter(Filter props) {
        super(props);
    }

    @Override
    public void migrate3_0_1(DonkeyElement filter) {
        for (DonkeyElement rule : filter.getChildElement("rules").getChildElements()) {
            rule.getChildElement("data").removeAttribute("class");
        }
    }

    @Override
    public void migrate3_5_0(DonkeyElement element) {
        DonkeyElement rulesElement = element.removeChild("rules");
        List<DonkeyElement> rules = rulesElement.getChildElements();
        rulesElement = element.addChildElement("elements");

        for (DonkeyElement ruleElement : rules) {
            DonkeyElement newRuleElement;
            String type = ruleElement.getChildElement("type").getTextContent();

            DonkeyElement dataElement = ruleElement.getChildElement("data");
            Map<String, DonkeyElement> dataMap = new HashMap<String, DonkeyElement>();
            for (DonkeyElement entry : dataElement.getChildElements()) {
                List<DonkeyElement> values = entry.getChildElements();
                dataMap.put(values.get(0).getTextContent(), values.get(1));
            }

            if (StringUtils.equals(type, "Rule Builder")) {
                newRuleElement = rulesElement.addChildElement("com.mirth.connect.plugins.rulebuilder.RuleBuilderRule");

                newRuleElement.addChildElement("field", dataMap.get("Field").getTextContent());

                String condition;
                switch (dataMap.get("Equals").getTextContent()) {
                    case "0":
                        condition = "NOT_EQUAL";
                        break;
                    case "1":
                        condition = "EQUALS";
                        break;
                    case "2":
                        condition = "EXISTS";
                        break;
                    case "3":
                        condition = "NOT_EXIST";
                        break;
                    case "4":
                        condition = "CONTAINS";
                        break;
                    case "5":
                        condition = "NOT_CONTAIN";
                        break;
                    default:
                        condition = "EXISTS";
                }
                newRuleElement.addChildElement("condition", condition);

                DonkeyElement values = newRuleElement.addChildElement("values");
                for (DonkeyElement value : dataMap.get("Values").getChildElements()) {
                    values.addChildElement("string", value.getTextContent());
                }
            } else if (StringUtils.equals(type, "External Script")) {
                newRuleElement = rulesElement.addChildElement("com.mirth.connect.plugins.scriptfilerule.ExternalScriptRule");

                newRuleElement.addChildElement("scriptPath", dataMap.get("Variable").getTextContent());
            } else {
                newRuleElement = rulesElement.addChildElement("com.mirth.connect.plugins.javascriptrule.JavaScriptRule");

                DonkeyElement script = dataMap.get("Script");
                if (script != null) {
                    newRuleElement.addChildElement("script", script.getTextContent());
                } else {
                    newRuleElement.addChildElement("script", ruleElement.getChildElement("script").getTextContent());
                }
            }

            newRuleElement.addChildElement("sequenceNumber", ruleElement.getChildElement("sequenceNumber").getTextContent());
            newRuleElement.addChildElement("operator", ruleElement.getChildElement("operator").getTextContent());

            DonkeyElement nameElement = ruleElement.getChildElement("name");
            newRuleElement.addChildElement("name", nameElement != null ? nameElement.getTextContent() : "");
        }
    }
    
    @Override
    public void migrate3_6_0(DonkeyElement element) {}

    @Override
    public Filter clone() {
        return new Filter(this);
    }
}
