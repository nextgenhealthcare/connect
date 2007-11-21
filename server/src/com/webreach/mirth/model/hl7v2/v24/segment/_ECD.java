package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ECD extends Segment {
	public _ECD(){
		fields = new Class[]{_NM.class, _CE.class, _ID.class, _TQ.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Reference Command Number", "Remote Control Command", "Response Required", "Requested Completion Time", "Parameters"};
		description = "Equipment Command";
		name = "ECD";
	}
}
