package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LCC extends Segment {
	public _LCC(){
		fields = new Class[]{_PL.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, -1, -1};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Location Department", "Accommodation Type", "Charge Code"};
		description = "Location Charge Code";
		name = "LCC";
	}
}
