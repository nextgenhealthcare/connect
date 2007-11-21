package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DG1 extends Segment {
	public _DG1(){
		fields = new Class[]{_SI.class, _ID.class, _CE.class, _ST.class, _TS.class, _IS.class, _CE.class, _CE.class, _ID.class, _IS.class, _CE.class, _NM.class, _CP.class, _ST.class, _ID.class, _XCN.class, _IS.class, _ID.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Diagnosis Coding Method", "Diagnosis Code", "Diagnosis Description", "Diagnosis Date/Time", "Diagnosis Type", "Major Diagnostic Category", "Diagnostic Related Group", "Drg Approval Indicator", "Drg Grouper Review Code", "Outlier Type", "Outlier Days", "Outlier Cost", "Grouper Version and Type", "Diagnosis Priority", "Diagnosing Clinician", "Diagnosis Classification", "Confidential Indicator", "Attestation Date/Time"};
		description = "Diagnosis";
		name = "DG1";
	}
}
