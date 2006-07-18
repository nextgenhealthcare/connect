/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.applets.editor;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

public class Editor extends Applet {
    private boolean isStandalone = false;
    private JPanel mainContainer = new JPanel();
    private JPanel topPane = new JPanel();
    private BoxLayout topPaneLayout = new BoxLayout(topPane, BoxLayout.X_AXIS);
    private JTextField methodName = new JTextField();
    private JButton validateButton = new JButton();
    private JScrollPane editorScrollPane = new JScrollPane();
    private JPanel editorPanel = new JPanel();
    private CardLayout editorPanelLayout = new CardLayout();
    private JTextPane editor = new JTextPane();
    private JPanel southPane = new JPanel();
    private JPanel endOfMethodPane = new JPanel();
    private BoxLayout endOfMethodPaneLayout = new BoxLayout(endOfMethodPane, BoxLayout.X_AXIS);
    private JPanel statusPane = new JPanel();
    private BoxLayout statusPaneLayout = new BoxLayout(statusPane, BoxLayout.X_AXIS);
    private JPanel rhinoConsole = new JPanel();
    private BoxLayout rhinoConsoleLayout = new BoxLayout(rhinoConsole, BoxLayout.X_AXIS);
    private JTextField endOfMethodText = new JTextField();
    private JTextField objectName = new JTextField();
    private JTextField cursorPosition = new JTextField();
    private JTextField editMode = new JTextField();
    private JTextField rhinoOutput = new JTextField();
    private boolean insertMode = true;
    private Caret insertCaret = new DefaultCaret();
    private Caret overlayCaret = new OvertypeCaret();
    
    private String scriptValidatorServlet = "";

    // Get a parameter value.
    public String getParameter(String key, String def) {
        String returnString = "";

        if (isStandalone)
            returnString = System.getProperty(key, def);
        else {
            if (getParameter(key) != null)
                returnString = getParameter(key);
            else
                returnString = def;
        }

        return returnString;
    }

    public String getEditorText() {
    	// removes the extra whitespace around the script
        return editor.getText().trim();
    }

    public Editor() {
    	
    }

    // initialize the applet
    public void init() {
        String scriptType = this.getParameter("scriptType", "JavaScript");
        editor.setStyledDocument(new SyntaxDocument(scriptType));
        
        String scriptValue = this.getParameter("scriptValue");
        editor.setText(scriptValue);
        
        scriptValidatorServlet = this.getParameter("scriptValidatorServlet", "validator");
        
        editorInit();
    }

