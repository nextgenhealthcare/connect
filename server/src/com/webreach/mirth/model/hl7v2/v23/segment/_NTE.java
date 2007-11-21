package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NTE extends Segment {
	public _NTE(){
		fields = new Class[]{_SI.class, _ID.class, _FT.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Source of Comment", "Comment"};
		description = "Notes and Comments";
		name = "NTE";
	}
}
