package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDT extends Segment {
	public _RDT(){
		fields = new Class[]{_ST.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Column Value"};
		description = "Table Row Data";
		name = "RDT";
	}
}
