/*
 * JEditTextArea.java - jEdit's text component
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.syntax.jedit.tokenmarker.Token;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.client.ui.editors.LineNumber;

/**
 * jEdit's text area component. It is more suited for editing program source
 * code than JEditorPane, because it drops the unnecessary features (images,
 * variable-width lines, and so on) and adds a whole bunch of useful goodies
 * such as:
 * <ul>
 * <li>More flexible key binding scheme
 * <li>Supports macro recorders
 * <li>Rectangular selection
 * <li>Bracket highlighting
 * <li>Syntax highlighting
 * <li>Command repetition
 * <li>Block caret can be enabled
 * </ul>
 * It is also faster and doesn't have as many problems. It can be used in other
 * applications; the only other part of jEdit it depends on is the syntax
 * package.
 * <p>
 * 
 * To use it in your app, treat it like any other component, for example:
 * 
 * <pre>
 * JEditTextArea ta = new JEditTextArea();
 * ta.setTokenMarker(new JavaTokenMarker());
 * ta.setText(&quot;public class Test {\n&quot; + &quot;    public static void main(String[] args) {\n&quot; + &quot;        System.out.println(\&quot;Hello World\&quot;);\n&quot; + &quot;    }\n&quot; + &quot;}&quot;);
 * </pre>
 * 
 * @author Slava Pestov
 * @version $Id: JEditTextArea.java,v 1.36 1999/12/13 03:40:30 sp Exp $
 */
public class JEditTextArea extends JComponent {
	private static final String MAC_OS = "Mac OS";

	/**
	 * Adding components with this name to the text area will place them left of
	 * the horizontal scroll bar. In jEdit, the status bar is added this way.
	 */
	public static String LEFT_OF_SCROLLBAR = "los";

	private int longestLine = 0;

	public int longestLineSize = 1;

	private boolean enableLineNumbers = false;
	
	private boolean showLineEndings = false;

	/**
	 * Creates a new JEditTextArea with the default settings.
	 */
	public JEditTextArea() {
		this(false);
	}

	public void setBackground(Color color) {
		painter.setBackground(color);
	}

	/**
	 * Creates a new JEditTextArea with the specified settings.
	 * 
	 * @param defaults
	 *            The default settings
	 */
	public JEditTextArea(boolean enableLineNumbers) {
		// Enable the necessary events
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		this.enableLineNumbers = enableLineNumbers;
		TextAreaDefaults defaults = new TextAreaDefaults();
		// Initialize some misc. stuff
		Font font = defaults.font;// new Font("Courier",Font.PLAIN,12);
		painter = new TextAreaPainter(this, defaults, font);
		documentHandler = new DocumentHandler();
		listenerList = new EventListenerList();
		caretEvent = new MutableCaretEvent();
		lineSegment = new Segment();
		bracketLine = bracketPosition = -1;
		blink = true;

		// Initialize the GUI
		setLayout(new ScrollLayout());
		if (enableLineNumbers) {
			left = new LineNumber(this);
			left.setFont(font);
			left.setLineHeight(painter.getFontMetrics().getHeight());
		}
		painter.addMouseWheelListener(new ScrollWheelHandler());
		add(CENTER, painter);
		add(RIGHT, vertical = new JScrollBar(JScrollBar.VERTICAL));
		add(BOTTOM, horizontal = new JScrollBar(JScrollBar.HORIZONTAL));

		if (enableLineNumbers)
			add(LEFT, left);
		// add(LEFT_OF_SCROLLBAR, new LineNumber(this));
		// Add some event listeners
		vertical.addAdjustmentListener(new AdjustHandler());
		horizontal.addAdjustmentListener(new AdjustHandler());
		vertical.setEnabled(false);
		horizontal.setEnabled(false);
		// left.

		painter.addComponentListener(new ComponentHandler());
		painter.addMouseListener(new MouseHandler());
		painter.addMouseMotionListener(new DragHandler());
		addFocusListener(new FocusHandler());

		// getDocument().addDocumentListener(new MyDocumentListener());

		// Load the defaults
		setInputHandler(defaults.inputHandler);
		setDocument(defaults.document);

		DropTarget gt = new DropTarget();
		try {
			gt.addDropTargetListener(new DropTargetListener() {

				public void dragEnter(DropTargetDragEvent dtde) {

					requestFocus();

					// Focus events not fired sometimes?
					setCaretVisible(true);
					focusedComponent = JEditTextArea.this;

				}

				public void dragExit(DropTargetEvent dte) {

				}

				public void dragOver(DropTargetDragEvent dtde) {
					int x = getAdjustedX(dtde.getLocation().x);
					int offset = xyToOffset(x, dtde.getLocation().y);
					setCaretPosition(offset);

				}

				private int getAdjustedX(int x) {
					if (left != null) {
						x = x - left.getWidth();
						return x;
					} else {
						return x;
					}
				}

				public void drop(DropTargetDropEvent dtde) {

					int line = yToLine(dtde.getLocation().y);
					int x = getAdjustedX(dtde.getLocation().x);
					int offset = xToOffset(line, x);

					Transferable transferable = dtde.getTransferable();
					String data = "";
					try {
						data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
					} catch (UnsupportedFlavorException e) {
						// e.printStackTrace();
					} catch (IOException e) {
						// e.printStackTrace();
					} catch (Exception e) {

					}

					setSelectedText(data);
				}

				public void dropActionChanged(DropTargetDragEvent dtde) {}
			});
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}

		gt.setFlavorMap(new FlavorMap() {

			public Map<String, DataFlavor> getFlavorsForNatives(String[] natives) {
				return null;
			}

			public Map<DataFlavor, String> getNativesForFlavors(DataFlavor[] flavors) {
				return null;
			}
		});
		setDropTarget(gt);

		editable = defaults.editable;
		caretVisible = defaults.caretVisible;
		caretBlinks = defaults.caretBlinks;
		electricScroll = defaults.electricScroll;

		popup = defaults.popup;

		// We don't seem to get the initial focus event?
		focusedComponent = this;
	}

	/**
	 * Returns if this component can be traversed by pressing the Tab key. This
	 * returns false.
	 */
	public final boolean isManagingFocus() {
		return true;
	}

	/**
	 * Returns the object responsible for painting this text area.
	 */
	public final TextAreaPainter getPainter() {
		return painter;
	}

	/**
	 * Returns the input handler.
	 */
	public final InputHandler getInputHandler() {
		return inputHandler;
	}

	/**
	 * Sets the input handler.
	 * 
	 * @param inputHandler
	 *            The new input handler
	 */
	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

	/**
	 * Returns true if the caret is blinking, false otherwise.
	 */
	public final boolean isCaretBlinkEnabled() {
		return caretBlinks;
	}

	/**
	 * Toggles caret blinking.
	 * 
	 * @param caretBlinks
	 *            True if the caret should blink, false otherwise
	 */
	public void setCaretBlinkEnabled(boolean caretBlinks) {
		this.caretBlinks = caretBlinks;
		if (!caretBlinks)
			blink = false;

		painter.invalidateSelectedLines();
	}

	/**
	 * Returns true if the caret is visible, false otherwise.
	 */
	public final boolean isCaretVisible() {
		return (!caretBlinks || blink) && caretVisible;
	}

