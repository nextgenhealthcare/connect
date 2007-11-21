package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SRMS08 extends Message{	
	public _SRMS08(){
		segments = new Class[]{_MSH.class, _ARQ.class, _APR.class, _NTE.class, _PID.class, _PV1.class, _PV2.class, _OBX.class, _DG1.class, _RGS.class, _AIS.class, _APR.class, _NTE.class, _AIG.class, _APR.class, _NTE.class, _AIL.class, _APR.class, _NTE.class, _AIP.class, _APR.class, _NTE.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, -1, -1, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, true, false, false, true, false, false, false, false, true, true, false, false, true, false, false, true, false, false, true, false, false};
		groups = new int[][]{{5, 9, 0, 1}, {11, 13, 0, 1}, {14, 16, 0, 1}, {17, 19, 0, 1}, {20, 22, 0, 1}, {10, 22, 1, 1}}; 
		description = "Request Modification of Service/Resource On Appointment";
		name = "SRMS08";
	}
}
