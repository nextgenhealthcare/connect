package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _GP2 extends Segment {
	public _GP2(){
		fields = new Class[]{_IS.class, _NM.class, _CP.class, _IS.class, _IS.class, _IS.class, _CE.class, _IS.class, _IS.class, _IS.class, _CP.class, _IS.class, _CP.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Revenue Code", "Number of Service Units", "Charge", "Reimbursement Action Code", "Denial or Rejection Code", "OCE Edit Code", "Ambulatory Payment Classification Code", "Modifier Edit Code", "Payment Adjustment Code", "Packaging Status Code", "Expected HCFA Payment Amount", "Reimbursement Type Code", "Co-Pay Amount", "Pay Rate per Unit"};
		description = "Grouping/Reimbursement - Procedure Line Item";
		name = "GP2";
	}
}
