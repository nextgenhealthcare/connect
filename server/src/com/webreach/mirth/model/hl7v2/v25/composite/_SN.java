package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SN extends Composite {
	public _SN(){
		fields = new Class[]{_ST.class, _NM.class, _ST.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Comparator", "Num1", "Separator/Suffix", "Num2"};
		description = "Structured Numeric";
		name = "SN";
	}
}
