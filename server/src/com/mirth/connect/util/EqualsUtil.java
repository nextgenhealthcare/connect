/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

/**
 * Collected methods which allow easy implementation of <code>equals</code>.
 * 
 * Example use case in a class called Car:
 * 
 * <pre>
 * public boolean equals(Object aThat) {
 * 	if (this == aThat)
 * 		return true;
 * 	if (!(aThat instanceof Car))
 * 		return false;
 * 	Car that = (Car) aThat;
 * 	return EqualsUtil.areEqual(this.fName, that.fName) &amp;&amp; EqualsUtil.areEqual(this.fNumDoors, that.fNumDoors) &amp;&amp; EqualsUtil.areEqual(this.fGasMileage, that.fGasMileage) &amp;&amp; EqualsUtil.areEqual(this.fColor, that.fColor) &amp;&amp; Arrays.equals(this.fMaintenanceChecks, that.fMaintenanceChecks); //array!
 * }
 * </pre>
 * 
 * <em>Arrays are not handled by this class</em>. This is because the
 * <code>Arrays.equals</code> methods should be used for array fields.
 */
public final class EqualsUtil {
	static public boolean areEqual(boolean left, boolean right) {
		return left == right;
	}

	static public boolean areEqual(char left, char right) {
		return left == right;
	}

	static public boolean areEqual(long left, long right) {
		/*
		 * Implementation Note: Note that byte, short, and int are handled by
		 * this method, through implicit conversion.
		 */
		return left == right;
	}

	static public boolean areEqual(float left, float right) {
		return Float.floatToIntBits(left) == Float.floatToIntBits(right);
	}

	static public boolean areEqual(double left, double right) {
		return Double.doubleToLongBits(left) == Double.doubleToLongBits(right);
	}

	/**
	 * Possibly-null object field.
	 * 
	 * Includes type-safe enumerations and collections, but does not include
	 * arrays. See class comment.
	 */
	static public boolean areEqual(Object left, Object right) {
		return left == null ? right == null : left.equals(right);
	}
}