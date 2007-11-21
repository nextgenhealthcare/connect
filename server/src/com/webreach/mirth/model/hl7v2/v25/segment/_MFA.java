package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFA extends Segment {
	public _MFA(){
		fields = new Class[]{_ID.class, _ST.class, _TS.class, _CE.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Record-level Event Code", "MFN Control ID", "Event Completion Date/Time", "MFN Record Level Error Return", "Primary Key Value", "Primary Key Value Type"};
		description = "Master File Acknowledgment";
		name = "MFA";
	}
}
