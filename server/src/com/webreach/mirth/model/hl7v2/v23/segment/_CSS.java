package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CSS extends Segment {
	public _CSS(){
		fields = new Class[]{_CE.class, _TS.class, _CE.class};
		repeats = new int[]{0, 0, 3};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Study Scheduled Time Point", "Study Scheduled Patient Time Point", "Study Quality Control Codes"};
		description = "Clinical Study Data Schedule";
		name = "CSS";
	}
}
