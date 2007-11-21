package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERR extends Segment {
	public _ERR(){
		fields = new Class[]{_CM.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Error Code and Location"};
		description = "Error";
		name = "ERR";
	}
}
