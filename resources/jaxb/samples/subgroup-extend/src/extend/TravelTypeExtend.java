/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package extend;

import org.example.impl.TravelTypeImpl;

public class TravelTypeExtend extends TravelTypeImpl {
    public void printTravelSummary() {
	System.out.println("Origin=" + getOrigin());
	System.out.println("Destination=" + getDestination());
    }
}
