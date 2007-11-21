package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MRG extends Segment {
	public _MRG(){
		fields = new Class[]{_CK.class, _CK.class, _CK.class, _CK.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Prior Patient ID - Internal", "Prior Alternate Patient ID", "Prior Patient Account Number", "Prior Patient ID - External"};
		description = "Merge Patient Information";
		name = "MRG";
	}
}
