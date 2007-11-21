package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADD extends Segment {
	public _ADD(){
		fields = new Class[]{_ST.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Addendum Continuation Pointer"};
		description = "Addendum";
		name = "ADD";
	}
}
