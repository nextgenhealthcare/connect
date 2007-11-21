package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _APR extends Segment {
	public _APR(){
		fields = new Class[]{_SCV.class, _SCV.class, _SCV.class, _NM.class, _SCV.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Time Selection Criteria", "Resource Selection Criteria", "Location Selection Criteria", "Slot Spacing Criteria", "Filler Override Criteria"};
		description = "Appointment Preferences";
		name = "APR";
	}
}
