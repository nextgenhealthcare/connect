package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QPD extends Segment {
	public _QPD(){
		fields = new Class[]{_CE.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Message Query Name", "Query Tag", "User Parameters (in successive fields)"};
		description = "Query Parameter Definition";
		name = "QPD";
	}
}
