package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EQU extends Segment {
	public _EQU(){
		fields = new Class[]{_EI.class, _TS.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Equipment Instance Identifier", "Event Date/Time", "Equipment State", "Local/Remote Control State", "Alert Level"};
		description = "Equipment Detail";
		name = "EQU";
	}
}
