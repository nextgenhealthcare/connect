package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SIUS22 extends Message{	
	public _SIUS22(){
		segments = new Class[]{_MSH.class, _SCH.class, _TQ1.class, _NTE.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _OBX.class, _DG1.class, _RGS.class, _AIS.class, _NTE.class, _AIG.class, _NTE.class, _AIL.class, _NTE.class, _AIP.class, _NTE.class};
		repeats = new int[]{0, 0, -1, -1, 0, 0, 0, 0, -1, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, false, true, false, false, false, false, false, true, true, false, true, false, true, false, true, false};
		groups = new int[][]{{5, 10, 0, 1}, {12, 13, 0, 1}, {14, 15, 0, 1}, {16, 17, 0, 1}, {18, 19, 0, 1}, {11, 19, 1, 1}}; 
		description = "Notification of Deletion of Service/Resource on Appointment";
		name = "SIUS22";
	}
}
