/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class DelimitedPropertiesBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor
    // The BeanInfo editor resets the property display order to alphabetical based on variable name
    // each time changes are made.  Here is a copy of the desired display order to put it back to 
    // the desired, default display order (not alphabetic).
    /*
    private static final int PROPERTY_columnWidths = 0;
    private static final int PROPERTY_columnDelimiter = 1;
    private static final int PROPERTY_recordDelimiter = 2;
    private static final int PROPERTY_quoteChar = 3;
    private static final int PROPERTY_escapeWithDoubleQuote = 4;
    private static final int PROPERTY_quoteEscapeChar = 5;
    private static final int PROPERTY_columnNames = 6;
    private static final int PROPERTY_numberedRows = 7;
    private static final int PROPERTY_ignoreCR = 8;
    private static final int PROPERTY_batchSkipRecords = 9;
    private static final int PROPERTY_batchSplitByRecord = 10;
    private static final int PROPERTY_batchMessageDelimiter = 11;
    private static final int PROPERTY_batchMessageDelimiterIncluded = 12;
    private static final int PROPERTY_batchGroupingColumn = 13;
    private static final int PROPERTY_batchScript = 14;
     */
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_columnWidths = 0;
    private static final int PROPERTY_columnDelimiter = 1;
    private static final int PROPERTY_recordDelimiter = 2;
    private static final int PROPERTY_quoteChar = 3;
    private static final int PROPERTY_escapeWithDoubleQuote = 4;
    private static final int PROPERTY_quoteEscapeChar = 5;
    private static final int PROPERTY_columnNames = 6;
    private static final int PROPERTY_numberedRows = 7;
    private static final int PROPERTY_ignoreCR = 8;
    private static final int PROPERTY_batchSkipRecords = 9;
    private static final int PROPERTY_batchSplitByRecord = 10;
    private static final int PROPERTY_batchMessageDelimiter = 11;
    private static final int PROPERTY_batchMessageDelimiterIncluded = 12;
    private static final int PROPERTY_batchGroupingColumn = 13;
    private static final int PROPERTY_batchScript = 14;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[15];
    
        try {
            properties[PROPERTY_batchGroupingColumn] = new PropertyDescriptor ( "batchGroupingColumn", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getBatchGroupingColumn", "setBatchGroupingColumn" ); // NOI18N
            properties[PROPERTY_batchGroupingColumn].setDisplayName ( "Split Batch by Grouping Column" );
            properties[PROPERTY_batchGroupingColumn].setShortDescription ( "For batch processing, enter the name of the column used to group multiple records into a single message.  When the specified column value changes, this signifies the boundary between messages.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchGroupingColumn].setBound ( true );
            properties[PROPERTY_batchMessageDelimiter] = new PropertyDescriptor ( "batchMessageDelimiter", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getBatchMessageDelimiter", "setBatchMessageDelimiter" ); // NOI18N
            properties[PROPERTY_batchMessageDelimiter].setDisplayName ( "Split Batch by Delimiter" );
            properties[PROPERTY_batchMessageDelimiter].setShortDescription ( "For batch processing, enter the delimiter that separates messages.  The message delimiter may be a sequence of characters.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchMessageDelimiter].setBound ( true );
            properties[PROPERTY_batchMessageDelimiterIncluded] = new PropertyDescriptor ( "batchMessageDelimiterIncluded", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "isBatchMessageDelimiterIncluded", "setBatchMessageDelimiterIncluded" ); // NOI18N
            properties[PROPERTY_batchMessageDelimiterIncluded].setDisplayName ( "Include Message Delimiter" );
            properties[PROPERTY_batchMessageDelimiterIncluded].setShortDescription ( "Check to include the message delimiter in the message returned by the batch processer.  By default, message delimiters are consumed.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchMessageDelimiterIncluded].setBound ( true );
            properties[PROPERTY_batchScript] = new PropertyDescriptor ( "batchScript", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getBatchScript", "setBatchScript" ); // NOI18N
            properties[PROPERTY_batchScript].setDisplayName ( "Split Batch by Javascript" );
            properties[PROPERTY_batchScript].setShortDescription ( "For batch processing, enter Javascript that splits the batch, and returns the next message.  This script has access to 'reader', a Java BufferedReader, to read the incoming data stream.  The script must return a string containing the next message, or an empty string to indicate end of input.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchScript].setBound ( true );
            properties[PROPERTY_batchScript].setPropertyEditorClass ( com.mirth.connect.client.ui.editors.JavaScriptPropertyEditor.class );
            properties[PROPERTY_batchSkipRecords] = new PropertyDescriptor ( "batchSkipRecords", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getBatchSkipRecords", "setBatchSkipRecords" ); // NOI18N
            properties[PROPERTY_batchSkipRecords].setDisplayName ( "Number of Header Records" );
            properties[PROPERTY_batchSkipRecords].setShortDescription ( "For batch processing, enter the number of header records to skip.  By default, no header records are skipped.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchSkipRecords].setBound ( true );
            properties[PROPERTY_batchSplitByRecord] = new PropertyDescriptor ( "batchSplitByRecord", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "isBatchSplitByRecord", "setBatchSplitByRecord" ); // NOI18N
            properties[PROPERTY_batchSplitByRecord].setDisplayName ( "Split Batch by Record" );
            properties[PROPERTY_batchSplitByRecord].setShortDescription ( "For batch processing, treat each record as a message.  Records are separated by the record delimiter.  This option has no effect unless Process Batch Files is enabled in the connector." );
            properties[PROPERTY_batchSplitByRecord].setBound ( true );
            properties[PROPERTY_columnDelimiter] = new PropertyDescriptor ( "columnDelimiter", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getColumnDelimiter", "setColumnDelimiter" ); // NOI18N
            properties[PROPERTY_columnDelimiter].setDisplayName ( "Column Delimiter" );
            properties[PROPERTY_columnDelimiter].setShortDescription ( "If column values are delimited, enter the column delimiter that separates columns.  For example, this is a comma in a CSV file." );
            properties[PROPERTY_columnDelimiter].setBound ( true );
            properties[PROPERTY_columnNames] = new PropertyDescriptor ( "columnNames", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getColumnNames", "setColumnNames" ); // NOI18N
            properties[PROPERTY_columnNames].setDisplayName ( "Column Names" );
            properties[PROPERTY_columnNames].setShortDescription ( "To override the default column names (column1, ..., columnN), enter a comma separated list of column names." );
            properties[PROPERTY_columnNames].setBound ( true );
            properties[PROPERTY_columnWidths] = new PropertyDescriptor ( "columnWidths", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getColumnWidths", "setColumnWidths" ); // NOI18N
            properties[PROPERTY_columnWidths].setDisplayName ( "Column Widths" );
            properties[PROPERTY_columnWidths].setShortDescription ( "If the column values are fixed width, enter a comma separated list of fixed column widths.  By default, column values are assumed to be delimited." );
            properties[PROPERTY_columnWidths].setBound ( true );
            properties[PROPERTY_escapeWithDoubleQuote] = new PropertyDescriptor ( "escapeWithDoubleQuote", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "isEscapeWithDoubleQuote", "setEscapeWithDoubleQuote" ); // NOI18N
            properties[PROPERTY_escapeWithDoubleQuote].setDisplayName ( "Double Quote Escaping" );
            properties[PROPERTY_escapeWithDoubleQuote].setShortDescription ( "By default, two consecutive quote characters within a quoted value are treated as an embedded quote character.  Uncheck to enable escaped quote character processing (and specify the Escape Character)." );
            properties[PROPERTY_escapeWithDoubleQuote].setBound ( true );
            properties[PROPERTY_ignoreCR] = new PropertyDescriptor ( "ignoreCR", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "isIgnoreCR", "setIgnoreCR" ); // NOI18N
            properties[PROPERTY_ignoreCR].setDisplayName ( "Ignore Carriage Returns" );
            properties[PROPERTY_ignoreCR].setShortDescription ( "Ignores carriage return (\\r) characters.  These are read over and skipped without processing them." );
            properties[PROPERTY_ignoreCR].setBound ( true );
            properties[PROPERTY_numberedRows] = new PropertyDescriptor ( "numberedRows", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "isNumberedRows", "setNumberedRows" ); // NOI18N
            properties[PROPERTY_numberedRows].setDisplayName ( "Numbered Rows" );
            properties[PROPERTY_numberedRows].setShortDescription ( "Check to number each row in the XML representation of the message." );
            properties[PROPERTY_numberedRows].setBound ( true );
            properties[PROPERTY_quoteChar] = new PropertyDescriptor ( "quoteChar", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getQuoteChar", "setQuoteChar" ); // NOI18N
            properties[PROPERTY_quoteChar].setDisplayName ( "Quote Character" );
            properties[PROPERTY_quoteChar].setShortDescription ( "Enter the quote character that is used to bracket delimit column values containing embedded special characters like column delimiters, record delimiters, quote characters and/or message delimiters.    For example, this is a double quote (\") in a CSV file." );
            properties[PROPERTY_quoteChar].setBound ( true );
            properties[PROPERTY_quoteEscapeChar] = new PropertyDescriptor ( "quoteEscapeChar", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getQuoteEscapeChar", "setQuoteEscapeChar" ); // NOI18N
            properties[PROPERTY_quoteEscapeChar].setDisplayName ( "Escape Character" );
            properties[PROPERTY_quoteEscapeChar].setShortDescription ( "Enter the character used to escape embedded quote characters.  By default, this is a back slash.  This option has no effect unless Double Quote Escaping is unchecked." );
            properties[PROPERTY_quoteEscapeChar].setBound ( true );
            properties[PROPERTY_recordDelimiter] = new PropertyDescriptor ( "recordDelimiter", com.mirth.connect.plugins.datatypes.delimited.DelimitedProperties.class, "getRecordDelimiter", "setRecordDelimiter" ); // NOI18N
            properties[PROPERTY_recordDelimiter].setDisplayName ( "Record Delimiter" );
            properties[PROPERTY_recordDelimiter].setShortDescription ( "Enter the character that separates each record (a message may contain multiple records).  For example, this is a newline (\\n) in a CSV file." );
            properties[PROPERTY_recordDelimiter].setBound ( true );
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        return properties;     }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[0];//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;     }//GEN-LAST:Events

    // Method identifiers//GEN-FIRST:Methods

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.

        return methods;     }//GEN-LAST:Methods
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx

//GEN-FIRST:Superclass
    // Here you can add code for customizing the Superclass BeanInfo.
//GEN-LAST:Superclass
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}

