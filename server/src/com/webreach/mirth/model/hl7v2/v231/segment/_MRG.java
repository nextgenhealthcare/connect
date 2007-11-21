package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MRG extends Segment {
	public _MRG(){
		fields = new Class[]{_CX.class, _CX.class, _CX.class, _CX.class, _CX.class, _CX.class, _XPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Prior Patient Identifier List", "Prior Alternate Patient ID", "Prior Patient Account Number", "Prior Patient ID", "Prior Visit Number", "Prior Alternate Visit ID", "Prior Patient Name"};
		description = "Merge Patient Information";
		name = "MRG";
	}
}
