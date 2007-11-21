package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VXQV01 extends Message{	
	public _VXQV01(){
		segments = new Class[]{_MSH.class, _SFT.class, _QRD.class, _QRF.class};
		repeats = new int[]{0, -1, 0, 0};
		required = new boolean[]{true, false, true, false};
		groups = new int[][]{}; 
		description = "Query For Vaccination Record";
		name = "VXQV01";
	}
}
