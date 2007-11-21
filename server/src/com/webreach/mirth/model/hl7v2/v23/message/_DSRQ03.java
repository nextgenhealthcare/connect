package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSRQ03 extends Message{	
	public _DSRQ03(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _QRF.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, false, false, false, true, false, true, false};
		groups = new int[][]{}; 
		description = "Deferred Response to a query (Later, Perhaps More Than Once) (B to A)";
		name = "DSRQ03";
	}
}
