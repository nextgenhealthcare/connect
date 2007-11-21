package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VTQ extends Segment {
	public _VTQ(){
		fields = new Class[]{_ST.class, _ID.class, _CE.class, _CE.class, _QSC.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Query Tag", "Query/ Response Format Code", "VT Query Name", "Virtual Table Name", "Selection Criteria"};
		description = "Virtual Table Query Request";
		name = "VTQ";
	}
}
