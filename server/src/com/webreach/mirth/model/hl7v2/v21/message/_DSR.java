package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DSR extends Message{	
	public _DSR(){
		segments = new Class[]{_MSH.class, _MSA.class, _QRD.class, _QRF.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, true};
		groups = new int[][]{}; 
		description = "No Description.";
		name = "DSR";
	}
}
