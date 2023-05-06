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
    		"<com.mirth.connect.server.userutil.MirthCachedRowSet>\n" + 
    		"  <delegate class=\"com.sun.rowset.CachedRowSetImpl\" serialization=\"custom\">\n" + 
    		"    <javax.sql.rowset.BaseRowSet>\n" + 
    		"      <default>\n" + 
    		"        <concurrency>1008</concurrency>\n" + 
    		"        <escapeProcessing>true</escapeProcessing>\n" + 
    		"        <fetchDir>1000</fetchDir>\n" + 
    		"        <fetchSize>0</fetchSize>\n" + 
    		"        <isolation>2</isolation>\n" + 
    		"        <maxFieldSize>0</maxFieldSize>\n" + 
    		"        <maxRows>0</maxRows>\n" + 
    		"        <queryTimeout>0</queryTimeout>\n" + 
    		"        <readOnly>true</readOnly>\n" + 
    		"        <rowSetType>1004</rowSetType>\n" + 
    		"        <showDeleted>false</showDeleted>\n" + 
    		"        <listeners/>\n" + 
    		"        <params/>\n" + 
    		"      </default>\n" + 
    		"    </javax.sql.rowset.BaseRowSet>\n" + 
    		"    <com.sun.rowset.CachedRowSetImpl>\n" + 
    		"      <default>\n" + 
    		"        <absolutePos>0</absolutePos>\n" + 
    		"        <callWithCon>false</callWithCon>\n" + 
    		"        <currentRow>0</currentRow>\n" + 
    		"        <cursorPos>0</cursorPos>\n" + 
    		"        <dbmslocatorsUpdateCopy>false</dbmslocatorsUpdateCopy>\n" + 
    		"        <endPos>0</endPos>\n" + 
    		"        <iMatchColumn>-1</iMatchColumn>\n" + 
    		"        <lastValueNull>false</lastValueNull>\n" + 
    		"        <maxRowsreached>0</maxRowsreached>\n" + 
    		"        <numDeleted>0</numDeleted>\n" + 
    		"        <numRows>0</numRows>\n" + 
    		"        <onFirstPage>false</onFirstPage>\n" + 
    		"        <onInsertRow>false</onInsertRow>\n" + 
    		"        <onLastPage>false</onLastPage>\n" + 
    		"        <pageSize>0</pageSize>\n" + 
    		"        <pagenotend>true</pagenotend>\n" + 
    		"        <populatecallcount>0</populatecallcount>\n" + 
    		"        <prevEndPos>0</prevEndPos>\n" + 
    		"        <startPos>0</startPos>\n" + 
    		"        <startPrev>0</startPrev>\n" + 
    		"        <tXWriter>true</tXWriter>\n" + 
    		"        <totalRows>0</totalRows>\n" + 
    		"        <updateOnInsert>false</updateOnInsert>\n" + 
    		"        <DEFAULT__SYNC__PROVIDER>com.sun.rowset.providers.RIOptimisticProvider</DEFAULT__SYNC__PROVIDER>\n" + 
    		"        <iMatchColumns>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"          <int>-1</int>\n" + 
    		"        </iMatchColumns>\n" + 
    		"        <provider class=\"com.sun.rowset.providers.RIOptimisticProvider\" serialization=\"custom\">\n" + 
    		"          <unserializable-parents/>\n" + 
    		"          <com.sun.rowset.providers.RIOptimisticProvider>\n" + 
    		"            <default>\n" + 
    		"              <providerID>com.sun.rowset.providers.RIOptimisticProvider</providerID>\n" + 
    		"              <reader serialization=\"custom\">\n" + 
    		"                <com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"                  <default>\n" + 
    		"                    <startPosition>0</startPosition>\n" + 
    		"                    <userCon>false</userCon>\n" + 
    		"                    <writerCalls>0</writerCalls>\n" + 
    		"                    <resBundle/>\n" + 
    		"                  </default>\n" + 
    		"                </com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"              </reader>\n" + 
    		"              <resBundle/>\n" + 
    		"              <vendorName>Oracle Corporation</vendorName>\n" + 
    		"              <versionNumber>1.0</versionNumber>\n" + 
    		"              <writer serialization=\"custom\">\n" + 
    		"                <com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"                  <default>\n" + 
    		"                    <callerColumnCount>0</callerColumnCount>\n" + 
    		"                    <iChangedValsInDbAndCRS>0</iChangedValsInDbAndCRS>\n" + 
    		"                    <iChangedValsinDbOnly>0</iChangedValsinDbOnly>\n" + 
    		"                    <reader serialization=\"custom\">\n" + 
    		"                      <com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"                        <default>\n" + 
    		"                          <startPosition>0</startPosition>\n" + 
    		"                          <userCon>false</userCon>\n" + 
    		"                          <writerCalls>0</writerCalls>\n" + 
    		"                          <resBundle/>\n" + 
    		"                        </default>\n" + 
    		"                      </com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"                    </reader>\n" + 
    		"                    <resBundle/>\n" + 
    		"                  </default>\n" + 
    		"                </com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"              </writer>\n" + 
    		"            </default>\n" + 
    		"          </com.sun.rowset.providers.RIOptimisticProvider>\n" + 
    		"        </provider>\n" + 
    		"        <rowSetReader class=\"com.sun.rowset.internal.CachedRowSetReader\" serialization=\"custom\">\n" + 
    		"          <com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"            <default>\n" + 
    		"              <startPosition>0</startPosition>\n" + 
    		"              <userCon>false</userCon>\n" + 
    		"              <writerCalls>0</writerCalls>\n" + 
    		"              <resBundle/>\n" + 
    		"            </default>\n" + 
    		"          </com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"        </rowSetReader>\n" + 
    		"        <rowSetWriter class=\"com.sun.rowset.internal.CachedRowSetWriter\" serialization=\"custom\">\n" + 
    		"          <com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"            <default>\n" + 
    		"              <callerColumnCount>0</callerColumnCount>\n" + 
    		"              <iChangedValsInDbAndCRS>0</iChangedValsInDbAndCRS>\n" + 
    		"              <iChangedValsinDbOnly>0</iChangedValsinDbOnly>\n" + 
    		"              <reader serialization=\"custom\">\n" + 
    		"                <com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"                  <default>\n" + 
    		"                    <startPosition>0</startPosition>\n" + 
    		"                    <userCon>false</userCon>\n" + 
    		"                    <writerCalls>0</writerCalls>\n" + 
    		"                    <resBundle/>\n" + 
    		"                  </default>\n" + 
    		"                </com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"              </reader>\n" + 
    		"              <resBundle/>\n" + 
    		"            </default>\n" + 
    		"          </com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"        </rowSetWriter>\n" + 
    		"        <rowsetWarning>\n" + 
    		"          <stackTrace>\n" + 
    		"            <trace>com.sun.rowset.CachedRowSetImpl.&lt;init&gt;(CachedRowSetImpl.java:384)</trace>\n" + 
    		"            <trace>com.sun.rowset.RowSetFactoryImpl.createCachedRowSet(RowSetFactoryImpl.java:49)</trace>\n" + 
    		"            <trace>com.mirth.connect.server.userutil.MirthCachedRowSet.&lt;init&gt;(MirthCachedRowSet.java:57)</trace>\n" + 
    		"            <trace>com.mirth.connect.model.converters.ObjectXMLSerializerTest.myTest(ObjectXMLSerializerTest.java:48)</trace>\n" + 
    		"            <trace>sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)</trace>\n" + 
    		"            <trace>sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)</trace>\n" + 
    		"            <trace>sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</trace>\n" + 
    		"            <trace>java.lang.reflect.Method.invoke(Method.java:498)</trace>\n" + 
    		"            <trace>org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)</trace>\n" + 
    		"            <trace>org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)</trace>\n" + 
    		"            <trace>org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:76)</trace>\n" + 
    		"            <trace>org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:28)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.run(ParentRunner.java:236)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:89)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:41)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:542)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:770)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:464)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:210)</trace>\n" + 
    		"          </stackTrace>\n" + 
    		"          <suppressedExceptions class=\"java.util.Collections$UnmodifiableRandomAccessList\" resolves-to=\"java.util.Collections$UnmodifiableList\">\n" + 
    		"            <c class=\"list\"/>\n" + 
    		"            <list/>\n" + 
    		"          </suppressedExceptions>\n" + 
    		"          <vendorCode>0</vendorCode>\n" + 
    		"        </rowsetWarning>\n" + 
    		"        <rvh/>\n" + 
    		"        <sqlwarn>\n" + 
    		"          <stackTrace>\n" + 
    		"            <trace>com.sun.rowset.CachedRowSetImpl.&lt;init&gt;(CachedRowSetImpl.java:383)</trace>\n" + 
    		"            <trace>com.sun.rowset.RowSetFactoryImpl.createCachedRowSet(RowSetFactoryImpl.java:49)</trace>\n" + 
    		"            <trace>com.mirth.connect.server.userutil.MirthCachedRowSet.&lt;init&gt;(MirthCachedRowSet.java:57)</trace>\n" + 
    		"            <trace>com.mirth.connect.model.converters.ObjectXMLSerializerTest.myTest(ObjectXMLSerializerTest.java:48)</trace>\n" + 
    		"            <trace>sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)</trace>\n" + 
    		"            <trace>sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)</trace>\n" + 
    		"            <trace>sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</trace>\n" + 
    		"            <trace>java.lang.reflect.Method.invoke(Method.java:498)</trace>\n" + 
    		"            <trace>org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)</trace>\n" + 
    		"            <trace>org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)</trace>\n" + 
    		"            <trace>org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:76)</trace>\n" + 
    		"            <trace>org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)</trace>\n" + 
    		"            <trace>org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:28)</trace>\n" + 
    		"            <trace>org.junit.runners.ParentRunner.run(ParentRunner.java:236)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:89)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:41)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:542)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:770)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:464)</trace>\n" + 
    		"            <trace>org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:210)</trace>\n" + 
    		"          </stackTrace>\n" + 
    		"          <suppressedExceptions class=\"java.util.Collections$UnmodifiableRandomAccessList\" resolves-to=\"java.util.Collections$UnmodifiableList\">\n" + 
    		"            <c class=\"list\"/>\n" + 
    		"            <list/>\n" + 
    		"          </suppressedExceptions>\n" + 
    		"          <vendorCode>0</vendorCode>\n" + 
    		"        </sqlwarn>\n" + 
    		"        <strMatchColumn></strMatchColumn>\n" + 
    		"        <strMatchColumns>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"          <null/>\n" + 
    		"        </strMatchColumns>\n" + 
    		"        <tWriter class=\"com.sun.rowset.internal.CachedRowSetWriter\" serialization=\"custom\">\n" + 
    		"          <com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"            <default>\n" + 
    		"              <callerColumnCount>0</callerColumnCount>\n" + 
    		"              <iChangedValsInDbAndCRS>0</iChangedValsInDbAndCRS>\n" + 
    		"              <iChangedValsinDbOnly>0</iChangedValsinDbOnly>\n" + 
    		"              <reader serialization=\"custom\">\n" + 
    		"                <com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"                  <default>\n" + 
    		"                    <startPosition>0</startPosition>\n" + 
    		"                    <userCon>false</userCon>\n" + 
    		"                    <writerCalls>0</writerCalls>\n" + 
    		"                    <resBundle/>\n" + 
    		"                  </default>\n" + 
    		"                </com.sun.rowset.internal.CachedRowSetReader>\n" + 
    		"              </reader>\n" + 
    		"              <resBundle/>\n" + 
    		"            </default>\n" + 
    		"          </com.sun.rowset.internal.CachedRowSetWriter>\n" + 
    		"        </tWriter>\n" + 
    		"      </default>\n" + 
    		"    </com.sun.rowset.CachedRowSetImpl>\n" + 
    		"  </delegate>\n" + 
    		"</com.mirth.connect.server.userutil.MirthCachedRowSet>";
    // @formatter:on
}
