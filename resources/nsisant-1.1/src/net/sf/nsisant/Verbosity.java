/*
 * $Id: Verbosity.java,v 1.1 2005/10/20 06:57:29 danreese Exp $
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

import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Enumerated attribute for the verbosity attribute of the task.
 */
public class Verbosity extends EnumeratedAttribute
{
	// Constants
	protected static final String[] VALUES = new String[] {
		"0", "1", "2", "3", "4"
	};

	// Methods
	/**
	 * Get enumeration values.
	 * 
	 * @return the array of enumeration values
	 */
	public String[] getValues()
	{
		return VALUES;
	}
}
