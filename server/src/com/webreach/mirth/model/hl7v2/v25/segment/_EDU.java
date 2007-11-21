package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EDU extends Segment {
	public _EDU(){
		fields = new Class[]{_SI.class, _IS.class, _DR.class, _DR.class, _DT.class, _XON.class, _CE.class, _XAD.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Academic Degree", "Academic Degree Program Date Range", "Academic Degree Program Participation Date Range", "Academic Degree Granted Date", "School", "School Type Code", "School Address", "Major Field of Study"};
		description = "Educational Detail";
		name = "EDU";
	}
}
