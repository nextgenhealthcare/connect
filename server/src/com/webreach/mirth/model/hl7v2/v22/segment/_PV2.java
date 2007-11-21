package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PV2 extends Segment {
	public _PV2(){
		fields = new Class[]{_CM.class, _CE.class, _CE.class, _CE.class, _ST.class, _ST.class, _ID.class, _DT.class, _DT.class, _ST.class, _ID.class, _ID.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Prior Pending Location", "Accommodation Code", "Admit Reason", "Transfer Reason", "Patient Valuables", "Patient Valuables Location", "Visit User Code", "Expected Admit Date", "Expected Discharge Date", "Birth Place", "Multiple Birth Indicator", "Birth Order", "Citizenship", "Veterans Military Status"};
		description = "Patient Visit - Additional Information";
		name = "PV2";
	}
}
