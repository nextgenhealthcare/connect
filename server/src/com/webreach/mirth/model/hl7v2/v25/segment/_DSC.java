package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSC extends Segment {
	public _DSC(){
		fields = new Class[]{_ST.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Continuation Pointer", "Continuation Style"};
		description = "Continuation Pointer";
		name = "DSC";
	}
}
