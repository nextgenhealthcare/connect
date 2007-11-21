package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OBR extends Segment {
	public _OBR(){
		fields = new Class[]{_SI.class, _EI.class, _EI.class, _CE.class, _ID.class, _TS.class, _TS.class, _TS.class, _CQ.class, _XCN.class, _ID.class, _CE.class, _ST.class, _TS.class, _SPS.class, _XCN.class, _XTN.class, _ST.class, _ST.class, _ST.class, _ST.class, _TS.class, _MOC.class, _ID.class, _ID.class, _PRL.class, _TQ.class, _XCN.class, _EIP.class, _ID.class, _CE.class, _NDL.class, _NDL.class, _NDL.class, _NDL.class, _TS.class, _NM.class, _CE.class, _CE.class, _CE.class, _ID.class, _ID.class, _CE.class, _CE.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Placer Order Number", "Filler Order Number", "Universal Service Identifier", "Priority", "Requested Date/Time", "Observation Date/Time #", "Observation End Date/Time #", "Collection Volume *", "Collector Identifier *", "Specimen Action Code *", "Danger Code", "Relevant Clinical Info.", "Specimen Received Date/Time *", "Specimen Source", "Ordering Provider", "Order Callback Phone Number", "Placer Field 1", "Placer Field 2", "Filler Field 1 +", "Filler Field 2 +", "Results Rpt/Status Chng - Date/Time +", "Charge to Practice +", "Diagnostic Serv Sect ID", "Result Status +", "Parent Result +", "Quantity/Timing", "Result Copies To", "Parent", "Transportation Mode", "Reason for Study", "Principal Result Interpreter +", "Assistant Result Interpreter +", "Technician +", "Transcriptionist +", "Scheduled Date/Time +", "Number of Sample Containers *", "Transport Logistics of Collected Sample *", "Collector's Comment *", "Transport Arrangement Responsibility", "Transport Arranged", "Escort Required", "Planned Patient Transport Comment", "Procedure Code", "Procedure Code Modifier", "Placer Supplemental Service Information", "Filler Supplemental Service Information"};
		description = "Observation Request";
		name = "OBR";
	}
}
