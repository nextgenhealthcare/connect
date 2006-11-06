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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;

import javax.swing.tree.TreeNode;

/**
 * Package TreeNodes for movement.
 */
public class TreeTransferable implements Transferable {

   private static DataFlavor[] flavors = null;
   private TreeNode data = null;
   private String prefix = "msg";
   private String suffix = "";
   /**
    * @param data the type of Ant element being transferred, e.g., target, task,
    * type, etc.
    */
   public TreeTransferable( TreeNode data, String prefix, String suffix ) {
      this.data = data;
      this.prefix = prefix;
      this.suffix = suffix;
      init();
   }

   /**
    * Set up the supported flavors: DataFlavor.stringFlavor for a raw string containing
    * an Ant element name (e.g. task, target, etc), or an ElementFlavor containing
    * an ElementPanel.
    */
   private void init() {
      try {
         flavors = new DataFlavor[ 1 ];
         flavors[ 0 ] = DataFlavor.stringFlavor;
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   /**
    * @param df the flavor type desired for the data. Acceptable value is
    * DataFlavor.stringFlavor.
    * @return if df is DataFlavor.stringFlavor, returns a raw string containing
    * an Ant element name.
    */
   public Object getTransferData( DataFlavor df ) {
      if ( df == null )
         return null;
   
      if ( data != null ){
         
        StringBuilder sb = new StringBuilder();
        sb.insert(0, prefix);
    	 TreeNode parent = data.getParent();
    	 LinkedList<String> nodeQ = new LinkedList<String>();
         while(parent != null){
        	 nodeQ.add(parent.toString());
        	 parent = parent.getParent();
         }
         if (!nodeQ.isEmpty())
        	 nodeQ.removeLast();
        // if (!nodeQ.isEmpty())
        //	 nodeQ.removeLast();
         while(!nodeQ.isEmpty()) {
        	 sb.append("['" + nodeQ.removeLast() + "']");
         }
         sb.append(suffix);
         return sb.toString();
      }
      return null;
   }

   /**
    * @return an array containing a single ElementFlavor.   
    */
   public DataFlavor[] getTransferDataFlavors() {
      return flavors;
   }

   /**
    * @param df the flavor to check
    * @return true if df is an ElementFlavor
    */
   public boolean isDataFlavorSupported( DataFlavor df ) {
      if ( df == null )
         return false;
      for ( int i = 0; i < flavors.length; i++ ) {
         if ( df.equals( flavors[ i ] ) ) {
            return true;
         }
      }
      return false;
   }
}
