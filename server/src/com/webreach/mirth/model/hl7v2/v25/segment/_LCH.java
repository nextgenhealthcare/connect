package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LCH extends Segment {
	public _LCH(){
		fields = new Class[]{_PL.class, _ID.class, _EI.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Segment Action Code", "Segment Unique Key", "Location Characteristic ID", "Location Characteristic Value"};
		description = "Location Characteristic";
		name = "LCH";
	}
}
