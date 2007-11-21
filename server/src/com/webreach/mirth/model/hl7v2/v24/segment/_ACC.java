package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACC extends Segment {
	public _ACC(){
		fields = new Class[]{_TS.class, _CE.class, _ST.class, _CE.class, _ID.class, _ID.class, _XCN.class, _ST.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Accident Date/Time", "Accident Code", "Accident Location", "Auto Accident State", "Accident Job Related Indicator", "Accident Death Indicator", "Entered By", "Accident Description", "Brought In By", "Police Notified Indicator"};
		description = "Accident";
		name = "ACC";
	}
}
