package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DDI extends Composite {
	public _DDI(){
		fields = new Class[]{_NM.class, _MO.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Delay Days", "Monetary Amount", "Number of Days"};
		description = "Daily Deductible Information";
		name = "DDI";
	}
}
