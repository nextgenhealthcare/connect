package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RMI extends Segment {
	public _RMI(){
		fields = new Class[]{_CE.class, _TS.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Risk Management Incident Code", "Date/Time Incident", "Incident Type Code"};
		description = "Risk Management Incident";
		name = "RMI";
	}
}