    // initialize the component.
    private void editorInit() {
        this.setLayout(new BorderLayout());

        mainContainer.setLayout(new BorderLayout());
        mainContainer.setBorder(BorderFactory.createEtchedBorder());

        topPane.setBackground(SystemColor.control);
        topPane.setPreferredSize(new Dimension(10, 30));
        topPane.setLayout(topPaneLayout);

        Font courierNew = new Font("Courier New", Font.PLAIN, 12);

        methodName.setBorder(null);
        methodName.setEnabled(false);
        methodName.setDisabledTextColor(SystemColor.BLACK);
        methodName.setBackground(SystemColor.control);
        methodName.setText("");
        methodName.setFont(courierNew);

        // validateButton.setBackground(SystemColor.control);
        validateButton.setText("Validate");

        topPane.add(methodName);
        topPane.add(validateButton);

        editorPanel.setLayout(editorPanelLayout);
        editorScrollPane.getViewport().add(editorPanel);
        editorScrollPane.setRowHeaderView(new LineNumber(editor));
        editorPanel.add(editor, "mainEditorField");
        editor.setMargin(new Insets(2, 5, 0, 0));
        editor.addCaretListener(new EditorCaretListener(editor, cursorPosition));

        southPane.setLayout(new BorderLayout());

        endOfMethodPane.setLayout(endOfMethodPaneLayout);
        statusPane.setLayout(statusPaneLayout);
        rhinoConsole.setLayout(rhinoConsoleLayout);

        endOfMethodPane.add(endOfMethodText);
        statusPane.add(objectName);
        statusPane.add(cursorPosition);
        statusPane.add(editMode);
        rhinoConsole.add(rhinoOutput);
        rhinoConsole.setPreferredSize(new Dimension(10, 50));

        endOfMethodText.setText("");
        endOfMethodText.setBackground(SystemColor.control);
        endOfMethodText.setBorder(null);
        endOfMethodText.setEnabled(false);
        endOfMethodText.setDisabledTextColor(SystemColor.BLACK);
        endOfMethodText.setFont(courierNew);

        objectName.setText("MyTransformerComponent");
        objectName.setEditable(false);
        objectName.setMaximumSize(new Dimension(500, 20));

        cursorPosition.setText("1:1");
        cursorPosition.setEnabled(false);
        cursorPosition.setBackground(objectName.getBackground());
        cursorPosition.setDisabledTextColor(SystemColor.BLACK);
        cursorPosition.setHorizontalAlignment(SwingConstants.CENTER);
        cursorPosition.setMaximumSize(new Dimension(100, 20));
        cursorPosition.setMinimumSize(new Dimension(100, 20));
        editMode.setText("INS");
        editMode.setEnabled(false);
        editMode.setBackground(objectName.getBackground());
        editMode.setDisabledTextColor(SystemColor.BLACK);
        editMode.setHorizontalAlignment(SwingConstants.CENTER);
        editMode.setMaximumSize(new Dimension(40, 20));
        editMode.setMinimumSize(new Dimension(40, 20));
        rhinoOutput.setFont(courierNew);
        rhinoOutput.setEditable(false);

        southPane.add(endOfMethodPane, BorderLayout.NORTH);
        southPane.add(statusPane, BorderLayout.CENTER);
        southPane.add(rhinoConsole, BorderLayout.SOUTH);

        mainContainer.add(topPane, BorderLayout.NORTH);
        mainContainer.add(editorScrollPane, BorderLayout.CENTER);
        mainContainer.add(southPane, BorderLayout.SOUTH);

        this.add(mainContainer, BorderLayout.CENTER);

        editor.requestFocusInWindow();
        editor.setCaretPosition(0);

        final CompoundUndoManager undoManager = new CompoundUndoManager(editor);

        editor.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {

                int shiftctrl = KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK;
                int ctrl = KeyEvent.CTRL_DOWN_MASK;

                // 90 = 'z'
                if (e.getKeyCode() == 90 && shiftctrl == e.getModifiersEx()) {
                    undoManager.redo();
                    return;
                }

                // 90 = 'z'
                if (e.getKeyCode() == 90 && ctrl == e.getModifiersEx()) {
                    undoManager.undo();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    int pos = editor.getCaretPosition();

                    insertMode = !insertMode;
                    if (insertMode) {
                        editor.setCaret(insertCaret);
                        editMode.setText("INS");
                    } else {
                        editor.setCaret(overlayCaret);
                        editMode.setText("OVL");
                    }
                    editor.setCaretPosition(pos);
                    for (int i = 0; i < editor.getCaretListeners().length; ++i) {
                        CaretListener [] cl = editor.getCaretListeners();
                        if (cl[i] instanceof EditorCaretListener) {
                            //trigger the caret update after we re-add the listener
                            ((EditorCaretListener) cl[i]).caretUpdate();
                        }
                    }

                }
            }
        });

        /*
        * Key Listener
        */
        KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                int keyCode = event.getKeyCode();
                String keyText = KeyEvent.getKeyText(keyCode);

                rhinoOutput.setText(keyText);

            }
        };
        editMode.addKeyListener(keyListener);

        ActionListener validateButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                	System.out.println("validator button press");
                	
                    // get input data for sending
                    String input = editor.getText();

                    // send data to the servlet
                    URLConnection con = getServletConnection();
                    OutputStream outstream = con.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(outstream);
                    oos.writeObject(input);
                    oos.flush();
                    oos.close();

                    // receive result from servlet
                    InputStream instr = con.getInputStream();
                    ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
                    String result = (String) inputFromServlet.readObject();
                    inputFromServlet.close();
                    instr.close();
                    System.out.println(result);
                    
                    // show result
                    rhinoOutput.setText(result);

                    System.out.println("syntax check button pressed.");
                } catch (Exception e) {
                	e.printStackTrace();
                	
                	rhinoOutput.setText(e.toString());
                }
            }
        };
        
        validateButton.addActionListener(validateButtonListener);
    }

    /**
     * Get a connection to the servlet.
     */
    private URLConnection getServletConnection() throws MalformedURLException, IOException {
    	URL servletURL = new URL(getCodeBase().getProtocol(), getCodeBase().getHost(), getCodeBase().getPort(), scriptValidatorServlet);
    	URLConnection connection = servletURL.openConnection();
    	connection.setDoInput(true);
    	connection.setDoOutput(true);
    	connection.setUseCaches(false);
    	connection.setDefaultUseCaches(false);
//    	connection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
    	connection.setRequestProperty("Content-Type", "text/plain");
    	
    	return connection;
    }

    public static void main(String[] args) {
        Editor e = new Editor();

        e.mainTest();
    }

    private void mainTest() {
        JFrame frame = new JFrame();

        editor.setStyledDocument(new SyntaxDocument("javascript"));
        editorInit();

        frame.getContentPane().add(mainContainer);

        frame.setPreferredSize(new Dimension(500, 500));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /*
      *  Paint a horizontal line the width of a column and 1 pixel high
      */
    private class OvertypeCaret extends DefaultCaret {
        /*
           *  The overtype caret will simply be a horizontal line one pixel high
           *  (once we determine where to paint it)
           */
        public void paint(Graphics g) {
            if (isVisible()) {
                try {
                    JTextComponent component = getComponent();
                    TextUI mapper = component.getUI();
                    Rectangle r = mapper.modelToView(component, getDot());
                    g.setColor(component.getCaretColor());
                    int width = g.getFontMetrics().charWidth('w');
                    int y = r.y + r.height - 2;
                    g.drawLine(r.x, y, r.x + width - 2, y);
                }
                catch (BadLocationException e) {
                }
            }
        }

        /*
           *  Damage must be overridden whenever the paint method is overridden
           *  (The damaged area is the area the caret is painted in. We must
           *  consider the area for the default caret and this caret)
           */
        protected synchronized void damage(Rectangle r) {
            if (r != null) {
                JTextComponent component = getComponent();
                x = r.x;
                y = r.y;
                width = component.getFontMetrics(component.getFont()).charWidth('w');
                height = r.height;
                repaint();
            }
        }
    }
}
