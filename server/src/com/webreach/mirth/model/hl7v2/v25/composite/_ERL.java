package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _ERL extends Composite {
	public _ERL(){
		fields = new Class[]{_ST.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Segment ID", "Segment Sequence", "Field Position", "Field Repetition", "Component Number", "Sub-component Number"};
		description = "Error Location";
		name = "ERL";
	}
}
