package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UDMQ05 extends Message{	
	public _UDMQ05(){
		segments = new Class[]{_MSH.class, _URD.class, _URS.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, false};
		groups = new int[][]{}; 
		description = "Unsolicited Display Update Message";
		name = "UDMQ05";
	}
}
