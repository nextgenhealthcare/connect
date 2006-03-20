/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package extend;

import org.example.impl.PlaneTypeImpl;

public class PlaneTypeExtend extends PlaneTypeImpl {
    public void printTravelSummary() {
	super.printTravelSummary();
	System.out.println("Flight Number: " + getFlightNumber());
	System.out.println("Meal: " + getMeal());
    }
}
