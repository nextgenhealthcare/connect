package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FHS extends Segment {
	public _FHS(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _TS.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"File Field Separator", "File Encoding Characters", "File Sending Application", "File Sending Facility", "File Receiving Application", "File Receiving Facility", "File Creation Date/Time", "File Security", "File Name/ID", "File Header Comment", "File Control ID", "Reference File Control ID"};
		description = "File Header";
		name = "FHS";
	}
}
