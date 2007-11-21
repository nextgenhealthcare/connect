package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXC extends Segment {
	public _RXC(){
		fields = new Class[]{_ID.class, _CE.class, _NM.class, _CE.class, _NM.class, _CE.class, _CE.class, _NM.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Rx Component Type", "Component Code", "Component Amount", "Component Units", "Component Strength", "Component Strength Units", "Supplementary Code", "Component Drug Strength Volume", "Component Drug Strength Volume Units"};
		description = "Pharmacy/Treatment Component Order";
		name = "RXC";
	}
}
