package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ODS extends Segment {
	public _ODS(){
		fields = new Class[]{_ID.class, _CE.class, _CE.class, _ST.class};
		repeats = new int[]{0, 10, 20, 2};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Type", "Service Period", "Diet, Supplement, or Preference Code", "Text Instruction"};
		description = "Dietary Orders, Supplements, and Preferences";
		name = "ODS";
	}
}
