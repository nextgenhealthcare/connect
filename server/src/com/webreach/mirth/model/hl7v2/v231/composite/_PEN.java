package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PEN extends Composite {
	public _PEN(){
		fields = new Class[]{_IS.class, _NM.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Penalty Type", "Penalty Amount"};
		description = "Penalty";
		name = "PEN";
	}
}
