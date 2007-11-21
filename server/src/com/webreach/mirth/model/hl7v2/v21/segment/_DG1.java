package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DG1 extends Segment {
	public _DG1(){
		fields = new Class[]{_SI.class, _ID.class, _ID.class, _ST.class, _TS.class, _ID.class, _ST.class, _ID.class, _ID.class, _ID.class, _ID.class, _NM.class, _NM.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Diagnosis Coding Method", "Diagnosis Code", "Diagnosis Description", "Diagnosis Date/Time", "Diagnosis/Drg Type", "Major Diagnostic Category", "Diagnostic Related Group", "Drg Approval Indicator", "Drg Grouper Review Code", "Outlier Type", "Outlier Days", "Outlier Cost", "Grouper Version and Type"};
		description = "Diagnosis";
		name = "DG1";
	}
}
