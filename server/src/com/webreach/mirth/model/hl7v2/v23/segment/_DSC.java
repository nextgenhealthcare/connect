package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSC extends Segment {
	public _DSC(){
		fields = new Class[]{_ST.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Continuation Pointer"};
		description = "Continuation Pointer";
		name = "DSC";
	}
}
