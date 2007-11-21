package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYA19 extends Message{	
	public _QRYA19(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Patient Query";
		name = "QRYA19";
	}
}
