package com.mirth.connect.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

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
