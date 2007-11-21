package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RF1 extends Segment {
	public _RF1(){
		fields = new Class[]{_CE.class, _CE.class, _CE.class, _CE.class, _CE.class, _EI.class, _TS.class, _TS.class, _TS.class, _CE.class, _EI.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Referral Status", "Referral Priority", "Referral Type", "Referral Disposition", "Referral Category", "Originating Referral Identifier", "Effective Date", "Expiration Date", "Process Date", "Referral Reason", "External Referral Identifier"};
		description = "Referral Information";
		name = "RF1";
	}
}
