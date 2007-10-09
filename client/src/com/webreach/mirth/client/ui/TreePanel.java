/*
 * TreePanel.java
 *
 * Created on July 3, 2007, 3:48 PM
 */

package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.ui.components.MirthTree;
import com.webreach.mirth.client.ui.components.MirthTree.FilterTreeModel;
import com.webreach.mirth.client.ui.components.MirthTreeNode;
import com.webreach.mirth.client.ui.editors.MessageTreePanel;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
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
import com.webreach.mirth.model.dicom.DICOMVocabulary;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.model.util.MessageVocabularyFactory;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.tree.TreeNode;

/**
 *
 * @author  brendanh
 */
public class TreePanel extends javax.swing.JPanel
{
    private static final String EMPTY = "[empty]";
    private String version = "";
    private String type = "";
    private Logger logger = Logger.getLogger(this.getClass());
    private String _dropPrefix;
    private String _dropSuffix;
    private String messageName;
    private MessageVocabulary vocabulary;
    private Timer timer;
    private MessageVocabularyFactory vocabFactory;
    
    /**
     * Creates new form TreePanel
     */
    public TreePanel()
    {
        setup();
    }
    
    public TreePanel(String prefix, String suffix)
    {
        _dropPrefix = prefix;
        _dropSuffix = suffix;
        
        setup();
    }
    
    public void setup()
    {
        initComponents();
        
        filterTextBox.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent arg0)
            {
            }
            
            public void keyReleased(KeyEvent e)
            {
                filterActionPerformed();
            }
            
