package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NTE extends Segment {
	public _NTE(){
		fields = new Class[]{_SI.class, _ID.class, _TX.class};
		repeats = new int[]{0, 0, -1};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Source of Comment", "Comment"};
		description = "Notes and Comments";
		name = "NTE";
	}
}
