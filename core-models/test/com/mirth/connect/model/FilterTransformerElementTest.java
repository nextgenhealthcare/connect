/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.util.ScriptBuilderException;

public class FilterTransformerElementTest {

    /*
     * filter3_6_0.xml contains 3 RuleBuilderRules
     */
    @Test
    public void testMigrate3_6_0FilterTransformerElement3_7_0() throws Exception {
        String oldFilterStr = FileUtils.readFileToString(new File("tests/RuleBuilderRule3_6_0.xml"));
        DonkeyElement filterDonkey = new DonkeyElement(oldFilterStr);
        FilterTransformerElement ftElement = new FilterTransformerElement() {

            @Override
            public String getType() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getScript(boolean loadFiles) throws ScriptBuilderException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FilterTransformerElement clone() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        ftElement.migrate3_7_0(filterDonkey);

        DonkeyElement enabled = filterDonkey.getChildElement("enabled");
        assertTrue(enabled != null && "true".equals(enabled.getTextContent()));
    }
}
