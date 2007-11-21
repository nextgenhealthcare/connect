package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFE extends Segment {
	public _MFE(){
		fields = new Class[]{_ID.class, _ST.class, _TS.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Record-level Event Code", "MFN Control ID", "Effective Date/Time", "Primary Key Value", "Primary Key Value Type"};
		description = "Master File Entry";
		name = "MFE";
	}
}
