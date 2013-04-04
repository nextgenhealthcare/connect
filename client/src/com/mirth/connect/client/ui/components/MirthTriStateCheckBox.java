/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

/**
 * This is a checkbox component that has a third "PARTIAL" state.
 * It is only meant to work with the JGoodies PlasticXPLookAndFeel
 *
 */
public class MirthTriStateCheckBox extends JCheckBox {
	
	// Determine the size of the checkbox. Copied from the PlasticXPLookAndFeel
	private static final int SIZE = LookUtils.IS_LOW_RESOLUTION ? 13 : 15;
	// The length of the box to draw in the checkbox
	private static final int length = 7;
	
	public static final int CHECKED = 0;
	public static final int UNCHECKED = 1;
	public static final int PARTIAL = 2;
	
	public MirthTriStateCheckBox() {
		this(null, UNCHECKED);
	}
	
	public MirthTriStateCheckBox(String text, int initial) {
		super.setText(text);
		setModel(new TriStateModel(initial));
	}

	@Override
	public void paintComponent( Graphics g ) 
	{
	  super.paintComponent( g );
	  
	  int radius = length / 2;
	  
	  // Paint a box in the checkbox if state is PARTIAL
	  if(((TriStateModel) model).getState() == PARTIAL) 
	  {      
	    Graphics2D g2 = (Graphics2D) g;
	    final RenderingHints.Key key = RenderingHints.KEY_ANTIALIASING;
        Object newAAHint = RenderingHints.VALUE_ANTIALIAS_ON;
        Object oldAAHint = g2.getRenderingHint(key);
        if (newAAHint != oldAAHint) {
            g2.setRenderingHint(key, newAAHint);
        } else {
            oldAAHint = null;
        }
        
        drawFill(g2, model.isPressed(), 1, (getHeight() / 2) - (SIZE / 2) + 1, SIZE - 2, SIZE - 2);
	    
	    g2.setColor(isEnabled()
                ? UIManager.getColor("CheckBox.check").brighter().brighter()
                : MetalLookAndFeel.getControlDisabled());   
	    
	    g2.fillRect( radius,  (getHeight() / 2) - radius,  length , length ); 
	    
	    if (oldAAHint != null) {
            g2.setRenderingHint(key, oldAAHint);
        }
	  }    
	}
	
	/**
	 * Draw the fill of the box as if it were armed
	 */
	private void drawFill(Graphics2D g2, boolean pressed, int x, int y, int w, int h) {
		Color upperLeft = MetalLookAndFeel.getControlShadow();
        Color lowerRight = PlasticLookAndFeel.getControlHighlight();
        
        g2.setPaint(new GradientPaint(x, y, upperLeft, x + w, y + h, lowerRight));
        g2.fillRect(x, y, w, h);
    }
	
	public void setState(int state) {
		((TriStateModel) model).setState(state);
	}
	
	public int getState() {
		return ((TriStateModel) model).getState();
	}
	
	@Override
	public void setSelected(boolean selected) {
		((TriStateModel) model).setSelected(selected);
	}
	
	@Override
	public boolean isSelected() {
		return ((TriStateModel) model).isSelected();
	}
	
	
	private class TriStateModel extends ToggleButtonModel {
		protected int state;
		
		public TriStateModel(int state) {
			this.state = state;
		}
		
		@Override
		public boolean isSelected() {
			return state == CHECKED;
		}
		
		public int getState() {
			return state;
		}
		
		public void setState(int state) {
		    this.state = state;
		    fireStateChanged();
	    }
		
		@Override
		public void setSelected(boolean selected) {
			// When the checkbox is clicked, update the tri-state instead of the default state
			switch(state)
	        {
	        case UNCHECKED: 
	        	state = CHECKED;
	        	break;
	        case PARTIAL: 
		        state = CHECKED;
		        break;
	        case CHECKED: 
		        state = UNCHECKED;
		        break;
	        } 
		}
	}
}
