package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PL extends Composite {
	public _PL(){
		fields = new Class[]{_IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Point of Care", "Room", "Bed", "Facility (HD)", "Location Status", "Person Location Type", "Building", "Floor", "Location Description"};
		description = "Person Location";
		name = "PL";
	}
}
