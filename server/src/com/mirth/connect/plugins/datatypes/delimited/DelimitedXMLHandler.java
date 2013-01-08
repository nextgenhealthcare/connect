/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


public class DelimitedXMLHandler extends DefaultHandler {

	private StringBuilder output = new StringBuilder();
	
	private DelimitedSerializerProperties props;
	private boolean inRow;
	private boolean inColumn;
	private int columnIndex;
	private String columnDelimiter = null;
	private String recordDelimiter = null;
	private String quoteChar = null;
	private String quoteEscapeChar = null;
	private String escapedQuote = null;
	private String escapedQuoteEscape = null;
	private StringBuilder columnValue = null;

	public DelimitedXMLHandler(DelimitedSerializerProperties props) {
		super();
		this.props = props;
	}

	// //////////////////////////////////////////////////////////////////
	// Event handlers.
	// //////////////////////////////////////////////////////////////////

	public void startDocument() {
		inRow = false;
		inColumn = false;
	}

	public void endDocument() {
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {
		
		// If it's a row
		if (!inRow && name.length() >= 3 && name.substring(0, 3).equalsIgnoreCase("row")) {
			inRow = true;
			inColumn = false;
			columnIndex = 0;
		}
		else if (inRow && !inColumn) {

			// It's a column
			inColumn = true;

			// If not fixed width columns
			if (props.getColumnWidths() == null) {

				// If this isn't the first column
				if (columnIndex > 0) {
					output.append(props.getColumnDelimiter().charAt(0));
				}
			}
			
			// Initialize the column value
			columnValue = new StringBuilder();
		}
	}

	public void endElement(String uri, String name, String qName) {
		
		// If in a column
		if (inColumn) {
			
			// If fixed width columns
			if (props.getColumnWidths() != null) {
			
				if (columnIndex < props.getColumnWidths().length) {

					output.append(columnValue);
					
					// Pad with trailing spaces to fixed column width
					int len = props.getColumnWidths()[columnIndex] - columnValue.length();
					while (len > 0) {
						output.append(' ');
						len--;
					}
				}
			}
			else {

				// If the column value contains the column delimiter, or record delimiter
				String temp = columnValue.toString();
				if (temp.contains(getColumnDelimiter()) || temp.contains(getRecordDelimiter())) {
					
					// Escape the escape characters and the quote characters
					temp = temp.replace(getQuoteEscapeChar(), getEscapedQuoteEscape());
					temp = temp.replace(getQuoteChar(), getEscapedQuote());
	
					output.append(getQuoteChar());
					output.append(temp);
					output.append(getQuoteChar());
				}
				else {
					output.append(columnValue);
				}
			}

			inColumn = false;
			columnIndex++;
		} else if (inRow) {
			inRow = false;
			
			// Append the record delimiter
			output.append(props.getRecordDelimiter().charAt(0));
		}
	}

	public void characters(char ch[], int start, int length) {

		if (inColumn) {
			
			if (props.getColumnWidths() != null) {

				if (columnIndex < props.getColumnWidths().length) {

					// Get the fixed width of this column
					int columnWidth = props.getColumnWidths()[columnIndex];
		
					// Truncate if the size of the column value exceeds the fixed column width
					if (columnValue.length() + length > columnWidth) {
						length = columnWidth - columnValue.length();
					}
					
					columnValue.append(ch, start, length);
				}
			}
			else {
				columnValue.append(ch, start, length);
			}
		}
	}

	public StringBuilder getOutput() {
		return output;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

	private String getColumnDelimiter() {
		
		if (columnDelimiter == null) {
			
			if (DelimitedSerializerProperties.isSet(props.getColumnDelimiter())) {
				columnDelimiter = props.getColumnDelimiter().substring(0,1);
			}
			else {
				columnDelimiter = ",";			// default
			}
		}
		
		return columnDelimiter; 
	}

	private String getRecordDelimiter() {
		
		if (recordDelimiter == null) {
			
			if (DelimitedSerializerProperties.isSet(props.getRecordDelimiter())) {
				recordDelimiter = props.getRecordDelimiter().substring(0,1);
			}
			else {
				recordDelimiter = "\n";			// default
			}
		}
		
		return recordDelimiter; 
	}

	private String getQuoteChar() {
		
		if (quoteChar == null) {
			
			if (DelimitedSerializerProperties.isSet(props.getQuoteChar())) {
				quoteChar = props.getQuoteChar().substring(0,1);
			}
			else {
				quoteChar = "\"";			// default
			}
		}
		
		return quoteChar; 
	}

	private String getQuoteEscapeChar() {
		
		if (quoteEscapeChar == null) {
			
			if (DelimitedSerializerProperties.isSet(props.getQuoteEscapeChar())) {
				quoteEscapeChar = props.getQuoteEscapeChar().substring(0,1);
			}
			else {
				quoteEscapeChar = "\\";		// default
			}
		}
		
		return quoteEscapeChar; 
	}

	private String getEscapedQuote() {
		
		if (escapedQuote == null) {
			
			if (props.isEscapeWithDoubleQuote()) {
				escapedQuote = getQuoteChar() + getQuoteChar();
			}
			else {
				escapedQuote = getQuoteEscapeChar() + getQuoteChar();
			}
		}
		
		return escapedQuote; 
	}

	private String getEscapedQuoteEscape() {
		
		if (escapedQuoteEscape == null) {
			escapedQuoteEscape = getQuoteEscapeChar() + getQuoteEscapeChar();
		}
		
		return escapedQuoteEscape; 
	}
}
