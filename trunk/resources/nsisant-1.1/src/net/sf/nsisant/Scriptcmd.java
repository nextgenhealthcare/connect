/*
 * $Id: Scriptcmd.java,v 1.1 2005/10/20 06:57:11 danreese Exp $
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
 * Represents an NSIS command to execute in the context of the build script.
 */
public class Scriptcmd
{
	// Fields
	/** Stores the cmd attribute */
	private String m_sCmd;

	// Methods
	/**
	 * Set the cmd attribute.
	 * 
	 * @param sCmd Attribute value
	 */
	public void setCmd(String sCmd)
	{
		m_sCmd = sCmd;
	}

	/**
	 * Get the cmd attribute. Deliberately package private.
	 * 
	 * @return Attribute value
	 */
	String getCmd()
	{
		return m_sCmd;
	}
}
