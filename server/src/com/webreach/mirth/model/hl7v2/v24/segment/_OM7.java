package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM7 extends Segment {
	public _OM7(){
		fields = new Class[]{_NM.class, _CE.class, _CE.class, _TX.class, _ST.class, _TS.class, _TS.class, _NM.class, _CE.class, _IS.class, _ID.class, _CE.class, _TS.class, _TS.class, _NM.class, _CE.class, _NM.class, _CE.class, _TS.class, _XCN.class, _PL.class, _IS.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/ Observation Master File", "Universal Service Identifier", "Category Identifier", "Category Description", "Category Synonym", "Effective Test/Service Start Date/Time", "Effective Test/Service End Date/Time", "Test/Service Default Duration Quantity", "Test/Service Default Duration Units", "Test/Service Default Frequency", "Consent Indicator", "Consent Identifier", "Consent Effective Start Date/Time", "Consent Effective End Date/Time", "Consent Interval Quantity", "Consent Interval Units", "Consent Waiting Period Quantity", "Consent Waiting Period Units", "Effective Date/Time of Change", "Entered By", "Orderable-at Location", "Formulary Status", "Special Order Indicator", "Primary Key Value"};
		description = "Additional Basic Attributes";
		name = "OM7";
	}
}
