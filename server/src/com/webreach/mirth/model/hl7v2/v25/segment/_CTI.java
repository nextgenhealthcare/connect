package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CTI extends Segment {
	public _CTI(){
		fields = new Class[]{_EI.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Sponsor Study ID", "Study Phase Identifier", "Study Scheduled Time Point"};
		description = "Clinical Trial Identification";
		name = "CTI";
	}
}
