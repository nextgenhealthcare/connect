package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NDS extends Segment {
	public _NDS(){
		fields = new Class[]{_NM.class, _TS.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Notification Reference Number", "Notification Date/Time", "Notification Alert Severity", "Notification Code"};
		description = "Notification Detail";
		name = "NDS";
	}
}
