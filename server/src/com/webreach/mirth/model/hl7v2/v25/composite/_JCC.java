package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _JCC extends Composite {
	public _JCC(){
		fields = new Class[]{_IS.class, _IS.class, _TX.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Job Code", "Job Class", "Job Description Text"};
		description = "Job Code/Class";
		name = "JCC";
	}
}
