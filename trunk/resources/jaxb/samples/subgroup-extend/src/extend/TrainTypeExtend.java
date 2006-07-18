/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package extend;

import org.example.impl.TrainTypeImpl;

public class TrainTypeExtend extends TrainTypeImpl {
    public void printTravelSummary() {
	super.printTravelSummary();
        System.out.println("Track: " + getTrack());
        System.out.println("Schedule# " + getDailyScheduleNumber());
    }
}
