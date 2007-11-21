package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LRL extends Segment {
	public _LRL(){
		fields = new Class[]{_PL.class, _ID.class, _EI.class, _CE.class, _XON.class, _PL.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Segment Action Code", "Segment Unique Key", "Location Relationship ID", "Organizational Location Relationship Value", "Patient Location Relationship Value"};
		description = "Location Relationship";
		name = "LRL";
	}
}
