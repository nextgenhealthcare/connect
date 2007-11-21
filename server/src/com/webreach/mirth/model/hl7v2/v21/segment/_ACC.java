package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACC extends Segment {
	public _ACC(){
		fields = new Class[]{_TS.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Accident Date/Time", "Accident Code", "Accident Location"};
		description = "Accident";
		name = "ACC";
	}
}
