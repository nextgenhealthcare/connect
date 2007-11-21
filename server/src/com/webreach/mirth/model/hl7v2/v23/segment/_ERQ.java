package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERQ extends Segment {
	public _ERQ(){
		fields = new Class[]{_ST.class, _CE.class, _QIP.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Query Tag", "Event Identifier", "Input Parameter List"};
		description = "Event Replay Query Segment";
		name = "ERQ";
	}
}
