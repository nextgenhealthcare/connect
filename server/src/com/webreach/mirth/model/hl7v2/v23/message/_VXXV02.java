package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VXXV02 extends Message{	
	public _VXXV02(){
		segments = new Class[]{_MSH.class, _MSA.class, _QRD.class, _QRF.class, _PID.class, _NK1.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, false, true, false};
		groups = new int[][]{{5, 6, 1, 1}}; 
		description = "Response to Vaccination Query Returning Multiple PID Matches";
		name = "VXXV02";
	}
}
