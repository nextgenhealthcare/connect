package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NST extends Segment {
	public _NST(){
		fields = new Class[]{_ID.class, _ST.class, _ID.class, _TS.class, _TS.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Statistics Available", "Source Identifier", "Source Type", "Statistics Start", "Statistics End", "Receive Character Count", "Send Character Count", "Messages Received", "Messages Sent", "Checksum Errors Received", "Length Errors Received", "Other Errors Received", "Connect Timeouts", "Receive Timeouts", "Application control-level Errors"};
		description = "Application control level statistics";
		name = "NST";
	}
}
