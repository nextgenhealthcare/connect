/*
 * 01/28/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.fife.rsta.ac.js;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.Element;

import org.fife.io.DocumentReader;
import org.fife.rsta.ac.js.ast.VariableResolver;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ErrorCollector;
import org.mozilla.javascript.ast.ParseProblem;


/**
 * Parses JavaScript code in an <code>RSyntaxTextArea</code>.
 * <p>
 * 
 * Like all RSTA <tt>Parser</tt>s, a <tt>JavaScriptParser</tt> instance is
 * notified when the RSTA's text content changes. After a small delay, it will
 * parse the content as JS code, building an AST and looking for any errors.
 * When parsing is complete, a property change event of type
 * {@link #PROPERTY_AST} is fired. Listeners can check the new value of the
 * property for the <code>AstRoot</code> built that represents the source code
 * in the text area.
 * <p>
 * 
 * This parser cannot be shared amongst multiple instances of
 * <code>RSyntaxTextArea</code>.
 * <p>
 * 
 * Please keep in mind that this class is a work-in-progress!
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class JavaScriptParser extends AbstractParser {

	/**
	 * The property change event that's fired when the document is re-parsed.
	 * Applications can listen for this property change and update themselves
	 * accordingly.  The "new" value of this property will be an instance of
	 * <code>org.mozilla.javascript.ast.AstRoot</code>.
	 */
	public static final String PROPERTY_AST = "AST";
	
	private AstRoot astRoot;
	private JavaScriptLanguageSupport langSupport;
	private PropertyChangeSupport support;
	private DefaultParseResult result;
	private VariableResolver variableResolver;

	/**
	 * Constructor.
	 */
	public JavaScriptParser(JavaScriptLanguageSupport langSupport,
			RSyntaxTextArea textArea) {
		this.langSupport = langSupport;
		support = new PropertyChangeSupport(this);
		result = new DefaultParseResult(this);
	}


	/**
	 * Registers a property change listener on this parser.  You'll probably
	 * want to listen for changes to {@link #PROPERTY_AST}.
	 *
	 * @param prop The property to listen for changes in.
	 * @param l The listener to add.
	 * @see #removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
		support.addPropertyChangeListener(prop, l);
	}


	/**
	 * Creates options for Rhino based off of the user's preferences.
	 *
	 * @param errorHandler The container for errors found while parsing.
	 * @return The properties for the JS compiler to use.
	 */
	public static CompilerEnvirons createCompilerEnvironment(ErrorReporter
			errorHandler, JavaScriptLanguageSupport langSupport) {
		CompilerEnvirons env = new CompilerEnvirons();
		env.setErrorReporter(errorHandler);
		env.setIdeMode(true);
		env.setRecordingComments(true);
		env.setRecordingLocalJsDocComments(true);
		env.setRecoverFromErrors(true);
		if(langSupport != null) {
			env.setXmlAvailable(langSupport.isXmlAvailable());
			env.setStrictMode(langSupport.isStrictMode());
			int version = langSupport.getLanguageVersion();
			if (version > 0) {
				Logger.log("[JavaScriptParser]: JS language version set to: " + version);
				env.setLanguageVersion(version);
			}
		}
		return env;
	}


	/**
	 * Launches jshint as an external process, and gathers syntax errors from
	 * it.
	 *
	 * @param doc the document to parse.
	 * @see #gatherParserErrorsRhino(ErrorCollector, Element)
	 */
	private void gatherParserErrorsJsHint(RSyntaxDocument doc) {

		try {
			JsHinter.parse(this, doc, result);
		} catch (IOException ioe) {
			// TODO: Localize me?
			String msg = "Error launching jshint: " + ioe.getMessage();
			result.addNotice(new DefaultParserNotice(this, msg, 0));
			ioe.printStackTrace();
		}
	}


	/**
	 * Gathers the syntax errors found by Rhino in-process when parsing the
	 * document.
	 *
	 * @param errorHandler The errors found by Rhino.
	 * @param root The root element of the document parsed.
	 * @see #gatherParserErrorsJsHint(RSyntaxDocument)
	 */
	private void gatherParserErrorsRhino(ErrorCollector errorHandler,
			Element root) {

		List<ParseProblem> errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {

			for (ParseProblem problem : errors) {

				int offs = problem.getFileOffset();
				int len = problem.getLength();
				int line = root.getElementIndex(offs);
				String desc = problem.getMessage();
				DefaultParserNotice notice = new DefaultParserNotice(this,
						desc, line, offs, len);
				if (problem.getType() == ParseProblem.Type.Warning) {
					notice.setLevel(ParserNotice.Level.WARNING);
				}
				result.addNotice(notice);

			}

		}

	}


	/**
	 * Returns the AST, or <code>null</code> if the editor's content has not
	 * yet been parsed.
	 * 
	 * @return The AST, or <code>null</code>.
	 */
	public AstRoot getAstRoot() {
		return astRoot;
	}


	public int getJsHintIndent() {
		return langSupport.getJsHintIndent();
	}


	/**
	 * Returns the location of the <code>.jshintrc</code> file to use if using
	 * JsHint as your error parser.  This property is ignored if
	 * {@link #getErrorParser()} does not return {@link JsErrorParser#JSHINT}.
	 *
	 * @return The <code>.jshintrc</code> file, or <code>null</code> if none;
	 *         in that case, the JsHint defaults will be used.
	 * @see #setJsHintRCFile(File)
	 * @see #setErrorParser(JsErrorParser)
	 */
	public File getJsHintRCFile() {
		return langSupport.getJsHintRCFile();
	}


	/**
	 * {@inheritDoc}
	 */
	public ParseResult parse(RSyntaxDocument doc, String style) {

		astRoot = null;
		result.clearNotices();
		// Always spell check all lines, for now.
		Element root = doc.getDefaultRootElement();
		int lineCount = root.getElementCount();
		result.setParsedLines(0, lineCount - 1);

		DocumentReader r = new DocumentReader(doc);
		ErrorCollector errorHandler = new ErrorCollector();
		CompilerEnvirons env = createCompilerEnvironment(errorHandler, langSupport);
		long start = System.currentTimeMillis();
		try {
			Parser parser = new Parser(env);
			astRoot = parser.parse(r, null, 0);
			long time = System.currentTimeMillis() - start;
			result.setParseTime(time);
		} catch (IOException ioe) { // Never happens
			result.setError(ioe);
			ioe.printStackTrace();
		} catch (RhinoException re) {
			// Shouldn't happen since we're passing an ErrorCollector in
			int line = re.lineNumber();
			// if (line>0) {
			Element elem = root.getElement(line);
			int offs = elem.getStartOffset();
			int len = elem.getEndOffset() - offs - 1;
			String msg = re.details();
			result.addNotice(new DefaultParserNotice(this, msg, line, offs, len));
			// }
		} catch (Exception e) {
			result.setError(e); // catch all
		}

		r.close();

		// Get any parser errors.
		switch (langSupport.getErrorParser()) {
			default:
			case RHINO:
				gatherParserErrorsRhino(errorHandler, root);
				break;
			case JSHINT:
				gatherParserErrorsJsHint(doc);
				break;
		}

		// addNotices(doc);
		support.firePropertyChange(PROPERTY_AST, null, astRoot);

		return result;

	}
	
	public void setVariablesAndFunctions(VariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}
	
	public VariableResolver getVariablesAndFunctions() {
		return variableResolver;
	}


	/**
	 * Removes a property change listener from this parser.
	 *
	 * @param prop The property that was being listened to.
	 * @param l The listener to remove.
	 * @see #addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
		support.removePropertyChangeListener(prop, l);
	}


	/**
	 * Error reporter for Rhino-based parsing.
	 */
	public static class JSErrorReporter implements ErrorReporter {

		public void error(String message, String sourceName, int line,
				String lineSource, int lineOffset) {
		}

		public EvaluatorException runtimeError(String message,
				String sourceName, int line, String lineSource,
				int lineOffset) {
			return null;
		}

		public void warning(String message, String sourceName, int line,
				String lineSource, int lineOffset) {

		}
	}

}