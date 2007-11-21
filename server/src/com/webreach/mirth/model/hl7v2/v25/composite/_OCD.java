package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _OCD extends Composite {
	public _OCD(){
		fields = new Class[]{_CNE.class, _DT.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Occurrence Code", "Occurrence Date"};
		description = "Occurrence Code and Date";
		name = "OCD";
	}
}
