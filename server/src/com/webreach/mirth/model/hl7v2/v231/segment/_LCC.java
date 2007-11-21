package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LCC extends Segment {
	public _LCC(){
		fields = new Class[]{_PL.class, _IS.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Location Department", "Accommodation Type", "Charge Code"};
		description = "Location Charge Code";
		name = "LCC";
	}
}
