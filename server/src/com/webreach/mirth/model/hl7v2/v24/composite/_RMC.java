package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RMC extends Composite {
	public _RMC(){
		fields = new Class[]{_IS.class, _IS.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Room Type", "Amount Type", "Coverage Amount"};
		description = "Room Coverage";
		name = "RMC";
	}
}
