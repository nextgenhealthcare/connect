package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OBR extends Segment {
	public _OBR(){
		fields = new Class[]{_SI.class, _CM.class, _CM.class, _CE.class, _ST.class, _TS.class, _TS.class, _TS.class, _CQ.class, _CN.class, _ST.class, _CM.class, _ST.class, _TS.class, _CM.class, _CN.class, _TN.class, _ST.class, _ST.class, _ST.class, _ST.class, _TS.class, _CM.class, _ID.class, _ID.class, _CE.class, _CM.class, _CN.class, _CM.class, _ID.class, _CE.class, _CN.class, _CN.class, _CN.class, _CN.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 5, 0, 0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Placer Orders #", "Fillers Order #", "Universal Service ID", "Priority", "Requested Date/Time", "Observation Date/Time", "Observation End Date/Time", "Collection Volume", "Collector Identifier", "Specimen Action Code", "Danger Code", "Relevant Clinical Info.", "Specimen Rcvd Date/Time", "Specimen Source", "Ordering Provider", "Order Call Back Phone Num", "Placers Field #1", "Placers Field #2", "Fillers Field #1", "Fillers Field #2", "Results Rpt/Status Chg-Dt", "Charge to Practice", "Diagnostic Serv Sect ID", "Result Status", "Linked Results", "Quantity/Timing", "Result Copies To", "Parent Accession #", "Transportation Mode", "Reason For Study", "Prin Result Interpreter", "Asst Result Interpreter", "Technician", "Transcriptionist", "Scheduled Date/Time"};
		description = "Observation Request";
		name = "OBR";
	}
}
