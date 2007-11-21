package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _LA1 extends Composite {
	public _LA1(){
		fields = new Class[]{_IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class, _AD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Point of Care (IS)", "Room", "Bed", "Facility (HD)", "Location Status", "Person Location Type", "Building", "Floor", "Address"};
		description = "Location with Address Information (Variant 1)";
		name = "LA1";
	}
}
