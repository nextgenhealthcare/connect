package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _OSP extends Composite {
	public _OSP(){
		fields = new Class[]{_CNE.class, _DT.class, _DT.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Occurrence Span Code", "Occurrence Span Start Date", "Occurrence Span Stop Date"};
		description = "Occurrence Span Code and Date";
		name = "OSP";
	}
}
