package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DLT extends Composite {
	public _DLT(){
		fields = new Class[]{_NR.class, _NM.class, _ST.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Range", "Numeric Threshold", "Change", "Length of Time-days"};
		description = "Delta Check";
		name = "DLT";
	}
}
