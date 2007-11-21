package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _GP1 extends Segment {
	public _GP1(){
		fields = new Class[]{_IS.class, _IS.class, _IS.class, _IS.class, _CP.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Type of Bill Code", "Revenue Code", "Overall Claim Disposition Code", "OCE Edits per Visit Code", "Outlier Cost"};
		description = "Grouping/Reimbursement - Visit";
		name = "GP1";
	}
}
