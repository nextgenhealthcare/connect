package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACC extends Segment {
	public _ACC(){
		fields = new Class[]{_TS.class, _CE.class, _ST.class, _CE.class, _ID.class, _ID.class, _XCN.class, _ST.class, _ST.class, _ID.class, _XAD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Accident Date/Time", "Accident Code", "Accident Location", "Auto Accident State", "Accident Job Related Indicator", "Accident Death Indicator", "Entered By", "Accident Description", "Brought In By", "Police Notified Indicator", "Accident Address"};
		description = "Accident";
		name = "ACC";
	}
}
