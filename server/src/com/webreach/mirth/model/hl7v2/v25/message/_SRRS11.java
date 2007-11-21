package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SRRS11 extends Message{	
	public _SRRS11(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SCH.class, _TQ1.class, _NTE.class, _PID.class, _PV1.class, _PV2.class, _DG1.class, _RGS.class, _AIS.class, _NTE.class, _AIG.class, _NTE.class, _AIL.class, _NTE.class, _AIP.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, -1, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, true, false, false, true, false, false, false, true, true, false, true, false, true, false, true, false};
		groups = new int[][]{{7, 10, 0, 1}, {12, 13, 0, 1}, {14, 15, 0, 1}, {16, 17, 0, 1}, {18, 19, 0, 1}, {11, 19, 1, 1}, {4, 19, 0, 0}}; 
		description = "Request Deletion of Service/Resource on Appointment - Response";
		name = "SRRS11";
	}
}
