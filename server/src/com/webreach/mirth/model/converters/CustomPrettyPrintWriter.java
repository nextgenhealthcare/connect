package com.webreach.mirth.model.converters;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.io.Writer;

/**
 * A simple writer that outputs XML in a pretty-printed indented stream.
 * 
 * <p>
 * By default, the chars <code><xmp>& < > " ' \r</xmp></code> are escaped and
 * replaced with a suitable XML entity. To alter this behavior, override the the
 * {@link #writeText(com.thoughtworks.xstream.core.util.QuickWriter, String)}
 * and
 * {@link #writeAttributeValue(com.thoughtworks.xstream.core.util.QuickWriter, String)}
 * methods.
 * </p>
 * 
 * @author Joe Walnes
 */
public class CustomPrettyPrintWriter implements HierarchicalStreamWriter {

	private final QuickWriter writer;
	private final FastStack elementStack = new FastStack(16);
	private final char[] lineIndenter;

	private boolean tagInProgress;
	private int depth;
	private boolean readyForNewLine;
	private boolean tagIsEmpty;

	private static final char[] NULL = "&#x0;".toCharArray();
	private static final char[] AMP = "&amp;".toCharArray();
	private static final char[] LT = "&lt;".toCharArray();
	private static final char[] GT = "&gt;".toCharArray();
	private static final char[] QUOT = "&quot;".toCharArray();
	private static final char[] APOS = "&apos;".toCharArray();
	private static final char[] CLOSE = "</".toCharArray();

	public CustomPrettyPrintWriter(Writer writer, char[] lineIndenter) {
		this.writer = new QuickWriter(writer);
		this.lineIndenter = lineIndenter;
	}

	public CustomPrettyPrintWriter(Writer writer, String lineIndenter) {
		this(writer, lineIndenter.toCharArray());
	}

	public CustomPrettyPrintWriter(Writer writer) {
		this(writer, new char[] { ' ', ' ' });
	}

	public void startNode(String name) {
		tagIsEmpty = false;
		finishTag();
		writer.write('<');
		writer.write(name);
		elementStack.push(name);
		tagInProgress = true;
		depth++;
		readyForNewLine = true;
		tagIsEmpty = true;
	}

	public void setValue(String text) {
		readyForNewLine = false;
		tagIsEmpty = false;
		finishTag();

		writeText(writer, text);
	}

	public void addAttribute(String key, String value) {
		writer.write(' ');
		writer.write(key);
		writer.write('=');
		writer.write('\"');
		writeAttributeValue(writer, value);
		writer.write('\"');
	}

	protected void writeAttributeValue(QuickWriter writer, String text) {
		writeText(text);
	}

	protected void writeText(QuickWriter writer, String text) {
		writeText(text);
	}

	private void writeText(String text) {
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char c = text.charAt(i);
			switch (c) {
				case '\0':
					this.writer.write(NULL);
					break;
				case '&':
					this.writer.write(AMP);
					break;
				case '<':
					this.writer.write(LT);
					break;
				case '>':
					this.writer.write(GT);
					break;
				case '"':
					this.writer.write(QUOT);
					break;
				case '\'':
					this.writer.write(APOS);
					break;
				default:
					this.writer.write(c);
			}
		}
	}

	public void endNode() {
		depth--;
		if (tagIsEmpty) {
			writer.write('/');
			readyForNewLine = false;
			finishTag();
			elementStack.popSilently();
		} else {
			finishTag();
			writer.write(CLOSE);
			writer.write((String) elementStack.pop());
			writer.write('>');
		}
		readyForNewLine = true;
		if (depth == 0) {
			writer.flush();
		}
	}

	private void finishTag() {
		if (tagInProgress) {
			writer.write('>');
		}
		tagInProgress = false;
		if (readyForNewLine) {
			endOfLine();
		}
		readyForNewLine = false;
		tagIsEmpty = false;
	}

	protected void endOfLine() {
		writer.write('\n');
		for (int i = 0; i < depth; i++) {
			writer.write(lineIndenter);
		}
	}

	public void flush() {
		writer.flush();
	}

	public void close() {
		writer.close();
	}

	public HierarchicalStreamWriter underlyingWriter() {
		return this;
	}
}
