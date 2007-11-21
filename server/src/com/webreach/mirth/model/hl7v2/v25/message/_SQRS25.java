package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SQRS25 extends Message{	
	public _SQRS25(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _SCH.class, _TQ1.class, _NTE.class, _PID.class, _PV1.class, _PV2.class, _DG1.class, _RGS.class, _AIS.class, _NTE.class, _AIG.class, _NTE.class, _AIP.class, _NTE.class, _AIL.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, -1, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, -1, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true, false, false, true, false, false, false, true, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{8, 11, 0, 0}, {13, 14, 0, 1}, {15, 16, 0, 1}, {17, 18, 0, 1}, {19, 20, 0, 1}, {12, 20, 1, 1}, {5, 20, 0, 1}}; 
		description = "Schedule Query Message and Response";
		name = "SQRS25";
	}
}
