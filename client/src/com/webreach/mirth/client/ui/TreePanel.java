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

package com.webreach.mirth.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.StringReader;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.webreach.mirth.client.ui.util.HL7Reference;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.EDISerializer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.model.converters.X12Serializer;

public class TreePanel extends JPanel
{
    private PipeParser parser;
    private XMLParser xmlParser;
    private String version;
    private EncodingCharacters encodingChars;
    private JTree tree;
    private Logger logger = Logger.getLogger(this.getClass());
    private String _dropPrefix;
    private String _dropSuffix;

    public TreePanel(String prefix, String suffix)
    {
        _dropPrefix = prefix;
        _dropSuffix = suffix;
        parser = new PipeParser();
        parser.setValidationContext(new NoValidation());
        xmlParser = new DefaultXMLParser();
        encodingChars = new EncodingCharacters('|', null);
        this.setLayout(new GridLayout(1, 1));
        this.setBackground(Color.white);
    }

    public void setMessage(Properties protocolProperties, String messageType, String source, String ignoreText, Properties dataProperties)
    {
        Document xmlDoc = null;
        String messageName = "";
        String messageDescription = "";

        source = source.replaceAll("\\n", "\r").trim();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;

        if (source.length() > 0 && !source.equals(ignoreText))
        {
            if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2).equals(messageType))
            {
                Message message = null;
                logger.debug("encoding HL7 message to XML:\n" + message);

                if (source != null && !source.equals(""))
                {
                    // This message might come from a system that doesn't use
                    // carriage returns
                    // Since hapi requires a CR for the end of segment character
                    // we will force it.
                    try
                    {
                        docBuilder = docFactory.newDocumentBuilder();
                        String er7Message = new ER7Serializer().toXML(source);
                        xmlDoc = docBuilder.parse(new InputSource(new StringReader(er7Message)));
                        message = parser.parse(source);
                    }
                    catch (SerializerException e)
                    {
                        // PlatformUI.MIRTH_FRAME.alertWarning( "Encoding not
                        // supported.\n" +
                        // "Please check the syntax of your message\n" +
                        // "and try again.");
                    }
                    catch (EncodingNotSupportedException e)
                    {
                        // PlatformUI.MIRTH_FRAME.alertWarning( "Encoding not
                        // supported.\n" +
                        // "Please check the syntax of your message\n" +
                        // "and try again.");
                    }
                    catch (HL7Exception e)
                    {
                        // PlatformUI.MIRTH_FRAME.alertError( "HL7 Error!\n" +
                        // "Please check the syntax of your message\n" +
                        // "and try again.");
                    }
                    catch (Exception e)
                    {
                        // PlatformUI.MIRTH_FRAME.alertException(e.getStackTrace(),
                        // e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (xmlDoc != null)
                {
                    Terser terser = new Terser(message);
                    version = message.getVersion();
                    try
                    {
                        messageName = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2") + " (" + version + ")";
                        messageDescription = HL7Reference.getInstance().getDescription(terser.get("/MSH-9-1") + terser.get("/MSH-9-2"), version);
                    }
                    catch (HL7Exception e)
                    {
                        // TODO Auto-generated catch block
                        logger.error(e);
                    }
                }
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3).equals(messageType))
            {
                try
                {
                    docBuilder = docFactory.newDocumentBuilder();
                    xmlDoc = docBuilder.parse(new InputSource(new StringReader(source)));
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }

                if (xmlDoc != null)
                {
                    version = "3.0";
                    messageName = xmlDoc.getDocumentElement().getNodeName() + " -" + " (" + version + ")";
                    messageDescription = "";
                }
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12).equals(messageType))
            {
                try
                {
                    docBuilder = docFactory.newDocumentBuilder();
                    String x12message = new X12Serializer(dataProperties).toXML(source);
                    xmlDoc = docBuilder.parse(new InputSource(new StringReader(x12message)));
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                if (xmlDoc != null)
                {
                    messageDescription = "";
                    version = "";
                    String event = "Unknown";
                    if (xmlDoc.getElementsByTagName("ST.1") != null)
                    {
                        Node type = xmlDoc.getElementsByTagName("ST.1").item(0);
                        type = type.getFirstChild();
                        event = type.getNodeValue();
                    }
                    String version = "";
                    if (xmlDoc.getElementsByTagName("GS.8") != null)
                    {
                        Node versionNode = xmlDoc.getElementsByTagName("GS.8").item(0);
                        versionNode = versionNode.getFirstChild();
                        version = versionNode.getNodeValue();
                    }
                    messageName = xmlDoc.getDocumentElement().getNodeName() + " - " + event + " (" + version + ")";
                    messageDescription = "";// HL7Reference.getInstance().getDescription(terser.get("/MSH-9-1")
                    // + terser.get("/MSH-9-2"),
                    // version);
                }
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML).equals(messageType))
            {
                try
                {
                    docBuilder = docFactory.newDocumentBuilder();
                    xmlDoc = docBuilder.parse(new InputSource(new StringReader(source)));
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }
                if (xmlDoc != null)
                {
                    version = "";
                    messageDescription = "";
                    messageName = xmlDoc.getDocumentElement().getNodeName();
                }
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI).equals(messageType))
            {
                try
                {
                    docBuilder = docFactory.newDocumentBuilder();
                    String ediMessage = new EDISerializer(dataProperties).toXML(source);
                    xmlDoc = docBuilder.parse(new InputSource(new StringReader(ediMessage)));
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                if (xmlDoc != null)
                {
                    messageDescription = "";
                    version = "";
                    messageName = xmlDoc.getDocumentElement().getNodeName() + "-" + " (" + version + ")";
                    messageDescription = "";// HL7Reference.getInstance().getDescription(terser.get("/MSH-9-1")
                    // + terser.get("/MSH-9-2"),
                    // version);
                }
            }

            if (xmlDoc != null)
                createTree(xmlDoc, messageName, messageDescription);
            else
                setInvalidMessage(messageType);
        }
        else
            clearMessage();
    }

    /**
     * Updates the panel with a new Message.
     */
    private void createTree(Document xmlDoc, String messageName, String messageDescription)
    {
        PlatformUI.MIRTH_FRAME.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Element el = xmlDoc.getDocumentElement();
        DefaultMutableTreeNode top;
        if (messageDescription.length() > 0)
            top = new DefaultMutableTreeNode(messageName + " (" + messageDescription + ")");
        else
            top = new DefaultMutableTreeNode(messageName);

        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            processElement(children.item(i), top);
        }
        // processElement(xmlDoc.getDocumentElement(), top);
        // addChildren(message, top);

        tree = new JTree(top);
        tree.setDragEnabled(true);
        tree.setTransferHandler(new TreeTransferHandler());
        tree.addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent evt)
            {
                if (tree.getSelectionPath() != null)
                {
                    TreePath tp = tree.getSelectionPath();
                    TreeNode tn = (TreeNode) tp.getLastPathComponent();
                    if (tn.isLeaf())
                    {
                        setHighlighters();
                        refTableMouseDragged(evt);
                    }
                }
            }

            public void mouseMoved(MouseEvent evt)
            {
                refTableMouseMoved(evt);
            }
        });
        tree.addMouseListener(new MouseAdapter()
        {
            public void mouseExited(MouseEvent evt)
            {
                refTableMouseExited(evt);

            }
        });

        removeAll();
        add(tree);
        revalidate();

        PlatformUI.MIRTH_FRAME.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void setHighlighters()
    {
        PlatformUI.MIRTH_FRAME.setHighlighters();
    }

    private void unsetHighlighters()
    {
        PlatformUI.MIRTH_FRAME.unsetHighlighters();
    }

    private void refTableMouseExited(MouseEvent evt)
    {
        tree.clearSelection();
    }

    private void refTableMouseDragged(MouseEvent evt)
    {

    }

    private void refTableMouseMoved(MouseEvent evt)
    {
        int row = tree.getRowForLocation(evt.getPoint().x, evt.getPoint().y);

        if (row >= 0 && row < tree.getRowCount())
            tree.setSelectionRow(row);
    }

    private void processElement(Object elo, DefaultMutableTreeNode dmtn)
    {
        if (elo instanceof Element)
        {
            Element el = (Element) elo;
            String description = HL7Reference.getInstance().getDescription(el.getNodeName(), version);
            DefaultMutableTreeNode currentNode;
            if (description.length() > 0)
                currentNode = new DefaultMutableTreeNode(el.getNodeName() + " (" + description + ")");
            else
                currentNode = new DefaultMutableTreeNode(el.getNodeName());
            String text = "";
            if (el.hasChildNodes())
            {
                text = el.getFirstChild().getNodeValue();
            }
            else
            {
                return;
                // text = el.getTextContent();
            }

            if (text != null)
            {
                text = text.trim();
            }
            if ((text != null) && (!text.equals("")))
            {

                currentNode.add(new DefaultMutableTreeNode(text));
            }

            processAttributes(el, currentNode);

            NodeList children = el.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                processElement(children.item(i), currentNode);
            }
            dmtn.add(currentNode);
        }
    }

    private void processAttributes(Element el, DefaultMutableTreeNode dmtn)
    {
        NamedNodeMap atts = el.getAttributes();
        for (int i = 0; i < atts.getLength(); i++)
        {
            Attr att = (Attr) atts.item(i);
            DefaultMutableTreeNode attNode = new DefaultMutableTreeNode("@" + att.getName());
            attNode.add(new DefaultMutableTreeNode(att.getValue()));
            dmtn.add(attNode);
        }
    }

    public class TreeTransferHandler extends TransferHandler
    {
        protected Transferable createTransferable(JComponent c)
        {
            if (c != null)
            {
                try
                {
                    TreePath path = ((JTree) c).getSelectionPath();
                    if (path == null)
                        return null;
                    TreeNode tp = (TreeNode) path.getLastPathComponent();
                    if (tp == null)
                        return null;
                    if (!tp.isLeaf())
                        return null;
                    String leaf = tp.toString();
                    // if (leaf.equals(DNDConstants.TASK) ||
                    // leaf.equals(DNDConstants.TYPE))
                    // return null;
                    return new TreeTransferable(tp, _dropPrefix, _dropSuffix);
                }
                catch (ClassCastException cce)
                {
                    return null;
                }
            }
            else
                return null;
        }

        public int getSourceActions(JComponent c)
        {
            return COPY;
        }

        public boolean canImport(JComponent c, DataFlavor[] df)
        {
            return false;
        }
    }

    public void clearMessage()
    {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Paste a sample message above to view the message tree.");
        JTree tree = new JTree(top);
        removeAll();
        add(tree);
        revalidate();
    }

    public void setInvalidMessage(String messageType)
    {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("The message pasted above does not appear to be valid " + messageType + ".");
        JTree tree = new JTree(top);
        removeAll();
        add(tree);
        revalidate();
    }
}
