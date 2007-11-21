package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERR extends Segment {
	public _ERR(){
		fields = new Class[]{_ELD.class, _ERL.class, _CWE.class, _ID.class, _CWE.class, _ST.class, _TX.class, _TX.class, _IS.class, _CWE.class, _CWE.class, _XTN.class};
		repeats = new int[]{-1, -1, 0, 0, 0, -1, 0, 0, -1, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Error Code and Location", "Error Location", "HL7 Error Code", "Severity", "Application Error Code", "Application Error Parameter", "Diagnostic Information", "User Message", "Inform Person Indicator", "Override Type", "Override Reason Code", "Help Desk Contact Point"};
		description = "Error";
		name = "ERR";
	}
}
