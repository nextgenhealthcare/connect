package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _ELD extends Composite {
	public _ELD(){
		fields = new Class[]{_ST.class, _NM.class, _NM.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Segment ID", "Segment Sequence", "Field Position", "Code Identifying Error"};
		description = "Error Location and Description";
		name = "ELD";
	}
}
