package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FHS extends Segment {
	public _FHS(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _TS.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"File Field Separators", "File Encoding Characters", "File Sending Application", "File Sending Facility", "File Rcving Application", "File Receiving Facility", "File Creation Date/Time", "File Security", "File Name/Id/Type", "File Comment", "File Control ID", "Reference File Cntrl ID"};
		description = "File Header";
		name = "FHS";
	}
}
