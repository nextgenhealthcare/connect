/*
 * $Id: Define.java,v 1.3 2005/10/20 06:57:29 danreese Exp $
 *
 * NSIS Ant Task
 * Copyright (c) 2004 Wayne Grant
 * Copyright (c) 2005 Daniel L. Reese
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sf.nsisant;

/**
 * Represents an NSIS definition in the context of the build script.
 */
public class Define
{
	// Fields
	/** Stores the name attribute */
	private String m_sName;

	/** Stores the value attribute */
	private String m_sValue;

	// Methods
	/**
	 * Set the name attribute.
	 * 
	 * @param sName the name attribute
	 */
	public void setName(String sName)
	{
		m_sName = sName;
	}

	/**
	 * Get the name attribute. Deliberately package private.
	 * 
	 * @return the name attribute
	 */
	String getName()
	{
		return m_sName;
	}

	/**
	 * Set the value attribute.
	 * 
	 * @param sValue the value attribute
	 */
	public void setValue(String sValue)
	{
		m_sValue = sValue;
	}

	/**
	 * Get the value attribute.
	 * 
	 * @return the value attribute
	 */
	String getValue()
	{
		return m_sValue;
	}
}