	/**
	 * Sets if the caret should be visible.
	 * 
	 * @param caretVisible
	 *            True if the caret should be visible, false otherwise
	 */
	public void setCaretVisible(boolean caretVisible) {
		this.caretVisible = caretVisible;
		blink = true;

		painter.invalidateSelectedLines();
	}

	/**
	 * Blinks the caret.
	 */
	public final void blinkCaret() {
		if (caretBlinks) {
			blink = !blink;
			painter.invalidateSelectedLines();
		} else
			blink = true;
	}

	/**
	 * Returns the number of lines from the top and button of the text area that
	 * are always visible.
	 */
	public final int getElectricScroll() {
		return electricScroll;
	}

	/**
	 * Sets the number of lines from the top and bottom of the text area that
	 * are always visible
	 * 
	 * @param electricScroll
	 *            The number of lines always visible from the top or bottom
	 */
	public final void setElectricScroll(int electricScroll) {
		this.electricScroll = electricScroll;
	}

	/**
	 * Updates the state of the scroll bars. This should be called if the number
	 * of lines in the document changes, or when the size of the text are
	 * changes.
	 */
	public void updateScrollBars() {
		// left.setBounds(left.getLocation().x,firstLine *
		// -painter.getFontMetrics().getHeight(),left.getWidth(),left.getHeight()
		// + firstLine * painter.getFontMetrics().getHeight());
		vertical.setEnabled(false);
		if (vertical != null && visibleLines != 0) {

			if (getLineCount() <= visibleLines) {
				// scrollTo(0, 0);
				vertical.setEnabled(false);

			} else {
				vertical.setEnabled(true);
			}
			vertical.setValues(firstLine, visibleLines, 0, getLineCount());
			// / left.setLocation(0, );
			vertical.setUnitIncrement(2);
			vertical.setBlockIncrement(visibleLines);
		}

		int width = painter.getWidth() - vertical.getWidth();
		// System.out.println("Width: " + width);
		// System.out.println("HorizontalOffset: " + horizontalOffset);
		// System.out.println("Longestline Size: " + longestLineSize);
		if (horizontalOffset > 0) {
			horizontalOffset = 0;
		}
		if (horizontal != null && width != 0) {
			int max_size = 0;
			if (horizontalOffset >= 0 && longestLineSize + horizontalOffset <= width + vertical.getWidth()) {
				horizontal.setEnabled(false);
				max_size = width;
			} else {
				horizontal.setEnabled(true);
				max_size = longestLineSize;
			}

			horizontal.setValues(-horizontalOffset, width + vertical.getWidth(), 0, max_size);
			horizontal.setUnitIncrement(painter.getFontMetrics().charWidth('w'));
			horizontal.setBlockIncrement(width / 2);
		} else {
			horizontal.setEnabled(false);
		}
	}

	/**
	 * Returns the line displayed at the text area's origin.
	 */
	public final int getFirstLine() {
		return firstLine;
	}

	/**
	 * Sets the line displayed at the text area's origin without updating the
	 * scroll bars.
	 */
	public void setFirstLine(int firstLine) {
		if (firstLine < 0) {
			// Fix for line starting in the middle of the document
			firstLine = 0;
		}
		if (firstLine == this.firstLine)
			return;
		int oldFirstLine = this.firstLine;
		this.firstLine = firstLine;
		if (firstLine != vertical.getValue())
			updateScrollBars();

		painter.repaint();

	}

	/**
	 * Returns the number of lines visible in this text area.
	 */
	public final int getVisibleLines() {
		return visibleLines;
	}

	/**
	 * Recalculates the number of visible lines. This should not be called
	 * directly.
	 */
	public final void recalculateVisibleLines() {
		if (painter == null)
			return;
		int height = painter.getHeight();
		int lineHeight = painter.getFontMetrics().getHeight();
		int oldVisibleLines = visibleLines;
		visibleLines = height / lineHeight;
		updateScrollBars();
	}

	/**
	 * Returns the horizontal offset of drawn lines.
	 */
	public final int getHorizontalOffset() {
		return horizontalOffset;
	}

	/**
	 * Sets the horizontal offset of drawn lines. This can be used to implement
	 * horizontal scrolling.
	 * 
	 * @param horizontalOffset
	 *            offset The new horizontal offset
	 */
	public void setHorizontalOffset(int horizontalOffset) {
		if (horizontalOffset == this.horizontalOffset)
			return;
		this.horizontalOffset = horizontalOffset;
		if (horizontalOffset != horizontal.getValue())
			updateScrollBars();
		painter.repaint();
	}

	/**
	 * A fast way of changing both the first line and horizontal offset.
	 * 
	 * @param firstLine
	 *            The new first line
	 * @param horizontalOffset
	 *            The new horizontal offset
	 * @return True if any of the values were changed, false otherwise
	 */
	public boolean setOrigin(int firstLine, int horizontalOffset) {
		boolean changed = false;
		int oldFirstLine = this.firstLine;

		if (horizontalOffset != this.horizontalOffset) {
			this.horizontalOffset = horizontalOffset;
			changed = true;
		}

		if (firstLine != this.firstLine) {
			this.firstLine = firstLine;
			changed = true;
		}

		if (changed) {
			updateScrollBars();
			painter.repaint();
		}

		return changed;
	}

	/**
	 * Ensures that the caret is visible by scrolling the text area if
	 * necessary.
	 * 
	 * @return True if scrolling was actually performed, false if the caret was
	 *         already visible
	 */
	public boolean scrollToCaret() {
		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		int offset = Math.max(0, Math.min(getLineLength(line) - 1, getCaretPosition() - lineStart));
		return scrollTo(line, offset);
	}

	/**
	 * Ensures that the specified line and offset is visible by scrolling the
	 * text area if necessary.
	 * 
	 * @param line
	 *            The line to scroll to
	 * @param offset
	 *            The offset in the line to scroll to
	 * @return True if scrolling was actually performed, false if the line and
	 *         offset was already visible
	 */
	public boolean scrollTo(int line, int offset) {

		// visibleLines == 0 before the component is realized
		// we can't do any proper scrolling then, so we have
		// this hack...
		if (visibleLines == 0) {
			setFirstLine(Math.max(0, line - electricScroll));
			return true;
		}

		int newFirstLine = firstLine;
		int newHorizontalOffset = horizontalOffset;

		if (line < firstLine + electricScroll) {
			newFirstLine = Math.max(0, line - electricScroll);
		} else if (line + electricScroll >= firstLine + visibleLines) {
			newFirstLine = (line - visibleLines) + electricScroll + 1;
			if (newFirstLine + visibleLines >= getLineCount())
				newFirstLine = getLineCount() - visibleLines;
			if (newFirstLine < 0)
				newFirstLine = 0;
		}
		if (offset >= 0) {
			// x is the location we currently have the caret
			int x = _offsetToX(line, offset) + painter.getFontMetrics().charWidth('w');
			x = _offsetToX(line, offset) + painter.getFontMetrics().charWidth('w');
			int width = painter.getFontMetrics().charWidth('w');
			//System.out.println("X: " + x + " " + "Width: " + width + " Offset: " + offset);
			if (x < painter.getFontMetrics().charWidth('w')) {
				// If we are scrolled over to the right and the carat is being
				// moved
				// towards the left side of the component edge
				// we will get an x value less than zero
				// in theory we should NEVER get here (see third if clause)
				// TODO: figure out why we sometimes get here.
				//if (horizontalOffset <= 0) {
					newHorizontalOffset = horizontalOffset - x + painter.getFontMetrics().charWidth('w');
				//} else {
					// 80, eh?
					//System.out.println("Moving to the left: " + newHorizontalOffset);
				//	newHorizontalOffset = horizontalOffset + x;// + 80;
				//}
			} else if (x + width >= painter.getWidth()) {
				// if we are typing (or scrolling) and we reached the right
				// edger
				// of the component, we need to move the contents over a bit
				newHorizontalOffset = horizontalOffset + (painter.getWidth() - (x + width - 4));
			} else if (horizontalOffset < 0) {
				// if we are scrolled over we need to see if we are on the
				// longest line or not
				updateLongestLine();
				if (line == longestLine) {
					// if we are on the longest line, then we check if the
					// horizontal offset
					// needs to be updated (our line length > width)
					int longestLineLen = painter.getFontMetrics().charsWidth(getLineText(line).toCharArray(), 0, getLineText(line).length());
					// check if we are at the end of the line
					if (-(horizontalOffset - painter.getWidth()) >= longestLineLen) {
						// we start to reduce the horizonal offset. This
						// essentially
						// moves the view area of the component
						// so that the end of the longest line is at the very
						// right edge
						//System.out.println("Moving over..." + longestLineLen);
						//newHorizontalOffset = (Math.min(0, -(longestLineLen)) + painter.getWidth() - width);
					}
				}
			}
		} else {
			newHorizontalOffset = horizontalOffset;
		}
		return setOrigin(newFirstLine, newHorizontalOffset);
	}

