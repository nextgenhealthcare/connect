package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ECR extends Segment {
	public _ECR(){
		fields = new Class[]{_CE.class, _TS.class, _TX.class};
		repeats = new int[]{0, 0, -1};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Command Response", "Date/Time Completed", "Command Response Parameters"};
		description = "Equipment Command Response";
		name = "ECR";
	}
}
