package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYP04 extends Message{	
	public _QRYP04(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Generate Bill and A/R Statements";
		name = "QRYP04";
	}
}
