package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _NDL extends Composite {
	public _NDL(){
		fields = new Class[]{_CNN.class, _TS.class, _TS.class, _IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Name", "Start Date/Time", "End Date/Time", "Point of Care", "Room", "Bed", "Facility", "Location Status", "Patient Location Type", "Building", "Floor"};
		description = "Name with Date and Location";
		name = "NDL";
	}
}
