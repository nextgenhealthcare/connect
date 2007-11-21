package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FTS extends Segment {
	public _FTS(){
		fields = new Class[]{_ST.class, _CM.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"File Batch Count", "File Trailer Comment"};
		description = "File Trailer";
		name = "FTS";
	}
}
