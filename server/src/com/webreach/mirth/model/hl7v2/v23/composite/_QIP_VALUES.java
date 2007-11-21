package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _QIP_VALUES extends Composite {
	public _QIP_VALUES(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Value1", "Value2", "Value3", "Value4", "Value5"};
		description = "Used by the QIP Composite";
		name = "QIP_VALUES";
	}
}
