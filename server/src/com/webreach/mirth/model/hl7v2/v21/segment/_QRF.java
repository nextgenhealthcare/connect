package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRF extends Segment {
	public _QRF(){
		fields = new Class[]{_ST.class, _TS.class, _TS.class, _ST.class, _ST.class};
		repeats = new int[]{-1, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Where Subject Filter", "When Data Start Date/Time", "When Data End Date/Time", "What User Qualifier", "Other Qry Subject Filter"};
		description = "Original Style Query Filter";
		name = "QRF";
	}
}
