/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DelimitedBatchProperties extends BatchProperties {

    private transient Logger logger = Logger.getLogger(this.getClass());

    private int batchSkipRecords = 0;
    private boolean batchSplitByRecord = true;
    private String batchMessageDelimiter = "";
    private boolean batchMessageDelimiterIncluded = false;
    private String batchGroupingColumn = "";
    private String batchScript = "";

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("batchSkipRecords", new DataTypePropertyDescriptor(Integer.toString(batchSkipRecords), "Number of Header Records", "For batch processing, enter the number of header records to skip.  By default, no header records are skipped.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchSplitByRecord", new DataTypePropertyDescriptor(batchSplitByRecord, "Split Batch by Record", "For batch processing, treat each record as a message.  Records are separated by the record delimiter.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.BOOLEAN));
        properties.put("batchMessageDelimiter", new DataTypePropertyDescriptor(batchMessageDelimiter, "Split Batch by Delimiter", "For batch processing, enter the delimiter that separates messages.  The message delimiter may be a sequence of characters.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchMessageDelimiterIncluded", new DataTypePropertyDescriptor(batchMessageDelimiterIncluded, "Include Message Delimiter", "Check to include the message delimiter in the message returned by the batch processer.  By default, message delimiters are consumed.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.BOOLEAN));
        properties.put("batchGroupingColumn", new DataTypePropertyDescriptor(batchGroupingColumn, "Split Batch by Grouping Column", "For batch processing, enter the name of the column used to group multiple records into a single message.  When the specified column value changes, this signifies the boundary between messages.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.STRING));
        properties.put("batchScript", new DataTypePropertyDescriptor(batchScript, "Split Batch by Javascript", "For batch processing, enter Javascript that splits the batch, and returns the next message.  This script has access to 'reader', a Java BufferedReader, to read the incoming data stream.  The script must return a string containing the next message, or an empty string to indicate end of input.  This option has no effect unless Process Batch Files is enabled in the connector.", PropertyEditorType.JAVASCRIPT));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (StringUtils.isNotEmpty((String) properties.get("batchSkipRecords"))) {
                // Store an int
                try {
                    batchSkipRecords = Integer.parseInt((String) properties.get("batchSkipRecords"));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid number format in Number of Header Records: " + (String) properties.get("batchSkipRecords"));
                }
            }

            int countSetBatchOptions = 0;
            if (properties.get("batchSplitByRecord") != null) {
                batchSplitByRecord = (Boolean) properties.get("batchSplitByRecord");
                if (batchSplitByRecord) {
                    countSetBatchOptions++;
                }
            }

            if (properties.get("batchMessageDelimiter") != null) {
                batchMessageDelimiter = (String) properties.get("batchMessageDelimiter");
                countSetBatchOptions++;
            }

            if (properties.get("batchMessageDelimiterIncluded") != null) {
                batchMessageDelimiterIncluded = (Boolean) properties.get("batchMessageDelimiterIncluded");
            }

            if (properties.get("batchGroupingColumn") != null) {
                batchGroupingColumn = (String) properties.get("batchGroupingColumn");
                countSetBatchOptions++;
            }

            if (properties.get("batchScript") != null) {
                batchScript = (String) properties.get("batchScript");
                countSetBatchOptions++;
            }

            if (countSetBatchOptions > 1) {
                logger.warn("Multiple batch splitting options are set");
            }
        }
    }

    public int getBatchSkipRecords() {
        return batchSkipRecords;
    }

    public void setBatchSkipRecords(int batchSkipRecords) {
        this.batchSkipRecords = batchSkipRecords;
    }

    public boolean isBatchSplitByRecord() {
        return batchSplitByRecord;
    }

    public void setBatchSplitByRecord(boolean batchSplitByRecord) {
        this.batchSplitByRecord = batchSplitByRecord;
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
}
