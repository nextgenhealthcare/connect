package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RMC extends Composite {
	public _RMC(){
		fields = new Class[]{_IS.class, _IS.class, _NM.class, _MOP.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Room Type", "Amount Type", "Coverage Amount", "Money or Percentage"};
		description = "Room Coverage";
		name = "RMC";
	}
}
