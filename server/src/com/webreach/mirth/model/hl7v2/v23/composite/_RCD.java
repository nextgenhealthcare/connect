package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RCD extends Composite {
	public _RCD(){
		fields = new Class[]{_ST.class, _ST.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Item Number", "Hl7 Data Type", "Maximum Column Width"};
		description = "Row Column Definition";
		name = "RCD";
	}
}
