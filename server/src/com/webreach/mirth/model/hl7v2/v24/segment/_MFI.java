package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFI extends Segment {
	public _MFI(){
		fields = new Class[]{_CE.class, _HD.class, _ID.class, _TS.class, _TS.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Master File Identifier", "Master File Application Identifier", "File-Level Event Code", "Entered Date/Time", "Effective Date/Time", "Response Level Code"};
		description = "Master File Identification";
		name = "MFI";
	}
}
