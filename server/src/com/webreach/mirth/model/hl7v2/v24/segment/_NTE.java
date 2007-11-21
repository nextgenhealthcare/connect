package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NTE extends Segment {
	public _NTE(){
		fields = new Class[]{_SI.class, _ID.class, _FT.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Source of Comment", "Comment", "Comment Type"};
		description = "Notes and Comments";
		name = "NTE";
	}
}
