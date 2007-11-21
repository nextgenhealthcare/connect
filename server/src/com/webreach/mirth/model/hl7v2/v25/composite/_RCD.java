package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RCD extends Composite {
	public _RCD(){
		fields = new Class[]{_ST.class, _ID.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Segment Field Name", "HL7 Data Type", "Maximum Column Width"};
		description = "Row Column Definition";
		name = "RCD";
	}
}
