package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSRQ01 extends Message{	
	public _DSRQ01(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _QRF.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, false, true, false, true, false};
		groups = new int[][]{}; 
		description = "Query Sent For Immediate Response";
		name = "DSRQ01";
	}
}
