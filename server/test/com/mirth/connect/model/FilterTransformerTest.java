package com.mirth.connect.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

public class FilterTransformerTest {

    /*
     * filter3_6_0.xml contains 3 RuleBuilderRules
     */
    @Test
    public void testMigrate3_6_0FilterTo3_7_0() throws Exception {
        String oldFilterStr = FileUtils.readFileToString(new File("tests/filter3_6_0.xml"));
        DonkeyElement filterDonkey = new DonkeyElement(oldFilterStr);
        Filter filter = new Filter();
        filter.migrate3_7_0(filterDonkey);
        
        DonkeyElement elements = filterDonkey.getChildElement("elements");
        assertEquals(3, elements.getChildElements().size());
        for (DonkeyElement element : elements.getChildElements()) {
            DonkeyElement enabled = element.getChildElement("enabled");
            assertTrue(enabled != null && "true".equals(enabled.getTextContent()));
        }
    }
    
    /*
     * transformerNestedIterators3_6_0.xml contains the following:
     * -> Iterator 1
     *      -> Iterator 2
     *          -> JavaScript Step 1
     *      -> JavaScript Step 2
     *      
     * Verify that all 4 now have an enabled element with value true  
     */
    @Test
    public void testMigrate3_6_0TranformerWithNestedIteratorsTo3_7_0() throws Exception {
        String oldTransformerStr = FileUtils.readFileToString(new File("tests/transformerNestedIterators3_6_0.xml"));
        DonkeyElement transformerDonkey = new DonkeyElement(oldTransformerStr);
        Transformer transformer = new Transformer();
        transformer.migrate3_7_0(transformerDonkey);

        // check Iterator 1 has enabled = true
        DonkeyElement elements = transformerDonkey.getChildElement("elements");
        assertEquals(1, elements.getChildElements().size());
        DonkeyElement iterator1 = elements.getChildElements().get(0);
        DonkeyElement iter1Enabled = iterator1.getChildElement("enabled");
        assertTrue(iter1Enabled != null && "true".equals(iter1Enabled.getTextContent()));
        
        // check Iterator 1's direct children (Iterator 2, JavaScript Step 2) have enable = true
        List<DonkeyElement> iterator1Children = iterator1.getChildElement("properties").getChildElement("children").getChildElements();
        assertEquals(2, iterator1Children.size());
        for (DonkeyElement element : iterator1Children) {
            DonkeyElement enabled = element.getChildElement("enabled");
            assertTrue(enabled != null && "true".equals(enabled.getTextContent()));
        }
        
        
        // check Iterator 2's child (JavaScript Step 1) has enabled = true
        DonkeyElement iterator2 = iterator1Children.get(0);
        if (IteratorStep.class.getName().equals(iterator1Children.get(1).getTagName())) {
            iterator2 = iterator1Children.get(1);
        }
        List<DonkeyElement> iterator2Children = iterator2.getChildElement("properties").getChildElement("children").getChildElements();
        assertEquals(1, iterator2Children.size());
        DonkeyElement javascript2 = iterator2Children.get(0);
        DonkeyElement enabled = javascript2.getChildElement("enabled");
        assertTrue(enabled != null && "true".equals(enabled.getTextContent()));
    }

    @Test
    public void testMigrate3_6_0EmptyFilterTo3_7_0() throws Exception {
        String oldFilterStr = FileUtils.readFileToString(new File("tests/emptyFilter3_6_0.xml"));
        DonkeyElement filterDonkey = new DonkeyElement(oldFilterStr);
        Filter filter = new Filter();
        filter.migrate3_7_0(filterDonkey);
        
        assertEquals(0, filterDonkey.getChildElement("elements").getChildElements().size());
    }
}
