package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRF extends Segment {
	public _QRF(){
		fields = new Class[]{_ST.class, _TS.class, _TS.class, _ST.class, _ST.class, _ID.class, _ID.class, _ID.class, _TQ.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Where Subject Filter", "When Data Start Date/Time", "When Data End Date/Time", "What User Qualifier", "Other QRY Subject Filter", "Which Date/Time Qualifier", "Which Date/Time Status Qualifier", "Date/Time Selection Qualifier", "When Quantity/Timing Qualifier"};
		description = "Original style Query filter";
		name = "QRF";
	}
}
