package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PTA extends Composite {
	public _PTA(){
		fields = new Class[]{_IS.class, _IS.class, _NM.class, _MOP.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Policy Type", "Amount Class", "Money or Percentage Quantity", "Money or Percentage"};
		description = "Policy Type and Amount";
		name = "PTA";
	}
}
