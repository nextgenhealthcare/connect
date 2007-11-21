package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PL extends Composite {
	public _PL(){
		fields = new Class[]{_IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class, _ST.class, _EI.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Point of Care", "Room", "Bed", "Facility", "Location Status", "Person Location Type", "Building", "Floor", "Location Description", "Comprehensive Location Identifier", "Assigning Authority For Location"};
		description = "Person Location";
		name = "PL";
	}
}
