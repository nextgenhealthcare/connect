package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYQ26 extends Message{	
	public _QRYQ26(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Pharmacy/Treatment Order Query";
		name = "QRYQ26";
	}
}
