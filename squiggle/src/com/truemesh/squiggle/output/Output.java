package com.truemesh.squiggle.output;

/**
 * The Output is where the elements of the query output their bits of SQL to.
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public class Output {

    /**
     * @param indent String to be used for indenting (e.g. "", "  ", "       ", "\t")
     */
    public Output(String indent) {
        this.indent = indent;
    }

    private StringBuffer result = new StringBuffer();
    private StringBuffer currentIndent = new StringBuffer();
    private boolean newLineComing;

    private final String indent;

    public String toString() {
        return result.toString();
    }

    public Output print(Object o) {
        writeNewLineIfNeeded();
        result.append(o);
        return this;
    }

    public Output print(char c) {
        writeNewLineIfNeeded();
        result.append(c);
        return this;
    }

    public Output println(Object o) {
        writeNewLineIfNeeded();
        result.append(o);
        newLineComing = true;
        return this;
    }

    public Output println() {
        newLineComing = true;
        return this;
    }

    public void indent() {
        currentIndent.append(indent);
    }

    public void unindent() {
        currentIndent.setLength(currentIndent.length() - indent.length());
    }

    private void writeNewLineIfNeeded() {
        if (newLineComing) {
            result.append('\n').append(currentIndent);
            newLineComing = false;
        }
    }
}
