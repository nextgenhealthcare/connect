package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCB extends Segment {
	public _PCB(){
		fields = new Class[]{_XON.class, _CX.class, _IS.class, _ST.class, _DT.class, _DT.class, _DT.class, _ST.class, _ST.class, _ST.class, _DT.class, _DT.class, _ST.class, _PPN.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Issuer", "External ID", "Board Certification", "Certificate Number", "Original Effective Date", "Expiration Date", "Last Re-certification Date", "Is not Specialty", "Is Eligible", "Is Certified", "Board Taken Date", "Board Scheduled Date", "Comment", "Revised By", "Standing"};
		description = "Board Certification Segment";
		name = "PCB";
	}
}
