package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PR1 extends Segment {
	public _PR1(){
		fields = new Class[]{_SI.class, _ID.class, _ID.class, _ST.class, _TS.class, _ID.class, _NM.class, _CN.class, _ID.class, _NM.class, _CN.class, _CN.class, _ID.class};
		repeats = new int[]{-1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Procedure Coding Method", "Procedure Code", "Procedure Description", "Procedure Date/Time", "Procedure Type", "Procedure Minutes", "Anesthesiologist", "Anesthesia Code", "Anesthesia Minutes", "Surgeon", "Resident Code", "Consent Code"};
		description = "Procedures";
		name = "PR1";
	}
}
