/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package extend;

import org.example.impl.AutoTypeImpl;

public class AutoTypeExtend extends AutoTypeImpl {
    public void printTravelSummary() {
	super.printTravelSummary();
	System.out.println("Rental Agency:" + getRentalAgency());
	System.out.println("Rate Per Hour:" + getRatePerHour());
    }
}
