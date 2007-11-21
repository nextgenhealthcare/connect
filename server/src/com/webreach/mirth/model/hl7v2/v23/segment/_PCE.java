package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCE extends Segment {
	public _PCE(){
		fields = new Class[]{_CX.class, _ST.class, _ST.class, _ST.class, _ST.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Position ID", "ExceptionType", "Value", "SourceID", "Recommendation", "Revised By"};
		description = "Exception Segment";
		name = "PCE";
	}
}
