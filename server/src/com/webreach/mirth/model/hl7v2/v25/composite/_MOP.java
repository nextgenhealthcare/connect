package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MOP extends Composite {
	public _MOP(){
		fields = new Class[]{_ID.class, _NM.class, _ID.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Money or Percentage Indicator", "Money or Percentage Quantity", "Currency Denomination"};
		description = "Money or Percentage";
		name = "MOP";
	}
}
