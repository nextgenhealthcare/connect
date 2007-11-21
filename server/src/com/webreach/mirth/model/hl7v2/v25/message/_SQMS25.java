package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SQMS25 extends Message{	
	public _SQMS25(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _ARQ.class, _APR.class, _PID.class, _RGS.class, _AIS.class, _APR.class, _AIG.class, _APR.class, _AIP.class, _APR.class, _AIL.class, _APR.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, false, false, true, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{8, 9, 0, 1}, {10, 11, 0, 1}, {12, 13, 0, 1}, {14, 15, 0, 1}, {7, 15, 1, 1}, {4, 15, 0, 0}}; 
		description = "Schedule Query Message and Response";
		name = "SQMS25";
	}
}
