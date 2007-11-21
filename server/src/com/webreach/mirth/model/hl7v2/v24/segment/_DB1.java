package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DB1 extends Segment {
	public _DB1(){
		fields = new Class[]{_SI.class, _IS.class, _CX.class, _ID.class, _DT.class, _DT.class, _DT.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Disabled Person Code", "Disabled Person Identifier", "Disability Indicator", "Disability Start Date", "Disability End Date", "Disability Return to Work Date", "Disability Unable to Work Date"};
		description = "Disability";
		name = "DB1";
	}
}
