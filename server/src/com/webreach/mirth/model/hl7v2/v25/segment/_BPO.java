package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BPO extends Segment {
	public _BPO(){
		fields = new Class[]{_SI.class, _CWE.class, _CWE.class, _NM.class, _NM.class, _CE.class, _TS.class, _PL.class, _XAD.class, _TS.class, _PL.class, _XAD.class, _CWE.class, _ID.class};
		repeats = new int[]{0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Universal Service ID", "Processing Requirements", "Quantity", "Amount", "Units", "Intended Use Date/Time", "Intended Dispense From Location", "Intended Dispense From Address", "Requested Dispense Date/Time", "Requested Dispense to Location", "Requested Dispense to Address", "Indication For Use", "Informed Consent Indicator"};
		description = "Blood Product Order";
		name = "BPO";
	}
}
