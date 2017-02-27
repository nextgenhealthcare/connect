/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.model.IteratorStep;

public class IteratorUtilTest {

    private static DefaultMutableTreeTableNode root;
    private static TransformerTreeTableNode node1;
    private static TransformerTreeTableNode node2;

    @BeforeClass
    public static void setup() {
        IteratorStep iterator1 = new IteratorStep();
        iterator1.getProperties().setTarget("msg['OBR']");
        iterator1.getProperties().setIndexVariable("i");
        iterator1.getProperties().getPrefixSubstitutions().add("msg['OBR']");
        iterator1.getProperties().getPrefixSubstitutions().add("tmp['OBR']");

        IteratorStep iterator2 = new IteratorStep();
        iterator2.getProperties().setTarget("msg['OBR'][i]['OBR.3']");
        iterator2.getProperties().setIndexVariable("j");
        iterator2.getProperties().getPrefixSubstitutions().add("msg['OBR'][i]['OBR.3']");
        iterator2.getProperties().getPrefixSubstitutions().add("tmp['OBR'][i]['OBR.3']");
        iterator1.getProperties().getChildren().add(iterator2);

        root = new DefaultMutableTreeTableNode();
        node1 = new TransformerTreeTableNode(null, iterator1);
        root.add(node1);
        node2 = new TransformerTreeTableNode(null, iterator2);
        node1.add(node2);
    }

    @Test
    public void testReplaceIteratorVariables1() {
        String expression = "msg['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node2, true);
        assertEquals("msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testReplaceIteratorVariables2() {
        String expression = "tmp['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1']";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node2, true);
        assertEquals("tmp['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1']", actual);
    }

    @Test
    public void testReplaceIteratorVariables3() {
        String expression = "msg['OBR'][j]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node2, true);
        assertEquals("msg['OBR'][i][j]['OBR.3']['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testReplaceIteratorVariables4() {
        String expression = "msg['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node1, true);
        assertEquals("msg['OBR'][i]['OBR.3']['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testReplaceIteratorVariables5() {
        String expression = "msg['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, root, true);
        assertEquals("msg['OBR'][0]['OBR.3'][0]['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testReplaceIteratorVariables6() {
        String expression = "";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node2, true);
        assertEquals("", actual);
    }

    @Test
    public void testRemoveIteratorVariables1() {
        String expression = "msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node2, false);
        assertEquals("msg['OBR']['OBR.3']['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testRemoveIteratorVariables2() {
        String expression = "msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, node1, false);
        assertEquals("msg['OBR']['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testRemoveIteratorVariables3() {
        String expression = "msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()";
        String actual = IteratorUtil.replaceOrRemoveIteratorVariables(expression, root, false);
        assertEquals("msg['OBR'][i]['OBR.3'][j]['OBR.3.1']['OBR.3.1.1'].toString()", actual);
    }

    @Test
    public void testGetAncestorIndexVariables() {
        assertArrayEquals(new Object[] { "i",
                "j" }, IteratorUtil.getAncestorIndexVariables(node2).toArray());
    }

    @Test
    public void testGetDescendantIndexVariables() {
        assertArrayEquals(new Object[] {
                "j" }, IteratorUtil.getDescendantIndexVariables(node2).toArray());
    }

    @Test
    public void testGetValidIndexVariable1() {
        assertEquals("j", IteratorUtil.getValidIndexVariable(node1, null));
    }

    @Test
    public void testGetValidIndexVariable2() {
        assertEquals("i", IteratorUtil.getValidIndexVariable(null, node2));
    }
}