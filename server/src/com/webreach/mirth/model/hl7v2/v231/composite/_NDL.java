package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _NDL extends Composite {
	public _NDL(){
		fields = new Class[]{_CN.class, _TS.class, _TS.class, _IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Name", "Start Date/Time", "End Date/Time", "Point of Care (IS)", "Room", "Bed", "Facility (HD)", "Location Status", "Person Location Type", "Building", "Floor"};
		description = "Observing Practitioner";
		name = "NDL";
	}
}
