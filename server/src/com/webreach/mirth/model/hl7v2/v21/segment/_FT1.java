package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FT1 extends Segment {
	public _FT1(){
		fields = new Class[]{_SI.class, _ST.class, _ST.class, _DT.class, _DT.class, _ID.class, _ID.class, _ST.class, _ST.class, _NM.class, _NM.class, _NM.class, _ST.class, _ID.class, _NM.class, _ST.class, _ID.class, _ID.class, _ID.class, _CN.class, _CN.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Transaction ID", "Transaction Batch ID", "Transaction Date", "Transaction Posting Date", "Transaction Type", "Transaction Code", "Transaction Description", "Transaction Description - Alternative", "Transaction Quantity", "Transaction Amount - Ext.", "Transaction Amount - Unit", "Department Code", "Insurance Plan ID", "Insurance Amount", "Patient Location", "Fee Schedule", "Patient Type", "Diagnosis Code", "Performed by Code", "Ordered by Code", "Unit Cost"};
		description = "Financial Transaction";
		name = "FT1";
	}
}
