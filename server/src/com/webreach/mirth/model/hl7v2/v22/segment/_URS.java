package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _URS extends Segment {
	public _URS(){
		fields = new Class[]{_ST.class, _TS.class, _TS.class, _ST.class, _ST.class, _ID.class, _ID.class, _ID.class};
		repeats = new int[]{-1, 0, 0, -1, -1, -1, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"R/U Where Subject Definition", "R/U When Data Start Date/Time", "R/U When Data End Date/Time", "R/U What User Qualifier", "R/U Other Results Subject Definition", "Which Date/Time Qualifier", "Which Date/Time Status Qualifier", "Date/Time Selection Qualifier"};
		description = "Unsolicited Selection";
		name = "URS";
	}
}
