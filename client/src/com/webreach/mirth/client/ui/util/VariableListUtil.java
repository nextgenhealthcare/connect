/*
 * VariableListUtil.java
 *
 * Created on March 12, 2007, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;

/**
 *
 * @author brendanh
 */
public class VariableListUtil
{
    final static String GLOBAL_AND_CHANNEL_VARIABLE_PATTERN = "(?:(?:(?:channel|global|response)(?:M|m)ap.put)|\\$(?:g|c|r))\\(\\s*['|\"]([^'|^\"|^\\s]*)[\"|']*";
    final static String LOCAL_VARIABLE_PATTERN = "(?:(?:(?:channel|global|response|connector)(?:M|m)ap.put)|\\$(?:g|c|r|co))\\(\\s*['|\"]([^'|^\"|^\\s]*)[\"|']*";
    final static int MATCHER_INDEX = 1;
    public static void getStepVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars)
    {
    	getStepVariables(targetSet, connector, includeLocalVars, -1);
    }
    /* 
     * Gets all steps that have variables that should show up in the global variable list
     */
    public static void getStepVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars, int row)
    {
        
        // add only the global variables
        List<Step> connectorSteps = connector.getTransformer().getSteps();
        Iterator<Step> stepIterator = connectorSteps.iterator();
        String varPattern = GLOBAL_AND_CHANNEL_VARIABLE_PATTERN;
        if (includeLocalVars){
        	varPattern = LOCAL_VARIABLE_PATTERN;
        }
        int currentRow = 0;
        while (stepIterator.hasNext())
        {
        	if (row > -1 && row <= currentRow){
        		break;
        	}
            Pattern pattern = Pattern.compile(varPattern);
            Matcher matcher = pattern.matcher(stepIterator.next().getScript());
            while (matcher.find()){
            	targetSet.add(matcher.group(1));
            }
            currentRow++;
        }
    }
    public static void getRuleVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars)
    {
    	 getRuleVariables(targetSet, connector, includeLocalVars, -1);
    }
    /* 
     * Gets all rules that have variables that should show up in the global variable list
     */
    public static void getRuleVariables(Set<String> targetSet, Connector connector, boolean includeLocalVars, int row)
    {
        
        // add only the global variables
        List<Rule> connectorRules = connector.getFilter().getRules();
        Iterator<Rule> ruleIterator = connectorRules.iterator();
        String varPattern = GLOBAL_AND_CHANNEL_VARIABLE_PATTERN;
        if (includeLocalVars){
        	varPattern = LOCAL_VARIABLE_PATTERN;
        }
        int currentRow = 0;
        while (ruleIterator.hasNext())
        {
        	if (row > -1 && row >= currentRow){
        		break;
        	}
            Pattern pattern = Pattern.compile(varPattern);
            Matcher matcher = pattern.matcher(ruleIterator.next().getScript());
            while (matcher.find())
            {
            	targetSet.add(matcher.group(MATCHER_INDEX));
            }
            currentRow++;
        }
    }
}
