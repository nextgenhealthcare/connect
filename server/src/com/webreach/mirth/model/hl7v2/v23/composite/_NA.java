package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _NA extends Composite {
	public _NA(){
		fields = new Class[]{_NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Value1", "Value2", "Value3", "Value4", "Value5", "Value6", "Value7"};
		description = "Numeric Array";
		name = "NA";
	}
}
