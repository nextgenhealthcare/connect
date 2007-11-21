package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MRG extends Segment {
	public _MRG(){
		fields = new Class[]{_CK.class, _CK.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Prior Patient ID - Internal", "Prior Alt Patient ID", "Prior Patient Account #"};
		description = "Merge Patient Information";
		name = "MRG";
	}
}
