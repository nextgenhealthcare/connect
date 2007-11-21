package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYQ01 extends Message{	
	public _QRYQ01(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, true};
		groups = new int[][]{}; 
		description = "Query For Immediate Response";
		name = "QRYQ01";
	}
}
