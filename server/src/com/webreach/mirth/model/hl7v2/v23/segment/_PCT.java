package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCT extends Segment {
	public _PCT(){
		fields = new Class[]{_XON.class, _CX.class, _XAD.class, _XTN.class, _XTN.class, _ST.class, _ST.class, _IS.class, _IS.class, _DT.class, _DT.class, _ST.class, _DT.class, _DT.class, _PPN.class, _ST.class, _IS.class, _NM.class};
		repeats = new int[]{0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"School", "External ID", "Address", "Phone", "Fax", "Contact", "AMA School Code", "Education Type", "Education Category", "Enter Date", "Graduation Date", "ECFMG Code", "ECFMG Effective Date", "ECFMG Expiration Date", "Revised By", "Comment", "Standing", "Completed"};
		description = "Training Information Segment";
		name = "PCT";
	}
}
