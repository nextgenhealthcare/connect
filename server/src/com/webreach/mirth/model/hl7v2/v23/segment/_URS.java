package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _URS extends Segment {
	public _URS(){
		fields = new Class[]{_ST.class, _TS.class, _TS.class, _ST.class, _ST.class, _ID.class, _ID.class, _ID.class, _TQ.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"R/U Where Subject Definition", "R/U When Data Start Date/Time", "R/U When Data End Date/Time", "R/U What User Qualifier", "R/U Other Results Subject Definition", "R/U Which Date/Time Qualifier", "R/U Which Date/Time Status Qualifier", "R/U Date/Time Selection Qualifier", "R/U Quantity/Timing Qualifier"};
		description = "Unsolicited Selection";
		name = "URS";
	}
}
