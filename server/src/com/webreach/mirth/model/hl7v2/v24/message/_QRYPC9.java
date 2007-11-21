package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYPC9 extends Message{	
	public _QRYPC9(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "PC/ Goal Query";
		name = "QRYPC9";
	}
}
