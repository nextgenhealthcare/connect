package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EQL extends Segment {
	public _EQL(){
		fields = new Class[]{_ST.class, _ID.class, _CE.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Query Tag", "Query/ Response Format Code", "EQL Query Name", "EQL Query"};
		description = "Embedded Query Language";
		name = "EQL";
	}
}
