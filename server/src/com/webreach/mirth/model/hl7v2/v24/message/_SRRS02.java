package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SRRS02 extends Message{	
	public _SRRS02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SCH.class, _NTE.class, _PID.class, _PV1.class, _PV2.class, _DG1.class, _RGS.class, _AIS.class, _NTE.class, _AIG.class, _NTE.class, _AIL.class, _NTE.class, _AIP.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, true, false, true, false, false, false, true, true, false, true, false, true, false, true, false};
		groups = new int[][]{{6, 9, 0, 1}, {11, 12, 0, 1}, {13, 14, 0, 1}, {15, 16, 0, 1}, {17, 18, 0, 1}, {10, 18, 1, 1}, {4, 18, 0, 0}}; 
		description = "Request Appointment Rescheduling - Response";
		name = "SRRS02";
	}
}
