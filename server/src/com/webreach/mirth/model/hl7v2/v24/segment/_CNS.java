package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CNS extends Segment {
	public _CNS(){
		fields = new Class[]{_NM.class, _NM.class, _TS.class, _TS.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Starting Notification Reference Number", "Ending Notification Reference Number", "Starting Notification Date/Time", "Ending Notification Date/Time", "Starting Notification Code", "Ending Notification Code"};
		description = "Clear Notification";
		name = "CNS";
	}
}
