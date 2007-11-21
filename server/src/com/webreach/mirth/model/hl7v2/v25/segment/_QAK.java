package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QAK extends Segment {
	public _QAK(){
		fields = new Class[]{_ST.class, _ID.class, _CE.class, _NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Query Tag", "Query Response Status", "Message Query Name", "Hit Count", "This Payload", "Hits Remaining"};
		description = "Query Acknowledgment";
		name = "QAK";
	}
}
