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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.model.util.MessageVocabularyFactory;

public class TreePanel extends JPanel
{
    private String version = "";
    private String type = "";
    private JTree tree;
    private Logger logger = Logger.getLogger(this.getClass());
    private String _dropPrefix;
    private String _dropSuffix;
    private String messageName;
    private MessageVocabulary vocabulary;
    public TreePanel(String prefix, String suffix)
    {
        _dropPrefix = prefix;
        _dropSuffix = suffix;

        this.setLayout(new GridLayout(1, 1));
        this.setBackground(Color.white);
    }

    public void setMessage(Properties protocolProperties, String messageType, String source, String ignoreText, Properties dataProperties)
    {
    	
        Document xmlDoc = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        
        messageName = "";
        version = "";
        type = "";
        String messageDescription = "";
        Protocol protocol = null;
        
        source = source.replaceAll("\\n", "\r").trim();        
        if (source.length() > 0 && !source.equals(ignoreText))
        {
        	IXMLSerializer<String> serializer;
            if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2).equals(messageType))
            { 
            	protocol = Protocol.HL7V2;
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3).equals(messageType))
            {
            	protocol = Protocol.HL7V3;
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12).equals(messageType))
            {
            	protocol = Protocol.X12;
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML).equals(messageType))
            {
            	protocol = Protocol.XML;
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI).equals(messageType))
            {
            	protocol = Protocol.EDI;
            }else{
            	logger.error("Invalid protocol");
            	return;
            }

            
            try
            {
            	serializer = SerializerFactory.getSerializer(protocol, protocolProperties);
            	docBuilder = docFactory.newDocumentBuilder();
            	String message = serializer.toXML(source);
            	xmlDoc = docBuilder.parse(new InputSource(new StringReader(message)));
           
                if (xmlDoc != null)
                {
                	Map<String, String> metadata = serializer.getMetadataFromDocument(xmlDoc);
                    version = metadata.get("version");
                    type = metadata.get("type");
                    messageName =  type + " (" + version + ")";
                	vocabulary = new MessageVocabularyFactory().getVocabulary(protocol, version, type);
                    if (vocabulary != null);
                    	messageDescription = vocabulary.getDescription(type.replaceAll("-", ""));
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                 e.printStackTrace();

            }
            
            if (xmlDoc != null)
                createTree(xmlDoc, messageName, messageDescription);
            else
                setInvalidMessage(messageType);
            
        }
        else{
            clearMessage();
        }
    }

    /**
     * Updates the panel with a new Message.
     */
    private void createTree(Document xmlDoc, String messageName, String messageDescription)
    {
        

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
        JScrollPane scrollPane = new JScrollPane();
        JViewport viewPort = scrollPane.getViewport();
        
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
        tree.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				 refTableMouseExited(e);
				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				unsetHighlighters();
				
			}
        	
        });
        		
        		
        		
     
    
        tree.setScrollsOnExpand(true);
      
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
            String description = vocabulary.getDescription(el.getNodeName());
            DefaultMutableTreeNode currentNode;
            if (description != null && description.length() > 0)
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

            
            if ((text == null) || (text.equals("") || text.trim().length() == 0))
            {
                currentNode.add(new DefaultMutableTreeNode(el.getNodeName()));
            }else{
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
