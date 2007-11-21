package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LAN extends Segment {
	public _LAN(){
		fields = new Class[]{_SI.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, -1, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Language Code", "Language Ability Code", "Language Proficiency Code"};
		description = "Language Detail";
		name = "LAN";
	}
}