	/**
	 * Converts a line index to a y co-ordinate.
	 * 
	 * @param line
	 *            The line
	 */
	public int lineToY(int line) {
		FontMetrics fm = painter.getFontMetrics();
		return (line - firstLine) * fm.getHeight() - (fm.getLeading() + fm.getMaxDescent());
	}

	/**
	 * Converts a y co-ordinate to a line index.
	 * 
	 * @param y
	 *            The y co-ordinate
	 */
	public int yToLine(int y) {
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		return Math.max(0, Math.min(getLineCount() - 1, y / height + firstLine));
	}

	/**
	 * Converts an offset in a line into an x co-ordinate. This is a slow
	 * version that can be used any time.
	 * 
	 * @param line
	 *            The line
	 * @param offset
	 *            The offset, from the start of the line
	 */
	public final int offsetToX(int line, int offset) {
		// don't use cached tokens
		painter.currentLineTokens = null;
		return _offsetToX(line, offset);
	}

	/**
	 * Converts an offset in a line into an x co-ordinate. This is a fast
	 * version that should only be used if no changes were made to the text
	 * since the last repaint.
	 * 
	 * @param line
	 *            The line
	 * @param offset
	 *            The offset, from the start of the line
	 */
	public int _offsetToX(int line, int offset) {
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line, lineSegment);

