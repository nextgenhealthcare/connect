/*
 * VariableListUtil.java
 *
 * Created on March 12, 2007, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.util;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author brendanh
 */
public class VariableListUtil
{
    final static String GLOBAL_VAR_PATTERN = "[channel][global][response]Map.put\\(['|\"]([^'|^\"]*)[\"|']";
    final static String VAR_PATTERN = "[connector][channel][global][response]Map.put\\(['|\"]([^'|^\"]*)[\"|']";
    
    public static ArrayList<String> getStepVariables(List<Step> addToList)
    {
        ArrayList<String> variables = new ArrayList<String>();
        int i = 0;
        for (Iterator it = addToList.iterator(); it.hasNext();)
        {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();
            if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE))
            {
                variables.add((String) data.get("Variable"));
                i++;
            }
            else if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE))
            {
                Pattern pattern = Pattern.compile(VAR_PATTERN);
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }
        return variables;
    }
    
    public static ArrayList<String> getRuleVariables(List<Rule> addToList)
    {
        ArrayList<String> variables = new ArrayList<String>();
        int i = 0;
        for (Iterator it = addToList.iterator(); it.hasNext();)
        {
            Rule rule = (Rule) it.next();
            Pattern pattern = Pattern.compile(VAR_PATTERN);
            Matcher matcher = pattern.matcher(rule.getScript());
            while (matcher.find())
            {
                String key = matcher.group(1);
                variables.add(key);
            }
        }
        return variables;
    }
    
    public static void getStepGlobalVariables(List<Step> addToList, Connector connector)
    {
        
        // add only the global variables
        List<Step> connectorSteps = connector.getTransformer().getSteps();
        Iterator stepIterator = connectorSteps.iterator();
        while (stepIterator.hasNext())
        {
            Step step = (Step) stepIterator.next();
            HashMap map = (HashMap) step.getData();
            if (step.getType().equals(TransformerPane.MAPPER_TYPE))
            {
                // Check if the step is global
                if (map.containsKey(UIConstants.IS_GLOBAL))
                {
                    if (!((String) map.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_CONNECTOR))
                        addToList.add(step);
                }
            }
            else if (step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
            {
                Pattern pattern = Pattern.compile(GLOBAL_VAR_PATTERN);
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    Step tempStep = new Step();
                    Map tempMap = new HashMap();
                    tempMap.put("Variable", key);
                    tempStep.setData(tempMap);
                    tempStep.setType(TransformerPane.MAPPER_TYPE);
                    addToList.add(tempStep);
                }
            }
        }
    }
    
    public static void getRuleGlobalVariables(List<Rule> addToList, Connector connector)
    {
        
        // add only the global variables
        List<Rule> connectorRules = connector.getFilter().getRules();
        Iterator ruleIterator = connectorRules.iterator();
        while (ruleIterator.hasNext())
        {
            Rule rule = (Rule) ruleIterator.next();
            Pattern pattern = Pattern.compile(GLOBAL_VAR_PATTERN);
            Matcher matcher = pattern.matcher(rule.getScript());
            while (matcher.find())
            {
                String key = matcher.group(1);
                addToList.add(rule);
            }
        }
    }
}
