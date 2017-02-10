/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JavaScriptSharedUtilTest {

    @Test
    public void testPrettyPrint() {
        String script = "for (var i = 0; i < getArrayOrXmlLength(msg['OBR']); i++) {for (var j = 0; j < getArrayOrXmlLength(msg['OBR'][i]['OBR.3']); j++) {if (typeof(tmp) == 'xml') {if (typeof(tmp['OBR'][i]) == 'undefined') {createSegment('OBR', tmp, i);}if (typeof(tmp['OBR'][i]['OBR.3'][j]) == 'undefined') {createSegment('OBR.3', tmp['OBR'][i], j);}} else {if (typeof(tmp) == 'undefined') {tmp = {};}if (typeof(tmp['OBR']) == 'undefined') {tmp['OBR'] = [];}if (typeof(tmp['OBR'][i]) == 'undefined') {tmp['OBR'][i] = {};}if (typeof(tmp['OBR'][i]['OBR.3']) == 'undefined') {tmp['OBR'][i]['OBR.3'] = [];}if (typeof(tmp['OBR'][i]['OBR.3'][j]) == 'undefined') {tmp['OBR'][i]['OBR.3'][j] = {};}if (typeof(tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']) == 'undefined') {tmp['OBR'][i]['OBR.3'][j]['OBR.3.1'] = {};}}tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'] = validate(msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString(), '', new Array());}}";

        // @formatter:off
        String expected =
            "for (var i = 0; i < getArrayOrXmlLength(msg['OBR']); i++) {\n"+
            "    for (var j = 0; j < getArrayOrXmlLength(msg['OBR'][i]['OBR.3']); j++) {\n"+
            "        if (typeof(tmp) == 'xml') {\n"+
            "            if (typeof(tmp['OBR'][i]) == 'undefined') {\n"+
            "                createSegment('OBR', tmp, i);\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR'][i]['OBR.3'][j]) == 'undefined') {\n"+
            "                createSegment('OBR.3', tmp['OBR'][i], j);\n"+
            "            }\n"+
            "        } else {\n"+
            "            if (typeof(tmp) == 'undefined') {\n"+
            "                tmp = {};\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR']) == 'undefined') {\n"+
            "                tmp['OBR'] = [];\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR'][i]) == 'undefined') {\n"+
            "                tmp['OBR'][i] = {};\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR'][i]['OBR.3']) == 'undefined') {\n"+
            "                tmp['OBR'][i]['OBR.3'] = [];\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR'][i]['OBR.3'][j]) == 'undefined') {\n"+
            "                tmp['OBR'][i]['OBR.3'][j] = {};\n"+
            "            }\n"+
            "            if (typeof(tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']) == 'undefined') {\n"+
            "                tmp['OBR'][i]['OBR.3'][j]['OBR.3.1'] = {};\n"+
            "            }\n"+
            "        }\n"+
            "        tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'] = validate(msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString(), '', new Array());\n"+
            "    }\n"+
            "}\n";
        // @formatter:on

        assertEquals(expected.trim(), JavaScriptSharedUtil.prettyPrint(script).trim());
    }

    @Test
    public void testPrettyPrintWithE4X() {
        String script = "var _results = Lists.list();\nfor (var i = 0; i < getArrayOrXmlLength(msg['OBX']); i++) {\n\ntFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\nxsltTemplate = new Packages.java.io.StringReader(<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">    <xsl:template match=\"/\">        <values>            <xsl:for-each select=\"OBX/OBX.5\">                <value><xsl:value-of select=\"OBX.5.1\"/></value>            </xsl:for-each>        </values>    </xsl:template></xsl:stylesheet>);\ntransformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\nsourceVar = new Packages.java.io.StringReader(msg['OBX'][i]);\nresultVar = new Packages.java.io.StringWriter();\ntransformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n_results.add(resultVar.toString());\n\n\n}\nchannelMap.put('results', _results.toArray());";

        // @formatter:off
        String expected = 
            "var _results = Lists.list();\n"+
            "for (var i = 0; i < getArrayOrXmlLength(msg['OBX']); i++) {\n"+
            "\n"+
            "    tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\n"+
            "    xsltTemplate = new Packages.java.io.StringReader(<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">    <xsl:template match=\"/\">        <values>            <xsl:for-each select=\"OBX/OBX.5\">                <value><xsl:value-of select=\"OBX.5.1\"/></value>            </xsl:for-each>        </values>    </xsl:template></xsl:stylesheet>);\n"+
            "    transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\n"+
            "    sourceVar = new Packages.java.io.StringReader(msg['OBX'][i]);\n"+
            "    resultVar = new Packages.java.io.StringWriter();\n"+
            "    transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n"+
            "    _results.add(resultVar.toString());\n"+
            "\n"+
            "\n"+
            "}\n"+
            "channelMap.put('results', _results.toArray());\n";
        // @formatter:on

        assertEquals(expected.trim(), JavaScriptSharedUtil.prettyPrint(script).trim());
    }

    @Test
    public void testPrettyPrintWithE4XAndProlog() {
        String script = "var _results = Lists.list();\nfor (var i = 0; i < getArrayOrXmlLength(msg['OBX']); i++) {\n\n    tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\n    xsltTemplate = new Packages.java.io.StringReader(<?xml version=\"1.0\" encoding=\"UTF-8\"?> <xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">    <xsl:template match=\"/\">        <values>            <xsl:for-each select=\"OBX/OBX.5\">                <value><xsl:value-of select=\"OBX.5.1\"/></value>            </xsl:for-each>        </values>    </xsl:template></xsl:stylesheet>);\n    transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\n    sourceVar = new Packages.java.io.StringReader(msg['OBX'][i]);\n    resultVar = new Packages.java.io.StringWriter();\n    transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n    _results.add(resultVar.toString());\n\n\n}\nchannelMap.put('results', _results.toArray());";

        // @formatter:off
        String expected = 
            "var _results = Lists.list();\n"+
            "for (var i = 0; i < getArrayOrXmlLength(msg['OBX']); i++) {\n"+
            "\n"+
            "    tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();\n"+
            "    xsltTemplate = new Packages.java.io.StringReader(<?xml version=\"1.0\" encoding=\"UTF-8\"?> <xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">    <xsl:template match=\"/\">        <values>            <xsl:for-each select=\"OBX/OBX.5\">                <value><xsl:value-of select=\"OBX.5.1\"/></value>            </xsl:for-each>        </values>    </xsl:template></xsl:stylesheet>);\n"+
            "    transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));\n"+
            "    sourceVar = new Packages.java.io.StringReader(msg['OBX'][i]);\n"+
            "    resultVar = new Packages.java.io.StringWriter();\n"+
            "    transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));\n"+
            "    _results.add(resultVar.toString());\n"+
            "\n"+
            "\n"+
            "}\n"+
            "channelMap.put('results', _results.toArray());\n";
        // @formatter:on

        assertEquals(expected.trim(), JavaScriptSharedUtil.prettyPrint(script).trim());
    }
}