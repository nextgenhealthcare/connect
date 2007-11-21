package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OBX extends Segment {
	public _OBX(){
		fields = new Class[]{_SI.class, _ID.class, _CE.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _NM.class, _ID.class, _ID.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Value Type", "Observation Identifier", "Observation Sub-ID", "Observation Results", "Units", "Reference Range", "Abnormal Flags", "Probability", "Nature of Abnormal Test", "Observ Result Status", "Date Last Normal Value"};
		description = "Observation/Result";
		name = "OBX";
	}
}
