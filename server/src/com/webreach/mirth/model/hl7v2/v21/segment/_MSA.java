package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MSA extends Segment {
	public _MSA(){
		fields = new Class[]{_ID.class, _ST.class, _ST.class, _NM.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Acknowledgement Code", "Message Control ID", "Text Message", "Expected Sequence Number", "Delayed Ack Type"};
		description = "Message Acknowledgement";
		name = "MSA";
	}
}
