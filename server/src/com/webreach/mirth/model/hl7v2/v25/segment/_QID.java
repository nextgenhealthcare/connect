package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QID extends Segment {
	public _QID(){
		fields = new Class[]{_ST.class, _CE.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Query Tag", "Message Query Name"};
		description = "Query Identification";
		name = "QID";
	}
}
