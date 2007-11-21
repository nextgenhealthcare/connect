package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EDU extends Segment {
	public _EDU(){
		fields = new Class[]{_SI.class, _IS.class, _DR.class, _DR.class, _DT.class, _XON.class, _CE.class, _XAD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Academic Degree", "Academic Degree Program Date Range", "Academic Degree Program Participation Date Range", "Academic Degree Granted Date", "School", "School Type Code", "School Address"};
		description = "Educational Detail";
		name = "EDU";
	}
}
