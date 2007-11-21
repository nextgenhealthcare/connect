package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ECR extends Segment {
	public _ECR(){
		fields = new Class[]{_CE.class, _TS.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Command Response", "Date/Time Completed", "Command Response Parameters"};
		description = "Equipment Command Response";
		name = "ECR";
	}
}
