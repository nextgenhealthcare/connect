package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CM0 extends Segment {
	public _CM0(){
		fields = new Class[]{_SI.class, _EI.class, _EI.class, _ST.class, _XCN.class, _DT.class, _NM.class, _DT.class, _XCN.class, _XTN.class, _XAD.class};
		repeats = new int[]{0, 0, -1, 0, -1, 0, 0, 0, -1, 0, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Sponsor Study ID", "Alternate Study ID", "Title of Study", "Chairman of Study", "Last Irb Approval Date", "Total Accrual to Date", "Last Accrual Date", "Contact For Study", "Contact's Telephone Number", "Contact's Address"};
		description = "Clinical Study Master";
		name = "CM0";
	}
}
