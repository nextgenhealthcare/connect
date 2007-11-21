package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCM extends Segment {
	public _PCM(){
		fields = new Class[]{_XON.class, _CX.class, _XON.class, _ST.class, _ST.class, _DR.class, _DT.class, _MO.class, _MO.class, _MO.class, _ST.class, _DT.class, _XAD.class, _XTN.class, _PPN.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Issuer", "External ID", "Insuree", "Policy Number", "Insurance Type", "Effective Dates", "Retroactive Date", "Aggregate Limit", "Claim Limit", "Umbrella Limit", "Is Certificate Holder", "Year With", "Address", "Phone", "Revised By", "Comment", "Standing"};
		description = "Malpractice Information Segment";
		name = "PCM";
	}
}
