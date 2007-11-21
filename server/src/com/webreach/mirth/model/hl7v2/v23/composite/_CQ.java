package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CQ extends Composite {
	public _CQ(){
		fields = new Class[]{_NM.class, _CE.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Quantity", "Units"};
		description = "Composite Quantity with Units";
		name = "CQ";
	}
}
