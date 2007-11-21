package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSP extends Segment {
	public _DSP(){
		fields = new Class[]{_SI.class, _SI.class, _TX.class, _ST.class, _TX.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Display Level", "Data Line", "Logical Break Point", "Result ID"};
		description = "Display Data";
		name = "DSP";
	}
}
