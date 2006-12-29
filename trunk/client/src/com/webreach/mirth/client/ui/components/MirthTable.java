/*
 * MirthTable.java
 *
 * Created on October 18, 2006, 10:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.jdesktop.swingx.JXTable;

import com.webreach.mirth.client.ui.PlatformUI;

/**
 *
 * @author brendanh
 */
public class MirthTable extends JXTable
{
    
    /** Creates a new instance of MirthTable */
    public MirthTable()
    {
        super();
        this.setDragEnabled(true);
        this.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()){
					PlatformUI.MIRTH_FRAME.doSaveChanges();
				}
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
    }
    
}
