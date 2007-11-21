package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RPT extends Composite {
	public _RPT(){
		fields = new Class[]{_CWE.class, _ID.class, _NM.class, _NM.class, _NM.class, _IS.class, _ID.class, _ID.class, _NM.class, _IS.class, _GTS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Repeat Pattern Code", "Calendar Alignment", "Phase Range Begin Value", "Phase Range End Value", "Period Quantity", "Period Units", "Institution Specified Time", "Event", "Event Offset Quantity", "Event Offset Units", "General Timing Specification"};
		description = "Repeat Pattern";
		name = "RPT";
	}
}
