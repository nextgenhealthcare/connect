package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFA extends Segment {
	public _MFA(){
		fields = new Class[]{_ID.class, _ST.class, _TS.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Record-Level Event Code", "MFN Control ID", "Event Completion Date/Time", "Error Return Code and/or Text", "Primary Key Value"};
		description = "Master File Acknowledgement";
		name = "MFA";
	}
}
