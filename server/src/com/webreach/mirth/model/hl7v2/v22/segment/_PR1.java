package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PR1 extends Segment {
	public _PR1(){
		fields = new Class[]{_SI.class, _ID.class, _ID.class, _ST.class, _TS.class, _ID.class, _NM.class, _CN.class, _ID.class, _NM.class, _CN.class, _CM.class, _ID.class, _NM.class};
		repeats = new int[]{0, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID procedure", "Procedure Coding method", "Procedure Code", "Procedure Description", "Procedure Date/Time", "Procedure Type", "Procedure Minutes", "Anesthesiologist", "Anesthesia Code", "Anesthesia Minutes", "Surgeon", "Procedure MD", "Consent Code", "Procedure Priority"};
		description = "Procedures";
		name = "PR1";
	}
}
