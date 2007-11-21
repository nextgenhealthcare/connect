package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERR extends Segment {
	public _ERR(){
		fields = new Class[]{_ID.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Error Code and Location"};
		description = "Error";
		name = "ERR";
	}
}
