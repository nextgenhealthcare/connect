package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFE extends Segment {
	public _MFE(){
		fields = new Class[]{_ID.class, _ST.class, _TS.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Record-Level Event Code", "MFN Control ID", "Effective Date/Time", "Primary Key Value"};
		description = "Master File Entry";
		name = "MFE";
	}
}
