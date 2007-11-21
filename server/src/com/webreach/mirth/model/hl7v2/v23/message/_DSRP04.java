package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSRP04 extends Message{	
	public _DSRP04(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _QRF.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, false, false, false, true, false, true, false};
		groups = new int[][]{}; 
		description = "Generate Bill and A/R Statements Response";
		name = "DSRP04";
	}
}
