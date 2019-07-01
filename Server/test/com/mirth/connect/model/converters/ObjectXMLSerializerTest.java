/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.MapContent;
import com.mirth.connect.server.userutil.MirthCachedRowSet;

public class ObjectXMLSerializerTest {

    @BeforeClass
    public static void setup() throws Exception {
        try {
            ObjectXMLSerializer.getInstance().init(Version.getLatest().toString());
        } catch (Exception e) {
            // Ignore if it has already been initialized
        }
    }

    @Test
    public void testInvalidMapContent() throws Exception {
        ConnectorMessage connectorMessage = new ConnectorMessage();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", ObjectXMLSerializer.getInstance().deserialize(CACHED_ROW_SET_XML, MirthCachedRowSet.class));
        connectorMessage.setChannelMapContent(new MapContent(map, true));

        // Shouldn't cause any errors
        String xml = ObjectXMLSerializer.getInstance().serialize(connectorMessage);
        ObjectXMLSerializer.getInstance().deserialize(xml, ConnectorMessage.class);
    }

    // @formatter:off
    private static final String CACHED_ROW_SET_XML = 
            "<com.mirth.connect.server.userutil.MirthCachedRowSet serialization=\"custom\">"+
            "    <javax.sql.rowset.BaseRowSet>"+
            "        <default>"+
            "            <concurrency>1008</concurrency>"+
            "            <escapeProcessing>true</escapeProcessing>"+
            "            <fetchDir>1000</fetchDir>"+
            "            <fetchSize>0</fetchSize>"+
            "            <isolation>2</isolation>"+
            "            <maxFieldSize>0</maxFieldSize>"+
            "            <maxRows>0</maxRows>"+
            "            <queryTimeout>0</queryTimeout>"+
            "            <readOnly>true</readOnly>"+
            "            <rowSetType>1004</rowSetType>"+
            "            <showDeleted>false</showDeleted>"+
            "            <listeners/>"+
            "            <params/>"+
            "        </default>"+
            "    </javax.sql.rowset.BaseRowSet>"+
            "    <com.sun.rowset.CachedRowSetImpl>"+
            "        <default>"+
            "            <absolutePos>0</absolutePos>"+
            "            <callWithCon>false</callWithCon>"+
            "            <currentRow>0</currentRow>"+
            "            <cursorPos>0</cursorPos>"+
            "            <dbmslocatorsUpdateCopy>false</dbmslocatorsUpdateCopy>"+
            "            <endPos>0</endPos>"+
            "            <iMatchColumn>-1</iMatchColumn>"+
            "            <lastValueNull>false</lastValueNull>"+
            "            <maxRowsreached>0</maxRowsreached>"+
            "            <numDeleted>0</numDeleted>"+
            "            <numRows>4373</numRows>"+
            "            <onFirstPage>false</onFirstPage>"+
            "            <onInsertRow>false</onInsertRow>"+
            "            <onLastPage>false</onLastPage>"+
            "            <pageSize>0</pageSize>"+
            "            <pagenotend>true</pagenotend>"+
            "            <populatecallcount>0</populatecallcount>"+
            "            <prevEndPos>0</prevEndPos>"+
            "            <startPos>0</startPos>"+
            "            <startPrev>0</startPrev>"+
            "            <tXWriter>true</tXWriter>"+
            "            <totalRows>0</totalRows>"+
            "            <updateOnInsert>false</updateOnInsert>"+
            "            <DEFAULT__SYNC__PROVIDER>com.sun.rowset.providers.RIOptimisticProvider</DEFAULT__SYNC__PROVIDER>"+
            "            <RowSetMD>"+
            "                <colCount>1</colCount>"+
            "                <colInfo>"+
            "                    <null/>"+
            "                    <javax.sql.rowset.RowSetMetaDataImpl_-ColInfo>"+
            "                        <autoIncrement>false</autoIncrement>"+
            "                        <caseSensitive>true</caseSensitive>"+
            "                        <currency>false</currency>"+
            "                        <nullable>1</nullable>"+
            "                        <signed>false</signed>"+
            "                        <searchable>true</searchable>"+
            "                        <columnDisplaySize>2147483647</columnDisplaySize>"+
            "                        <columnLabel>col1</columnLabel>"+
            "                        <columnName>col1</columnName>"+
            "                        <schemaName/>"+
            "                        <colPrecision>2147483647</colPrecision>"+
            "                        <colScale>0</colScale>"+
            "                        <tableName>testtable</tableName>"+
            "                        <catName/>"+
            "                        <colType>12</colType>"+
            "                        <colTypeName>varchar</colTypeName>"+
            "                        <readOnly>false</readOnly>"+
            "                        <writable>true</writable>"+
            "                        <outer-class reference=\"../../..\"/>"+
            "                    </javax.sql.rowset.RowSetMetaDataImpl_-ColInfo>"+
            "                </colInfo>"+
            "            </RowSetMD>"+
            "            <iMatchColumns>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "                <int>-1</int>"+
            "            </iMatchColumns>"+
            "            <provider class=\"com.sun.rowset.providers.RIOptimisticProvider\" serialization=\"custom\">"+
            "                <unserializable-parents/>"+
            "                <com.sun.rowset.providers.RIOptimisticProvider>"+
            "                    <default>"+
            "                        <providerID>com.sun.rowset.providers.RIOptimisticProvider</providerID>"+
            "                        <reader serialization=\"custom\">"+
            "                            <com.sun.rowset.internal.CachedRowSetReader>"+
            "                                <default>"+
            "                                    <startPosition>0</startPosition>"+
            "                                    <userCon>false</userCon>"+
            "                                    <writerCalls>0</writerCalls>"+
            "                                    <resBundle/>"+
            "                                </default>"+
            "                            </com.sun.rowset.internal.CachedRowSetReader>"+
            "                        </reader>"+
            "                        <resBundle reference=\"../reader/com.sun.rowset.internal.CachedRowSetReader/default/resBundle\"/>"+
            "                        <vendorName>Oracle Corporation</vendorName>"+
            "                        <versionNumber>1.0</versionNumber>"+
            "                        <writer serialization=\"custom\">"+
            "                            <com.sun.rowset.internal.CachedRowSetWriter>"+
            "                                <default>"+
            "                                    <callerColumnCount>0</callerColumnCount>"+
            "                                    <iChangedValsInDbAndCRS>0</iChangedValsInDbAndCRS>"+
            "                                    <iChangedValsinDbOnly>0</iChangedValsinDbOnly>"+
            "                                    <reader reference=\"../../../../reader\"/>"+
            "                                    <resBundle reference=\"../../../../reader/com.sun.rowset.internal.CachedRowSetReader/default/resBundle\"/>"+
            "                                </default>"+
            "                            </com.sun.rowset.internal.CachedRowSetWriter>"+
            "                        </writer>"+
            "                    </default>"+
            "                </com.sun.rowset.providers.RIOptimisticProvider>"+
            "            </provider>"+
            "            <rowSetReader class=\"com.sun.rowset.internal.CachedRowSetReader\" reference=\"../provider/com.sun.rowset.providers.RIOptimisticProvider/default/reader\"/>"+
            "            <rowSetWriter class=\"com.sun.rowset.internal.CachedRowSetWriter\" reference=\"../provider/com.sun.rowset.providers.RIOptimisticProvider/default/writer\"/>"+
            "            <rowsetWarning>"+
            "                <stackTrace>"+
            "                    <trace>com.sun.rowset.CachedRowSetImpl.&lt;init&gt;(CachedRowSetImpl.java:384)</trace>"+
            "                    <trace>com.mirth.connect.server.userutil.MirthCachedRowSet.&lt;init&gt;(MirthCachedRowSet.java:37)</trace>"+
            "                    <trace>com.mirth.connect.server.userutil.DatabaseConnection.executeCachedQuery(DatabaseConnection.java:128)</trace>"+
            "                    <trace>sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)</trace>"+
            "                    <trace>sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)</trace>"+
            "                    <trace>sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</trace>"+
            "                    <trace>java.lang.reflect.Method.invoke(Method.java:498)</trace>"+
            "                    <trace>org.mozilla.javascript.MemberBox.invoke(MemberBox.java:126)</trace>"+
            "                    <trace>org.mozilla.javascript.NativeJavaMethod.call(NativeJavaMethod.java:225)</trace>"+
            "                    <trace>org.mozilla.javascript.Interpreter.interpretLoop(Interpreter.java:1479)</trace>"+
            "                    <trace>org.mozilla.javascript.Interpreter.interpret(Interpreter.java:815)</trace>"+
            "                    <trace>org.mozilla.javascript.InterpretedFunction.call(InterpretedFunction.java:109)</trace>"+
            "                    <trace>org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:405)</trace>"+
            "                    <trace>org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3508)</trace>"+
            "                    <trace>org.mozilla.javascript.InterpretedFunction.exec(InterpretedFunction.java:120)</trace>"+
            "                    <trace>com.mirth.connect.server.util.javascript.JavaScriptTask.executeScript(JavaScriptTask.java:150)</trace>"+
            "                    <trace>com.mirth.connect.server.transformers.JavaScriptFilterTransformer$FilterTransformerTask.doCall(JavaScriptFilterTransformer.java:143)</trace>"+
            "                    <trace>com.mirth.connect.server.transformers.JavaScriptFilterTransformer$FilterTransformerTask.doCall(JavaScriptFilterTransformer.java:1)</trace>"+
            "                    <trace>com.mirth.connect.server.util.javascript.JavaScriptTask.call(JavaScriptTask.java:113)</trace>"+
            "                    <trace>java.util.concurrent.FutureTask.run(FutureTask.java:266)</trace>"+
            "                    <trace>java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)</trace>"+
            "                    <trace>java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)</trace>"+
            "                    <trace>java.lang.Thread.run(Thread.java:748)</trace>"+
            "                </stackTrace>"+
            "                <suppressedExceptions class=\"java.util.Collections$UnmodifiableRandomAccessList\" resolves-to=\"java.util.Collections$UnmodifiableList\">"+
            "                    <c class=\"list\"/>"+
            "                    <list reference=\"../c\"/>"+
            "                </suppressedExceptions>"+
            "                <vendorCode>0</vendorCode>"+
            "            </rowsetWarning>"+
            "            <rvh>"+
            "                <com.sun.rowset.internal.Row>"+
            "                    <origVals>"+
            "                        <string>dummy</string>"+
            "                    </origVals>"+
            "                    <currentVals>"+
            "                        <null/>"+
            "                    </currentVals>"+
            "                    <colsChanged/>"+
            "                    <deleted>false</deleted>"+
            "                    <updated>false</updated>"+
            "                    <inserted>false</inserted>"+
            "                    <numCols>1</numCols>"+
            "                </com.sun.rowset.internal.Row>"+
            "            </rvh>"+
            "            <sqlwarn>"+
            "                <stackTrace>"+
            "                    <trace>com.sun.rowset.CachedRowSetImpl.&lt;init&gt;(CachedRowSetImpl.java:383)</trace>"+
            "                    <trace>com.mirth.connect.server.userutil.MirthCachedRowSet.&lt;init&gt;(MirthCachedRowSet.java:37)</trace>"+
            "                    <trace>com.mirth.connect.server.userutil.DatabaseConnection.executeCachedQuery(DatabaseConnection.java:128)</trace>"+
            "                    <trace>sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)</trace>"+
            "                    <trace>sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)</trace>"+
            "                    <trace>sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</trace>"+
            "                    <trace>java.lang.reflect.Method.invoke(Method.java:498)</trace>"+
            "                    <trace>org.mozilla.javascript.MemberBox.invoke(MemberBox.java:126)</trace>"+
            "                    <trace>org.mozilla.javascript.NativeJavaMethod.call(NativeJavaMethod.java:225)</trace>"+
            "                    <trace>org.mozilla.javascript.Interpreter.interpretLoop(Interpreter.java:1479)</trace>"+
            "                    <trace>org.mozilla.javascript.Interpreter.interpret(Interpreter.java:815)</trace>"+
            "                    <trace>org.mozilla.javascript.InterpretedFunction.call(InterpretedFunction.java:109)</trace>"+
            "                    <trace>org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:405)</trace>"+
            "                    <trace>org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3508)</trace>"+
            "                    <trace>org.mozilla.javascript.InterpretedFunction.exec(InterpretedFunction.java:120)</trace>"+
            "                    <trace>com.mirth.connect.server.util.javascript.JavaScriptTask.executeScript(JavaScriptTask.java:150)</trace>"+
            "                    <trace>com.mirth.connect.server.transformers.JavaScriptFilterTransformer$FilterTransformerTask.doCall(JavaScriptFilterTransformer.java:143)</trace>"+
            "                    <trace>com.mirth.connect.server.transformers.JavaScriptFilterTransformer$FilterTransformerTask.doCall(JavaScriptFilterTransformer.java:1)</trace>"+
            "                    <trace>com.mirth.connect.server.util.javascript.JavaScriptTask.call(JavaScriptTask.java:113)</trace>"+
            "                    <trace>java.util.concurrent.FutureTask.run(FutureTask.java:266)</trace>"+
            "                    <trace>java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)</trace>"+
            "                    <trace>java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)</trace>"+
            "                    <trace>java.lang.Thread.run(Thread.java:748)</trace>"+
            "                </stackTrace>"+
            "                <suppressedExceptions class=\"java.util.Collections$UnmodifiableRandomAccessList\" reference=\"../../rowsetWarning/suppressedExceptions\"/>"+
            "                <vendorCode>0</vendorCode>"+
            "            </sqlwarn>"+
            "            <strMatchColumn/>"+
            "            <strMatchColumns>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "                <null/>"+
            "            </strMatchColumns>"+
            "            <tWriter class=\"com.sun.rowset.internal.CachedRowSetWriter\" reference=\"../provider/com.sun.rowset.providers.RIOptimisticProvider/default/writer\"/>"+
            "        </default>"+
            "    </com.sun.rowset.CachedRowSetImpl>"+
            "</com.mirth.connect.server.userutil.MirthCachedRowSet>";
    // @formatter:on
}
