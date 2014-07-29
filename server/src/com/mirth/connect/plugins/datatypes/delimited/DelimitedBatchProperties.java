/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DelimitedBatchProperties extends BatchProperties {

    private transient Logger logger = Logger.getLogger(this.getClass());

    public enum SplitType {
        Record, Delimiter, Grouping_Column, JavaScript;

        @Override
        public String toString() {
            return super.toString().replace('_', ' ');
        }
    };

    private SplitType splitType = SplitType.values()[0];
    private int batchSkipRecords = 0;
    private String batchMessageDelimiter = "";
    private boolean batchMessageDelimiterIncluded = false;
    private String batchGroupingColumn = "";
    private String batchScript = "";

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("splitType", new DataTypePropertyDescriptor(splitType, "Split Batch By", "Select the method for splitting the batch message.  This option has no effect unless Process Batch is enabled in the connector.\n\nRecord: Treat each record as a message.  Records are separated by the record delimiter.\n\nDelimiter: Use the Batch Delimiter to separate messages.\n\nGrouping Column: Use a column to group multiple records into a single message.  When the specified column value changes, this signifies the boundary between messages.\n\nJavaScript: Use JavaScript to split messages.", PropertyEditorType.OPTION, SplitType.values()));
        properties.put("batchSkipRecords", new DataTypePropertyDescriptor(Integer.toString(batchSkipRecords), "Number of Header Records", "The number of header records to skip.  By default, no header records are skipped.  This option has no effect unless Process Batch is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchMessageDelimiter", new DataTypePropertyDescriptor(batchMessageDelimiter, "Batch Delimiter", "The delimiter that separates messages.  The batch delimiter may be a sequence of characters.  This option has no effect unless Process Batch is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchMessageDelimiterIncluded", new DataTypePropertyDescriptor(batchMessageDelimiterIncluded, "Include Batch Delimiter", "Check to include the batch delimiter in the message returned by the batch processer.  By default, batch delimiters are consumed.  This option has no effect unless Process Batch is enabled in the connector.", PropertyEditorType.BOOLEAN));
        properties.put("batchGroupingColumn", new DataTypePropertyDescriptor(batchGroupingColumn, "Grouping Column", "The name of the column used to group multiple records into a single message.  When the specified column value changes, this signifies the boundary between messages.  This option has no effect unless Process Batch is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchScript", new DataTypePropertyDescriptor(batchScript, "JavaScript", "Enter JavaScript that splits the batch, and returns the next message.  This script has access to 'reader', a Java BufferedReader, to read the incoming data stream.  The script must return a string containing the next message, or a null/empty string to indicate end of input.  This option has no effect unless Process Batch is enabled in the connector.", PropertyEditorType.JAVASCRIPT));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("splitType") != null) {
                splitType = (SplitType) properties.get("splitType");
            }

            if (StringUtils.isNotEmpty((String) properties.get("batchSkipRecords"))) {
                // Store an int
                try {
                    batchSkipRecords = Integer.parseInt((String) properties.get("batchSkipRecords"));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid number format in Number of Header Records: " + (String) properties.get("batchSkipRecords"));
                }
            }

            if (properties.get("batchMessageDelimiter") != null) {
                batchMessageDelimiter = (String) properties.get("batchMessageDelimiter");
            }

            if (properties.get("batchMessageDelimiterIncluded") != null) {
                batchMessageDelimiterIncluded = (Boolean) properties.get("batchMessageDelimiterIncluded");
            }

            if (properties.get("batchGroupingColumn") != null) {
                batchGroupingColumn = (String) properties.get("batchGroupingColumn");
            }

            if (properties.get("batchScript") != null) {
                batchScript = (String) properties.get("batchScript");
            }
        }
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    public int getBatchSkipRecords() {
        return batchSkipRecords;
    }

    public void setBatchSkipRecords(int batchSkipRecords) {
        this.batchSkipRecords = batchSkipRecords;
    }

    public String getBatchMessageDelimiter() {
        return batchMessageDelimiter;
    }

    public void setBatchMessageDelimiter(String batchMessageDelimiter) {
        this.batchMessageDelimiter = batchMessageDelimiter;
    }

    public boolean isBatchMessageDelimiterIncluded() {
        return batchMessageDelimiterIncluded;
    }

    public void setBatchMessageDelimiterIncluded(boolean batchMessageDelimiterIncluded) {
        this.batchMessageDelimiterIncluded = batchMessageDelimiterIncluded;
    }

    public String getBatchGroupingColumn() {
        return batchGroupingColumn;
    }

    public void setBatchGroupingColumn(String batchGroupingColumn) {
        this.batchGroupingColumn = batchGroupingColumn;
    }

    public String getBatchScript() {
        return batchScript;
    }

    public void setBatchScript(String batchScript) {
        this.batchScript = batchScript;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        String splitType = "Record";
        if (StringUtils.equalsIgnoreCase(element.getChildElement("batchSplitByRecord").getTextContent(), "false")) {
            if (StringUtils.isNotEmpty(element.getChildElement("batchMessageDelimiter").getTextContent())) {
                splitType = "Delimiter";
            } else if (StringUtils.isNotEmpty(element.getChildElement("batchGroupingColumn").getTextContent())) {
                splitType = "Grouping_Column";
            } else if (StringUtils.isNotEmpty(element.getChildElement("batchScript").getTextContent())) {
                splitType = "JavaScript";
            }
        }

        element.addChildElement("splitType", splitType);
        element.removeChild("batchSplitByRecord");
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("batchSkipRecords", batchSkipRecords);
        purgedProperties.put("batchMessageDelimiterIncluded", batchMessageDelimiterIncluded);
        purgedProperties.put("batchScriptLines", PurgeUtil.countLines(batchScript));
        return purgedProperties;
    }
}
