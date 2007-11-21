package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SPR extends Segment {
	public _SPR(){
		fields = new Class[]{_ST.class, _ID.class, _CE.class, _QIP.class};
		repeats = new int[]{0, 0, 0, -1};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Query Tag", "Query/Response Format Code", "Stored Procedure Name", "Input Parameter List"};
		description = "Stored Procedure Request Definition";
		name = "SPR";
	}
}
