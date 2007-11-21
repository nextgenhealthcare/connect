package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCR extends Segment {
	public _PCR(){
		fields = new Class[]{_CE.class, _IS.class, _CE.class, _CQ.class, _TS.class, _TS.class, _TS.class, _TS.class, _IS.class, _CE.class, _IS.class, _ST.class, _IS.class, _CE.class, _CE.class, _CE.class, _ID.class, _TS.class, _ID.class, _ID.class, _ID.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 6, 6, 3};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Implicated Product", "Generic Product", "Product Class", "Total Duration of Therapy", "Product Manufacture Date", "Product Expiration Date", "Product Implantation Date", "Product Explantation Date", "Single Use Device", "Indication For Product Use", "Product Problem", "Product Serial/Lot Number", "Product Available For Inspection", "Product Evaluation Performed", "Product Evaluation Status", "Product Evaluation Results", "Evaluated Product Source", "Date Product Returned to Manufacturer", "Device Operator Qualifications", "Relatedness Assessment", "Action Taken In Response to the Event", "Event Causality Observations", "Indirect Exposure Mechanism"};
		description = "Possible Causal Relationship";
		name = "PCR";
	}
}
