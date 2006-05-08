/*
 * MyRenderer.java
 *
 * Created on May 3, 2006, 9:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MyRenderer extends DefaultTableCellRenderer {

  /*
   * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
   */
  ImageIcon icon;
  public MyRenderer(String img)
  {
      icon = new ImageIcon(getClass().getResource(img));
      setHorizontalAlignment( CENTER );
  }
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
  {
    setIcon(icon);
    return this;
  }
}
