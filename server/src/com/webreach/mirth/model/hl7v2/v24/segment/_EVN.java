package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EVN extends Segment {
	public _EVN(){
		fields = new Class[]{_ID.class, _TS.class, _TS.class, _IS.class, _XCN.class, _TS.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Event Type Code", "Recorded Date/Time", "Date/Time Planned Event", "Event Reason Code", "Operator ID", "Event Occurred", "Event Facility"};
		description = "Event Type";
		name = "EVN";
	}
}
