package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FT1 extends Segment {
	public _FT1(){
		fields = new Class[]{_SI.class, _ST.class, _ST.class, _DR.class, _TS.class, _IS.class, _CE.class, _ST.class, _ST.class, _NM.class, _CP.class, _CP.class, _CE.class, _CE.class, _CP.class, _PL.class, _IS.class, _IS.class, _CE.class, _XCN.class, _XCN.class, _CP.class, _EI.class, _XCN.class, _CE.class, _CE.class, _CE.class, _CWE.class, _CNE.class, _CX.class, _SI.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, -1, 0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Transaction ID", "Transaction Batch ID", "Transaction Date", "Transaction Posting Date", "Transaction Type", "Transaction Code", "Transaction Description", "Transaction Description - Alternative", "Transaction Quantity", "Transaction Amount - Extended", "Transaction Amount - Unit", "Department Code", "Insurance Plan ID", "Insurance Amount", "Assigned Patient Location", "Fee Schedule", "Patient Type", "Diagnosis Code", "Performed by Code", "Ordered by Code", "Unit Cost", "Filler Order Number", "Entered by Code", "Procedure Code", "Procedure Code Modifier", "Advanced Beneficiary Notice Code", "Medically Necessary Duplicate Procedure Reason.", "NDC Code", "Payment Reference ID", "Transaction Reference Key"};
		description = "Financial Transaction";
		name = "FT1";
	}
}