		int segmentOffset = lineSegment.offset;
		int x = horizontalOffset;
		/* If syntax coloring is disabled, do simple translation */
		if (tokenMarker == null) {
			lineSegment.count = offset;
			return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
		}
		/*
		 * If syntax coloring is enabled, we have to do this because tokens can
		 * vary in width
		 */
		else {
			Token tokens;
			if (painter.currentLineIndex == line && painter.currentLineTokens != null)
				tokens = painter.currentLineTokens;
			else {
				painter.currentLineIndex = line;
				tokens = painter.currentLineTokens = tokenMarker.markTokens(lineSegment, line);
			}

			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

            /*
             * MIRTH-2000: For some reason certain monospaced fonts display
             * invalid control characters as a space with a smaller width than a
             * standard space. Since we are changing the character that is
             * rendered for these invalid control characters, this width
             * difference must be used to adjust the cursor position.
             */
            int invalidCharWidthDiff = fm.charWidth('w') - fm.charWidth('\u0001');

			for (;;) {
				byte id = tokens.id;
				if (id == Token.END) {
					return x;
				}

				if (id == Token.NULL)
					fm = painter.getFontMetrics();
				else
					fm = styles[id].getFontMetrics(defaultFont);

				int length = tokens.length;

				if (offset + segmentOffset < lineSegment.offset + length) {
					lineSegment.count = offset - (lineSegment.offset - segmentOffset);
					
					// MIRTH: 2000 - See comment above
					int padding = 0;
					if (invalidCharWidthDiff != 0) {
    					for (int i = lineSegment.getBeginIndex(); i < lineSegment.getEndIndex(); i++) {
    					    if (SyntaxUtilities.invalidCharSet.contains(lineSegment.array[i])) {
    					        padding += invalidCharWidthDiff;
    					    }
    					}
					}
					
					return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0) + padding;
				} else {
					lineSegment.count = length;
					
					// MIRTH: 2000 - See comment above
					int padding = 0;
					if (invalidCharWidthDiff != 0) {
                        for (int i = lineSegment.getBeginIndex(); i < lineSegment.getEndIndex(); i++) {
                            if (SyntaxUtilities.invalidCharSet.contains(lineSegment.array[i])) {
                                padding += invalidCharWidthDiff;
                            }
                        }
					}
                    
					x += Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0) + padding;
					lineSegment.offset += length;
				}
				tokens = tokens.next;
			}
		}
	}

	/**
	 * Converts an x co-ordinate to an offset within a line.
	 * 
	 * @param line
	 *            The line
	 * @param x
	 *            The x co-ordinate
	 */
	public int xToOffset(int line, int x) {
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line, lineSegment);

		char[] segmentArray = lineSegment.array;
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		int width = horizontalOffset;

		if (tokenMarker == null) {
			for (int i = 0; i < segmentCount; i++) {
				char c = segmentArray[i + segmentOffset];
				int charWidth;
				if (c == '\t')
					charWidth = (int) painter.nextTabStop(width, i) - width;
				else
//					charWidth = fm.charWidth(c);
				    charWidth = fm.charWidth('w');

				if (painter.isBlockCaretEnabled()) {
					if (x - charWidth <= width)
						return i;
				} else {
					if (x - charWidth / 2 <= width)
						return i;
				}

				width += charWidth;
			}

			return segmentCount;
		} else {
			Token tokens;
			if (painter.currentLineIndex == line && painter.currentLineTokens != null)
				tokens = painter.currentLineTokens;
			else {
				painter.currentLineIndex = line;
				tokens = painter.currentLineTokens = tokenMarker.markTokens(lineSegment, line);
			}

			int offset = 0;
			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			for (;;) {
				byte id = tokens.id;
				if (id == Token.END)
					return offset;

				if (id == Token.NULL)
					fm = painter.getFontMetrics();
				else
					fm = styles[id].getFontMetrics(defaultFont);

				int length = tokens.length;

				for (int i = 0; i < length; i++) {
					char c = segmentArray[segmentOffset + offset + i];
					int charWidth;
					if (c == '\t')
						charWidth = (int) painter.nextTabStop(width, offset + i) - width;
					else
						charWidth = fm.charWidth(c);

					if (painter.isBlockCaretEnabled()) {
						if (x - charWidth <= width)
							return offset + i;
					} else {
						if (x - charWidth / 2 <= width)
							return offset + i;
					}

					width += charWidth;
				}

				offset += length;
				tokens = tokens.next;
			}
		}
	}

	/**
	 * Converts a point to an offset, from the start of the text.
	 * 
	 * @param x
	 *            The x co-ordinate of the point
	 * @param y
	 *            The y co-ordinate of the point
	 */
	public int xyToOffset(int x, int y) {
		int line = yToLine(y);
		int start = getLineStartOffset(line);
		return start + xToOffset(line, x);
	}

	/**
	 * Returns the document this text area is editing.
	 */
	public final SyntaxDocument getDocument() {
		return document;
	}

	/**
	 * Sets the document this text area is editing.
	 * 
	 * @param document
	 *            The document
	 */
	public void setDocument(SyntaxDocument document) {
		if (this.document == document)
			return;
		if (this.document != null)
			this.document.removeDocumentListener(documentHandler);
		this.document = document;
		this.document.putProperty("filterNewlines", false);
		document.addDocumentListener(documentHandler);
		document.addUndoableEditListener(new MyUndoableEditListener());
		select(0, 0);
		updateScrollBars();
		painter.repaint();
	}

	/**
	 * Returns the document's token marker. Equivalent to calling
	 * <code>getDocument().getTokenMarker()</code>.
	 */
	public final TokenMarker getTokenMarker() {
		return document.getTokenMarker();
	}

	/**
	 * Sets the document's token marker. Equivalent to caling
	 * <code>getDocument().setTokenMarker()</code>.
	 * 
	 * @param tokenMarker
	 *            The token marker
	 */
	public final void setTokenMarker(TokenMarker tokenMarker) {
		document.setTokenMarker(tokenMarker);
	}

	/**
	 * Returns the length of the document. Equivalent to calling
	 * <code>getDocument().getLength()</code>.
	 */
	public final int getDocumentLength() {
		return document.getLength();
	}

	/**
	 * Returns the number of lines in the document.
	 */
	public final int getLineCount() {
		return document.getDefaultRootElement().getElementCount();
	}

	/**
	 * Returns the line containing the specified offset.
	 * 
	 * @param offset
	 *            The offset
	 */
	public final int getLineOfOffset(int offset) {
		return document.getDefaultRootElement().getElementIndex(offset);
	}

	/**
	 * Returns the start offset of the specified line.
	 * 
	 * @param line
	 *            The line
	 * @return The start offset of the specified line, or -1 if the line is
	 *         invalid
	 */
	public int getLineStartOffset(int line) {
		Element lineElement = document.getDefaultRootElement().getElement(line);
		if (lineElement == null)
			return -1;
		else
			return lineElement.getStartOffset();
	}

	/**
	 * Returns the end offset of the specified line.
	 * 
	 * @param line
	 *            The line
	 * @return The end offset of the specified line, or -1 if the line is
	 *         invalid.
	 */
	public int getLineEndOffset(int line) {
		Element lineElement = document.getDefaultRootElement().getElement(line);
		if (lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset();
	}

	/**
	 * Returns the length of the specified line.
	 * 
	 * @param line
	 *            The line
	 */
	public int getLineLength(int line) {
		Element lineElement = document.getDefaultRootElement().getElement(line);

		if (lineElement == null)
			return -1;
		else
			return getLineText(line).replaceAll("\\t", "        ").length();// lineElement.getEndOffset()
																			// -
																			// lineElement.getStartOffset()
																			// - 1;
	}

	/**
	 * Returns the entire text of this text area.
	 */
	public String getText() {
		try {
			return document.getText(0, document.getLength());
		} catch (BadLocationException bl) {
			bl.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the entire text of this text area.
	 */
	public void setText(String text) {
		try {
			document.beginCompoundEdit();
			document.remove(0, document.getLength());
			document.insertString(0, text, null);
			updateLongestLine();
			setCaretPosition(0);
			updateScrollBars();
			undo.discardAllEdits();
		} catch (BadLocationException bl) {
			bl.printStackTrace();
		} finally {
			document.endCompoundEdit();
		}
	}

	/**
	 * Returns the specified substring of the document.
	 * 
	 * @param start
	 *            The start offset
	 * @param len
	 *            The length of the substring
	 * @return The substring, or null if the offsets are invalid
	 */
	public final String getText(int start, int len) {
		try {
			return document.getText(start, len);
		} catch (BadLocationException bl) {
			bl.printStackTrace();
			return null;
		}
	}

	/**
	 * Copies the specified substring of the document into a segment. If the
	 * offsets are invalid, the segment will contain a null string.
	 * 
	 * @param start
	 *            The start offset
	 * @param len
	 *            The length of the substring
	 * @param segment
	 *            The segment
	 */
	public final void getText(int start, int len, Segment segment) {
		try {
			document.getText(start, len, segment);
		} catch (BadLocationException bl) {
			bl.printStackTrace();
			segment.offset = segment.count = 0;
		}
	}

	/**
	 * Returns the text on the specified line.
	 * 
	 * @param lineIndex
	 *            The line
	 * @return The text, or null if the line is invalid
	 */
	public final String getLineText(int lineIndex) {
		int start = getLineStartOffset(lineIndex);
		return getText(start, getLineEndOffset(lineIndex) - start - 1);
	}

	/**
	 * Returns the text on the specified line.
	 * 
	 * @param lineIndex
	 *            The line
	 * @return The text, or null if the line is invalid
	 */
	public final String getLineTextWithEOL(int lineIndex) {
		int start = getLineStartOffset(lineIndex);
		return getText(start, getLineEndOffset(lineIndex) - start);
	}
	/**
	 * Copies the text on the specified line into a segment. If the line is
	 * invalid, the segment will contain a null string.
	 * 
	 * @param lineIndex
	 *            The line
	 */
	public final void getLineText(int lineIndex, Segment segment) {
		int start = getLineStartOffset(lineIndex);
		getText(start, getLineEndOffset(lineIndex) - start - 1, segment);
	}

	/**
	 * Returns the selection start offset.
	 */
	public final int getSelectionStart() {
		return selectionStart;
	}

	/**
	 * Returns the offset where the selection starts on the specified line.
	 */
	public int getSelectionStart(int line) {
		if (line == selectionStartLine)
			return selectionStart;
		else if (rectSelect) {
			Element map = document.getDefaultRootElement();
			int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();

			Element lineElement = map.getElement(line);
			int lineStart = lineElement.getStartOffset();
			int lineEnd = lineElement.getEndOffset() - 1;
			return Math.min(lineEnd, lineStart + start);
		} else
			return getLineStartOffset(line);
	}

	/**
	 * Returns the selection start line.
	 */
	public final int getSelectionStartLine() {
		return selectionStartLine;
	}

	/**
	 * Sets the selection start. The new selection will be the new selection
	 * start and the old selection end.
	 * 
	 * @param selectionStart
	 *            The selection start
	 * @see #select(int,int)
	 */
	public final void setSelectionStart(int selectionStart) {
		select(selectionStart, selectionEnd);
	}

	/**
	 * Returns the selection end offset.
	 */
	public final int getSelectionEnd() {
		return selectionEnd;
	}

	/**
	 * Returns the offset where the selection ends on the specified line.
	 */
	public int getSelectionEnd(int line) {
		if (line == selectionEndLine)
			return selectionEnd;
		else if (rectSelect) {
			Element map = document.getDefaultRootElement();
			int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

			Element lineElement = map.getElement(line);
			int lineStart = lineElement.getStartOffset();
			int lineEnd = lineElement.getEndOffset() - 1;
			return Math.min(lineEnd, lineStart + end);
		} else
			return getLineEndOffset(line) - 1;
	}

	/**
	 * Returns the selection end line.
	 */
	public final int getSelectionEndLine() {
		return selectionEndLine;
	}

	/**
	 * Sets the selection end. The new selection will be the old selection start
	 * and the bew selection end.
	 * 
	 * @param selectionEnd
	 *            The selection end
	 * @see #select(int,int)
	 */
	public final void setSelectionEnd(int selectionEnd) {
		select(selectionStart, selectionEnd);
	}

	/**
	 * Returns the caret position. This will either be the selection start or
	 * the selection end, depending on which direction the selection was made
	 * in.
	 */
	public final int getCaretPosition() {
		return (biasLeft ? selectionStart : selectionEnd);
	}

	/**
	 * Returns the caret line.
	 */
	public final int getCaretLine() {
		return (biasLeft ? selectionStartLine : selectionEndLine);
	}

	/**
	 * Returns the mark position. This will be the opposite selection bound to
	 * the caret position.
	 * 
	 * @see #getCaretPosition()
	 */
	public final int getMarkPosition() {
		return (biasLeft ? selectionEnd : selectionStart);
	}

	/**
	 * Returns the mark line.
	 */
	public final int getMarkLine() {
		return (biasLeft ? selectionEndLine : selectionStartLine);
	}

	/**
	 * Sets the caret position. The new selection will consist of the caret
	 * position only (hence no text will be selected)
	 * 
	 * @param caret
	 *            The caret position
	 * @see #select(int,int)
	 */
	public final void setCaretPosition(int caret) {
		select(caret, caret);
	}

	/**
	 * Selects all text in the document.
	 */
	public final void selectAll() {
		select(0, getDocumentLength());
	}

	/**
	 * Moves the mark to the caret position.
	 */
	public final void selectNone() {
		select(getCaretPosition(), getCaretPosition());
	}

	/**
	 * Selects from the start offset to the end offset. This is the general
	 * selection method used by all other selecting methods. The caret position
	 * will be start if start &lt; end, and end if end &gt; start.
	 * 
	 * @param start
	 *            The start offset
	 * @param end
	 *            The end offset
	 */
	public void select(int start, int end) {
		int newStart, newEnd;
		boolean newBias;
		if (start <= end) {
			newStart = start;
			newEnd = end;
			newBias = false;
		} else {
			newStart = end;
			newEnd = start;
			newBias = true;
		}

		if (newStart < 0 || newEnd > getDocumentLength()) {
			throw new IllegalArgumentException("Bounds out of" + " range: " + newStart + "," + newEnd);
		}

		// If the new position is the same as the old, we don't
		// do all this crap, however we still do the stuff at
		// the end (clearing magic position, scrolling)
		if (newStart != selectionStart || newEnd != selectionEnd || newBias != biasLeft) {
			int newStartLine = getLineOfOffset(newStart);
			int newEndLine = getLineOfOffset(newEnd);
			if (painter.isBracketHighlightEnabled()) {
				if (bracketLine != -1)
					painter.invalidateLine(bracketLine);
				updateBracketHighlight(end);
				if (bracketLine != -1)
					painter.invalidateLine(bracketLine);
			}

			painter.invalidateLineRange(selectionStartLine, selectionEndLine);
			painter.invalidateLineRange(newStartLine, newEndLine);

			document.addUndoableEdit(new CaretUndo(selectionStart, selectionEnd));

			selectionStart = newStart;
			selectionEnd = newEnd;
			selectionStartLine = newStartLine;
			selectionEndLine = newEndLine;
			biasLeft = newBias;

			fireCaretEvent();
		}

		// When the user is typing, etc, we don't want the caret
		// to blink
		blink = true;
		caretTimer.restart();

		// Disable rectangle select if selection start = selection end
		if (selectionStart == selectionEnd)
			rectSelect = false;

		// Clear the `magic' caret position used by up/down
		magicCaret = -1;

		scrollToCaret();
	}

	/**
	 * Returns the selected text, or null if no selection is active.
	 */
	public final String getSelectedText() {
		if (selectionStart == selectionEnd)
			return null;

		if (rectSelect) {
			// Return each row of the selection on a new line

			Element map = document.getDefaultRootElement();

			int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
			int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

			// Certain rectangles satisfy this condition...
			if (end < start) {
				int tmp = end;
				end = start;
				start = tmp;
			}

			StringBuffer buf = new StringBuffer();
			Segment seg = new Segment();

			for (int i = selectionStartLine; i <= selectionEndLine; i++) {
				Element lineElement = map.getElement(i);
				int lineStart = lineElement.getStartOffset();
				int lineEnd = lineElement.getEndOffset() - 1;
				int lineLen = lineEnd - lineStart;

				lineStart = Math.min(lineStart + start, lineEnd);
				lineLen = Math.min(end - start, lineEnd - lineStart);

				getText(lineStart, lineLen, seg);
				buf.append(seg.array, seg.offset, seg.count);

				if (i != selectionEndLine)
					buf.append('\n');
			}

			return buf.toString();
		} else {
			return getText(selectionStart, selectionEnd - selectionStart);
		}
	}

	/**
	 * Replaces the selection with the specified text.
	 * 
	 * @param selectedText
	 *            The replacement text for the selection
	 */
	public void setSelectedText(String selectedText) {
		if (!editable) {
			throw new InternalError("Text component" + " read only");
		}

		document.beginCompoundEdit();

		try {
			if (rectSelect) {
				Element map = document.getDefaultRootElement();

				int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
				int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

				// Certain rectangles satisfy this condition...
				if (end < start) {
					int tmp = end;
					end = start;
					start = tmp;
				}

				int lastNewline = 0;
				int currNewline = 0;

				for (int i = selectionStartLine; i <= selectionEndLine; i++) {
					Element lineElement = map.getElement(i);
					int lineStart = lineElement.getStartOffset();
					int lineEnd = lineElement.getEndOffset() - 1;
					int rectStart = Math.min(lineEnd, lineStart + start);

					document.remove(rectStart, Math.min(lineEnd - rectStart, end - start));

					if (selectedText == null)
						continue;

					currNewline = selectedText.indexOf('\n', lastNewline);
					if (currNewline == -1)
						currNewline = selectedText.length();

					document.insertString(rectStart, selectedText.substring(lastNewline, currNewline), null);

					lastNewline = Math.min(selectedText.length(), currNewline + 1);
				}

				if (selectedText != null && currNewline != selectedText.length()) {
					int offset = map.getElement(selectionEndLine).getEndOffset() - 1;
					document.insertString(offset, "\n", null);
					document.insertString(offset + 1, selectedText.substring(currNewline + 1), null);

				}
				updateLongestLine();
				updateScrollBars();
			} else {
				document.remove(selectionStart, selectionEnd - selectionStart);
				if (selectedText != null) {
					int line = getSelectionStartLine();
					document.insertString(selectionStart, selectedText, null);
					// recalculateVisibleLines();

					/*
					 * int size = getLineLength(line); if (size >
					 * longestLineSize){ longestLineSize = size; longestLine =
					 * line; }
					 */

				}
			}
		} catch (BadLocationException bl) {
			bl.printStackTrace();
			throw new InternalError("Cannot replace" + " selection");
		}
		// No matter what happends... stops us from leaving document
		// in a bad state
		finally {
			document.endCompoundEdit();

		}
		setCaretPosition(selectionEnd);
		updateLongestLine();
		// updateScrollBars();
	}

	/**
	 * Returns true if this text area is editable, false otherwise.
	 */
	public final boolean isEditable() {
		return editable;
	}

	/**
	 * Sets if this component is editable.
	 * 
	 * @param editable
	 *            True if this text area should be editable, false otherwise
	 */
	public final void setEditable(boolean editable) {
		this.editable = editable;
		setCaretVisible(false);
	}

	/**
	 * Returns the right click popup menu.
	 */
	public final JPopupMenu getRightClickPopup() {
		return popup;
	}

	/**
	 * Sets the right click popup menu.
	 * 
	 * @param popup
	 *            The popup
	 */
	public final void setRightClickPopup(JPopupMenu popup) {
		this.popup = popup;
	}

	/**
	 * Returns the `magic' caret position. This can be used to preserve the
	 * column position when moving up and down lines.
	 */
	public final int getMagicCaretPosition() {
		return magicCaret;
	}

	/**
	 * Sets the `magic' caret position. This can be used to preserve the column
	 * position when moving up and down lines.
	 * 
	 * @param magicCaret
	 *            The magic caret position
	 */
	public final void setMagicCaretPosition(int magicCaret) {
		this.magicCaret = magicCaret;
	}

	public void updateLongestLine() {
		// this loops through each line and figures out the longest line size
		// needs optimization. One day.
		int size = getLineCount();
		int max_size = 0;
		for (int i = 0; i < size; i++) {
			String line = getLineText(i).replaceAll("\\t", "        ");
			int lsize = painter.getFontMetrics().charsWidth(line.toCharArray(), 0, line.length());

			if (lsize >= max_size) {
				longestLine = i;
				if (showLineEndings){
					longestLineSize = lsize + 20; // added padding
				}else{
					longestLineSize = lsize + 10; // added padding
				}
				max_size = lsize;
			}
		}

	}

	/**
	 * Similar to <code>setSelectedText()</code>, but overstrikes the
	 * appropriate number of characters if overwrite mode is enabled.
	 * 
	 * @param str
	 *            The string
	 * @see #setSelectedText(String)
	 * @see #isOverwriteEnabled()
	 */
	public void overwriteSetSelectedText(String str) {
		// Don't overstrike if there is a selection
		if (!overwrite || selectionStart != selectionEnd) {
			setSelectedText(str);
			return;
		}

		// Don't overstrike if we're on the end of
		// the line
		int caret = getCaretPosition();
		int caretLineEnd = getLineEndOffset(getCaretLine());
		if (caretLineEnd - caret <= str.length()) {
			setSelectedText(str);
			return;
		}

		document.beginCompoundEdit();

		try {
			document.remove(caret, str.length());
			document.insertString(caret, str, null);
			updateLongestLine();
		} catch (BadLocationException bl) {
			bl.printStackTrace();
		} finally {
			document.endCompoundEdit();
		}
	}

	/**
	 * Returns true if overwrite mode is enabled, false otherwise.
	 */
	public final boolean isOverwriteEnabled() {
		return overwrite;
	}

	/**
	 * Sets if overwrite mode should be enabled.
	 * 
	 * @param overwrite
	 *            True if overwrite mode should be enabled, false otherwise.
	 */
	public final void setOverwriteEnabled(boolean overwrite) {
		this.overwrite = overwrite;
		painter.invalidateSelectedLines();
	}

	/**
	 * Returns true if the selection is rectangular, false otherwise.
	 */
	public final boolean isSelectionRectangular() {
		return rectSelect;
	}

	/**
	 * Sets if the selection should be rectangular.
	 * 
	 * @param overwrite
	 *            True if the selection should be rectangular, false otherwise.
	 */
	public final void setSelectionRectangular(boolean rectSelect) {
		this.rectSelect = rectSelect;
		painter.invalidateSelectedLines();
	}

	/**
	 * Returns the position of the highlighted bracket (the bracket matching the
	 * one before the caret)
	 */
	public final int getBracketPosition() {
		return bracketPosition;
	}

	/**
	 * Returns the line of the highlighted bracket (the bracket matching the one
	 * before the caret)
	 */
	public final int getBracketLine() {
		return bracketLine;
	}

	/**
	 * Adds a caret change listener to this text area.
	 * 
	 * @param listener
	 *            The listener
	 */
	public final void addCaretListener(CaretListener listener) {
		listenerList.add(CaretListener.class, listener);
	}

	/**
	 * Removes a caret change listener from this text area.
	 * 
	 * @param listener
	 *            The listener
	 */
	public final void removeCaretListener(CaretListener listener) {
		listenerList.remove(CaretListener.class, listener);
	}

	/**
	 * Deletes the selected text from the text area and places it into the
	 * clipboard.
	 */
	public void cut() {
		if (editable) {
			copy();
			setSelectedText("");
		}
	}

	/**
	 * Places the selected text into the clipboard.
	 */
	public void copy() {
		if (selectionStart != selectionEnd) {
			Clipboard clipboard = getToolkit().getSystemClipboard();

			String selection = getSelectedText();

			int repeatCount = inputHandler.getRepeatCount();
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < repeatCount; i++)
				buf.append(selection);

			clipboard.setContents(new StringSelection(buf.toString()), null);
		}
	}

	/**
	 * Inserts the clipboard contents into the text.
	 */
	public void paste() {
		if (editable) {
			Clipboard clipboard = getToolkit().getSystemClipboard();
			try {
				painter.currentLineTokens = null;
				// The MacOS MRJ doesn't convert \r to \n,
				// so do it here
				String selection = (String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor);
				// if (System.getProperty("os.name").equals(MAC_OS)){
				//

				//selection = selection.replaceAll("\r\n", "\n");
				//selection = selection.replaceAll("\r", "\n");
				// }

				int repeatCount = inputHandler.getRepeatCount();
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < repeatCount; i++)
					buf.append(selection);
				selection = buf.toString();
				setSelectedText(selection);
			} catch (Exception e) {
				getToolkit().beep();
				System.err.println("Clipboard does not" + " contain a string");
			}
		}
	}

	/**
	 * Called by the AWT when this component is removed from it's parent. This
	 * stops clears the currently focused component.
	 */
	public void removeNotify() {
		super.removeNotify();
		if (focusedComponent == this)
			focusedComponent = null;
	}

	/**
	 * Forwards key events directly to the input handler. This is slightly
	 * faster than using a KeyListener because some Swing overhead is avoided.
	 */
	public void processKeyEvent(KeyEvent evt) {
		if (inputHandler == null)
			return;
		switch (evt.getID()) {
			case KeyEvent.KEY_TYPED:
				inputHandler.keyTyped(evt);
				break;
			case KeyEvent.KEY_PRESSED:
				inputHandler.keyPressed(evt);
				break;
			case KeyEvent.KEY_RELEASED:
				inputHandler.keyReleased(evt);
				break;
		}
	}

	// protected members
	protected static String CENTER = "center";

	protected static String RIGHT = "right";

	protected static String BOTTOM = "bottom";

	protected static String LEFT = "left";

	protected static JEditTextArea focusedComponent;

	protected static Timer caretTimer;

	protected TextAreaPainter painter;

	protected JPopupMenu popup;

	protected EventListenerList listenerList;

	protected MutableCaretEvent caretEvent;

	protected boolean caretBlinks;

	protected boolean caretVisible;

	protected boolean blink;

	protected boolean editable;

	protected int firstLine;

	protected int visibleLines;

	protected int electricScroll;

	protected int horizontalOffset;

	protected LineNumber left;

	protected JScrollBar vertical;

	protected JScrollBar horizontal;

	protected boolean scrollBarsInitialized;

	protected InputHandler inputHandler;

	protected SyntaxDocument document;

	protected DocumentHandler documentHandler;

	protected Segment lineSegment;

	protected int selectionStart;

	protected int selectionStartLine;

	protected int selectionEnd;

	protected int selectionEndLine;

	protected boolean biasLeft;

	protected int bracketPosition;

	protected int bracketLine;

	protected int magicCaret;

	protected boolean overwrite;

	protected boolean rectSelect;

	protected PopUpHandler popupHandler;

	protected UndoManager undo = new UndoManager();

	protected void fireCaretEvent() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i--) {
			if (listeners[i] == CaretListener.class) {
				((CaretListener) listeners[i + 1]).caretUpdate(caretEvent);
			}
		}
	}

	protected void updateBracketHighlight(int newCaretPosition) {
		if (newCaretPosition == 0) {
			bracketPosition = bracketLine = -1;
			return;
		}

		try {
			int offset = TextUtilities.findMatchingBracket(document, newCaretPosition - 1);
			if (offset != -1) {
				bracketLine = getLineOfOffset(offset);
				bracketPosition = offset - getLineStartOffset(bracketLine);
				return;
			}
		} catch (BadLocationException bl) {
			bl.printStackTrace();
		}

		bracketLine = bracketPosition = -1;
	}

	protected void documentChanged(DocumentEvent evt) {
		DocumentEvent.ElementChange ch = evt.getChange(document.getDefaultRootElement());

		int count;
		if (ch == null)
			count = 0;
		else
			count = ch.getChildrenAdded().length - ch.getChildrenRemoved().length;

		int line = getLineOfOffset(evt.getOffset());
		if (count == 0) {
			painter.invalidateLine(line);

		}
		// do magic stuff
		else if (line < firstLine) {
			setFirstLine(firstLine + count);
		}
		// end of magic stuff
		else {
			painter.invalidateLineRange(line, firstLine + visibleLines);
			// updateScrollBars();
		}
		if (count < 0) {
			if (firstLine >= count) {
				setFirstLine(firstLine + count);
			} else {
				setFirstLine(0);
			}
		}

		updateLongestLine();
		updateScrollBars();

	}

	class ScrollLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
			if (name.equals(CENTER))
				center = comp;
			else if (name.equals(RIGHT))
				right = comp;
			else if (name.equals(BOTTOM))
				bottom = comp;
			else if (name.equals(LEFT))
				left = comp;
			else if (name.equals(LEFT_OF_SCROLLBAR))
				leftOfScrollBar.addElement(comp);
		}

		public void removeLayoutComponent(Component comp) {
			if (center == comp)
				center = null;
			if (right == comp)
				right = null;
			if (bottom == comp)
				bottom = null;
			if (left == comp)
				left = null;
			else
				leftOfScrollBar.removeElement(comp);
		}

		public Dimension preferredLayoutSize(Container parent) {
			Dimension dim = new Dimension();
			Insets insets = getInsets();
			dim.width = insets.left + insets.right;
			dim.height = insets.top + insets.bottom;
			if (left != null) {
				Dimension leftPref = left.getPreferredSize();
				dim.width += leftPref.width;
			}
			Dimension centerPref = center.getPreferredSize();
			dim.width += centerPref.width;
			dim.height += centerPref.height;
			Dimension rightPref = right.getPreferredSize();
			dim.width += rightPref.width;
			Dimension bottomPref = bottom.getPreferredSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension dim = new Dimension();
			Insets insets = getInsets();
			dim.width = insets.left + insets.right;
			dim.height = insets.top + insets.bottom;
			if (left != null) {
				Dimension leftPref = left.getPreferredSize();
				dim.width += leftPref.width;
			}
			Dimension centerPref = center.getMinimumSize();
			dim.width += centerPref.width;
			dim.height += centerPref.height;
			Dimension rightPref = right.getMinimumSize();
			dim.width += rightPref.width;
			Dimension bottomPref = bottom.getMinimumSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public void layoutContainer(Container parent) {
			Dimension size = parent.getSize();
			Insets insets = parent.getInsets();
			int itop = insets.top;
			int ileft = insets.left;
			int ibottom = insets.bottom;
			int iright = insets.right;
			int leftWidth = 0;
			if (left != null) {
				leftWidth = left.getPreferredSize().width;
			}
			int rightWidth = right.getPreferredSize().width;
			int bottomHeight = bottom.getPreferredSize().height;
			int centerWidth = size.width - rightWidth - leftWidth - ileft - iright;
			int centerHeight = size.height - bottomHeight - itop - ibottom;

			center.setBounds(ileft + leftWidth, itop, centerWidth, centerHeight);
			if (left != null) {
				if (left.getHeight() > center.getHeight())
					left.setBounds(ileft, left.getY(), leftWidth, left.getHeight());
				else
					left.setBounds(ileft, itop, leftWidth, centerHeight);
			}
			right.setBounds(ileft + centerWidth + leftWidth, itop, rightWidth, centerHeight);

			// Lay out all status components, in order
			Enumeration status = leftOfScrollBar.elements();
			while (status.hasMoreElements()) {
				Component comp = (Component) status.nextElement();
				Dimension dim = comp.getPreferredSize();
				comp.setBounds(ileft, itop + centerHeight, dim.width, bottomHeight);
				ileft += dim.width;
			}

			bottom.setBounds(ileft, itop + centerHeight, size.width - rightWidth - ileft - iright, bottomHeight);
		}

		// private members
		private Component center;

		private Component right;

		private Component bottom;

		private Component left;

		private Vector leftOfScrollBar = new Vector();
	}

	static class CaretBlinker implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (focusedComponent != null && focusedComponent.hasFocus())
				focusedComponent.blinkCaret();
		}
	}

	class MutableCaretEvent extends CaretEvent {
		MutableCaretEvent() {
			super(JEditTextArea.this);
		}

		public int getDot() {
			return getCaretPosition();
		}

		public int getMark() {
			return getMarkPosition();
		}
	}

	class AdjustHandler implements AdjustmentListener {
		public void adjustmentValueChanged(final AdjustmentEvent evt) {
			if (!scrollBarsInitialized)
				return;

			// If this is not done, mousePressed events accumilate
			// and the result is that scrolling doesn't stop after
			// the mouse is released
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (evt.getAdjustable() == vertical) {
						setFirstLine(vertical.getValue());
						if (enableLineNumbers) {
							int top = firstLine * -painter.getFontMetrics().getHeight();
							int height = -top * painter.getHeight();
							if (left.getHeight() < height) {
								height = height + left.getHeight();
							} else {
								height = vertical.getHeight();
							}
							left.setBounds(left.getX(), vertical.getY() + top, left.getWidth(), left.getHeight() + height);
						}
					} else
						setHorizontalOffset(-horizontal.getValue());
				}
			});
		}
	}

	class ComponentHandler extends ComponentAdapter {
		public void componentResized(ComponentEvent evt) {
			recalculateVisibleLines();
			scrollBarsInitialized = true;
		}
	}

	class DocumentHandler implements DocumentListener {
		public void insertUpdate(DocumentEvent evt) {
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if (selectionStart > offset || (selectionStart == selectionEnd && selectionStart == offset))
				newStart = selectionStart + length;
			else
				newStart = selectionStart;

			if (selectionEnd >= offset)
				newEnd = selectionEnd + length;
			else
				newEnd = selectionEnd;

			select(newStart, newEnd);
		}

		public void removeUpdate(DocumentEvent evt) {
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if (selectionStart > offset) {
				if (selectionStart > offset + length)
					newStart = selectionStart - length;
				else
					newStart = offset;
			} else
				newStart = selectionStart;

			if (selectionEnd > offset) {
				if (selectionEnd > offset + length)
					newEnd = selectionEnd - length;
				else
					newEnd = offset;
			} else
				newEnd = selectionEnd;

			select(newStart, newEnd);
		}

		public void changedUpdate(DocumentEvent evt) {}
	}

	class DragHandler implements MouseMotionListener {
		public void mouseDragged(MouseEvent evt) {
			if (popup != null && popup.isVisible())
				return;

			setSelectionRectangular((evt.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0);
			select(getMarkPosition(), xyToOffset(evt.getX(), evt.getY()));
		}

		public void mouseMoved(MouseEvent evt) {}
	}

	class FocusHandler implements FocusListener {
		public void focusGained(FocusEvent evt) {
			setCaretVisible(true);
			focusedComponent = JEditTextArea.this;
		}

		public void focusLost(FocusEvent evt) {
			setCaretVisible(false);
			focusedComponent = null;
		}
	}

	class MouseHandler extends MouseAdapter {
		public void mousePressed(MouseEvent evt) {
			requestFocus();

			// Focus events not fired sometimes?
			setCaretVisible(true);
			focusedComponent = JEditTextArea.this;

			if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && popup != null) {

				// setCursor(Cursor.getDefaultCursor());
				if (popupHandler != null) {
					popupHandler.showPopupMenu(popup, evt);
				} else {
					popup.show(painter, evt.getX(), evt.getY());
				}
				// setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
				return;
			}

			int line = yToLine(evt.getY());
			int offset = xToOffset(line, evt.getX());
			int dot = getLineStartOffset(line) + offset;

			switch (evt.getClickCount()) {
				case 1:
					doSingleClick(evt, line, offset, dot);
					break;
				case 2:
					// It uses the bracket matching stuff, so
					// it can throw a BLE
					try {
						doDoubleClick(evt, line, offset, dot);
					} catch (BadLocationException bl) {
						bl.printStackTrace();
					}
					break;
				case 3:
					doTripleClick(evt, line, offset, dot);
					break;
			}
		}

		private void doSingleClick(MouseEvent evt, int line, int offset, int dot) {
			if (isEnabled()) {
				if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
					rectSelect = (evt.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
					select(getMarkPosition(), dot);
				} else
					setCaretPosition(dot);
			}
		}

		private void doDoubleClick(MouseEvent evt, int line, int offset, int dot) throws BadLocationException {
			// Ignore empty lines
			if (isEditable()) {
				if (getLineLength(line) == 0)
					return;

				try {
					int bracket = TextUtilities.findMatchingBracket(document, Math.max(0, dot - 1));
					if (bracket != -1) {
						int mark = getMarkPosition();
						// Hack
						if (bracket > mark) {
							bracket++;
							mark--;
						}
						select(mark, bracket);
						return;
					}
				} catch (BadLocationException bl) {
					bl.printStackTrace();
				}

				// Ok, it's not a bracket... select the word
				String lineText = getLineText(line);
				char ch = lineText.charAt(Math.max(0, offset - 1));

				String noWordSep = (String) document.getProperty("noWordSep");
				if (noWordSep == null)
					noWordSep = "";

				// If the user clicked on a non-letter char,
				// we select the surrounding non-letters
				boolean selectNoLetter = (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1);

				int wordStart = 0;

				for (int i = offset - 1; i >= 0; i--) {
					ch = lineText.charAt(i);
					if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
						wordStart = i + 1;
						break;
					}
				}

				int wordEnd = lineText.length();
				for (int i = offset; i < lineText.length(); i++) {
					ch = lineText.charAt(i);
					if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
						wordEnd = i;
						break;
					}
				}

				int lineStart = getLineStartOffset(line);
				select(lineStart + wordStart, lineStart + wordEnd);

				/*
				 * String lineText = getLineText(line); String noWordSep =
				 * (String)document.getProperty("noWordSep"); int wordStart =
				 * TextUtilities.findWordStart(lineText,offset,noWordSep); int
				 * wordEnd =
				 * TextUtilities.findWordEnd(lineText,offset,noWordSep);
				 * 
				 * int lineStart = getLineStartOffset(line); select(lineStart +
				 * wordStart,lineStart + wordEnd);
				 */
			}
		}

		private void doTripleClick(MouseEvent evt, int line, int offset, int dot) {
			select(getLineStartOffset(line), getLineEndOffset(line) - 1);
		}
	}

	class ScrollWheelHandler implements MouseWheelListener {
		/**
		 * Invoked when the mouse wheel is rotated.
		 * 
		 * @see MouseWheelEvent
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			int line_to_scroll_to;
			int units = e.getUnitsToScroll();
			line_to_scroll_to = firstLine + units;
			if (units < 0) {
				line_to_scroll_to += electricScroll;
				if (line_to_scroll_to < 0) {
					line_to_scroll_to = 0;
				}
			} else if (units > 0) {
				line_to_scroll_to += visibleLines - 1 - electricScroll;
				if (line_to_scroll_to >= getLineCount()) {
					line_to_scroll_to = getLineCount() - 1;
				}
			}

			scrollTo(line_to_scroll_to, -1);
		}
	}

	public interface PopUpHandler {
		void showPopupMenu(JPopupMenu menu, java.awt.event.MouseEvent evt);
	}

	// This one listens for edits that can be undone.
	protected class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			// Remember the edit and update the menus.
			undo.addEdit(e.getEdit());

		}
	}

	class CaretUndo extends AbstractUndoableEdit {
		private int start;

		private int end;

		CaretUndo(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public boolean isSignificant() {
			return false;
		}

		public String getPresentationName() {
			return "caret move";
		}

		public void undo() throws CannotUndoException {
			super.undo();

			select(start, end);
		}

		public void redo() throws CannotRedoException {
			super.redo();

			select(start, end);
		}

		public boolean addEdit(UndoableEdit edit) {
			if (edit instanceof CaretUndo) {
				CaretUndo cedit = (CaretUndo) edit;
				start = cedit.start;
				end = cedit.end;
				cedit.die();

				return true;
			} else
				return false;
		}
	}

	static {
		caretTimer = new Timer(500, new CaretBlinker());
		caretTimer.setInitialDelay(500);
		caretTimer.start();
	}

	public boolean isShowLineEndings() {
		return showLineEndings;
	}

	public void setShowLineEndings(boolean showLineEndings) {
		this.showLineEndings = showLineEndings;
		painter.setEOLMarkersPainted(showLineEndings);
	}

}
