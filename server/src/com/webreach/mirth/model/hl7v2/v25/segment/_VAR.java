package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VAR extends Segment {
	public _VAR(){
		fields = new Class[]{_EI.class, _TS.class, _TS.class, _XCN.class, _CE.class, _ST.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Variance Instance ID", "Documented Date/Time", "Stated Variance Date/Time", "Variance Originator", "Variance Classification", "Variance Description"};
		description = "Variance";
		name = "VAR";
	}
}
