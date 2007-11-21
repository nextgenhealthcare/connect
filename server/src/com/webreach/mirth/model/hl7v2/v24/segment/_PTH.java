package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PTH extends Segment {
	public _PTH(){
		fields = new Class[]{_ID.class, _CE.class, _EI.class, _TS.class, _CE.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Action Code", "Pathway ID", "Pathway Instance ID", "Pathway Established Date/Time", "Pathway Life Cycle Status", "Change Pathway Life Cycle Status Date/Time"};
		description = "Pathway";
		name = "PTH";
	}
}
