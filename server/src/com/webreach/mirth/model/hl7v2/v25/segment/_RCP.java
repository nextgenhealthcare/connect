package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RCP extends Segment {
	public _RCP(){
		fields = new Class[]{_ID.class, _CQ.class, _CE.class, _TS.class, _ID.class, _SRT.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Query Priority", "Quantity Limited Request", "Response Modality", "Execution and Delivery Time", "Modify Indicator", "Sort-by Field", "Segment Group Inclusion"};
		description = "Response Control Parameter";
		name = "RCP";
	}
}
