package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYQ05 extends Message{	
	public _QRYQ05(){
		segments = new Class[]{_MSH.class, _URD.class, _URS.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true};
		groups = new int[][]{}; 
		description = "No Description.";
		name = "QRYQ05";
	}
}
