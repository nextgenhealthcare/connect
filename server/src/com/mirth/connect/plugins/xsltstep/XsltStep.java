/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.xsltstep;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.Step;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("step")
public class XsltStep extends Step {

    public static final String PLUGIN_POINT = "XSLT Step";

    private String sourceXml;
    private String resultVariable;
    private String template;
    private boolean useCustomFactory;
    private String customFactory;

    public XsltStep() {
        sourceXml = "";
        resultVariable = "";
        template = "";
        useCustomFactory = false;
        customFactory = "";
    }

    public XsltStep(XsltStep props) {
        sourceXml = props.getSourceXml();
        resultVariable = props.getResultVariable();
        template = props.getTemplate();
        useCustomFactory = props.isUseCustomFactory();
        customFactory = props.getCustomFactory();
    }

    @Override
    public String getScript(boolean loadFiles) {
        StringBuilder script = new StringBuilder();
        script.append(getTransformationScript());
        script.append("channelMap.put('" + resultVariable + "', resultVar.toString());\n");
        return script.toString();
    }

    private String getTransformationScript() {
        StringBuilder script = new StringBuilder();
        if (useCustomFactory && StringUtils.isNotEmpty(customFactory)) {
            script.append("tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance(\"" + customFactory + "\", null);\n");
        } else {
            script.append("tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\n");
        }

        script.append("xsltTemplate = new Packages.java.io.StringReader(" + template + ");\n");
        script.append("transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\n");
        script.append("sourceVar = new Packages.java.io.StringReader(" + sourceXml + ");\n");
        script.append("resultVar = new Packages.java.io.StringWriter();\n");
        script.append("transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n");

        return script.toString();
    }

    public String getSourceXml() {
        return sourceXml;
    }

    public void setSourceXml(String sourceXml) {
        this.sourceXml = sourceXml;
    }

    public String getResultVariable() {
        return resultVariable;
    }

    public void setResultVariable(String resultVariable) {
        this.resultVariable = resultVariable;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isUseCustomFactory() {
        return useCustomFactory;
    }

    public void setUseCustomFactory(boolean useCustomFactory) {
        this.useCustomFactory = useCustomFactory;
    }

    public String getCustomFactory() {
        return customFactory;
    }

    public void setCustomFactory(String customFactory) {
        this.customFactory = customFactory;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Step clone() {
        return new XsltStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("templateLines", PurgeUtil.countLines(template));
        purgedProperties.put("useCustomFactory", useCustomFactory);
        return purgedProperties;
    }
}