            public void keyTyped(KeyEvent e)
            {
            }
        });
        
        exact.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                filterActionPerformed();
            }
        });
    }
    
    public void setPrefix(String prefix)
    {
        _dropPrefix = prefix;
    }
    
    public void setSuffix(String suffix)
    {
        _dropSuffix = suffix;
    }
    
    public void setBorderText(String text)
    {
        
    }
    
    public void filterActionPerformed()
    {
        
        class FilterTimer extends TimerTask
        {
            
            @Override
            public void run()
            {
                filter();
            }
            
        }
        
        if (timer == null)
        {
            timer = new Timer();
            timer.schedule(new FilterTimer(), 1000);
        }
        else
        {
            timer.cancel();
            PlatformUI.MIRTH_FRAME.setWorking("", false);
            timer = new Timer();
            timer.schedule(new FilterTimer(), 1000);
        }
    }
    
    public void filter()
    {
        PlatformUI.MIRTH_FRAME.setWorking("Filtering...", true);
        FilterTreeModel model = (FilterTreeModel) tree.getModel();
        
        if(filterTextBox.getText().length() > 0)
            model.setFiltered(true);
        else
            model.setFiltered(false);
        
        model.performFilter(model.getRoot(), filterTextBox.getText(), exact.isSelected(), false);
        model.updateTreeStructure();
        if(filterTextBox.getText().length() > 0)
            tree.expandAll();
        
        PlatformUI.MIRTH_FRAME.setWorking("", false);
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
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP).equals(messageType))
            {
                protocol = Protocol.NCPDP;
            }
            else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DICOM).equals(messageType))
            {
                protocol = Protocol.DICOM;
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
            }
            else
            {
                logger.error("Invalid protocol");
                return;
            }
            
            try
            {
                serializer = SerializerFactory.getSerializer(protocol, protocolProperties);
                docBuilder = docFactory.newDocumentBuilder();

                String message;
                if(protocol.equals(Protocol.DICOM)){
                    DICOMSerializer dicomSerializer = new DICOMSerializer(protocolProperties);
                    message = dicomSerializer.toXML(new File(source));        
                }
                else {
                    message = serializer.toXML(source);
                }
                xmlDoc = docBuilder.parse(new InputSource(new StringReader(message)));
                
                if (xmlDoc != null)
                {
                    Map<String, String> metadata = serializer.getMetadataFromDocument(xmlDoc);
                    version = metadata.get("version");
                    type = metadata.get("type");
                    messageName =  type + " (" + version + ")";
                    vocabulary = vocabFactory.getInstance(PlatformUI.MIRTH_FRAME.mirthClient).getVocabulary(protocol, version, type);
                    messageDescription = vocabulary.getDescription(type.replaceAll("-", ""));
                    
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                
            }
            
            if (xmlDoc != null)
            {
                createTree(protocol, xmlDoc, messageName, messageDescription);
                filter();
            }
            else
                setInvalidMessage(messageType);
        }
        else
        {
            clearMessage();
        }
    }
    
    /**
     * Updates the panel with a new Message.
     */
    private void createTree(Protocol protocol, Document xmlDoc, String messageName, String messageDescription)
    {
        Element el = xmlDoc.getDocumentElement();
        MirthTreeNode top;
        if (messageDescription.length() > 0)
            top = new MirthTreeNode(messageName + " (" + messageDescription + ")");
        else
            top = new MirthTreeNode(messageName);
        
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            processElement(protocol, children.item(i), top);
        }
        // processElement(xmlDoc.getDocumentElement(), top);
        // addChildren(message, top);
        
        tree = new MirthTree(top, _dropPrefix, _dropSuffix);
        
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
                        refTableMouseDragged(evt);
                    }
                }
            }
            
            public void mouseMoved(MouseEvent evt)
            {
                refTableMouseMoved(evt);
            }
        });
        tree.addMouseListener(new MouseListener()
        {
            
            public void mouseClicked(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mouseEntered(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mouseExited(MouseEvent e)
            {
                refTableMouseExited(e);
                
            }
            
            public void mousePressed(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void mouseReleased(MouseEvent e)
            {                
            }
            
        });
        try
        {
            tree.setScrollsOnExpand(true);
            treePane.setViewportView(tree);
            tree.revalidate();
        }
        catch(Exception e)
        {
            logger.error(e);
        }
        PlatformUI.MIRTH_FRAME.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
    
    private void processElement(Protocol protocol, Object elo, MirthTreeNode mtn)
    {
        if (elo instanceof Element)
        {
            Element el = (Element) elo;
            String description;
            if(vocabulary instanceof DICOMVocabulary){
                description = vocabulary.getDescription(el.getAttribute("tag"));
            }
            else {
                description = vocabulary.getDescription(el.getNodeName());
            }
            MirthTreeNode currentNode;
            if (description != null && description.length() > 0) {
                if(vocabulary instanceof DICOMVocabulary){
                    currentNode = new MirthTreeNode(vocabulary.getDescription(el.getAttribute("tag").replaceAll(" ","")) + " (" + description + ")");
                }
                else {
                currentNode = new MirthTreeNode(el.getNodeName() + " (" + description + ")");
                }
            }
            else
                currentNode = new MirthTreeNode(el.getNodeName());
            
            String text = "";
            if (el.hasChildNodes())
            {
                text = el.getFirstChild().getNodeValue();
                if ((text == null) || (text.equals("") || text.trim().length() == 0))
                {
                    currentNode.add(new MirthTreeNode(el.getNodeName()));
                }
                else
                {
                    currentNode.add(new MirthTreeNode(text));
                }
            }
            else
            {
                //Check if we are in the format SEG.1.1
            	if (protocol.equals(Protocol.HL7V3) || protocol.equals(Protocol.XML) || el.getNodeName().matches(".*\\..*\\..")){
            		//We already at the last possible child segment, so just add empty node
            		currentNode.add(new MirthTreeNode(EMPTY));
            	}else{
            		//We have empty node and possibly empty children
            		//Add the sub-node handler (SEG.1)
	            	currentNode.add(new MirthTreeNode(el.getNodeName()));         	
            		//Add a sub node (SEG.1.1)
	            	String newNodeName = el.getNodeName() + ".1";
	            	description = vocabulary.getDescription(newNodeName);
	            	MirthTreeNode parentNode;
	            	if (description != null && description.length() > 0){
	            		parentNode = new MirthTreeNode(newNodeName + " (" + description + ")");
	            	}else{
	            		parentNode = new MirthTreeNode(newNodeName);
	            	}
	            	parentNode.add(new MirthTreeNode(EMPTY));
	            	currentNode.add(parentNode);
            	}
                
            }
                        
            processAttributes(el, currentNode);
            
            NodeList children = el.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                processElement(protocol, children.item(i), currentNode);
            }
            mtn.add(currentNode);
        }
    }
    
    private void processAttributes(Element el, MirthTreeNode dmtn)
    {
        NamedNodeMap atts = el.getAttributes();
        for (int i = 0; i < atts.getLength(); i++)
        {
            Attr att = (Attr) atts.item(i);
            MirthTreeNode attNode = new MirthTreeNode("@" + att.getName());
            attNode.add(new MirthTreeNode(att.getValue()));
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
                    TreePath path = ((MirthTree) c).getSelectionPath();
                    if (path == null)
                        return null;
                    TreeNode tp = (TreeNode) path.getLastPathComponent();
                    if (tp == null)
                        return null;
                    if (!tp.isLeaf())
                        return null;
                    // String leaf = tp.toString();
                    // if (leaf.equals(DNDConstants.TASK) ||
                    // leaf.equals(DNDConstants.TYPE))
                    // return null;
                    
                    if(_dropPrefix.equals(MessageTreePanel.MAPPER_PREFIX))
                        return new TreeTransferable(tp, _dropPrefix, _dropSuffix, TreeTransferable.MAPPER_DATA_FLAVOR);
                    else
                        return new TreeTransferable(tp, _dropPrefix, _dropSuffix, TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
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
        MirthTreeNode top = new MirthTreeNode("Please provide a message template.");
        MirthTree tree = new MirthTree(top, _dropPrefix, _dropSuffix);
        treePane.setViewportView(tree);
        revalidate();
    }
    
    public void setInvalidMessage(String messageType)
    {
        MirthTreeNode top = new MirthTreeNode("The above message is not valid " + messageType + ".");
        MirthTree tree = new MirthTree(top, _dropPrefix, _dropSuffix);
        treePane.setViewportView(tree);
        revalidate();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jLabel1 = new javax.swing.JLabel();
        filterTextBox = new javax.swing.JTextField();
        treePane = new javax.swing.JScrollPane();
        tree = new com.webreach.mirth.client.ui.components.MirthTree();
        exact = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1), "Message Tree", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Filter:");

        treePane.setViewportView(tree);

        exact.setBackground(new java.awt.Color(255, 255, 255));
        exact.setText("Match Exact");
        exact.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exact.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(treePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exact)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(filterTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(exact))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(treePane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox exact;
    private javax.swing.JTextField filterTextBox;
    private javax.swing.JLabel jLabel1;
    private com.webreach.mirth.client.ui.components.MirthTree tree;
    private javax.swing.JScrollPane treePane;
    // End of variables declaration//GEN-END:variables    
}
